package com.example.reglamentoupp;

import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.cloudinary.android.MediaManager;
import com.cloudinary.android.callback.ErrorInfo;
import com.cloudinary.android.callback.UploadCallback;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class ProfileActivity extends AppCompatActivity {

    private EditText etNombre, etApellidos, etTelefono, etNewPassword, etConfirmNewPassword;
    private ImageView ivProfilePhoto;
    private ImageView logroDerechos, logroObligaciones, logroProhibiciones, logroSanciones, logroReconocimientos;
    private ProgressBar progressBar;

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private FirebaseUser currentUser;

    private ActivityResultLauncher<String> galleryLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        currentUser = mAuth.getCurrentUser();

        vincularVistas();
        cargarDatosUsuario();

        // Botón retroceder
        findViewById(R.id.btnBack).setOnClickListener(v -> finish());

        // Configuración para abrir la galería y cambiar foto
        galleryLauncher = registerForActivityResult(
                new ActivityResultContracts.GetContent(),
                uri -> {
                    if (uri != null) {
                        ivProfilePhoto.setImageURI(uri);
                        subirFotoACloudinary(uri);
                    }
                }
        );
        findViewById(R.id.cardChangePhoto).setOnClickListener(v -> galleryLauncher.launch("image/*"));

        // Guardar cambios del Perfil
        findViewById(R.id.btnSaveProfile).setOnClickListener(v -> actualizarDatosPersonales());

        // Actualizar Contraseña
        findViewById(R.id.btnUpdatePassword).setOnClickListener(v -> actualizarContraseña());
    }

    private void vincularVistas() {
        etNombre = findViewById(R.id.etEditNombre);
        etApellidos = findViewById(R.id.etEditApellidos);
        etTelefono = findViewById(R.id.etEditTelefono);
        etNewPassword = findViewById(R.id.etNewPassword);
        etConfirmNewPassword = findViewById(R.id.etConfirmNewPassword); // Vinculamos el nuevo campo
        ivProfilePhoto = findViewById(R.id.ivProfilePhoto);
        progressBar = findViewById(R.id.progressProfile);

        logroDerechos = findViewById(R.id.logroDerechos);
        logroObligaciones = findViewById(R.id.logroObligaciones);
        logroProhibiciones = findViewById(R.id.logroProhibiciones);
        logroSanciones = findViewById(R.id.logroSanciones);
        logroReconocimientos = findViewById(R.id.logroReconocimientos);
    }

    private void cargarDatosUsuario() {
        if (currentUser == null) return;
        progressBar.setVisibility(View.VISIBLE);

        db.collection("usuarios").document(currentUser.getUid()).get()
                .addOnSuccessListener(document -> {
                    progressBar.setVisibility(View.GONE);
                    if (document.exists()) {
                        etNombre.setText(document.getString("nombre"));
                        etApellidos.setText(document.getString("apellidos"));
                        etTelefono.setText(document.getString("telefono"));

                        String fotoUrl = document.getString("fotoUrl");
                        if (fotoUrl != null && !fotoUrl.isEmpty()) {
                            Glide.with(this).load(fotoUrl).centerCrop().into(ivProfilePhoto);
                        }

                        Long puntaje = document.getLong("puntaje");
                        if (puntaje != null) {
                            iluminarLogros(puntaje);
                        }
                    }
                }).addOnFailureListener(e -> progressBar.setVisibility(View.GONE));
    }

    private void actualizarDatosPersonales() {
        if (currentUser == null) return;

        String nombre = etNombre.getText().toString().trim();
        String apellidos = etApellidos.getText().toString().trim();
        String telefono = etTelefono.getText().toString().trim();

        if (nombre.isEmpty() || apellidos.isEmpty()) {
            Toast.makeText(this, "Nombre y Apellidos son obligatorios", Toast.LENGTH_SHORT).show();
            return;
        }

        progressBar.setVisibility(View.VISIBLE);
        Map<String, Object> updates = new HashMap<>();
        updates.put("nombre", nombre);
        updates.put("apellidos", apellidos);
        updates.put("telefono", telefono);

        db.collection("usuarios").document(currentUser.getUid()).update(updates)
                .addOnSuccessListener(aVoid -> {
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(this, "Perfil actualizado con éxito", Toast.LENGTH_SHORT).show();
                }).addOnFailureListener(e -> {
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(this, "Error al actualizar perfil", Toast.LENGTH_SHORT).show();
                });
    }

    private void actualizarContraseña() {
        if (currentUser == null) return;
        String newPass = etNewPassword.getText().toString().trim();
        String confirmPass = etConfirmNewPassword.getText().toString().trim();

        // Validaciones
        if (newPass.isEmpty()) {
            etNewPassword.setError("Ingresa una nueva contraseña");
            return;
        }

        if (newPass.length() < 6) {
            etNewPassword.setError("Mínimo 6 caracteres");
            return;
        }

        if (!newPass.equals(confirmPass)) {
            etConfirmNewPassword.setError("Las contraseñas no coinciden");
            return;
        }

        progressBar.setVisibility(View.VISIBLE);
        currentUser.updatePassword(newPass)
                .addOnCompleteListener(task -> {
                    progressBar.setVisibility(View.GONE);
                    if (task.isSuccessful()) {
                        Toast.makeText(this, "Contraseña actualizada. Usa tu nueva clave en tu próximo inicio.", Toast.LENGTH_LONG).show();
                        etNewPassword.setText("");
                        etConfirmNewPassword.setText("");
                    } else {
                        Toast.makeText(this, "Por seguridad, cierra sesión y vuelve a entrar para cambiar tu clave.", Toast.LENGTH_LONG).show();
                    }
                });
    }

    private void subirFotoACloudinary(Uri imageUri) {
        progressBar.setVisibility(View.VISIBLE);
        Toast.makeText(this, "Subiendo foto...", Toast.LENGTH_SHORT).show();

        MediaManager.get().upload(imageUri).unsigned("mi_app_preset")
                .callback(new UploadCallback() {
                    @Override public void onStart(String requestId) {}
                    @Override public void onProgress(String requestId, long bytes, long totalBytes) {}
                    @Override public void onReschedule(String requestId, ErrorInfo error) {}

                    @Override
                    public void onSuccess(String requestId, Map resultData) {
                        String newUrl = (String) resultData.get("secure_url");
                        db.collection("usuarios").document(currentUser.getUid())
                                .update("fotoUrl", newUrl)
                                .addOnSuccessListener(aVoid -> {
                                    progressBar.setVisibility(View.GONE);
                                    Toast.makeText(ProfileActivity.this, "Foto guardada correctamente", Toast.LENGTH_SHORT).show();
                                });
                    }

                    @Override
                    public void onError(String requestId, ErrorInfo error) {
                        progressBar.setVisibility(View.GONE);
                        Toast.makeText(ProfileActivity.this, "Error al subir foto", Toast.LENGTH_SHORT).show();
                    }
                }).dispatch();
    }

    private void iluminarLogros(long puntaje) {
        // Colorea los iconos dependiendo del puntaje actual del usuario
        if (puntaje >= 0) logroDerechos.setColorFilter(Color.parseColor("#448AFF")); // Azul
        if (puntaje >= 50) logroObligaciones.setColorFilter(Color.parseColor("#FF9800")); // Naranja
        if (puntaje >= 100) logroProhibiciones.setColorFilter(Color.parseColor("#E91E63")); // Rosa
        if (puntaje >= 150) logroSanciones.setColorFilter(Color.parseColor("#9C27B0")); // Morado
        if (puntaje >= 200) logroReconocimientos.setColorFilter(Color.parseColor("#4CAF50")); // Verde
    }
}
package com.example.reglamentoupp;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.HashMap;
import java.util.Map;

public class ProfileActivity extends AppCompatActivity {

    private ImageView ivProfilePic;
    private TextView tvProfileName, tvProfileEmail, tvProfileLevel, tvProfileScore, tvProgressText;
    private ProgressBar pbProfileLevel, pbLoading;

    // Botones con ID únicos y seguros
    private MaterialButton btnLogout, btnSaveProfile, btnCancelEdit, btnActionEdit, btnChangeProfilePic;

    // Contenedores
    private LinearLayout layoutViewMode, layoutEditMode, layoutActionButtons;

    // Campos de texto de edición
    private TextInputEditText etEditName, etEditEmail, etEditNewPassword, etEditCurrentPassword;

    // Insignias
    private ImageView ivBadgeDerechos, ivBadgeObligaciones, ivBadgeProhibiciones, ivBadgeSanciones, ivBadgeReconocimientos;

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private FirebaseUser currentUser;

    private boolean isEditing = false;
    private String currentName = "";
    private String currentEmail = "";

    // URI para la nueva imagen seleccionada
    private Uri imageUri = null;

    // Lanzador para seleccionar imagen de la galería
    private final ActivityResultLauncher<String> imagePickerLauncher = registerForActivityResult(
            new ActivityResultContracts.GetContent(),
            uri -> {
                if (uri != null) {
                    imageUri = uri;
                    // Previsualización inmediata de la imagen elegida
                    Glide.with(this).load(imageUri).centerCrop().into(ivProfilePic);
                }
            }
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        currentUser = mAuth.getCurrentUser();

        vincularVistas();
        cargarDatosUsuario();

        // Listeners principales
        btnLogout.setOnClickListener(v -> cerrarSesion());
        findViewById(R.id.btnBackProfile).setOnClickListener(v -> finish());

        // Listeners de edición
        btnActionEdit.setOnClickListener(v -> toggleEditMode());
        btnCancelEdit.setOnClickListener(v -> toggleEditMode());
        btnSaveProfile.setOnClickListener(v -> validarYGuardarCambios());

        // Listener para abrir galería
        btnChangeProfilePic.setOnClickListener(v -> imagePickerLauncher.launch("image/*"));
    }

    private void vincularVistas() {
        ivProfilePic = findViewById(R.id.ivProfilePic);
        tvProfileName = findViewById(R.id.tvProfileName);
        tvProfileEmail = findViewById(R.id.tvProfileEmail);
        tvProfileLevel = findViewById(R.id.tvProfileLevel);
        tvProfileScore = findViewById(R.id.tvProfileScore);
        tvProgressText = findViewById(R.id.tvProgressText);
        pbProfileLevel = findViewById(R.id.pbProfileLevel);
        pbLoading = findViewById(R.id.pbLoading);

        btnLogout = findViewById(R.id.btnLogoutProfile);
        btnActionEdit = findViewById(R.id.btnActionEditProfile);
        btnChangeProfilePic = findViewById(R.id.btnChangeProfilePic);

        btnSaveProfile = findViewById(R.id.btnSaveProfile);
        btnCancelEdit = findViewById(R.id.btnCancelEdit);

        layoutViewMode = findViewById(R.id.layoutViewMode);
        layoutEditMode = findViewById(R.id.layoutEditMode);
        layoutActionButtons = findViewById(R.id.layoutActionButtons);

        etEditName = findViewById(R.id.etEditName);
        etEditEmail = findViewById(R.id.etEditEmail);
        etEditNewPassword = findViewById(R.id.etEditNewPassword);
        etEditCurrentPassword = findViewById(R.id.etEditCurrentPassword);

        // Insignias
        ivBadgeDerechos = findViewById(R.id.ivBadgeDerechos);
        ivBadgeObligaciones = findViewById(R.id.ivBadgeObligaciones);
        ivBadgeProhibiciones = findViewById(R.id.ivBadgeProhibiciones);
        ivBadgeSanciones = findViewById(R.id.ivBadgeSanciones);
        ivBadgeReconocimientos = findViewById(R.id.ivBadgeReconocimientos);
    }

    private void cargarDatosUsuario() {
        if (currentUser == null) {
            cerrarSesion();
            return;
        }

        currentEmail = currentUser.getEmail();
        tvProfileEmail.setText(currentEmail);
        etEditEmail.setText(currentEmail);

        db.collection("usuarios").document(currentUser.getUid()).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        currentName = documentSnapshot.getString("nombre");
                        Long puntaje = documentSnapshot.getLong("puntaje");
                        String fotoUrl = documentSnapshot.getString("fotoUrl");

                        if (currentName != null) {
                            tvProfileName.setText(currentName);
                            etEditName.setText(currentName);
                        }

                        long xpTotal = (puntaje != null) ? puntaje : 0;
                        actualizarNivel(xpTotal);
                        actualizarInsignias(xpTotal);

                        if (fotoUrl != null && !fotoUrl.isEmpty() && !isDestroyed()) {
                            Glide.with(this)
                                    .load(fotoUrl)
                                    .centerCrop()
                                    .placeholder(R.drawable.ic_profile)
                                    .into(ivProfilePic);
                        }
                    }
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Error al cargar datos", Toast.LENGTH_SHORT).show());
    }

    private void toggleEditMode() {
        isEditing = !isEditing;
        if (isEditing) {
            layoutViewMode.setVisibility(View.GONE);
            layoutActionButtons.setVisibility(View.GONE);
            layoutEditMode.setVisibility(View.VISIBLE);
            btnChangeProfilePic.setVisibility(View.VISIBLE);
        } else {
            layoutViewMode.setVisibility(View.VISIBLE);
            layoutActionButtons.setVisibility(View.VISIBLE);
            layoutEditMode.setVisibility(View.GONE);
            btnChangeProfilePic.setVisibility(View.GONE);

            // Restaurar valores originales si se cancela
            etEditName.setText(currentName);
            etEditEmail.setText(currentEmail);
            etEditNewPassword.setText("");
            etEditCurrentPassword.setText("");
            etEditCurrentPassword.setError(null);

            if (imageUri != null) {
                imageUri = null;
                cargarDatosUsuario(); // Recargar foto desde Firestore
            }
        }
    }

    private void validarYGuardarCambios() {
        String nuevoNombre = etEditName.getText().toString().trim();
        String nuevoCorreo = etEditEmail.getText().toString().trim();
        String nuevaContrasena = etEditNewPassword.getText().toString().trim();
        String contrasenaActual = etEditCurrentPassword.getText().toString().trim();

        if (nuevoNombre.isEmpty() || nuevoCorreo.isEmpty()) {
            Toast.makeText(this, "Nombre y correo son obligatorios.", Toast.LENGTH_SHORT).show();
            return;
        }

        if (contrasenaActual.isEmpty()) {
            etEditCurrentPassword.setError("Requerido para confirmar");
            etEditCurrentPassword.requestFocus();
            return;
        }

        mostrarCarga(true);

        // Re-autenticación obligatoria para cambios críticos
        AuthCredential credential = EmailAuthProvider.getCredential(currentEmail, contrasenaActual);
        currentUser.reauthenticate(credential).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                actualizarDatosFirebase(nuevoNombre, nuevoCorreo, nuevaContrasena);
            } else {
                mostrarCarga(false);
                Toast.makeText(this, "Contraseña actual incorrecta.", Toast.LENGTH_LONG).show();
                etEditCurrentPassword.setError("Error");
            }
        });
    }

    private void actualizarDatosFirebase(String nuevoNombre, String nuevoCorreo, String nuevaContrasena) {
        // Actualizar contraseña si se ingresó una nueva
        if (!nuevaContrasena.isEmpty() && nuevaContrasena.length() >= 6) {
            currentUser.updatePassword(nuevaContrasena);
        }

        // Actualizar correo si cambió
        if (!nuevoCorreo.equals(currentEmail)) {
            currentUser.updateEmail(nuevoCorreo).addOnSuccessListener(aVoid -> {
                currentEmail = nuevoCorreo;
                tvProfileEmail.setText(currentEmail);
            });
        }

        // Si hay una nueva imagen, subirla a Storage
        if (imageUri != null) {
            StorageReference fileRef = FirebaseStorage.getInstance().getReference()
                    .child("perfiles")
                    .child(currentUser.getUid() + ".jpg");

            fileRef.putFile(imageUri).addOnSuccessListener(taskSnapshot -> {
                fileRef.getDownloadUrl().addOnSuccessListener(uri -> {
                    actualizarFirestore(nuevoNombre, uri.toString());
                });
            }).addOnFailureListener(e -> {
                mostrarCarga(false);
                Toast.makeText(this, "Error al subir imagen.", Toast.LENGTH_SHORT).show();
            });
        } else {
            actualizarFirestore(nuevoNombre, null);
        }
    }

    private void actualizarFirestore(String nuevoNombre, String nuevaFotoUrl) {
        Map<String, Object> updates = new HashMap<>();
        updates.put("nombre", nuevoNombre);
        if (nuevaFotoUrl != null) {
            updates.put("fotoUrl", nuevaFotoUrl);
        }

        db.collection("usuarios").document(currentUser.getUid())
                .update(updates)
                .addOnSuccessListener(aVoid -> {
                    mostrarCarga(false);
                    currentName = nuevoNombre;
                    tvProfileName.setText(currentName);
                    imageUri = null;
                    Toast.makeText(this, "Perfil actualizado con éxito.", Toast.LENGTH_LONG).show();
                    toggleEditMode();
                })
                .addOnFailureListener(e -> {
                    mostrarCarga(false);
                    Toast.makeText(this, "Error al guardar en base de datos.", Toast.LENGTH_SHORT).show();
                });
    }

    private void mostrarCarga(boolean isLoading) {
        pbLoading.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        btnSaveProfile.setEnabled(!isLoading);
        btnCancelEdit.setEnabled(!isLoading);
        btnChangeProfilePic.setEnabled(!isLoading);
        etEditName.setEnabled(!isLoading);
        etEditEmail.setEnabled(!isLoading);
    }

    private void actualizarNivel(long xpActual) {
        tvProfileScore.setText(xpActual + " XP");
        long xpParaSiguienteNivel = 50;
        int nivelActual = (int) (xpActual / xpParaSiguienteNivel) + 1;
        long xpEnNivelActual = xpActual % xpParaSiguienteNivel;

        tvProfileLevel.setText("Nivel " + nivelActual);
        tvProgressText.setText(xpEnNivelActual + " / " + xpParaSiguienteNivel + " XP");
        pbProfileLevel.setProgress((int) xpEnNivelActual);
    }

    private void actualizarInsignias(long puntaje) {
        if (ivBadgeDerechos == null) return;
        ivBadgeDerechos.setAlpha(puntaje >= 0 ? 1.0f : 0.25f);
        ivBadgeObligaciones.setAlpha(puntaje >= 50 ? 1.0f : 0.25f);
        ivBadgeProhibiciones.setAlpha(puntaje >= 100 ? 1.0f : 0.25f);
        ivBadgeSanciones.setAlpha(puntaje >= 150 ? 1.0f : 0.25f);
        ivBadgeReconocimientos.setAlpha(puntaje >= 200 ? 1.0f : 0.25f);
    }

    private void cerrarSesion() {
    mAuth.signOut();
    Intent intent = new Intent(ProfileActivity.this, LoginActivity.class);
    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
    startActivity(intent);
    finish();
}
}
package com.example.reglamentoupp;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

public class ProfileActivity extends AppCompatActivity {

    private ImageView ivProfilePic;
    private TextView tvProfileName, tvProfileEmail, tvProfileLevel, tvProfileScore, tvProgressText;
    private ProgressBar pbProfileLevel, pbLoading;
    private MaterialButton btnLogout, btnSaveProfile, btnCancelEdit, btnEditAction;
    private ImageButton btnTopEdit;

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
        btnEditAction.setOnClickListener(v -> toggleEditMode());
        btnTopEdit.setOnClickListener(v -> toggleEditMode());
        btnCancelEdit.setOnClickListener(v -> toggleEditMode());
        btnSaveProfile.setOnClickListener(v -> validarYGuardarCambios());
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
        btnEditAction = findViewById(R.id.btnEditProfile); // Botón debajo del nombre
        btnTopEdit = findViewById(R.id.btnEditProfile); // Ajuste según si quieres que ambos abran (tienen mismo ID o los puedes separar)
        btnTopEdit = findViewById(R.id.topBar).findViewById(R.id.btnEditProfile);

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
            // Activar MODO EDICIÓN
            layoutViewMode.setVisibility(View.GONE);
            layoutActionButtons.setVisibility(View.GONE);
            layoutEditMode.setVisibility(View.VISIBLE);

            // Opcional: Cambiar icono de arriba por una X
            btnTopEdit.setImageResource(R.drawable.ic_prohibiciones);
        } else {
            // Volver a MODO VISTA
            layoutViewMode.setVisibility(View.VISIBLE);
            layoutActionButtons.setVisibility(View.VISIBLE);
            layoutEditMode.setVisibility(View.GONE);

            btnTopEdit.setImageResource(R.drawable.ic_foco); // Restaurar icono

            // Limpiar campos para evitar confusiones
            etEditName.setText(currentName);
            etEditEmail.setText(currentEmail);
            etEditNewPassword.setText("");
            etEditCurrentPassword.setText("");
            etEditCurrentPassword.setError(null);
        }
    }

    private void validarYGuardarCambios() {
        String nuevoNombre = etEditName.getText().toString().trim();
        String nuevoCorreo = etEditEmail.getText().toString().trim();
        String nuevaContrasena = etEditNewPassword.getText().toString().trim();
        String contrasenaActual = etEditCurrentPassword.getText().toString().trim();

        if (nuevoNombre.isEmpty() || nuevoCorreo.isEmpty()) {
            Toast.makeText(this, "Nombre y correo son obligatorios", Toast.LENGTH_SHORT).show();
            return;
        }

        if (contrasenaActual.isEmpty()) {
            etEditCurrentPassword.setError("Ingresa tu contraseña actual por seguridad");
            etEditCurrentPassword.requestFocus();
            return;
        }

        mostrarCarga(true);

        // Re-autenticar al usuario para cambios sensibles
        AuthCredential credential = EmailAuthProvider.getCredential(currentEmail, contrasenaActual);
        currentUser.reauthenticate(credential).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                actualizarDatosFirebase(nuevoNombre, nuevoCorreo, nuevaContrasena);
            } else {
                mostrarCarga(false);
                Toast.makeText(this, "Contraseña actual incorrecta", Toast.LENGTH_LONG).show();
                etEditCurrentPassword.setError("Incorrecta");
            }
        });
    }

    private void actualizarDatosFirebase(String nuevoNombre, String nuevoCorreo, String nuevaContrasena) {
        // Actualizar Contraseña si hay una nueva
        if (!nuevaContrasena.isEmpty() && nuevaContrasena.length() >= 6) {
            currentUser.updatePassword(nuevaContrasena).addOnFailureListener(e ->
                    Toast.makeText(this, "Error al cambiar contraseña", Toast.LENGTH_SHORT).show()
            );
        } else if (!nuevaContrasena.isEmpty() && nuevaContrasena.length() < 6) {
            mostrarCarga(false);
            Toast.makeText(this, "La contraseña requiere mínimo 6 caracteres", Toast.LENGTH_LONG).show();
            return;
        }

        // Actualizar Correo si cambió
        if (!nuevoCorreo.equals(currentEmail)) {
            currentUser.updateEmail(nuevoCorreo).addOnSuccessListener(aVoid -> {
                currentEmail = nuevoCorreo;
                tvProfileEmail.setText(currentEmail);
            }).addOnFailureListener(e ->
                    Toast.makeText(this, "Correo inválido o ya en uso", Toast.LENGTH_SHORT).show()
            );
        }

        // Actualizar Nombre en la Base de Datos
        db.collection("usuarios").document(currentUser.getUid())
                .update("nombre", nuevoNombre)
                .addOnSuccessListener(aVoid -> {
                    mostrarCarga(false);
                    currentName = nuevoNombre;
                    tvProfileName.setText(currentName);
                    Toast.makeText(this, "¡Perfil actualizado con éxito!", Toast.LENGTH_LONG).show();
                    toggleEditMode(); // Salir del modo edición
                })
                .addOnFailureListener(e -> {
                    mostrarCarga(false);
                    Toast.makeText(this, "Error al actualizar tu nombre", Toast.LENGTH_SHORT).show();
                });
    }

    private void mostrarCarga(boolean isLoading) {
        pbLoading.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        btnSaveProfile.setEnabled(!isLoading);
        btnCancelEdit.setEnabled(!isLoading);
        etEditName.setEnabled(!isLoading);
        etEditEmail.setEnabled(!isLoading);
        etEditNewPassword.setEnabled(!isLoading);
        etEditCurrentPassword.setEnabled(!isLoading);
    }

    private void actualizarNivel(long xpActual) {
        tvProfileScore.setText(xpActual + " XP");
        long xpParaSiguienteNivel = 50;
        int nivelActual = (int) (xpActual / xpParaSiguienteNivel) + 1;
        long xpEnNivelActual = xpActual % xpParaSiguienteNivel;

        tvProfileLevel.setText("Nivel " + nivelActual);
        tvProgressText.setText(xpEnNivelActual + " / " + xpParaSiguienteNivel + " XP");

        pbProfileLevel.setMax((int) xpParaSiguienteNivel);
        pbProfileLevel.setProgress((int) xpEnNivelActual);
    }

    private void actualizarInsignias(long puntaje) {
        if (ivBadgeDerechos == null) return;

        ivBadgeDerechos.clearColorFilter();
        ivBadgeObligaciones.clearColorFilter();
        ivBadgeProhibiciones.clearColorFilter();
        ivBadgeSanciones.clearColorFilter();
        ivBadgeReconocimientos.clearColorFilter();

        // Transparencias: 1.0f para color real, 0.25f para bloqueado opaco
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
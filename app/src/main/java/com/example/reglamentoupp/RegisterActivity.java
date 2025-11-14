package com.example.reglamentoupp;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import com.example.reglamentoupp.databinding.ActivityRegisterBinding; // Importa el nuevo binding
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class RegisterActivity extends AppCompatActivity {

    private ActivityRegisterBinding binding; // Binding para el nuevo layout
    private FirebaseAuth mAuth;
    private FirebaseFirestore mStore;
    private static final String TAG = "RegisterActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityRegisterBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        mAuth = FirebaseAuth.getInstance();
        mStore = FirebaseFirestore.getInstance();

        binding.btnDoRegister.setOnClickListener(v -> validateAndRegisterUser());
        binding.btnGoToLogin.setOnClickListener(v -> finish()); // Regresa a LoginActivity
    }

    private void validateAndRegisterUser() {
        // Obtenemos todos los datos de los campos
        String nombre = Objects.requireNonNull(binding.etNombre.getText()).toString().trim();
        String apellidos = Objects.requireNonNull(binding.etApellidos.getText()).toString().trim();
        String telefono = Objects.requireNonNull(binding.etTelefono.getText()).toString().trim();
        String email = Objects.requireNonNull(binding.etEmailRegister.getText()).toString().trim();
        String password = Objects.requireNonNull(binding.etPasswordRegister.getText()).toString().trim();
        String confirmPassword = Objects.requireNonNull(binding.etConfirmPassword.getText()).toString().trim();

        // Validaciones
        if (nombre.isEmpty() || apellidos.isEmpty() || email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Nombre, Apellidos, Correo y Contraseña son obligatorios", Toast.LENGTH_SHORT).show();
            return;
        }
        if (password.length() < 6) {
            Toast.makeText(this, "La contraseña debe tener al menos 6 caracteres", Toast.LENGTH_SHORT).show();
            return;
        }
        if (!password.equals(confirmPassword)) {
            Toast.makeText(this, "Las contraseñas no coinciden", Toast.LENGTH_SHORT).show();
            return;
        }

        setLoading(true);

        // 1. Crear usuario en Firebase Auth
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        Log.d(TAG, "Usuario creado en Auth con éxito.");
                        FirebaseUser user = mAuth.getCurrentUser();
                        // 2. Guardar datos adicionales en Firestore
                        createNewUserInFirestore(user, nombre, apellidos, telefono);
                    } else {
                        Log.w(TAG, "Fallo createUser: ", task.getException());
                        Toast.makeText(RegisterActivity.this, "Error al registrar: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                        setLoading(false);
                    }
                });
    }

    // Método MEJORADO para guardar todos los datos que pediste
    private void createNewUserInFirestore(FirebaseUser firebaseUser, String nombre, String apellidos, String telefono) {
        if (firebaseUser == null) {
            setLoading(false);
            return;
        }

        Map<String, Object> userData = new HashMap<>();
        userData.put("uid", firebaseUser.getUid());
        userData.put("email", firebaseUser.getEmail());
        userData.put("nombre", nombre);
        userData.put("apellidos", apellidos);
        userData.put("telefono", telefono);
        userData.put("puntaje", 0); // Puntaje inicial
        userData.put("nivelDesbloqueado", 1); // Nivel 1 (Derechos) desbloqueado por defecto

        mStore.collection("usuarios").document(firebaseUser.getUid())
                .set(userData)
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "Documento de usuario creado en Firestore.");
                    navigateToMain();
                })
                .addOnFailureListener(e -> {
                    Log.w(TAG, "Error al crear documento en Firestore", e);
                    Toast.makeText(RegisterActivity.this, "Error al crear perfil: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    setLoading(false);
                });
    }

    private void navigateToMain() {
        Intent intent = new Intent(RegisterActivity.this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    private void setLoading(boolean isLoading) {
        if (isLoading) {
            binding.progressBarRegister.setVisibility(View.VISIBLE);
            binding.btnDoRegister.setEnabled(false);
            binding.btnGoToLogin.setEnabled(false);
        } else {
            binding.progressBarRegister.setVisibility(View.GONE);
            binding.btnDoRegister.setEnabled(true);
            binding.btnGoToLogin.setEnabled(true);
        }
    }
}
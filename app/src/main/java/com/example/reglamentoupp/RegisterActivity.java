package com.example.reglamentoupp;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import com.example.reglamentoupp.databinding.ActivityRegisterBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class RegisterActivity extends AppCompatActivity {

    private ActivityRegisterBinding binding;
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
        binding.btnGoToLogin.setOnClickListener(v -> finish());
    }

    private void validateAndRegisterUser() {
        // Uso seguro de Objects.requireNonNull para evitar warnings, aunque getText() suele no ser null
        if (binding.etNombre.getText() == null || binding.etApellidos.getText() == null ||
                binding.etEmailRegister.getText() == null || binding.etPasswordRegister.getText() == null) {
            return;
        }

        String nombre = binding.etNombre.getText().toString().trim();
        String apellidos = binding.etApellidos.getText().toString().trim();
        String telefono = binding.etTelefono.getText() != null ? binding.etTelefono.getText().toString().trim() : "";
        String email = binding.etEmailRegister.getText().toString().trim();
        String password = binding.etPasswordRegister.getText().toString().trim();
        String confirmPassword = binding.etConfirmPassword.getText() != null ? binding.etConfirmPassword.getText().toString().trim() : "";

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
                        Toast.makeText(RegisterActivity.this, "Error al registrar: " +
                                        (task.getException() != null ? task.getException().getMessage() : "Error desconocido"),
                                Toast.LENGTH_LONG).show();
                        setLoading(false);
                    }
                });
    }

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
        userData.put("puntaje", 0);
        userData.put("nivelDesbloqueado", 1);

        mStore.collection("usuarios").document(firebaseUser.getUid())
                .set(userData)
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "Documento de usuario creado en Firestore.");
                    navigateToMain();
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error al crear documento en Firestore. Eliminando usuario Auth para evitar inconsistencia.", e);
                    Toast.makeText(RegisterActivity.this, "Error al guardar datos. Intenta de nuevo.", Toast.LENGTH_SHORT).show();

                    // --- CORRECCIÓN CLAVE ---
                    // Si falla la BD, borramos el usuario de Auth para que no quede "zombie"
                    firebaseUser.delete();

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
        if (binding == null) return;
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
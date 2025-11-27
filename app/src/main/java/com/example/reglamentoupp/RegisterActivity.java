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

        // 1. Validar campos obligatorios
        if (nombre.isEmpty() || apellidos.isEmpty() || email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Nombre, Apellidos, Correo y Contraseña son obligatorios", Toast.LENGTH_SHORT).show();
            return;
        }

        // 2. VALIDACIÓN NOMBRE: Solo letras (incluye acentos, ñ y espacios)
        if (!nombre.matches("^[a-zA-ZáéíóúÁÉÍÓÚñÑ\\s]+$")) {
            binding.etNombre.setError("El nombre solo puede contener letras");
            binding.etNombre.requestFocus();
            return;
        }

        // 3. VALIDACIÓN APELLIDOS: Solo letras
        if (!apellidos.matches("^[a-zA-ZáéíóúÁÉÍÓÚñÑ\\s]+$")) {
            binding.etApellidos.setError("Los apellidos solo pueden contener letras");
            binding.etApellidos.requestFocus();
            return;
        }

        // 4. VALIDACIÓN TELÉFONO: Solo números y longitud exacta de 10
        if (!telefono.isEmpty()) {
            if (!telefono.matches("[0-9]+")) {
                binding.etTelefono.setError("Solo se permiten números");
                binding.etTelefono.requestFocus();
                return;
            }
            if (telefono.length() != 10) {
                binding.etTelefono.setError("El teléfono debe tener 10 dígitos");
                binding.etTelefono.requestFocus();
                return;
            }
        }

        // 5. Validar longitud de contraseña
        if (password.length() < 6) {
            binding.etPasswordRegister.setError("La contraseña debe tener al menos 6 caracteres");
            binding.etPasswordRegister.requestFocus();
            return;
        }

        // 6. Validar coincidencia de contraseñas
        if (!password.equals(confirmPassword)) {
            binding.etConfirmPassword.setError("Las contraseñas no coinciden");
            binding.etConfirmPassword.requestFocus();
            return;
        }

        setLoading(true);

        // Crear usuario en Firebase Auth
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        Log.d(TAG, "Usuario creado en Auth con éxito.");
                        FirebaseUser user = mAuth.getCurrentUser();
                        // Guardar datos adicionales en Firestore
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
                    Log.e(TAG, "Error al crear documento en Firestore.", e);
                    Toast.makeText(RegisterActivity.this, "Error al guardar datos. Intenta de nuevo.", Toast.LENGTH_SHORT).show();
                    // ROLLBACK: Borrar el usuario de Auth para evitar inconsistencias
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
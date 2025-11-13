package com.example.reglamentoupp;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

// --- INICIO DE CORRECCIÓN (Importar ViewBinding) ---
import com.example.reglamentoupp.databinding.ActivityLoginBinding;
// --- FIN DE CORRECCIÓN ---

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.HashMap;
import java.util.Map;

public class LoginActivity extends AppCompatActivity {

    // --- INICIO DE CORRECCIÓN (Declarar variable ViewBinding) ---
    private ActivityLoginBinding binding;
    // --- FIN DE CORRECCIÓN ---

    private FirebaseAuth mAuth;
    private FirebaseFirestore mStore;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // --- INICIO DE CORRECCIÓN (Inflar layout con ViewBinding) ---
        binding = ActivityLoginBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        // --- FIN DE CORRECCIÓN ---

        try {
            mAuth = FirebaseAuth.getInstance();
            mStore = FirebaseFirestore.getInstance();
        } catch (IllegalStateException e) {
            Log.e("LOGIN_FIREBASE_ERROR", "Error al inicializar Firebase: " + e.getMessage());
            Log.e("LOGIN_FIREBASE_ERROR", "Asegúrate de que 'google-services.json' esté en la carpeta /app y el package_name sea correcto.");
            Toast.makeText(this, "Error fatal de configuración de Firebase. Revisa Logcat.", Toast.LENGTH_LONG).show();

            // Deshabilitar vistas usando binding
            binding.btnLogin.setEnabled(false);
            binding.btnRegister.setEnabled(false);
            binding.etEmail.setEnabled(false);
            binding.etPassword.setEnabled(false);
            return;
        }

        // Configurar listeners usando binding (ya no se necesitan las variables de vistas)
        binding.btnLogin.setOnClickListener(v -> loginUser());
        binding.btnRegister.setOnClickListener(v -> registerUser());
    }

    private void registerUser() {
        // --- CORRECCIÓN 1 (Verificación de texto nulo y obtención desde binding) ---
        String email = "";
        if (binding.etEmail.getText() != null) {
            email = binding.etEmail.getText().toString().trim();
        }

        String password = "";
        if (binding.etPassword.getText() != null) {
            password = binding.etPassword.getText().toString().trim();
        }
        // --- FIN DE CORRECCIÓN 1 ---

        if (email.isEmpty() || password.isEmpty() || password.length() < 6) {
            Toast.makeText(this, "Correo inválido o contraseña (mín 6 caracteres)", Toast.LENGTH_SHORT).show();
            return;
        }

        setLoading(true);

        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        Log.d("LoginActivity", "Usuario creado con éxito.");
                        FirebaseUser user = mAuth.getCurrentUser();
                        createNewUserInFirestore(user);
                    } else {
                        Log.w("LoginActivity", "Fallo createUser: ", task.getException());

                        // --- INICIO DE CORRECCIÓN 2 (Verificación de excepción nula) ---
                        String errorMessage = "Error al registrar.";
                        if (task.getException() != null && task.getException().getMessage() != null) {
                            errorMessage = task.getException().getMessage();
                        }
                        Toast.makeText(LoginActivity.this, "Error: " + errorMessage,
                                Toast.LENGTH_LONG).show();
                        // --- FIN DE CORRECCIÓN 2 ---

                        setLoading(false);
                    }
                });
    }

    private void createNewUserInFirestore(FirebaseUser firebaseUser) {
        if (firebaseUser == null) {
            setLoading(false);
            return;
        }

        Map<String, Object> userData = new HashMap<>();
        userData.put("email", firebaseUser.getEmail());
        userData.put("puntaje", 0);
        userData.put("uid", firebaseUser.getUid());

        mStore.collection("usuarios").document(firebaseUser.getUid())
                .set(userData)
                .addOnSuccessListener(aVoid -> {
                    Log.d("LoginActivity", "Documento de usuario creado en Firestore.");
                    navigateToMain();
                })
                .addOnFailureListener(e -> {
                    Log.w("LoginActivity", "Error al crear documento en Firestore", e);
                    Toast.makeText(LoginActivity.this, "Error al crear perfil de puntaje: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    setLoading(false);
                });
    }

    private void loginUser() {
        // --- CORRECCIÓN 1 (Verificación de texto nulo y obtención desde binding) ---
        String email = "";
        if (binding.etEmail.getText() != null) {
            email = binding.etEmail.getText().toString().trim();
        }

        String password = "";
        if (binding.etPassword.getText() != null) {
            password = binding.etPassword.getText().toString().trim();
        }
        // --- FIN DE CORRECCIÓN 1 ---

        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Campos vacíos", Toast.LENGTH_SHORT).show();
            return;
        }

        setLoading(true);

        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        Log.d("LoginActivity", "Inicio de sesión exitoso.");
                        navigateToMain();
                    } else {
                        Log.w("LoginActivity", "Fallo signIn: ", task.getException());

                        // --- INICIO DE CORRECCIÓN 2 (Verificación de excepción nula) ---
                        String errorMessage = "Error de autenticación. Verifique sus datos.";
                        if (task.getException() != null && task.getException().getMessage() != null) {
                            errorMessage = task.getException().getMessage();
                        }
                        Toast.makeText(LoginActivity.this, "Error: " + errorMessage,
                                Toast.LENGTH_LONG).show();
                        // --- FIN DE CORRECCIÓN 2 ---

                        setLoading(false);
                    }
                });
    }

    private void navigateToMain() {
        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
        startActivity(intent);
        finish();
    }

    private void setLoading(boolean isLoading) {
        // Controlar vistas usando binding
        if (isLoading) {
            binding.progressBar.setVisibility(View.VISIBLE);
            binding.btnLogin.setEnabled(false);
            binding.btnRegister.setEnabled(false);
        } else {
            binding.progressBar.setVisibility(View.GONE);
            binding.btnLogin.setEnabled(true);
            binding.btnRegister.setEnabled(true);
        }
    }
}
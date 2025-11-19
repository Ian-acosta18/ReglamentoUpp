package com.example.reglamentoupp;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import com.example.reglamentoupp.databinding.ActivityLoginBinding;

import com.google.firebase.auth.FirebaseAuth;

public class LoginActivity extends AppCompatActivity {

    private ActivityLoginBinding binding;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityLoginBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        try {
            mAuth = FirebaseAuth.getInstance();
        } catch (IllegalStateException e) {
            Log.e("LOGIN_FIREBASE_ERROR", "Error al inicializar Firebase: " + e.getMessage());
            Toast.makeText(this, "Error fatal de configuración de Firebase.", Toast.LENGTH_LONG).show();

            binding.btnLogin.setEnabled(false);
            binding.tvRegister.setEnabled(false); // CORREGIDO: Ahora es tvRegister
            binding.etEmail.setEnabled(false);
            binding.etPassword.setEnabled(false);
            return;
        }

        // Autologueo si el usuario ya está activo
        if (mAuth.getCurrentUser() != null) {
            Log.d("LoginActivity", "Usuario ya logueado, saltando a MainActivity.");
            navigateToMain();
            return;
        }

        // Configurar listeners
        binding.btnLogin.setOnClickListener(v -> loginUser());

        // CORREGIDO: Listener actualizado para el nuevo TextView
        binding.tvRegister.setOnClickListener(v -> {
            Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
            startActivity(intent);
        });
    }

    private void loginUser() {
        String email = "";
        if (binding.etEmail.getText() != null) {
            email = binding.etEmail.getText().toString().trim();
        }

        String password = "";
        if (binding.etPassword.getText() != null) {
            password = binding.etPassword.getText().toString().trim();
        }

        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Por favor, llena todos los campos", Toast.LENGTH_SHORT).show();
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
                        String errorMessage = "Error de autenticación.";
                        if (task.getException() != null && task.getException().getMessage() != null) {
                            if (task.getException().getMessage().contains("INVALID_LOGIN_CREDENTIALS")) {
                                errorMessage = "Credenciales inválidas. Revisa tu correo y contraseña.";
                            } else {
                                errorMessage = task.getException().getMessage();
                            }
                        }
                        Toast.makeText(LoginActivity.this, errorMessage, Toast.LENGTH_LONG).show();
                        setLoading(false);
                    }
                });
    }

    private void navigateToMain() {
        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    private void setLoading(boolean isLoading) {
        if (isLoading) {
            binding.progressBar.setVisibility(View.VISIBLE);
            binding.btnLogin.setEnabled(false);
            binding.tvRegister.setEnabled(false); // CORREGIDO
        } else {
            binding.progressBar.setVisibility(View.GONE);
            binding.btnLogin.setEnabled(true);
            binding.tvRegister.setEnabled(true); // CORREGIDO
        }
    }
}
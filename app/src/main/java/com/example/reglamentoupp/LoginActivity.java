package com.example.reglamentoupp;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import androidx.core.splashscreen.SplashScreen;

import com.example.reglamentoupp.databinding.ActivityLoginBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthInvalidUserException;

public class LoginActivity extends AppCompatActivity {

    private ActivityLoginBinding binding;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // 1. Instalar el Splash Screen
        SplashScreen splashScreen = SplashScreen.installSplashScreen(this);

        super.onCreate(savedInstanceState);
        binding = ActivityLoginBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // 2. Animación de salida del Splash Screen
        splashScreen.setOnExitAnimationListener(splashScreenViewProvider -> {
            ObjectAnimator fadeOut = ObjectAnimator.ofFloat(
                    splashScreenViewProvider.getView(),
                    View.ALPHA,
                    1f,
                    0f
            );
            fadeOut.setDuration(500);
            fadeOut.setInterpolator(new AccelerateInterpolator());
            fadeOut.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    splashScreenViewProvider.remove();
                }
            });
            fadeOut.start();

            // 3. Entrada animada del formulario
            try {
                Animation formAnimation = AnimationUtils.loadAnimation(this, R.anim.slide_up);
                binding.getRoot().startAnimation(formAnimation);
            } catch (Exception e) {
                binding.getRoot().setAlpha(0f);
                binding.getRoot().setTranslationY(50f);
                binding.getRoot().animate().alpha(1f).translationY(0f).setDuration(600).start();
            }
        });

        // Configuración de Firebase
        try {
            mAuth = FirebaseAuth.getInstance();
        } catch (IllegalStateException e) {
            Log.e("LOGIN_FIREBASE_ERROR", "Error Firebase: " + e.getMessage());
            Toast.makeText(this, "Error de configuración. Inténtalo de nuevo.", Toast.LENGTH_LONG).show();
            setInputsEnabled(false);
            return;
        }

        if (mAuth.getCurrentUser() != null) {
            navigateToMain();
            return;
        }

        // Listeners de botones
        binding.btnLogin.setOnClickListener(v -> loginUser());

        binding.tvRegister.setOnClickListener(v -> {
            try {
                Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
                startActivity(intent);
            } catch (Throwable e) {
                Log.e("ERROR_NAVEGACION", "Fallo al abrir RegisterActivity", e);
                Toast.makeText(LoginActivity.this, "Error en la pantalla de registro.", Toast.LENGTH_LONG).show();
            }
        });
    }

    private void loginUser() {
        if (binding.etEmail.getText() == null || binding.etPassword.getText() == null) return;

        String email = binding.etEmail.getText().toString().trim();
        String password = binding.etPassword.getText().toString().trim();

        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Por favor, llena todos los campos", Toast.LENGTH_SHORT).show();
            return;
        }

        setLoading(true);

        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        navigateToMain();
                    } else {
                        // Traducción de errores de Firebase a Español
                        String errorMessage = "Error de autenticación. Inténtalo más tarde.";
                        if (task.getException() != null) {
                            try {
                                throw task.getException();
                            } catch (FirebaseAuthInvalidUserException e) {
                                errorMessage = "No existe una cuenta registrada con este correo.";
                            } catch (FirebaseAuthInvalidCredentialsException e) {
                                errorMessage = "Credenciales inválidas. Revisa tu correo y contraseña.";
                            } catch (Exception e) {
                                errorMessage = "Error de inicio de sesión. Revisa tu conexión.";
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
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        finish();
    }

    private void setLoading(boolean isLoading) {
        binding.progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        setInputsEnabled(!isLoading);
    }

    private void setInputsEnabled(boolean enabled) {
        binding.btnLogin.setEnabled(enabled);
        binding.tvRegister.setEnabled(enabled);
        binding.etEmail.setEnabled(enabled);
        binding.etPassword.setEnabled(enabled);
    }
}
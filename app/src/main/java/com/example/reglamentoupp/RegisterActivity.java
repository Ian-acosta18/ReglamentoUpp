package com.example.reglamentoupp;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide; // Importación de Glide para mejorar la carga de imagen
import com.cloudinary.android.MediaManager;
import com.cloudinary.android.callback.ErrorInfo;
import com.cloudinary.android.callback.UploadCallback;
import com.example.reglamentoupp.databinding.ActivityRegisterBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseAuthWeakPasswordException;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.HashMap;
import java.util.Map;

public class RegisterActivity extends AppCompatActivity {

    private ActivityRegisterBinding binding;
    private FirebaseAuth mAuth;
    private FirebaseFirestore mStore;
    private static final String TAG = "RegisterActivity";
    private Uri imageUri;
    private ActivityResultLauncher<String> galleryLauncher;

    private static final String CLOUD_NAME = "dfgj9sdma";
    private static final String UPLOAD_PRESET = "mi_app_preset";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityRegisterBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        initCloudinary();
        mAuth = FirebaseAuth.getInstance();
        mStore = FirebaseFirestore.getInstance();

        // AQUÍ ESTÁ LA CORRECCIÓN PRINCIPAL
        galleryLauncher = registerForActivityResult(
                new ActivityResultContracts.GetContent(),
                uri -> {
                    if (uri != null) {
                        imageUri = uri;

                        // 1. Quitar el tinte gris (tint) para que se vean los colores de la foto
                        binding.ivPerfilRegister.clearColorFilter();

                        // 2. Cargar la imagen usando Glide para que se centre bien en el círculo
                        Glide.with(this)
                                .load(uri)
                                .centerCrop()
                                .into(binding.ivPerfilRegister);
                    }
                }
        );

        binding.ivPerfilRegister.setOnClickListener(v -> galleryLauncher.launch("image/*"));
        binding.btnDoRegister.setOnClickListener(v -> validateAndRegisterUser());
        binding.btnGoToLogin.setOnClickListener(v -> finish());
    }

    private void initCloudinary() {
        try {
            MediaManager.get();
        } catch (IllegalStateException e) {
            Map<String, Object> config = new HashMap<>();
            config.put("cloud_name", CLOUD_NAME);
            config.put("secure", true);
            MediaManager.init(this, config);
        }
    }

    private void validateAndRegisterUser() {
        if (binding.etNombre.getText() == null || binding.etApellidos.getText() == null ||
                binding.etEmailRegister.getText() == null || binding.etPasswordRegister.getText() == null) return;

        String nombre = binding.etNombre.getText().toString().trim();
        String apellidos = binding.etApellidos.getText().toString().trim();
        String telefono = binding.etTelefono.getText() != null ? binding.etTelefono.getText().toString().trim() : "";
        String email = binding.etEmailRegister.getText().toString().trim();
        String password = binding.etPasswordRegister.getText().toString().trim();
        String confirmPassword = binding.etConfirmPassword.getText() != null ? binding.etConfirmPassword.getText().toString().trim() : "";

        // Validación de campos vacíos
        if (nombre.isEmpty() || apellidos.isEmpty() || email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Llena todos los campos obligatorios", Toast.LENGTH_SHORT).show();
            return;
        }

        // Validación: Nombre y Apellidos solo letras (incluye espacios y acentos)
        String regexLetras = "^[a-zA-ZáéíóúÁÉÍÓÚñÑ\\s]+$";
        if (!nombre.matches(regexLetras)) {
            binding.etNombre.setError("El nombre solo debe contener letras");
            return;
        }
        if (!apellidos.matches(regexLetras)) {
            binding.etApellidos.setError("Los apellidos solo deben contener letras");
            return;
        }

        // Validación: Correo institucional
        if (!email.endsWith("@uppuebla.edu.mx")) {
            binding.etEmailRegister.setError("Usa tu correo de @uppuebla.edu.mx");
            return;
        }

        // Validación: Teléfono solo números
        if (!telefono.isEmpty() && !telefono.matches("^[0-9]+$")) {
            binding.etTelefono.setError("El teléfono solo debe contener números");
            return;
        }

        // Validación: Contraseña
        if (password.length() < 6) {
            binding.etPasswordRegister.setError("Mínimo 6 caracteres");
            return;
        }

        if (!password.equals(confirmPassword)) {
            binding.etConfirmPassword.setError("Las contraseñas no coinciden");
            return;
        }

        setLoading(true);

        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = mAuth.getCurrentUser();
                        if (imageUri != null) {
                            uploadImageToCloudinary(user, nombre, apellidos, telefono);
                        } else {
                            createNewUserInFirestore(user, nombre, apellidos, telefono, "");
                        }
                    } else {
                        setLoading(false);

                        // Traducción de errores de Firebase al español
                        String mensajeError = "Error desconocido al registrar";
                        if (task.getException() != null) {
                            try {
                                throw task.getException();
                            } catch (FirebaseAuthWeakPasswordException e) {
                                mensajeError = "La contraseña es muy débil.";
                            } catch (FirebaseAuthInvalidCredentialsException e) {
                                mensajeError = "El correo electrónico es inválido.";
                            } catch (FirebaseAuthUserCollisionException e) {
                                mensajeError = "Este correo ya se encuentra registrado.";
                            } catch (Exception e) {
                                mensajeError = "Error en el registro. Inténtalo más tarde.";
                            }
                        }
                        Toast.makeText(this, mensajeError, Toast.LENGTH_LONG).show();
                    }
                });
    }

    private void uploadImageToCloudinary(FirebaseUser user, String nombre, String apellidos, String telefono) {
        MediaManager.get().upload(imageUri)
                .unsigned(UPLOAD_PRESET)
                .callback(new UploadCallback() {
                    @Override
                    public void onStart(String requestId) {}
                    @Override
                    public void onProgress(String requestId, long bytes, long totalBytes) {}

                    @Override
                    public void onSuccess(String requestId, Map resultData) {
                        String url = (String) resultData.get("secure_url");
                        createNewUserInFirestore(user, nombre, apellidos, telefono, url);
                    }

                    @Override
                    public void onError(String requestId, ErrorInfo error) {
                        Log.e(TAG, "Error Cloudinary: " + error.getDescription());
                        Toast.makeText(RegisterActivity.this, "Aviso: No se pudo subir la foto de perfil", Toast.LENGTH_SHORT).show();
                        createNewUserInFirestore(user, nombre, apellidos, telefono, "");
                    }

                    @Override
                    public void onReschedule(String requestId, ErrorInfo error) {}
                }).dispatch();
    }

    private void createNewUserInFirestore(FirebaseUser firebaseUser, String nombre, String apellidos, String telefono, String fotoUrl) {
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
        userData.put("fotoUrl", fotoUrl);

        mStore.collection("usuarios").document(firebaseUser.getUid())
                .set(userData)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(RegisterActivity.this, "¡Registro exitoso en UPPue!", Toast.LENGTH_SHORT).show();
                    navigateToMain();
                })
                .addOnFailureListener(e -> {
                    setLoading(false);
                    Toast.makeText(this, "Error al guardar tu perfil de usuario", Toast.LENGTH_SHORT).show();
                });
    }

    private void navigateToMain() {
        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    private void setLoading(boolean isLoading) {
        binding.progressBarRegister.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        binding.btnDoRegister.setEnabled(!isLoading);
    }
}
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

import com.cloudinary.android.MediaManager;
import com.cloudinary.android.callback.ErrorInfo;
import com.cloudinary.android.callback.UploadCallback;
import com.example.reglamentoupp.databinding.ActivityRegisterBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.HashMap;
import java.util.Map;

public class RegisterActivity extends AppCompatActivity {

    private ActivityRegisterBinding binding;
    private FirebaseAuth mAuth;
    private FirebaseFirestore mStore;
    private static final String TAG = "RegisterActivity";

    // Variables para imagen
    private Uri imageUri;
    private ActivityResultLauncher<String> galleryLauncher;

    // --- DATOS DE CLOUDINARY ---
    // Verifica que estos coincidan exactamente con tu Dashboard
    private static final String CLOUD_NAME = "dianacosta";
    private static final String UPLOAD_PRESET = "mi_app_preset"; // ¡Debe ser Unsigned en la web!

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityRegisterBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Inicializar Cloudinary
        initCloudinary();

        mAuth = FirebaseAuth.getInstance();
        mStore = FirebaseFirestore.getInstance();

        // Configurar galería
        galleryLauncher = registerForActivityResult(
                new ActivityResultContracts.GetContent(),
                uri -> {
                    if (uri != null) {
                        imageUri = uri;
                        binding.ivPerfilRegister.setImageURI(uri);
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
        if (binding.etNombre.getText() == null || binding.etApellidos.getText() == null) return;

        String nombre = binding.etNombre.getText().toString().trim();
        String apellidos = binding.etApellidos.getText().toString().trim();
        String telefono = binding.etTelefono.getText() != null ? binding.etTelefono.getText().toString().trim() : "";
        String email = binding.etEmailRegister.getText().toString().trim();
        String password = binding.etPasswordRegister.getText().toString().trim();
        String confirmPassword = binding.etConfirmPassword.getText() != null ? binding.etConfirmPassword.getText().toString().trim() : "";

        if (nombre.isEmpty() || apellidos.isEmpty() || email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Llena todos los campos", Toast.LENGTH_SHORT).show();
            return;
        }
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
                        Log.e(TAG, "Error Auth", task.getException());
                        Toast.makeText(RegisterActivity.this, "Error registro: " +
                                (task.getException() != null ? task.getException().getMessage() : ""), Toast.LENGTH_LONG).show();
                        setLoading(false);
                    }
                });
    }

    private void uploadImageToCloudinary(FirebaseUser user, String nombre, String apellidos, String telefono) {
        Log.d(TAG, "Iniciando subida a Cloudinary...");

        MediaManager.get().upload(imageUri)
                .unsigned(UPLOAD_PRESET) // Usa el preset configurado
                .callback(new UploadCallback() {
                    @Override
                    public void onStart(String requestId) {
                        Log.d(TAG, "Subida iniciada: " + requestId);
                    }

                    @Override
                    public void onProgress(String requestId, long bytes, long totalBytes) { }

                    @Override
                    public void onSuccess(String requestId, Map resultData) {
                        Log.d(TAG, "Subida exitosa: " + resultData.get("secure_url"));
                        String url = (String) resultData.get("secure_url");
                        createNewUserInFirestore(user, nombre, apellidos, telefono, url);
                    }

                    @Override
                    public void onError(String requestId, ErrorInfo error) {
                        // AQUÍ VERÁS EL ERROR REAL EN EL LOGCAT
                        Log.e(TAG, "ERROR CLOUDINARY: " + error.getDescription());
                        Log.e(TAG, "CÓDIGO ERROR: " + error.getCode());

                        Toast.makeText(RegisterActivity.this,
                                "Error foto: " + error.getDescription(), Toast.LENGTH_LONG).show();

                        // Guardamos sin foto para no perder el usuario
                        createNewUserInFirestore(user, nombre, apellidos, telefono, "");
                    }

                    @Override
                    public void onReschedule(String requestId, ErrorInfo error) { }
                })
                .dispatch();
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
                    navigateToMain();
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error Firestore", e);
                    Toast.makeText(RegisterActivity.this, "Error al guardar datos.", Toast.LENGTH_SHORT).show();
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
        binding.progressBarRegister.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        binding.btnDoRegister.setEnabled(!isLoading);
    }
}
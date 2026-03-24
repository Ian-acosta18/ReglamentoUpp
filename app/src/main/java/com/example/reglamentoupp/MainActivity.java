package com.example.reglamentoupp;

import android.animation.ObjectAnimator;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.animation.DecelerateInterpolator;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.cloudinary.android.MediaManager;
import com.cloudinary.android.callback.ErrorInfo;
import com.cloudinary.android.callback.UploadCallback;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.card.MaterialCardView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    public static final String KEY_NIVEL_JUEGO = "nivel_juego";
    public static final String KEY_PUNTAJE_ACTUAL = "puntaje_actual";
    public static final String KEY_NIVEL_DESBLOQUEADO = "nivel_desbloqueado";

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    private TextView tvUserName, tvUserPuntaje, tvUserLevel;
    private ImageView ivUserProfile;
    private MaterialCardView cardProfilePic;
    private View btnLogout;

    // Declaramos las barras de progreso
    private ProgressBar progressDerechos, progressObligaciones, progressProhibiciones, progressSanciones, progressReconocimientos;

    // Variables para la imagen de perfil
    private ActivityResultLauncher<String> galleryLauncher;
    private Uri newImageUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initCloudinary(); // Iniciamos Cloudinary por si la app entra directo aquí

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        tvUserName = findViewById(R.id.tvUserName);
        tvUserPuntaje = findViewById(R.id.tvUserPuntaje);
        tvUserLevel = findViewById(R.id.tvUserLevel);
        btnLogout = findViewById(R.id.btnLogout);
        ivUserProfile = findViewById(R.id.ivUserProfile);
        cardProfilePic = findViewById(R.id.cardProfilePic);

        // Inicializamos las barras
        progressDerechos = findViewById(R.id.progressDerechos);
        progressObligaciones = findViewById(R.id.progressObligaciones);
        progressProhibiciones = findViewById(R.id.progressProhibiciones);
        progressSanciones = findViewById(R.id.progressSanciones);
        progressReconocimientos = findViewById(R.id.progressReconocimientos);

        // Ponemos todas las barras en 0 al iniciar
        reiniciarBarras();
        cargarDatosUsuario();
        configurarBottomNavigation();
        configurarJuegosRapidos();
        configurarCaminoCategorias();

        // Configurar el seleccionador de imágenes
        galleryLauncher = registerForActivityResult(
                new ActivityResultContracts.GetContent(),
                uri -> {
                    if (uri != null) {
                        newImageUri = uri;
                        ivUserProfile.setImageURI(uri);
                        actualizarImagenEnBaseDeDatos(uri);
                    }
                }
        );

        // Al presionar la foto de perfil, abrimos la galería
        cardProfilePic.setOnClickListener(v -> galleryLauncher.launch("image/*"));

        if (btnLogout != null) {
            btnLogout.setOnClickListener(v -> {
                mAuth.signOut();
                Intent intent = new Intent(MainActivity.this, LoginActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                finish();
            });
        }
    }

    // Aseguramos que Cloudinary esté listo
    private void initCloudinary() {
        try {
            MediaManager.get();
        } catch (IllegalStateException e) {
            Map<String, Object> config = new HashMap<>();
            config.put("cloud_name", "dfgj9sdma"); // Tu nombre de Cloudinary
            config.put("secure", true);
            MediaManager.init(this, config);
        }
    }

    private void reiniciarBarras() {
        if (progressDerechos != null) progressDerechos.setProgress(0);
        if (progressObligaciones != null) progressObligaciones.setProgress(0);
        if (progressProhibiciones != null) progressProhibiciones.setProgress(0);
        if (progressSanciones != null) progressSanciones.setProgress(0);
        if (progressReconocimientos != null) progressReconocimientos.setProgress(0);
    }

    private void cargarDatosUsuario() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            String userId = currentUser.getUid();

            db.collection("usuarios").document(userId).get()
                    .addOnSuccessListener(documentSnapshot -> {
                        if (documentSnapshot.exists()) {
                            String nombre = documentSnapshot.getString("nombre");
                            Long puntaje = documentSnapshot.getLong("puntaje");
                            String fotoUrl = documentSnapshot.getString("fotoUrl"); // Obtenemos la URL de la foto

                            if (nombre != null && tvUserName != null) {
                                String[] partesNombre = nombre.split(" ");
                                tvUserName.setText(partesNombre[0]);
                            }

                            if (puntaje != null) {
                                if (tvUserPuntaje != null) {
                                    tvUserPuntaje.setText("⚡ " + puntaje + " XP");
                                }
                                long nivel = (puntaje / 50) + 1;
                                if (tvUserLevel != null) {
                                    tvUserLevel.setText("🏆 Nivel " + nivel);
                                }
                                actualizarProgresoBarras(puntaje);
                            }

                            // Cargamos la imagen de perfil con Glide
                            if (fotoUrl != null && !fotoUrl.isEmpty() && ivUserProfile != null) {
                                Glide.with(MainActivity.this)
                                        .load(fotoUrl)
                                        .centerCrop()
                                        .into(ivUserProfile);
                            }
                        }
                    });
        }
    }

    private void actualizarImagenEnBaseDeDatos(Uri imageUri) {
        Toast.makeText(this, "Subiendo nueva imagen de perfil...", Toast.LENGTH_SHORT).show();

        MediaManager.get().upload(imageUri)
                .unsigned("mi_app_preset") // Tu preset
                .callback(new UploadCallback() {
                    @Override
                    public void onStart(String requestId) {}
                    @Override
                    public void onProgress(String requestId, long bytes, long totalBytes) {}
                    @Override
                    public void onReschedule(String requestId, ErrorInfo error) {}

                    @Override
                    public void onSuccess(String requestId, Map resultData) {
                        String newUrl = (String) resultData.get("secure_url");
                        FirebaseUser user = mAuth.getCurrentUser();

                        if(user != null) {
                            db.collection("usuarios").document(user.getUid())
                                    .update("fotoUrl", newUrl)
                                    .addOnSuccessListener(aVoid -> {
                                        Toast.makeText(MainActivity.this, "¡Foto actualizada con éxito!", Toast.LENGTH_SHORT).show();
                                    })
                                    .addOnFailureListener(e -> {
                                        Toast.makeText(MainActivity.this, "Error al guardar en base de datos", Toast.LENGTH_SHORT).show();
                                    });
                        }
                    }

                    @Override
                    public void onError(String requestId, ErrorInfo error) {
                        Toast.makeText(MainActivity.this, "Error al subir imagen a Cloudinary", Toast.LENGTH_SHORT).show();
                        Log.e("CLOUDINARY_ERROR", error.getDescription());
                    }
                }).dispatch();
    }

    private void actualizarProgresoBarras(long puntaje) {
        int maxXP = 50;
        int p1 = (int) Math.min(maxXP, Math.max(0, puntaje));
        animarBarra(progressDerechos, (p1 * 100) / maxXP);

        int p2 = (int) Math.min(maxXP, Math.max(0, puntaje - 50));
        animarBarra(progressObligaciones, (p2 * 100) / maxXP);

        int p3 = (int) Math.min(maxXP, Math.max(0, puntaje - 100));
        animarBarra(progressProhibiciones, (p3 * 100) / maxXP);

        int p4 = (int) Math.min(maxXP, Math.max(0, puntaje - 150));
        animarBarra(progressSanciones, (p4 * 100) / maxXP);

        int p5 = (int) Math.min(maxXP, Math.max(0, puntaje - 200));
        animarBarra(progressReconocimientos, (p5 * 100) / maxXP);
    }

    private void animarBarra(ProgressBar bar, int progresoDestino) {
        if (bar != null) {
            ObjectAnimator animation = ObjectAnimator.ofInt(bar, "progress", bar.getProgress(), progresoDestino);
            animation.setDuration(1200);
            animation.setInterpolator(new DecelerateInterpolator());
            animation.start();
        }
    }

    private void configurarBottomNavigation() {
        BottomNavigationView bottomNav = findViewById(R.id.bottomNav);
        if (bottomNav != null) {
            bottomNav.setSelectedItemId(R.id.nav_home);
            bottomNav.setOnItemSelectedListener(item -> {
                int id = item.getItemId();
                if (id == R.id.nav_home) {
                    return true;
                } else if (id == R.id.nav_games) {
                    startActivity(new Intent(MainActivity.this, ManualJuegosActivity.class));
                    return true;
                } else if (id == R.id.nav_ranking) {
                    startActivity(new Intent(MainActivity.this, RankingActivity.class));
                    return true;
                } else if (id == R.id.nav_profile) {
                    Toast.makeText(this, "Toca tu foto arriba para cambiarla ☝️", Toast.LENGTH_SHORT).show();
                    return true;
                }
                return false;
            });
        }
    }

    private void configurarJuegosRapidos() {
        View btnJugarModoDesafio = findViewById(R.id.btnJugarModoDesafio);
        if (btnJugarModoDesafio != null) {
            btnJugarModoDesafio.setOnClickListener(v -> startActivity(new Intent(MainActivity.this, QuizActivity.class)));
        }

        View btnJugarAhorcado = findViewById(R.id.btnJugarAhorcado);
        if (btnJugarAhorcado != null) {
            btnJugarAhorcado.setOnClickListener(v -> startActivity(new Intent(MainActivity.this, HangmanActivity.class)));
        }

        View btnJugarMemorama = findViewById(R.id.btnJugarMemorama);
        if (btnJugarMemorama != null) {
            btnJugarMemorama.setOnClickListener(v -> startActivity(new Intent(MainActivity.this, MemoryGameActivity.class)));
        }

        View btnJugarVerdaderoFalso = findViewById(R.id.btnJugarVerdaderoFalso);
        if (btnJugarVerdaderoFalso != null) {
            btnJugarVerdaderoFalso.setOnClickListener(v -> startActivity(new Intent(MainActivity.this, TrueFalseActivity.class)));
        }

        View btnJugarSopaLetras = findViewById(R.id.btnJugarSopaLetras);
        if (btnJugarSopaLetras != null) {
            btnJugarSopaLetras.setOnClickListener(v -> startActivity(new Intent(MainActivity.this, WordSearchActivity.class)));
        }
    }

    private void configurarCaminoCategorias() {
        View btnDerechos = findViewById(R.id.btnJugarDerechos);
        if (btnDerechos != null) {
            btnDerechos.setOnClickListener(v -> abrirApartado("Derechos"));
        }

        View btnObligaciones = findViewById(R.id.btnJugarObligaciones);
        if (btnObligaciones != null) {
            btnObligaciones.setOnClickListener(v -> abrirApartado("Obligaciones"));
        }

        View btnProhibiciones = findViewById(R.id.btnJugarProhibiciones);
        if (btnProhibiciones != null) {
            btnProhibiciones.setOnClickListener(v -> abrirApartado("Prohibiciones"));
        }

        View btnSanciones = findViewById(R.id.btnJugarSanciones);
        if (btnSanciones != null) {
            btnSanciones.setOnClickListener(v -> abrirApartado("Sanciones"));
        }

        View btnReconocimientos = findViewById(R.id.btnJugarReconocimientos);
        if (btnReconocimientos != null) {
            btnReconocimientos.setOnClickListener(v -> abrirApartado("Reconocimientos"));
        }
    }

    private void abrirApartado(String categoria) {
        Intent intent = new Intent(MainActivity.this, GameLevelActivity.class);
        intent.putExtra(KEY_NIVEL_JUEGO, categoria);
        startActivity(intent);
    }
}
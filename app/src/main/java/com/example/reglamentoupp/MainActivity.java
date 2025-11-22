package com.example.reglamentoupp;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.example.reglamentoupp.databinding.ActivityMainBinding;
import com.google.android.material.card.MaterialCardView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class MainActivity extends AppCompatActivity implements BaseReglamentoFragment.ReglamentoInteractionListener {

    private ActivityMainBinding binding;
    private FirebaseAuth mAuth;
    private FirebaseFirestore mStore;
    private String userID;
    private long userPuntaje = 0;
    private int userNivel = 1; // Nivel de desbloqueo del usuario

    public static final String KEY_NIVEL_JUEGO = "nivelJuego";
    public static final String KEY_PUNTAJE_ACTUAL = "puntajeActual";
    public static final String KEY_NIVEL_DESBLOQUEADO = "nivelDesbloqueado";
    private static final String TAG = "MainActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        mAuth = FirebaseAuth.getInstance();
        mStore = FirebaseFirestore.getInstance();

        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            Log.w(TAG, "Usuario no logueado. Regresando a Login.");
            navigateToLogin();
            return;
        }
        userID = currentUser.getUid();

        binding.btnLogout.setOnClickListener(v -> {
            mAuth.signOut();
            navigateToLogin();
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadUserData();
        animarMenu(); // Llamada a la nueva animación
    }

    private void loadUserData() {
        if (userID == null) return;

        mStore.collection("usuarios").document(userID).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        String nombre = documentSnapshot.getString("nombre");
                        if (nombre != null && !nombre.isEmpty()) {
                            binding.tvUserName.setText("Bienvenido, " + nombre);
                        } else {
                            binding.tvUserName.setText(documentSnapshot.getString("email"));
                        }

                        Long puntajeDb = documentSnapshot.getLong("puntaje");
                        if (puntajeDb != null) {
                            userPuntaje = puntajeDb;
                        }
                        binding.tvUserPuntaje.setText(userPuntaje + " Puntos");

                        Long nivelDb = documentSnapshot.getLong("nivelDesbloqueado");
                        if (nivelDb != null) {
                            userNivel = nivelDb.intValue();
                        }

                        Log.d(TAG, "Usuario cargado. Puntaje: " + userPuntaje + ", Nivel Desbloqueado: " + userNivel);

                        // --- Actualizar Insignias ---
                        actualizarInsignias(userPuntaje);

                        // --- Configurar Listeners de Juegos ---
                        binding.btnJugarModoDesafio.setOnClickListener(v -> {
                            Intent intent = new Intent(MainActivity.this, QuizActivity.class);
                            startActivity(intent);
                        });

                        binding.btnJugarVerdaderoFalso.setOnClickListener(v -> {
                            Intent intent = new Intent(MainActivity.this, TrueFalseActivity.class);
                            startActivity(intent);
                        });

                        // --- NUEVO LISTENER PARA AHORCADO ---
                        binding.btnJugarAhorcado.setOnClickListener(v -> {
                            Intent intent = new Intent(MainActivity.this, HangmanActivity.class);
                            startActivity(intent);
                        });


                        // --- Configura los botones de Nivel (Modo Estudio) ---
                        // (El diseño ahora es blanco por defecto en el XML, la lógica de bloqueo sigue aquí)
                        setupNivelButton(binding.btnJugarDerechos, null, binding.tvDerechos, binding.ivDerechos,
                                "Derechos", 1, R.color.upp_primary, R.color.text_primary, 0);
                        setupNivelButton(binding.btnJugarObligaciones, binding.ivLockObligaciones, binding.tvObligaciones, binding.ivObligaciones,
                                "Obligaciones", 2, R.color.upp_primary, R.color.text_primary, 50);
                        setupNivelButton(binding.btnJugarProhibiciones, binding.ivLockProhibiciones, binding.tvProhibiciones, binding.ivProhibiciones,
                                "Prohibiciones", 3, R.color.upp_primary, R.color.text_primary, 100);
                        setupNivelButton(binding.btnJugarSanciones, binding.ivLockSanciones, binding.tvSanciones, binding.ivSanciones,
                                "Sanciones", 4, R.color.upp_primary, R.color.text_primary, 150);
                        setupNivelButton(binding.btnJugarReconocimientos, binding.ivLockReconocimientos, binding.tvReconocimientos, binding.ivReconocimientos,
                                "Reconocimientos", 5, R.color.upp_primary, R.color.text_primary, 200);


                    } else {
                        Log.w(TAG, "No existe el documento del usuario en Firestore.");
                        mAuth.signOut();
                        navigateToLogin();
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error al cargar datos", e);
                    binding.tvUserName.setText("Error al cargar");
                    binding.tvUserPuntaje.setText("Puntaje: Error");
                });
    }

    // --- NUEVA FUNCIÓN DE ANIMACIÓN ---
    private void animarMenu() {
        Animation fadeIn = AnimationUtils.loadAnimation(this, R.anim.fade_in);
        LinearLayout menuContainer = binding.llMenuContainer;

        // Animar cada tarjeta una por una con un ligero retraso
        for (int i = 0; i < menuContainer.getChildCount(); i++) {
            View child = menuContainer.getChildAt(i);
            if (child instanceof MaterialCardView) {
                Animation anim = AnimationUtils.loadAnimation(this, R.anim.fade_in);
                anim.setStartOffset(i * 100L); // 100ms de retraso por cada tarjeta
                child.startAnimation(anim);
            }
        }
    }

    // --- FUNCIÓN DE INSIGNIAS ---
    private void actualizarInsignias(long puntaje) {
        // Insignia 1: 50 Puntos
        if (puntaje >= 50) {
            binding.ivBadge1.setAlpha(1.0f); // Completamente visible
            binding.ivBadge1.setOnClickListener(v -> Toast.makeText(this, "Insignia: Principiante (50 pts)", Toast.LENGTH_SHORT).show());
        } else {
            binding.ivBadge1.setAlpha(0.3f); // Opaco (bloqueado)
            binding.ivBadge1.setOnClickListener(null); // No hacer nada si está bloqueada
        }

        // Insignia 2: 150 Puntos
        if (puntaje >= 150) {
            binding.ivBadge2.setAlpha(1.0f);
            binding.ivBadge2.setOnClickListener(v -> Toast.makeText(this, "Insignia: Experto (150 pts)", Toast.LENGTH_SHORT).show());
        } else {
            binding.ivBadge2.setAlpha(0.3f);
            binding.ivBadge2.setOnClickListener(null);
        }

        // Insignia 3: 300 Puntos
        if (puntaje >= 300) {
            binding.ivBadge3.setAlpha(1.0f);
            binding.ivBadge3.setOnClickListener(v -> Toast.makeText(this, "Insignia: Maestro (300 pts)", Toast.LENGTH_SHORT).show());
        } else {
            binding.ivBadge3.setAlpha(0.3f);
            binding.ivBadge3.setOnClickListener(null);
        }
    }

    private void setupNivelButton(MaterialCardView button, ImageView lockIcon, TextView textView, ImageView iconView,
                                  String nivel, int nivelRequerido, int colorDesbloqueado, int textColorDesbloqueado, int puntosRequeridos) {

        // Colores base definidos en colors.xml
        int colorBloqueado = ContextCompat.getColor(this, R.color.game_locked);
        int colorBgBloqueado = ContextCompat.getColor(this, R.color.game_locked_bg); // Fondo gris suave
        int colorIconoDesbloqueado = ContextCompat.getColor(this, colorDesbloqueado);
        int colorTextoDesbloqueado = ContextCompat.getColor(this, textColorDesbloqueado);
        int colorBgDesbloqueado = ContextCompat.getColor(this, R.color.white); // Fondo blanco limpio

        if (userNivel >= nivelRequerido) {
            // --- ESTADO: DESBLOQUEADO (Activo) ---
            button.setEnabled(true);
            button.setClickable(true);

            // Estilo visual activo: Fondo blanco, borde sutil del color del nivel
            button.setCardBackgroundColor(colorBgDesbloqueado);
            button.setStrokeColor(colorIconoDesbloqueado);
            button.setStrokeWidth(2); // Borde delgado para resaltar
            button.setCardElevation(6f); // Elevación para efecto "flotante"

            // Ocultar candado
            if (lockIcon != null) {
                lockIcon.setVisibility(View.GONE);
            }

            // Textos e Iconos coloridos
            textView.setTextColor(colorTextoDesbloqueado);
            textView.setText(nivel);
            iconView.setImageTintList(ColorStateList.valueOf(colorIconoDesbloqueado));

            // Listener de Clic con Animación Nueva
            button.setOnClickListener(v -> {
                Log.d(TAG, "Iniciando nivel: " + nivel);
                Intent intent = new Intent(MainActivity.this, GameLevelActivity.class);
                intent.putExtra(KEY_NIVEL_JUEGO, nivel);
                intent.putExtra(KEY_PUNTAJE_ACTUAL, userPuntaje);
                intent.putExtra(KEY_NIVEL_DESBLOQUEADO, userNivel);
                startActivity(intent);

                // --- AQUÍ ESTÁ LA MAGIA: Transición suave hacia arriba ---
                overridePendingTransition(R.anim.slide_up, R.anim.fade_in);
            });

        } else {
            // --- ESTADO: BLOQUEADO (Inactivo) ---
            button.setEnabled(false);
            button.setClickable(false);

            // Estilo visual bloqueado: Fondo gris plano, sin borde, sin sombra
            button.setCardBackgroundColor(colorBgBloqueado);
            button.setStrokeWidth(0);
            button.setCardElevation(0f);

            // Mostrar candado en gris
            if (lockIcon != null) {
                lockIcon.setVisibility(View.VISIBLE);
                lockIcon.setImageTintList(ColorStateList.valueOf(colorBloqueado));
            }

            // Textos e Iconos en gris
            textView.setTextColor(colorBloqueado);
            textView.setText("Bloqueado"); // Texto informativo
            iconView.setImageTintList(ColorStateList.valueOf(colorBloqueado));
        }
    }

    private void navigateToLogin() {
        Intent intent = new Intent(MainActivity.this, LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    @Override
    public void onQuizClick(String itemText, String itemType) {
        Log.d(TAG, "Clic en Quiz (ignorado en MainActivity): " + itemType);
    }

    @Override
    public void onCaseStudyClick(String itemText, String itemType) {
        Log.d(TAG, "Clic en Caso de Estudio (ignorado en MainActivity): " + itemType);
    }

}
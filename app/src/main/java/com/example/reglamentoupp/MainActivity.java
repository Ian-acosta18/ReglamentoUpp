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
        animarMenu(); // Animación de entrada de los elementos del menú
    }

    private void loadUserData() {
        if (userID == null) return;

        mStore.collection("usuarios").document(userID).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        String nombre = documentSnapshot.getString("nombre");
                        if (nombre != null && !nombre.isEmpty()) {
                            binding.tvUserName.setText("Hola, " + nombre);
                        } else {
                            binding.tvUserName.setText(documentSnapshot.getString("email"));
                        }

                        Long puntajeDb = documentSnapshot.getLong("puntaje");
                        if (puntajeDb != null) {
                            userPuntaje = puntajeDb;
                        }
                        binding.tvUserPuntaje.setText(userPuntaje + " XP");

                        Long nivelDb = documentSnapshot.getLong("nivelDesbloqueado");
                        if (nivelDb != null) {
                            userNivel = nivelDb.intValue();
                        }

                        Log.d(TAG, "Usuario cargado. Puntaje: " + userPuntaje + ", Nivel Desbloqueado: " + userNivel);

                        // --- Configurar Listeners de Juegos Arcade ---
                        binding.btnJugarModoDesafio.setOnClickListener(v -> {
                            startActivity(new Intent(MainActivity.this, QuizActivity.class));
                        });

                        binding.btnJugarVerdaderoFalso.setOnClickListener(v -> {
                            startActivity(new Intent(MainActivity.this, TrueFalseActivity.class));
                        });

                        binding.btnJugarAhorcado.setOnClickListener(v -> {
                            startActivity(new Intent(MainActivity.this, HangmanActivity.class));
                        });

                        // --- Configurar botones de Nivel (Lista Vertical) ---
                        // Configuración para Nivel 1: Derechos
                        setupNivelButton(binding.btnJugarDerechos, null, binding.tvDerechos, binding.ivDerechos,
                                "Derechos", 1, R.color.upp_primary, R.color.text_primary);

                        // Configuración para Nivel 2: Obligaciones
                        setupNivelButton(binding.btnJugarObligaciones, binding.ivLockObligaciones, binding.tvObligaciones, binding.ivObligaciones,
                                "Obligaciones", 2, R.color.upp_primary, R.color.text_primary);

                        // Configuración para Nivel 3: Prohibiciones
                        setupNivelButton(binding.btnJugarProhibiciones, binding.ivLockProhibiciones, binding.tvProhibiciones, binding.ivProhibiciones,
                                "Prohibiciones", 3, R.color.upp_primary, R.color.text_primary);

                        // Configuración para Nivel 4: Sanciones
                        setupNivelButton(binding.btnJugarSanciones, binding.ivLockSanciones, binding.tvSanciones, binding.ivSanciones,
                                "Sanciones", 4, R.color.upp_primary, R.color.text_primary);

                        // Configuración para Nivel 5: Reconocimientos
                        setupNivelButton(binding.btnJugarReconocimientos, binding.ivLockReconocimientos, binding.tvReconocimientos, binding.ivReconocimientos,
                                "Reconocimientos", 5, R.color.upp_primary, R.color.text_primary);

                    } else {
                        Log.w(TAG, "No existe el documento del usuario en Firestore.");
                        mAuth.signOut();
                        navigateToLogin();
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error al cargar datos", e);
                    binding.tvUserName.setText("Error de conexión");
                    binding.tvUserPuntaje.setText("---");
                });
    }

    private void animarMenu() {
        // Animación simple de aparición en cascada para los contenedores principales
        LinearLayout menuContainer = binding.llMenuContainer;
        for (int i = 0; i < menuContainer.getChildCount(); i++) {
            View child = menuContainer.getChildAt(i);
            Animation anim = AnimationUtils.loadAnimation(this, R.anim.fade_in);
            anim.setStartOffset(i * 100L);
            child.startAnimation(anim);
        }
    }

    /**
     * Configura el aspecto visual y funcional de un botón de nivel.
     */
    private void setupNivelButton(MaterialCardView button, ImageView lockIcon, TextView textView, ImageView iconView,
                                  String nivelNombre, int nivelRequerido, int colorDesbloqueado, int textColorDesbloqueado) {

        // Colores desde recursos
        int colorBloqueado = ContextCompat.getColor(this, R.color.game_locked); // Gris texto
        int colorBgBloqueado = ContextCompat.getColor(this, R.color.game_locked_bg); // Gris fondo
        int colorTextoDesbloqueado = ContextCompat.getColor(this, textColorDesbloqueado); // Negro/Gris oscuro
        int colorBgDesbloqueado = ContextCompat.getColor(this, R.color.white); // Blanco puro

        if (userNivel >= nivelRequerido) {
            // --- ESTADO: DESBLOQUEADO (Activo) ---
            button.setEnabled(true);
            button.setClickable(true);

            // Diseño "Limpio y Elevado"
            button.setCardBackgroundColor(colorBgDesbloqueado);
            button.setStrokeWidth(0); // Sin borde para look moderno
            button.setCardElevation(12f); // Sombra pronunciada

            // Ocultar candado
            if (lockIcon != null) {
                lockIcon.setVisibility(View.GONE);
            }

            // Colorear textos
            textView.setTextColor(colorTextoDesbloqueado);
            textView.setText(nivelNombre);

            // --- MOSTRAR ICONO ORIGINAL (SIN TINT) ---
            iconView.clearColorFilter(); // Limpia el filtro gris/morado
            iconView.setImageTintList(null); // Asegura que no haya tinte

            // Fondo circular sutil para el icono
            iconView.setBackgroundResource(R.drawable.white_circle_bg);
            iconView.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(this, R.color.app_bg)));

            // Listener para abrir el nivel con animación
            button.setOnClickListener(v -> {
                Intent intent = new Intent(MainActivity.this, GameLevelActivity.class);
                intent.putExtra(KEY_NIVEL_JUEGO, nivelNombre);
                intent.putExtra(KEY_PUNTAJE_ACTUAL, userPuntaje);
                intent.putExtra(KEY_NIVEL_DESBLOQUEADO, userNivel);
                startActivity(intent);
                // Transición suave hacia arriba
                overridePendingTransition(R.anim.slide_up, R.anim.fade_in);
            });

        } else {
            // --- ESTADO: BLOQUEADO (Inactivo) ---
            button.setEnabled(false);
            button.setClickable(false);

            // Diseño "Plano y Gris"
            button.setCardBackgroundColor(colorBgBloqueado);
            button.setStrokeWidth(0);
            button.setCardElevation(0f); // Sin sombra

            // Mostrar candado
            if (lockIcon != null) {
                lockIcon.setVisibility(View.VISIBLE);
                lockIcon.setImageTintList(ColorStateList.valueOf(colorBloqueado));
            }

            // Textos en gris
            textView.setTextColor(colorBloqueado);

            // ICONO BLOQUEADO: Se pinta de GRIS
            iconView.setColorFilter(colorBloqueado);
            iconView.setBackground(null); // Quitar fondo circular
        }
    }

    private void navigateToLogin() {
        Intent intent = new Intent(MainActivity.this, LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    // Métodos de la interfaz ReglamentoInteractionListener
    @Override
    public void onQuizClick(String itemText, String itemType) {
        Log.d(TAG, "Clic en Quiz (ignorado en MainActivity): " + itemType);
    }

    @Override
    public void onCaseStudyClick(String itemText, String itemType) {
        Log.d(TAG, "Clic en Caso de Estudio (ignorado en MainActivity): " + itemType);
    }
}
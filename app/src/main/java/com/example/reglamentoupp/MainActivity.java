package com.example.reglamentoupp;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.example.reglamentoupp.databinding.ActivityMainBinding;
import com.google.android.material.card.MaterialCardView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Calendar;

public class MainActivity extends AppCompatActivity implements BaseReglamentoFragment.ReglamentoInteractionListener {

    private ActivityMainBinding binding;
    private FirebaseAuth mAuth;
    private FirebaseFirestore mStore;
    private String userID;
    private long userPuntaje = 0;
    private int userNivel = 1;

    public static final String KEY_NIVEL_JUEGO = "nivelJuego";
    public static final String KEY_PUNTAJE_ACTUAL = "puntajeActual";
    public static final String KEY_NIVEL_DESBLOQUEADO = "nivelDesbloqueado";
    private static final String TAG = "MainActivity";

    // --- Variables para la Rotación de Mensajes ---
    private Handler handlerRotacion = new Handler(Looper.getMainLooper());
    private int indiceMensaje = 0;
    private final String[] mensajesRotativos = {
            "¡Hola! ¿Listo para aprender?",
            "Recuerda revisar tus obligaciones.",
            "¡Gana puntos en los juegos rápidos!",
            "El saber no ocupa lugar 📚",
            "¿Ya desbloqueaste el Nivel 2?",
            "La constancia es la clave del éxito."
    };

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
        actualizarSaludoInicial();
        iniciarRotacionMensajes();
        loadUserData();
        animarMenu();
    }

    @Override
    protected void onPause() {
        super.onPause();
        detenerRotacionMensajes();
    }

    // --- Lógica de Mensajes Rotativos ---
    private void iniciarRotacionMensajes() {
        handlerRotacion.postDelayed(runnableRotacion, 4000);
    }

    private void detenerRotacionMensajes() {
        handlerRotacion.removeCallbacks(runnableRotacion);
    }

    private Runnable runnableRotacion = new Runnable() {
        @Override
        public void run() {
            if (binding != null && binding.tvWelcomeBubble != null) {
                binding.tvWelcomeBubble.animate().alpha(0f).setDuration(300).withEndAction(() -> {
                    indiceMensaje = (indiceMensaje + 1) % mensajesRotativos.length;
                    binding.tvWelcomeBubble.setText(mensajesRotativos[indiceMensaje]);
                    binding.tvWelcomeBubble.animate().alpha(1f).setDuration(300).start();
                }).start();
                handlerRotacion.postDelayed(this, 4000);
            }
        }
    };

    private void actualizarSaludoInicial() {
        Calendar calendar = Calendar.getInstance();
        int hora = calendar.get(Calendar.HOUR_OF_DAY);
        String saludo;

        if (hora >= 5 && hora < 12) {
            saludo = "¡Buenos días! ☀️\n¿Listo para aprender?";
        } else if (hora >= 12 && hora < 19) {
            saludo = "¡Buenas tardes! 🌤️\nRepasemos un poco.";
        } else {
            saludo = "¡Buenas noches! 🌙\nNunca es tarde para estudiar.";
        }

        if (binding.tvWelcomeBubble != null) {
            binding.tvWelcomeBubble.setText(saludo);
            binding.tvWelcomeBubble.setAlpha(1f);
        }

        if (binding.lottieWelcome != null) {
            binding.lottieWelcome.playAnimation();
        }
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

                        // Listeners de Juegos
                        binding.btnJugarModoDesafio.setOnClickListener(v -> startActivity(new Intent(MainActivity.this, QuizActivity.class)));
                        binding.btnJugarVerdaderoFalso.setOnClickListener(v -> startActivity(new Intent(MainActivity.this, TrueFalseActivity.class)));
                        binding.btnJugarAhorcado.setOnClickListener(v -> startActivity(new Intent(MainActivity.this, HangmanActivity.class)));

                        // BOTÓN MEMORAMA
                        binding.btnJugarMemorama.setOnClickListener(v -> startActivity(new Intent(MainActivity.this, MemoryGameActivity.class)));

                        // Configurar Niveles
                        setupNivelButton(binding.btnJugarDerechos, null, binding.tvDerechos, binding.ivDerechos, "Derechos", 1, R.color.upp_primary, R.color.text_primary);
                        setupNivelButton(binding.btnJugarObligaciones, binding.ivLockObligaciones, binding.tvObligaciones, binding.ivObligaciones, "Obligaciones", 2, R.color.upp_primary, R.color.text_primary);
                        setupNivelButton(binding.btnJugarProhibiciones, binding.ivLockProhibiciones, binding.tvProhibiciones, binding.ivProhibiciones, "Prohibiciones", 3, R.color.upp_primary, R.color.text_primary);
                        setupNivelButton(binding.btnJugarSanciones, binding.ivLockSanciones, binding.tvSanciones, binding.ivSanciones, "Sanciones", 4, R.color.upp_primary, R.color.text_primary);
                        setupNivelButton(binding.btnJugarReconocimientos, binding.ivLockReconocimientos, binding.tvReconocimientos, binding.ivReconocimientos, "Reconocimientos", 5, R.color.upp_primary, R.color.text_primary);

                    } else {
                        mAuth.signOut();
                        navigateToLogin();
                    }
                })
                .addOnFailureListener(e -> {
                    binding.tvUserName.setText("Error de conexión");
                    binding.tvUserPuntaje.setText("---");
                });
    }

    private void animarMenu() {
        LinearLayout menuContainer = binding.llMenuContainer;
        for (int i = 0; i < menuContainer.getChildCount(); i++) {
            View child = menuContainer.getChildAt(i);
            Animation anim = AnimationUtils.loadAnimation(this, R.anim.fade_in);
            anim.setStartOffset(i * 100L);
            child.startAnimation(anim);
        }
    }

    private void setupNivelButton(MaterialCardView button, ImageView lockIcon, TextView textView, ImageView iconView,
                                  String nivelNombre, int nivelRequerido, int colorDesbloqueado, int textColorDesbloqueado) {

        int colorBloqueado = ContextCompat.getColor(this, R.color.game_locked);
        int colorBgBloqueado = ContextCompat.getColor(this, R.color.game_locked_bg);
        int colorTextoDesbloqueado = ContextCompat.getColor(this, textColorDesbloqueado);
        int colorBgDesbloqueado = ContextCompat.getColor(this, R.color.white);

        if (userNivel >= nivelRequerido) {
            button.setEnabled(true);
            button.setClickable(true);
            button.setCardBackgroundColor(colorBgDesbloqueado);
            button.setStrokeWidth(0);
            button.setCardElevation(12f);

            if (lockIcon != null) lockIcon.setVisibility(View.GONE);

            textView.setTextColor(colorTextoDesbloqueado);
            textView.setText(nivelNombre);

            iconView.clearColorFilter();
            iconView.setImageTintList(null);
            iconView.setBackgroundResource(R.drawable.white_circle_bg);
            iconView.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(this, R.color.app_bg)));

            button.setOnClickListener(v -> {
                Intent intent = new Intent(MainActivity.this, GameLevelActivity.class);
                intent.putExtra(KEY_NIVEL_JUEGO, nivelNombre);
                intent.putExtra(KEY_PUNTAJE_ACTUAL, userPuntaje);
                intent.putExtra(KEY_NIVEL_DESBLOQUEADO, userNivel);
                startActivity(intent);
                overridePendingTransition(R.anim.slide_up, R.anim.fade_in);
            });

        } else {
            button.setEnabled(false);
            button.setClickable(false);
            button.setCardBackgroundColor(colorBgBloqueado);
            button.setStrokeWidth(0);
            button.setCardElevation(0f);

            if (lockIcon != null) {
                lockIcon.setVisibility(View.VISIBLE);
                lockIcon.setImageTintList(ColorStateList.valueOf(colorBloqueado));
            }

            textView.setTextColor(colorBloqueado);
            iconView.setColorFilter(colorBloqueado);
            iconView.setBackground(null);
        }
    }

    private void navigateToLogin() {
        Intent intent = new Intent(MainActivity.this, LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    @Override
    public void onQuizClick(String itemText, String itemType) {}

    @Override
    public void onCaseStudyClick(String itemText, String itemType) {}
}
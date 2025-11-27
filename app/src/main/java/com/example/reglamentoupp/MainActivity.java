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
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.example.reglamentoupp.databinding.ActivityMainBinding;
import com.google.android.material.card.MaterialCardView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

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

        // Configurar botones que no cambian (Quiz, Ahorcado, Ranking, etc.)
        setupStaticGameListeners();

        // --- IMPORTANTE: DESCOMENTA ESTA LÍNEA SOLO UNA VEZ PARA SUBIR LAS PREGUNTAS NUEVAS ---
        // cargarPreguntasRealesUPP();
    }

    @Override
    protected void onResume() {
        super.onResume();
        actualizarSaludoInicial();
        iniciarRotacionMensajes();
        loadUserData(); // Cargar datos del usuario (Nivel y Puntaje)
        animarMenu();
    }

    @Override
    protected void onPause() {
        super.onPause();
        detenerRotacionMensajes();
    }

    // --- Configuración de Botones Estáticos ---
    private void setupStaticGameListeners() {
        binding.btnJugarModoDesafio.setOnClickListener(v -> startActivity(new Intent(MainActivity.this, QuizActivity.class)));
        binding.btnJugarVerdaderoFalso.setOnClickListener(v -> startActivity(new Intent(MainActivity.this, TrueFalseActivity.class)));
        binding.btnJugarAhorcado.setOnClickListener(v -> startActivity(new Intent(MainActivity.this, HangmanActivity.class)));
        binding.btnJugarMemorama.setOnClickListener(v -> startActivity(new Intent(MainActivity.this, MemoryGameActivity.class)));

        // --- NUEVO LISTENER PARA EL RANKING ---
        binding.btnVerRanking.setOnClickListener(v -> startActivity(new Intent(MainActivity.this, RankingActivity.class)));
    }

    // --- Lógica de Mensajes Rotativos ---
    private void iniciarRotacionMensajes() {
        handlerRotacion.removeCallbacks(runnableRotacion);
        handlerRotacion.postDelayed(runnableRotacion, 4000);
    }

    private void detenerRotacionMensajes() {
        handlerRotacion.removeCallbacks(runnableRotacion);
    }

    private Runnable runnableRotacion = new Runnable() {
        @Override
        public void run() {
            // Verificación de seguridad: Si la actividad se está cerrando o el binding es nulo, no hacer nada.
            if (!isFinishing() && binding != null && binding.tvWelcomeBubble != null) {
                binding.tvWelcomeBubble.animate().alpha(0f).setDuration(300).withEndAction(() -> {
                    // Doble verificación dentro del callback de animación
                    if (!isFinishing() && binding != null && binding.tvWelcomeBubble != null) {
                        indiceMensaje = (indiceMensaje + 1) % mensajesRotativos.length;
                        binding.tvWelcomeBubble.setText(mensajesRotativos[indiceMensaje]);
                        binding.tvWelcomeBubble.animate().alpha(1f).setDuration(300).start();
                    }
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

        if (binding != null && binding.tvWelcomeBubble != null) {
            binding.tvWelcomeBubble.setText(saludo);
            binding.tvWelcomeBubble.setAlpha(1f);
        }

        if (binding != null && binding.lottieWelcome != null) {
            binding.lottieWelcome.playAnimation();
        }
    }

    private void loadUserData() {
        if (userID == null) return;

        mStore.collection("usuarios").document(userID).get()
                .addOnSuccessListener(documentSnapshot -> {
                    // Verificación crítica para evitar crashes
                    if (isFinishing() || isDestroyed() || binding == null) return;

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
                        } else {
                            userNivel = 1;
                        }

                        actualizarUIdeNivel(userNivel);

                        // Configurar Niveles (Dependen del nivel desbloqueado)
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
                    if (isFinishing() || isDestroyed() || binding == null) return;
                    binding.tvUserName.setText("Error de conexión");
                    binding.tvUserPuntaje.setText("---");
                });
    }

    private void actualizarUIdeNivel(int nivel) {
        if (binding == null) return;

        binding.tvUserLevel.setText("Nivel " + nivel);

        int iconRes;
        switch (nivel) {
            case 1:
                iconRes = R.drawable.ic_derechos;
                break;
            case 2:
                iconRes = R.drawable.ic_obligaciones;
                break;
            case 3:
                iconRes = R.drawable.ic_prohibiciones;
                break;
            case 4:
                iconRes = R.drawable.ic_sanciones;
                break;
            case 5:
                iconRes = R.drawable.ic_reconocimientos;
                break;
            default:
                iconRes = R.drawable.ic_check_circle;
                break;
        }
        binding.ivUserLevelIcon.setImageResource(iconRes);
    }

    private void animarMenu() {
        if (binding == null) return;
        LinearLayout menuContainer = binding.llMenuContainer;
        for (int i = 0; i < menuContainer.getChildCount(); i++) {
            View child = menuContainer.getChildAt(i);
            if (child.getVisibility() == View.VISIBLE) {
                Animation anim = AnimationUtils.loadAnimation(this, R.anim.fade_in);
                anim.setStartOffset(i * 100L);
                child.startAnimation(anim);
            }
        }
    }

    private void setupNivelButton(MaterialCardView button, ImageView lockIcon, TextView textView, ImageView iconView,
                                  String nivelNombre, int nivelRequerido, int colorDesbloqueado, int textColorDesbloqueado) {

        if (button == null || textView == null || iconView == null) return;

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

    // --- MÉTODO PARA CARGAR PREGUNTAS ESPECÍFICAS UPP A FIREBASE ---
    private void cargarPreguntasRealesUPP() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        // 1. TRIVIA (QUIZ) - Datos técnicos del reglamento
        List<Pregunta> quizUPP = new ArrayList<>();

        quizUPP.add(new Pregunta(
                "¿Calificación mínima aprobatoria en la UPP (escala 0-10)?",
                "6.0", "7.0", "8.0",
                "7.0"
        ));

        quizUPP.add(new Pregunta(
                "¿Duración mínima de la Estadía para Licenciatura?",
                "480 horas", "500 horas", "600 horas",
                "600 horas"
        ));

        quizUPP.add(new Pregunta(
                "¿Qué causa baja definitiva inmediata (sin amonestación)?",
                "Reprobar 1 parcial", "Faltar 2 semanas consecutivas", "Llegar tarde 3 veces",
                "Faltar 2 semanas consecutivas"
        ));

        quizUPP.add(new Pregunta(
                "¿Nivel de inglés requerido para la titulación?",
                "Básico A2", "Intermedio B1", "Avanzado C1",
                "Intermedio B1"
        ));

        quizUPP.add(new Pregunta(
                "¿Quién sanciona el plagio en proyectos de Estadía?",
                "El Rector", "Servicios Escolares", "Consejo de Calidad",
                "Consejo de Calidad"
        ));

        quizUPP.add(new Pregunta(
                "¿Porcentaje de asistencia para tener derecho a evaluación ordinaria?",
                "60%", "80%", "100%",
                "80%"
        ));

        for (Pregunta p : quizUPP) {
            db.collection("preguntas").add(p);
        }

        // 2. VERDADERO O FALSO - Situaciones reglamentarias
        List<PreguntaVF> vfUPP = new ArrayList<>();

        vfUPP.add(new PreguntaVF("¿La Estadía Profesional se realiza en el primer cuatrimestre?", false));
        vfUPP.add(new PreguntaVF("¿Debes liberar el Servicio Social antes de iniciar la Estadía?", true));
        vfUPP.add(new PreguntaVF("¿La calificación mínima aprobatoria es 6.0?", false));
        vfUPP.add(new PreguntaVF("¿Cometer plagio en la Estadía suspende tu proceso de titulación?", true));
        vfUPP.add(new PreguntaVF("¿Es posible solicitar una baja temporal por motivos de salud/embarazo?", true));
        vfUPP.add(new PreguntaVF("¿El Rector es elegido por votación directa de los alumnos?", false));

        for (PreguntaVF p : vfUPP) {
            db.collection("preguntasVF").add(p);
        }

        Toast.makeText(this, "¡Preguntas UPP Experto subidas!", Toast.LENGTH_LONG).show();
    }

    @Override
    public void onQuizClick(String itemText, String itemType) {}

    @Override
    public void onCaseStudyClick(String itemText, String itemType) {}
}
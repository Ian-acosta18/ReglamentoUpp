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
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.reglamentoupp.databinding.ActivityMainBinding;
import com.google.android.material.card.MaterialCardView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

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

    // --- Variables para el Ranking Automático ---
    private RecyclerView recyclerRanking;
    private RankingAdapter rankingAdapter;
    private List<Usuario> listaRanking;
    private ListenerRegistration rankingListener;

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

        // Configurar botones de juegos
        setupStaticGameListeners();

        // --- INICIALIZAR RANKING ---
        setupRankingAutomatico();
    }

    private void setupRankingAutomatico() {
        // Enlazamos el RecyclerView del diseño (asegúrate de haberlo puesto en el XML con este ID)
        recyclerRanking = binding.getRoot().findViewById(R.id.recyclerViewRankingMain);

        // Si usas binding, también podrías acceder como binding.recyclerViewRankingMain (si el ID existe)
        // Por seguridad, usamos findViewById si no estás seguro de que binding se actualizó.

        if (recyclerRanking != null) {
            listaRanking = new ArrayList<>();
            rankingAdapter = new RankingAdapter(listaRanking);
            recyclerRanking.setLayoutManager(new LinearLayoutManager(this));
            recyclerRanking.setAdapter(rankingAdapter);

            // Llamamos a la carga de datos
            cargarDatosRanking();
        }
    }

    private void cargarDatosRanking() {
        // Escuchamos en tiempo real, limitando a los Top 10 para no saturar la pantalla principal
        rankingListener = mStore.collection("usuarios")
                .orderBy("puntaje", Query.Direction.DESCENDING)
                .limit(10)
                .addSnapshotListener((value, error) -> {
                    if (error != null) {
                        Log.e(TAG, "Error al cargar ranking: " + error.getMessage());
                        return;
                    }
                    if (value != null) {
                        listaRanking.clear();
                        for (QueryDocumentSnapshot doc : value) {
                            Usuario user = doc.toObject(Usuario.class);
                            listaRanking.add(user);
                        }
                        rankingAdapter.notifyDataSetChanged();
                    }
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

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Detenemos la escucha del ranking para ahorrar datos y batería al cerrar
        if (rankingListener != null) {
            rankingListener.remove();
        }
    }

    // --- Configuración de Botones Estáticos ---
    private void setupStaticGameListeners() {
        binding.btnJugarModoDesafio.setOnClickListener(v -> startActivity(new Intent(MainActivity.this, QuizActivity.class)));
        binding.btnJugarVerdaderoFalso.setOnClickListener(v -> startActivity(new Intent(MainActivity.this, TrueFalseActivity.class)));
        binding.btnJugarAhorcado.setOnClickListener(v -> startActivity(new Intent(MainActivity.this, HangmanActivity.class)));
        binding.btnJugarMemorama.setOnClickListener(v -> startActivity(new Intent(MainActivity.this, MemoryGameActivity.class)));

        // Ya NO necesitamos el listener del botón ranking porque ahora es automático
    }

    // --- Lógica de Mensajes Rotativos (Igual que antes) ---
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
            if (!isFinishing() && binding != null && binding.tvWelcomeBubble != null) {
                binding.tvWelcomeBubble.animate().alpha(0f).setDuration(300).withEndAction(() -> {
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
        if (hora >= 5 && hora < 12) saludo = "¡Buenos días! ☀️";
        else if (hora >= 12 && hora < 19) saludo = "¡Buenas tardes! 🌤️";
        else saludo = "¡Buenas noches! 🌙";

        saludo += "\n¿Listo para aprender?";

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
                    if (isFinishing() || isDestroyed() || binding == null) return;
                    if (documentSnapshot.exists()) {
                        String nombre = documentSnapshot.getString("nombre");
                        if (nombre != null && !nombre.isEmpty()) binding.tvUserName.setText("Hola, " + nombre);
                        else binding.tvUserName.setText(documentSnapshot.getString("email"));

                        Long puntajeDb = documentSnapshot.getLong("puntaje");
                        if (puntajeDb != null) userPuntaje = puntajeDb;
                        binding.tvUserPuntaje.setText(userPuntaje + " XP");

                        Long nivelDb = documentSnapshot.getLong("nivelDesbloqueado");
                        userNivel = (nivelDb != null) ? nivelDb.intValue() : 1;

                        actualizarUIdeNivel(userNivel);
                        configurarBotonesNiveles();
                    }
                });
    }

    private void actualizarUIdeNivel(int nivel) {
        if (binding == null) return;
        binding.tvUserLevel.setText("Nivel " + nivel);
        int iconRes;
        switch (nivel) {
            case 1: iconRes = R.drawable.ic_derechos; break;
            case 2: iconRes = R.drawable.ic_obligaciones; break;
            case 3: iconRes = R.drawable.ic_prohibiciones; break;
            case 4: iconRes = R.drawable.ic_sanciones; break;
            case 5: iconRes = R.drawable.ic_reconocimientos; break;
            default: iconRes = R.drawable.ic_check_circle; break;
        }
        binding.ivUserLevelIcon.setImageResource(iconRes);
    }

    private void configurarBotonesNiveles() {
        setupNivelButton(binding.btnJugarDerechos, null, binding.tvDerechos, binding.ivDerechos, "Derechos", 1, R.color.upp_primary);
        setupNivelButton(binding.btnJugarObligaciones, binding.ivLockObligaciones, binding.tvObligaciones, binding.ivObligaciones, "Obligaciones", 2, R.color.upp_primary);
        setupNivelButton(binding.btnJugarProhibiciones, binding.ivLockProhibiciones, binding.tvProhibiciones, binding.ivProhibiciones, "Prohibiciones", 3, R.color.upp_primary);
        setupNivelButton(binding.btnJugarSanciones, binding.ivLockSanciones, binding.tvSanciones, binding.ivSanciones, "Sanciones", 4, R.color.upp_primary);
        setupNivelButton(binding.btnJugarReconocimientos, binding.ivLockReconocimientos, binding.tvReconocimientos, binding.ivReconocimientos, "Reconocimientos", 5, R.color.upp_primary);
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
                                  String nivelNombre, int nivelRequerido, int textColorDesbloqueado) {
        if (button == null) return;

        int colorBloqueado = ContextCompat.getColor(this, R.color.game_locked);
        int colorBgBloqueado = ContextCompat.getColor(this, R.color.game_locked_bg);
        int colorTextoDesbloqueado = ContextCompat.getColor(this, textColorDesbloqueado);
        int colorBgDesbloqueado = ContextCompat.getColor(this, R.color.white);

        if (userNivel >= nivelRequerido) {
            button.setEnabled(true);
            button.setClickable(true);
            button.setCardBackgroundColor(colorBgDesbloqueado);
            button.setCardElevation(8f);
            if (lockIcon != null) lockIcon.setVisibility(View.GONE);
            textView.setTextColor(colorTextoDesbloqueado);
            textView.setText(nivelNombre);
            iconView.clearColorFilter();

            button.setOnClickListener(v -> {
                Intent intent = new Intent(MainActivity.this, GameLevelActivity.class);
                intent.putExtra(KEY_NIVEL_JUEGO, nivelNombre);
                intent.putExtra(KEY_PUNTAJE_ACTUAL, userPuntaje);
                intent.putExtra(KEY_NIVEL_DESBLOQUEADO, userNivel);
                startActivity(intent);
            });
        } else {
            button.setEnabled(false);
            button.setCardBackgroundColor(colorBgBloqueado);
            button.setCardElevation(0f);
            if (lockIcon != null) lockIcon.setVisibility(View.VISIBLE);
            textView.setTextColor(colorBloqueado);
            iconView.setColorFilter(colorBloqueado);
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
package com.example.reglamentoupp;

import android.animation.ObjectAnimator;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.animation.DecelerateInterpolator;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.cloudinary.android.MediaManager;
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

    // Barras de progreso para las 5 categorías
    private ProgressBar progressDerechos, progressObligaciones, progressProhibiciones, progressSanciones, progressReconocimientos;

    // Textos e Íconos de Estado para "Tu Camino"
    private TextView tvStatusDerechos, tvStatusObligaciones, tvStatusProhibiciones, tvStatusSanciones, tvStatusReconocimientos;
    private ImageView ivStatusDerechos, ivStatusObligaciones, ivStatusProhibiciones, ivStatusSanciones, ivStatusReconocimientos;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initCloudinary();

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        vincularVistas();
        configurarNavegacion();
        configurarAcciones();
    }

    @Override
    protected void onResume() {
        super.onResume();
        cargarDatosUsuario();
    }

    private void vincularVistas() {
        tvUserName = findViewById(R.id.tvUserName);
        tvUserPuntaje = findViewById(R.id.tvUserPuntaje);
        tvUserLevel = findViewById(R.id.tvUserLevel);
        btnLogout = findViewById(R.id.btnLogout);
        ivUserProfile = findViewById(R.id.ivUserProfile);
        cardProfilePic = findViewById(R.id.cardProfilePic);

        // Barras de Progreso
        progressDerechos = findViewById(R.id.progressDerechos);
        progressObligaciones = findViewById(R.id.progressObligaciones);
        progressProhibiciones = findViewById(R.id.progressProhibiciones);
        progressSanciones = findViewById(R.id.progressSanciones);
        progressReconocimientos = findViewById(R.id.progressReconocimientos);

        // Textos de estado (Nivel 1 • Desbloqueado, etc.)
        tvStatusDerechos = findViewById(R.id.tvStatusDerechos);
        tvStatusObligaciones = findViewById(R.id.tvStatusObligaciones);
        tvStatusProhibiciones = findViewById(R.id.tvStatusProhibiciones);
        tvStatusSanciones = findViewById(R.id.tvStatusSanciones);
        tvStatusReconocimientos = findViewById(R.id.tvStatusReconocimientos);

        // Íconos de estado (Candado / Palomita)
        ivStatusDerechos = findViewById(R.id.ivStatusDerechos);
        ivStatusObligaciones = findViewById(R.id.ivStatusObligaciones);
        ivStatusProhibiciones = findViewById(R.id.ivStatusProhibiciones);
        ivStatusSanciones = findViewById(R.id.ivStatusSanciones);
        ivStatusReconocimientos = findViewById(R.id.ivStatusReconocimientos);

        reiniciarBarras();
    }

    private void configurarAcciones() {
        cardProfilePic.setOnClickListener(v -> startActivity(new Intent(MainActivity.this, ProfileActivity.class)));

        if (btnLogout != null) {
            btnLogout.setOnClickListener(v -> {
                mAuth.signOut();
                Intent intent = new Intent(MainActivity.this, LoginActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                finish();
            });
        }

        // Configuración de clics para juegos rápidos
        View btnModoDesafio = findViewById(R.id.btnJugarModoDesafio);
        if(btnModoDesafio != null) btnModoDesafio.setOnClickListener(v -> startActivity(new Intent(this, QuizActivity.class)));

        View btnVf = findViewById(R.id.btnJugarVerdaderoFalso);
        if(btnVf != null) btnVf.setOnClickListener(v -> startActivity(new Intent(this, TrueFalseActivity.class)));

        View btnAhorcado = findViewById(R.id.btnJugarAhorcado);
        if(btnAhorcado != null) btnAhorcado.setOnClickListener(v -> startActivity(new Intent(this, HangmanActivity.class)));

        View btnMemorama = findViewById(R.id.btnJugarMemorama);
        if(btnMemorama != null) btnMemorama.setOnClickListener(v -> startActivity(new Intent(this, MemoryGameActivity.class)));

        View btnSopa = findViewById(R.id.btnJugarSopaLetras);
        if(btnSopa != null) btnSopa.setOnClickListener(v -> startActivity(new Intent(this, WordSearchActivity.class)));

        // Caminos de categoría
        View btnDerechos = findViewById(R.id.btnJugarDerechos);
        if(btnDerechos != null) btnDerechos.setOnClickListener(v -> abrirApartado("Derechos"));

        View btnObligaciones = findViewById(R.id.btnJugarObligaciones);
        if(btnObligaciones != null) btnObligaciones.setOnClickListener(v -> abrirApartado("Obligaciones"));

        View btnProhibiciones = findViewById(R.id.btnJugarProhibiciones);
        if(btnProhibiciones != null) btnProhibiciones.setOnClickListener(v -> abrirApartado("Prohibiciones"));

        View btnSanciones = findViewById(R.id.btnJugarSanciones);
        if(btnSanciones != null) btnSanciones.setOnClickListener(v -> abrirApartado("Sanciones"));

        View btnReconocimientos = findViewById(R.id.btnJugarReconocimientos);
        if(btnReconocimientos != null) btnReconocimientos.setOnClickListener(v -> abrirApartado("Reconocimientos"));
    }

    private void cargarDatosUsuario() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            db.collection("usuarios").document(currentUser.getUid()).get()
                    .addOnSuccessListener(document -> {
                        if (document.exists()) {
                            String nombre = document.getString("nombre");
                            Long puntaje = document.getLong("puntaje");
                            String fotoUrl = document.getString("fotoUrl");

                            if (nombre != null && tvUserName != null) {
                                tvUserName.setText(nombre.split(" ")[0]);
                            }

                            if (puntaje != null) {
                                if (tvUserPuntaje != null) tvUserPuntaje.setText(puntaje + " XP");
                                if (tvUserLevel != null) tvUserLevel.setText("Nivel " + ((puntaje / 50) + 1));
                                actualizarProgresoVisual(puntaje);
                            }

                            if (fotoUrl != null && !fotoUrl.isEmpty() && ivUserProfile != null) {
                                Glide.with(this).load(fotoUrl).centerCrop().into(ivUserProfile);
                            }
                        }
                    });
        }
    }

    /**
     * Actualiza las 5 barras de progreso y los íconos dinámicamente según la experiencia (XP)
     */
    private void actualizarProgresoVisual(long xpActual) {
        // Nivel 1: Derechos (0 - 50 XP)
        evaluarEstadoCategoria(progressDerechos, ivStatusDerechos, tvStatusDerechos, xpActual, 0, 50, "Nivel 1");

        // Nivel 2: Obligaciones (50 - 100 XP)
        evaluarEstadoCategoria(progressObligaciones, ivStatusObligaciones, tvStatusObligaciones, xpActual, 50, 100, "Nivel 2");

        // Nivel 3: Prohibiciones (100 - 150 XP)
        evaluarEstadoCategoria(progressProhibiciones, ivStatusProhibiciones, tvStatusProhibiciones, xpActual, 100, 150, "Nivel 3");

        // Nivel 4: Sanciones (150 - 200 XP)
        evaluarEstadoCategoria(progressSanciones, ivStatusSanciones, tvStatusSanciones, xpActual, 150, 200, "Nivel 4");

        // Nivel 5: Reconocimientos (200 - 250 XP)
        evaluarEstadoCategoria(progressReconocimientos, ivStatusReconocimientos, tvStatusReconocimientos, xpActual, 200, 250, "Nivel 5");
    }

    /**
     * Calcula dinámicamente si el módulo está bloqueado, en progreso o completado.
     * Incluye validaciones por si algún elemento XML aún no ha sido creado.
     */
    private void evaluarEstadoCategoria(ProgressBar bar, ImageView ivLockIcon, TextView tvStatus, long xpGlobal, int minXP, int maxXP, String nivelLabel) {
        int progresoModulo = (int) Math.min(50, Math.max(0, xpGlobal - minXP));

        // Animamos la barra de progreso
        if (bar != null) {
            ObjectAnimator anim = ObjectAnimator.ofInt(bar, "progress", bar.getProgress(), (progresoModulo * 100) / 50);
            anim.setDuration(1000);
            anim.setInterpolator(new DecelerateInterpolator());
            anim.start();
        }

        // Cambiamos textos y colores solo si las vistas existen
        if (ivLockIcon != null && tvStatus != null) {
            if (xpGlobal >= maxXP) {
                // Nivel Completado
                ivLockIcon.setImageResource(R.drawable.ic_check_circle);
                ivLockIcon.setColorFilter(getResources().getColor(R.color.game_success)); // <-- ¡Aquí está la corrección!
                tvStatus.setText(nivelLabel + " • Completado");
                ivLockIcon.setVisibility(View.VISIBLE);
            } else if (xpGlobal >= minXP) {
                // Nivel Actual (Desbloqueado)
                tvStatus.setText(nivelLabel + " • Desbloqueado");
                ivLockIcon.setVisibility(View.GONE); // Ocultamos el ícono porque está jugándolo
            } else {
                // Nivel Bloqueado
                ivLockIcon.setImageResource(R.drawable.ic_lock);
                ivLockIcon.setColorFilter(getResources().getColor(android.R.color.darker_gray));
                tvStatus.setText(nivelLabel + " • Bloqueado");
                ivLockIcon.setVisibility(View.VISIBLE);
            }
        }
    }

    private void reiniciarBarras() {
        if (progressDerechos != null) progressDerechos.setProgress(0);
        if (progressObligaciones != null) progressObligaciones.setProgress(0);
        if (progressProhibiciones != null) progressProhibiciones.setProgress(0);
        if (progressSanciones != null) progressSanciones.setProgress(0);
        if (progressReconocimientos != null) progressReconocimientos.setProgress(0);
    }

    private void configurarNavegacion() {
        BottomNavigationView bottomNav = findViewById(R.id.bottomNav);
        if (bottomNav != null) {
            bottomNav.setSelectedItemId(R.id.nav_home);
            bottomNav.setOnItemSelectedListener(item -> {
                int id = item.getItemId();
                if (id == R.id.nav_home) return true;
                if (id == R.id.nav_games) startActivity(new Intent(this, ManualJuegosActivity.class));
                else if (id == R.id.nav_ranking) startActivity(new Intent(this, RankingActivity.class));
                else if (id == R.id.nav_profile) startActivity(new Intent(this, ProfileActivity.class));
                return false;
            });
        }
    }

    private void initCloudinary() {
        try {
            MediaManager.get();
        } catch (IllegalStateException e) {
            Map<String, Object> config = new HashMap<>();
            config.put("cloud_name", "dfgj9sdma");
            config.put("secure", true);
            MediaManager.init(this, config);
        }
    }

    private void abrirApartado(String categoria) {
        Intent intent = new Intent(MainActivity.this, GameLevelActivity.class);
        intent.putExtra(KEY_NIVEL_JUEGO, categoria);
        startActivity(intent);
    }
}
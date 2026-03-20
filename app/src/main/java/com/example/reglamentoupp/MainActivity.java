package com.example.reglamentoupp;

import android.animation.ObjectAnimator;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.animation.DecelerateInterpolator;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

public class MainActivity extends AppCompatActivity {

    public static final String KEY_NIVEL_JUEGO = "nivel_juego";
    public static final String KEY_PUNTAJE_ACTUAL = "puntaje_actual";
    public static final String KEY_NIVEL_DESBLOQUEADO = "nivel_desbloqueado";

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    private TextView tvUserName, tvUserPuntaje, tvUserLevel;
    private View btnLogout;

    // Declaramos las barras de progreso
    private ProgressBar progressDerechos, progressObligaciones, progressProhibiciones, progressSanciones, progressReconocimientos;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        tvUserName = findViewById(R.id.tvUserName);
        tvUserPuntaje = findViewById(R.id.tvUserPuntaje);
        tvUserLevel = findViewById(R.id.tvUserLevel);
        btnLogout = findViewById(R.id.btnLogout);

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

                            if (nombre != null && tvUserName != null) {
                                String[] partesNombre = nombre.split(" ");
                                tvUserName.setText(partesNombre[0]);
                            }

                            if (puntaje != null) {
                                if (tvUserPuntaje != null) {
                                    tvUserPuntaje.setText("⚡ " + puntaje + " XP");
                                }
                                // Corregido: Calculamos el nivel en base a 50 puntos como en GameLevelActivity
                                long nivel = (puntaje / 50) + 1;
                                if (tvUserLevel != null) {
                                    tvUserLevel.setText("🏆 Nivel " + nivel);
                                }

                                // Actualizamos las barras de progreso con animación
                                actualizarProgresoBarras(puntaje);
                            }
                        }
                    });
        }
    }

    private void actualizarProgresoBarras(long puntaje) {
        int maxXP = 50; // Cada nivel requiere 50 puntos

        // Nivel 1: Derechos (0 - 50 XP)
        int p1 = (int) Math.min(maxXP, Math.max(0, puntaje));
        animarBarra(progressDerechos, (p1 * 100) / maxXP);

        // Nivel 2: Obligaciones (50 - 100 XP)
        int p2 = (int) Math.min(maxXP, Math.max(0, puntaje - 50));
        animarBarra(progressObligaciones, (p2 * 100) / maxXP);

        // Nivel 3: Prohibiciones (100 - 150 XP)
        int p3 = (int) Math.min(maxXP, Math.max(0, puntaje - 100));
        animarBarra(progressProhibiciones, (p3 * 100) / maxXP);

        // Nivel 4: Sanciones (150 - 200 XP)
        int p4 = (int) Math.min(maxXP, Math.max(0, puntaje - 150));
        animarBarra(progressSanciones, (p4 * 100) / maxXP);

        // Nivel 5: Reconocimientos (200 - 250 XP)
        int p5 = (int) Math.min(maxXP, Math.max(0, puntaje - 200));
        animarBarra(progressReconocimientos, (p5 * 100) / maxXP);
    }

    // Método para hacer que la barra se llene suavemente
    private void animarBarra(ProgressBar bar, int progresoDestino) {
        if (bar != null) {
            ObjectAnimator animation = ObjectAnimator.ofInt(bar, "progress", bar.getProgress(), progresoDestino);
            animation.setDuration(1200); // 1.2 segundos de animación
            animation.setInterpolator(new DecelerateInterpolator()); // Inicia rápido y termina suave
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
                    Toast.makeText(this, "Tus datos de perfil están en la parte superior ☝️", Toast.LENGTH_SHORT).show();
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
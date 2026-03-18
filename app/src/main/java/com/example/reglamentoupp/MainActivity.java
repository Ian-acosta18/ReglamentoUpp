package com.example.reglamentoupp;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

public class MainActivity extends AppCompatActivity {

    // Constantes para GameLevelActivity
    public static final String KEY_NIVEL_JUEGO = "nivel_juego";
    public static final String KEY_PUNTAJE_ACTUAL = "puntaje_actual";
    public static final String KEY_NIVEL_DESBLOQUEADO = "nivel_desbloqueado";

    // Variables de Firebase
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    // Variables de UI
    private TextView tvUserName, tvUserPuntaje, tvUserLevel;
    private View btnLogout;

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
                                long nivel = (puntaje / 100) + 1;
                                if (tvUserLevel != null) {
                                    tvUserLevel.setText("🏆 Nivel " + nivel);
                                }
                            }
                        }
                    });
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
                    Toast.makeText(this, "Sección de Juegos", Toast.LENGTH_SHORT).show();
                    return true;
                } else if (id == R.id.nav_ranking) {
                    // Ranking ahora se abre desde el menú inferior
                    startActivity(new Intent(MainActivity.this, RankingActivity.class));
                    return true;
                } else if (id == R.id.nav_profile) {
                    Toast.makeText(this, "Perfil de Usuario", Toast.LENGTH_SHORT).show();
                    return true;
                }
                return false;
            });
        }
    }

    private void configurarJuegosRapidos() {
        // Se quitó el btnVerRanking de aquí porque ahora está en el BottomNavigation

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
        // --- TODOS LOS NIVELES AHORA ESTÁN DESBLOQUEADOS Y ABREN LOS APARTADOS ---
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

    // Método auxiliar para no repetir tanto código
    private void abrirApartado(String categoria) {
        Intent intent = new Intent(MainActivity.this, GameLevelActivity.class);
        intent.putExtra("categoria", categoria);
        startActivity(intent);
    }
}
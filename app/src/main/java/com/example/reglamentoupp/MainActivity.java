package com.example.reglamentoupp;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
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
                } else if (id == R.id.nav_profile) {
                    Toast.makeText(this, "Perfil de Usuario", Toast.LENGTH_SHORT).show();
                    return true;
                }
                return false;
            });
        }
    }

    private void configurarJuegosRapidos() {
        View btnVerRanking = findViewById(R.id.btnVerRanking);
        if (btnVerRanking != null) {
            btnVerRanking.setOnClickListener(v -> startActivity(new Intent(MainActivity.this, RankingActivity.class)));
        }

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
    }

    private void configurarCaminoCategorias() {
        View btnDerechos = findViewById(R.id.btnJugarDerechos);
        View btnObligaciones = findViewById(R.id.btnJugarObligaciones);

        if (btnDerechos != null) {
            btnDerechos.setOnClickListener(v -> {
                Intent intent = new Intent(MainActivity.this, GameLevelActivity.class);
                intent.putExtra("categoria", "Derechos");
                startActivity(intent);
            });
        }

        if (btnObligaciones != null) {
            btnObligaciones.setOnClickListener(v -> {
                Intent intent = new Intent(MainActivity.this, GameLevelActivity.class);
                intent.putExtra("categoria", "Obligaciones");
                startActivity(intent);
            });
        }
    }
}
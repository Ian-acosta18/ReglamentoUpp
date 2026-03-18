package com.example.reglamentoupp;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.card.MaterialCardView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class MainActivity extends AppCompatActivity {

    // Variables de Firebase
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    // Variables de UI (Perfil)
    private TextView tvUserName, tvUserPuntaje, tvUserLevel, tvUserStreak;
    private ImageButton btnLogout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Inicializar Firebase
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // Vincular vistas de Perfil
        tvUserName = findViewById(R.id.tvUserName);
        tvUserPuntaje = findViewById(R.id.tvUserPuntaje);
        tvUserLevel = findViewById(R.id.tvUserLevel);
        tvUserStreak = findViewById(R.id.tvUserStreak); // Si no lo agregaste en el XML, puedes comentarlo
        btnLogout = findViewById(R.id.btnLogout);

        // Cargar datos del usuario
        cargarDatosUsuario();

        // Configurar Barra de Navegación Inferior
        configurarBottomNavigation();

        // Configurar Botones de Juegos Rápidos
        configurarJuegosRapidos();

        // Configurar Botones de "Tu Camino" (Categorías)
        configurarCaminoCategorias();

        // Botón de Cerrar Sesión
        btnLogout.setOnClickListener(v -> {
            mAuth.signOut();
            Intent intent = new Intent(MainActivity.this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        });
    }

    private void cargarDatosUsuario() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            String userId = currentUser.getUid();

            db.collection("usuarios").document(userId).get()
                    .addOnSuccessListener(documentSnapshot -> {
                        if (documentSnapshot.exists()) {
                            // Obtener datos (ajusta las claves según cómo las tengas en Firestore)
                            String nombre = documentSnapshot.getString("nombre");
                            Long puntaje = documentSnapshot.getLong("puntaje");

                            if (nombre != null) {
                                // Mostrar solo el primer nombre para el diseño compacto
                                String[] partesNombre = nombre.split(" ");
                                tvUserName.setText(partesNombre[0]);
                            }

                            if (puntaje != null) {
                                tvUserPuntaje.setText(puntaje + " XP");
                                // Calcular nivel básico (Ejemplo: cada 100 XP es un nivel)
                                long nivel = (puntaje / 100) + 1;
                                tvUserLevel.setText("Nivel " + nivel);
                            }
                        }
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(this, "Error al cargar perfil", Toast.LENGTH_SHORT).show();
                    });
        }
    }

    private void configurarBottomNavigation() {
        BottomNavigationView bottomNav = findViewById(R.id.bottomNav);
        // Indicar que estamos en la pestaña de Inicio
        bottomNav.setSelectedItemId(R.id.nav_home);

        bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_home) {
                return true; // Ya estamos aquí
            } else if (id == R.id.nav_games) {
                // Ir a otra actividad si tienes un HUB de juegos, o hacer scroll
                Toast.makeText(this, "Sección de Juegos", Toast.LENGTH_SHORT).show();
                return true;
            } else if (id == R.id.nav_profile) {
                // Ir al perfil del usuario
                Toast.makeText(this, "Perfil de Usuario", Toast.LENGTH_SHORT).show();
                return true;
            }
            return false;
        });
    }

    private void configurarJuegosRapidos() {
        // Botón Ranking
        View btnVerRanking = findViewById(R.id.btnVerRanking);
        if (btnVerRanking != null) {
            btnVerRanking.setOnClickListener(v -> startActivity(new Intent(MainActivity.this, RankingActivity.class)));
        }

        // Trivia
        MaterialCardView btnJugarModoDesafio = findViewById(R.id.btnJugarModoDesafio);
        btnJugarModoDesafio.setOnClickListener(v -> startActivity(new Intent(MainActivity.this, QuizActivity.class)));

        // Verdadero / Falso
        MaterialCardView btnJugarVerdaderoFalso = findViewById(R.id.btnJugarVerdaderoFalso);
        if (btnJugarVerdaderoFalso != null) {
            btnJugarVerdaderoFalso.setOnClickListener(v -> startActivity(new Intent(MainActivity.this, TrueFalseActivity.class)));
        }

        // Ahorcado
        MaterialCardView btnJugarAhorcado = findViewById(R.id.btnJugarAhorcado);
        btnJugarAhorcado.setOnClickListener(v -> startActivity(new Intent(MainActivity.this, HangmanActivity.class)));

        // Memorama
        MaterialCardView btnJugarMemorama = findViewById(R.id.btnJugarMemorama);
        btnJugarMemorama.setOnClickListener(v -> startActivity(new Intent(MainActivity.this, MemoryGameActivity.class)));

        // Sopa de Letras
        MaterialCardView btnJugarSopaLetras = findViewById(R.id.btnJugarSopaLetras);
        if (btnJugarSopaLetras != null) {
            btnJugarSopaLetras.setOnClickListener(v -> startActivity(new Intent(MainActivity.this, WordSearchActivity.class)));
        }
    }

    private void configurarCaminoCategorias() {
        MaterialCardView btnDerechos = findViewById(R.id.btnJugarDerechos);
        MaterialCardView btnObligaciones = findViewById(R.id.btnJugarObligaciones);
        MaterialCardView btnProhibiciones = findViewById(R.id.btnJugarProhibiciones);
        MaterialCardView btnSanciones = findViewById(R.id.btnJugarSanciones);
        MaterialCardView btnReconocimientos = findViewById(R.id.btnJugarReconocimientos);

        // Nivel 1: Derechos
        btnDerechos.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, GameLevelActivity.class);
            intent.putExtra("categoria", "Derechos");
            startActivity(intent);
        });

        // Nivel 2: Obligaciones
        btnObligaciones.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, GameLevelActivity.class);
            intent.putExtra("categoria", "Obligaciones");
            startActivity(intent);
        });

        // Nivel 3: Prohibiciones (Si está bloqueado puedes poner un Toast por ahora)
        if (btnProhibiciones != null) {
            btnProhibiciones.setOnClickListener(v -> {
                Toast.makeText(this, "Bloqueado. ¡Completa el Nivel 2 primero!", Toast.LENGTH_SHORT).show();
            });
        }

        // Nivel 4: Sanciones
        if (btnSanciones != null) {
            btnSanciones.setOnClickListener(v -> {
                Toast.makeText(this, "Nivel 4 Bloqueado", Toast.LENGTH_SHORT).show();
            });
        }

        // Nivel 5: Reconocimientos
        if (btnReconocimientos != null) {
            btnReconocimientos.setOnClickListener(v -> {
                Toast.makeText(this, "Nivel 5 Bloqueado", Toast.LENGTH_SHORT).show();
            });
        }
    }
}
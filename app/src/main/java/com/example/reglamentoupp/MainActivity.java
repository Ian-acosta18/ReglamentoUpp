package com.example.reglamentoupp;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
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

// Esta clase YA implementa la interfaz correcta, no es necesario cambiarla
public class MainActivity extends AppCompatActivity implements BaseReglamentoFragment.ReglamentoInteractionListener {

    private ActivityMainBinding binding;
    private FirebaseAuth mAuth;
    private FirebaseFirestore mStore;
    private String userID;
    private long userPuntaje = 0;
    private int userNivel = 1; // Nivel de desbloqueo del usuario

    public static final String KEY_NIVEL_JUEGO = "nivelJuego";
    public static final String KEY_PUNTAJE_ACTUAL = "puntajeActual";
    public static final String KEY_NIVEL_DESBLOQUEADO = "nivelDesbloqueado"; // <-- AÑADIR ESTA LÍNEA
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

        // La carga de datos inicial se moverá a onResume para que
        // se actualice cada vez que volvamos de un nivel
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Recarga los datos del usuario cada vez que la pantalla se vuelve visible
        // (el puntaje y el nivel pueden cambiar después de jugar)
        loadUserData();
    }

    private void loadUserData() {
        if (userID == null) return;

        mStore.collection("usuarios").document(userID).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        // Cargar nuevos datos del perfil
                        String nombre = documentSnapshot.getString("nombre");
                        String apellidos = documentSnapshot.getString("apellidos");
                        if (nombre != null && !nombre.isEmpty()) {
                            binding.tvUserName.setText(nombre + " " + apellidos); // Actualizamos el nombre
                        } else {
                            binding.tvUserName.setText(documentSnapshot.getString("email")); // Fallback al email
                        }

                        // Cargar puntaje
                        Long puntajeDb = documentSnapshot.getLong("puntaje");
                        if (puntajeDb != null) {
                            userPuntaje = puntajeDb;
                        }
                        binding.tvUserPuntaje.setText(userPuntaje + " Puntos");

                        // --- LÓGICA DE NIVELES ---
                        Long nivelDb = documentSnapshot.getLong("nivelDesbloqueado");
                        if (nivelDb != null) {
                            userNivel = nivelDb.intValue();
                        }

                        Log.d(TAG, "Usuario cargado. Puntaje: " + userPuntaje + ", Nivel Desbloqueado: " + userNivel);

                        // Configurar los botones de nivel con la lógica de bloqueo
                        // Nivel 1 (Derechos) - 0 Puntos requeridos
                        setupNivelButton(binding.btnJugarDerechos, null, binding.tvDerechos, binding.ivDerechos,
                                "Derechos", 1, R.color.upp_primary, R.color.upp_text_title, 0);

                        // Nivel 2 (Obligaciones) - 50 Puntos requeridos
                        setupNivelButton(binding.btnJugarObligaciones, binding.ivLockObligaciones, binding.tvObligaciones, binding.ivObligaciones,
                                "Obligaciones", 2, R.color.upp_primary, R.color.upp_text_title, 50);

                        // Nivel 3 (Prohibiciones) - 100 Puntos requeridos
                        setupNivelButton(binding.btnJugarProhibiciones, binding.ivLockProhibiciones, binding.tvProhibiciones, binding.ivProhibiciones,
                                "Prohibiciones", 3, R.color.upp_primary, R.color.upp_text_title, 100);

                        // Nivel 4 (Sanciones) - 150 Puntos requeridos
                        setupNivelButton(binding.btnJugarSanciones, binding.ivLockSanciones, binding.tvSanciones, binding.ivSanciones,
                                "Sanciones", 4, R.color.upp_primary, R.color.upp_text_title, 150);

                        // Nivel 5 (Reconocimientos) - 200 Puntos requeridos
                        setupNivelButton(binding.btnJugarReconocimientos, binding.ivLockReconocimientos, binding.tvReconocimientos, binding.ivReconocimientos,
                                "Reconocimientos", 5, R.color.upp_primary, R.color.upp_text_title, 200);

                    } else {
                        Log.w(TAG, "No existe el documento del usuario en Firestore.");
                        // (Opcional) Forzar un deslogueo o re-crear el documento
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

    /**
     * Configura un botón de nivel, mostrando/ocultando el candado
     * y activando el click listener si el nivel está desbloqueado.
     */
    private void setupNivelButton(MaterialCardView button, ImageView lockIcon, TextView textView, ImageView iconView,
                                  String nivel, int nivelRequerido, int colorDesbloqueado, int textColorDesbloqueado, int puntosRequeridos) {

        int colorBloqueado = ContextCompat.getColor(this, R.color.game_locked);
        int colorBgBloqueado = ContextCompat.getColor(this, R.color.game_locked_bg);
        int colorBgDesbloqueado = ContextCompat.getColor(this, R.color.upp_card_bg);
        int colorIconoDesbloqueado = ContextCompat.getColor(this, colorDesbloqueado);
        int colorTextoDesbloqueado = ContextCompat.getColor(this, textColorDesbloqueado);

        // AHORA COMPROBAMOS EL NIVEL Y LOS PUNTOS
        if (userNivel >= nivelRequerido && userPuntaje >= puntosRequeridos) {
            // --- Nivel Desbloqueado ---
            button.setEnabled(true);
            button.setClickable(true);
            button.setCardBackgroundColor(colorBgDesbloqueado); // Color de fondo normal
            button.setCardElevation(getResources().getDimension(com.google.android.material.R.dimen.m3_card_elevation)); // Restaurar elevación

            // Ocultar el candado (si existe)
            if (lockIcon != null) {
                lockIcon.setVisibility(View.GONE);
            }

            // Restaurar colores de texto e ícono
            textView.setTextColor(colorTextoDesbloqueado);
            textView.setText(String.format("Nivel %d: %s", nivelRequerido, nivel)); // Texto normal
            iconView.setImageTintList(ColorStateList.valueOf(colorIconoDesbloqueado));

            // Configurar el OnClickListener
            button.setOnClickListener(v -> {
                Log.d(TAG, "Iniciando nivel: " + nivel);
                Intent intent = new Intent(MainActivity.this, GameLevelActivity.class);
                intent.putExtra(KEY_NIVEL_JUEGO, nivel);
                intent.putExtra(KEY_PUNTAJE_ACTUAL, userPuntaje);
                intent.putExtra(KEY_NIVEL_DESBLOQUEADO, userNivel); // <-- AÑADIR ESTA LÍNEA
                startActivity(intent);
            });

        } else {
            // --- Nivel Bloqueado ---
            button.setEnabled(false);
            button.setClickable(false);
            button.setCardBackgroundColor(colorBgBloqueado); // Color de fondo bloqueado
            button.setCardElevation(0); // Quitar sombra

            // Mostrar el candado (si existe)
            if (lockIcon != null) {
                lockIcon.setVisibility(View.VISIBLE);
            }

            // Poner colores de "bloqueado"
            textView.setTextColor(colorBloqueado);
            // Mostrar los puntos necesarios
            textView.setText(String.format("Nivel %d: %s (Req. %d Pts)", nivelRequerido, nivel, puntosRequeridos));
            iconView.setImageTintList(ColorStateList.valueOf(colorBloqueado));

            // El OnClickListener no se configura, por lo que el botón no hace nada
        }
    }


    private void navigateToLogin() {
        Intent intent;
        intent = new Intent(LoginActivity.this, LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    // --- Estos métodos son requeridos por la interfaz ---
    // Esta actividad (MainActivity) no los usa, pero debe tenerlos.
    // La que los usará es GameLevelActivity.

    @Override
    public void onQuizClick(String itemText, String itemType) {
        Log.d(TAG, "Clic en Quiz (ignorado en MainActivity): " + itemType);
    }

    @Override
    public void onCaseStudyClick(String itemText, String itemType) {
        Log.d(TAG, "Clic en Caso de Estudio (ignorado en MainActivity): " + itemType);
    }
}
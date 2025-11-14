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

// Importaciones limpias (sin el script de subida)

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

        // --- El código del script de subida (FAB) se ha eliminado ---

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
    }

    private void loadUserData() {
        if (userID == null) return;

        mStore.collection("usuarios").document(userID).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        String nombre = documentSnapshot.getString("nombre");
                        if (nombre != null && !nombre.isEmpty()) {
                            binding.tvUserName.setText("Bienvenido, " + nombre);
                        } else {
                            binding.tvUserName.setText(documentSnapshot.getString("email"));
                        }

                        Long puntajeDb = documentSnapshot.getLong("puntaje");
                        if (puntajeDb != null) {
                            userPuntaje = puntajeDb;
                        }
                        binding.tvUserPuntaje.setText(userPuntaje + " Puntos");

                        Long nivelDb = documentSnapshot.getLong("nivelDesbloqueado");
                        if (nivelDb != null) {
                            userNivel = nivelDb.intValue();
                        }

                        Log.d(TAG, "Usuario cargado. Puntaje: " + userPuntaje + ", Nivel Desbloqueado: " + userNivel);

                        // ----- INICIO DE CORRECCIÓN (Se cambió R.color.upp_text_title por R.color.text_primary) -----
                        setupNivelButton(binding.btnJugarDerechos, null, binding.tvDerechos, binding.ivDerechos,
                                "Derechos", 1, R.color.upp_primary, R.color.text_primary, 0);
                        setupNivelButton(binding.btnJugarObligaciones, binding.ivLockObligaciones, binding.tvObligaciones, binding.ivObligaciones,
                                "Obligaciones", 2, R.color.upp_primary, R.color.text_primary, 50);
                        setupNivelButton(binding.btnJugarProhibiciones, binding.ivLockProhibiciones, binding.tvProhibiciones, binding.ivProhibiciones,
                                "Prohibiciones", 3, R.color.upp_primary, R.color.text_primary, 100);
                        setupNivelButton(binding.btnJugarSanciones, binding.ivLockSanciones, binding.tvSanciones, binding.ivSanciones,
                                "Sanciones", 4, R.color.upp_primary, R.color.text_primary, 150);
                        setupNivelButton(binding.btnJugarReconocimientos, binding.ivLockReconocimientos, binding.tvReconocimientos, binding.ivReconocimientos,
                                "Reconocimientos", 5, R.color.upp_primary, R.color.text_primary, 200);
                        // ----- FIN DE CORRECCIÓN -----

                    } else {
                        Log.w(TAG, "No existe el documento del usuario en Firestore.");
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

    private void setupNivelButton(MaterialCardView button, ImageView lockIcon, TextView textView, ImageView iconView,
                                  String nivel, int nivelRequerido, int colorDesbloqueado, int textColorDesbloqueado, int puntosRequeridos) {

        int colorBloqueado = ContextCompat.getColor(this, R.color.game_locked);
        int colorBgBloqueado = ContextCompat.getColor(this, R.color.game_locked_bg);

        // ----- INICIO DE CORRECCIÓN (Se cambió R.color.upp_card_bg por R.color.card_bg) -----
        int colorBgDesbloqueado = ContextCompat.getColor(this, R.color.card_bg);
        // ----- FIN DE CORRECCIÓN -----

        int colorIconoDesbloqueado = ContextCompat.getColor(this, colorDesbloqueado);
        int colorTextoDesbloqueado = ContextCompat.getColor(this, textColorDesbloqueado);

        if (userNivel >= nivelRequerido) {
            button.setEnabled(true);
            button.setClickable(true);
            button.setCardBackgroundColor(colorBgDesbloqueado);
            button.setCardElevation(getResources().getDimension(com.google.android.material.R.dimen.m3_card_elevation));

            if (lockIcon != null) {
                lockIcon.setVisibility(View.GONE);
            }

            textView.setTextColor(colorTextoDesbloqueado);
            textView.setText(nivel);
            iconView.setImageTintList(ColorStateList.valueOf(colorIconoDesbloqueado));

            button.setOnClickListener(v -> {
                Log.d(TAG, "Iniciando nivel: " + nivel);
                Intent intent = new Intent(MainActivity.this, GameLevelActivity.class);
                intent.putExtra(KEY_NIVEL_JUEGO, nivel);
                intent.putExtra(KEY_PUNTAJE_ACTUAL, userPuntaje);
                intent.putExtra(KEY_NIVEL_DESBLOQUEADO, userNivel);
                startActivity(intent);
            });

        } else {
            button.setEnabled(false);
            button.setClickable(false);
            button.setCardBackgroundColor(colorBgBloqueado);
            button.setCardElevation(0);

            if (lockIcon != null) {
                lockIcon.setVisibility(View.VISIBLE);
            }

            textView.setTextColor(colorBloqueado);
            textView.setText(nivel);
            iconView.setImageTintList(ColorStateList.valueOf(colorBloqueado));
        }
    }

    private void navigateToLogin() {
        // ----- INICIO DE CORRECCIÓN (Error de Contexto) -----
        Intent intent = new Intent(MainActivity.this, LoginActivity.class);
        // ----- FIN DE CORRECCIÓN -----

        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    @Override
    public void onQuizClick(String itemText, String itemType) {
        Log.d(TAG, "Clic en Quiz (ignorado en MainActivity): " + itemType);
    }

    @Override
    public void onCaseStudyClick(String itemText, String itemType) {
        Log.d(TAG, "Clic en Caso de Estudio (ignorado en MainActivity): " + itemType);
    }

}
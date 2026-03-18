package com.example.reglamentoupp;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import androidx.appcompat.app.AppCompatActivity;
import com.bumptech.glide.Glide;
import com.example.reglamentoupp.databinding.ActivityMainBinding;
import com.google.android.material.card.MaterialCardView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

public class MainActivity extends AppCompatActivity implements BaseReglamentoFragment.ReglamentoInteractionListener {

    private ActivityMainBinding binding;
    private FirebaseAuth mAuth;
    private FirebaseFirestore mStore;
    private int userNivel = 1;

    // CONSTANTES CORREGIDAS
    public static final String KEY_NIVEL_JUEGO = "nivelJuego";
    public static final String KEY_PUNTAJE_ACTUAL = "puntajeActual";
    public static final String KEY_NIVEL_DESBLOQUEADO = "nivelDesbloqueado";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        mAuth = FirebaseAuth.getInstance();
        mStore = FirebaseFirestore.getInstance();

        if (mAuth.getCurrentUser() == null) {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }
        setupGameListeners();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadUserData();
    }

    private void loadUserData() {
        String uid = mAuth.getCurrentUser().getUid();
        mStore.collection("usuarios").document(uid).get().addOnSuccessListener(doc -> {
            if (doc.exists() && binding != null) {
                binding.tvUserName.setText(doc.getString("nombre"));
                binding.tvUserPuntaje.setText(doc.getLong("puntaje") + " XP");
                userNivel = doc.getLong("nivelDesbloqueado").intValue();
                binding.tvUserLevel.setText("Nivel " + userNivel);

                String fotoUrl = doc.getString("fotoUrl");
                if (fotoUrl != null && !fotoUrl.isEmpty()) {
                    Glide.with(this).load(fotoUrl).circleCrop().into(binding.ivUserProfile);
                }
                actualizarInterfazNiveles();
            }
        });
    }

    private void actualizarInterfazNiveles() {
        configurarNivel(binding.btnJugarDerechos, binding.progressDerechos, 1);
        configurarNivel(binding.btnJugarObligaciones, binding.ivLockObligaciones, 2);
        configurarNivel(binding.btnJugarProhibiciones, binding.ivLockProhibiciones, 3);
        configurarNivel(binding.btnJugarSanciones, binding.ivLockSanciones, 4);
        configurarNivel(binding.btnJugarReconocimientos, binding.ivLockReconocimientos, 5);
    }

    private void configurarNivel(MaterialCardView card, View lockOrProgress, int nivelReq) {
        boolean unlocked = userNivel >= nivelReq;
        card.setAlpha(unlocked ? 1.0f : 0.5f);
        card.setClickable(unlocked);

        if (lockOrProgress != null) {
            lockOrProgress.setVisibility(unlocked ? View.GONE : View.VISIBLE);
        }

        if (unlocked) {
            card.setOnClickListener(v -> {
                Intent i = new Intent(this, GameLevelActivity.class);
                i.putExtra(KEY_NIVEL_JUEGO, "Nivel " + nivelReq);
                startActivity(i);
            });
        } else {
            card.setOnClickListener(null);
        }
    }

    private void setupGameListeners() {
        binding.btnJugarModoDesafio.setOnClickListener(v -> startActivity(new Intent(this, QuizActivity.class)));
        binding.btnJugarAhorcado.setOnClickListener(v -> startActivity(new Intent(this, HangmanActivity.class)));
        binding.btnJugarMemorama.setOnClickListener(v -> startActivity(new Intent(this, MemoryGameActivity.class)));
        binding.btnVerRanking.setOnClickListener(v -> startActivity(new Intent(this, RankingActivity.class)));
    }

    @Override public void onQuizClick(String t, String type) {}
    @Override public void onCaseStudyClick(String t, String type) {}
}
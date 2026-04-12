package com.example.reglamentoupp;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class ProfileActivity extends AppCompatActivity {

    private ImageView ivProfilePic;
    private TextView tvProfileName, tvProfileEmail, tvProfileLevel, tvProfileScore, tvProgressText;
    private ProgressBar pbProfileLevel;
    private MaterialButton btnLogout;

    // Insignias
    private ImageView ivBadgeDerechos, ivBadgeObligaciones, ivBadgeProhibiciones, ivBadgeSanciones, ivBadgeReconocimientos;

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        vincularVistas();
        cargarDatosUsuario();

        btnLogout.setOnClickListener(v -> cerrarSesion());
        findViewById(R.id.btnBackProfile).setOnClickListener(v -> finish());
    }

    private void vincularVistas() {
        ivProfilePic = findViewById(R.id.ivProfilePic);
        tvProfileName = findViewById(R.id.tvProfileName);
        tvProfileEmail = findViewById(R.id.tvProfileEmail);
        tvProfileLevel = findViewById(R.id.tvProfileLevel);
        tvProfileScore = findViewById(R.id.tvProfileScore);
        tvProgressText = findViewById(R.id.tvProgressText);
        pbProfileLevel = findViewById(R.id.pbProfileLevel);
        btnLogout = findViewById(R.id.btnLogoutProfile);

        // Insignias
        ivBadgeDerechos = findViewById(R.id.ivBadgeDerechos);
        ivBadgeObligaciones = findViewById(R.id.ivBadgeObligaciones);
        ivBadgeProhibiciones = findViewById(R.id.ivBadgeProhibiciones);
        ivBadgeSanciones = findViewById(R.id.ivBadgeSanciones);
        ivBadgeReconocimientos = findViewById(R.id.ivBadgeReconocimientos);
    }

    private void cargarDatosUsuario() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            cerrarSesion();
            return;
        }

        tvProfileEmail.setText(currentUser.getEmail());

        db.collection("usuarios").document(currentUser.getUid()).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        String nombre = documentSnapshot.getString("nombre");
                        Long puntaje = documentSnapshot.getLong("puntaje");
                        String fotoUrl = documentSnapshot.getString("fotoUrl");

                        if (nombre != null) tvProfileName.setText(nombre);

                        long xpTotal = (puntaje != null) ? puntaje : 0;
                        actualizarNivel(xpTotal);
                        actualizarInsignias(xpTotal);

                        if (fotoUrl != null && !fotoUrl.isEmpty() && !isDestroyed()) {
                            Glide.with(this)
                                    .load(fotoUrl)
                                    .centerCrop()
                                    .placeholder(R.drawable.ic_profile)
                                    .into(ivProfilePic);
                        }
                    }
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Error al cargar datos", Toast.LENGTH_SHORT).show());
    }

    private void actualizarNivel(long xpActual) {
        tvProfileScore.setText(xpActual + " XP Totales");

        long xpParaSiguienteNivel = 50;
        int nivelActual = (int) (xpActual / xpParaSiguienteNivel) + 1;
        long xpEnNivelActual = xpActual % xpParaSiguienteNivel;

        tvProfileLevel.setText("Nivel " + nivelActual);
        tvProgressText.setText(xpEnNivelActual + " / " + xpParaSiguienteNivel + " XP");

        pbProfileLevel.setMax((int) xpParaSiguienteNivel);
        pbProfileLevel.setProgress((int) xpEnNivelActual);
    }

    private void actualizarInsignias(long puntaje) {
        if (ivBadgeDerechos == null) return;

        // Limpiamos cualquier filtro de color previo que pueda estar afectando los colores
        ivBadgeDerechos.clearColorFilter();
        ivBadgeObligaciones.clearColorFilter();
        ivBadgeProhibiciones.clearColorFilter();
        ivBadgeSanciones.clearColorFilter();
        ivBadgeReconocimientos.clearColorFilter();

        // Derechos (0+ puntos)
        ivBadgeDerechos.setAlpha(puntaje >= 0 ? 1.0f : 0.25f);

        // Obligaciones (50+ puntos)
        ivBadgeObligaciones.setAlpha(puntaje >= 50 ? 1.0f : 0.25f);

        // Prohibiciones (100+ puntos)
        ivBadgeProhibiciones.setAlpha(puntaje >= 100 ? 1.0f : 0.25f);

        // Sanciones (150+ puntos)
        ivBadgeSanciones.setAlpha(puntaje >= 150 ? 1.0f : 0.25f);

        // Reconocimientos (200+ puntos)
        ivBadgeReconocimientos.setAlpha(puntaje >= 200 ? 1.0f : 0.25f);
    }

    private void cerrarSesion() {
        mAuth.signOut();
        Intent intent = new Intent(ProfileActivity.this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}
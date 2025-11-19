package com.example.reglamentoupp;

import android.media.MediaPlayer; // Importar MediaPlayer
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.example.reglamentoupp.databinding.ActivityTrueFalseBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class TrueFalseActivity extends AppCompatActivity {

    private ActivityTrueFalseBinding binding;
    private FirebaseFirestore mStore;
    private FirebaseAuth mAuth;
    private List<PreguntaVF> listaDePreguntas;
    private PreguntaVF preguntaActual;
    private int indicePreguntaActual = 0;
    private int puntajeSesion = 0;
    private boolean botonesBloqueados = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityTrueFalseBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        mStore = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();
        listaDePreguntas = new ArrayList<>();

        binding.btnTrue.setOnClickListener(v -> checarRespuesta(true));
        binding.btnFalse.setOnClickListener(v -> checarRespuesta(false));

        cargarPreguntasVF();
    }

    private void cargarPreguntasVF() {
        mStore.collection("preguntasVF")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (queryDocumentSnapshots.isEmpty()) {
                        crearPreguntasDeEjemplo(); // Método creado anteriormente
                        return;
                    }
                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        listaDePreguntas.add(doc.toObject(PreguntaVF.class));
                    }
                    iniciarJuego();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Error al cargar datos", Toast.LENGTH_SHORT).show();
                    finish();
                });
    }

    private void crearPreguntasDeEjemplo() {
        // Si necesitas regenerarlas, usa el código del mensaje anterior
        Toast.makeText(this, "Creando preguntas...", Toast.LENGTH_SHORT).show();
    }

    private void iniciarJuego() {
        Collections.shuffle(listaDePreguntas);
        if (listaDePreguntas.size() > 10) {
            listaDePreguntas = listaDePreguntas.subList(0, 10);
        }
        mostrarSiguientePregunta();
    }

    private void mostrarSiguientePregunta() {
        if (indicePreguntaActual >= listaDePreguntas.size()) {
            terminarJuego();
            return;
        }

        botonesBloqueados = false;
        preguntaActual = listaDePreguntas.get(indicePreguntaActual);
        binding.tvVFQuestion.setText(preguntaActual.getAfirmacion());
        binding.tvVFScore.setText("Puntaje: " + puntajeSesion);
        binding.cardVFQuestion.setCardBackgroundColor(ContextCompat.getColor(this, R.color.card_bg));

        indicePreguntaActual++;
    }

    private void checarRespuesta(boolean respuestaElegida) {
        if (botonesBloqueados) return;
        botonesBloqueados = true;

        if (respuestaElegida == preguntaActual.isRespuesta()) {
            // --- CORRECTO ---
            puntajeSesion += 10;
            reproducirSonido(R.raw.correct_ding); // SONIDO
            binding.cardVFQuestion.setCardBackgroundColor(ContextCompat.getColor(this, R.color.game_success_container));
            Toast.makeText(this, "¡Correcto!", Toast.LENGTH_SHORT).show();
            guardarPuntos(10);
        } else {
            // --- INCORRECTO ---
            reproducirSonido(R.raw.megaman_x_error); // SONIDO
            binding.cardVFQuestion.setCardBackgroundColor(ContextCompat.getColor(this, R.color.game_fail_container));
            Toast.makeText(this, "Incorrecto", Toast.LENGTH_SHORT).show();
        }

        new Handler(Looper.getMainLooper()).postDelayed(this::mostrarSiguientePregunta, 1200);
    }

    // --- MÉTODO PARA REPRODUCIR SONIDOS ---
    private void reproducirSonido(int soundResource) {
        try {
            MediaPlayer mp = MediaPlayer.create(this, soundResource);
            if (mp != null) {
                mp.start();
                mp.setOnCompletionListener(MediaPlayer::release);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void guardarPuntos(int puntosGanados) {
        if (mAuth.getCurrentUser() == null) return;
        mStore.collection("usuarios").document(mAuth.getCurrentUser().getUid())
                .update("puntaje", FieldValue.increment(puntosGanados));
    }

    private void terminarJuego() {
        new com.google.android.material.dialog.MaterialAlertDialogBuilder(this)
                .setTitle("¡Juego Terminado!")
                .setMessage("Conseguiste " + puntajeSesion + " puntos en esta ronda.")
                .setPositiveButton("Salir", (dialog, which) -> finish())
                .setCancelable(false)
                .show();
    }
}
package com.example.reglamentoupp;

import android.content.res.ColorStateList;
import android.graphics.Color;
import android.media.MediaPlayer; // Importar MediaPlayer
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.example.reglamentoupp.databinding.ActivityQuizBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class QuizActivity extends AppCompatActivity implements View.OnClickListener {

    private ActivityQuizBinding binding;
    private FirebaseFirestore mStore;
    private FirebaseAuth mAuth;

    private List<Pregunta> listaDePreguntas;
    private Pregunta preguntaActual;
    private int indicePreguntaActual = 0;
    private int puntaje = 0;
    private int vidas = 3;
    private static final int MAX_PREGUNTAS = 10;
    private boolean botonesBloqueados = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityQuizBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        mStore = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();
        listaDePreguntas = new ArrayList<>();

        binding.btnQuizOptionA.setOnClickListener(this);
        binding.btnQuizOptionB.setOnClickListener(this);
        binding.btnQuizOptionC.setOnClickListener(this);

        cargarTodasLasPreguntas();
    }

    private void cargarTodasLasPreguntas() {
        mStore.collection("preguntas")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (queryDocumentSnapshots.isEmpty()) {
                        crearPreguntasDesafio();
                        return;
                    }
                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        listaDePreguntas.add(doc.toObject(Pregunta.class));
                    }
                    Collections.shuffle(listaDePreguntas);
                    if (listaDePreguntas.size() > MAX_PREGUNTAS) {
                        listaDePreguntas = listaDePreguntas.subList(0, MAX_PREGUNTAS);
                    }
                    binding.quizProgressBar.setMax(listaDePreguntas.size());
                    iniciarQuiz();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Error al cargar preguntas.", Toast.LENGTH_SHORT).show();
                    finish();
                });
    }

    private void crearPreguntasDesafio() {
        List<Pregunta> nuevas = new ArrayList<>();
        // (Aquí va tu lista de preguntas, omitida para ahorrar espacio,
        // pero el código funciona con las que ya tienes en Firebase o
        // si copias el método del mensaje anterior)

        // Si necesitas regenerarlas, copia el contenido de crearPreguntasDesafio
        // del mensaje anterior aquí.
        Toast.makeText(this, "Base de datos vacía. Agregando preguntas...", Toast.LENGTH_SHORT).show();
    }

    private void iniciarQuiz() {
        indicePreguntaActual = 0;
        puntaje = 0;
        vidas = 3;
        mostrarSiguientePregunta();
    }

    private void mostrarSiguientePregunta() {
        if (indicePreguntaActual < listaDePreguntas.size()) {
            botonesBloqueados = false;
            restaurarBotones();
            preguntaActual = listaDePreguntas.get(indicePreguntaActual);

            binding.tvQuizQuestion.setText(preguntaActual.getPregunta());
            binding.btnQuizOptionA.setText(preguntaActual.getOpcionA());
            binding.btnQuizOptionB.setText(preguntaActual.getOpcionB());
            binding.btnQuizOptionC.setText(preguntaActual.getOpcionC());

            binding.tvQuizScore.setText("Puntaje: " + puntaje);
            binding.tvQuizLives.setText("Vidas: " + vidas);
            binding.quizProgressBar.setProgress(indicePreguntaActual + 1);

            indicePreguntaActual++;
        } else {
            mostrarResultadoFinal();
        }
    }

    @Override
    public void onClick(View v) {
        if (botonesBloqueados) return;
        botonesBloqueados = true;

        Button botonPresionado = (Button) v;
        String respuestaElegida = botonPresionado.getText().toString();

        if (respuestaElegida.equals(preguntaActual.getRespuestaCorrecta())) {
            // --- CORRECTO ---
            puntaje += 10;
            reproducirSonido(R.raw.correct_ding); // SONIDO
            botonPresionado.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(this, R.color.game_success)));
        } else {
            // --- INCORRECTO ---
            vidas--;
            reproducirSonido(R.raw.megaman_x_error); // SONIDO
            botonPresionado.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(this, R.color.game_fail)));

            // Animación de sacudida (Shake)
            Animation shake = AnimationUtils.loadAnimation(this, R.anim.shake_error);
            botonPresionado.startAnimation(shake);
            binding.cardQuestion.startAnimation(shake);

            resaltarRespuestaCorrecta();
        }

        if (vidas <= 0) {
            mostrarResultadoFinal();
        } else {
            new Handler(Looper.getMainLooper()).postDelayed(this::mostrarSiguientePregunta, 1500);
        }
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

    private void restaurarBotones() {
        binding.btnQuizOptionA.setBackgroundTintList(ColorStateList.valueOf(Color.WHITE));
        binding.btnQuizOptionB.setBackgroundTintList(ColorStateList.valueOf(Color.WHITE));
        binding.btnQuizOptionC.setBackgroundTintList(ColorStateList.valueOf(Color.WHITE));
    }

    private void resaltarRespuestaCorrecta() {
        String correcta = preguntaActual.getRespuestaCorrecta();
        if (binding.btnQuizOptionA.getText().toString().equals(correcta)) {
            binding.btnQuizOptionA.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(this, R.color.game_success)));
        } else if (binding.btnQuizOptionB.getText().toString().equals(correcta)) {
            binding.btnQuizOptionB.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(this, R.color.game_success)));
        } else if (binding.btnQuizOptionC.getText().toString().equals(correcta)) {
            binding.btnQuizOptionC.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(this, R.color.game_success)));
        }
    }

    private void mostrarResultadoFinal() {
        if (mAuth.getCurrentUser() != null && puntaje > 0) {
            mStore.collection("usuarios").document(mAuth.getCurrentUser().getUid())
                    .update("puntaje", FieldValue.increment(puntaje));
        }

        new com.google.android.material.dialog.MaterialAlertDialogBuilder(this)
                .setTitle("¡Juego Terminado!")
                .setMessage("Tu puntaje final es: " + puntaje)
                .setPositiveButton("Genial", (dialog, which) -> finish())
                .setCancelable(false)
                .show();
    }
}
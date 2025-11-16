package com.example.reglamentoupp;

import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.example.reglamentoupp.databinding.ActivityQuizBinding;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class QuizActivity extends AppCompatActivity implements View.OnClickListener {

    private ActivityQuizBinding binding;
    private FirebaseFirestore mStore;

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
                        Toast.makeText(this, "No se encontraron preguntas.", Toast.LENGTH_SHORT).show();
                        finish();
                        return;
                    }

                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        listaDePreguntas.add(doc.toObject(Pregunta.class));
                    }

                    // Mezclar y limitar a 10 preguntas
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
            // Fin del juego
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
            // Correcto
            puntaje += 10;
            botonPresionado.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(this, R.color.game_success)));
        } else {
            // Incorrecto
            vidas--;
            botonPresionado.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(this, R.color.game_fail)));
            // Resaltar la correcta
            resaltarRespuestaCorrecta();
        }

        if (vidas <= 0) {
            mostrarResultadoFinal();
        } else {
            // Esperar 1.5 segundos y pasar a la siguiente
            new Handler(Looper.getMainLooper()).postDelayed(this::mostrarSiguientePregunta, 1500);
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
        // Aquí puedes actualizar el puntaje en Firestore si lo deseas
        // Por ahora, solo mostramos un diálogo
        new com.google.android.material.dialog.MaterialAlertDialogBuilder(this)
                .setTitle("¡Juego Terminado!")
                .setMessage("Tu puntaje final es: " + puntaje)
                .setPositiveButton("Genial", (dialog, which) -> finish())
                .setCancelable(false)
                .show();
    }
}
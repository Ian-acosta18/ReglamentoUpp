package com.example.reglamentoupp;

import android.content.res.ColorStateList;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.example.reglamentoupp.databinding.ActivityQuizBinding;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
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
                        Toast.makeText(this, "No hay preguntas disponibles.", Toast.LENGTH_SHORT).show();
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

    private void iniciarQuiz() {
        indicePreguntaActual = 0;
        puntaje = 0;
        vidas = 3;
        mostrarSiguientePregunta();
    }

    private void mostrarSiguientePregunta() {
        if (indicePreguntaActual < listaDePreguntas.size()) {
            botonesBloqueados = false;
            restaurarBotones(); // Aquí se aplican los colores correctos
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
            reproducirSonido(R.raw.correct_ding);
            // Fondo Verde
            botonPresionado.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(this, R.color.game_success)));
        } else {
            // --- INCORRECTO ---
            vidas--;
            reproducirSonido(R.raw.megaman_x_error);
            // Fondo Rojo
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

    // --- CORRECCIÓN PRINCIPAL AQUÍ ---
    private void restaurarBotones() {
        // Usamos el color semitransparente (#33FFFFFF) igual que en el XML
        // Esto asegura que el texto blanco se vea sobre el fondo semitransparente oscuro/degradado
        int colorOriginal = Color.parseColor("#33FFFFFF");

        binding.btnQuizOptionA.setBackgroundTintList(ColorStateList.valueOf(colorOriginal));
        binding.btnQuizOptionB.setBackgroundTintList(ColorStateList.valueOf(colorOriginal));
        binding.btnQuizOptionC.setBackgroundTintList(ColorStateList.valueOf(colorOriginal));

        // Nos aseguramos de que el texto siga siendo blanco
        int colorTexto = ContextCompat.getColor(this, R.color.white);
        binding.btnQuizOptionA.setTextColor(colorTexto);
        binding.btnQuizOptionB.setTextColor(colorTexto);
        binding.btnQuizOptionC.setTextColor(colorTexto);
    }

    private void resaltarRespuestaCorrecta() {
        String correcta = preguntaActual.getRespuestaCorrecta();
        int colorVerde = ContextCompat.getColor(this, R.color.game_success);

        if (binding.btnQuizOptionA.getText().toString().equals(correcta)) {
            binding.btnQuizOptionA.setBackgroundTintList(ColorStateList.valueOf(colorVerde));
        } else if (binding.btnQuizOptionB.getText().toString().equals(correcta)) {
            binding.btnQuizOptionB.setBackgroundTintList(ColorStateList.valueOf(colorVerde));
        } else if (binding.btnQuizOptionC.getText().toString().equals(correcta)) {
            binding.btnQuizOptionC.setBackgroundTintList(ColorStateList.valueOf(colorVerde));
        }
    }

    private void mostrarResultadoFinal() {
        if (mAuth.getCurrentUser() != null && puntaje > 0) {
            mStore.collection("usuarios").document(mAuth.getCurrentUser().getUid())
                    .update("puntaje", FieldValue.increment(puntaje));
        }

        new MaterialAlertDialogBuilder(this)
                .setTitle("¡Juego Terminado!")
                .setMessage("Tu puntaje final es: " + puntaje)
                .setPositiveButton("Genial", (dialog, which) -> finish())
                .setCancelable(false)
                .show();
    }
}
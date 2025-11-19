package com.example.reglamentoupp;

import android.content.res.ColorStateList;
import android.graphics.Color;
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
                        // Si no hay preguntas, las creamos
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

    // --- AGREGAR MÁS PREGUNTAS MODO DESAFÍO ---
    private void crearPreguntasDesafio() {
        List<Pregunta> nuevas = new ArrayList<>();

        nuevas.add(new Pregunta("General", "¿Cuál es la sanción por inasistencia colectiva?", "Amonestación", "Expulsión", "Suspensión", "Amonestación"));
        nuevas.add(new Pregunta("Derechos", "¿Qué artículo habla sobre recibir asesorías?", "Art. 3 (XIII)", "Art. 5 (II)", "Art. 8 (I)", "Art. 3 (XIII)"));

        // --- NUEVAS PREGUNTAS ---
        nuevas.add(new Pregunta("Académico", "¿Cuál es la calificación mínima aprobatoria?", "6.0", "7.0", "8.0", "7.0"));
        nuevas.add(new Pregunta("Sanciones", "¿Qué sanción aplica por falsificar documentos?", "Suspensión", "Baja Definitiva", "Amonestación", "Baja Definitiva"));
        nuevas.add(new Pregunta("Derechos", "¿Puedes solicitar revisión de calificación?", "No", "Sí, en 24hrs", "Sí, cuando quieras", "Sí, en 24hrs"));
        nuevas.add(new Pregunta("Obligaciones", "Si rompes un microscopio por descuido...", "No pasa nada", "Lo pagas", "Te expulsan", "Lo pagas"));
        nuevas.add(new Pregunta("Prohibiciones", "¿Está permitido ingresar con aliento alcohólico?", "Solo viernes", "Si no manejas", "Prohibido siempre", "Prohibido siempre"));
        nuevas.add(new Pregunta("Académico", "¿Cuántas oportunidades tienes para pasar una materia?", "1 (Ordinaria)", "2 (Ord y Rec)", "3 (Ord, Rec, Esp)", "3 (Ord, Rec, Esp)"));
        nuevas.add(new Pregunta("General", "La credencial es de uso:", "Compartido", "Personal e intransferible", "Opcional", "Personal e intransferible"));
        nuevas.add(new Pregunta("Reconocimientos", "¿Qué promedio necesitas para Mención Honorífica?", "8.5", "9.0", "9.5 o más", "9.5 o más"));
        nuevas.add(new Pregunta("Obligaciones", "Asistir puntualmente a clases es una:", "Sugerencia", "Obligación", "Opción", "Obligación"));
        nuevas.add(new Pregunta("Prohibiciones", "Vender dulces en el salón es:", "Emprendimiento", "Prohibición", "Derecho", "Prohibición"));

        for (Pregunta p : nuevas) {
            mStore.collection("preguntas").add(p);
        }
        Toast.makeText(this, "Creando preguntas... Reinicia.", Toast.LENGTH_LONG).show();
        finish();
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
            puntaje += 10;
            botonPresionado.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(this, R.color.game_success)));
        } else {
            vidas--;
            botonPresionado.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(this, R.color.game_fail)));

            // --- ANIMACIÓN DE ERROR (SHAKE) ---
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
            // Actualizar en Firestore
            mStore.collection("usuarios").document(mAuth.getCurrentUser().getUid())
                    .update("puntaje", FieldValue.increment(puntaje))
                    .addOnSuccessListener(aVoid -> Log.d("Quiz", "Puntaje guardado"))
                    .addOnFailureListener(e -> Log.e("Quiz", "Error guardando puntaje", e));
        }

        new com.google.android.material.dialog.MaterialAlertDialogBuilder(this)
                .setTitle("¡Juego Terminado!")
                .setMessage("Tu puntaje final es: " + puntaje)
                .setPositiveButton("Genial", (dialog, which) -> finish())
                .setCancelable(false)
                .show();
    }
}
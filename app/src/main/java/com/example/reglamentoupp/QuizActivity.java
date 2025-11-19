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
import com.google.firebase.auth.FirebaseAuth; // Importar
import com.google.firebase.firestore.FieldValue; // Importar
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class QuizActivity extends AppCompatActivity implements View.OnClickListener {

    private ActivityQuizBinding binding;
    private FirebaseFirestore mStore;
    private FirebaseAuth mAuth; // Importante para guardar puntos

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
        mAuth = FirebaseAuth.getInstance(); // Inicializar
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
                        // Si no hay preguntas en Modo Desafío, las creamos
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
        nuevas.add(new Pregunta("Prohibiciones", "¿Se permite consumir alimentos en la biblioteca?", "Sí, snacks", "No, en ningún caso", "Solo agua", "No, en ningún caso"));
        nuevas.add(new Pregunta("Obligaciones", "¿Quién es responsable del proceso de formación?", "El profesor", "El alumno", "La universidad", "El alumno"));
        nuevas.add(new Pregunta("General", "¿Cuándo debes portar tu credencial?", "Solo en exámenes", "Al ingresar", "Nunca", "Al ingresar"));
        nuevas.add(new Pregunta("Sanciones", "¿Qué pasa si rompes material de laboratorio?", "Nada", "Lo pagas o repones", "Te expulsan", "Lo pagas o repones"));
        nuevas.add(new Pregunta("Derechos", "Tienes derecho a conocer el resultado de evaluaciones...", "Al final del año", "Oportunamente", "Nunca", "Oportunamente"));
        nuevas.add(new Pregunta("Prohibiciones", "¿Qué actividad está prohibida en los salones?", "Estudiar", "Juegos de azar", "Hablar", "Juegos de azar"));

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

    // --- AQUÍ ES DONDE GUARDAMOS LOS PUNTOS ---
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
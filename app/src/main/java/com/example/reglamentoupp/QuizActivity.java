package com.example.reglamentoupp;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;

import com.airbnb.lottie.LottieAnimationView;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class QuizActivity extends AppCompatActivity {

    private TextView tvQuestion, tvLives, tvScore, tvSpeechBubble;
    private AppCompatButton btnOptionA, btnOptionB, btnOptionC;
    private ProgressBar progressBar;
    private ImageButton btnBack, btnHelp;
    private LottieAnimationView lottieCharacter;

    private FirebaseFirestore db;
    private List<Pregunta> listaPreguntas;
    private Pregunta preguntaActual;
    private int indicePreguntaActual = 0;
    private int vidas = 3;
    private int puntaje = 0;
    private int preguntasRespondidas = 0;

    private String modoJuego;
    private String categoriaSeleccionada;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_quiz);

        tvQuestion = findViewById(R.id.tvQuizQuestion);
        tvLives = findViewById(R.id.tvQuizLives);
        tvScore = findViewById(R.id.tvQuizScore);
        tvSpeechBubble = findViewById(R.id.tvSpeechBubble);
        btnOptionA = findViewById(R.id.btnQuizOptionA);
        btnOptionB = findViewById(R.id.btnQuizOptionB);
        btnOptionC = findViewById(R.id.btnQuizOptionC);
        progressBar = findViewById(R.id.quizProgressBar);
        btnBack = findViewById(R.id.btnBackQuiz);
        btnHelp = findViewById(R.id.btnHelpQuiz);
        lottieCharacter = findViewById(R.id.lottieCharacter);

        if (lottieCharacter != null) {
            lottieCharacter.setVisibility(View.VISIBLE);
            // CAMBIO AQUI: Búho inicial
            lottieCharacter.setAnimation(R.raw.owlie_nodding);
            lottieCharacter.playAnimation();
        }

        db = FirebaseFirestore.getInstance();
        listaPreguntas = new ArrayList<>();

        modoJuego = getIntent().getStringExtra("MODO_JUEGO");
        categoriaSeleccionada = getIntent().getStringExtra("CATEGORIA");

        if (modoJuego == null) modoJuego = "supervivencia";
        if (categoriaSeleccionada == null) categoriaSeleccionada = "General";

        configurarUI();
        cargarPreguntas();

        btnBack.setOnClickListener(v -> mostrarDialogoSalida());
        btnHelp.setOnClickListener(v -> usarComodin());

        btnOptionA.setOnClickListener(v -> verificarRespuesta(btnOptionA.getText().toString(), btnOptionA));
        btnOptionB.setOnClickListener(v -> verificarRespuesta(btnOptionB.getText().toString(), btnOptionB));
        btnOptionC.setOnClickListener(v -> verificarRespuesta(btnOptionC.getText().toString(), btnOptionC));
    }

    private void configurarUI() {
        actualizarMarcadores();
        progressBar.setMax(10);
        progressBar.setProgress(0);
        tvSpeechBubble.setVisibility(View.VISIBLE);
        tvSpeechBubble.setText("¡Demuestra lo que sabes!");
    }

    private void cargarPreguntas() {
        tvQuestion.setText("Cargando preguntas...");
        deshabilitarBotones();

        db.collection("preguntas")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            Pregunta p = document.toObject(Pregunta.class);
                            listaPreguntas.add(p);
                        }

                        if (!listaPreguntas.isEmpty()) {
                            Collections.shuffle(listaPreguntas);
                            mostrarSiguientePregunta();
                        } else {
                            tvQuestion.setText("Aún no tienes preguntas en Firebase.");
                            Toast.makeText(this, "Base de datos vacía", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        tvQuestion.setText("Error al cargar.");
                        Toast.makeText(this, "Error de conexión", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void mostrarSiguientePregunta() {
        if (indicePreguntaActual < listaPreguntas.size()) {
            preguntaActual = listaPreguntas.get(indicePreguntaActual);

            tvQuestion.setText(preguntaActual.getPregunta());
            btnOptionA.setText(preguntaActual.getOpcionA());
            btnOptionB.setText(preguntaActual.getOpcionB());
            btnOptionC.setText(preguntaActual.getOpcionC());

            restaurarEstiloBotones();
            habilitarBotones();

            tvSpeechBubble.setText("¿Cuál es la correcta?");
        } else {
            finalizarJuego(true);
        }
    }

    private void verificarRespuesta(String respuestaSeleccionada, AppCompatButton botonSeleccionado) {
        deshabilitarBotones();
        String correcta = preguntaActual.getRespuestaCorrecta();

        if (respuestaSeleccionada.equals(correcta)) {
            // ACERTÓ
            botonSeleccionado.setBackgroundResource(R.drawable.button_quiz_correct);
            puntaje += 10;
            preguntasRespondidas++;
            progressBar.setProgress(preguntasRespondidas);
            tvSpeechBubble.setText("¡Excelente!");

            // CAMBIO AQUI: Búho asintiendo para el acierto (en vez de sol)
            lottieCharacter.setAnimation(R.raw.owlie_nodding);
            lottieCharacter.playAnimation();

            new Handler(Looper.getMainLooper()).postDelayed(() -> {
                indicePreguntaActual++;

                lottieCharacter.setAnimation(R.raw.owlie_nodding);
                lottieCharacter.playAnimation();

                mostrarSiguientePregunta();
                actualizarMarcadores();
            }, 1500);

        } else {
            // FALLÓ
            botonSeleccionado.setBackgroundResource(R.drawable.button_quiz_incorrect);
            vidas--;
            tvSpeechBubble.setText("¡Ups! Fallaste.");

            // CAMBIO AQUI: Búho enojado para el error
            lottieCharacter.setAnimation(R.raw.owlie_mad);
            lottieCharacter.playAnimation();

            resaltarRespuestaCorrecta(correcta);
            actualizarMarcadores();

            new Handler(Looper.getMainLooper()).postDelayed(() -> {
                if (vidas <= 0) {
                    finalizarJuego(false);
                } else {
                    indicePreguntaActual++;

                    // Regresa al búho normal para la siguiente pregunta
                    lottieCharacter.setAnimation(R.raw.owlie_nodding);
                    lottieCharacter.playAnimation();

                    mostrarSiguientePregunta();
                }
            }, 2000);
        }
    }

    private void resaltarRespuestaCorrecta(String correcta) {
        if (btnOptionA.getText().toString().equals(correcta)) {
            btnOptionA.setBackgroundResource(R.drawable.button_quiz_correct);
        } else if (btnOptionB.getText().toString().equals(correcta)) {
            btnOptionB.setBackgroundResource(R.drawable.button_quiz_correct);
        } else if (btnOptionC.getText().toString().equals(correcta)) {
            btnOptionC.setBackgroundResource(R.drawable.button_quiz_correct);
        }
    }

    private void actualizarMarcadores() {
        tvLives.setText("❤️ " + vidas);
        tvScore.setText("🏆 " + puntaje);
    }

    private void deshabilitarBotones() {
        btnOptionA.setEnabled(false);
        btnOptionB.setEnabled(false);
        btnOptionC.setEnabled(false);
    }

    private void habilitarBotones() {
        btnOptionA.setEnabled(true);
        btnOptionB.setEnabled(true);
        btnOptionC.setEnabled(true);
    }

    private void restaurarEstiloBotones() {
        btnOptionA.setBackgroundResource(R.drawable.button_quiz_default);
        btnOptionB.setBackgroundResource(R.drawable.button_quiz_default);
        btnOptionC.setBackgroundResource(R.drawable.button_quiz_default);
    }

    private void usarComodin() {
        if (puntaje >= 20) {
            puntaje -= 20;
            actualizarMarcadores();
            tvSpeechBubble.setText("Pista: Piensa en el Reglamento...");
            btnHelp.setEnabled(false);
            btnHelp.setAlpha(0.5f);
        } else {
            Toast.makeText(this, "Necesitas 20 puntos", Toast.LENGTH_SHORT).show();
        }
    }

    private void finalizarJuego(boolean victoria) {
        String titulo = victoria ? "¡Ganaste!" : "¡Fin del Juego!";
        String mensaje = "Lograste " + puntaje + " puntos.";

        new MaterialAlertDialogBuilder(this)
                .setTitle(titulo)
                .setMessage(mensaje)
                .setCancelable(false)
                .setPositiveButton("Regresar", (dialog, which) -> finish())
                .show();
    }

    private void mostrarDialogoSalida() {
        new MaterialAlertDialogBuilder(this)
                .setTitle("¿Abandonar partida?")
                .setMessage("Perderás tu progreso actual.")
                .setPositiveButton("Sí, salir", (dialog, which) -> finish())
                .setNegativeButton("Cancelar", null)
                .show();
    }

    @Override
    public void onBackPressed() {
        mostrarDialogoSalida();
    }
}
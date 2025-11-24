package com.example.reglamentoupp;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Looper;
import android.os.VibrationEffect;
import android.os.Vibrator;
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

    // --- VARIABLES DE DINAMISMO (NUEVAS) ---
    private CountDownTimer temporizador;
    private static final long TIEMPO_POR_PREGUNTA = 15000; // 15 segundos
    private int racha = 0; // Contador de respuestas seguidas correctas
    private Vibrator vibrator;

    private final String[] frasesExito = {"¡Brillante!", "¡Iluminaste el día!", "¡Correcto!", "¡Radiante!"};
    private final String[] frasesError = {"¡Rayos!", "Se nubló...", "Intenta de nuevo", "¡Cuidado!"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityQuizBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Inicializar Vibrator
        vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);

        mStore = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();
        listaDePreguntas = new ArrayList<>();

        // Mensaje inicial
        binding.tvSpeechBubble.setText("¡Quiz de Opción Múltiple!");
        binding.tvSpeechBubble.setVisibility(View.VISIBLE);

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
        racha = 0;
        mostrarSiguientePregunta();
    }

    private void mostrarSiguientePregunta() {
        if (indicePreguntaActual < listaDePreguntas.size()) {
            botonesBloqueados = false;
            restaurarBotones();

            // 1. Restaurar Mascota
            binding.lottieCharacter.setAnimation(R.raw.smilling_cloud);
            binding.lottieCharacter.playAnimation();

            preguntaActual = listaDePreguntas.get(indicePreguntaActual);

            binding.tvQuizQuestion.setText(preguntaActual.getPregunta());
            binding.btnQuizOptionA.setText(preguntaActual.getOpcionA());
            binding.btnQuizOptionB.setText(preguntaActual.getOpcionB());
            binding.btnQuizOptionC.setText(preguntaActual.getOpcionC());

            binding.tvQuizScore.setText("Puntaje: " + puntaje);
            binding.tvQuizLives.setText("Vidas: " + vidas);
            binding.quizProgressBar.setProgress(indicePreguntaActual + 1);

            // INICIAR TEMPORIZADOR PARA ESTA PREGUNTA
            iniciarTemporizador();

            indicePreguntaActual++;
        } else {
            mostrarResultadoFinal();
        }
    }

    // --- NUEVO: Lógica del Temporizador ---
    private void iniciarTemporizador() {
        if (temporizador != null) {
            temporizador.cancel();
        }

        binding.tvSpeechBubble.setTextColor(Color.BLACK); // Color normal

        temporizador = new CountDownTimer(TIEMPO_POR_PREGUNTA, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                int segundos = (int) (millisUntilFinished / 1000);
                binding.tvSpeechBubble.setText("Tiempo: " + segundos + "s ⏳");

                // Efecto de urgencia
                if (segundos <= 5) {
                    binding.tvSpeechBubble.setTextColor(Color.RED);
                    binding.lottieCharacter.setSpeed(1.5f); // Acelerar animación
                } else {
                    binding.lottieCharacter.setSpeed(1.0f);
                }
            }

            @Override
            public void onFinish() {
                binding.tvSpeechBubble.setText("¡Tiempo agotado!");
                manejarError(null, true); // True indica que fue por tiempo
            }
        }.start();
    }

    @Override
    public void onClick(View v) {
        if (botonesBloqueados) return;
        botonesBloqueados = true;

        if (temporizador != null) temporizador.cancel(); // Detener reloj

        Button botonPresionado = (Button) v;
        String respuestaElegida = botonPresionado.getText().toString();
        boolean esCorrecto = respuestaElegida.equals(preguntaActual.getRespuestaCorrecta());

        if (esCorrecto) {
            manejarAcierto(botonPresionado);
        } else {
            manejarError(botonPresionado, false);
        }
    }

    private void manejarAcierto(Button botonPresionado) {
        // Lógica de Racha
        racha++;
        int puntosGanados = 10;

        // Bonus
        if (racha >= 3) {
            puntosGanados += 5;
            binding.tvSpeechBubble.setText("¡RACHA x" + racha + "! 🔥");
        } else {
            binding.tvSpeechBubble.setText(frasesExito[(int) (Math.random() * frasesExito.length)]);
        }

        puntaje += puntosGanados;

        // Efectos
        vibrar(50); // Vibración corta y agradable
        reproducirSonido(R.raw.correct_ding);

        botonPresionado.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(this, R.color.game_success)));

        // Animar texto de puntaje
        binding.tvQuizScore.animate().scaleX(1.2f).scaleY(1.2f).setDuration(200).withEndAction(() ->
                binding.tvQuizScore.animate().scaleX(1f).scaleY(1f).setDuration(200).start()
        ).start();

        actualizarMascota(true);
        postQuestionDelay(true);
    }

    private void manejarError(Button botonPresionado, boolean porTiempo) {
        racha = 0; // Romper racha
        vidas--;

        // Efectos
        vibrar(400); // Vibración larga de error
        reproducirSonido(R.raw.megaman_x_error);

        if (!porTiempo && botonPresionado != null) {
            botonPresionado.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(this, R.color.game_fail)));
            botonPresionado.startAnimation(AnimationUtils.loadAnimation(this, R.anim.shake_error));
        }

        resaltarRespuestaCorrecta();
        actualizarMascota(false);
        postQuestionDelay(false);
    }

    private void postQuestionDelay(boolean acerto) {
        long delay = acerto ? 2000 : 2500;
        if (vidas <= 0) {
            new Handler(Looper.getMainLooper()).postDelayed(this::mostrarResultadoFinal, 1500);
        } else {
            new Handler(Looper.getMainLooper()).postDelayed(this::mostrarSiguientePregunta, delay);
        }
    }

    private void actualizarMascota(boolean esCorrecto) {
        int animacionRes;
        if (esCorrecto) {
            animacionRes = R.raw.happy_sun;
        } else {
            // Si no hay texto de racha/error seteado, poner frase de error aleatoria
            if(binding.tvSpeechBubble.getText().toString().contains("Tiempo")) {
                binding.tvSpeechBubble.setText(frasesError[(int) (Math.random() * frasesError.length)]);
            }
            animacionRes = R.raw.angry_thunderstorm;
        }
        binding.lottieCharacter.setAnimation(animacionRes);
        binding.lottieCharacter.playAnimation();
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

    // Método helper para vibración compatible
    private void vibrar(long milisegundos) {
        if (vibrator != null && vibrator.hasVibrator()) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator.vibrate(VibrationEffect.createOneShot(milisegundos, VibrationEffect.DEFAULT_AMPLITUDE));
            } else {
                vibrator.vibrate(milisegundos);
            }
        }
    }

    private void restaurarBotones() {
        int colorOriginal = Color.parseColor("#33FFFFFF");
        binding.btnQuizOptionA.setBackgroundTintList(ColorStateList.valueOf(colorOriginal));
        binding.btnQuizOptionB.setBackgroundTintList(ColorStateList.valueOf(colorOriginal));
        binding.btnQuizOptionC.setBackgroundTintList(ColorStateList.valueOf(colorOriginal));
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

    @Override
    protected void onPause() {
        super.onPause();
        if (temporizador != null) temporizador.cancel();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (temporizador != null) temporizador.cancel();
    }
}
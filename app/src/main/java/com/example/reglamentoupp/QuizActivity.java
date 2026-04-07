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
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.airbnb.lottie.RenderMode; // IMPORTANTE: Importación de Lottie
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
    private boolean botonesBloqueados = false;
    private boolean pistaUsada = false; // Control de pista

    private static final int MAX_PREGUNTAS = 10;

    private CountDownTimer temporizador;
    private static final long TIEMPO_POR_PREGUNTA = 15000;
    private long tiempoRestante = TIEMPO_POR_PREGUNTA;

    private int racha = 0;
    private Vibrator vibrator;
    private MediaPlayer mediaPlayer;

    private final String[] frasesExito = {"¡Brillante!", "¡Iluminaste el día!", "¡Correcto!", "¡Radiante!"};
    private final String[] frasesError = {"¡Rayos!", "Se nubló...", "Intenta de nuevo", "¡Cuidado!"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityQuizBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // FORZAR RENDERIZADO POR SOFTWARE PARA LOTTIE DESDE JAVA
        binding.lottieCharacter.setRenderMode(RenderMode.SOFTWARE);

        vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        mStore = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();
        listaDePreguntas = new ArrayList<>();

        binding.tvSpeechBubble.setText("¡Quiz de Opción Múltiple!");
        binding.tvSpeechBubble.setVisibility(View.VISIBLE);

        binding.btnQuizOptionA.setOnClickListener(this);
        binding.btnQuizOptionB.setOnClickListener(this);
        binding.btnQuizOptionC.setOnClickListener(this);

        binding.btnBackQuiz.setOnClickListener(v -> {
            if (temporizador != null) temporizador.cancel();
            finish();
        });

        binding.btnHelpQuiz.setOnClickListener(v -> usarPista());

        cargarTodasLasPreguntas();
    }

    private void usarPista() {
        if (pistaUsada || botonesBloqueados || preguntaActual == null) {
            Toast.makeText(this, "Ya usaste la pista o no está disponible", Toast.LENGTH_SHORT).show();
            return;
        }
        pistaUsada = true;

        String correcta = preguntaActual.getRespuestaCorrecta();
        List<Button> botones = new ArrayList<>();
        botones.add(binding.btnQuizOptionA);
        botones.add(binding.btnQuizOptionB);
        botones.add(binding.btnQuizOptionC);

        for (Button btn : botones) {
            if (!btn.getText().toString().equals(correcta) && !btn.getText().toString().isEmpty()) {
                btn.setText(""); // Oculta el texto
                btn.setEnabled(false); // Desactiva el botón
                binding.tvSpeechBubble.setText("¡Pista! Eliminé una opción falsa.");
                break; // Solo elimina una
            }
        }
    }

    private void cargarTodasLasPreguntas() {
        mStore.collection("preguntas").get().addOnSuccessListener(queryDocumentSnapshots -> {
            if (isFinishing()) return;
            for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                try { listaDePreguntas.add(doc.toObject(Pregunta.class)); } catch (Exception e) {}
            }
            Collections.shuffle(listaDePreguntas);
            if (listaDePreguntas.size() > MAX_PREGUNTAS) {
                listaDePreguntas = listaDePreguntas.subList(0, MAX_PREGUNTAS);
            }
            if(!listaDePreguntas.isEmpty()) {
                binding.quizProgressBar.setMax(listaDePreguntas.size());
                iniciarQuiz();
            }
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
        if (isFinishing()) return;
        tiempoRestante = TIEMPO_POR_PREGUNTA;
        pistaUsada = false; // Reiniciar pista para la nueva pregunta

        if (indicePreguntaActual < listaDePreguntas.size()) {
            botonesBloqueados = false;
            restaurarBotones();

            binding.lottieCharacter.setAnimation(R.raw.smilling_cloud);
            binding.lottieCharacter.playAnimation();

            preguntaActual = listaDePreguntas.get(indicePreguntaActual);

            binding.tvQuizQuestion.setText(preguntaActual.getPregunta());
            binding.btnQuizOptionA.setText(preguntaActual.getOpcionA());
            binding.btnQuizOptionB.setText(preguntaActual.getOpcionB());
            binding.btnQuizOptionC.setText(preguntaActual.getOpcionC());

            binding.tvQuizScore.setText("🏆 " + puntaje);
            binding.tvQuizLives.setText("❤️ " + vidas);
            binding.quizProgressBar.setProgress(indicePreguntaActual + 1);

            iniciarTemporizador();
            indicePreguntaActual++;
        } else {
            mostrarResultadoFinal();
        }
    }

    private void iniciarTemporizador() {
        if (temporizador != null) temporizador.cancel();
        binding.tvSpeechBubble.setTextColor(Color.BLACK);

        temporizador = new CountDownTimer(tiempoRestante, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                if(isFinishing()) { cancel(); return; }
                tiempoRestante = millisUntilFinished;
                int segundos = (int) (millisUntilFinished / 1000);

                if (!binding.tvSpeechBubble.getText().toString().contains("Pista")) {
                    binding.tvSpeechBubble.setText("Tiempo: " + segundos + "s ⏳");
                }

                if (segundos <= 5) {
                    binding.tvSpeechBubble.setTextColor(Color.RED);
                    binding.lottieCharacter.setSpeed(1.5f);
                } else {
                    binding.lottieCharacter.setSpeed(1.0f);
                }
            }
            @Override
            public void onFinish() {
                if(!isFinishing()) manejarError(null, true);
            }
        }.start();
    }

    @Override
    public void onClick(View v) {
        if (botonesBloqueados) return;
        botonesBloqueados = true;
        if (temporizador != null) temporizador.cancel();

        Button botonPresionado = (Button) v;
        String respuestaElegida = botonPresionado.getText().toString();
        String correcta = preguntaActual.getRespuestaCorrecta();

        if (respuestaElegida.equals(correcta)) manejarAcierto(botonPresionado);
        else manejarError(botonPresionado, false);
    }

    private void manejarAcierto(Button botonPresionado) {
        racha++;
        int puntosGanados = (racha >= 3) ? 15 : 10;
        if (racha >= 3) binding.tvSpeechBubble.setText("¡RACHA x" + racha + "! 🔥");
        else binding.tvSpeechBubble.setText(frasesExito[(int) (Math.random() * frasesExito.length)]);

        puntaje += puntosGanados;
        vibrar(50);
        reproducirSonido(R.raw.correct_ding);

        botonPresionado.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(this, R.color.game_success)));
        botonPresionado.setTextColor(Color.WHITE);
        actualizarMascota(true);
        postQuestionDelay(true);
    }

    private void manejarError(Button botonPresionado, boolean porTiempo) {
        racha = 0;
        vidas--;
        vibrar(400);
        reproducirSonido(R.raw.megaman_x_error);

        if (!porTiempo && botonPresionado != null) {
            botonPresionado.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(this, R.color.game_fail)));
            botonPresionado.setTextColor(Color.WHITE);
            botonPresionado.startAnimation(AnimationUtils.loadAnimation(this, R.anim.shake_error));
        }

        resaltarRespuestaCorrecta();
        actualizarMascota(false);
        postQuestionDelay(false);
    }

    private void postQuestionDelay(boolean acerto) {
        long delay = acerto ? 2000 : 2500;
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            if (isFinishing() || isDestroyed()) return;
            if (vidas <= 0) mostrarResultadoFinal();
            else mostrarSiguientePregunta();
        }, delay);
    }

    private void actualizarMascota(boolean esCorrecto) {
        binding.lottieCharacter.setAnimation(esCorrecto ? R.raw.happy_sun : R.raw.angry_thunderstorm);
        binding.lottieCharacter.playAnimation();
    }

    private void reproducirSonido(int soundResource) {
        try {
            if (mediaPlayer != null) { mediaPlayer.release(); mediaPlayer = null; }
            mediaPlayer = MediaPlayer.create(this, soundResource);
            if (mediaPlayer != null) {
                mediaPlayer.start();
                mediaPlayer.setOnCompletionListener(MediaPlayer::release);
            }
        } catch (Exception e) {}
    }

    private void vibrar(long milisegundos) {
        if (vibrator != null && vibrator.hasVibrator()) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) vibrator.vibrate(VibrationEffect.createOneShot(milisegundos, VibrationEffect.DEFAULT_AMPLITUDE));
            else vibrator.vibrate(milisegundos);
        }
    }

    private void restaurarBotones() {
        binding.btnQuizOptionA.setEnabled(true);
        binding.btnQuizOptionB.setEnabled(true);
        binding.btnQuizOptionC.setEnabled(true);

        binding.btnQuizOptionA.setBackgroundTintList(null);
        binding.btnQuizOptionB.setBackgroundTintList(null);
        binding.btnQuizOptionC.setBackgroundTintList(null);

        binding.btnQuizOptionA.setBackgroundResource(R.drawable.button_quiz_default);
        binding.btnQuizOptionB.setBackgroundResource(R.drawable.button_quiz_default);
        binding.btnQuizOptionC.setBackgroundResource(R.drawable.button_quiz_default);

        int colorTexto = Color.BLACK;
        binding.btnQuizOptionA.setTextColor(colorTexto);
        binding.btnQuizOptionB.setTextColor(colorTexto);
        binding.btnQuizOptionC.setTextColor(colorTexto);
    }

    private void resaltarRespuestaCorrecta() {
        if(preguntaActual == null) return;
        String correcta = preguntaActual.getRespuestaCorrecta();
        int colorVerde = ContextCompat.getColor(this, R.color.game_success);

        if (binding.btnQuizOptionA.getText().toString().equals(correcta)) {
            binding.btnQuizOptionA.setBackgroundTintList(ColorStateList.valueOf(colorVerde));
            binding.btnQuizOptionA.setTextColor(Color.WHITE);
        } else if (binding.btnQuizOptionB.getText().toString().equals(correcta)) {
            binding.btnQuizOptionB.setBackgroundTintList(ColorStateList.valueOf(colorVerde));
            binding.btnQuizOptionB.setTextColor(Color.WHITE);
        } else if (binding.btnQuizOptionC.getText().toString().equals(correcta)) {
            binding.btnQuizOptionC.setBackgroundTintList(ColorStateList.valueOf(colorVerde));
            binding.btnQuizOptionC.setTextColor(Color.WHITE);
        }
    }

    private void mostrarResultadoFinal() {
        if(isFinishing()) return;
        if (mAuth.getCurrentUser() != null && puntaje > 0) {
            mStore.collection("usuarios").document(mAuth.getCurrentUser().getUid())
                    .update("puntaje", FieldValue.increment(puntaje));
        }

        android.graphics.drawable.GradientDrawable shape = new android.graphics.drawable.GradientDrawable();
        shape.setColor(android.graphics.Color.WHITE);
        shape.setCornerRadius(40f);

        android.text.SpannableString titulo = new android.text.SpannableString("¡Juego Terminado!");
        titulo.setSpan(new android.text.style.ForegroundColorSpan(android.graphics.Color.BLACK), 0, titulo.length(), 0);
        titulo.setSpan(new android.text.style.StyleSpan(android.graphics.Typeface.BOLD), 0, titulo.length(), 0);

        android.text.SpannableString mensaje = new android.text.SpannableString("Tu puntaje final es: " + puntaje);
        mensaje.setSpan(new android.text.style.ForegroundColorSpan(android.graphics.Color.DKGRAY), 0, mensaje.length(), 0);

        new MaterialAlertDialogBuilder(this)
                .setTitle(titulo)
                .setMessage(mensaje)
                .setPositiveButton("Genial", (dialog, which) -> finish())
                .setCancelable(false)
                .setBackground(shape)
                .show();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (temporizador != null) temporizador.cancel();
        if (mediaPlayer != null) { mediaPlayer.release(); mediaPlayer = null; }
    }
}
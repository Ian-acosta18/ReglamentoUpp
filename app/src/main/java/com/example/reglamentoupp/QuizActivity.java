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

    // --- VARIABLES PARA LA MASCOTA ---
    private final String[] frasesExito = {"¡Brillante!", "¡Iluminaste el día!", "¡Correcto!", "¡Radiante!"};
    private final String[] frasesError = {"¡Rayos!", "Se nubló...", "Intenta de nuevo", "¡Cuidado!"};

    // Mensajes de apoyo (mientras piensa)
    private final String[] frasesApoyo = {
            "¿Cuál será? 🤔",
            "¡Tú puedes! 💪",
            "Lee con atención 🧐",
            "¡Vamos por el 10! 🌟",
            "Confía en ti ✨"
    };

    private Handler handlerApoyo = new Handler(Looper.getMainLooper());
    private int indiceApoyo = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityQuizBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

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

    // --- LÓGICA DE MENSAJES ROTATIVOS ---
    private Runnable runnableApoyo = new Runnable() {
        @Override
        public void run() {
            if (binding != null && binding.tvSpeechBubble != null && !botonesBloqueados) {
                // Animación suave de cambio de texto
                binding.tvSpeechBubble.animate().alpha(0f).setDuration(300).withEndAction(() -> {
                    if (binding != null && !botonesBloqueados) { // Doble check por si acaso
                        indiceApoyo = (indiceApoyo + 1) % frasesApoyo.length;
                        binding.tvSpeechBubble.setText(frasesApoyo[indiceApoyo]);
                        binding.tvSpeechBubble.animate().alpha(1f).setDuration(300).start();

                        // Programar siguiente frase en 4 segundos
                        handlerApoyo.postDelayed(this, 4000);
                    }
                }).start();
            }
        }
    };

    private void iniciarMensajesApoyo() {
        detenerMensajesApoyo(); // Limpiar anteriores
        // Esperar 3 segundos antes de empezar a rotar frases para dejar leer la pregunta
        handlerApoyo.postDelayed(runnableApoyo, 3000);
    }

    private void detenerMensajesApoyo() {
        handlerApoyo.removeCallbacks(runnableApoyo);
        binding.tvSpeechBubble.animate().cancel();
        binding.tvSpeechBubble.setAlpha(1f); // Restaurar visibilidad por si quedó a medias
    }

    @Override
    protected void onPause() {
        super.onPause();
        detenerMensajesApoyo();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        detenerMensajesApoyo();
    }

    // -------------------------------------

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
            restaurarBotones();

            // 1. Restaurar Mascota a estado normal (Nube)
            binding.lottieCharacter.setAnimation(R.raw.smilling_cloud);
            binding.lottieCharacter.playAnimation();

            // 2. Mensaje inicial de la pregunta (opcional, o dejar el último)
            binding.tvSpeechBubble.setText("¿Cuál será la respuesta?");
            binding.tvSpeechBubble.setVisibility(View.VISIBLE);

            // 3. INICIAR EL CICLO DE MENSAJES DE APOYO
            iniciarMensajesApoyo();

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

        // IMPORTANTE: Detener mensajes de apoyo para mostrar el resultado
        detenerMensajesApoyo();

        Button botonPresionado = (Button) v;
        String respuestaElegida = botonPresionado.getText().toString();
        boolean esCorrecto = respuestaElegida.equals(preguntaActual.getRespuestaCorrecta());

        if (esCorrecto) {
            puntaje += 10;
            reproducirSonido(R.raw.correct_ding);
            botonPresionado.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(this, R.color.game_success)));
            actualizarMascota(true);
        } else {
            vidas--;
            reproducirSonido(R.raw.megaman_x_error);
            botonPresionado.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(this, R.color.game_fail)));
            Animation shake = AnimationUtils.loadAnimation(this, R.anim.shake_error);
            botonPresionado.startAnimation(shake);
            resaltarRespuestaCorrecta();
            actualizarMascota(false);
        }

        if (vidas <= 0) {
            new Handler(Looper.getMainLooper()).postDelayed(this::mostrarResultadoFinal, 1500);
        } else {
            new Handler(Looper.getMainLooper()).postDelayed(this::mostrarSiguientePregunta, 2500);
        }
    }

    private void actualizarMascota(boolean esCorrecto) {
        String frase;
        int animacionRes;

        if (esCorrecto) {
            frase = frasesExito[(int) (Math.random() * frasesExito.length)];
            animacionRes = R.raw.happy_sun;
        } else {
            frase = frasesError[(int) (Math.random() * frasesError.length)];
            animacionRes = R.raw.angry_thunderstorm;
        }

        binding.tvSpeechBubble.setText(frase);
        binding.tvSpeechBubble.setVisibility(View.VISIBLE);
        binding.lottieCharacter.setAnimation(animacionRes);
        binding.lottieCharacter.playAnimation();
        binding.lottieCharacter.loop(true);
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
}
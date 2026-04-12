package com.example.reglamentoupp;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Looper;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.util.TypedValue;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.airbnb.lottie.RenderMode;
import com.example.reglamentoupp.databinding.ActivityTrueFalseBinding;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class TrueFalseActivity extends AppCompatActivity {

    private ActivityTrueFalseBinding binding;
    private FirebaseFirestore db;
    private List<PreguntaVF> questionList;
    private int currentQuestionIndex = 0;
    private int score = 0;
    private int lives = 3;
    private boolean botonesBloqueados = false;
    private boolean pistaUsada = false;

    private static final int MAX_PREGUNTAS = 10;
    private CountDownTimer temporizador;
    private long tiempoRestante = 10000;

    private int racha = 0;
    private Vibrator vibrator;
    private MediaPlayer mediaPlayer;
    private long ultimoClickTrue = 0;
    private long ultimoClickFalse = 0;

    private final String[] frasesExito = {"¡Es Verdad!", "¡Exacto!", "¡Bien visto!", "¡No te engañan!"};
    private final String[] frasesError = {"¡Caíste!", "Era mentira...", "Lee bien...", "¡Ups!"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityTrueFalseBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        if (binding.lottieCharacter != null) {
            binding.lottieCharacter.setRenderMode(RenderMode.SOFTWARE);
            // CAMBIO AQUI: Búho normal al iniciar
            binding.lottieCharacter.setAnimation(R.raw.owlie_nodding);
        }

        vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        db = FirebaseFirestore.getInstance();
        questionList = new ArrayList<>();

        binding.tvSpeechBubble.setText("¡Verdadero o Falso!");

        loadQuestions();

        binding.btnTrue.setOnClickListener(v -> {
            if (botonesBloqueados) return;
            if (System.currentTimeMillis() - ultimoClickFalse < 300) return;
            ultimoClickTrue = System.currentTimeMillis();
            checkAnswer(true);
        });

        binding.btnFalse.setOnClickListener(v -> {
            if (botonesBloqueados) return;
            if (System.currentTimeMillis() - ultimoClickTrue < 300) return;
            ultimoClickFalse = System.currentTimeMillis();
            checkAnswer(false);
        });

        if(binding.btnBack != null) {
            binding.btnBack.setOnClickListener(v -> {
                if (temporizador != null) temporizador.cancel();
                finish();
            });
        }

        if(binding.btnHelpTF != null) {
            binding.btnHelpTF.setOnClickListener(v -> usarPista());
        }
    }

    private void usarPista() {
        if (pistaUsada || botonesBloqueados) {
            Toast.makeText(this, "Ya usaste esta pista", Toast.LENGTH_SHORT).show();
            return;
        }
        pistaUsada = true;

        if (temporizador != null) temporizador.cancel();
        tiempoRestante += 5000;
        binding.tvSpeechBubble.setText("¡+5 Segundos extra! ⏳");
        binding.tvSpeechBubble.setTextColor(Color.parseColor("#4CAF50"));

        iniciarTemporizador(tiempoRestante);
    }

    private void loadQuestions() {
        db.collection("preguntasVF").get().addOnSuccessListener(queryDocumentSnapshots -> {
            if(isFinishing()) return;
            for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                try { questionList.add(doc.toObject(PreguntaVF.class)); } catch (Exception e) {}
            }
            Collections.shuffle(questionList);
            if (questionList.size() > MAX_PREGUNTAS) questionList = questionList.subList(0, MAX_PREGUNTAS);
            binding.progressBar.setMax(questionList.size());
            if(!questionList.isEmpty()) showQuestion();
        });
    }

    private void showQuestion() {
        if (isFinishing()) return;

        if (currentQuestionIndex < questionList.size() && lives > 0) {
            botonesBloqueados = false;
            pistaUsada = false;
            tiempoRestante = 10000;

            // CAMBIO AQUI: Búho normal al cargar pregunta
            binding.lottieCharacter.setAnimation(R.raw.owlie_nodding);
            binding.lottieCharacter.playAnimation();
            binding.lottieCharacter.setSpeed(1.0f);

            PreguntaVF q = questionList.get(currentQuestionIndex);
            binding.tvQuestion.setText(q.getAfirmacion());
            binding.progressBar.setProgress(currentQuestionIndex + 1);
            binding.tvScore.setText("🏆 " + score);
            binding.tvLives.setText("❤️ " + lives);

            iniciarTemporizador(tiempoRestante);
        } else {
            finishGame();
        }
    }

    private void iniciarTemporizador(long millisRestantes) {
        if (temporizador != null) temporizador.cancel();
        binding.tvSpeechBubble.setTextColor(Color.BLACK);

        temporizador = new CountDownTimer(millisRestantes, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                if(isFinishing()) { cancel(); return; }
                tiempoRestante = millisUntilFinished;
                int segundos = (int) (millisUntilFinished / 1000);

                if(!binding.tvSpeechBubble.getText().toString().contains("extra")) {
                    binding.tvSpeechBubble.setText("Tiempo: " + segundos + "s ⏳");
                }

                if (segundos <= 3) {
                    binding.tvSpeechBubble.setTextColor(Color.RED);
                    binding.lottieCharacter.setSpeed(1.5f);
                }
            }

            @Override
            public void onFinish() {
                if(!isFinishing()) {
                    binding.tvSpeechBubble.setText("¡Tiempo agotado!");
                    procesarResultado(false, true);
                }
            }
        }.start();
    }

    private void checkAnswer(boolean userSelectedTrue) {
        if (botonesBloqueados) return;
        if (temporizador != null) temporizador.cancel();

        boolean correctAnswer = questionList.get(currentQuestionIndex).isRespuesta();
        procesarResultado((userSelectedTrue == correctAnswer), false);
    }

    private void procesarResultado(boolean isCorrect, boolean porTiempo) {
        botonesBloqueados = true;

        if (isCorrect) {
            racha++;
            int puntos = (racha >= 3) ? 15 : 10;
            binding.tvSpeechBubble.setText(racha >= 3 ? "¡Racha de " + racha + "! 🔥" : frasesExito[(int) (Math.random() * frasesExito.length)]);
            score += puntos;
            vibrar(50);
            playSound(R.raw.correct_ding);
            actualizarMascota(true);
        } else {
            racha = 0;
            lives--;
            vibrar(400);
            playSound(R.raw.megaman_x_error);
            actualizarMascota(false);
            if(!porTiempo) binding.tvSpeechBubble.setText(frasesError[(int) (Math.random() * frasesError.length)]);
        }

        currentQuestionIndex++;
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            if(!isFinishing()) showQuestion();
        }, 2000);
    }

    private void actualizarMascota(boolean esCorrecto) {
        // CAMBIO AQUI: Selección del búho correcto en lugar de nubes
        binding.lottieCharacter.setAnimation(esCorrecto ? R.raw.owlie_nodding : R.raw.owlie_mad);
        binding.lottieCharacter.playAnimation();
    }

    private void playSound(int resId) {
        try {
            if(mediaPlayer != null) mediaPlayer.release();
            mediaPlayer = MediaPlayer.create(this, resId);
            if (mediaPlayer != null) mediaPlayer.start();
        } catch (Exception e) {}
    }

    private void vibrar(long milisegundos) {
        if (vibrator != null && vibrator.hasVibrator()) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) vibrator.vibrate(VibrationEffect.createOneShot(milisegundos, VibrationEffect.DEFAULT_AMPLITUDE));
            else vibrator.vibrate(milisegundos);
        }
    }

    private void finishGame() {
        if(isFinishing()) return;
        String uid = FirebaseAuth.getInstance().getUid();
        if (uid != null && score > 0) db.collection("usuarios").document(uid).update("puntaje", FieldValue.increment(score));

        android.graphics.drawable.GradientDrawable shape = new android.graphics.drawable.GradientDrawable();
        shape.setColor(android.graphics.Color.WHITE);
        shape.setCornerRadius(40f);

        android.text.SpannableString titulo = new android.text.SpannableString("Juego Terminado");
        titulo.setSpan(new android.text.style.ForegroundColorSpan(android.graphics.Color.BLACK), 0, titulo.length(), 0);
        titulo.setSpan(new android.text.style.StyleSpan(android.graphics.Typeface.BOLD), 0, titulo.length(), 0);

        android.text.SpannableString mensaje = new android.text.SpannableString("Puntaje Final: " + score);
        mensaje.setSpan(new android.text.style.ForegroundColorSpan(android.graphics.Color.DKGRAY), 0, mensaje.length(), 0);

        new MaterialAlertDialogBuilder(this).setTitle(titulo).setMessage(mensaje)
                .setPositiveButton("Salir", (dialog, which) -> finish())
                .setCancelable(false).setBackground(shape).show();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (temporizador != null) temporizador.cancel();
        if (mediaPlayer != null) { mediaPlayer.release(); mediaPlayer = null; }
    }
}
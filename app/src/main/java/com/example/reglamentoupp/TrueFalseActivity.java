package com.example.reglamentoupp;

import android.content.Context;
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
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

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

    private CountDownTimer temporizador;
    private static final long TIEMPO_POR_PREGUNTA = 10000;
    private int racha = 0;
    private Vibrator vibrator;
    private MediaPlayer mediaPlayer;

    private final String[] frasesExito = {"¡Es Verdad!", "¡Exacto!", "¡Bien visto!", "¡No te engañan!"};
    private final String[] frasesError = {"¡Caíste!", "Era mentira...", "Lee bien...", "¡Ups!"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityTrueFalseBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        db = FirebaseFirestore.getInstance();
        questionList = new ArrayList<>();

        binding.tvSpeechBubble.setText("¡Verdadero o Falso!");
        // REGRESAMOS A LA NUBE
        if (binding.lottieCharacter != null) {
            binding.lottieCharacter.setAnimation(R.raw.smilling_cloud);
        }

        loadQuestions();

        binding.btnTrue.setOnClickListener(v -> checkAnswer(true));
        binding.btnFalse.setOnClickListener(v -> checkAnswer(false));

        if(binding.btnBack != null) {
            binding.btnBack.setOnClickListener(v -> finish());
        }
    }

    private void loadQuestions() {
        db.collection("preguntasVF")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if(isFinishing()) return;
                    if (queryDocumentSnapshots.isEmpty()) return;
                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        try { questionList.add(doc.toObject(PreguntaVF.class)); } catch (Exception e) {}
                    }
                    Collections.shuffle(questionList);
                    if(!questionList.isEmpty()) showQuestion();
                })
                .addOnFailureListener(e -> {
                    if(!isFinishing()) Toast.makeText(this, "Error al cargar", Toast.LENGTH_SHORT).show();
                });
    }

    private void showQuestion() {
        if (isFinishing()) return;

        if (currentQuestionIndex < questionList.size() && lives > 0) {
            botonesBloqueados = false;

            // REGRESAMOS A LA NUBE
            binding.lottieCharacter.setAnimation(R.raw.smilling_cloud);
            binding.lottieCharacter.playAnimation();
            binding.lottieCharacter.setSpeed(1.0f);

            PreguntaVF q = questionList.get(currentQuestionIndex);
            binding.tvQuestion.setText(q.getAfirmacion());

            binding.progressBar.setProgress(currentQuestionIndex + 1);
            binding.tvScore.setText("Puntos: " + score);
            binding.tvLives.setText("❤️ " + lives);

            iniciarTemporizador();
        } else {
            finishGame();
        }
    }

    private void iniciarTemporizador() {
        if (temporizador != null) temporizador.cancel();
        binding.tvSpeechBubble.setTextColor(Color.BLACK);

        temporizador = new CountDownTimer(TIEMPO_POR_PREGUNTA, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                if(isFinishing()) { cancel(); return; }
                int segundos = (int) (millisUntilFinished / 1000);
                binding.tvSpeechBubble.setText("Tiempo: " + segundos + "s ⏳");
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
        boolean isCorrect = (userSelectedTrue == correctAnswer);

        procesarResultado(isCorrect, false);
    }

    private void procesarResultado(boolean isCorrect, boolean porTiempo) {
        botonesBloqueados = true;

        if (isCorrect) {
            racha++;
            int puntos = 10;
            if (racha >= 3) {
                puntos += 5;
                binding.tvSpeechBubble.setText("¡Racha de " + racha + "! 🔥");
            } else {
                binding.tvSpeechBubble.setText(frasesExito[(int) (Math.random() * frasesExito.length)]);
            }
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
            if(!porTiempo) {
                binding.tvSpeechBubble.setText(frasesError[(int) (Math.random() * frasesError.length)]);
            }
        }

        currentQuestionIndex++;
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            if(!isFinishing()) showQuestion();
        }, 2000);
    }

    private void actualizarMascota(boolean esCorrecto) {
        int animacionRes = esCorrecto ? R.raw.happy_sun : R.raw.angry_thunderstorm;
        binding.lottieCharacter.setAnimation(animacionRes);
        binding.lottieCharacter.playAnimation();
    }

    private void playSound(int resId) {
        try {
            if(mediaPlayer != null) mediaPlayer.release();
            mediaPlayer = MediaPlayer.create(this, resId);
            if (mediaPlayer != null) {
                mediaPlayer.start();
                mediaPlayer.setOnCompletionListener(MediaPlayer::release);
            }
        } catch (Exception e) { e.printStackTrace(); }
    }

    private void vibrar(long milisegundos) {
        if (vibrator != null && vibrator.hasVibrator()) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator.vibrate(VibrationEffect.createOneShot(milisegundos, VibrationEffect.DEFAULT_AMPLITUDE));
            } else {
                vibrator.vibrate(milisegundos);
            }
        }
    }

    private void finishGame() {
        if(isFinishing()) return;
        saveScore();
        new MaterialAlertDialogBuilder(this)
                .setTitle("Juego Terminado")
                .setMessage("Puntaje Final: " + score)
                .setPositiveButton("Salir", (dialog, which) -> finish())
                .setCancelable(false)
                .show();
    }

    private void saveScore() {
        String uid = FirebaseAuth.getInstance().getUid();
        if (uid != null && score > 0) {
            db.collection("usuarios").document(uid).update("puntaje", FieldValue.increment(score));
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (temporizador != null) temporizador.cancel();
        if (mediaPlayer != null) { mediaPlayer.release(); mediaPlayer = null; }
    }
}
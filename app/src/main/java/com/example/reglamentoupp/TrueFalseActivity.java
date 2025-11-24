package com.example.reglamentoupp;

import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
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

    // --- FRASES MASCOTA ---
    private final String[] frasesExito = {"¡Es Verdad!", "¡Exacto!", "¡Bien visto!", "¡No te engañan!"};
    private final String[] frasesError = {"¡Caíste!", "Era mentira...", "Lee bien...", "¡Ups!"};

    // Mensajes de apoyo rotativos
    private final String[] frasesApoyo = {
            "¿Será cierto? 🤔",
            "¡Cuidado con las trampas! ⚠️",
            "Analiza bien... 🧐",
            "¿Verdad o Mentira? ⚖️",
            "¡Confía en tu memoria! 🧠"
    };

    private Handler handlerApoyo = new Handler(Looper.getMainLooper());
    private int indiceApoyo = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityTrueFalseBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        db = FirebaseFirestore.getInstance();
        questionList = new ArrayList<>();

        // Mensaje inicial
        binding.tvSpeechBubble.setText("¡Verdadero o Falso!");
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

    // --- LÓGICA MENSAJES ROTATIVOS ---
    private Runnable runnableApoyo = new Runnable() {
        @Override
        public void run() {
            if (binding != null && binding.tvSpeechBubble != null && !botonesBloqueados) {
                binding.tvSpeechBubble.animate().alpha(0f).setDuration(300).withEndAction(() -> {
                    if (binding != null && !botonesBloqueados) {
                        indiceApoyo = (indiceApoyo + 1) % frasesApoyo.length;
                        binding.tvSpeechBubble.setText(frasesApoyo[indiceApoyo]);
                        binding.tvSpeechBubble.animate().alpha(1f).setDuration(300).start();
                        handlerApoyo.postDelayed(this, 4000);
                    }
                }).start();
            }
        }
    };

    private void iniciarMensajesApoyo() {
        detenerMensajesApoyo();
        handlerApoyo.postDelayed(runnableApoyo, 3000); // Esperar 3s iniciales
    }

    private void detenerMensajesApoyo() {
        handlerApoyo.removeCallbacks(runnableApoyo);
        binding.tvSpeechBubble.animate().cancel();
        binding.tvSpeechBubble.setAlpha(1f);
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
    // --------------------------------

    private void loadQuestions() {
        db.collection("preguntasVF")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (queryDocumentSnapshots.isEmpty()) return;
                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        questionList.add(doc.toObject(PreguntaVF.class));
                    }
                    Collections.shuffle(questionList);
                    showQuestion();
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Error al cargar", Toast.LENGTH_SHORT).show());
    }

    private void showQuestion() {
        if (currentQuestionIndex < questionList.size() && lives > 0) {
            botonesBloqueados = false;

            // 1. Restaurar mascota (Nube)
            binding.lottieCharacter.setAnimation(R.raw.smilling_cloud);
            binding.lottieCharacter.playAnimation();

            // 2. Reiniciar mensajes de apoyo
            binding.tvSpeechBubble.setText("¿Verdad o Mentira?");
            iniciarMensajesApoyo();

            PreguntaVF q = questionList.get(currentQuestionIndex);
            binding.tvQuestion.setText(q.getAfirmacion());

            binding.progressBar.setProgress(currentQuestionIndex + 1);
            binding.tvScore.setText("Puntos: " + score);
            binding.tvLives.setText("❤️ " + lives);
        } else {
            finishGame();
        }
    }

    private void checkAnswer(boolean userSelectedTrue) {
        if (botonesBloqueados) return;
        botonesBloqueados = true;

        // IMPORTANTE: Detener mensajes para mostrar resultado
        detenerMensajesApoyo();

        boolean correctAnswer = questionList.get(currentQuestionIndex).isRespuesta();
        boolean isCorrect = (userSelectedTrue == correctAnswer);

        if (isCorrect) {
            score += 10;
            playSound(R.raw.correct_ding);
            actualizarMascota(true);
        } else {
            lives--;
            playSound(R.raw.megaman_x_error);
            actualizarMascota(false);
        }

        currentQuestionIndex++;
        new Handler(Looper.getMainLooper()).postDelayed(this::showQuestion, 2000);
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
    }

    private void playSound(int resId) {
        try {
            MediaPlayer mp = MediaPlayer.create(this, resId);
            if (mp != null) {
                mp.start();
                mp.setOnCompletionListener(MediaPlayer::release);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void finishGame() {
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
}
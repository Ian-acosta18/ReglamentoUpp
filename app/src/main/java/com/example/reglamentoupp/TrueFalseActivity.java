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

    // Frases Específicas para Verdadero/Falso
    private final String[] frasesExito = {"¡Es Verdad!", "¡Exacto!", "¡Bien visto!", "¡No te engañan!"};
    private final String[] frasesError = {"¡Caíste!", "Era mentira...", "Lee bien...", "¡Ups!"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityTrueFalseBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        db = FirebaseFirestore.getInstance();
        questionList = new ArrayList<>();

        // MENSAJE INICIAL AL ENTRAR AL JUEGO
        binding.tvSpeechBubble.setText("¡Verdadero o Falso!");
        if (binding.lottieCharacter != null) {
            binding.lottieCharacter.setAnimation(R.raw.smilling_cloud);
        }

        loadQuestions();

        binding.btnTrue.setOnClickListener(v -> checkAnswer(true));
        binding.btnFalse.setOnClickListener(v -> checkAnswer(false));

        // --- CORRECCIÓN AQUÍ: Usamos btnBack en lugar de toolbarTf ---
        binding.btnBack.setOnClickListener(v -> finish());
    }

    private void loadQuestions() {
        // Asegúrate de que tu colección en Firebase se llame 'preguntasVF'
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

            // Restaurar mascota a estado normal
            binding.lottieCharacter.setAnimation(R.raw.smilling_cloud);
            binding.lottieCharacter.playAnimation();

            PreguntaVF q = questionList.get(currentQuestionIndex);

            // Usamos el getter correcto de tu clase PreguntaVF
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

        // Usamos el getter correcto de tu clase PreguntaVF
        boolean correctAnswer = questionList.get(currentQuestionIndex).isRespuesta();

        boolean isCorrect = (userSelectedTrue == correctAnswer);

        if (isCorrect) {
            score += 10;
            playSound(R.raw.correct_ding);
            actualizarMascota(true); // Mascota Feliz
        } else {
            lives--;
            playSound(R.raw.megaman_x_error);
            actualizarMascota(false); // Mascota Enojada
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
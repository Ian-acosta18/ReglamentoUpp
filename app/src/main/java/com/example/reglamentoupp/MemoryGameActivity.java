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
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.gridlayout.widget.GridLayout; // Asegúrate de tener esta dependencia o usa android.widget.GridLayout

import com.airbnb.lottie.LottieAnimationView;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class MemoryGameActivity extends AppCompatActivity {

    // --- Configuración ---
    private static final int COLUMNS = 3;
    private static final int TOTAL_PAIRS = 6; // 12 cartas en total (3x4)
    private static final long GAME_TIME_MS = 60000; // 60 segundos para resolverlo

    // UI
    private GridLayout glCards;
    private TextView tvScore, tvLives, tvMessage;
    private LottieAnimationView lottieMascot;
    private CountDownTimer timer;
    private Vibrator vibrator;

    // Estado del Juego
    private List<MemoryCard> cards;
    private MemoryCard selectedDetails1 = null;
    private MaterialButton selectedButton1 = null;
    private boolean isProcessing = false; // Bloquear toques mientras anima
    private int score = 0;
    private int lives = 5;
    private int pairsFound = 0;
    private int racha = 0;

    // Datos (Reutilizando conceptos del Reglamento)
    private String[][] dataPairs = {
            {"Tutor", "Guía"},
            {"Beca", "Apoyo"},
            {"Rector", "Autoridad"},
            {"Kardex", "Notas"},
            {"Baja", "Pausa"},
            {"Plagio", "Copia"},
            {"Falta", "Error"},
            {"Norma", "Regla"}
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_memory_game);

        // Inicializar vistas
        glCards = findViewById(R.id.glCards);
        tvScore = findViewById(R.id.tvScore);
        tvLives = findViewById(R.id.tvLives);
        tvMessage = findViewById(R.id.tvMessage);
        lottieMascot = findViewById(R.id.lottieMascot);
        vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);

        findViewById(R.id.toolbarMemory).setOnClickListener(v -> finish());

        startNewGame();
    }

    private void startNewGame() {
        score = 0;
        lives = 5;
        pairsFound = 0;
        racha = 0;
        isProcessing = false;
        selectedDetails1 = null;
        selectedButton1 = null;

        updateUI();
        setupBoard();
        startTimer();
    }

    private void setupBoard() {
        glCards.removeAllViews();
        cards = new ArrayList<>();

        // 1. Seleccionar pares aleatorios
        List<String[]> selectedPairs = new ArrayList<>();
        List<Integer> indices = new ArrayList<>();
        for (int i = 0; i < dataPairs.length; i++) indices.add(i);
        Collections.shuffle(indices);

        for (int i = 0; i < TOTAL_PAIRS; i++) {
            selectedPairs.add(dataPairs[indices.get(i)]);
        }

        // 2. Crear cartas (Concepto y Definición)
        int idCounter = 0;
        for (String[] pair : selectedPairs) {
            // Carta A (Concepto)
            cards.add(new MemoryCard(idCounter, pair[0], true, idCounter)); // ID compartido para par
            // Carta B (Definición)
            cards.add(new MemoryCard(idCounter + 100, pair[1], false, idCounter));
            idCounter++;
        }
        Collections.shuffle(cards);

        // 3. Añadir botones al Grid
        for (MemoryCard card : cards) {
            MaterialButton btn = new MaterialButton(this);

            // Estilo inicial (Boca abajo)
            btn.setText("?");
            btn.setTextSize(24);
            btn.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(this, R.color.upp_primary)));
            btn.setTextColor(Color.WHITE);
            btn.setCornerRadius(16);
            btn.setTag(card); // Guardar objeto en el tag

            // Layout Params para grid (peso equitativo)
            GridLayout.LayoutParams params = new GridLayout.LayoutParams();
            params.width = 0;
            params.height = 250; // Altura fija o dinámica
            params.columnSpec = GridLayout.spec(GridLayout.UNDEFINED, 1f);
            params.rowSpec = GridLayout.spec(GridLayout.UNDEFINED, 1f);
            params.setMargins(8, 8, 8, 8);
            btn.setLayoutParams(params);

            btn.setOnClickListener(v -> onCardClick(btn));
            glCards.addView(btn);
        }
    }

    private void onCardClick(MaterialButton btn) {
        if (isProcessing) return;
        MemoryCard cardData = (MemoryCard) btn.getTag();

        if (cardData.isMatched || cardData == selectedDetails1) return; // Ya resuelta o es la misma

        // Voltear carta (Animación visual)
        flipCard(btn, cardData, true);

        if (selectedDetails1 == null) {
            // Primera carta seleccionada
            selectedDetails1 = cardData;
            selectedButton1 = btn;
        } else {
            // Segunda carta seleccionada -> Comprobar Match
            isProcessing = true;
            checkMatch(btn, cardData);
        }
    }

    private void checkMatch(MaterialButton btn2, MemoryCard cardData2) {
        if (selectedDetails1.pairId == cardData2.pairId) {
            // --- ACIERTO ---
            handleMatch(selectedButton1, btn2);
        } else {
            // --- ERROR ---
            handleMismatch(selectedButton1, btn2);
        }
    }

    private void handleMatch(MaterialButton btn1, MaterialButton btn2) {
        racha++;
        score += (10 + (racha * 2)); // Bonus racha
        pairsFound++;

        playSound(R.raw.correct_ding);
        vibrar(50);

        MemoryCard c1 = (MemoryCard) btn1.getTag();
        MemoryCard c2 = (MemoryCard) btn2.getTag();
        c1.isMatched = true;
        c2.isMatched = true;

        tvMessage.setText("¡Correcto! " + c1.text + " = " + c2.text);
        lottieMascot.setAnimation(R.raw.happy_sun);
        lottieMascot.playAnimation();

        // Cambiar color a verde (éxito)
        btn1.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(this, R.color.game_success)));
        btn2.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(this, R.color.game_success)));

        selectedDetails1 = null;
        selectedButton1 = null;
        isProcessing = false;
        updateUI();

        if (pairsFound == TOTAL_PAIRS) {
            endGame(true);
        }
    }

    private void handleMismatch(MaterialButton btn1, MaterialButton btn2) {
        racha = 0;
        lives--;
        playSound(R.raw.megaman_x_error);
        vibrar(300);

        tvMessage.setText("¡No coinciden!");
        lottieMascot.setAnimation(R.raw.angry_thunderstorm);
        lottieMascot.playAnimation();

        // Color rojo temporal
        btn1.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(this, R.color.game_fail)));
        btn2.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(this, R.color.game_fail)));

        // Esperar 1 segundo y voltear de nuevo
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            if (!isFinishing()) {
                flipCard(btn1, (MemoryCard) btn1.getTag(), false);
                flipCard(btn2, (MemoryCard) btn2.getTag(), false);

                // Restaurar color original al voltear (se hace en flipCard, pero aseguramos)
                btn1.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(this, R.color.upp_primary)));
                btn2.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(this, R.color.upp_primary)));

                selectedDetails1 = null;
                selectedButton1 = null;
                isProcessing = false;
                updateUI();

                if (lives <= 0) endGame(false);
            }
        }, 1000);
    }

    private void flipCard(MaterialButton btn, MemoryCard data, boolean showFace) {
        // Animación simple de escala
        btn.animate().scaleX(0f).setDuration(150).withEndAction(() -> {
            if (showFace) {
                btn.setText(data.text);
                btn.setTextSize(14); // Texto más pequeño para que quepa
                // Color de fondo para carta abierta (ej. blanco o gris claro)
                btn.setBackgroundTintList(ColorStateList.valueOf(Color.DKGRAY));
            } else {
                btn.setText("?");
                btn.setTextSize(24);
                btn.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(this, R.color.upp_primary)));
            }
            btn.animate().scaleX(1f).setDuration(150).start();
        }).start();
    }

    private void startTimer() {
        if (timer != null) timer.cancel();
        timer = new CountDownTimer(GAME_TIME_MS, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                int sec = (int) (millisUntilFinished / 1000);
                tvMessage.setText("Tiempo: " + sec + "s");
                if (sec < 10) tvMessage.setTextColor(Color.RED);
            }

            @Override
            public void onFinish() {
                endGame(false);
            }
        }.start();
    }

    private void endGame(boolean win) {
        if (timer != null) timer.cancel();

        String title = win ? "¡Ganaste!" : "Juego Terminado";
        String msg = win ? "Puntaje final: " + score : "Te quedaste sin vidas o tiempo.";
        int icon = win ? R.drawable.ic_check_circle : R.drawable.ic_prohibiciones;

        new MaterialAlertDialogBuilder(this)
                .setTitle(title)
                .setMessage(msg)
                .setIcon(icon)
                .setCancelable(false)
                .setPositiveButton("Jugar otra vez", (d, w) -> startNewGame())
                .setNegativeButton("Salir", (d, w) -> finish())
                .show();
    }

    private void updateUI() {
        tvScore.setText("Puntos: " + score);
        tvLives.setText("Vidas: " + lives);
    }

    private void playSound(int resId) {
        MediaPlayer mp = MediaPlayer.create(this, resId);
        if (mp != null) {
            mp.start();
            mp.setOnCompletionListener(MediaPlayer::release);
        }
    }

    private void vibrar(long ms) {
        if (vibrator != null && vibrator.hasVibrator()) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator.vibrate(VibrationEffect.createOneShot(ms, VibrationEffect.DEFAULT_AMPLITUDE));
            } else {
                vibrator.vibrate(ms);
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (timer != null) timer.cancel();
    }

    // Clase auxiliar simple
    private static class MemoryCard {
        int id;
        String text;
        boolean isConcept; // true = Concepto, false = Definición
        int pairId; // Para identificar match
        boolean isMatched = false;

        public MemoryCard(int id, String text, boolean isConcept, int pairId) {
            this.id = id;
            this.text = text;
            this.isConcept = isConcept;
            this.pairId = pairId;
        }
    }
}
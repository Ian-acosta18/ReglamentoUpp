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
import android.view.ContextThemeWrapper; // Importante para evitar crash de temas
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.gridlayout.widget.GridLayout;

import com.airbnb.lottie.LottieAnimationView;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class MemoryGameActivity extends AppCompatActivity {

    // --- Configuración ---
    private static final int TOTAL_PAIRS = 6; // 12 cartas en total (3x4)
    private static final long GAME_TIME_MS = 60000; // 60 segundos

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
    private boolean isProcessing = false;
    private int score = 0;
    private int lives = 5;
    private int pairsFound = 0;
    private int racha = 0;

    // Datos
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

        // Inicializar vistas con seguridad
        glCards = findViewById(R.id.glCards);
        tvScore = findViewById(R.id.tvScore);
        tvLives = findViewById(R.id.tvLives);
        tvMessage = findViewById(R.id.tvMessage);
        lottieMascot = findViewById(R.id.lottieMascot);

        // Inicializar Vibrator de forma segura
        try {
            vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        } catch (Exception e) {
            e.printStackTrace(); // Evita crash si falla el servicio
        }

        // Configurar Toolbar (Botón de salir)
        View toolbar = findViewById(R.id.toolbarMemory);
        if (toolbar instanceof Toolbar) {
            ((Toolbar) toolbar).setNavigationOnClickListener(v -> finish());
        } else {
            toolbar.setOnClickListener(v -> finish());
        }

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

        // 1. Seleccionar pares
        List<String[]> selectedPairs = new ArrayList<>();
        List<Integer> indices = new ArrayList<>();
        for (int i = 0; i < dataPairs.length; i++) indices.add(i);
        Collections.shuffle(indices);

        // Asegurarnos de no exceder el tamaño del array
        int pairsToUse = Math.min(TOTAL_PAIRS, dataPairs.length);
        for (int i = 0; i < pairsToUse; i++) {
            selectedPairs.add(dataPairs[indices.get(i)]);
        }

        // 2. Crear cartas
        int idCounter = 0;
        for (String[] pair : selectedPairs) {
            cards.add(new MemoryCard(idCounter, pair[0], true, idCounter));
            cards.add(new MemoryCard(idCounter + 100, pair[1], false, idCounter));
            idCounter++;
        }
        Collections.shuffle(cards);

        // 3. Añadir botones al Grid
        // CORRECCIÓN: Usamos un ContextThemeWrapper para evitar crash por Tema
        Context themeContext = new ContextThemeWrapper(this, com.google.android.material.R.style.Theme_MaterialComponents_DayNight_NoActionBar);

        for (MemoryCard card : cards) {
            MaterialButton btn = new MaterialButton(themeContext);

            btn.setText("?");
            btn.setTextSize(24);
            try {
                btn.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(this, R.color.upp_primary)));
            } catch (Exception e) {
                btn.setBackgroundTintList(ColorStateList.valueOf(Color.BLUE)); // Color fallback
            }
            btn.setTextColor(Color.WHITE);
            btn.setCornerRadius(16);
            btn.setTag(card);

            // Eliminar sombra/stroke por defecto si causa problemas visuales
            btn.setStateListAnimator(null);
            btn.setInsetTop(0);
            btn.setInsetBottom(0);

            // Layout Params para grid
            GridLayout.LayoutParams params = new GridLayout.LayoutParams();
            // En GridLayout de AndroidX, usa width 0 para weights, pero asignamos tamaño fijo para asegurar visibilidad en ScrollView
            params.width = 0;
            params.height = 250;
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

        if (cardData.isMatched || cardData == selectedDetails1) return;

        flipCard(btn, cardData, true);

        if (selectedDetails1 == null) {
            selectedDetails1 = cardData;
            selectedButton1 = btn;
        } else {
            isProcessing = true;
            checkMatch(btn, cardData);
        }
    }

    private void checkMatch(MaterialButton btn2, MemoryCard cardData2) {
        if (selectedDetails1.pairId == cardData2.pairId) {
            handleMatch(selectedButton1, btn2);
        } else {
            handleMismatch(selectedButton1, btn2);
        }
    }

    private void handleMatch(MaterialButton btn1, MaterialButton btn2) {
        racha++;
        score += (10 + (racha * 2));
        pairsFound++;

        playSound(R.raw.correct_ding);
        vibrarSafe(50); // Usar método seguro

        MemoryCard c1 = (MemoryCard) btn1.getTag();
        MemoryCard c2 = (MemoryCard) btn2.getTag();
        c1.isMatched = true;
        c2.isMatched = true;

        tvMessage.setText("¡Correcto!");
        if (lottieMascot != null) {
            lottieMascot.setAnimation(R.raw.happy_sun);
            lottieMascot.playAnimation();
        }

        int colorVerde = ContextCompat.getColor(this, R.color.game_success);
        btn1.setBackgroundTintList(ColorStateList.valueOf(colorVerde));
        btn2.setBackgroundTintList(ColorStateList.valueOf(colorVerde));

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
        vibrarSafe(300); // Usar método seguro

        tvMessage.setText("¡No coinciden!");
        if (lottieMascot != null) {
            lottieMascot.setAnimation(R.raw.angry_thunderstorm);
            lottieMascot.playAnimation();
        }

        int colorRojo = ContextCompat.getColor(this, R.color.game_fail);
        btn1.setBackgroundTintList(ColorStateList.valueOf(colorRojo));
        btn2.setBackgroundTintList(ColorStateList.valueOf(colorRojo));

        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            if (!isFinishing()) {
                flipCard(btn1, (MemoryCard) btn1.getTag(), false);
                flipCard(btn2, (MemoryCard) btn2.getTag(), false);

                // Restaurar color original
                int colorOriginal = ContextCompat.getColor(this, R.color.upp_primary);
                btn1.setBackgroundTintList(ColorStateList.valueOf(colorOriginal));
                btn2.setBackgroundTintList(ColorStateList.valueOf(colorOriginal));

                selectedDetails1 = null;
                selectedButton1 = null;
                isProcessing = false;
                updateUI();

                if (lives <= 0) endGame(false);
            }
        }, 1000);
    }

    private void flipCard(MaterialButton btn, MemoryCard data, boolean showFace) {
        btn.animate().scaleX(0f).setDuration(150).withEndAction(() -> {
            if (showFace) {
                btn.setText(data.text);
                btn.setTextSize(14);
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

        try {
            new MaterialAlertDialogBuilder(this)
                    .setTitle(title)
                    .setMessage(msg)
                    .setIcon(icon)
                    .setCancelable(false)
                    .setPositiveButton("Jugar otra vez", (d, w) -> startNewGame())
                    .setNegativeButton("Salir", (d, w) -> finish())
                    .show();
        } catch (Exception e) {
            // Fallback si falla el MaterialAlertDialog
            Toast.makeText(this, msg, Toast.LENGTH_LONG).show();
            finish();
        }
    }

    private void updateUI() {
        tvScore.setText("Puntos: " + score);
        tvLives.setText("Vidas: " + lives);
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

    // MÉTODO SEGURO DE VIBRACIÓN
    private void vibrarSafe(long ms) {
        try {
            if (vibrator != null && vibrator.hasVibrator()) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    vibrator.vibrate(VibrationEffect.createOneShot(ms, VibrationEffect.DEFAULT_AMPLITUDE));
                } else {
                    vibrator.vibrate(ms);
                }
            }
        } catch (SecurityException e) {
            // Ignorar error si falta el permiso, para que no cierre la app
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (timer != null) timer.cancel();
    }

    private static class MemoryCard {
        int id;
        String text;
        boolean isConcept;
        int pairId;
        boolean isMatched = false;

        public MemoryCard(int id, String text, boolean isConcept, int pairId) {
            this.id = id;
            this.text = text;
            this.isConcept = isConcept;
            this.pairId = pairId;
        }
    }
}
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
import android.util.TypedValue;
import android.view.ContextThemeWrapper;
import android.view.Gravity;
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
    private static final int TOTAL_PAIRS = 8; // ¡Ahora son 8 pares! (16 cartas)
    private static final long GAME_TIME_MS = 90000; // 90 seg (más tiempo por más cartas)

    // UI
    private GridLayout glCards;
    private TextView tvScore, tvLives, tvMessage;
    private LottieAnimationView lottieMascot;
    private CountDownTimer timer;
    private Vibrator vibrator;

    // Estado
    private List<MemoryCard> cards;
    private MemoryCard selectedDetails1 = null;
    private MaterialButton selectedButton1 = null;
    private boolean isProcessing = false;
    private int score = 0;
    private int lives = 6; // Una vida extra
    private int pairsFound = 0;
    private int racha = 0;

    // --- DEFINICIÓN DE LAS 8 CARTAS (Todos tus iconos) ---
    private final CardDefinition[] definitions = {
            new CardDefinition(R.drawable.ic_derechos, "Derechos"),
            new CardDefinition(R.drawable.ic_obligaciones, "Obligaciones"),
            new CardDefinition(R.drawable.ic_prohibiciones, "Prohibido"),
            new CardDefinition(R.drawable.ic_sanciones, "Sanciones"),
            new CardDefinition(R.drawable.ic_reconocimientos, "Méritos"),
            new CardDefinition(R.drawable.ic_game_trivia, "Evaluación"),
            new CardDefinition(R.drawable.ic_game_vf, "Verdad/Falso"),
            new CardDefinition(R.drawable.ic_game_hangman, "Ahorcado")
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_memory_game);

        glCards = findViewById(R.id.glCards);
        tvScore = findViewById(R.id.tvScore);
        tvLives = findViewById(R.id.tvLives);
        tvMessage = findViewById(R.id.tvMessage);
        lottieMascot = findViewById(R.id.lottieMascot);

        try {
            vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        } catch (Exception e) { e.printStackTrace(); }

        View toolbar = findViewById(R.id.toolbarMemory);
        if (toolbar instanceof Toolbar) ((Toolbar) toolbar).setNavigationOnClickListener(v -> finish());
        else toolbar.setOnClickListener(v -> finish());

        startNewGame();
    }

    private void startNewGame() {
        score = 0;
        lives = 6;
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
        glCards.setColumnCount(4); // Asegurar 4 columnas por código también
        cards = new ArrayList<>();

        // Usamos TODOS los iconos definidos
        List<CardDefinition> selectedDefs = new ArrayList<>();
        for (CardDefinition def : definitions) {
            selectedDefs.add(def);
        }

        // Crear pares
        int idCounter = 0;
        for (CardDefinition def : selectedDefs) {
            cards.add(new MemoryCard(idCounter++, def.iconRes, def.textLabel));
            cards.add(new MemoryCard(idCounter++, def.iconRes, def.textLabel));
        }
        Collections.shuffle(cards);

        Context themeContext = new ContextThemeWrapper(this, com.google.android.material.R.style.Theme_MaterialComponents_DayNight_NoActionBar);

        for (MemoryCard card : cards) {
            MaterialButton btn = new MaterialButton(themeContext);

            // --- DISEÑO INICIAL ---
            btn.setText("?");
            btn.setTextSize(TypedValue.COMPLEX_UNIT_SP, 22);
            btn.setIcon(null);

            try {
                btn.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(this, R.color.upp_primary)));
            } catch (Exception e) {
                btn.setBackgroundTintList(ColorStateList.valueOf(Color.BLUE));
            }
            btn.setTextColor(Color.WHITE);
            btn.setCornerRadius(dpToPx(12));
            btn.setInsetTop(0);
            btn.setInsetBottom(0);
            btn.setPadding(0,0,0,0);
            btn.setElevation(dpToPx(2));
            btn.setGravity(Gravity.CENTER);

            btn.setTag(card);

            // --- TAMAÑO PARA 4 COLUMNAS ---
            GridLayout.LayoutParams params = new GridLayout.LayoutParams();
            params.width = 0;
            params.height = dpToPx(100); // Altura compacta pero suficiente
            params.columnSpec = GridLayout.spec(GridLayout.UNDEFINED, 1f);
            params.rowSpec = GridLayout.spec(GridLayout.UNDEFINED, 1f);
            params.setMargins(dpToPx(4), dpToPx(4), dpToPx(4), dpToPx(4)); // Margen pequeño
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
        if (selectedDetails1.textLabel.equals(cardData2.textLabel)) {
            handleMatch(selectedButton1, btn2);
        } else {
            handleMismatch(selectedButton1, btn2);
        }
    }

    private void handleMatch(MaterialButton btn1, MaterialButton btn2) {
        racha++;
        score += (15 + (racha * 5)); // Más puntos!
        pairsFound++;

        playSound(R.raw.correct_ding);
        vibrarSafe(50);

        MemoryCard c1 = (MemoryCard) btn1.getTag();
        MemoryCard c2 = (MemoryCard) btn2.getTag();
        c1.isMatched = true;
        c2.isMatched = true;

        tvMessage.setText("¡" + c1.textLabel + " Encontrado!");
        if (lottieMascot != null) {
            lottieMascot.setAnimation(R.raw.happy_sun);
            lottieMascot.playAnimation();
        }

        int colorVerde = ContextCompat.getColor(this, R.color.game_success);
        btn1.setStrokeColor(ColorStateList.valueOf(colorVerde));
        btn1.setStrokeWidth(dpToPx(3));
        btn2.setStrokeColor(ColorStateList.valueOf(colorVerde));
        btn2.setStrokeWidth(dpToPx(3));

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
        vibrarSafe(200);

        tvMessage.setText("¡Intenta de nuevo!");
        if (lottieMascot != null) {
            lottieMascot.setAnimation(R.raw.angry_thunderstorm);
            lottieMascot.playAnimation();
        }

        int colorRojo = ContextCompat.getColor(this, R.color.game_fail);
        btn1.setStrokeColor(ColorStateList.valueOf(colorRojo));
        btn1.setStrokeWidth(dpToPx(3));
        btn2.setStrokeColor(ColorStateList.valueOf(colorRojo));
        btn2.setStrokeWidth(dpToPx(3));

        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            if (!isFinishing()) {
                flipCard(btn1, (MemoryCard) btn1.getTag(), false);
                flipCard(btn2, (MemoryCard) btn2.getTag(), false);
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
                // --- CARA VISIBLE ---
                btn.setText(data.textLabel);
                btn.setTextSize(TypedValue.COMPLEX_UNIT_SP, 10); // Texto ajustado a la celda
                btn.setSupportAllCaps(false);
                btn.setTextColor(ContextCompat.getColor(this, R.color.upp_primary));
                btn.setGravity(Gravity.CENTER | Gravity.BOTTOM);

                btn.setIconResource(data.iconResId);
                btn.setIconSize(dpToPx(45)); // Icono grande para la celda
                btn.setIconTint(null);
                btn.setIconGravity(MaterialButton.ICON_GRAVITY_TOP);
                btn.setIconPadding(dpToPx(2));

                btn.setBackgroundTintList(ColorStateList.valueOf(Color.WHITE));
                btn.setStrokeWidth(dpToPx(1));
                btn.setStrokeColor(ColorStateList.valueOf(Color.LTGRAY));
            } else {
                // --- DORSO ---
                btn.setText("?");
                btn.setTextSize(TypedValue.COMPLEX_UNIT_SP, 22);
                btn.setTextColor(Color.WHITE);
                btn.setGravity(Gravity.CENTER);
                btn.setIcon(null);

                btn.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(this, R.color.upp_primary)));
                btn.setStrokeWidth(0);
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
                else tvMessage.setTextColor(ContextCompat.getColor(MemoryGameActivity.this, R.color.upp_primary));
            }
            @Override
            public void onFinish() { endGame(false); }
        }.start();
    }

    private void endGame(boolean win) {
        if (timer != null) timer.cancel();
        String title = win ? "¡Felicidades!" : "Juego Terminado";
        String msg = win ? "¡Encontraste los 8 pares! Puntaje: " + score : "Se acabaron las vidas o el tiempo.";
        int icon = win ? R.drawable.ic_check_circle : R.drawable.ic_prohibiciones;

        try {
            new MaterialAlertDialogBuilder(this)
                    .setTitle(title)
                    .setMessage(msg)
                    .setIcon(icon)
                    .setCancelable(false)
                    .setPositiveButton("Reintentar", (d, w) -> startNewGame())
                    .setNegativeButton("Salir", (d, w) -> finish())
                    .show();
        } catch (Exception e) { finish(); }
    }

    private void updateUI() {
        tvScore.setText("Puntos: " + score);
        tvLives.setText("Vidas: " + lives);
    }

    private void playSound(int resId) {
        try {
            MediaPlayer mp = MediaPlayer.create(this, resId);
            if (mp != null) { mp.start(); mp.setOnCompletionListener(MediaPlayer::release); }
        } catch (Exception e) { e.printStackTrace(); }
    }

    private void vibrarSafe(long ms) {
        try {
            if (vibrator != null && vibrator.hasVibrator()) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) vibrator.vibrate(VibrationEffect.createOneShot(ms, VibrationEffect.DEFAULT_AMPLITUDE));
                else vibrator.vibrate(ms);
            }
        } catch (Exception e) {}
    }

    private int dpToPx(int dp) {
        return Math.round(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, getResources().getDisplayMetrics()));
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (timer != null) timer.cancel();
    }

    private static class CardDefinition {
        int iconRes;
        String textLabel;
        public CardDefinition(int iconRes, String textLabel) {
            this.iconRes = iconRes;
            this.textLabel = textLabel;
        }
    }

    private static class MemoryCard {
        int id;
        int iconResId;
        String textLabel;
        boolean isMatched = false;
        public MemoryCard(int id, int iconResId, String textLabel) {
            this.id = id;
            this.iconResId = iconResId;
            this.textLabel = textLabel;
        }
    }
}
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
    private static final int TOTAL_PAIRS = 6; // 12 cartas en total
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

    // --- DEFINICIÓN DE CARTAS (Icono + Texto) ---
    private final CardDefinition[] definitions = {
            new CardDefinition(R.drawable.ic_derechos, "Derechos"),
            new CardDefinition(R.drawable.ic_obligaciones, "Obligaciones"),
            new CardDefinition(R.drawable.ic_prohibiciones, "Prohibiciones"),
            new CardDefinition(R.drawable.ic_sanciones, "Sanciones"),
            new CardDefinition(R.drawable.ic_reconocimientos, "Méritos"), // Texto corto para que quepa
            new CardDefinition(R.drawable.ic_game_trivia, "Evaluación")
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

        try {
            vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Configurar Toolbar
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

        // 1. Preparar lista de definiciones para usar
        List<CardDefinition> selectedDefs = new ArrayList<>();
        List<Integer> indices = new ArrayList<>();

        // Asegurar que no pedimos más pares de los que tenemos definidos
        int pairsToUse = Math.min(TOTAL_PAIRS, definitions.length);

        for (int i = 0; i < definitions.length; i++) indices.add(i);
        Collections.shuffle(indices);

        for (int i = 0; i < pairsToUse; i++) {
            selectedDefs.add(definitions[indices.get(i)]);
        }

        // 2. Crear las cartas (duplicando para hacer pares)
        int idCounter = 0;
        for (CardDefinition def : selectedDefs) {
            // Carta A
            cards.add(new MemoryCard(idCounter++, def.iconRes, def.textLabel));
            // Carta B (Par idéntico)
            cards.add(new MemoryCard(idCounter++, def.iconRes, def.textLabel));
        }
        Collections.shuffle(cards);

        // 3. Añadir botones al Grid
        Context themeContext = new ContextThemeWrapper(this, com.google.android.material.R.style.Theme_MaterialComponents_DayNight_NoActionBar);

        for (MemoryCard card : cards) {
            MaterialButton btn = new MaterialButton(themeContext);

            // --- Estado Inicial (Boca abajo) ---
            btn.setText("?");
            btn.setTextSize(TypedValue.COMPLEX_UNIT_SP, 24);
            btn.setIcon(null);

            // Colores iniciales
            try {
                btn.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(this, R.color.upp_primary)));
            } catch (Exception e) {
                btn.setBackgroundTintList(ColorStateList.valueOf(Color.BLUE));
            }
            btn.setTextColor(Color.WHITE);

            // Estilo y Layout
            btn.setCornerRadius(16);
            btn.setInsetTop(0);
            btn.setInsetBottom(0);
            btn.setPadding(0,0,0,0);

            btn.setTag(card);

            GridLayout.LayoutParams params = new GridLayout.LayoutParams();
            params.width = 0;
            params.height = dpToPx(110); // Altura suficiente para Icono + Texto
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
        // Coincidencia si el texto (y por ende el icono) es el mismo
        if (selectedDetails1.textLabel.equals(cardData2.textLabel)) {
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
        vibrarSafe(50);

        MemoryCard c1 = (MemoryCard) btn1.getTag();
        MemoryCard c2 = (MemoryCard) btn2.getTag();
        c1.isMatched = true;
        c2.isMatched = true;

        tvMessage.setText("¡" + c1.textLabel + " Correcto!");
        if (lottieMascot != null) {
            lottieMascot.setAnimation(R.raw.happy_sun);
            lottieMascot.playAnimation();
        }

        // Pintar borde o fondo verde suave para indicar éxito
        int colorVerde = ContextCompat.getColor(this, R.color.game_success);
        btn1.setStrokeColor(ColorStateList.valueOf(colorVerde));
        btn1.setStrokeWidth(dpToPx(3));
        btn2.setStrokeColor(ColorStateList.valueOf(colorVerde));
        btn2.setStrokeWidth(dpToPx(3));

        selectedDetails1 = null;
        selectedButton1 = null;
        isProcessing = false;
        updateUI();

        if (pairsFound >= Math.min(TOTAL_PAIRS, definitions.length)) {
            endGame(true);
        }
    }

    private void handleMismatch(MaterialButton btn1, MaterialButton btn2) {
        racha = 0;
        lives--;
        playSound(R.raw.megaman_x_error);
        vibrarSafe(300);

        tvMessage.setText("¡No son iguales!");
        if (lottieMascot != null) {
            lottieMascot.setAnimation(R.raw.angry_thunderstorm);
            lottieMascot.playAnimation();
        }

        // Color rojo de error temporal
        int colorRojo = ContextCompat.getColor(this, R.color.game_fail);
        btn1.setBackgroundTintList(ColorStateList.valueOf(colorRojo));
        btn1.setTextColor(Color.WHITE); // Texto blanco para leer sobre rojo

        btn2.setBackgroundTintList(ColorStateList.valueOf(colorRojo));
        btn2.setTextColor(Color.WHITE);

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
                // --- MOSTRAR CARA (ICONO + TEXTO) ---
                btn.setText(data.textLabel);
                btn.setTextSize(TypedValue.COMPLEX_UNIT_SP, 11); // Texto pequeño
                btn.setTextColor(ContextCompat.getColor(this, R.color.upp_primary)); // Texto oscuro

                // Configurar Icono
                btn.setIconResource(data.iconResId);
                btn.setIconSize(dpToPx(40)); // Tamaño del icono
                btn.setIconTint(null); // Colores originales
                btn.setIconGravity(MaterialButton.ICON_GRAVITY_TOP); // Icono arriba, texto abajo

                // Fondo Blanco
                btn.setBackgroundTintList(ColorStateList.valueOf(Color.WHITE));
                btn.setStrokeWidth(dpToPx(1));
                btn.setStrokeColor(ColorStateList.valueOf(Color.LTGRAY));

            } else {
                // --- MOSTRAR DORSO (?) ---
                btn.setText("?");
                btn.setTextSize(TypedValue.COMPLEX_UNIT_SP, 24);
                btn.setTextColor(Color.WHITE);
                btn.setIcon(null); // Quitar icono

                // Restaurar color primario
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
            public void onFinish() {
                endGame(false);
            }
        }.start();
    }

    private void endGame(boolean win) {
        if (timer != null) timer.cancel();

        String title = win ? "¡Felicidades!" : "Juego Terminado";
        String msg = win ? "Completaste el nivel. Puntaje: " + score : "Se acabaron las vidas o el tiempo.";
        int icon = win ? R.drawable.ic_check_circle : R.drawable.ic_prohibiciones;

        try {
            new MaterialAlertDialogBuilder(this)
                    .setTitle(title)
                    .setMessage(msg)
                    .setIcon(icon)
                    .setCancelable(false)
                    .setPositiveButton("Jugar de nuevo", (d, w) -> startNewGame())
                    .setNegativeButton("Salir", (d, w) -> finish())
                    .show();
        } catch (Exception e) {
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

    private void vibrarSafe(long ms) {
        try {
            if (vibrator != null && vibrator.hasVibrator()) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    vibrator.vibrate(VibrationEffect.createOneShot(ms, VibrationEffect.DEFAULT_AMPLITUDE));
                } else {
                    vibrator.vibrate(ms);
                }
            }
        } catch (Exception e) {
            // Ignorar error
        }
    }

    private int dpToPx(int dp) {
        return Math.round(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, getResources().getDisplayMetrics()));
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (timer != null) timer.cancel();
    }

    // --- CLASES AUXILIARES ---

    // Definición del contenido (Estática)
    private static class CardDefinition {
        int iconRes;
        String textLabel;
        public CardDefinition(int iconRes, String textLabel) {
            this.iconRes = iconRes;
            this.textLabel = textLabel;
        }
    }

    // Instancia de carta en juego
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
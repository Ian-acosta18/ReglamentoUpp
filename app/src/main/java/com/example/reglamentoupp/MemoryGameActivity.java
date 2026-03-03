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
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;

import com.airbnb.lottie.LottieAnimationView;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class MemoryGameActivity extends AppCompatActivity {

    // --- Configuración ---
    private static final int TOTAL_PAIRS = 8;
    private static final long GAME_TIME_MS = 120000; // Aumentado a 2 mins por la lectura

    // UI
    private GridLayout glCards;
    private TextView tvScore, tvLives, tvMessage;
    private LottieAnimationView lottieMascot;
    private CountDownTimer timer;
    private Vibrator vibrator;

    // Estado
    private List<MemoryCard> cards;
    private MemoryCard selectedDetails1 = null;
    private View selectedView1 = null;
    private boolean isProcessing = false;
    private int score = 0;
    private int lives = 6;
    private int pairsFound = 0;
    private int racha = 0;

    // --- DEFINICIÓN DE LAS CARTAS (REGLAS Y ARTÍCULOS) ---
    // Cada definición crea un par: Texto A (Artículo) y Texto B (Descripción)
    private final CardDefinition[] definitions = {
            // --- DERECHOS (Icono: ic_derechos) ---
            new CardDefinition(R.drawable.ic_derechos, "Art. 3 (I)", "Cursar los estudios según planes vigentes."),
            new CardDefinition(R.drawable.ic_derechos, "Art. 3 (III)", "Recibir orientación académica."),
            new CardDefinition(R.drawable.ic_derechos, "Art. 3 (VII)", "Conocer resultados de evaluaciones."),
            new CardDefinition(R.drawable.ic_derechos, "Art. 3 (VIII)", "Obtener credencial al inscribirse."),

            // --- OBLIGACIONES (Icono: ic_obligaciones) ---
            new CardDefinition(R.drawable.ic_obligaciones, "Art. 5 (I)", "Ser responsables de su formación."),
            new CardDefinition(R.drawable.ic_obligaciones, "Art. 5 (V)", "Asistir puntualmente a clases."),
            new CardDefinition(R.drawable.ic_obligaciones, "Art. 5 (X)", "Cuidar espacios y materiales."),
            new CardDefinition(R.drawable.ic_obligaciones, "Art. 5 (XII)", "Mostrar credencial al ingresar.")
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
        selectedView1 = null;

        updateUI();
        setupBoard();
        startTimer();
    }

    private void setupBoard() {
        glCards.removeAllViews();
        glCards.setColumnCount(4);
        cards = new ArrayList<>();

        int idCounter = 0;
        int matchIdCounter = 0;

        // Crear pares con ID de coincidencia único
        for (CardDefinition def : definitions) {
            // Carta 1: El Título (Ej. Art 3)
            cards.add(new MemoryCard(idCounter++, def.iconRes, def.textTitle, matchIdCounter));
            // Carta 2: La Descripción (Ej. Cursar estudios...)
            cards.add(new MemoryCard(idCounter++, def.iconRes, def.textDesc, matchIdCounter));

            matchIdCounter++;
        }

        Collections.shuffle(cards);

        LayoutInflater inflater = LayoutInflater.from(this);

        for (MemoryCard card : cards) {
            View cardView = inflater.inflate(R.layout.item_memory_card, glCards, false);

            TextView tvText = cardView.findViewById(R.id.tv_card_text);
            ImageView ivIcon = cardView.findViewById(R.id.iv_card_icon);

            if (tvText != null) tvText.setText(card.textLabel);
            if (ivIcon != null) ivIcon.setImageResource(card.iconResId);

            GridLayout.LayoutParams params = new GridLayout.LayoutParams();
            params.width = 0;
            // ALTURA MODIFICADA: Aumentada a 130dp para dar espacio al icono más grande y al texto
            params.height = dpToPx(130);
            params.columnSpec = GridLayout.spec(GridLayout.UNDEFINED, 1f);
            params.rowSpec = GridLayout.spec(GridLayout.UNDEFINED, 1f);
            params.setMargins(dpToPx(4), dpToPx(4), dpToPx(4), dpToPx(4));

            cardView.setLayoutParams(params);
            cardView.setTag(card);

            cardView.setOnClickListener(v -> onCardClick(cardView));

            glCards.addView(cardView);
        }
    }

    private void onCardClick(View view) {
        if (isProcessing) return;
        MemoryCard cardData = (MemoryCard) view.getTag();

        if (cardData.isMatched || cardData == selectedDetails1) return;

        flipCard(view, true);

        if (selectedDetails1 == null) {
            selectedDetails1 = cardData;
            selectedView1 = view;
        } else {
            isProcessing = true;
            checkMatch(view, cardData);
        }
    }

    private void checkMatch(View view2, MemoryCard cardData2) {
        // AHORA COMPARAMOS EL matchId, NO EL TEXTO
        if (selectedDetails1.matchId == cardData2.matchId) {
            handleMatch(selectedView1, view2);
        } else {
            handleMismatch(selectedView1, view2);
        }
    }

    private void handleMatch(View view1, View view2) {
        racha++;
        score += (15 + (racha * 5));
        pairsFound++;

        playSound(R.raw.correct_ding);
        vibrarSafe(50);

        MemoryCard c1 = (MemoryCard) view1.getTag();
        MemoryCard c2 = (MemoryCard) view2.getTag();
        c1.isMatched = true;
        c2.isMatched = true;

        tvMessage.setText("¡Correcto!");
        if (lottieMascot != null) {
            lottieMascot.setAnimation(R.raw.buho_1);
            lottieMascot.playAnimation();
        }

        setCardStroke(view1, R.color.game_success, 3);
        setCardStroke(view2, R.color.game_success, 3);

        selectedDetails1 = null;
        selectedView1 = null;
        isProcessing = false;
        updateUI();

        if (pairsFound == TOTAL_PAIRS) {
            endGame(true);
        }
    }

    private void handleMismatch(View view1, View view2) {
        racha = 0;
        lives--;
        playSound(R.raw.megaman_x_error);
        vibrarSafe(200);

        tvMessage.setText("¡Intenta de nuevo!");
        if (lottieMascot != null) {
            lottieMascot.setAnimation(R.raw.angry_thunderstorm);
            lottieMascot.playAnimation();
        }

        setCardStroke(view1, R.color.game_fail, 3);
        setCardStroke(view2, R.color.game_fail, 3);

        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            if (!isFinishing()) {
                flipCard(view1, false);
                flipCard(view2, false);

                setCardStroke(view1, android.R.color.transparent, 0);
                setCardStroke(view2, android.R.color.transparent, 0);

                selectedDetails1 = null;
                selectedView1 = null;
                isProcessing = false;
                updateUI();
                if (lives <= 0) endGame(false);
            }
        }, 1200);
    }

    private void setCardStroke(View view, int colorRes, int widthDp) {
        if (view instanceof MaterialCardView) {
            MaterialCardView card = (MaterialCardView) view;
            if (widthDp > 0) {
                card.setStrokeColor(ContextCompat.getColor(this, colorRes));
                card.setStrokeWidth(dpToPx(widthDp));
            } else {
                card.setStrokeWidth(dpToPx(1));
                card.setStrokeColor(Color.parseColor("#E0E0E0"));
            }
        }
    }

    private void flipCard(View view, boolean showFace) {
        final View front = view.findViewById(R.id.layout_front);
        final View back = view.findViewById(R.id.layout_back);

        view.animate().scaleX(0f).setDuration(150).withEndAction(() -> {
            if (showFace) {
                if(front != null) front.setVisibility(View.VISIBLE);
                if(back != null) back.setVisibility(View.GONE);
            } else {
                if(front != null) front.setVisibility(View.GONE);
                if(back != null) back.setVisibility(View.VISIBLE);
            }
            view.animate().scaleX(1f).setDuration(150).start();
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
        String textTitle;
        String textDesc;

        public CardDefinition(int iconRes, String textTitle, String textDesc) {
            this.iconRes = iconRes;
            this.textTitle = textTitle;
            this.textDesc = textDesc;
        }
    }

    private static class MemoryCard {
        int id;
        int iconResId;
        String textLabel;
        int matchId;
        boolean isMatched = false;

        public MemoryCard(int id, int iconResId, String textLabel, int matchId) {
            this.id = id;
            this.iconResId = iconResId;
            this.textLabel = textLabel;
            this.matchId = matchId;
        }
    }
}
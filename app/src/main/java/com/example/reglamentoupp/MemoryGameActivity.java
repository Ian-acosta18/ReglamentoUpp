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

    // MODIFICADO: 4 pares (8 cartas) + 1 Comodín = 9 cartas (Grid 3x3)
    private static final int TOTAL_PAIRS = 4;
    private static final long GAME_TIME_MS = 120000;

    private GridLayout glCards;
    private TextView tvScore, tvLives, tvMessage;
    private LottieAnimationView lottieMascot;
    private CountDownTimer timer;
    private Vibrator vibrator;

    private List<MemoryCard> cards;
    private MemoryCard selectedDetails1 = null;
    private View selectedView1 = null;
    private boolean isProcessing = false;
    private int score = 0;
    private int lives = 6;
    private int pairsFound = 0;
    private int racha = 0;

    private final CardDefinition[] definitions = {
            new CardDefinition(R.drawable.ic_derechos, "Art. 3 (I)", "Cursar los estudios según planes vigentes."),
            new CardDefinition(R.drawable.ic_derechos, "Art. 3 (III)", "Recibir orientación académica."),
            new CardDefinition(R.drawable.ic_derechos, "Art. 3 (VII)", "Conocer resultados de evaluaciones."),
            new CardDefinition(R.drawable.ic_derechos, "Art. 3 (VIII)", "Obtener credencial al inscribirse."),
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
        glCards.setColumnCount(3);
        glCards.setRowCount(3); // Forzar 3 filas
        cards = new ArrayList<>();

        int idCounter = 0;
        int matchIdCounter = 0;

        // 1. Agregar los 4 pares (8 cartas en total)
        for (int i = 0; i < TOTAL_PAIRS; i++) {
            CardDefinition def = definitions[i];
            cards.add(new MemoryCard(idCounter++, def.iconRes, def.textTitle, matchIdCounter));
            cards.add(new MemoryCard(idCounter++, def.iconRes, def.textDesc, matchIdCounter));
            matchIdCounter++;
        }

        // 2. Agregar la Carta Impar (Comodín)
        // Le damos un matchId de -1 para identificarla fácilmente
        cards.add(new MemoryCard(idCounter++, R.drawable.mi_logo, "¡Comodín!\n+20 Pts", -1));

        // 3. Mezclar las 9 cartas
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
            // Hacemos las cartas un poquito más altas para que llenen la pantalla en el 3x3
            params.height = dpToPx(150);
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

        // MODIFICADO: Lógica para la carta comodín impar
        if (cardData.matchId == -1) {
            cardData.isMatched = true; // Se queda boca arriba
            score += 20; // Regala puntos
            tvMessage.setText("¡Carta Comodín! +20 Puntos");
            playSound(R.raw.correct_ding);
            vibrarSafe(100);
            updateUI();

            // Le ponemos un borde amarillo/dorado para destacarla
            if (view instanceof MaterialCardView) {
                ((MaterialCardView) view).setStrokeColor(Color.parseColor("#FFD700"));
                ((MaterialCardView) view).setStrokeWidth(dpToPx(3));
            }
            return; // Detenemos aquí para que no interfiera si ya había una carta normal volteada
        }

        if (selectedDetails1 == null) {
            selectedDetails1 = cardData;
            selectedView1 = view;
        } else {
            isProcessing = true;
            checkMatch(view, cardData);
        }
    }

    private void checkMatch(View view2, MemoryCard cardData2) {
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
            lottieMascot.setAnimation(R.raw.happy_sun);
            lottieMascot.playAnimation();
        }

        setCardStroke(view1, "#4CAF50", 3); // Verde éxito
        setCardStroke(view2, "#4CAF50", 3); // Verde éxito

        selectedDetails1 = null;
        selectedView1 = null;
        isProcessing = false;
        updateUI();

        // Si encontró los 4 pares, gana el juego
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

        setCardStroke(view1, "#F44336", 3); // Rojo error
        setCardStroke(view2, "#F44336", 3); // Rojo error

        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            if (!isFinishing()) {
                flipCard(view1, false);
                flipCard(view2, false);

                setCardStroke(view1, "#D3DCE6", 0); // Regresa a borde default
                setCardStroke(view2, "#D3DCE6", 0);

                selectedDetails1 = null;
                selectedView1 = null;
                isProcessing = false;
                updateUI();
                if (lives <= 0) endGame(false);
            }
        }, 1200);
    }

    private void setCardStroke(View view, String hexColor, int widthDp) {
        if (view instanceof MaterialCardView) {
            MaterialCardView card = (MaterialCardView) view;
            if (widthDp > 0) {
                card.setStrokeColor(Color.parseColor(hexColor));
                card.setStrokeWidth(dpToPx(widthDp));
            } else {
                card.setStrokeWidth(dpToPx(1.5f));
                card.setStrokeColor(Color.parseColor(hexColor));
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
        String msg = win ? "¡Encontraste los pares! Puntaje: " + score : "Se acabaron las vidas o el tiempo.";
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

    private int dpToPx(float dp) {
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
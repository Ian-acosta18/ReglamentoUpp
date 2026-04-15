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
import android.widget.Toast;

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

    private static final int TOTAL_PAIRS = 4;
    private static final long GAME_TIME_MS = 120000;

    private GridLayout glCards;
    private TextView tvScore, tvLives, tvMessage;
    private LottieAnimationView lottieMascot;
    private CountDownTimer timer;
    private Vibrator vibrator;
    private MediaPlayer mediaPlayer;

    private List<MemoryCard> cards;
    private MemoryCard selectedDetails1 = null;
    private View selectedView1 = null;
    private boolean isProcessing = false;
    private int score = 0;
    private int lives = 6;
    private int pairsFound = 0;
    private int racha = 0;
    private int pistasRestantes = 1;

    private final CardDefinition[] definitions = {
            new CardDefinition(R.drawable.ic_derechos, "Art. 3 (I)", "Cursar los estudios según planes vigentes.", "Ejemplo: Inscribirte a las materias de tu plan actual."),
            new CardDefinition(R.drawable.ic_derechos, "Art. 3 (III)", "Recibir orientación académica.", "Ejemplo: Pedir tutorías si tienes dudas en una materia."),
            new CardDefinition(R.drawable.ic_derechos, "Art. 3 (VII)", "Conocer resultados de evaluaciones.", "Ejemplo: Revisar tus calificaciones en el sistema."),
            new CardDefinition(R.drawable.ic_derechos, "Art. 3 (VIII)", "Obtener credencial al inscribirse.", "Ejemplo: Tramitar tu credencial en Servicios Escolares."),
            new CardDefinition(R.drawable.ic_obligaciones, "Art. 5 (I)", "Ser responsables de su formación.", "Ejemplo: Entregar tareas a tiempo y estudiar para exámenes."),
            new CardDefinition(R.drawable.ic_obligaciones, "Art. 5 (V)", "Asistir puntualmente a clases.", "Ejemplo: Llegar antes de que termine la tolerancia del profesor.")
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

        try { vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE); } catch (Exception e) {}

        View toolbar = findViewById(R.id.toolbarMemory);
        if (toolbar instanceof Toolbar) ((Toolbar) toolbar).setNavigationOnClickListener(v -> finish());
        else toolbar.setOnClickListener(v -> finish());

        findViewById(R.id.btnHelpMemory).setOnClickListener(v -> usarPista());

        startNewGame();
    }

    private void usarPista() {
        if (isProcessing || pistasRestantes <= 0) {
            Toast.makeText(this, pistasRestantes <= 0 ? "Ya usaste el vistazo rápido" : "Espera un momento", Toast.LENGTH_SHORT).show();
            return;
        }
        pistasRestantes--;
        isProcessing = true;
        tvMessage.setText("¡Pista! Vistazo rápido...");

        List<View> cartasVolteadas = new ArrayList<>();
        for (int i = 0; i < glCards.getChildCount(); i++) {
            View cardView = glCards.getChildAt(i);
            MemoryCard cardData = (MemoryCard) cardView.getTag();
            if (!cardData.isMatched && cardData != selectedDetails1) {
                flipCard(cardView, true);
                cartasVolteadas.add(cardView);
            }
        }

        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            if (!isFinishing() && !isDestroyed()) {
                for (View cardView : cartasVolteadas) flipCard(cardView, false);
                isProcessing = false;
                tvMessage.setText("¡Sigue buscando!");
            }
        }, 1500);
    }

    private void startNewGame() {
        score = 0;
        lives = 6;
        pairsFound = 0;
        racha = 0;
        pistasRestantes = 1;
        isProcessing = false;
        selectedDetails1 = null;
        selectedView1 = null;

        if (lottieMascot != null) {
            lottieMascot.setAnimation(R.raw.owlie_nodding);
            lottieMascot.playAnimation();
        }

        updateUI();
        setupBoard();
        startTimer();
    }

    private void setupBoard() {
        glCards.removeAllViews();
        glCards.setColumnCount(3);
        glCards.setRowCount(3);
        cards = new ArrayList<>();

        int idCounter = 0;
        int matchIdCounter = 0;

        for (int i = 0; i < TOTAL_PAIRS; i++) {
            CardDefinition def = definitions[i];

            // Carta 1: Título del Artículo (AHORA SÍ MUESTRA EL EJEMPLO)
            cards.add(new MemoryCard(idCounter++, def.iconRes, def.textTitle, matchIdCounter, def.example));

            // Carta 2: Descripción del Artículo (TAMBIÉN MUESTRA EL EJEMPLO)
            // Si quieres que el ejemplo SOLO salga en la carta de "Art.", cambia 'def.example' por 'null' en la línea de abajo.
            cards.add(new MemoryCard(idCounter++, def.iconRes, def.textDesc, matchIdCounter, def.example));

            matchIdCounter++;
        }

        // Comodín (Sin ejemplo)
        cards.add(new MemoryCard(idCounter++, R.drawable.mi_logo, "¡Comodín!\n+20 Pts", -1, null));

        Collections.shuffle(cards);

        LayoutInflater inflater = LayoutInflater.from(this);

        for (MemoryCard card : cards) {
            View cardView = inflater.inflate(R.layout.item_memory_card, glCards, false);

            TextView tvText = cardView.findViewById(R.id.tv_card_text);
            TextView tvExample = cardView.findViewById(R.id.tvCardExample);
            ImageView ivIcon = cardView.findViewById(R.id.iv_card_icon);

            if (tvText != null) tvText.setText(card.textLabel);
            if (ivIcon != null) ivIcon.setImageResource(card.iconResId);

            if (tvExample != null) {
                if (card.example != null && !card.example.isEmpty()) {
                    tvExample.setVisibility(View.VISIBLE);
                    tvExample.setText(card.example);
                } else {
                    tvExample.setVisibility(View.GONE);
                }
            }

            GridLayout.LayoutParams params = new GridLayout.LayoutParams();
            params.width = 0; params.height = dpToPx(150);
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

        if (cardData.matchId == -1) {
            cardData.isMatched = true; score += 20;
            tvMessage.setText("¡Carta Comodín! +20 Pts");
            playSound(R.raw.correct_ding); vibrarSafe(100); updateUI();
            setCardStroke(view, "#FFD700", 3);
            selectedDetails1 = null; selectedView1 = null; isProcessing = false;
            return;
        }

        if (selectedDetails1 == null) {
            selectedDetails1 = cardData; selectedView1 = view;
        } else {
            isProcessing = true;
            checkMatch(view, cardData);
        }
    }

    private void checkMatch(View view2, MemoryCard cardData2) {
        if (selectedDetails1.matchId == cardData2.matchId) {
            racha++; score += (15 + (racha * 5)); pairsFound++;
            playSound(R.raw.correct_ding); vibrarSafe(50);
            ((MemoryCard) selectedView1.getTag()).isMatched = true; cardData2.isMatched = true;
            tvMessage.setText("¡Correcto!");

            if (lottieMascot != null) { lottieMascot.setAnimation(R.raw.owlie_nodding); lottieMascot.playAnimation(); }

            setCardStroke(selectedView1, "#4CAF50", 3); setCardStroke(view2, "#4CAF50", 3);
            selectedDetails1 = null; selectedView1 = null; isProcessing = false; updateUI();
            if (pairsFound == TOTAL_PAIRS) endGame(true);
        } else {
            racha = 0; lives--;
            playSound(R.raw.megaman_x_error); vibrarSafe(200);
            tvMessage.setText("¡Intenta de nuevo!");

            if (lottieMascot != null) { lottieMascot.setAnimation(R.raw.owlie_mad); lottieMascot.playAnimation(); }

            setCardStroke(selectedView1, "#F44336", 3); setCardStroke(view2, "#F44336", 3);

            new Handler(Looper.getMainLooper()).postDelayed(() -> {
                if (!isFinishing() && !isDestroyed()) {
                    if (lottieMascot != null && lives > 0) {
                        lottieMascot.setAnimation(R.raw.owlie_nodding);
                        lottieMascot.playAnimation();
                    }

                    flipCard(selectedView1, false); flipCard(view2, false);
                    setCardStroke(selectedView1, "#D3DCE6", 0); setCardStroke(view2, "#D3DCE6", 0);
                    selectedDetails1 = null; selectedView1 = null; isProcessing = false; updateUI();
                    if (lives <= 0) endGame(false);
                }
            }, 1200);
        }
    }

    private void setCardStroke(View view, String hexColor, int widthDp) {
        if (view instanceof MaterialCardView) {
            MaterialCardView card = (MaterialCardView) view;
            if (widthDp > 0) { card.setStrokeColor(Color.parseColor(hexColor)); card.setStrokeWidth(dpToPx(widthDp)); }
            else { card.setStrokeWidth(dpToPx(1.5f)); card.setStrokeColor(Color.parseColor(hexColor)); }
        }
    }

    private void flipCard(View view, boolean showFace) {
        final View front = view.findViewById(R.id.layout_front);
        final View back = view.findViewById(R.id.layout_back);
        view.animate().scaleX(0f).setDuration(150).withEndAction(() -> {
            if (showFace) { if(front!=null) front.setVisibility(View.VISIBLE); if(back!=null) back.setVisibility(View.GONE); }
            else { if(front!=null) front.setVisibility(View.GONE); if(back!=null) back.setVisibility(View.VISIBLE); }
            view.animate().scaleX(1f).setDuration(150).start();
        }).start();
    }

    private void startTimer() {
        if (timer != null) timer.cancel();
        timer = new CountDownTimer(GAME_TIME_MS, 1000) {
            @Override public void onTick(long m) {
                if(isFinishing()) { cancel(); return; }
                int sec = (int) (m / 1000);
                if (!tvMessage.getText().toString().contains("Pista")) tvMessage.setText("Tiempo: " + sec + "s");
                tvMessage.setTextColor(sec < 10 ? Color.RED : ContextCompat.getColor(MemoryGameActivity.this, R.color.upp_primary));
            }
            @Override public void onFinish() { if(!isFinishing()){ endGame(false); } }
        }.start();
    }

    private void endGame(boolean win) {
        if (timer != null) timer.cancel();
        if(isFinishing()) return;

        android.graphics.drawable.GradientDrawable shape = new android.graphics.drawable.GradientDrawable();
        shape.setColor(android.graphics.Color.WHITE); shape.setCornerRadius(40f);

        android.text.SpannableString titulo = new android.text.SpannableString(win ? "¡Felicidades!" : "Juego Terminado");
        titulo.setSpan(new android.text.style.ForegroundColorSpan(android.graphics.Color.BLACK), 0, titulo.length(), 0);
        titulo.setSpan(new android.text.style.StyleSpan(android.graphics.Typeface.BOLD), 0, titulo.length(), 0);

        android.text.SpannableString mensaje = new android.text.SpannableString(win ? "¡Encontraste los pares! Puntaje: " + score : "Se acabaron las vidas o el tiempo.");
        mensaje.setSpan(new android.text.style.ForegroundColorSpan(android.graphics.Color.DKGRAY), 0, mensaje.length(), 0);

        new MaterialAlertDialogBuilder(this).setTitle(titulo).setMessage(mensaje).setIcon(win ? R.drawable.ic_check_circle : R.drawable.ic_prohibiciones)
                .setCancelable(false).setPositiveButton("Reintentar", (d, w) -> startNewGame()).setNegativeButton("Salir", (d, w) -> finish())
                .setBackground(shape).show();
    }

    private void updateUI() { tvScore.setText("🏆 " + score); tvLives.setText("❤️ " + lives); }

    private void playSound(int resId) {
        try {
            if (mediaPlayer != null) { mediaPlayer.release(); mediaPlayer = null; }
            mediaPlayer = MediaPlayer.create(this, resId);
            if (mediaPlayer != null) mediaPlayer.start();
        } catch (Exception e) {}
    }

    private void vibrarSafe(long ms) {
        try {
            if (vibrator != null && vibrator.hasVibrator()) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) vibrator.vibrate(VibrationEffect.createOneShot(ms, VibrationEffect.DEFAULT_AMPLITUDE));
                else vibrator.vibrate(ms);
            }
        } catch (Exception e) {}
    }

    private int dpToPx(float dp) { return Math.round(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, getResources().getDisplayMetrics())); }

    @Override protected void onPause() { super.onPause(); if (timer != null) timer.cancel(); if (mediaPlayer != null) { mediaPlayer.release(); mediaPlayer = null; } }
    @Override protected void onDestroy() { super.onDestroy(); if (timer != null) timer.cancel(); if (mediaPlayer != null) { mediaPlayer.release(); mediaPlayer = null; } }

    private static class CardDefinition {
        int iconRes; String textTitle; String textDesc; String example;
        public CardDefinition(int iconRes, String textTitle, String textDesc, String example) {
            this.iconRes = iconRes; this.textTitle = textTitle; this.textDesc = textDesc; this.example = example;
        }
    }

    private static class MemoryCard {
        int id; int iconResId; String textLabel; int matchId; boolean isMatched = false; String example;
        public MemoryCard(int id, int iconResId, String textLabel, int matchId, String example) {
            this.id = id; this.iconResId = iconResId; this.textLabel = textLabel; this.matchId = matchId; this.example = example;
        }
    }
}
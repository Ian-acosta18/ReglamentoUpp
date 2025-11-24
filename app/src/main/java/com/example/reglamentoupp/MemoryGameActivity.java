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
import android.view.ContextThemeWrapper;
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
    private static final int TOTAL_PAIRS = 6; // 12 cartas (3 columnas x 4 filas)
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

    // --- CAMBIO: Usamos tus iconos (Drawables) en lugar de texto ---
    private final int[] availableIcons = {
            R.drawable.ic_derechos,
            R.drawable.ic_obligaciones,
            R.drawable.ic_prohibiciones,
            R.drawable.ic_sanciones,
            R.drawable.ic_reconocimientos,
            R.drawable.ic_game_trivia,
            R.drawable.ic_game_vf,
            R.drawable.ic_game_hangman
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

        // 1. Seleccionar 6 iconos aleatorios de tu lista
        List<Integer> indices = new ArrayList<>();
        for (int i = 0; i < availableIcons.length; i++) indices.add(i);
        Collections.shuffle(indices);

        List<Integer> selectedDrawables = new ArrayList<>();
        for (int i = 0; i < TOTAL_PAIRS; i++) {
            selectedDrawables.add(availableIcons[indices.get(i)]);
        }

        // 2. Crear las cartas (duplicando para hacer pares)
        int idCounter = 0;
        for (int iconRes : selectedDrawables) {
            // Carta A
            cards.add(new MemoryCard(idCounter++, iconRes));
            // Carta B (Par idéntico)
            cards.add(new MemoryCard(idCounter++, iconRes));
        }
        Collections.shuffle(cards);

        // 3. Añadir botones al Grid
        // ContextThemeWrapper para evitar crashes por el tema
        Context themeContext = new ContextThemeWrapper(this, com.google.android.material.R.style.Theme_MaterialComponents_DayNight_NoActionBar);

        for (MemoryCard card : cards) {
            MaterialButton btn = new MaterialButton(themeContext);

            // Estado Inicial: Signo de interrogación y SIN icono
            btn.setText("?");
            btn.setTextSize(24);
            btn.setIcon(null);

            // Estilo del botón
            try {
                btn.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(this, R.color.upp_primary)));
            } catch (Exception e) {
                btn.setBackgroundTintList(ColorStateList.valueOf(Color.BLUE));
            }
            btn.setTextColor(Color.WHITE);
            btn.setCornerRadius(24);

            // Configuración importante para iconos
            btn.setIconGravity(MaterialButton.ICON_GRAVITY_TEXT_START);
            btn.setIconPadding(0);
            btn.setInsetTop(0);
            btn.setInsetBottom(0);

            btn.setTag(card); // Guardamos la info de la carta en el botón

            // Layout Params
            GridLayout.LayoutParams params = new GridLayout.LayoutParams();
            params.width = 0;
            params.height = dpToPx(100); // Altura fija (aprox 100dp)
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

        // Si ya está encontrada o es la misma carta que acabo de tocar
        if (cardData.isMatched || cardData == selectedDetails1) return;

        // Voltear carta (Mostrar Icono)
        flipCard(btn, cardData, true);

        if (selectedDetails1 == null) {
            // Primera selección
            selectedDetails1 = cardData;
            selectedButton1 = btn;
        } else {
            // Segunda selección -> Verificar match
            isProcessing = true;
            checkMatch(btn, cardData);
        }
    }

    private void checkMatch(MaterialButton btn2, MemoryCard cardData2) {
        // Comparamos si los recursos de icono son iguales
        if (selectedDetails1.iconResId == cardData2.iconResId) {
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

        tvMessage.setText("¡Correcto!");
        if (lottieMascot != null) {
            lottieMascot.setAnimation(R.raw.happy_sun);
            lottieMascot.playAnimation();
        }

        // Pintar de verde
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
        vibrarSafe(300);

        tvMessage.setText("¡No coinciden!");
        if (lottieMascot != null) {
            lottieMascot.setAnimation(R.raw.angry_thunderstorm);
            lottieMascot.playAnimation();
        }

        // Pintar de rojo temporalmente
        int colorRojo = ContextCompat.getColor(this, R.color.game_fail);
        btn1.setBackgroundTintList(ColorStateList.valueOf(colorRojo));
        btn2.setBackgroundTintList(ColorStateList.valueOf(colorRojo));

        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            if (!isFinishing()) {
                // Voltear de nuevo (Ocultar icono)
                flipCard(btn1, (MemoryCard) btn1.getTag(), false);
                flipCard(btn2, (MemoryCard) btn2.getTag(), false);

                // Restaurar color azul
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
        // Animación de volteo
        btn.animate().scaleX(0f).setDuration(150).withEndAction(() -> {
            if (showFace) {
                // MOSTRAR LA CARA (ICONO)
                btn.setText(""); // Quitar texto
                btn.setIconResource(data.iconResId); // Poner icono
                btn.setIconSize(dpToPx(48)); // Tamaño del icono
                btn.setIconTint(null); // Importante: NULL para que se vean los colores originales
                btn.setIconGravity(MaterialButton.ICON_GRAVITY_TEXT_START); // Centrar (hack visual)

                // Fondo blanco para que resalte el icono
                btn.setBackgroundTintList(ColorStateList.valueOf(Color.WHITE));
            } else {
                // MOSTRAR EL DORSO (OCULTAR)
                btn.setIcon(null); // Quitar icono
                btn.setText("?"); // Poner interrogación
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
        } catch (SecurityException e) {
            // Ignorar si falta permiso
        }
    }

    private int dpToPx(int dp) {
        float density = getResources().getDisplayMetrics().density;
        return Math.round((float) dp * density);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (timer != null) timer.cancel();
    }

    // Clase auxiliar modificada para guardar Icono
    private static class MemoryCard {
        int id;
        int iconResId; // ID del recurso dibujable
        boolean isMatched = false;

        public MemoryCard(int id, int iconResId) {
            this.id = id;
            this.iconResId = iconResId;
        }
    }
}
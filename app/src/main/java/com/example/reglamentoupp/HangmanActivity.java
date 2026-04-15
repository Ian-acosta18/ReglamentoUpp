package com.example.reglamentoupp;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.Typeface;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Bundle;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.view.Gravity;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.example.reglamentoupp.databinding.ActivityHangmanBinding;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.util.Random;

public class HangmanActivity extends AppCompatActivity {

    private ActivityHangmanBinding binding;
    private Vibrator vibrator;
    private MediaPlayer mediaPlayer;

    private String[][] palabrasConPistas = {
            {"ESTADIA", "Etapa final donde aplicas lo aprendido trabajando en un proyecto dentro de una empresa real:"},
            {"CALIDAD", "Si repruebas tus materias al límite, pierdes esta condición como estudiante de la UPPue:"},
            {"BAJA", "Trámite que debes solicitar si tienes un problema grave y necesitas pausar tus estudios un cuatrimestre:"},
            {"RECTOR", "Es la máxima autoridad ejecutiva que toma las decisiones finales en la Universidad:"},
            {"TITULO", "Documento profesional final que demuestra que cumpliste con tu plan de estudios y estadía:"},
            {"KARDEX", "Si vas a pedir una beca y te piden tu historial completo de calificaciones, solicitas tu:"},
            {"CONSEJO", "Grupo de especialistas externos que opina y evalúa para que tu carrera tenga mejor nivel:"},
            {"SANCION", "Lo que recibes si te descubren haciendo trampa en un examen o faltando al respeto:"},
            {"COMITE", "Si sufres de acoso o discriminación en la universidad, debes acudir a este grupo:"},
            {"CREDITO", "Valor numérico que se le da a cada materia; necesitas juntar todos para poder graduarte:"}
    };

    private String palabraSecreta;
    private char[] palabraAdivinada;
    private int vidas = 6;
    private int pistasRestantes = 2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityHangmanBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);

        iniciarJuego();

        binding.btnBack.setOnClickListener(v -> finish());

        // NUEVA LÓGICA DEL BOTÓN INFO
        binding.btnHelp.setOnClickListener(v -> mostrarInstruccionesYPista());
    }

    private void mostrarInstruccionesYPista() {
        String reglas = "Reglas del Ahorcado:\n\n" +
                "• Lee la definición y adivina el concepto letra por letra.\n" +
                "• Tienes 6 vidas (errores permitidos).\n" +
                "• Evita que las vidas lleguen a 0.\n\n" +
                "¿Quieres revelar una letra correcta?";

        new MaterialAlertDialogBuilder(this)
                .setTitle("Información del Juego")
                .setMessage(reglas)
                .setPositiveButton("Revelar Letra", (dialog, which) -> usarPista())
                .setNegativeButton("Cerrar", null)
                .show();
    }

    private void usarPista() {
        if (vidas <= 0) return;
        if (pistasRestantes <= 0) {
            Toast.makeText(this, "Se agotaron tus pistas", Toast.LENGTH_SHORT).show();
            return;
        }

        for (int i = 0; i < palabraSecreta.length(); i++) {
            if (palabraAdivinada[i] == '_') {
                char letraOculta = palabraSecreta.charAt(i);

                for (int r = 0; r < binding.layoutTeclado.getChildCount(); r++) {
                    LinearLayout row = (LinearLayout) binding.layoutTeclado.getChildAt(r);
                    for (int c = 0; c < row.getChildCount(); c++) {
                        MaterialButton btn = (MaterialButton) row.getChildAt(c);
                        if (btn.getText().toString().charAt(0) == letraOculta && btn.isEnabled()) {
                            pistasRestantes--;
                            Toast.makeText(this, "¡Pista! (" + pistasRestantes + " restantes)", Toast.LENGTH_SHORT).show();
                            onLetraClick(btn);
                            return;
                        }
                    }
                }
            }
        }
    }

    private void iniciarJuego() {
        vidas = 6;
        pistasRestantes = 2;
        int index = new Random().nextInt(palabrasConPistas.length);
        palabraSecreta = palabrasConPistas[index][0];
        String definicion = palabrasConPistas[index][1];

        palabraAdivinada = new char[palabraSecreta.length()];
        for (int i = 0; i < palabraSecreta.length(); i++) palabraAdivinada[i] = '_';

        binding.tvQuestion.setText(definicion);
        actualizarUI();
        generarTecladoQWERTY();
    }

    private void actualizarUI() {
        StringBuilder sb = new StringBuilder();
        for (char c : palabraAdivinada) sb.append(c).append(" ");
        binding.tvHangmanWord.setText(sb.toString());
        binding.tvHangmanLives.setText("❤️ " + vidas);
        binding.tvHangmanLives.setTextColor(vidas <= 2 ? ContextCompat.getColor(this, R.color.game_fail) : Color.WHITE);
    }

    private void generarTecladoQWERTY() {
        binding.layoutTeclado.removeAllViews();
        String[] filas = {"QWERTYUIOP", "ASDFGHJKLÑ", "ZXCVBNM"};
        int heightPx = (int) (60 * getResources().getDisplayMetrics().density);

        for (String filaLetras : filas) {
            LinearLayout rowLayout = new LinearLayout(this);
            LinearLayout.LayoutParams rowParams = new LinearLayout.LayoutParams(-1, -2);
            rowParams.setMargins(0, 4, 0, 4);
            rowLayout.setLayoutParams(rowParams);
            rowLayout.setOrientation(LinearLayout.HORIZONTAL);
            rowLayout.setGravity(Gravity.CENTER);
            rowLayout.setWeightSum(10f);

            for (char c : filaLetras.toCharArray()) {
                MaterialButton btnLetra = new MaterialButton(this);
                btnLetra.setText(String.valueOf(c));
                btnLetra.setOnClickListener(this::onLetraClick);
                btnLetra.setBackgroundResource(R.drawable.fondo_tecla_ahorcado);
                btnLetra.setTextColor(Color.WHITE);
                btnLetra.setTypeface(null, Typeface.BOLD);
                btnLetra.setTextSize(32);
                btnLetra.setPadding(0, 0, 0, 0);
                btnLetra.setInsetTop(0); btnLetra.setInsetBottom(0); btnLetra.setMinHeight(0); btnLetra.setMinimumHeight(0);

                LinearLayout.LayoutParams btnParams = new LinearLayout.LayoutParams(0, heightPx);
                btnParams.weight = 1f; btnParams.setMargins(6, 6, 6, 6);
                btnLetra.setLayoutParams(btnParams);
                rowLayout.addView(btnLetra);
            }
            binding.layoutTeclado.addView(rowLayout);
        }
    }

    private void onLetraClick(View view) {
        MaterialButton btn = (MaterialButton) view;
        btn.setEnabled(false);
        btn.setAlpha(0.5f);
        String letra = btn.getText().toString();

        if (palabraSecreta.contains(letra)) {
            vibrar(50);
            reproducirSonido(R.raw.correct_ding);
            btn.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(this, R.color.status_green)));
            btn.setTextColor(Color.WHITE);
            binding.tvHangmanWord.animate().scaleX(1.1f).scaleY(1.1f).setDuration(100)
                    .withEndAction(() -> binding.tvHangmanWord.animate().scaleX(1f).scaleY(1f).start()).start();

            boolean gano = true;
            for (int i = 0; i < palabraSecreta.length(); i++) {
                if (palabraSecreta.charAt(i) == letra.charAt(0)) palabraAdivinada[i] = letra.charAt(0);
                if (palabraAdivinada[i] == '_') gano = false;
            }
            if (gano) mostrarDialogo("¡Excelente!", "La palabra era: " + palabraSecreta, true);
        } else {
            vibrar(300);
            reproducirSonido(R.raw.megaman_x_error);
            btn.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#FFC107")));
            btn.setTextColor(Color.BLACK);
            binding.tvHangmanLives.startAnimation(AnimationUtils.loadAnimation(this, R.anim.shake_error));
            vidas--;
            if (vidas <= 0) mostrarDialogo("¡Juego Terminado!", "La palabra era: " + palabraSecreta, false);
        }
        actualizarUI();
    }

    private void vibrar(long ms) {
        if (vibrator != null && vibrator.hasVibrator()) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) vibrator.vibrate(VibrationEffect.createOneShot(ms, VibrationEffect.DEFAULT_AMPLITUDE));
            else vibrator.vibrate(ms);
        }
    }

    private void reproducirSonido(int resId) {
        try {
            if (mediaPlayer != null) { mediaPlayer.release(); mediaPlayer = null; }
            mediaPlayer = MediaPlayer.create(this, resId);
            if (mediaPlayer != null) mediaPlayer.start();
        } catch (Exception e) {}
    }

    private void mostrarDialogo(String titleText, String msgText, boolean ganado) {
        if(isFinishing() || isDestroyed()) return;
        int icon = ganado ? R.drawable.ic_check_circle : R.drawable.ic_prohibiciones;

        android.graphics.drawable.GradientDrawable shape = new android.graphics.drawable.GradientDrawable();
        shape.setColor(android.graphics.Color.WHITE);
        shape.setCornerRadius(40f);

        android.text.SpannableString titulo = new android.text.SpannableString(titleText);
        titulo.setSpan(new android.text.style.ForegroundColorSpan(android.graphics.Color.BLACK), 0, titulo.length(), 0);
        titulo.setSpan(new android.text.style.StyleSpan(android.graphics.Typeface.BOLD), 0, titulo.length(), 0);

        android.text.SpannableString mensaje = new android.text.SpannableString(msgText);
        mensaje.setSpan(new android.text.style.ForegroundColorSpan(android.graphics.Color.DKGRAY), 0, mensaje.length(), 0);

        new MaterialAlertDialogBuilder(this).setTitle(titulo).setMessage(mensaje).setIcon(icon)
                .setPositiveButton("Otra vez", (d, w) -> iniciarJuego()).setNegativeButton("Salir", (d, w) -> finish())
                .setCancelable(false).setBackground(shape).show();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mediaPlayer != null) { mediaPlayer.release(); mediaPlayer = null; }
    }
}
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

    // Matriz con [Palabra Respuesta] y [Situación Práctica]
    private String[][] palabrasConPistas = {
            {"ESTADIA", "Etapa final donde aplicas lo aprendido trabajando en un proyecto dentro de una empresa real:"},
            {"CALIDAD", "Si repruebas tus materias al límite, pierdes esta condición como estudiante de la UPP:"},
            {"BAJA", "Trámite que debes solicitar si tienes un problema grave y necesitas pausar tus estudios un cuatrimestre:"},
            {"RECTOR", "Es la máxima autoridad ejecutiva que toma las decisiones finales en la Universidad:"},
            {"TITULO", "Documento profesional final que demuestra que cumpliste con tu plan de estudios y estadía:"},
            {"KARDEX", "Si vas a pedir una beca y te piden tu historial completo de calificaciones, solicitas tu:"},
            {"CONSEJO", "Grupo de especialistas externos que opina y evalúa para que tu carrera tenga mejor nivel:"},
            {"SANCION", "Lo que recibes si te descubren haciendo trampa en un examen o faltando al respeto:"},
            {"COMITE", "Si sufres de acoso o discriminación en la universidad, debes acudir a este grupo:"},
            {"CREDITO", "Valor numérico que se le da a cada materia; necesitas juntar todos para poder graduarte:"},
            {"ASISTENCIA", "Debes mantener al menos el 80% de esto en el parcial para tener derecho a calificación ordinaria:"},
            {"RENUNCIA", "Si decides irte de la universidad definitivamente por cuenta propia, debes firmar tu:"}
    };

    private String palabraSecreta;
    private char[] palabraAdivinada;
    private int vidas = 6;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityHangmanBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);

        iniciarJuego();

        binding.btnBack.setOnClickListener(v -> finish());

        // Listener del botón de ayuda con el ícono de foco
        binding.btnHelp.setOnClickListener(v -> mostrarInstrucciones());
    }

    // Método para mostrar el cuadro de diálogo con las instrucciones
    private void mostrarInstrucciones() {
        new MaterialAlertDialogBuilder(this)
                .setTitle("Instrucciones: Ahorcado")
                .setMessage("1. Lee cuidadosamente la definición que aparece en el recuadro blanco.\n\n" +
                        "2. Toca las letras en el teclado para adivinar la palabra oculta.\n\n" +
                        "3. Tienes 6 vidas (❤️). Cada error te restará una.\n\n" +
                        "4. ¡Adivina toda la palabra antes de perder tus vidas!")
                .setPositiveButton("¡Entendido!", (dialog, which) -> dialog.dismiss())
                .show();
    }

    private void iniciarJuego() {
        vidas = 6;
        int index = new Random().nextInt(palabrasConPistas.length);
        palabraSecreta = palabrasConPistas[index][0];
        String definicion = palabrasConPistas[index][1];

        palabraAdivinada = new char[palabraSecreta.length()];
        for (int i = 0; i < palabraSecreta.length(); i++) {
            palabraAdivinada[i] = '_';
        }

        binding.tvQuestion.setText(definicion);
        actualizarUI();
        generarTecladoQWERTY();
    }

    private void actualizarUI() {
        StringBuilder sb = new StringBuilder();
        for (char c : palabraAdivinada) {
            sb.append(c).append(" ");
        }
        binding.tvHangmanWord.setText(sb.toString());

        binding.tvHangmanLives.setText("❤️ " + vidas);
        if (vidas <= 2) {
            binding.tvHangmanLives.setTextColor(ContextCompat.getColor(this, R.color.game_fail));
        } else {
            binding.tvHangmanLives.setTextColor(Color.WHITE);
        }
    }

    private void generarTecladoQWERTY() {
        binding.layoutTeclado.removeAllViews();

        String[] filas = {
                "QWERTYUIOP",
                "ASDFGHJKLÑ",
                "ZXCVBNM"
        };

        int heightPx = (int) (60 * getResources().getDisplayMetrics().density);

        for (String filaLetras : filas) {
            LinearLayout rowLayout = new LinearLayout(this);
            LinearLayout.LayoutParams rowParams = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            );
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
                btnLetra.setTextSize(26);

                btnLetra.setPadding(0, 0, 0, 0);
                btnLetra.setInsetTop(0);
                btnLetra.setInsetBottom(0);
                btnLetra.setMinHeight(0);
                btnLetra.setMinimumHeight(0);

                LinearLayout.LayoutParams btnParams = new LinearLayout.LayoutParams(0, heightPx);
                btnParams.weight = 1f;
                btnParams.setMargins(6, 6, 6, 6);

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
                if (palabraSecreta.charAt(i) == letra.charAt(0)) {
                    palabraAdivinada[i] = letra.charAt(0);
                }
                if (palabraAdivinada[i] == '_') {
                    gano = false;
                }
            }

            if (gano) mostrarDialogo("¡Excelente!", "La palabra era: " + palabraSecreta, true);
        } else {
            vibrar(300);
            reproducirSonido(R.raw.megaman_x_error);

            btn.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#FFC107")));
            btn.setTextColor(Color.BLACK);

            binding.tvHangmanLives.startAnimation(AnimationUtils.loadAnimation(this, R.anim.shake_error));
            vidas--;
            if (vidas <= 0) mostrarDialogo("¡Juego Terminado!", "La palabra correcta era: " + palabraSecreta, false);
        }
        actualizarUI();
    }

    private void vibrar(long milisegundos) {
        if (vibrator != null && vibrator.hasVibrator()) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator.vibrate(VibrationEffect.createOneShot(milisegundos, VibrationEffect.DEFAULT_AMPLITUDE));
            } else {
                vibrator.vibrate(milisegundos);
            }
        }
    }

    private void reproducirSonido(int soundResource) {
        try {
            if(mediaPlayer != null) mediaPlayer.release();
            mediaPlayer = MediaPlayer.create(this, soundResource);
            if (mediaPlayer != null) {
                mediaPlayer.start();
                mediaPlayer.setOnCompletionListener(MediaPlayer::release);
            }
        } catch (Exception e) { e.printStackTrace(); }
    }

    private void mostrarDialogo(String titulo, String mensaje, boolean ganado) {
        if(isFinishing() || isDestroyed()) return;
        int icon = ganado ? R.drawable.ic_check_circle : R.drawable.ic_prohibiciones;
        new MaterialAlertDialogBuilder(this)
                .setTitle(titulo)
                .setMessage(mensaje)
                .setIcon(icon)
                .setPositiveButton("Jugar Otra vez", (d, w) -> iniciarJuego())
                .setNegativeButton("Salir", (d, w) -> finish())
                .setCancelable(false)
                .show();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mediaPlayer != null) { mediaPlayer.release(); mediaPlayer = null; }
    }
}
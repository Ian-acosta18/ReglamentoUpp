package com.example.reglamentoupp;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Bundle;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.widget.GridLayout;

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

    // --- LISTA DE PALABRAS ESPECÍFICAS DEL REGLAMENTO UPP ---
    private String[][] palabrasConPistas = {
            {"ESTADIA", "Práctica profesional obligatoria en el sector productivo para titularse:"},
            {"CALIDAD", "Condición oficial de 'Alumno' que se pierde al reprobar definitivamente:"},
            {"BAJA", "Suspensión de estudios permitida hasta por 3 cuatrimestres:"},
            {"RECTOR", "Máxima autoridad ejecutiva de la Universidad Politécnica:"},
            {"TITULO", "Grado académico obtenido al concluir el plan de estudios y la estadía:"},
            {"KARDEX", "Documento oficial que acredita todo el historial académico del alumno:"},
            {"CONSEJO", "Órgano colegiado de calidad o social que apoya la gestión universitaria:"},
            {"SANCION", "Consecuencia aplicable por infringir las normas de conducta universitaria:"},
            {"COMITE", "Grupo encargado de vigilar la igualdad laboral y no discriminación:"},
            {"CREDITO", "Unidad de valor académico asignada a cada asignatura del plan de estudios:"},
            {"ASISTENCIA", "Requisito mínimo del 80% para tener derecho a evaluación ordinaria:"},
            {"RENUNCIA", "Acto voluntario de darse de baja definitiva de la universidad:"}
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
        generarTeclado();
    }

    private void actualizarUI() {
        StringBuilder sb = new StringBuilder();
        for (char c : palabraAdivinada) {
            sb.append(c).append(" ");
        }
        binding.tvHangmanWord.setText(sb.toString());

        binding.tvHangmanLives.setText("Vidas: " + vidas);
        if (vidas <= 2) {
            binding.tvHangmanLives.setTextColor(ContextCompat.getColor(this, R.color.game_fail));
        } else {
            binding.tvHangmanLives.setTextColor(Color.WHITE);
        }
    }

    private void generarTeclado() {
        binding.glKeyboard.removeAllViews();
        for (char c = 'A'; c <= 'Z'; c++) {
            MaterialButton btnLetra = new MaterialButton(this, null, com.google.android.material.R.attr.materialButtonOutlinedStyle);
            btnLetra.setText(String.valueOf(c));
            btnLetra.setOnClickListener(this::onLetraClick);

            btnLetra.setTextColor(Color.WHITE);
            btnLetra.setStrokeColor(ColorStateList.valueOf(Color.WHITE));
            btnLetra.setCornerRadius(20);
            btnLetra.setRippleColor(ColorStateList.valueOf(ContextCompat.getColor(this, R.color.upp_accent)));

            GridLayout.LayoutParams params = new GridLayout.LayoutParams();
            params.width = 0;
            params.height = GridLayout.LayoutParams.WRAP_CONTENT;
            params.columnSpec = GridLayout.spec(GridLayout.UNDEFINED, 1f);
            params.setMargins(6, 6, 6, 6);
            btnLetra.setLayoutParams(params);

            binding.glKeyboard.addView(btnLetra);
        }
    }

    private void onLetraClick(View view) {
        MaterialButton btn = (MaterialButton) view;
        btn.setEnabled(false);
        String letra = btn.getText().toString();

        if (palabraSecreta.contains(letra)) {
            vibrar(50);
            reproducirSonido(R.raw.correct_ding);
            btn.setBackgroundColor(ContextCompat.getColor(this, R.color.status_green));
            btn.setStrokeColor(ColorStateList.valueOf(Color.TRANSPARENT));

            binding.tvHangmanWord.animate().scaleX(1.1f).scaleY(1.1f).setDuration(100)
                    .withEndAction(() -> binding.tvHangmanWord.animate().scaleX(1f).scaleY(1f).start()).start();

            boolean gano = true;
            for (int i = 0; i < palabraSecreta.length(); i++) {
                if (palabraAdivinada[i] == '_') {
                    if (palabraSecreta.charAt(i) == letra.charAt(0)) {
                        palabraAdivinada[i] = letra.charAt(0);
                    }
                }
                // Segunda pasada para asegurar letras repetidas
                if (palabraSecreta.charAt(i) == letra.charAt(0)) {
                    palabraAdivinada[i] = letra.charAt(0);
                }
                if (palabraAdivinada[i] == '_') gano = false;
            }

            if (gano) mostrarDialogo("¡Excelente!", "La palabra era: " + palabraSecreta, true);
        } else {
            vibrar(300);
            reproducirSonido(R.raw.megaman_x_error);
            btn.setBackgroundColor(ContextCompat.getColor(this, R.color.game_fail));
            btn.setStrokeColor(ColorStateList.valueOf(Color.TRANSPARENT));
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
        if(isFinishing()) return;
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
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

    // Palabras y sus DEFINICIONES
    private String[][] palabrasConPistas = {
            {"REGLAMENTO", "¿Cómo se llama el documento rector con todas las normas?"},
            {"DERECHOS", "¿Qué nombre reciben las facultades y permisos que tienes como alumno?"},
            {"SANCION", "¿Cuál es la consecuencia directa de cometer una falta al reglamento?"},
            {"BECA", "Apoyo económico o académico otorgado por desempeño o necesidad:"},
            {"ALUMNO", "Término oficial con el que se denomina al estudiante matriculado:"},
            {"UPP", "¿Cuáles son las siglas oficiales de tu universidad?"},
            {"FALTA", "Acción u omisión que contraviene las normas universitarias:"},
            {"NORMA", "Sinónimo de regla o disposición establecida:"},
            {"CREDENCIAL", "Documento personal e intransferible para ingresar al campus:"},
            {"RECTOR", "¿Quién es la máxima autoridad ejecutiva de la Universidad?"},
            {"TUTOR", "Docente asignado para guiar tu trayectoria académica:"},
            {"BAJA", "Suspensión temporal o definitiva de los estudios:"},
            {"TITULO", "Documento oficial que recibes al finalizar y aprobar toda la carrera:"},
            {"ESTADIA", "Periodo de prácticas profesionales que realizas al final del plan:"},
            {"KARDEX", "Documento que resume todo tu historial de calificaciones:"},
            {"BIBLIOTECA", "Espacio universitario dedicado al resguardo de libros y estudio:"},
            {"LABORATORIO", "Área equipada para realizar prácticas experimentales o tecnológicas:"},
            {"CUATRIMESTRE", "Periodo lectivo de aproximadamente 4 meses de duración:"},
            {"EXTRAORDINARIO", "Tipo de examen que presentas si repruebas la ordinaria:"},
            {"PLAGIO", "Falta grave que consiste en copiar obras ajenas haciéndolas pasar por propias:"}
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

        // Botón de salir o regresar
        binding.btnBack.setOnClickListener(v -> finish());
    }

    private void iniciarJuego() {
        vidas = 6;
        // Seleccionar palabra aleatoria
        int index = new Random().nextInt(palabrasConPistas.length);
        palabraSecreta = palabrasConPistas[index][0];
        String definicion = palabrasConPistas[index][1];

        // Preparar los guiones bajos
        palabraAdivinada = new char[palabraSecreta.length()];
        for (int i = 0; i < palabraSecreta.length(); i++) {
            palabraAdivinada[i] = '_';
        }

        // UI Inicial
        binding.tvQuestion.setText(definicion); // Mostrar la pregunta
        actualizarUI();
        generarTeclado();
    }

    private void actualizarUI() {
        // Actualizar texto de la palabra con espacios (ej: _ _ G _ A)
        StringBuilder sb = new StringBuilder();
        for (char c : palabraAdivinada) {
            sb.append(c).append(" ");
        }
        binding.tvHangmanWord.setText(sb.toString());

        // Actualizar Vidas (Texto y Color)
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

            // Estilo moderno del botón
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
        btn.setEnabled(false); // Desactivar botón pulsado
        String letra = btn.getText().toString();

        if (palabraSecreta.contains(letra)) {
            // --- ACIERTO ---
            vibrar(50); // Vibración leve
            reproducirSonido(R.raw.correct_ding);
            btn.setBackgroundColor(ContextCompat.getColor(this, R.color.status_green));
            btn.setStrokeColor(ColorStateList.valueOf(Color.TRANSPARENT));

            // Animación de rebote (POP) en el texto de la palabra
            binding.tvHangmanWord.animate()
                    .scaleX(1.1f).scaleY(1.1f)
                    .setDuration(100)
                    .withEndAction(() -> binding.tvHangmanWord.animate().scaleX(1f).scaleY(1f).start())
                    .start();

            boolean gano = true;
            for (int i = 0; i < palabraSecreta.length(); i++) {
                if (palabraSecreta.charAt(i) == letra.charAt(0)) {
                    palabraAdivinada[i] = letra.charAt(0);
                }
                if (palabraAdivinada[i] == '_') {
                    gano = false;
                }
            }

            if (gano) {
                mostrarDialogo("¡Excelente!", "La palabra era: " + palabraSecreta, true);
            }
        } else {
            // --- ERROR ---
            vibrar(300); // Vibración fuerte
            reproducirSonido(R.raw.megaman_x_error);
            btn.setBackgroundColor(ContextCompat.getColor(this, R.color.game_fail));
            btn.setStrokeColor(ColorStateList.valueOf(Color.TRANSPARENT));

            // Animación de sacudida en el texto de vidas
            binding.tvHangmanLives.startAnimation(AnimationUtils.loadAnimation(this, R.anim.shake_error));

            vidas--;
            if (vidas <= 0) {
                mostrarDialogo("¡Juego Terminado!", "La palabra correcta era: " + palabraSecreta, false);
            }
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
            MediaPlayer mp = MediaPlayer.create(this, soundResource);
            if (mp != null) {
                mp.start();
                mp.setOnCompletionListener(MediaPlayer::release);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void mostrarDialogo(String titulo, String mensaje, boolean ganado) {
        int icon = ganado ? R.drawable.ic_check_circle : R.drawable.ic_prohibiciones;

        new MaterialAlertDialogBuilder(this)
                .setTitle(titulo)
                .setMessage(mensaje)
                .setIcon(icon)
                .setPositiveButton("Jugar Otra vez", (dialog, which) -> {
                    iniciarJuego();
                })
                .setNegativeButton("Salir", (dialog, which) -> finish())
                .setCancelable(false)
                .show();
    }
}
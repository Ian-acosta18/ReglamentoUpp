package com.example.reglamentoupp;

import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.GridLayout;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.reglamentoupp.databinding.ActivityHangmanBinding;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.util.Random;

public class HangmanActivity extends AppCompatActivity {

    private ActivityHangmanBinding binding;

    // Palabras clave con PISTAS AUMENTADAS
    private String[][] palabrasConPistas = {
            // Básicos
            {"REGLAMENTO", "Documento con todas las normas"},
            {"DERECHOS", "Lo que tienes permitido como alumno"},
            {"SANCION", "Consecuencia de una falta"},
            {"BECA", "Apoyo económico o académico"},
            {"ALUMNO", "Así se le llama al estudiante"},
            {"UPP", "Siglas de tu universidad"},
            {"FALTA", "Incumplimiento de una norma"},
            {"NORMA", "Sinónimo de regla"},
            // Nuevas palabras agregadas
            {"CREDENCIAL", "Identificación oficial para ingresar"},
            {"RECTOR", "Máxima autoridad de la Universidad"},
            {"TUTOR", "Docente que guía tu trayectoria"},
            {"BAJA", "Suspensión temporal o definitiva"},
            {"TITULO", "Documento al finalizar la carrera"},
            {"ESTADIA", "Prácticas profesionales finales"},
            {"KARDEX", "Historial de tus calificaciones"},
            {"BIBLIOTECA", "Lugar de estudio y libros"},
            {"LABORATORIO", "Espacio para prácticas con equipo"},
            {"CUATRIMESTRE", "Periodo de estudio de 4 meses"},
            {"EXTRAORDINARIO", "Examen de recuperación"},
            {"PLAGIO", "Copiar trabajos (prohibido)"}
    };

    private String palabraSecreta;
    private String pistaActual;
    private char[] palabraAdivinada;
    private int vidas = 6;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityHangmanBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        iniciarJuego();
        generarTeclado();

        // Listener para el botón de Pista
        binding.btnHangmanHint.setOnClickListener(v -> mostrarPista());
    }

    private void iniciarJuego() {
        vidas = 6;

        // Seleccionar palabra y pista
        int index = new Random().nextInt(palabrasConPistas.length);
        palabraSecreta = palabrasConPistas[index][0];
        pistaActual = palabrasConPistas[index][1];

        palabraAdivinada = new char[palabraSecreta.length()];

        for (int i = 0; i < palabraSecreta.length(); i++) {
            palabraAdivinada[i] = '_';
        }

        // Resetear UI de pista
        binding.tvHangmanHint.setVisibility(View.GONE);
        binding.btnHangmanHint.setEnabled(true);

        actualizarUI();
    }

    private void mostrarPista() {
        if (vidas > 1) {
            vidas--; // Penalización por usar pista
            binding.tvHangmanHint.setText(pistaActual);
            binding.tvHangmanHint.setVisibility(View.VISIBLE);
            binding.btnHangmanHint.setEnabled(false); // Solo una pista por palabra
            actualizarUI();
            Toast.makeText(this, "¡Pierdes 1 vida por la pista!", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "No puedes usar la pista, te queda 1 vida", Toast.LENGTH_SHORT).show();
        }
    }

    private void actualizarUI() {
        binding.tvHangmanWord.setText(getPalabraAdivinadaString());
        binding.tvHangmanLives.setText("Vidas restantes: " + vidas);
    }

    private String getPalabraAdivinadaString() {
        StringBuilder sb = new StringBuilder();
        for (char c : palabraAdivinada) {
            sb.append(c).append(" ");
        }
        return sb.toString();
    }

    private void generarTeclado() {
        binding.glKeyboard.removeAllViews();
        for (char c = 'A'; c <= 'Z'; c++) {
            // Usar MaterialButton para mejor diseño
            MaterialButton btnLetra = new MaterialButton(this, null, com.google.android.material.R.attr.materialButtonOutlinedStyle);
            btnLetra.setText(String.valueOf(c));
            btnLetra.setOnClickListener(this::onLetraClick);
            btnLetra.setTextColor(Color.WHITE);
            btnLetra.setStrokeColor(ColorStateList.valueOf(Color.WHITE));
            btnLetra.setCornerRadius(16);

            GridLayout.LayoutParams params = new GridLayout.LayoutParams();
            params.width = 0;
            params.height = GridLayout.LayoutParams.WRAP_CONTENT;
            params.columnSpec = GridLayout.spec(GridLayout.UNDEFINED, 1f); // Distribuir equitativamente
            params.setMargins(8, 8, 8, 8);
            btnLetra.setLayoutParams(params);

            binding.glKeyboard.addView(btnLetra);
        }
    }

    private void onLetraClick(View view) {
        MaterialButton btn = (MaterialButton) view;
        btn.setEnabled(false); // Deshabilitar botón
        btn.setStrokeColor(ColorStateList.valueOf(Color.GRAY));
        btn.setTextColor(Color.GRAY);

        String letra = btn.getText().toString();

        if (palabraSecreta.contains(letra)) {
            // Letra correcta
            btn.setBackgroundColor(Color.parseColor("#804CAF50")); // Verde semitransparente
            for (int i = 0; i < palabraSecreta.length(); i++) {
                if (palabraSecreta.charAt(i) == letra.charAt(0)) {
                    palabraAdivinada[i] = letra.charAt(0);
                }
            }
            if (haGanado()) {
                mostrarDialogo("¡Ganaste!", "Adivinaste la palabra: " + palabraSecreta);
            }
        } else {
            // Letra incorrecta
            btn.setBackgroundColor(Color.parseColor("#80F44336")); // Rojo semitransparente
            vidas--;
            Toast.makeText(this, "¡Incorrecto!", Toast.LENGTH_SHORT).show();
            if (vidas <= 0) {
                mostrarDialogo("¡Perdiste!", "La palabra era: " + palabraSecreta);
            }
        }
        actualizarUI();
    }

    private boolean haGanado() {
        return new String(palabraAdivinada).equals(palabraSecreta);
    }

    private void mostrarDialogo(String titulo, String mensaje) {
        new MaterialAlertDialogBuilder(this)
                .setTitle(titulo)
                .setMessage(mensaje)
                .setPositiveButton("Jugar de Nuevo", (dialog, which) -> {
                    iniciarJuego();
                    generarTeclado();
                })
                .setNegativeButton("Salir", (dialog, which) -> finish())
                .setCancelable(false)
                .show();
    }
}
package com.example.reglamentoupp;

import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.reglamentoupp.databinding.ActivityHangmanBinding;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.util.Random;

import android.widget.GridLayout; // <-- ¡ESTA ES LA LÍNEA QUE FALTABA!

public class HangmanActivity extends AppCompatActivity {

    private ActivityHangmanBinding binding;

    // Palabras clave del reglamento
    private String[] palabras = {"REGLAMENTO", "DERECHOS", "SANCION", "BECA", "ALUMNO", "UPP", "FALTA", "NORMA"};
    private String palabraSecreta;
    private char[] palabraAdivinada;
    private int vidas = 6;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityHangmanBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        iniciarJuego();
        generarTeclado();
    }

    private void iniciarJuego() {
        vidas = 6;
        palabraSecreta = palabras[new Random().nextInt(palabras.length)];
        palabraAdivinada = new char[palabraSecreta.length()];

        for (int i = 0; i < palabraSecreta.length(); i++) {
            palabraAdivinada[i] = '_';
        }
        actualizarUI();
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
            Button btnLetra = new Button(this);
            btnLetra.setText(String.valueOf(c));
            btnLetra.setOnClickListener(this::onLetraClick);

            // Estilo del botón (opcional, pero recomendado)
            // Estas líneas ahora funcionan gracias al import
            GridLayout.LayoutParams params = new GridLayout.LayoutParams();
            params.width = 0;
            params.height = GridLayout.LayoutParams.WRAP_CONTENT;
            params.columnSpec = GridLayout.spec(GridLayout.UNDEFINED, 1f); // Distribuir equitativamente
            params.setMargins(4, 4, 4, 4);
            btnLetra.setLayoutParams(params);

            binding.glKeyboard.addView(btnLetra);
        }
    }

    private void onLetraClick(View view) {
        Button btn = (Button) view;
        btn.setEnabled(false); // Deshabilitar botón
        String letra = btn.getText().toString();

        if (palabraSecreta.contains(letra)) {
            // Letra correcta
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
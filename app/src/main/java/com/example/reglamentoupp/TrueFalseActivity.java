package com.example.reglamentoupp;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.example.reglamentoupp.databinding.ActivityTrueFalseBinding;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class TrueFalseActivity extends AppCompatActivity {

    private ActivityTrueFalseBinding binding;
    private FirebaseFirestore mStore;
    private List<PreguntaVF> listaDePreguntas;
    private PreguntaVF preguntaActual;
    private int indicePreguntaActual = 0;
    private int puntaje = 0;
    private boolean botonesBloqueados = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityTrueFalseBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        mStore = FirebaseFirestore.getInstance();
        listaDePreguntas = new ArrayList<>();

        binding.btnTrue.setOnClickListener(v -> checarRespuesta(true));
        binding.btnFalse.setOnClickListener(v -> checarRespuesta(false));

        cargarPreguntasVF();
    }

    private void cargarPreguntasVF() {
        // --- ¡IMPORTANTE! ---
        // Debes crear esta colección "preguntasVF" en tu Firestore
        // y añadir documentos con "afirmacion" (String) y "respuesta" (Boolean)

        mStore.collection("preguntasVF")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (queryDocumentSnapshots.isEmpty()) {
                        Toast.makeText(this, "No hay preguntas V/F.", Toast.LENGTH_SHORT).show();
                        // Añadir preguntas de ejemplo si está vacío
                        crearPreguntasDeEjemplo();
                        return;
                    }
                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        listaDePreguntas.add(doc.toObject(PreguntaVF.class));
                    }
                    Collections.shuffle(listaDePreguntas);
                    mostrarSiguientePregunta();
                })
                .addOnFailureListener(e -> finish());
    }

    private void crearPreguntasDeEjemplo() {
        // Solo para probar. Borra esto en producción.
        FirebaseFirestore.getInstance().collection("preguntasVF").add(new PreguntaVF("Puedes fumar en los baños.", false));
        FirebaseFirestore.getInstance().collection("preguntasVF").add(new PreguntaVF("Debes portar tu credencial en todo momento.", true));
        Toast.makeText(this, "Añadiendo preguntas de ejemplo. Reinicia el juego.", Toast.LENGTH_LONG).show();
    }

    private void mostrarSiguientePregunta() {
        if (indicePreguntaActual >= listaDePreguntas.size()) {
            indicePreguntaActual = 0; // Reiniciar el ciclo
            Collections.shuffle(listaDePreguntas);
        }

        botonesBloqueados = false;
        preguntaActual = listaDePreguntas.get(indicePreguntaActual);
        binding.tvVFQuestion.setText(preguntaActual.getAfirmacion());
        binding.tvVFScore.setText("Puntaje: " + puntaje);
        binding.cardVFQuestion.setCardBackgroundColor(ContextCompat.getColor(this, R.color.card_bg));

        indicePreguntaActual++;
    }

    private void checarRespuesta(boolean respuestaElegida) {
        if (botonesBloqueados) return;
        botonesBloqueados = true;

        if (respuestaElegida == preguntaActual.isRespuesta()) {
            // Correcto
            puntaje += 10;
            binding.cardVFQuestion.setCardBackgroundColor(ContextCompat.getColor(this, R.color.game_success_bg));
            Toast.makeText(this, "¡Correcto!", Toast.LENGTH_SHORT).show();
        } else {
            // Incorrecto
            binding.cardVFQuestion.setCardBackgroundColor(ContextCompat.getColor(this, R.color.game_fail_bg));
            Toast.makeText(this, "Incorrecto. La respuesta era: " + !respuestaElegida, Toast.LENGTH_SHORT).show();
        }

        new Handler(Looper.getMainLooper()).postDelayed(this::mostrarSiguientePregunta, 1200);
    }
}
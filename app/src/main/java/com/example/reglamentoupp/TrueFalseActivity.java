package com.example.reglamentoupp;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log; // Importar Log
import android.view.View;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.example.reglamentoupp.databinding.ActivityTrueFalseBinding;
import com.google.firebase.auth.FirebaseAuth; // Importar Auth
import com.google.firebase.firestore.FieldValue; // Importar FieldValue
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class TrueFalseActivity extends AppCompatActivity {

    private ActivityTrueFalseBinding binding;
    private FirebaseFirestore mStore;
    private FirebaseAuth mAuth; // Para obtener el usuario actual
    private List<PreguntaVF> listaDePreguntas;
    private PreguntaVF preguntaActual;
    private int indicePreguntaActual = 0;
    private int puntajeSesion = 0; // Puntos ganados en ESTA partida
    private boolean botonesBloqueados = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityTrueFalseBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        mStore = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance(); // Inicializar Auth
        listaDePreguntas = new ArrayList<>();

        binding.btnTrue.setOnClickListener(v -> checarRespuesta(true));
        binding.btnFalse.setOnClickListener(v -> checarRespuesta(false));

        cargarPreguntasVF();
    }

    private void cargarPreguntasVF() {
        mStore.collection("preguntasVF")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (queryDocumentSnapshots.isEmpty()) {
                        // Si no hay preguntas, creamos un set completo
                        crearPreguntasDeEjemplo();
                        return;
                    }
                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        listaDePreguntas.add(doc.toObject(PreguntaVF.class));
                    }
                    iniciarJuego();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Error al cargar datos", Toast.LENGTH_SHORT).show();
                    finish();
                });
    }

    private void iniciarJuego() {
        Collections.shuffle(listaDePreguntas);
        // Opcional: Limitar a 10 preguntas por ronda para no hacerlo infinito
        if (listaDePreguntas.size() > 10) {
            listaDePreguntas = listaDePreguntas.subList(0, 10);
        }
        mostrarSiguientePregunta();
    }

    // --- AQUÍ AGREGAMOS MÁS PREGUNTAS ---
    private void crearPreguntasDeEjemplo() {
        List<PreguntaVF> nuevasPreguntas = new ArrayList<>();

        nuevasPreguntas.add(new PreguntaVF("¿Los alumnos tienen derecho a recibir asesorías del personal académico?", true));
        nuevasPreguntas.add(new PreguntaVF("¿Está permitido fumar en los baños de la universidad?", false));
        nuevasPreguntas.add(new PreguntaVF("¿Es una obligación portar la credencial de estudiante al ingresar?", true));
        nuevasPreguntas.add(new PreguntaVF("¿Puedes comercializar dulces o productos dentro de los salones?", false));
        nuevasPreguntas.add(new PreguntaVF("¿La inasistencia colectiva (pinta) está permitida si todos están de acuerdo?", false));
        nuevasPreguntas.add(new PreguntaVF("¿Tienes derecho a conocer el resultado de tus evaluaciones oportunamente?", true));
        nuevasPreguntas.add(new PreguntaVF("¿Dañar el mobiliario de la escuela es causa de sanción?", true));
        nuevasPreguntas.add(new PreguntaVF("¿Los juegos de azar y apuestas están permitidos en la cafetería?", false));
        nuevasPreguntas.add(new PreguntaVF("¿Debes respetar las disposiciones de la legislación universitaria?", true));
        nuevasPreguntas.add(new PreguntaVF("¿La beca a la excelencia académica se otorga por promedio?", true));

        // Subir todas a Firestore
        for (PreguntaVF p : nuevasPreguntas) {
            mStore.collection("preguntasVF").add(p);
        }

        Toast.makeText(this, "Cargando nuevas preguntas... Reinicia el juego.", Toast.LENGTH_LONG).show();
        // Recargamos la actividad para que aparezcan
        new Handler(Looper.getMainLooper()).postDelayed(this::recreate, 2000);
    }

    private void mostrarSiguientePregunta() {
        if (indicePreguntaActual >= listaDePreguntas.size()) {
            terminarJuego(); // Juego terminado
            return;
        }

        botonesBloqueados = false;
        preguntaActual = listaDePreguntas.get(indicePreguntaActual);
        binding.tvVFQuestion.setText(preguntaActual.getAfirmacion());
        binding.tvVFScore.setText("Puntaje: " + puntajeSesion);
        binding.cardVFQuestion.setCardBackgroundColor(ContextCompat.getColor(this, R.color.card_bg));

        indicePreguntaActual++;
    }

    private void checarRespuesta(boolean respuestaElegida) {
        if (botonesBloqueados) return;
        botonesBloqueados = true;

        if (respuestaElegida == preguntaActual.isRespuesta()) {
            puntajeSesion += 10;
            binding.cardVFQuestion.setCardBackgroundColor(ContextCompat.getColor(this, R.color.game_success_container));
            Toast.makeText(this, "¡Correcto!", Toast.LENGTH_SHORT).show();
            // Guardamos el punto INMEDIATAMENTE en la base de datos
            guardarPuntos(10);
        } else {
            binding.cardVFQuestion.setCardBackgroundColor(ContextCompat.getColor(this, R.color.game_fail_container));
            Toast.makeText(this, "Incorrecto", Toast.LENGTH_SHORT).show();
        }

        new Handler(Looper.getMainLooper()).postDelayed(this::mostrarSiguientePregunta, 1200);
    }

    // --- SOLUCIÓN AL PROBLEMA DE SUMA DE PUNTOS ---
    private void guardarPuntos(int puntosGanados) {
        if (mAuth.getCurrentUser() == null) return;

        // Usamos FieldValue.increment para sumar al valor existente en la nube
        // esto evita que se sobrescriba o se bloquee en 100
        mStore.collection("usuarios").document(mAuth.getCurrentUser().getUid())
                .update("puntaje", FieldValue.increment(puntosGanados))
                .addOnFailureListener(e -> Log.e("TrueFalse", "Error al guardar puntos", e));
    }

    private void terminarJuego() {
        new com.google.android.material.dialog.MaterialAlertDialogBuilder(this)
                .setTitle("¡Juego Terminado!")
                .setMessage("Conseguiste " + puntajeSesion + " puntos en esta ronda.")
                .setPositiveButton("Salir", (dialog, which) -> finish())
                .setCancelable(false)
                .show();
    }
}
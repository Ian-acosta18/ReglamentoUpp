package com.example.reglamentoupp;

import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.example.reglamentoupp.databinding.ActivityTrueFalseBinding;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class TrueFalseActivity extends AppCompatActivity {

    private ActivityTrueFalseBinding binding;
    private FirebaseFirestore mStore;
    private FirebaseAuth mAuth;
    private List<PreguntaVF> listaDePreguntas;
    private PreguntaVF preguntaActual;
    private int indicePreguntaActual = 0;
    private int puntajeSesion = 0;
    private boolean botonesBloqueados = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityTrueFalseBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        mStore = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();
        listaDePreguntas = new ArrayList<>();

        binding.btnTrue.setOnClickListener(v -> checarRespuesta(true));
        binding.btnFalse.setOnClickListener(v -> checarRespuesta(false));

        // Botón de salir
        if (getSupportActionBar() != null) getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        binding.toolbarTf.setNavigationOnClickListener(v -> finish());

        cargarPreguntasVF();
    }

    private void cargarPreguntasVF() {
        mStore.collection("preguntasVF")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (queryDocumentSnapshots.isEmpty()) {
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

    private void crearPreguntasDeEjemplo() {
        List<PreguntaVF> nuevasPreguntas = new ArrayList<>();

        // --- ACADÉMICO ---
        nuevasPreguntas.add(new PreguntaVF("¿La calificación mínima aprobatoria en una asignatura ordinaria es de 7.0?", true));
        nuevasPreguntas.add(new PreguntaVF("¿Tienes derecho a solicitar revisión de calificación si no estás de acuerdo con el resultado?", true));
        nuevasPreguntas.add(new PreguntaVF("¿Puedes cursar una materia si reprobaste la anterior que está seriada (prerrequisito)?", false));
        nuevasPreguntas.add(new PreguntaVF("¿La Estadía Profesional se puede realizar debiendo 3 materias?", false));
        nuevasPreguntas.add(new PreguntaVF("¿El alumno tiene un límite de tiempo para concluir su plan de estudios?", true));
        nuevasPreguntas.add(new PreguntaVF("¿Es posible dar de baja una materia después de haber presentado el examen ordinario?", false));
        nuevasPreguntas.add(new PreguntaVF("¿El título profesional se expide automáticamente al terminar las materias sin hacer trámites?", false));

        // --- DISCIPLINA Y SANCIONES ---
        nuevasPreguntas.add(new PreguntaVF("¿Está permitido consumir bebidas alcohólicas dentro del campus?", false));
        nuevasPreguntas.add(new PreguntaVF("¿Fumar (incluyendo vapeadores) está prohibido en todas las instalaciones de la UPP?", true));
        nuevasPreguntas.add(new PreguntaVF("¿Agredir verbalmente a un docente o compañero es motivo de sanción?", true));
        nuevasPreguntas.add(new PreguntaVF("¿Puedes realizar ventas de productos personales en los salones sin permiso?", false));
        nuevasPreguntas.add(new PreguntaVF("¿El uso de la credencial institucional es obligatorio para el acceso al campus?", true));
        nuevasPreguntas.add(new PreguntaVF("¿Dañar el mobiliario o equipo de laboratorio se considera una falta grave?", true));

        // --- DERECHOS Y SERVICIOS ---
        nuevasPreguntas.add(new PreguntaVF("¿Tienes derecho a recibir un trato digno y respetuoso de toda la comunidad?", true));
        nuevasPreguntas.add(new PreguntaVF("¿El seguro facultativo (IMSS) es gratuito para los estudiantes matriculados?", true));
        nuevasPreguntas.add(new PreguntaVF("¿Tienes derecho a recibir asesorías académicas (tutorías) durante tu carrera?", true));
        nuevasPreguntas.add(new PreguntaVF("¿Puedes hacer uso de las instalaciones deportivas en los horarios establecidos?", true));
        nuevasPreguntas.add(new PreguntaVF("¿La biblioteca permite el préstamo de libros a domicilio?", true));

        // --- GENERAL ---
        nuevasPreguntas.add(new PreguntaVF("¿La UPP se rige por un modelo educativo basado en competencias?", true));
        nuevasPreguntas.add(new PreguntaVF("¿Copiar textualmente un trabajo de internet sin citar es considerado plagio?", true));
        nuevasPreguntas.add(new PreguntaVF("¿Las inasistencias a clase no afectan tu derecho a calificación?", false));
        nuevasPreguntas.add(new PreguntaVF("¿Es responsabilidad del alumno mantener actualizados sus datos de contacto?", true));

        for (PreguntaVF p : nuevasPreguntas) {
            mStore.collection("preguntasVF").add(p);
        }

        Toast.makeText(this, "Banco de preguntas actualizado. Reiniciando...", Toast.LENGTH_SHORT).show();
        new Handler(Looper.getMainLooper()).postDelayed(this::recreate, 1500);
    }

    private void iniciarJuego() {
        Collections.shuffle(listaDePreguntas);
        // Tomamos hasta 15 preguntas para que el juego dure más
        if (listaDePreguntas.size() > 15) {
            listaDePreguntas = listaDePreguntas.subList(0, 15);
        }
        mostrarSiguientePregunta();
    }

    private void mostrarSiguientePregunta() {
        if (indicePreguntaActual >= listaDePreguntas.size()) {
            terminarJuego();
            return;
        }

        botonesBloqueados = false;
        preguntaActual = listaDePreguntas.get(indicePreguntaActual);
        binding.tvVFQuestion.setText(preguntaActual.getAfirmacion());
        binding.tvVFScore.setText("Puntaje: " + puntajeSesion);

        // Restaurar color original
        binding.cardVFQuestion.setCardBackgroundColor(ContextCompat.getColor(this, R.color.white));

        indicePreguntaActual++;
    }

    private void checarRespuesta(boolean respuestaElegida) {
        if (botonesBloqueados) return;
        botonesBloqueados = true;

        if (respuestaElegida == preguntaActual.isRespuesta()) {
            // --- CORRECTO ---
            puntajeSesion += 10;
            reproducirSonido(R.raw.correct_ding);
            binding.cardVFQuestion.setCardBackgroundColor(ContextCompat.getColor(this, R.color.game_success_container));
            Toast.makeText(this, "¡Correcto!", Toast.LENGTH_SHORT).show();
            guardarPuntos(10);
        } else {
            // --- INCORRECTO ---
            reproducirSonido(R.raw.megaman_x_error);
            binding.cardVFQuestion.setCardBackgroundColor(ContextCompat.getColor(this, R.color.game_fail_container));
            Toast.makeText(this, "Incorrecto", Toast.LENGTH_SHORT).show();
        }

        new Handler(Looper.getMainLooper()).postDelayed(this::mostrarSiguientePregunta, 1200);
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

    private void guardarPuntos(int puntosGanados) {
        if (mAuth.getCurrentUser() == null) return;

        mStore.collection("usuarios").document(mAuth.getCurrentUser().getUid())
                .update("puntaje", FieldValue.increment(puntosGanados))
                .addOnFailureListener(e -> Log.e("TrueFalse", "Error al guardar puntos", e));
    }

    private void terminarJuego() {
        new MaterialAlertDialogBuilder(this)
                .setTitle("¡Ronda Terminada!")
                .setMessage("Conseguiste " + puntajeSesion + " puntos.")
                .setPositiveButton("Salir", (dialog, which) -> finish())
                .setCancelable(false)
                .show();
    }
}
package com.example.reglamentoupp;

import android.util.Log;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ReglamentoSeeder {

    private final FirebaseFirestore db;

    public ReglamentoSeeder() {
        db = FirebaseFirestore.getInstance();
    }

    public void subirDatos() {
        subirPreguntasQuiz();
        subirPreguntasVF();
    }

    private void subirPreguntasQuiz() {
        // Colección "preguntas" para el Quiz de Opción Múltiple
        List<Pregunta> quizQuestions = Arrays.asList(
                new Pregunta(
                        "Según el Art. 3 (VIII), ¿qué derecho tienes al inscribirte?",
                        "Tener casillero propio",
                        "Obtener número de matrícula y credencial",
                        "Elegir a los maestros",
                        "Obtener número de matrícula y credencial"
                ),
                new Pregunta(
                        "De acuerdo al Art. 5 (V), es obligación de los alumnos:",
                        "Asistir puntualmente a las actividades académicas",
                        "Traer uniforme todos los días",
                        "Comprar libros en la biblioteca",
                        "Asistir puntualmente a las actividades académicas"
                ),
                new Pregunta(
                        "¿Qué deben hacer los alumnos al ingresar según el Art. 5 (XII)?",
                        "Firmar una bitácora",
                        "Saludar al guardia",
                        "Mostrar la credencial de alumno",
                        "Mostrar la credencial de alumno"
                ),
                new Pregunta(
                        "El Art. 3 (I) garantiza el derecho a:",
                        "Cursar estudios según planes y programas vigentes",
                        "Exentar todas las materias",
                        "Salir temprano los viernes",
                        "Cursar estudios según planes y programas vigentes"
                ),
                new Pregunta(
                        "Según el Art. 5 (X), los alumnos deben cuidar:",
                        "Los coches de los maestros",
                        "Los espacios, muebles y materiales de la Universidad",
                        "Solo su salón de clases",
                        "Los espacios, muebles y materiales de la Universidad"
                ),
                new Pregunta(
                        "Art. 3 (VII): Tienes derecho a conocer oportunamente:",
                        "La vida privada de los profesores",
                        "El resultado de las evaluaciones que presentes",
                        "Las preguntas del examen final",
                        "El resultado de las evaluaciones que presentes"
                ),
                new Pregunta(
                        "Si dañas algo por negligencia, el Art. 5 (XI) te obliga a:",
                        "Pedir disculpas",
                        "Reparar los daños ocasionados",
                        "Ser expulsado inmediatamente",
                        "Reparar los daños ocasionados"
                ),
                new Pregunta(
                        "Art. 3 (III): Tienes derecho a recibir orientación de:",
                        "Los alumnos de semestres superiores",
                        "Las Direcciones de Programas Académicos",
                        "El personal de seguridad",
                        "Las Direcciones de Programas Académicos"
                )
        );

        for (Pregunta p : quizQuestions) {
            db.collection("preguntas").add(p)
                    .addOnSuccessListener(doc -> Log.d("SEEDER", "Quiz subido: " + p.getPregunta()));
        }
    }

    private void subirPreguntasVF() {
        // Colección "preguntasVF" para Verdadero o Falso
        List<PreguntaVF> vfQuestions = Arrays.asList(
                new PreguntaVF("El Art. 5 (I) dice que eres responsable de tu propio proceso de formación.", true),
                new PreguntaVF("Según el Art. 3, los alumnos no tienen derecho a conocer sus calificaciones.", false),
                new PreguntaVF("Es obligación (Art. 5 XI) reparar los daños que causes a la Universidad.", true),
                new PreguntaVF("El Art. 3 (V) dice que la evaluación puede ser secreta y sin criterios claros.", false),
                new PreguntaVF("Debes mostrar tu credencial al ingresar a las instalaciones (Art. 5 XII).", true),
                new PreguntaVF("El Art. 5 (II) obliga a respetar la legislación universitaria.", true),
                new PreguntaVF("Tienes derecho a recibir asesorías y tutorías (Art. 3 XIII).", true),
                new PreguntaVF("Los alumnos pueden faltar a clases cuando quieran sin justificación (Art. 5 V).", false)
        );

        for (PreguntaVF p : vfQuestions) {
            db.collection("preguntasVF").add(p)
                    .addOnSuccessListener(doc -> Log.d("SEEDER", "VF subido: " + p.getAfirmacion()));
        }
    }

    // Clases internas simples para asegurar compatibilidad si no tienes los getters/setters exactos
    public static class Pregunta {
        String pregunta, opcionA, opcionB, opcionC, respuestaCorrecta;
        public Pregunta(String p, String a, String b, String c, String r) {
            this.pregunta = p; this.opcionA = a; this.opcionB = b; this.opcionC = c; this.respuestaCorrecta = r;
        }
        public String getPregunta() { return pregunta; }
        public String getOpcionA() { return opcionA; }
        public String getOpcionB() { return opcionB; }
        public String getOpcionC() { return opcionC; }
        public String getRespuestaCorrecta() { return respuestaCorrecta; }
    }

    public static class PreguntaVF {
        String afirmacion;
        boolean respuesta;
        public PreguntaVF(String a, boolean r) { this.afirmacion = a; this.respuesta = r; }
        public String getAfirmacion() { return afirmacion; }
        public boolean isRespuesta() { return respuesta; }
    }
}
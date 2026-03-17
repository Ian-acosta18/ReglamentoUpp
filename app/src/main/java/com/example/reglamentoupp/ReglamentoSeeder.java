package com.example.reglamentoupp;

import android.util.Log;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.ArrayList;
import java.util.List;

public class ReglamentoSeeder {

    private static final String TAG = "ReglamentoSeeder";

    public static void seedPreguntas() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        List<Pregunta> preguntas = getPreguntasTrivia();

        for (int i = 0; i < preguntas.size(); i++) {
            Pregunta p = preguntas.get(i);
            db.collection("preguntas").document("pregunta_" + i)
                    .set(p)
                    .addOnSuccessListener(aVoid -> Log.d(TAG, "Pregunta " + p.getPregunta() + " agregada exitosamente"))
                    .addOnFailureListener(e -> Log.e(TAG, "Error al agregar pregunta", e));
        }
    }

    public static void seedPreguntasVF() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        List<PreguntaVF> preguntasVF = getPreguntasVF();

        for (int i = 0; i < preguntasVF.size(); i++) {
            PreguntaVF p = preguntasVF.get(i);
            db.collection("preguntasVF").document("vf_" + i)
                    .set(p)
                    .addOnSuccessListener(aVoid -> Log.d(TAG, "Pregunta V/F agregada exitosamente"))
                    .addOnFailureListener(e -> Log.e(TAG, "Error al agregar pregunta V/F", e));
        }
    }

    private static List<Pregunta> getPreguntasTrivia() {
        List<Pregunta> lista = new ArrayList<>();

        // 1 al 5
        lista.add(new Pregunta("Un profesor te asigna calificación de 6, pero sacaste 10 en tus exámenes. ¿Qué derecho ejerces?", "Guardar silencio", "Pedir una revisión formal (Apelación)", "Darte de baja de la materia", "Pedir una revisión formal (Apelación)"));
        lista.add(new Pregunta("Faltaste 3 días por tener influenza. ¿Qué debes hacer para que no te afecte en tus asistencias?", "Avisar por WhatsApp al jefe de grupo", "Entregar justificante médico en Control Escolar", "No hacer nada, el profesor entenderá", "Entregar justificante médico en Control Escolar"));
        lista.add(new Pregunta("Llegas a la entrada de la universidad pero olvidaste tu credencial. ¿Qué procede?", "No puedes ingresar al campus", "Entras si el guardia te conoce", "Pagas una multa en caja", "No puedes ingresar al campus"));
        lista.add(new Pregunta("Termina el cuatrimestre y tienes 3 materias reprobadas de forma definitiva. ¿Cuál es tu estatus?", "Alumno irregular", "Baja definitiva de la institución", "Cursar el año completo otra vez", "Baja definitiva de la institución"));
        lista.add(new Pregunta("Durante un examen, el profesor te descubre copiando del celular. ¿Qué sucede?", "Solo te quita el celular", "Te expulsa un mes del campus", "Anulación del examen y posible sanción", "Anulación del examen y posible sanción"));

        // 6 al 10
        lista.add(new Pregunta("Llegas tarde y no te dejan entrar. ¿Qué porcentaje de asistencia mínima necesitas para no reprobar?", "70% de asistencia", "80% de asistencia", "90% de asistencia", "80% de asistencia"));
        lista.add(new Pregunta("Estás haciendo tu Servicio Social. ¿Cuántas horas en total debes cubrir para que sea válido?", "300 horas", "480 horas", "500 horas", "480 horas"));
        lista.add(new Pregunta("Te peleas a golpes con un compañero dentro del campus. ¿Qué tipo de falta es?", "Falta leve (Amonestación)", "Falta grave (Expulsión definitiva)", "No aplica si fue en las canchas", "Falta grave (Expulsión definitiva)"));
        lista.add(new Pregunta("¿Qué pasa si te sorprenden consumiendo bebidas alcohólicas dentro de la universidad?", "Te mandan a tu casa por un día", "Baja definitiva de la institución", "Te quitan la credencial una semana", "Baja definitiva de la institución"));
        lista.add(new Pregunta("Pierdes un libro de la biblioteca. ¿Qué debes hacer para poder reinscribirte?", "Reponerlo con uno igual o pagarlo", "Pedir disculpas al bibliotecario", "Imprimir una copia en blanco y negro", "Reponerlo con uno igual o pagarlo"));

        // 11 al 15
        lista.add(new Pregunta("¿Cuál es el tiempo máximo que tienes para terminar tu carrera (incluyendo estadía)?", "10 cuatrimestres", "15 cuatrimestres", "Tiempo ilimitado", "15 cuatrimestres"));
        lista.add(new Pregunta("Dañas intencionalmente el proyector del salón. ¿Cuál es tu responsabilidad?", "Ninguna, la universidad lo paga", "Reparar el daño o pagar su costo total", "Comprar uno de menor calidad", "Reparar el daño o pagar su costo total"));
        lista.add(new Pregunta("Se te pasó la fecha límite de pago de reinscripción. ¿Qué consecuencia tiene esto?", "Pagas el doble de colegiatura", "Causas baja temporal automática", "Te dejan pagar a fin de mes", "Causas baja temporal automática"));
        lista.add(new Pregunta("Descubres un error de dedo en tu nombre impreso en el sistema. ¿Cuándo debes pedir la corrección?", "El día que te gradúes", "Inmediatamente en Control Escolar", "No importa, el SAT lo arregla", "Inmediatamente en Control Escolar"));
        lista.add(new Pregunta("Estás a punto de iniciar tus prácticas de Estadía. ¿Qué requisito previo es obligatorio?", "Haber liberado el Servicio Social", "Comprar el uniforme de gala", "Tener promedio general de 10", "Haber liberado el Servicio Social"));

        // 16 al 20
        lista.add(new Pregunta("Vas a presentar tu examen de regularización (extra) pero no lo pagaste a tiempo.", "El profesor te lo aplica de favor", "Pierdes el derecho a presentarlo", "Lo pagas el próximo cuatrimestre", "Pierdes el derecho a presentarlo"));
        lista.add(new Pregunta("Un alumno falsifica una receta médica para justificar faltas. ¿Qué sanción le corresponde?", "Se le perdonan las faltas", "Expulsión definitiva (falta gravísima)", "Llamada de atención verbal", "Expulsión definitiva (falta gravísima)"));
        lista.add(new Pregunta("¿Qué documento te acredita como alumno activo y debes portar visible en el campus?", "El recibo de pago impreso", "La tira de materias sellada", "La credencial escolar vigente", "La credencial escolar vigente"));
        lista.add(new Pregunta("Entregas un ensayo final, pero le copiaste todo a un compañero haciéndolo pasar por tuyo.", "Es plagio, amerita anulación y sanción", "Es trabajo en equipo, no pasa nada", "Te bajan 2 puntos nada más", "Es plagio, amerita anulación y sanción"));
        lista.add(new Pregunta("Terminaste tu carrera, estadía y servicio social. ¿Qué trámite final te falta?", "Tramitar tu Título Profesional", "Hacer examen de admisión de nuevo", "Entregar tu credencial al guardia", "Tramitar tu Título Profesional"));

        return lista;
    }

    private static List<PreguntaVF> getPreguntasVF() {
        List<PreguntaVF> lista = new ArrayList<>();

        lista.add(new PreguntaVF("Si se te olvida tu credencial, puedes prestarle la suya a un amigo para que ambos entren.", false));
        lista.add(new PreguntaVF("Tienes el derecho de exigir ver los resultados de tus exámenes antes de que el profe firme actas.", true));
        lista.add(new PreguntaVF("Las faltas por enfermedad se borran solas del sistema sin que entregues ningún documento.", false));
        lista.add(new PreguntaVF("Si dañas un microscopio del laboratorio por jugar, es tu obligación pagar su reparación.", true));
        lista.add(new PreguntaVF("Falsificar la firma de un profesor es una falta leve que solo amerita un regaño rápido.", false));
        lista.add(new PreguntaVF("Para poder titularte, es obligatorio liberar tu Servicio Social y concluir tu Estadía.", true));
        lista.add(new PreguntaVF("Puedes fumar cigarrillos electrónicos (vapes) dentro del salón si el profesor no está.", false));
        lista.add(new PreguntaVF("La universidad está obligada a darte orientación o tutoría si tienes problemas académicos.", true));
        lista.add(new PreguntaVF("Si repruebas un examen ordinario, causas baja automática sin derecho a regularización.", false));
        lista.add(new PreguntaVF("Es obligatorio dirigirte con respeto a todo el personal, maestros y compañeros en el campus.", true));
        lista.add(new PreguntaVF("Puedes ingresar con comida para vender en los pasillos y pagar tu colegiatura sin pedir permiso.", false));
        lista.add(new PreguntaVF("Tienes derecho a recibir tu horario (carga académica) de manera oficial al inicio del cuatrimestre.", true));
        lista.add(new PreguntaVF("Si cometes una falta disciplinaria leve, la primera sanción suele ser una amonestación por escrito.", true));
        lista.add(new PreguntaVF("El Servicio Social lo puedes iniciar desde el primer día de clases de tu primer cuatrimestre.", false));
        lista.add(new PreguntaVF("Está prohibido portar armas de cualquier tipo dentro de las instalaciones de la universidad.", true));
        lista.add(new PreguntaVF("Si no asistes a tu fiesta o ceremonia de graduación, se invalida tu título universitario.", false));
        lista.add(new PreguntaVF("Cuidar el medio ambiente y depositar la basura en su lugar es una obligación del reglamento.", true));
        lista.add(new PreguntaVF("Las faltas colectivas (ponerse de acuerdo todo el salón para no entrar) están justificadas.", false));
        lista.add(new PreguntaVF("Como alumno, tienes derecho a ser escuchado antes de que te apliquen una sanción muy grave.", true));
        lista.add(new PreguntaVF("Usar el logo oficial de la UPP para organizar fiestas externas en antros está permitido.", false));

        return lista;
    }
}
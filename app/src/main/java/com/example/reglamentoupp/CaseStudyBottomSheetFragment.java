package com.example.reglamentoupp;

import android.os.Bundle;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class CaseStudyBottomSheetFragment extends BottomSheetDialogFragment {

    private static final String ARG_CATEGORIA = "categoria";
    private static final String ARG_REGLA = "regla";

    private String categoria, regla;

    // Base de datos simple de casos de estudio
    private static final Map<String, String[]> casosDb = new HashMap<>();
    static {
        casosDb.put("Derecho", new String[]{
                "Un alumno reprueba una materia y siente que el profesor no fue claro con la forma de evaluar. El alumno solicita ver su examen final.",
                "Un estudiante nuevo se siente perdido y no sabe qué materias inscribir para el siguiente cuatrimestre.",
                "Un estudiante de último cuatrimestre necesita realizar su servicio social y prácticas profesionales."
        });
        casosDb.put("Obligación", new String[]{
                "Un grupo de alumnos daña intencionalmente una de las bancas del patio de la universidad.",
                "Un estudiante pierde su credencial y un guardia de seguridad le niega el acceso a la biblioteca.",
                "Un alumno falta a 3 clases seguidas sin justificación y su profesor le advierte sobre el reglamento."
        });
        casosDb.put("Prohibición", new String[]{
                "Un estudiante es sorprendido fumando dentro de los baños de la universidad.",
                "Un grupo de alumnos está organizando una 'tanda' (juego de azar) en medio del salón de clases.",
                "Un estudiante decide vender dulces y frituras en el salón de clases durante el receso."
        });
        casosDb.put("Sanción", new String[]{
                "Un grupo de alumnos organiza una 'pinta' (inasistencia colectiva) para no presentar un examen.",
                "Un estudiante rompe por accidente un matraz en el laboratorio de química.",
                "Un alumno es sorprendido copiando en un examen final usando su teléfono."
        });
        casosDb.put("Reconocimiento", new String[]{
                "Un estudiante mantiene un promedio de 9.8 durante tres cuatrimestres seguidos.",
                "Un equipo de estudiantes gana el primer lugar en un concurso nacional de robótica representando a la UPP.",
                "Un alumno presenta una tesis sobresaliente y el jurado decide otorgarle una mención."
        });
    }

    public static CaseStudyBottomSheetFragment newInstance(String categoria, String regla) {
        CaseStudyBottomSheetFragment fragment = new CaseStudyBottomSheetFragment();
        Bundle args = new Bundle();
        args.putString(ARG_CATEGORIA, categoria);
        args.putString(ARG_REGLA, regla);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            categoria = getArguments().getString(ARG_CATEGORIA);
            regla = getArguments().getString(ARG_REGLA);
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_case_study, container, false);

        TextView tvSituacion = view.findViewById(R.id.tv_case_situacion);
        TextView tvRegla = view.findViewById(R.id.tv_case_regla);
        Button btnEntendido = view.findViewById(R.id.btn_case_entendido);

        // Selecciona un caso al azar de la categoría
        String situacion = "No se encontró un caso de estudio para esta regla.";
        if (casosDb.containsKey(categoria)) {
            String[] casos = casosDb.get(categoria);
            if (casos != null && casos.length > 0) {
                situacion = casos[new Random().nextInt(casos.length)];
            }
        }

        tvSituacion.setText(situacion);
        tvRegla.setText(Html.fromHtml(regla, Html.FROM_HTML_MODE_LEGACY));

        btnEntendido.setOnClickListener(v -> dismiss());

        return view;
    }
}
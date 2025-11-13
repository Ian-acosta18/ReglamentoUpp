package com.example.reglamentoupp;

public class ObligacionesFragment extends BaseReglamentoFragment {

    @Override
    protected int getLayoutRes() {
        return R.layout.fragment_obligaciones; //
    }

    @Override
    protected String[] getItems() {
        return new String[]{
                "<strong>Artículo 5 (I):</strong> Ser responsables de su proceso de formación profesional.",
                "<strong>Artículo 5 (II):</strong> Observar y respetar las disposiciones de la legislación universitaria.",
                "<strong>Artículo 5 (V):</strong> Asistir puntualmente y participar en las actividades académicas.",
                "<strong>Artículo 5 (X):</strong> Utilizar y conservar de manera responsable los espacios y materiales.",
                "<strong>Artículo 5 (XI):</strong> Reparar los daños que por negligencia o intencionalmente ocasionen.",
                "<strong>Artículo 5 (XII):</strong> Mostrar la credencial que los identifica como alumnos al ingresar."
        };
    }


    @Override
    protected int getRecyclerViewId() {
        return R.id.obligaciones_recycler_view; //
    }

    // --- NUEVO MÉTODO IMPLEMENTADO ---
    @Override
    protected String getItemType() {
        return "Obligación"; // Respuesta correcta
    }
}
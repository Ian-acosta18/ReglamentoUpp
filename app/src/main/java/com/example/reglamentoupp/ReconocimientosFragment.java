package com.example.reglamentoupp;

public class ReconocimientosFragment extends BaseReglamentoFragment {

    @Override
    protected int getLayoutRes() {
        return R.layout.fragment_reconocimientos; //
    }

    // ... (dentro de la clase ReconocimientosFragment)
    @Override
    protected String[] getItems() {
        return new String[]{
                "<strong>Reconocimiento:</strong> Beca a la Excelencia Académica por promedio.",
                "<strong>Reconocimiento:</strong> Diploma de aprovechamiento por cuatrimestre.",
                "<strong>Reconocimiento:</strong> Mención honorífica en proyectos o concursos.",
                "<strong>Reconocimiento:</strong> Apoyos para movilidad estudiantil (intercambios)."
        };
    }
// ...

    @Override
    protected int getRecyclerViewId() {
        return R.id.reconocimientos_recycler_view; //
    }

    // --- NUEVO MÉTODO IMPLEMENTADO ---
    @Override
    protected String getItemType() {
        return "Reconocimiento"; // Respuesta correcta
    }
}
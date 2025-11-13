package com.example.reglamentoupp;

public class DerechosFragment extends BaseReglamentoFragment {

    @Override
    protected int getLayoutRes() {
        return R.layout.fragment_derechos; //
    }

    @Override
    protected String[] getItems() {
        return new String[]{
                "<strong>Artículo 3 (I):</strong> Cursar los estudios de conformidad con los planes y programas vigentes.",
                "<strong>Artículo 3 (III):</strong> Recibir orientación e información de las Direcciones de Programas Académicos.",
                "<strong>Artículo 3 (V):</strong> Recibir información clara sobre los criterios y formas de evaluación.",
                "<strong>Artículo 3 (VII):</strong> Conocer oportunamente el resultado de las evaluaciones que presenten.",
                "<strong>Artículo 3 (VIII):</strong> Obtener su número de matrícula y credencial al inscribirse.",
                "<strong>Artículo 3 (XIII):</strong> Recibir asesorías y tutorías del personal académico."
        };
    }

    @Override
    protected int getRecyclerViewId() {
        return R.id.derechos_recycler_view; //
    }

    // --- NUEVO MÉTODO IMPLEMENTADO ---
    @Override
    protected String getItemType() {
        return "Derecho"; // Esta es la respuesta correcta para el quiz
    }

}
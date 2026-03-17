package com.example.reglamentoupp;

public class DerechosFragment extends BaseReglamentoFragment {

    @Override
    protected int getLayoutRes() {
        return R.layout.fragment_derechos;
    }

    @Override
    protected ReglamentoItem[] getItems() {
        return new ReglamentoItem[]{
                // Asocia cada texto con su audio correspondiente
                new ReglamentoItem(
                        "<strong>Artículo 3 (I):</strong> Cursar los estudios de conformidad con los planes y programas vigentes.",
                        R.raw.regla_art3_i
                ),
                new ReglamentoItem(
                        "<strong>Artículo 3 (III):</strong> Recibir orientación e información de las Direcciones de Programas Académicos.",
                        R.raw.regla_art3_iii
                ),
                new ReglamentoItem(
                        "<strong>Artículo 3 (V):</strong> Recibir información clara sobre los criterios y formas de evaluación.",
                        R.raw.regla_art3_v
                ),
                new ReglamentoItem(
                        "<strong>Artículo 3 (VII):</strong> Conocer oportunamente el resultado de las evaluaciones que presenten.",
                        R.raw.regla_art3_vii
                ),
                new ReglamentoItem(
                        "<strong>Artículo 3 (VIII):</strong> Obtener su número de matrícula y credencial al inscribirse.",
                        R.raw.regla_art3_viii
                ),
                new ReglamentoItem(
                        "<strong>Artículo 3 (XIII):</strong> Recibir asesorías y tutorías del personal académico.",
                        R.raw.regla_art3_xiii
                )
        };
    }

    @Override
    protected int getRecyclerViewId() {
        return R.id.derechos_recycler_view;
    }

    @Override
    protected String getItemType() {
        return "Derecho";
    }
}
package com.example.reglamentoupp;

public class DerechosFragment extends BaseReglamentoFragment {

    @Override
    protected int getLayoutRes() {
        return R.layout.fragment_derechos;
    }

    @Override
    protected ReglamentoItem[] getItems() {
        return new ReglamentoItem[]{
                // Usamos <b> para poner en negrita el título y <br> para saltar a la siguiente línea
                new ReglamentoItem(
                        "<b>Artículo 3 (Fracción I)</b><br>Cursar los estudios de conformidad con los planes y programas vigentes.",
                        R.raw.regla_art3_i
                ),
                new ReglamentoItem(
                        "<b>Artículo 3 (Fracción III)</b><br>Recibir orientación e información de las Direcciones de Programas Académicos.",
                        R.raw.regla_art3_iii
                ),
                new ReglamentoItem(
                        "<b>Artículo 3 (Fracción V)</b><br>Recibir información clara sobre los criterios y formas de evaluación.",
                        R.raw.regla_art3_v
                ),
                new ReglamentoItem(
                        "<b>Artículo 3 (Fracción VII)</b><br>Conocer oportunamente el resultado de las evaluaciones que presenten.",
                        R.raw.regla_art3_vii
                ),
                new ReglamentoItem(
                        "<b>Artículo 3 (Fracción VIII)</b><br>Obtener su número de matrícula y credencial oficial al inscribirse.",
                        R.raw.regla_art3_viii
                ),
                new ReglamentoItem(
                        "<b>Artículo 3 (Fracción XIII)</b><br>Recibir asesorías y tutorías por parte del personal académico.",
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
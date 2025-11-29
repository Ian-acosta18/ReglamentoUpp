package com.example.reglamentoupp;

public class ObligacionesFragment extends BaseReglamentoFragment {

    @Override
    protected int getLayoutRes() {
        return R.layout.fragment_obligaciones;
    }

    @Override
    protected ReglamentoItem[] getItems() {
        return new ReglamentoItem[]{
                // Artículo 5 (I) -> obli_art5_i
                new ReglamentoItem(
                        "<strong>Artículo 5 (I):</strong> Ser responsables de su proceso de formación profesional.",
                        R.raw.obli_art5_i
                ),
                // Artículo 5 (II) -> obli_art5_ii
                new ReglamentoItem(
                        "<strong>Artículo 5 (II):</strong> Observar y respetar las disposiciones de la legislación universitaria.",
                        R.raw.obli_art5_ii
                ),
                // Artículo 5 (V) -> obli_art5_v
                new ReglamentoItem(
                        "<strong>Artículo 5 (V):</strong> Asistir puntualmente y participar en las actividades académicas.",
                        R.raw.obli_art5_v
                ),
                // Artículo 5 (X) -> obli_art5_x
                new ReglamentoItem(
                        "<strong>Artículo 5 (X):</strong> Utilizar y conservar de manera responsable los espacios y materiales.",
                        R.raw.obli_art5_x
                ),
                // Artículo 5 (XI) -> obli_art5_xi
                new ReglamentoItem(
                        "<strong>Artículo 5 (XI):</strong> Reparar los daños que por negligencia o intencionalmente ocasionen.",
                        R.raw.obli_art5_xi
                ),
                // Artículo 5 (XII) -> obli_art5_xii
                new ReglamentoItem(
                        "<strong>Artículo 5 (XII):</strong> Mostrar la credencial que los identifica como alumnos al ingresar.",
                        R.raw.obli_art5_xii
                )
        };
    }

    @Override
    protected int getRecyclerViewId() {
        return R.id.obligaciones_recycler_view;
    }

    @Override
    protected String getItemType() {
        return "Obligación";
    }
}
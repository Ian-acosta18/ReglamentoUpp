package com.example.reglamentoupp;

public class ObligacionesFragment extends BaseReglamentoFragment {

    @Override
    protected int getLayoutRes() {
        return R.layout.fragment_obligaciones;
    }

    @Override
    protected ReglamentoItem[] getItems() {
        return new ReglamentoItem[]{
                new ReglamentoItem(
                        "<b>Artículo 5 (Fracción I)</b><br>Ser responsables de su propio proceso de formación profesional.",
                        R.raw.obli_art5_i
                ),
                new ReglamentoItem(
                        "<b>Artículo 5 (Fracción II)</b><br>Observar y respetar todas las disposiciones de la legislación universitaria.",
                        R.raw.obli_art5_ii
                ),
                new ReglamentoItem(
                        "<b>Artículo 5 (Fracción V)</b><br>Asistir puntualmente y participar activamente en las actividades académicas.",
                        R.raw.obli_art5_v
                ),
                new ReglamentoItem(
                        "<b>Artículo 5 (Fracción X)</b><br>Utilizar y conservar de manera responsable los espacios y materiales de la institución.",
                        R.raw.obli_art5_x
                ),
                new ReglamentoItem(
                        "<b>Artículo 5 (Fracción XI)</b><br>Reparar los daños que, por negligencia o de forma intencional, ocasionen a las instalaciones.",
                        R.raw.obli_art5_xi
                ),
                new ReglamentoItem(
                        "<b>Artículo 5 (Fracción XII)</b><br>Mostrar la credencial vigente que los identifica como alumnos al ingresar al campus.",
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
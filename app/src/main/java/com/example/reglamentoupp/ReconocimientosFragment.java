package com.example.reglamentoupp;

public class ReconocimientosFragment extends BaseReglamentoFragment {

    @Override
    protected int getLayoutRes() {
        return R.layout.fragment_reconocimientos;
    }

    @Override
    protected ReglamentoItem[] getItems() {
        return new ReglamentoItem[]{
                // Beca -> rec_beca
                new ReglamentoItem(
                        "<strong>Reconocimiento:</strong> Beca a la Excelencia Académica por promedio.",
                        R.raw.rec_beca
                ),
                // Diploma -> rec_diploma
                new ReglamentoItem(
                        "<strong>Reconocimiento:</strong> Diploma de aprovechamiento por cuatrimestre.",
                        R.raw.rec_diploma
                ),
                // Mención -> rec_mencion
                new ReglamentoItem(
                        "<strong>Reconocimiento:</strong> Mención honorífica en proyectos o concursos.",
                        R.raw.rec_mencion
                ),
                // Movilidad -> rec_movilidad
                new ReglamentoItem(
                        "<strong>Reconocimiento:</strong> Apoyos para movilidad estudiantil (intercambios).",
                        R.raw.rec_movilidad
                )
        };
    }

    @Override
    protected int getRecyclerViewId() {
        return R.id.reconocimientos_recycler_view;
    }

    @Override
    protected String getItemType() {
        return "Reconocimiento";
    }
}
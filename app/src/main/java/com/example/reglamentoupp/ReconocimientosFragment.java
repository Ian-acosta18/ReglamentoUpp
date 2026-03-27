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
                        "<strong>💎 Beca de Excelencia:</strong> Exención del pago de colegiatura." +
                                "<br><br><i>Para alumnos con los mejores promedios.</i>",
                        R.raw.rec_beca
                ),
                // Diploma -> rec_diploma
                new ReglamentoItem(
                        "<strong>📜 Diploma al Mérito:</strong> Reconocimiento público y oficial." +
                                "<br><br><i>Entregado anualmente a estudiantes destacados.</i>",
                        R.raw.rec_diploma
                ),
                // Mencion -> rec_mencion
                new ReglamentoItem(
                        "<strong>🏆 Mención Honorífica:</strong> Máxima distinción en titulación." +
                                "<br><br><i>Se otorga por una trayectoria intachable y excelencia académica.</i>",
                        R.raw.rec_mencion
                ),
                // Movilidad -> rec_movilidad
                new ReglamentoItem(
                        "<strong>✈️ Movilidad Estudiantil:</strong> Programa de intercambio." +
                                "<br><br><i>Oportunidad de cursar un periodo en el extranjero o en otro estado.</i>",
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
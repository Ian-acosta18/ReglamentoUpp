package com.example.reglamentoupp;

public class SancionesFragment extends BaseReglamentoFragment {

    @Override
    protected int getLayoutRes() {
        return R.layout.fragment_sanciones;
    }

    @Override
    protected ReglamentoItem[] getItems() {
        return new ReglamentoItem[]{
                // Sanción (I) -> san_i
                new ReglamentoItem(
                        "<strong>Sanción (I):</strong> Amonestación escrita con copia al expediente.",
                        R.raw.san_i
                ),
                // Sanción (II) -> san_ii
                new ReglamentoItem(
                        "<strong>Sanción (II):</strong> Reposición o pago del material o bien dañado.",
                        R.raw.san_ii
                ),
                // Sanción (III) -> san_iii
                new ReglamentoItem(
                        "<strong>Sanción (III):</strong> Suspensión temporal de derechos (según la gravedad).",
                        R.raw.san_iii
                ),
                // Sanción (IV) -> san_iv
                new ReglamentoItem(
                        "<strong>Sanción (IV):</strong> Expulsión definitiva de la Universidad.",
                        R.raw.san_iv
                ),
                // Artículo 35 -> san_art35
                new ReglamentoItem(
                        "<strong>Artículo 35:</strong> Inasistencia colectiva a clases sin causa justificada.",
                        R.raw.san_art35
                )
        };
    }

    @Override
    protected int getRecyclerViewId() {
        return R.id.sanciones_recycler_view;
    }

    @Override
    protected String getItemType() {
        return "Sanción";
    }
}
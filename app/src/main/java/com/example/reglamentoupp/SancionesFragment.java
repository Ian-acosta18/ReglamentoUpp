package com.example.reglamentoupp;

public class SancionesFragment extends BaseReglamentoFragment {

    @Override
    protected int getLayoutRes() {
        return R.layout.fragment_sanciones; //
    }

    // ... (dentro de la clase SancionesFragment)
    @Override
    protected String[] getItems() {
        return new String[]{
                "<strong>Sanción (I):</strong> Amonestación escrita con copia al expediente.",
                "<strong>Sanción (II):</strong> Reposición o pago del material o bien dañado.",
                "<strong>Sanción (III):</strong> Suspensión temporal de derechos (según la gravedad).",
                "<strong>Sanción (IV):</strong> Expulsión definitiva de la Universidad.",
                "<strong>Artículo 35:</strong> Inasistencia colectiva a clases sin causa justificada."
        };
    }
// ...

    @Override
    protected int getRecyclerViewId() {
        return R.id.sanciones_recycler_view; //
    }

    // --- NUEVO MÉTODO IMPLEMENTADO ---
    @Override
    protected String getItemType() {
        return "Sanción"; // Respuesta correcta
    }
}
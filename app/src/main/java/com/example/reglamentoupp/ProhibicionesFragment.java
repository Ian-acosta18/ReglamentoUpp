package com.example.reglamentoupp;

public class ProhibicionesFragment extends BaseReglamentoFragment {

    @Override
    protected int getLayoutRes() {
        return R.layout.fragment_prohibiciones; //
    }

    @Override
    protected String[] getItems() {
        return new String[]{
                "<strong>Artículo 8 (I):</strong> Fumar en las instalaciones (excepto áreas autorizadas).",
                "<strong>Artículo 8 (II):</strong> Practicar juegos de azar y/o apuestas dentro de la Universidad.",
                "<strong>Artículo 8 (III):</strong> Efectuar juegos bruscos y peleas dentro de la Universidad.",
                "<strong>Artículo 8 (IV):</strong> Consumir alimentos y bebidas dentro de salón de clases, biblioteca o laboratorios.",
                "<strong>Artículo 8 (VII):</strong> Comercializar bienes o servicios dentro de las instalaciones.",
                "<strong>Artículo 8 (VIII):</strong> Promover actividades político partidistas."
        };
    }
    @Override
    protected int getRecyclerViewId() {
        return R.id.prohibiciones_recycler_view; //
    }

    // --- NUEVO MÉTODO IMPLEMENTADO ---
    @Override
    protected String getItemType() {
        return "Prohibición"; // Respuesta correcta
    }
}
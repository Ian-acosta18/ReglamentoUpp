package com.example.reglamentoupp;

public class ProhibicionesFragment extends BaseReglamentoFragment {

    @Override
    protected int getLayoutRes() {
        return R.layout.fragment_prohibiciones;
    }

    @Override
    protected ReglamentoItem[] getItems() {
        return new ReglamentoItem[]{
                // Artículo 8 (I) -> pro_art8_i
                new ReglamentoItem(
                        "<strong>Artículo 8 (I):</strong> Fumar en las instalaciones (excepto áreas autorizadas).",
                        R.raw.pro_art8_i
                ),
                // Artículo 8 (II) -> pro_art8_ii
                new ReglamentoItem(
                        "<strong>Artículo 8 (II):</strong> Practicar juegos de azar y/o apuestas dentro de la Universidad.",
                        R.raw.pro_art8_ii
                ),
                // Artículo 8 (III) -> pro_art8_iii
                new ReglamentoItem(
                        "<strong>Artículo 8 (III):</strong> Efectuar juegos bruscos y peleas dentro de la Universidad.",
                        R.raw.pro_art8_iii
                ),
                // Artículo 8 (IV) -> pro_art8_iv
                new ReglamentoItem(
                        "<strong>Artículo 8 (IV):</strong> Consumir alimentos y bebidas dentro de salón de clases, biblioteca o laboratorios.",
                        R.raw.pro_art8_iv
                ),
                // Artículo 8 (VII) -> pro_art8_vii
                new ReglamentoItem(
                        "<strong>Artículo 8 (VII):</strong> Comercializar bienes o servicios dentro de las instalaciones.",
                        R.raw.pro_art8_vii
                ),
                // Artículo 8 (VIII) -> pro_art8_viii
                new ReglamentoItem(
                        "<strong>Artículo 8 (VIII):</strong> Promover actividades político partidistas.",
                        R.raw.pro_art8_viii
                )
        };
    }

    @Override
    protected int getRecyclerViewId() {
        return R.id.prohibiciones_recycler_view;
    }

    @Override
    protected String getItemType() {
        return "Prohibición";
    }
}
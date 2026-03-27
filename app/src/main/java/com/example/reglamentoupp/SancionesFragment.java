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
                        "<strong>Sanción (I):</strong> Amonestación escrita con copia al expediente." +
                                "<br><br><i>Ejemplo: Cometer una falta disciplinaria leve, como dirigirse con falta de respeto a un compañero o profesor.</i>",
                        R.raw.san_i
                ),
                // Sanción (II) -> san_ii
                new ReglamentoItem(
                        "<strong>Sanción (II):</strong> Reposición o pago del material o bien dañado." +
                                "<br><br><i>Ejemplo: Dañar intencionalmente un proyector, romper un microscopio por jugar en el laboratorio o perder un libro de la biblioteca.</i>",
                        R.raw.san_ii
                ),
                // Sanción (III) -> san_iii
                new ReglamentoItem(
                        "<strong>Sanción (III):</strong> Suspensión temporal de derechos (según la gravedad)." +
                                "<br><br><i>Ejemplo: Ser sorprendido copiando en un examen, entregando un trabajo con plagio, o alterando el orden en la institución.</i>",
                        R.raw.san_iii
                ),
                // Sanción (IV) -> san_iv
                new ReglamentoItem(
                        "<strong>Sanción (IV):</strong> Expulsión definitiva de la Universidad." +
                                "<br><br><i>Ejemplo: Falsificar documentos (como recetas médicas o firmas), pelear a golpes dentro del campus o consumir bebidas alcohólicas en la universidad.</i>",
                        R.raw.san_iv
                ),
                // Artículo 35 -> san_art35
                new ReglamentoItem(
                        "<strong>Artículo 35:</strong> Inasistencia colectiva a clases sin causa justificada." +
                                "<br><br><i>Ejemplo: Ponerse de acuerdo todo el salón para no entrar a una clase de forma intencional sin un permiso válido.</i>",
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
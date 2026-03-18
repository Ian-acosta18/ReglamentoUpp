package com.example.reglamentoupp;

import android.text.Html;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.card.MaterialCardView;

public class ReglamentoAdapter extends RecyclerView.Adapter<ReglamentoAdapter.ViewHolder> {

    private final ReglamentoItem[] items;
    private final String itemType;
    private final BaseReglamentoFragment fragment;
    private final BaseReglamentoFragment.ReglamentoInteractionListener listener;

    // Almacena qué reglas ya fueron escuchadas
    private SparseBooleanArray itemsLeidos = new SparseBooleanArray();

    public ReglamentoAdapter(ReglamentoItem[] items, String itemType,
                             BaseReglamentoFragment fragment,
                             BaseReglamentoFragment.ReglamentoInteractionListener listener) {
        this.items = items;
        this.itemType = itemType;
        this.fragment = fragment;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_reglamento, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ReglamentoItem item = items[position];

        holder.textView.setText(Html.fromHtml(item.getTexto(), Html.FROM_HTML_MODE_COMPACT));

        // Asignar colores por categoría
        int colorTint, colorBg;
        switch (itemType) {
            case "Derecho":
                colorTint = R.color.category_derechos;
                colorBg = R.color.category_derechos_bg;
                break;
            case "Obligación":
                colorTint = R.color.category_obligaciones;
                colorBg = R.color.category_obligaciones_bg;
                break;
            case "Prohibición":
                colorTint = R.color.category_prohibiciones;
                colorBg = R.color.category_prohibiciones_bg;
                break;
            case "Sanción":
                colorTint = R.color.category_sanciones;
                colorBg = R.color.category_sanciones_bg;
                break;
            default:
                colorTint = R.color.category_reconocimientos;
                colorBg = R.color.category_reconocimientos_bg;
                break;
        }

        // Aplicamos el color al fondo cuadrado del ícono y al propio ícono
        holder.cardIconBg.setCardBackgroundColor(ContextCompat.getColor(holder.itemView.getContext(), colorBg));
        holder.audioIcon.setColorFilter(ContextCompat.getColor(holder.itemView.getContext(), colorTint));

        // Lógica de Leído / No Leído
        boolean isLeido = itemsLeidos.get(position, false);
        if (isLeido) {
            holder.ivCheck.setColorFilter(ContextCompat.getColor(holder.itemView.getContext(), R.color.status_green));
            holder.textView.setAlpha(0.6f); // Atenuar el texto si ya lo leyó
        } else {
            holder.ivCheck.setColorFilter(ContextCompat.getColor(holder.itemView.getContext(), R.color.level_locked_text));
            holder.textView.setAlpha(1.0f);
        }

        // Animación suave de entrada
        holder.itemView.setAnimation(AnimationUtils.loadAnimation(holder.itemView.getContext(), R.anim.fade_in));

        // Evento al tocar la tarjeta
        holder.itemView.setOnClickListener(v -> {
            // Animación de rebote al hacer click
            v.animate().scaleX(0.97f).scaleY(0.97f).setDuration(100).withEndAction(() -> {
                v.animate().scaleX(1f).scaleY(1f).setDuration(100).start();
            }).start();

            // Marcar como leído
            if (!isLeido) {
                itemsLeidos.put(position, true);
                notifyItemChanged(position);
            }

            // Reproducir audio
            if (fragment != null && item.getAudioResId() != 0) {
                fragment.playAudio(item.getAudioResId());
            }
        });
    }

    @Override
    public int getItemCount() { return items.length; }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView textView;
        MaterialCardView cardIconBg;
        ImageView audioIcon;
        ImageView ivCheck;

        ViewHolder(View itemView) {
            super(itemView);
            textView = itemView.findViewById(R.id.tv_item_text);
            cardIconBg = itemView.findViewById(R.id.card_icon_bg);
            audioIcon = itemView.findViewById(R.id.iv_audio_icon);
            ivCheck = itemView.findViewById(R.id.iv_status_check);
        }
    }
}
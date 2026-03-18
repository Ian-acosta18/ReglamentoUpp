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
    private final SparseBooleanArray itemsLeidos = new SparseBooleanArray();

    public ReglamentoAdapter(ReglamentoItem[] items, String itemType, BaseReglamentoFragment fragment, BaseReglamentoFragment.ReglamentoInteractionListener listener) {
        this.items = items;
        this.itemType = itemType;
        this.fragment = fragment;
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

        // Configuración de colores según tipo
        int colorMain, colorBg;
        switch (itemType) {
            case "Derecho": colorMain = R.color.category_derechos; colorBg = R.color.category_derechos_bg; break;
            case "Obligación": colorMain = R.color.category_obligaciones; colorBg = R.color.category_obligaciones_bg; break;
            case "Prohibición": colorMain = R.color.category_prohibiciones; colorBg = R.color.category_prohibiciones_bg; break;
            default: colorMain = R.color.category_reconocimientos; colorBg = R.color.category_reconocimientos_bg; break;
        }

        holder.cardIconBg.setCardBackgroundColor(ContextCompat.getColor(holder.itemView.getContext(), colorBg));
        holder.audioIcon.setColorFilter(ContextCompat.getColor(holder.itemView.getContext(), colorMain));

        // Estado Leído
        if (itemsLeidos.get(position)) {
            holder.ivCheck.setColorFilter(ContextCompat.getColor(holder.itemView.getContext(), R.color.status_green));
            holder.itemView.setAlpha(0.7f);
        }

        holder.itemView.setOnClickListener(v -> {
            // Animación de escala
            v.animate().scaleX(0.96f).scaleY(0.96f).setDuration(100).withEndAction(() -> {
                v.animate().scaleX(1f).scaleY(1f).setDuration(100).start();
                itemsLeidos.put(position, true);
                notifyItemChanged(position);
                if (fragment != null) fragment.playAudio(item.getAudioResId());
            }).start();
        });
    }

    @Override
    public int getItemCount() { return items.length; }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView textView;
        MaterialCardView cardIconBg;
        ImageView audioIcon, ivCheck;
        ViewHolder(View v) {
            super(v);
            textView = v.findViewById(R.id.tv_item_text);
            cardIconBg = v.findViewById(R.id.card_icon_bg);
            audioIcon = v.findViewById(R.id.iv_audio_icon);
            ivCheck = v.findViewById(R.id.iv_status_check);
        }
    }
}
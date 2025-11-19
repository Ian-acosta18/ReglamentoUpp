package com.example.reglamentoupp;

import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils; // Importar animación
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.card.MaterialCardView;
import com.google.android.material.chip.Chip;

public class ReglamentoAdapter extends RecyclerView.Adapter<ReglamentoAdapter.ViewHolder> {

    private final String[] mItems;
    private final String mItemType;
    private final BaseReglamentoFragment.ReglamentoInteractionListener mListener;

    public ReglamentoAdapter(String[] items, String itemType, BaseReglamentoFragment.ReglamentoInteractionListener listener) {
        mItems = items;
        mItemType = itemType;
        mListener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_list, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        String currentItemText = mItems[position];

        holder.textView.setText(Html.fromHtml(currentItemText, Html.FROM_HTML_MODE_LEGACY));

        // 1. Clic en la tarjeta principal
        holder.cardRoot.setOnClickListener(v -> {
            if (mListener != null) {
                mListener.onQuizClick(currentItemText, mItemType);
            }
        });

        // 2. Clic en el botón "Analizar Caso"
        holder.btnCaseStudy.setOnClickListener(v -> {
            if (mListener != null) {
                mListener.onCaseStudyClick(currentItemText, mItemType);
            }
        });

        // Iconos según el tipo
        int iconRes = getIconForItemType(mItemType);
        if (iconRes != 0) {
            holder.itemIcon.setImageResource(iconRes);
        }

        // --- ANIMACIÓN DE ENTRADA ---
        holder.cardRoot.startAnimation(
                AnimationUtils.loadAnimation(holder.itemView.getContext(), R.anim.item_animation_fall_down)
        );
    }

    private int getIconForItemType(String itemType) {
        switch (itemType) {
            case "Derecho":
                return R.drawable.ic_derechos;
            case "Obligación":
                return R.drawable.ic_obligaciones;
            case "Prohibición":
                return R.drawable.ic_prohibiciones;
            case "Sanción":
                return R.drawable.ic_sanciones;
            case "Reconocimiento":
                return R.drawable.ic_reconocimientos;
            default:
                return R.drawable.ic_check_circle;
        }
    }

    @Override
    public int getItemCount() {
        return mItems.length;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public final TextView textView;
        public final MaterialCardView cardRoot;
        public final Chip btnCaseStudy;
        public final ImageView itemIcon;

        public ViewHolder(View view) {
            super(view);
            textView = view.findViewById(android.R.id.text1);
            cardRoot = view.findViewById(R.id.card_root);
            btnCaseStudy = view.findViewById(R.id.btn_case_study);
            itemIcon = view.findViewById(R.id.item_icon);
        }
    }
}
package com.example.reglamentoupp;

import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
// Importamos Chip en lugar de Button
import com.google.android.material.chip.Chip;
import com.google.android.material.card.MaterialCardView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class ReglamentoAdapter extends RecyclerView.Adapter<ReglamentoAdapter.ViewHolder> {
    // ... (variables sin cambios)

    // ... (constructor sin cambios)

    // ... (onCreateViewHolder sin cambios)

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        String currentItemText = mItems[position];
        holder.textView.setText(Html.fromHtml(currentItemText, Html.FROM_HTML_MODE_LEGACY));

        // 1. Clic en la tarjeta principal (para el Quiz)
        holder.cardRoot.setOnClickListener(v -> {
            if (mListener != null) {
                mListener.onQuizClick(currentItemText, mItemType);
            }
        });

        // 2. Clic en el Chip "Analizar Caso"
        holder.btnCaseStudy.setOnClickListener(v -> {
            if (mListener != null) {
                mListener.onCaseStudyClick(currentItemText, mItemType);
            }
        });

        // ... (resto de onBindViewHolder)
    }

    // ... (getIconForItemType y getItemCount sin cambios)

    // --- ViewHolder actualizado para encontrar el Chip ---
    public static class ViewHolder extends RecyclerView.ViewHolder {
        public final TextView textView;
        public final MaterialCardView cardRoot;
        public final Chip btnCaseStudy; // <-- Cambiado de Button a Chip
        public final ImageView itemIcon;

        public ViewHolder(View view) {
            super(view);
            textView = view.findViewById(android.R.id.text1);
            cardRoot = view.findViewById(R.id.card_root);
            btnCaseStudy = view.findViewById(R.id.btn_case_study); // <-- ID sigue siendo el mismo
            itemIcon = view.findViewById(R.id.item_icon);
        }
    }
}
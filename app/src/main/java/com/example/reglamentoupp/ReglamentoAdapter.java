package com.example.reglamentoupp;

import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;

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

        // Configurar Título y Descripción
        holder.tvTitle.setText(mItemType);
        holder.tvDescription.setText(Html.fromHtml(currentItemText, Html.FROM_HTML_MODE_LEGACY));

        // Icono
        int iconRes = getIconForItemType(mItemType);
        if (iconRes != 0) {
            holder.itemIcon.setImageResource(iconRes);
        }

        // Animaciones y Clics
        final Animation pressAnimation = AnimationUtils.loadAnimation(holder.itemView.getContext(), R.anim.scale_press);

        holder.cardRoot.setOnClickListener(v -> {
            v.startAnimation(pressAnimation);
            v.postDelayed(() -> {
                if (mListener != null) mListener.onQuizClick(currentItemText, mItemType);
            }, 150);
        });

        holder.btnCaseStudy.setOnClickListener(v -> {
            v.startAnimation(pressAnimation);
            v.postDelayed(() -> {
                if (mListener != null) mListener.onCaseStudyClick(currentItemText, mItemType);
            }, 150);
        });

        holder.cardRoot.startAnimation(
                AnimationUtils.loadAnimation(holder.itemView.getContext(), R.anim.item_animation_fall_down)
        );
    }

    private int getIconForItemType(String itemType) {
        switch (itemType) {
            case "Derecho": return R.drawable.ic_derechos;
            case "Obligación": return R.drawable.ic_obligaciones;
            case "Prohibición": return R.drawable.ic_prohibiciones;
            case "Sanción": return R.drawable.ic_sanciones;
            case "Reconocimiento": return R.drawable.ic_reconocimientos;
            default: return R.drawable.ic_check_circle;
        }
    }

    @Override
    public int getItemCount() {
        return mItems.length;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public final TextView tvTitle;
        public final TextView tvDescription;
        public final MaterialCardView cardRoot;
        public final MaterialButton btnCaseStudy;
        public final ImageView itemIcon;

        public ViewHolder(View view) {
            super(view);
            tvTitle = view.findViewById(R.id.tv_card_title);
            // CORRECCIÓN: Usamos el nuevo ID local
            tvDescription = view.findViewById(R.id.tv_item_description);
            cardRoot = view.findViewById(R.id.card_root);
            btnCaseStudy = view.findViewById(R.id.btn_case_study);
            itemIcon = view.findViewById(R.id.item_icon);
        }
    }
}
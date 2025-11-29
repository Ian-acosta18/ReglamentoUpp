package com.example.reglamentoupp;

import android.content.Context;
import android.content.res.ColorStateList;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;

public class ReglamentoAdapter extends RecyclerView.Adapter<ReglamentoAdapter.ViewHolder> {

    private final ReglamentoItem[] mItems;
    private final String mItemType;
    private final BaseReglamentoFragment mFragment; // Referencia para reproducir audio
    private final BaseReglamentoFragment.ReglamentoInteractionListener mListener;

    public ReglamentoAdapter(ReglamentoItem[] items, String itemType, BaseReglamentoFragment fragment, BaseReglamentoFragment.ReglamentoInteractionListener listener) {
        mItems = items;
        mItemType = itemType;
        mFragment = fragment;
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
        ReglamentoItem item = mItems[position];
        Context context = holder.itemView.getContext();

        // 1. Configurar Textos
        holder.tvTitle.setText(mItemType.toUpperCase());
        holder.tvDescription.setText(Html.fromHtml(item.getTexto(), Html.FROM_HTML_MODE_LEGACY));

        // 2. Colores e Iconos
        int iconRes = getIconForItemType(mItemType);
        int colorRes = getColorForItemType(mItemType);
        int colorInt = ContextCompat.getColor(context, colorRes);
        int bgTintInt = ContextCompat.getColor(context, getBgTintForItemType(mItemType));

        // Configurar icono visualmente
        if (iconRes != 0) {
            holder.itemIcon.setImageResource(iconRes);
            holder.itemIcon.clearColorFilter();

            // Opcional: Si también quieres que el audio suene al tocar el ícono pequeño
            holder.itemIcon.setOnClickListener(v -> {
                if (item.getAudioResId() != 0) {
                    mFragment.playAudio(item.getAudioResId());
                }
            });
        }

        if (holder.iconContainer != null) {
            holder.iconContainer.setBackgroundTintList(ColorStateList.valueOf(bgTintInt));
        }
        holder.tvTitle.setTextColor(colorInt);

        // Estilos del botón de Caso Práctico
        holder.btnCaseStudy.setTextColor(colorInt);
        holder.btnCaseStudy.setIconTint(ColorStateList.valueOf(colorInt));
        holder.btnCaseStudy.setBackgroundTintList(ColorStateList.valueOf(bgTintInt));

        // 3. ANIMACIONES Y CLICS
        final Animation pressAnimation = AnimationUtils.loadAnimation(context, R.anim.scale_press);

        // --- AQUI ESTA EL CAMBIO: ---

        // A) CLIC EN LA TARJETA (Quiz): Solo navega, NO reproduce audio.
        holder.cardRoot.setOnClickListener(v -> {
            v.startAnimation(pressAnimation);
            v.postDelayed(() -> {
                if (mListener != null) mListener.onQuizClick(item.getTexto(), mItemType);
            }, 150);
        });

        // B) CLIC EN BOTÓN "CASO PRÁCTICO": Reproduce audio Y navega.
        holder.btnCaseStudy.setOnClickListener(v -> {
            v.startAnimation(pressAnimation);

            // 1. REPRODUCIR AUDIO AQUÍ
            if (item.getAudioResId() != 0) {
                mFragment.playAudio(item.getAudioResId());
            }

            // 2. ABRIR EL CASO PRÁCTICO
            v.postDelayed(() -> {
                if (mListener != null) mListener.onCaseStudyClick(item.getTexto(), mItemType);
            }, 150);
        });

        holder.cardRoot.startAnimation(
                AnimationUtils.loadAnimation(context, R.anim.item_animation_fall_down)
        );
    }

    // --- Métodos auxiliares (sin cambios) ---
    private int getIconForItemType(String itemType) {
        if (itemType == null) return R.drawable.ic_check_circle;
        String tipo = itemType.toLowerCase().trim();
        if (tipo.contains("derecho")) return R.drawable.ic_derechos;
        if (tipo.contains("obligaci")) return R.drawable.ic_obligaciones;
        if (tipo.contains("prohibici")) return R.drawable.ic_prohibiciones;
        if (tipo.contains("sanci")) return R.drawable.ic_sanciones;
        if (tipo.contains("reconocimiento")) return R.drawable.ic_reconocimientos;
        return R.drawable.ic_check_circle;
    }

    private int getColorForItemType(String itemType) {
        if (itemType == null) return R.color.upp_primary;
        String tipo = itemType.toLowerCase().trim();
        if (tipo.contains("derecho")) return R.color.upp_primary;
        if (tipo.contains("obligaci")) return R.color.status_green;
        if (tipo.contains("prohibici")) return R.color.game_fail;
        if (tipo.contains("sanci")) return R.color.upp_secondary;
        if (tipo.contains("reconocimiento")) return R.color.upp_accent;
        return R.color.upp_primary;
    }

    private int getBgTintForItemType(String itemType) {
        if (itemType == null) return R.color.app_bg;
        String tipo = itemType.toLowerCase().trim();
        if (tipo.contains("derecho")) return R.color.app_bg;
        if (tipo.contains("obligaci")) return R.color.status_green_bg;
        if (tipo.contains("prohibici")) return R.color.game_fail_bg;
        if (tipo.contains("sanci")) return R.color.game_fail_bg;
        if (tipo.contains("reconocimiento")) return R.color.game_success_bg;
        return R.color.app_bg;
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
        public final FrameLayout iconContainer;

        public ViewHolder(View view) {
            super(view);
            tvTitle = view.findViewById(R.id.tv_card_title);
            tvDescription = view.findViewById(R.id.tv_item_description);
            cardRoot = view.findViewById(R.id.card_root);
            btnCaseStudy = view.findViewById(R.id.btn_case_study);
            itemIcon = view.findViewById(R.id.item_icon);
            iconContainer = view.findViewById(R.id.icon_container);
        }
    }
}

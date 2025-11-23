package com.example.reglamentoupp;

import android.content.Context;
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
        Context context = holder.itemView.getContext();

        // 1. Configurar Textos
        holder.tvTitle.setText(mItemType.toUpperCase()); // Convertir a mayúsculas para estilo "etiqueta"
        // Renderizar HTML para que las negritas (<strong>) se vean bien
        holder.tvDescription.setText(Html.fromHtml(currentItemText, Html.FROM_HTML_MODE_LEGACY));

        // 2. Configurar Iconos y Colores dinámicos según el tipo
        int iconRes = getIconForItemType(mItemType);
        int colorRes = getColorForItemType(mItemType);
        int colorInt = ContextCompat.getColor(context, colorRes);
        int bgTintInt = ContextCompat.getColor(context, getBgTintForItemType(mItemType));

        if (iconRes != 0) {
            holder.itemIcon.setImageResource(iconRes);
            holder.itemIcon.setColorFilter(colorInt); // Tintar el icono del color del tema
        }

        // Tintar el fondo circular del icono
        holder.iconContainer.setBackgroundTintList(android.content.res.ColorStateList.valueOf(bgTintInt));
        // Colorear el título
        holder.tvTitle.setTextColor(colorInt);
        // Colorear icono y texto del botón ligeramente
        holder.btnCaseStudy.setTextColor(colorInt);
        holder.btnCaseStudy.setIconTint(android.content.res.ColorStateList.valueOf(colorInt));
        // Fondo muy suave para el botón
        holder.btnCaseStudy.setBackgroundTintList(android.content.res.ColorStateList.valueOf(bgTintInt));


        // 3. Animaciones de clic
        final Animation pressAnimation = AnimationUtils.loadAnimation(context, R.anim.scale_press);

        // Listener en toda la tarjeta (para Quiz)
        holder.cardRoot.setOnClickListener(v -> {
            v.startAnimation(pressAnimation);
            v.postDelayed(() -> {
                if (mListener != null) mListener.onQuizClick(currentItemText, mItemType);
            }, 150);
        });

        // Listener en el botón (para Caso Práctico)
        holder.btnCaseStudy.setOnClickListener(v -> {
            v.startAnimation(pressAnimation);
            v.postDelayed(() -> {
                if (mListener != null) mListener.onCaseStudyClick(currentItemText, mItemType);
            }, 150);
        });

        // Animación de entrada (Caída)
        holder.cardRoot.startAnimation(
                AnimationUtils.loadAnimation(context, R.anim.item_animation_fall_down)
        );
    }

    // Método auxiliar para obtener iconos
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

    // Método auxiliar para colores principales (Texto e Iconos fuertes)
    private int getColorForItemType(String itemType) {
        switch (itemType) {
            case "Derecho": return R.color.upp_primary; // Morado
            case "Obligación": return R.color.status_green; // Verde
            case "Prohibición": return R.color.game_fail; // Rojo
            case "Sanción": return R.color.upp_secondary; // Morado oscuro/Marron
            case "Reconocimiento": return R.color.upp_accent; // Dorado/Magenta
            default: return R.color.upp_primary;
        }
    }

    // Método auxiliar para fondos suaves (Pasteles)
    private int getBgTintForItemType(String itemType) {
        switch (itemType) {
            case "Derecho": return R.color.app_bg; // Lila suave
            case "Obligación": return R.color.status_green_bg; // Verde suave
            case "Prohibición": return R.color.game_fail_bg; // Rojo suave
            case "Sanción": return R.color.game_fail_bg; // Similar a alerta
            case "Reconocimiento": return R.color.game_success_bg; // Similar a éxito
            default: return R.color.app_bg;
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
        public final FrameLayout iconContainer; // Referencia al contenedor para cambiar color

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
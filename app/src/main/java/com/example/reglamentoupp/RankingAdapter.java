package com.example.reglamentoupp;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.material.card.MaterialCardView;

import java.util.List;

public class RankingAdapter extends RecyclerView.Adapter<RankingAdapter.RankingViewHolder> {

    private Context context;
    private List<Usuario> listaUsuarios;

    public RankingAdapter(Context context, List<Usuario> listaUsuarios) {
        this.context = context;
        this.listaUsuarios = listaUsuarios;
    }

    @NonNull
    @Override
    public RankingViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_ranking, parent, false);
        return new RankingViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RankingViewHolder holder, int position) {
        Usuario usuario = listaUsuarios.get(position);

        // 1. Textos principales
        holder.tvPosition.setText(String.valueOf(position + 1));
        holder.tvName.setText(usuario.getNombre());
        holder.tvScore.setText(String.valueOf(usuario.getPuntaje()));

        int nivelActual = usuario.getNivel() > 0 ? usuario.getNivel() : 1;
        holder.tvLevel.setText("Nivel " + nivelActual);

        // 2. Destacar Top 3 (Oro, Plata, Bronce)
        holder.ivTrophy.setVisibility(View.GONE);
        holder.cardRanking.setStrokeWidth(0);

        if (position == 0) { // ORO
            holder.tvPosition.setBackgroundTintList(android.content.res.ColorStateList.valueOf(Color.parseColor("#FFD700")));
            holder.cardRanking.setStrokeColor(Color.parseColor("#FFD700"));
            holder.cardRanking.setStrokeWidth(5);
            holder.cardRanking.setCardBackgroundColor(Color.parseColor("#FFFDF0"));
            holder.ivTrophy.setVisibility(View.VISIBLE);
            holder.ivTrophy.setColorFilter(Color.parseColor("#FFD700"));
        } else if (position == 1) { // PLATA
            holder.tvPosition.setBackgroundTintList(android.content.res.ColorStateList.valueOf(Color.parseColor("#C0C0C0")));
            holder.cardRanking.setStrokeColor(Color.parseColor("#C0C0C0"));
            holder.cardRanking.setStrokeWidth(5);
            holder.cardRanking.setCardBackgroundColor(Color.parseColor("#FAFAFA"));
            holder.ivTrophy.setVisibility(View.VISIBLE);
            holder.ivTrophy.setColorFilter(Color.parseColor("#C0C0C0"));
        } else if (position == 2) { // BRONCE
            holder.tvPosition.setBackgroundTintList(android.content.res.ColorStateList.valueOf(Color.parseColor("#CD7F32")));
            holder.cardRanking.setStrokeColor(Color.parseColor("#CD7F32"));
            holder.cardRanking.setStrokeWidth(5);
            holder.cardRanking.setCardBackgroundColor(Color.parseColor("#FFF9F5"));
            holder.ivTrophy.setVisibility(View.VISIBLE);
            holder.ivTrophy.setColorFilter(Color.parseColor("#CD7F32"));
        } else { // RESTO DE JUGADORES
            holder.tvPosition.setBackgroundTintList(android.content.res.ColorStateList.valueOf(Color.parseColor("#9C27B0"))); // Color morado UPP
            holder.cardRanking.setCardBackgroundColor(Color.WHITE);
        }

        // 3. Cargar la imagen circularmente con Glide
        String urlFoto = usuario.getFotoPerfilUrl();
        if (urlFoto != null && !urlFoto.isEmpty()) {
            Glide.with(context)
                    .load(urlFoto)
                    .placeholder(R.drawable.ic_profile)
                    .error(R.drawable.ic_profile)
                    .circleCrop() // <-- CRUCIAL: Esto convierte la foto en un círculo perfecto
                    .into(holder.ivProfile);
        } else {
            // Si el usuario no tiene foto, mostramos el icono por defecto circular
            Glide.with(context)
                    .load(R.drawable.ic_profile)
                    .circleCrop()
                    .into(holder.ivProfile);
        }
    }

    @Override
    public int getItemCount() {
        return listaUsuarios.size();
    }

    public static class RankingViewHolder extends RecyclerView.ViewHolder {
        TextView tvPosition, tvName, tvScore, tvLevel;
        ImageView ivProfile, ivTrophy;
        MaterialCardView cardRanking;

        public RankingViewHolder(@NonNull View itemView) {
            super(itemView);
            cardRanking = itemView.findViewById(R.id.cardRanking);
            tvPosition = itemView.findViewById(R.id.tvRankingPosition);
            tvName = itemView.findViewById(R.id.tvRankingName);
            tvScore = itemView.findViewById(R.id.tvRankingScore);
            tvLevel = itemView.findViewById(R.id.tvRankingLevel);
            ivProfile = itemView.findViewById(R.id.ivRankingProfile);
            ivTrophy = itemView.findViewById(R.id.ivTrophy);
        }
    }
}
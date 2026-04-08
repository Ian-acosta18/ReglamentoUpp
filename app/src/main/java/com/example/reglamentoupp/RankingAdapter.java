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

        // 1. Mostrar Posición, Nombre, Nivel y Puntaje
        holder.tvPosition.setText(String.valueOf(position + 1));
        holder.tvName.setText(usuario.getNombre());
        holder.tvScore.setText(usuario.getPuntaje() + " pts");

        int nivelActual = usuario.getNivel() > 0 ? usuario.getNivel() : 1;
        holder.tvLevel.setText("Nivel " + nivelActual);

        // 2. Destacar visualmente el Top 3 en toda la tarjeta
        if (position == 0) { // ORO
            holder.tvPosition.setBackgroundTintList(android.content.res.ColorStateList.valueOf(Color.parseColor("#FFD700")));
            holder.cardRanking.setStrokeColor(Color.parseColor("#FFD700"));
            holder.cardRanking.setStrokeWidth(5);
            holder.cardRanking.setCardBackgroundColor(Color.parseColor("#FFFDF0")); // Fondo amarillento muy claro
        } else if (position == 1) { // PLATA
            holder.tvPosition.setBackgroundTintList(android.content.res.ColorStateList.valueOf(Color.parseColor("#C0C0C0")));
            holder.cardRanking.setStrokeColor(Color.parseColor("#C0C0C0"));
            holder.cardRanking.setStrokeWidth(5);
            holder.cardRanking.setCardBackgroundColor(Color.parseColor("#FAFAFA")); // Fondo gris muy claro
        } else if (position == 2) { // BRONCE
            holder.tvPosition.setBackgroundTintList(android.content.res.ColorStateList.valueOf(Color.parseColor("#CD7F32")));
            holder.cardRanking.setStrokeColor(Color.parseColor("#CD7F32"));
            holder.cardRanking.setStrokeWidth(5);
            holder.cardRanking.setCardBackgroundColor(Color.parseColor("#FFF9F5")); // Fondo anaranjado muy claro
        } else { // RESTO DE JUGADORES
            holder.tvPosition.setBackgroundTintList(android.content.res.ColorStateList.valueOf(Color.parseColor("#9C27B0"))); // Tu morado
            holder.cardRanking.setStrokeWidth(0); // Sin borde
            holder.cardRanking.setCardBackgroundColor(Color.WHITE); // Fondo blanco normal
        }

        // 3. Cargar la Foto de Perfil usando Glide
        String urlFoto = usuario.getFotoPerfilUrl();
        if (urlFoto != null && !urlFoto.isEmpty()) {
            Glide.with(context)
                    .load(urlFoto)
                    .placeholder(R.drawable.ic_profile)
                    .error(R.drawable.ic_profile)
                    .circleCrop()
                    .into(holder.ivProfile);
        } else {
            holder.ivProfile.setImageResource(R.drawable.ic_profile);
        }

        // 4. Mostrar Insignias de Logros según su puntaje
        holder.badge1.setVisibility(View.GONE);
        holder.badge2.setVisibility(View.GONE);
        holder.badge3.setVisibility(View.GONE);

        int puntos = usuario.getPuntaje();

        if (puntos >= 50) {
            holder.badge1.setVisibility(View.VISIBLE);
        }
        if (puntos >= 150) {
            holder.badge2.setVisibility(View.VISIBLE);
        }
        if (puntos >= 300) {
            holder.badge3.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public int getItemCount() {
        return listaUsuarios.size();
    }

    public static class RankingViewHolder extends RecyclerView.ViewHolder {
        TextView tvPosition, tvName, tvScore, tvLevel;
        ImageView ivProfile, badge1, badge2, badge3;
        MaterialCardView cardRanking; // NUEVO: Referencia a la tarjeta

        public RankingViewHolder(@NonNull View itemView) {
            super(itemView);
            cardRanking = itemView.findViewById(R.id.cardRanking); // NUEVO
            tvPosition = itemView.findViewById(R.id.tvRankingPosition);
            tvName = itemView.findViewById(R.id.tvRankingName);
            tvScore = itemView.findViewById(R.id.tvRankingScore);
            tvLevel = itemView.findViewById(R.id.tvRankingLevel);
            ivProfile = itemView.findViewById(R.id.ivRankingProfile);
            badge1 = itemView.findViewById(R.id.badge1);
            badge2 = itemView.findViewById(R.id.badge2);
            badge3 = itemView.findViewById(R.id.badge3);
        }
    }
}
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

import com.bumptech.glide.Glide; // Importación de la librería de imágenes

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

        // 1. Mostrar Posición, Nombre y Puntaje
        holder.tvPosition.setText(String.valueOf(position + 1));
        holder.tvName.setText(usuario.getNombre());
        holder.tvScore.setText(usuario.getPuntaje() + " pts");

        // Cambiar color del círculo de posición para los primeros 3 lugares (Oro, Plata, Bronce)
        if (position == 0) {
            holder.tvPosition.setBackgroundTintList(android.content.res.ColorStateList.valueOf(Color.parseColor("#FFD700")));
        } else if (position == 1) {
            holder.tvPosition.setBackgroundTintList(android.content.res.ColorStateList.valueOf(Color.parseColor("#C0C0C0")));
        } else if (position == 2) {
            holder.tvPosition.setBackgroundTintList(android.content.res.ColorStateList.valueOf(Color.parseColor("#CD7F32")));
        } else {
            holder.tvPosition.setBackgroundTintList(android.content.res.ColorStateList.valueOf(Color.parseColor("#9C27B0"))); // Color por defecto
        }

        // 2. Cargar la Foto de Perfil usando Glide
        String urlFoto = usuario.getFotoPerfilUrl();
        if (urlFoto != null && !urlFoto.isEmpty()) {
            Glide.with(context)
                    .load(urlFoto)
                    .placeholder(R.drawable.ic_profile) // Imagen temporal mientras carga
                    .error(R.drawable.ic_profile)       // Imagen si falla la descarga
                    .circleCrop()                       // Asegura que sea un círculo
                    .into(holder.ivProfile);
        } else {
            // Si el usuario no tiene foto, poner la imagen predeterminada
            holder.ivProfile.setImageResource(R.drawable.ic_profile);
        }

        // 3. Mostrar Insignias de Logros según su puntaje
        // Primero ocultamos todas para que no haya errores visuales al reciclar las tarjetas
        holder.badge1.setVisibility(View.GONE);
        holder.badge2.setVisibility(View.GONE);
        holder.badge3.setVisibility(View.GONE);

        int puntos = usuario.getPuntaje();

        // Lógica de insignias (modifica estos valores según la dificultad de tu juego)
        if (puntos >= 50) {
            holder.badge1.setVisibility(View.VISIBLE); // Gana la primera insignia a los 50 pts
        }
        if (puntos >= 150) {
            holder.badge2.setVisibility(View.VISIBLE); // Gana la segunda insignia a los 150 pts
        }
        if (puntos >= 300) {
            holder.badge3.setVisibility(View.VISIBLE); // Gana la tercera insignia a los 300 pts
        }
    }

    @Override
    public int getItemCount() {
        return listaUsuarios.size();
    }

    public static class RankingViewHolder extends RecyclerView.ViewHolder {
        TextView tvPosition, tvName, tvScore;
        ImageView ivProfile, badge1, badge2, badge3;

        public RankingViewHolder(@NonNull View itemView) {
            super(itemView);
            tvPosition = itemView.findViewById(R.id.tvRankingPosition);
            tvName = itemView.findViewById(R.id.tvRankingName);
            tvScore = itemView.findViewById(R.id.tvRankingScore);
            ivProfile = itemView.findViewById(R.id.ivRankingProfile);
            badge1 = itemView.findViewById(R.id.badge1);
            badge2 = itemView.findViewById(R.id.badge2);
            badge3 = itemView.findViewById(R.id.badge3);
        }
    }
}
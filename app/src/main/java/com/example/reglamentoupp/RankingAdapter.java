package com.example.reglamentoupp;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class RankingAdapter extends RecyclerView.Adapter<RankingAdapter.RankingViewHolder> {

    private List<Usuario> listaUsuarios;

    public RankingAdapter(List<Usuario> listaUsuarios) {
        this.listaUsuarios = listaUsuarios;
    }

    @NonNull
    @Override
    public RankingViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_ranking, parent, false);
        return new RankingViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RankingViewHolder holder, int position) {
        Usuario usuario = listaUsuarios.get(position);
        int rank = position + 1;

        holder.tvNombre.setText(usuario.getNombre() + " " + usuario.getApellidos());
        holder.tvPuntaje.setText(usuario.getPuntaje() + " XP");
        holder.tvPosicion.setText(String.valueOf(rank));

        // Lógica visual para Top 3
        switch (rank) {
            case 1: // Oro
                holder.tvPosicion.setTextColor(Color.parseColor("#FFD700"));
                holder.tvPosicion.setText("👑"); // Icono especial al 1ro
                break;
            case 2: // Plata
                holder.tvPosicion.setTextColor(Color.parseColor("#C0C0C0"));
                break;
            case 3: // Bronce
                holder.tvPosicion.setTextColor(Color.parseColor("#CD7F32"));
                break;
            default:
                holder.tvPosicion.setTextColor(Color.BLACK);
                break;
        }
    }

    @Override
    public int getItemCount() {
        return listaUsuarios.size();
    }

    public static class RankingViewHolder extends RecyclerView.ViewHolder {
        TextView tvPosicion, tvNombre, tvPuntaje;

        public RankingViewHolder(@NonNull View itemView) {
            super(itemView);
            tvPosicion = itemView.findViewById(R.id.tvPosicion);
            tvNombre = itemView.findViewById(R.id.tvNombreJugador);
            tvPuntaje = itemView.findViewById(R.id.tvPuntajeRanking);
        }
    }
}
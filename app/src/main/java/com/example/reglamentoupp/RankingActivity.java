package com.example.reglamentoupp;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class RankingActivity extends AppCompatActivity {

    private RecyclerView recyclerRanking;
    private ProgressBar progressBar;
    private ImageButton btnBack;
    private RankingAdapter adapter;
    private List<Usuario> listaUsuarios;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ranking);

        // Inicializar Vistas
        recyclerRanking = findViewById(R.id.recyclerRanking);
        progressBar = findViewById(R.id.progressBarRanking);
        btnBack = findViewById(R.id.btnBackRanking);

        // Configurar RecyclerView
        recyclerRanking.setLayoutManager(new LinearLayoutManager(this));
        listaUsuarios = new ArrayList<>();

        adapter = new RankingAdapter(this, listaUsuarios);
        recyclerRanking.setAdapter(adapter);

        // Inicializar Firebase
        db = FirebaseFirestore.getInstance();

        // Botón de retroceso
        btnBack.setOnClickListener(v -> finish());

        // Cargar Datos
        cargarRanking();
    }

    private void cargarRanking() {
        progressBar.setVisibility(View.VISIBLE);

        // Consultamos los usuarios ordenados por puntaje limitando a los 10 mejores
        db.collection("usuarios")
                .orderBy("puntaje", Query.Direction.DESCENDING)
                .limit(10) // <-- Modificado para mostrar solo 10
                .get()
                .addOnCompleteListener(task -> {
                    progressBar.setVisibility(View.GONE);
                    if (task.isSuccessful()) {
                        listaUsuarios.clear();
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            Usuario usuario = document.toObject(Usuario.class);
                            if (usuario.getPuntaje() > 0) {
                                listaUsuarios.add(usuario);
                            }
                        }
                        adapter.notifyDataSetChanged();

                        if (listaUsuarios.isEmpty()) {
                            Toast.makeText(RankingActivity.this, "Aún no hay jugadores en el ranking", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(RankingActivity.this, "Error al cargar el ranking: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }
}
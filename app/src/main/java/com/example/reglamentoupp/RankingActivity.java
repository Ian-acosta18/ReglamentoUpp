package com.example.reglamentoupp;

import android.os.Bundle;
import android.view.View;
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

    private RecyclerView recyclerView;
    private ProgressBar progressBar;
    private RankingAdapter adapter;
    private List<Usuario> listaUsuarios;
    private FirebaseFirestore mStore;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ranking);

        recyclerView = findViewById(R.id.recyclerViewRanking);
        progressBar = findViewById(R.id.progressBarRanking);
        mStore = FirebaseFirestore.getInstance();
        listaUsuarios = new ArrayList<>();

        // Configurar RecyclerView
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new RankingAdapter(listaUsuarios);
        recyclerView.setAdapter(adapter);

        cargarRanking();
    }

    private void cargarRanking() {
        // Ordenamos por 'puntaje' de mayor a menor y limitamos a los top 20
        mStore.collection("usuarios")
                .orderBy("puntaje", Query.Direction.DESCENDING)
                .limit(20)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    progressBar.setVisibility(View.GONE);
                    if (!queryDocumentSnapshots.isEmpty()) {
                        listaUsuarios.clear();
                        for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                            Usuario user = doc.toObject(Usuario.class);
                            listaUsuarios.add(user);
                        }
                        adapter.notifyDataSetChanged();
                    } else {
                        Toast.makeText(this, "Aún no hay jugadores registrados.", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(this, "Error al cargar ranking: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
    }
}
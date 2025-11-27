package com.example.reglamentoupp;

import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
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
    private ListenerRegistration registration; // Variable para controlar la conexión automática

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ranking);

        recyclerView = findViewById(R.id.recyclerViewRanking);
        progressBar = findViewById(R.id.progressBarRanking);
        mStore = FirebaseFirestore.getInstance();
        listaUsuarios = new ArrayList<>();

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new RankingAdapter(listaUsuarios);
        recyclerView.setAdapter(adapter);

        // Se llama a la función que activa la escucha automática
        cargarRankingEnTiempoReal();
    }

    private void cargarRankingEnTiempoReal() {
        // Usamos addSnapshotListener en lugar de get() para actualizaciones automáticas
        registration = mStore.collection("usuarios")
                .orderBy("puntaje", Query.Direction.DESCENDING)
                .limit(20)
                .addSnapshotListener((value, error) -> {
                    progressBar.setVisibility(View.GONE);

                    if (error != null) {
                        Toast.makeText(this, "Error al cargar datos: " + error.getMessage(), Toast.LENGTH_LONG).show();
                        return;
                    }

                    if (value != null) {
                        listaUsuarios.clear();
                        for (QueryDocumentSnapshot doc : value) {
                            Usuario user = doc.toObject(Usuario.class);
                            listaUsuarios.add(user);
                        }
                        adapter.notifyDataSetChanged();

                        if(listaUsuarios.isEmpty()){
                            Toast.makeText(this, "Aún no hay jugadores en el ranking.", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    @Override
    protected void onStop() {
        super.onStop();
        // Detenemos la escucha automática cuando la app se minimiza para ahorrar batería
        if (registration != null) {
            registration.remove();
        }
    }
}
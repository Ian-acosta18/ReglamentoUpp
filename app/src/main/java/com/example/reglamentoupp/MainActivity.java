package com.example.reglamentoupp;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View; // Importación necesaria
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import com.example.reglamentoupp.databinding.ActivityMainBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

// --- CORRECCIÓN DE ERRORES 13, 14, 15 ---
// Implementa la interfaz correcta de BaseReglamentoFragment (Turno 7)
public class MainActivity extends AppCompatActivity implements BaseReglamentoFragment.ReglamentoInteractionListener {

    private ActivityMainBinding binding;
    private FirebaseAuth mAuth;
    private FirebaseFirestore mStore;
    private String userID;
    private long userPuntaje = 0;

    public static final String KEY_NIVEL_JUEGO = "nivelJuego";
    public static final String KEY_PUNTAJE_ACTUAL = "puntajeActual";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        mAuth = FirebaseAuth.getInstance();
        mStore = FirebaseFirestore.getInstance();

        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            Log.w("MainActivity", "Usuario no logueado. Regresando a Login.");
            navigateToLogin();
            return;
        }
        userID = currentUser.getUid();

        binding.btnLogout.setOnClickListener(v -> {
            mAuth.signOut();
            navigateToLogin();
        });

        loadUserData();

        // (El fragmentClass no se usa, pero es bueno tener la referencia)
        setupNivelButton(binding.btnJugarDerechos, "Derechos", DerechosFragment.class);
        setupNivelButton(binding.btnJugarObligaciones, "Obligaciones", ObligacionesFragment.class);
        setupNivelButton(binding.btnJugarProhibiciones, "Prohibiciones", ProhibicionesFragment.class);
        setupNivelButton(binding.btnJugarSanciones, "Sanciones", SancionesFragment.class);
        setupNivelButton(binding.btnJugarReconocimientos, "Reconocimientos", ReconocimientosFragment.class);
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Recarga los datos del usuario (el puntaje puede cambiar)
        loadUserData();
    }

    private void loadUserData() {
        if (userID == null) return;

        mStore.collection("usuarios").document(userID).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        String email = documentSnapshot.getString("email");
                        Long puntajeDb = documentSnapshot.getLong("puntaje");
                        if (puntajeDb != null) {
                            userPuntaje = puntajeDb;
                        }
                        binding.tvUserEmail.setText(email);
                        binding.tvUserPuntaje.setText("Puntaje: " + userPuntaje);
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e("MainActivity", "Error al cargar datos", e);
                    FirebaseUser user = mAuth.getCurrentUser();
                    if (user != null) {
                        binding.tvUserEmail.setText(user.getEmail());
                    }
                    binding.tvUserPuntaje.setText("Puntaje: Error");
                });
    }

    /**
     * Configura un botón para iniciar un nivel de juego.
     * Acepta un 'View' porque tus botones son 'MaterialCardView'
     */
    private void setupNivelButton(View button, String nivel, Class<? extends Fragment> fragmentClass) {
        button.setOnClickListener(v -> {
            Log.d("MainActivity", "Iniciando nivel: " + nivel);
            Intent intent = new Intent(MainActivity.this, GameLevelActivity.class);
            intent.putExtra(KEY_NIVEL_JUEGO, nivel);
            intent.putExtra(KEY_PUNTAJE_ACTUAL, userPuntaje);
            startActivity(intent);
        });
    }

    private void navigateToLogin() {
        Intent intent = new Intent(MainActivity.this, LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    // --- CORRECCIÓN DE ERRORES 13, 14, 15 ---
    // Implementa los métodos que tu ReglamentoAdapter SÍ necesita

    @Override
    public void onQuizClick(String itemText, String itemType) {
        Log.d("MainActivity", "Clic en Quiz: " + itemType);
        Toast.makeText(this, "Quiz: " + itemType, Toast.LENGTH_SHORT).show();
        // Esta actividad no hace nada con el clic, pero debe tener el método.
    }

    @Override
    public void onCaseStudyClick(String itemText, String itemType) {
        Log.d("MainActivity", "Clic en Caso de Estudio: " + itemType);
        Toast.makeText(this, "Caso: " + itemType, Toast.LENGTH_SHORT).show();
        // Esta actividad no hace nada con el clic, pero debe tener el método.
    }
}
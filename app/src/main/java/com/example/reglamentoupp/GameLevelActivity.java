package com.example.reglamentoupp;

import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.example.reglamentoupp.databinding.ActivityGameLevelBinding;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

// --- IMPLEMENTAMOS LA NUEVA INTERFAZ DEL QUIZ ---
public class GameLevelActivity extends AppCompatActivity implements
        BaseReglamentoFragment.ReglamentoInteractionListener,
        QuizBottomSheetFragment.OnQuizCompleteListener {

    private ActivityGameLevelBinding binding;
    private String nivelJuego; // "Derechos", "Obligaciones", etc.
    private long puntajeActual;
    private int nivelActualDesbloqueado; // 1, 2, 3, etc.

    private FirebaseFirestore mStore;
    private FirebaseUser currentUser;
    private static final String TAG = "GameLevelActivity";

    // Puntos necesarios para desbloquear el siguiente nivel
    private static final int PUNTOS_PARA_DESBLOQUEAR = 50;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityGameLevelBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        mStore = FirebaseFirestore.getInstance();
        currentUser = FirebaseAuth.getInstance().getCurrentUser();

        if (currentUser == null) {
            Log.e(TAG, "Usuario no logueado, cerrando nivel.");
            finish();
            return;
        }

        // Obtener datos pasados desde MainActivity
        nivelJuego = getIntent().getStringExtra(MainActivity.KEY_NIVEL_JUEGO);
        puntajeActual = getIntent().getLongExtra(MainActivity.KEY_PUNTAJE_ACTUAL, 0);
        nivelActualDesbloqueado = getIntent().getIntExtra(MainActivity.KEY_NIVEL_DESBLOQUEADO, 1);


        if (nivelJuego == null) {
            Log.e(TAG, "No se recibió el nombre del nivel.");
            Toast.makeText(this, "Error al cargar nivel", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Configurar la Toolbar
        binding.toolbarLevel.setTitle(nivelJuego);
        binding.toolbarLevel.setNavigationOnClickListener(v -> finish()); // Botón de regresar

        // Cargar el fragmento correcto en el contenedor
        loadLevelFragment(nivelJuego);
    }

    private void loadLevelFragment(String nivel) {
        Fragment fragmentToLoad = null;

        switch (nivel) {
            case "Derechos":
                fragmentToLoad = new DerechosFragment();
                break;
            case "Obligaciones":
                fragmentToLoad = new ObligacionesFragment();
                break;
            case "Prohibiciones":
                fragmentToLoad = new ProhibicionesFragment();
                break;
            case "Sanciones":
                fragmentToLoad = new SancionesFragment();
                break;
            case "Reconocimientos":
                fragmentToLoad = new ReconocimientosFragment();
                break;
            default:
                Log.e(TAG, "Nombre de nivel desconocido: " + nivel);
                Toast.makeText(this, "Nivel no encontrado", Toast.LENGTH_SHORT).show();
                finish();
                return;
        }

        if (fragmentToLoad != null) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.game_content_container, fragmentToLoad)
                    .commit();
        }
    }

    // --- Implementación de los clics del Fragment ---

    /**
     * Se llama cuando el usuario hace clic en una tarjeta de regla (para el Quiz).
     */
    @Override
    public void onQuizClick(String itemText, String itemType) {
        Log.d(TAG, "Iniciando Quiz para: " + itemType);
        QuizBottomSheetFragment quizFragment = QuizBottomSheetFragment.newInstance(itemType);
        quizFragment.show(getSupportFragmentManager(), "QuizBottomSheet");
    }

    /**
     * Se llama cuando el usuario hace clic en el botón "Analizar Caso".
     */
    @Override
    public void onCaseStudyClick(String itemText, String itemType) {
        Log.d(TAG, "Iniciando Caso de Estudio para: " + itemType);
        CaseStudyBottomSheetFragment caseFragment = CaseStudyBottomSheetFragment.newInstance(itemType, itemText);
        caseFragment.show(getSupportFragmentManager(), "CaseStudyBottomSheet");
    }

    /**
     * Se llama cuando el QuizBottomSheetFragment se cierra y reporta el resultado.
     * Esta es la implementación de la interfaz QuizBottomSheetFragment.OnQuizCompleteListener.
     */
    @Override
    public void onQuizComplete(int puntos, boolean esCorrecto) {
        Log.d(TAG, "Quiz completado. Puntos ganados: " + puntos);

        if (puntos > 0) {
            // Actualizar puntaje local
            puntajeActual += puntos;
            Toast.makeText(this, "¡+10 puntos!", Toast.LENGTH_SHORT).show();

            // Guardar el nuevo puntaje en Firestore
            actualizarPuntajeEnFirestore();

            // Verificar si se desbloquea el siguiente nivel
            verificarDesbloqueoDeNivel();
        } else {
            Toast.makeText(this, "Respuesta incorrecta. ¡Sigue intentando!", Toast.LENGTH_SHORT).show();
        }
    }

    private void actualizarPuntajeEnFirestore() {
        if (currentUser == null) return;

        mStore.collection("usuarios").document(currentUser.getUid())
                .update("puntaje", puntajeActual)
                .addOnSuccessListener(aVoid -> Log.d(TAG, "Puntaje actualizado en Firestore a: " + puntajeActual))
                .addOnFailureListener(e -> Log.e(TAG, "Error al actualizar puntaje en Firestore", e));
    }

    private void verificarDesbloqueoDeNivel() {
        // Obtenemos el nivel que se está jugando (ej: "Derechos" es nivel 1)
        int nivelJugando = getNivelIndex(nivelJuego);

        // Solo intentamos desbloquear si estamos jugando el nivel más alto que tenemos
        if (nivelJugando == nivelActualDesbloqueado) {
            // Verificamos si el puntaje actual supera el umbral del nivel
            // (Ej: 50 pts para Nivel 2, 100 para Nivel 3, etc.)
            if (puntajeActual >= (nivelJugando * PUNTOS_PARA_DESBLOQUEAR)) {
                int proximoNivel = nivelJugando + 1;
                // Actualizar el nivel en Firestore
                desbloquearSiguienteNivel(proximoNivel);
            }
        }
    }

    private void desbloquearSiguienteNivel(int proximoNivel) {
        if (currentUser == null) return;

        // Máximo 5 niveles
        if (proximoNivel > 5) {
            Log.d(TAG, "¡Felicidades! Todos los niveles están desbloqueados.");
            return;
        }

        mStore.collection("usuarios").document(currentUser.getUid())
                .update("nivelDesbloqueado", proximoNivel)
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "¡Nivel " + proximoNivel + " desbloqueado!");
                    this.nivelActualDesbloqueado = proximoNivel;
                    // Mostrar un diálogo de felicitación
                    new MaterialAlertDialogBuilder(this)
                            .setTitle("¡Nivel Desbloqueado!")
                            .setMessage("¡Felicidades! Has desbloqueado el nivel: " + getNivelNombre(proximoNivel))
                            .setPositiveButton("Genial", null)
                            .show();
                })
                .addOnFailureListener(e -> Log.e(TAG, "Error al desbloquear el nivel", e));
    }


    private int getNivelIndex(String nombreNivel) {
        switch (nombreNivel) {
            case "Derechos": return 1;
            case "Obligaciones": return 2;
            case "Prohibiciones": return 3;
            case "Sanciones": return 4;
            case "Reconocimientos": return 5;
            default: return 0;
        }
    }

    private String getNivelNombre(int index) {
        switch (index) {
            case 1: return "Derechos";
            case 2: return "Obligaciones";
            case 3: return "Prohibiciones";
            case 4: return "Sanciones";
            case 5: return "Reconocimientos";
            default: return "Desconocido";
        }
    }
}
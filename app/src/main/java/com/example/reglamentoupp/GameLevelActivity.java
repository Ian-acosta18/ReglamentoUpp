package com.example.reglamentoupp;

import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

public class GameLevelActivity extends AppCompatActivity implements
        BaseReglamentoFragment.ReglamentoInteractionListener,
        QuizBottomSheetFragment.OnQuizCompleteListener {

    private String nivelJuego; // "Derechos", "Obligaciones", etc.
    private long puntajeActual = 0;
    private int nivelActualDesbloqueado = 1;

    private FirebaseFirestore mStore;
    private FirebaseUser currentUser;
    private MaterialToolbar toolbarLevel;

    private static final String TAG = "GameLevelActivity";
    // Puntos necesarios para desbloquear el siguiente nivel
    private static final int PUNTOS_PARA_DESBLOQUEAR = 50;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Usamos el layout tradicional en lugar de ViewBinding
        setContentView(R.layout.activity_game_level);

        mStore = FirebaseFirestore.getInstance();
        currentUser = FirebaseAuth.getInstance().getCurrentUser();

        if (currentUser == null) {
            Log.e(TAG, "Usuario no logueado, cerrando nivel.");
            finish();
            return;
        }

        // Vincular Vistas
        toolbarLevel = findViewById(R.id.toolbar_level);

        // Obtener el nivel seleccionado desde MainActivity
        nivelJuego = getIntent().getStringExtra(MainActivity.KEY_NIVEL_JUEGO);

        if (nivelJuego == null) {
            Log.e(TAG, "No se recibió el nombre del nivel.");
            Toast.makeText(this, "Error al cargar nivel", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Configurar la Toolbar
        toolbarLevel.setTitle(nivelJuego);
        toolbarLevel.setNavigationOnClickListener(v -> finish()); // Botón de regresar

        // ⚠️ CARGAMOS LOS PUNTOS REALES DESDE FIRESTORE PARA NO SOBRESCRIBIRLOS A CERO
        cargarPuntajeRealUsuario();

        // Cargar el fragmento correcto en el contenedor
        loadLevelFragment(nivelJuego);
    }

    /**
     * Consulta Firestore al abrir la actividad para saber exactamente
     * cuántos puntos y qué nivel tiene el usuario actualmente.
     */
    private void cargarPuntajeRealUsuario() {
        mStore.collection("usuarios").document(currentUser.getUid()).get()
                .addOnSuccessListener(document -> {
                    if (document.exists()) {
                        Long puntajeBD = document.getLong("puntaje");
                        Long nivelBD = document.getLong("nivelDesbloqueado");

                        if (puntajeBD != null) {
                            this.puntajeActual = puntajeBD;
                        }
                        if (nivelBD != null) {
                            this.nivelActualDesbloqueado = nivelBD.intValue();
                        }
                    }
                })
                .addOnFailureListener(e -> Log.e(TAG, "Error al cargar datos del usuario", e));
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

    @Override
    public void onQuizClick(String itemText, String itemType) {
        Log.d(TAG, "Iniciando Quiz para: " + itemType);
        QuizBottomSheetFragment quizFragment = QuizBottomSheetFragment.newInstance(itemType);
        quizFragment.show(getSupportFragmentManager(), "QuizBottomSheet");
    }

    @Override
    public void onCaseStudyClick(String itemText, String itemType) {
        Log.d(TAG, "Iniciando Caso de Estudio para: " + itemType);
        CaseStudyBottomSheetFragment caseFragment = CaseStudyBottomSheetFragment.newInstance(itemType, itemText);
        caseFragment.show(getSupportFragmentManager(), "CaseStudyBottomSheet");
    }

    // --- Implementación de finalización del Quiz ---

    @Override
    public void onQuizComplete(int puntos, boolean esCorrecto) {
        Log.d(TAG, "Quiz completado. Puntos ganados: " + puntos);

        if (puntos > 0) {
            // Sumamos los puntos al total que obtuvimos de la base de datos
            puntajeActual += puntos;
            Toast.makeText(this, "¡+10 puntos!", Toast.LENGTH_SHORT).show();

            // Guardar el nuevo puntaje en Firestore
            actualizarPuntajeEnFirestore();

            // Verificar si el nuevo puntaje alcanza para desbloquear el siguiente nivel
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
        int nivelJugando = getNivelIndex(nivelJuego);

        // Solo verificamos el progreso si estamos jugando el nivel más alto que tenemos desbloqueado
        if (nivelJugando == nivelActualDesbloqueado) {
            // Ejemplo: Si estoy en Nivel 1 (Derechos), necesito 50 puntos para desbloquear Nivel 2.
            if (puntajeActual >= (nivelJugando * PUNTOS_PARA_DESBLOQUEAR)) {
                int proximoNivel = nivelJugando + 1;
                desbloquearSiguienteNivel(proximoNivel);
            }
        }
    }

    private void desbloquearSiguienteNivel(int proximoNivel) {
        if (currentUser == null) return;

        // Máximo 5 niveles en el sistema
        if (proximoNivel > 5) {
            Log.d(TAG, "Todos los niveles están desbloqueados.");
            return;
        }

        mStore.collection("usuarios").document(currentUser.getUid())
                .update("nivelDesbloqueado", proximoNivel)
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "¡Nivel " + proximoNivel + " desbloqueado!");
                    this.nivelActualDesbloqueado = proximoNivel;

                    new MaterialAlertDialogBuilder(this)
                            .setTitle("¡Nivel Desbloqueado!")
                            .setMessage("¡Felicidades! Has desbloqueado el módulo de: " + getNivelNombre(proximoNivel))
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
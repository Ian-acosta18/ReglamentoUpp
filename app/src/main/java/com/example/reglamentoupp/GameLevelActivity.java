package com.example.reglamentoupp;

import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.example.reglamentoupp.databinding.ActivityGameLevelBinding;

// --- INICIO DE CORRECCIÓN GENERAL (Errores 11-25) ---
// Esta clase debe implementar la interfaz para escuchar los clics del fragmento.
public class GameLevelActivity extends AppCompatActivity implements BaseReglamentoFragment.ReglamentoInteractionListener {

    private ActivityGameLevelBinding binding;
    private String nivelJuego;
    private long puntajeActual;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityGameLevelBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Obtener datos pasados desde MainActivity
        nivelJuego = getIntent().getStringExtra(MainActivity.KEY_NIVEL_JUEGO);
        puntajeActual = getIntent().getLongExtra(MainActivity.KEY_PUNTAJE_ACTUAL, 0);

        if (nivelJuego == null) {
            Log.e("GameLevelActivity", "No se recibió el nombre del nivel.");
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

        // Determina qué fragmento instanciar basado en el string
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
                Log.e("GameLevelActivity", "Nombre de nivel desconocido: " + nivel);
                Toast.makeText(this, "Nivel no encontrado", Toast.LENGTH_SHORT).show();
                finish();
                return;
        }

        // Carga el fragmento en el FrameLayout 'game_content_container'
        if (fragmentToLoad != null) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.game_content_container, fragmentToLoad)
                    .commit();
        }
    }

    // --- Implementación de los clics del Fragment ---
    // (Aquí es donde pondrás la lógica del Quiz y Casos de Estudio)

    @Override
    public void onQuizClick(String itemText, String itemType) {
        Log.d("GameLevelActivity", "Clic en Quiz: " + itemType);
        // TODO: Iniciar la lógica del Quiz
        Toast.makeText(this, "Iniciando Quiz para: " + itemType, Toast.LENGTH_SHORT).show();

        // Ejemplo de cómo actualizar puntaje y regresar:
        // long nuevoPuntaje = puntajeActual + 10;
        // updatePuntajeInFirestore(nuevoPuntaje);
    }

    @Override
    public void onCaseStudyClick(String itemText, String itemType) {
        Log.d("GameLevelActivity", "Clic en Caso de Estudio: " + itemType);
        // TODO: Iniciar la lógica del Caso de Estudio
        Toast.makeText(this, "Abriendo Caso para: " + itemType, Toast.LENGTH_SHORT).show();
    }

    // (Opcional) Si el quiz actualiza el puntaje, necesitarás una función
    // similar a la que borramos de GameLevelActivity (Turno 4) para
    // guardar el puntaje en Firebase.

}
// --- FIN DE CORRECCIÓN GENERAL ---
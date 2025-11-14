package com.example.reglamentoupp;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.example.reglamentoupp.databinding.ActivityMainBinding;
import com.google.android.material.card.MaterialCardView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

// --- IMPORTACIONES NECESARIAS PARA EL SCRIPT ---
import com.google.firebase.firestore.WriteBatch;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import java.util.ArrayList;
import java.util.List;
// --- FIN DE IMPORTACIONES ---

public class MainActivity extends AppCompatActivity implements BaseReglamentoFragment.ReglamentoInteractionListener {

    private ActivityMainBinding binding;
    private FirebaseAuth mAuth;
    private FirebaseFirestore mStore;
    private String userID;
    private long userPuntaje = 0;
    private int userNivel = 1; // Nivel de desbloqueo del usuario

    public static final String KEY_NIVEL_JUEGO = "nivelJuego";
    public static final String KEY_PUNTAJE_ACTUAL = "puntajeActual";
    public static final String KEY_NIVEL_DESBLOQUEADO = "nivelDesbloqueado";
    private static final String TAG = "MainActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // =================================================================
        // = SCRIPT TEMPORAL PARA SUBIR PREGUNTAS (Paso 2)                 =
        // =================================================================
        // Este botón se añade temporalmente en activity_main.xml
        FloatingActionButton fab = findViewById(R.id.fab_subir_preguntas);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Primero revisa que la base de datos esté vacía
                // para no duplicar preguntas.
                verificarYSubirPreguntas();
            }
        });
        // =================================================================
        // = FIN DEL SCRIPT TEMPORAL                                       =
        // =================================================================

        mAuth = FirebaseAuth.getInstance();
        mStore = FirebaseFirestore.getInstance();

        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            Log.w(TAG, "Usuario no logueado. Regresando a Login.");
            navigateToLogin();
            return;
        }
        userID = currentUser.getUid();

        binding.btnLogout.setOnClickListener(v -> {
            mAuth.signOut();
            navigateToLogin();
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadUserData();
    }

    private void loadUserData() {
        if (userID == null) return;

        mStore.collection("usuarios").document(userID).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        String nombre = documentSnapshot.getString("nombre");
                        if (nombre != null && !nombre.isEmpty()) {
                            binding.tvUserName.setText("Bienvenido, " + nombre);
                        } else {
                            binding.tvUserName.setText(documentSnapshot.getString("email"));
                        }

                        Long puntajeDb = documentSnapshot.getLong("puntaje");
                        if (puntajeDb != null) {
                            userPuntaje = puntajeDb;
                        }
                        binding.tvUserPuntaje.setText(userPuntaje + " Puntos");

                        Long nivelDb = documentSnapshot.getLong("nivelDesbloqueado");
                        if (nivelDb != null) {
                            userNivel = nivelDb.intValue();
                        }

                        Log.d(TAG, "Usuario cargado. Puntaje: " + userPuntaje + ", Nivel Desbloqueado: " + userNivel);

                        // ----- Código de configuración de botones (ya está corregido) -----
                        setupNivelButton(binding.btnJugarDerechos, null, binding.tvDerechos, binding.ivDerechos,
                                "Derechos", 1, R.color.upp_primary, R.color.text_primary, 0);
                        setupNivelButton(binding.btnJugarObligaciones, binding.ivLockObligaciones, binding.tvObligaciones, binding.ivObligaciones,
                                "Obligaciones", 2, R.color.upp_primary, R.color.text_primary, 50);
                        setupNivelButton(binding.btnJugarProhibiciones, binding.ivLockProhibiciones, binding.tvProhibiciones, binding.ivProhibiciones,
                                "Prohibiciones", 3, R.color.upp_primary, R.color.text_primary, 100);
                        setupNivelButton(binding.btnJugarSanciones, binding.ivLockSanciones, binding.tvSanciones, binding.ivSanciones,
                                "Sanciones", 4, R.color.upp_primary, R.color.text_primary, 150);
                        setupNivelButton(binding.btnJugarReconocimientos, binding.ivLockReconocimientos, binding.tvReconocimientos, binding.ivReconocimientos,
                                "Reconocimientos", 5, R.color.upp_primary, R.color.text_primary, 200);

                    } else {
                        Log.w(TAG, "No existe el documento del usuario en Firestore.");
                        mAuth.signOut();
                        navigateToLogin();
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error al cargar datos", e);
                    binding.tvUserName.setText("Error al cargar");
                    binding.tvUserPuntaje.setText("Puntaje: Error");
                });
    }

    private void setupNivelButton(MaterialCardView button, ImageView lockIcon, TextView textView, ImageView iconView,
                                  String nivel, int nivelRequerido, int colorDesbloqueado, int textColorDesbloqueado, int puntosRequeridos) {

        int colorBloqueado = ContextCompat.getColor(this, R.color.game_locked);
        int colorBgBloqueado = ContextCompat.getColor(this, R.color.game_locked_bg);
        int colorBgDesbloqueado = ContextCompat.getColor(this, R.color.card_bg);
        int colorIconoDesbloqueado = ContextCompat.getColor(this, colorDesbloqueado);
        int colorTextoDesbloqueado = ContextCompat.getColor(this, textColorDesbloqueado);

        if (userNivel >= nivelRequerido) {
            button.setEnabled(true);
            button.setClickable(true);
            button.setCardBackgroundColor(colorBgDesbloqueado);
            button.setCardElevation(getResources().getDimension(com.google.android.material.R.dimen.m3_card_elevation));

            if (lockIcon != null) {
                lockIcon.setVisibility(View.GONE);
            }

            textView.setTextColor(colorTextoDesbloqueado);
            textView.setText(nivel);
            iconView.setImageTintList(ColorStateList.valueOf(colorIconoDesbloqueado));

            button.setOnClickListener(v -> {
                Log.d(TAG, "Iniciando nivel: " + nivel);
                Intent intent = new Intent(MainActivity.this, GameLevelActivity.class);
                intent.putExtra(KEY_NIVEL_JUEGO, nivel);
                intent.putExtra(KEY_PUNTAJE_ACTUAL, userPuntaje);
                intent.putExtra(KEY_NIVEL_DESBLOQUEADO, userNivel);
                startActivity(intent);
            });

        } else {
            button.setEnabled(false);
            button.setClickable(false);
            button.setCardBackgroundColor(colorBgBloqueado);
            button.setCardElevation(0);

            if (lockIcon != null) {
                lockIcon.setVisibility(View.VISIBLE);
            }

            textView.setTextColor(colorBloqueado);
            textView.setText(nivel);
            iconView.setImageTintList(ColorStateList.valueOf(colorBloqueado));
        }
    }

    private void navigateToLogin() {
        Intent intent = new Intent(MainActivity.this, LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    @Override
    public void onQuizClick(String itemText, String itemType) {
        Log.d(TAG, "Clic en Quiz (ignorado en MainActivity): " + itemType);
    }

    @Override
    public void onCaseStudyClick(String itemText, String itemType) {
        Log.d(TAG, "Clic en Caso de Estudio (ignorado en MainActivity): " + itemType);
    }

    // ===================================================================
    // =          SCRIPT PARA SUBIR PREGUNTAS (Paso 3)                   =
    // ===================================================================

    private void verificarYSubirPreguntas() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("preguntas")
                .limit(1)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (queryDocumentSnapshots.isEmpty()) {
                        // La colección está vacía, podemos subir las preguntas
                        Log.d(TAG, "Base de datos vacía. Subiendo preguntas...");
                        subirPreguntasDePrueba(db);
                    } else {
                        // Ya hay preguntas, no hacemos nada
                        Log.d(TAG, "La base de datos ya tiene preguntas.");
                        Toast.makeText(MainActivity.this, "Las preguntas ya fueron subidas.", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error al verificar la base de datos", e);
                    Toast.makeText(MainActivity.this, "Error al verificar la BD.", Toast.LENGTH_SHORT).show();
                });
    }

    private void subirPreguntasDePrueba(FirebaseFirestore db) {
        WriteBatch batch = db.batch();
        List<Pregunta> preguntas = new ArrayList<>();

        // --- CATEGORÍA: DERECHOS ---
        preguntas.add(new Pregunta("Derecho", "¿Qué artículo te permite recibir asesorías y tutorías?", "Artículo 3 (I)", "Artículo 3 (XIII)", "Artículo 8 (II)", "Artículo 3 (XIII)"));
        preguntas.add(new Pregunta("Derecho", "¿Qué derecho tienes relacionado con tus exámenes? (Art. 3 (VII))", "Repetir el examen si repruebas", "Que el examen sea en parejas", "Conocer oportunamente el resultado", "Conocer oportunamente el resultado"));
        preguntas.add(new Pregunta("Derecho", "¿Qué debes obtener al inscribirte? (Art. 3 (VIII))", "Tu número de matrícula y credencial", "Un correo institucional", "Un lugar de estacionamiento", "Tu número de matrícula y credencial"));

        // --- CATEGORÍA: OBLIGACIONES ---
        preguntas.add(new Pregunta("Obligación", "¿Cuál es tu obligación si ocasionas daños? (Art. 5 (XI))", "Reparar los daños o pagarlos", "Reportarlo anónimamente", "Pedir una disculpa pública", "Reparar los daños o pagarlos"));
        preguntas.add(new Pregunta("Obligación", "¿Qué debes hacer al ingresar a la universidad? (Art. 5 (XII))", "Saludar al guardia", "Registrar tu entrada", "Mostrar la credencial de alumno", "Mostrar la credencial de alumno"));
        preguntas.add(new Pregunta("Obligación", "El Artículo 5 (V) menciona como una obligación:", "Asistir puntualmente y participar", "Traer tu propia laptop", "Ceder el paso a los profesores", "Asistir puntualmente y participar"));

        // --- CATEGORÍA: PROHIBICIONES ---
        preguntas.add(new Pregunta("Prohibición", "¿Qué prohíbe el Artículo 8 (I)?", "Usar el celular en clase", "Fumar en las instalaciones", "Escuchar música en la biblioteca", "Fumar en las instalaciones"));
        preguntas.add(new Pregunta("Prohibición", "Según el Artículo 8 (II), está prohibido...", "Jugar videojuegos en laboratorios", "Realizar fiestas en la cafetería", "Practicar juegos de azar y/o apuestas", "Practicar juegos de azar y/o apuestas"));
        preguntas.add(new Pregunta("Prohibición", "El Art. 8 (IV) prohíbe consumir alimentos en:", "Salón de clases, biblioteca o laboratorios", "Únicamente en los pasillos", "En las áreas verdes", "Salón de clases, biblioteca o laboratorios"));

        // --- CATEGORÍA: SANCIONES ---
        preguntas.add(new Pregunta("Sanción", "¿Cuál es la Sanción (IV), considerada la más grave?", "Suspensión temporal", "Expulsión definitiva", "Amonestación escrita", "Expulsión definitiva"));
        preguntas.add(new Pregunta("Sanción", "¿Qué dice la Sanción (II) sobre dañar material?", "Pide una disculpa al rector", "Realizar servicio comunitario", "Reposición o pago del material", "Reposición o pago del material"));
        preguntas.add(new Pregunta("Sanción", "¿Qué se sanciona según el Artículo 35?", "Inasistencia colectiva a clases", "Llegar tarde 3 veces", "Faltar a un examen final", "Inasistencia colectiva a clases"));

        // --- CATEGORÍA: RECONOCIMIENTOS ---
        preguntas.add(new Pregunta("Reconocimiento", "¿Qué reconocimiento se da por un buen promedio?", "Beca a la Excelencia Académica", "Un trofeo", "Un viaje pagado", "Beca a la Excelencia Académica"));
        preguntas.add(new Pregunta("Reconocimiento", "¿Qué recibes por un buen aprovechamiento cuatrimestral?", "Diploma de aprovechamiento", "Puntos extra", "Un día libre", "Diploma de aprovechamiento"));
        preguntas.add(new Pregunta("Reconocimiento", "Ganar un concurso o proyecto puede resultar en:", "Mención honorífica", "Un premio en efectivo", "Una nueva laptop", "Mención honorífica"));

        // Agregamos cada pregunta al lote
        for (Pregunta p : preguntas) {
            batch.set(db.collection("preguntas").document(), p);
        }

        // Ejecutamos la subida
        batch.commit()
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "¡ÉXITO! Se subieron " + preguntas.size() + " preguntas a Firestore.");
                    Toast.makeText(MainActivity.this, "¡ÉXITO! Se subieron las " + preguntas.size() + " preguntas.", Toast.LENGTH_LONG).show();
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error al subir preguntas", e);
                    Toast.makeText(MainActivity.this, "Error al subir preguntas: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
    }
}
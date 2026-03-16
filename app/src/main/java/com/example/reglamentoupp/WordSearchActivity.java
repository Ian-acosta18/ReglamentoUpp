package com.example.reglamentoupp;

import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.ForegroundColorSpan;
import android.text.style.StrikethroughSpan;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.GridLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class WordSearchActivity extends AppCompatActivity {

    private final char[][] matrizLetras = {
            {'S', 'E', 'N', 'O', 'I', 'C', 'I', 'B', 'I', 'H', 'O', 'R', 'P', 'Z'},
            {'U', 'N', 'O', 'I', 'C', 'A', 'C', 'I', 'F', 'I', 'S', 'L', 'A', 'F'},
            {'S', 'E', 'N', 'O', 'I', 'C', 'A', 'U', 'L', 'A', 'V', 'E', 'D', 'A'},
            {'P', 'I', 'A', 'L', 'U', 'M', 'N', 'O', 'S', 'E', 'N', 'A', 'E', 'T'},
            {'E', 'B', 'L', 'I', 'G', 'A', 'C', 'I', 'O', 'N', 'E', 'S', 'R', 'I'},
            {'N', 'E', 'U', 'A', 'P', 'E', 'L', 'A', 'C', 'I', 'O', 'N', 'E', 'T'},
            {'S', 'R', 'M', 'E', 'I', 'S', 'G', 'N', 'C', 'C', 'E', 'C', 'C', 'U'},
            {'I', 'N', 'I', 'V', 'E', 'R', 'S', 'I', 'S', 'O', 'C', 'I', 'A', 'L'},
            {'O', 'J', 'U', 'S', 'T', 'I', 'F', 'I', 'C', 'A', 'N', 'T', 'E', 'O'},
            {'N', 'D', 'T', 'D', 'E', 'S', 'A', 'I', 'R', 'O', 'S', 'E', 'S', 'A'},
            {'R', 'E', 'I', 'N', 'S', 'C', 'R', 'I', 'P', 'C', 'I', 'O', 'N', 'A'},
            {'R', 'E', 'G', 'L', 'A', 'M', 'E', 'N', 'T', 'O', 'A', 'A', 'M', 'M'},
            {'E', 'E', 'G', 'N', 'O', 'I', 'S', 'L', 'U', 'P', 'X', 'E', 'N', 'N'},
            {'O', 'B', 'L', 'I', 'G', 'A', 'C', 'E', 'Q', 'U', 'I', 'P', 'O', 'X'}
    };

    private static final int GRID_SIZE = 14;
    private GridLayout gridLayout;
    private TextView tvListaPalabras, tvScorePuntos;
    private Button btnRegresar;

    private final List<Word> wordsToFind = new ArrayList<>();
    private final List<TextView> selectedCells = new ArrayList<>();

    // CORRECCIÓN: Variables para el cálculo exacto de la línea
    private TextView startCell = null;
    private int wordsFoundCount = 0;
    private long currentLocalScore = 0;

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    private static class Word {
        String text;
        boolean found = false;
        Word(String text) { this.text = text; }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_word_search);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        gridLayout = findViewById(R.id.contenedor_cuadricula);
        tvListaPalabras = findViewById(R.id.lista_palabras);
        btnRegresar = findViewById(R.id.btn_regresar);
        tvScorePuntos = findViewById(R.id.tv_score_puntos);

        findViewById(R.id.btnBack).setOnClickListener(v -> finish());
        btnRegresar.setOnClickListener(v -> finish());

        initializeWords();
        generarCuadricula();
        setupTouchListener();

        // Obtenemos el puntaje una sola vez al iniciar
        fetchInitialScore();
    }

    private void initializeWords() {
        List<String> palabrasClave = Arrays.asList(
                "JUSTIFICANTE", "REGLAMENTO", "TITULO", "EQUIPO", "REINSCRIPCION",
                "APELACION", "SOCIAL", "SUSPENSION", "FALSIFICACION", "PROHIBICIONES"
        );
        for(String word : palabrasClave) {
            wordsToFind.add(new Word(word));
        }
    }

    // CORRECCIÓN: Evitar saturar Firebase obteniendo el score solo al inicio
    private void fetchInitialScore(){
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            db.collection("usuarios").document(currentUser.getUid()).get().addOnSuccessListener(documentSnapshot -> {
                if (documentSnapshot.exists()) {
                    Long score = documentSnapshot.getLong("puntaje");
                    if (score != null) {
                        currentLocalScore = score;
                        tvScorePuntos.setText(currentLocalScore + " Pts");
                    }
                }
            });
        }
    }

    private void generarCuadricula() {
        gridLayout.setColumnCount(GRID_SIZE);
        gridLayout.setRowCount(GRID_SIZE);
        for (int row = 0; row < GRID_SIZE; row++) {
            for (int col = 0; col < GRID_SIZE; col++) {
                TextView cellTextView = new TextView(this);
                cellTextView.setText(String.valueOf(matrizLetras[row][col]));
                cellTextView.setTextSize(18f);
                cellTextView.setTypeface(null, Typeface.BOLD);
                cellTextView.setTextColor(Color.WHITE);
                cellTextView.setGravity(Gravity.CENTER);
                GridLayout.LayoutParams params = new GridLayout.LayoutParams(GridLayout.spec(row, 1f), GridLayout.spec(col, 1f));
                params.width = 0;
                params.height = 0;
                cellTextView.setLayoutParams(params);
                cellTextView.setTag(new int[]{row, col});
                gridLayout.addView(cellTextView);
            }
        }
    }

    // CORRECCIÓN: Nueva lógica táctil que no se corta si mueves el dedo rápido
    private void setupTouchListener() {
        gridLayout.setOnTouchListener((v, event) -> {
            int action = event.getAction();
            TextView cell = getCellFromCoordinates(event.getX(), event.getY());

            switch (action) {
                case MotionEvent.ACTION_DOWN:
                    if (cell != null) {
                        startCell = cell;
                        updateSelectionPath(startCell, startCell);
                    }
                    break;
                case MotionEvent.ACTION_MOVE:
                    if (startCell != null && cell != null) {
                        updateSelectionPath(startCell, cell);
                    }
                    break;
                case MotionEvent.ACTION_UP:
                    if (startCell != null) {
                        processSelection();
                    }
                    startCell = null;
                    clearTemporarySelection();
                    break;
            }
            return true;
        });
    }

    private void updateSelectionPath(TextView start, TextView current) {
        clearTemporarySelection();
        int[] p1 = (int[]) start.getTag();
        int[] p2 = (int[]) current.getTag();

        int dr = p2[0] - p1[0];
        int dc = p2[1] - p1[1];

        // Validamos si es una línea recta (horizontal, vertical o diagonal perfecta)
        if (dr == 0 || dc == 0 || Math.abs(dr) == Math.abs(dc)) {
            int stepR = Integer.compare(dr, 0);
            int stepC = Integer.compare(dc, 0);
            int steps = Math.max(Math.abs(dr), Math.abs(dc));

            for (int i = 0; i <= steps; i++) {
                int r = p1[0] + (i * stepR);
                int c = p1[1] + (i * stepC);
                TextView cellInPath = getCellAt(r, c);
                if (cellInPath != null) {
                    selectedCells.add(cellInPath);
                    cellInPath.setBackgroundColor(Color.parseColor("#90E1BEE7")); // Efecto sombra
                }
            }
        }
    }

    private TextView getCellAt(int row, int col) {
        int index = (row * GRID_SIZE) + col;
        if (index >= 0 && index < gridLayout.getChildCount()) {
            return (TextView) gridLayout.getChildAt(index);
        }
        return null;
    }

    private void processSelection() {
        if (selectedCells.isEmpty()) return;

        StringBuilder selectedWord = new StringBuilder();
        for (TextView cell : selectedCells) {
            selectedWord.append(cell.getText());
        }

        checkForWordMatch(selectedWord.toString());
        checkForWordMatch(selectedWord.reverse().toString());
    }

    private void checkForWordMatch(String formedWord) {
        for (Word word : wordsToFind) {
            if (!word.found && word.text.equalsIgnoreCase(formedWord)) {
                word.found = true;
                wordsFoundCount++;

                // Actualizar puntaje local para evitar lags en UI
                currentLocalScore += 10;
                tvScorePuntos.setText(currentLocalScore + " Pts");

                FirebaseUser currentUser = mAuth.getCurrentUser();
                if (currentUser != null) {
                    DocumentReference userDocRef = db.collection("usuarios").document(currentUser.getUid());
                    userDocRef.update("puntaje", com.google.firebase.firestore.FieldValue.increment(10));
                }

                for (TextView cellInPath : selectedCells) {
                    cellInPath.setBackgroundColor(Color.parseColor("#C0CE93D8"));
                    cellInPath.setTextColor(Color.BLACK);
                }

                strikeThroughWordInList(word.text);
                Toast.makeText(this, "¡+10 Puntos! Encontraste '" + word.text + "'", Toast.LENGTH_SHORT).show();
                selectedCells.clear();

                if (wordsFoundCount == wordsToFind.size()) {
                    btnRegresar.setVisibility(View.VISIBLE);
                    Toast.makeText(this, "¡Felicidades, has encontrado todas las palabras!", Toast.LENGTH_LONG).show();
                }
                return;
            }
        }
    }

    private void clearTemporarySelection() {
        for (TextView cell : selectedCells) {
            if (cell.getCurrentTextColor() != Color.BLACK) { // Si no es negra, no ha sido descubierta
                cell.setBackgroundColor(Color.TRANSPARENT);
            }
        }
        selectedCells.clear();
    }

    private TextView getCellFromCoordinates(float x, float y) {
        for (int i = 0; i < gridLayout.getChildCount(); i++) {
            View child = gridLayout.getChildAt(i);
            Rect hitRect = new Rect();
            child.getHitRect(hitRect);
            if (hitRect.contains((int) x, (int) y)) {
                return (TextView) child;
            }
        }
        return null;
    }

    private String getDefinitionForWord(String word) {
        switch(word.toUpperCase()) {
            case "JUSTIFICANTE": return "1. DOCUMENTO QUE SE SOLICITA POR ENFERMEDAD.";
            case "REGLAMENTO": return "2. DOCUMENTO QUE HABLA SOBRE REGLAS.";
            case "TITULO": return "3. DOCUMENTO QUE RECIBES AL FINALIZAR LA CARRERA.";
            case "EQUIPO": return "4. RECURSO QUE EL ESTUDIANTE DEBE CUIDAR AL USAR EN LABORATORIOS.";
            case "REINSCRIPCION": return "5. TRÁMITE OBLIGATORIO PARA MANTENER EL ESTATUS DE ALUMNO ACTIVO CADA PERIODO.";
            case "APELACION": return "6. EL PROCESO DE REVISIÓN FORMAL DE UNA CALIFICACIÓN CON LA QUE EL ALUMNO NO ESTÁ DE ACUERDO.";
            case "SOCIAL": return "7. SERVICIO OBLIGATORIO QUE EL ALUMNO DEBE CUMPLIR COMO REQUISITO DE TITULACIÓN.";
            case "SUSPENSION": return "8. RESULTADO DIRECTO DE COMETER UNA FALTA GRAVE.";
            case "FALSIFICACION": return "9. LA CLASIFICACIÓN QUE RECIBE LA ALTERACIÓN DE HORARIOS, FECHAS O FOLIOS DE DOCUMENTOS OFICIALES.";
            case "PROHIBICIONES": return "10. NOMBRE DEL LISTADO DE ACCIONES QUE NO SON PERMITIDAS.";
        }
        return "";
    }

    private void strikeThroughWordInList(String foundWord) {
        String definition = getDefinitionForWord(foundWord);
        if (definition.isEmpty()) return;

        SpannableStringBuilder builder = new SpannableStringBuilder(tvListaPalabras.getText());
        String fullText = tvListaPalabras.getText().toString();
        int startIndex = fullText.indexOf(definition);

        if (startIndex != -1) {
            builder.setSpan(new StrikethroughSpan(), startIndex, startIndex + definition.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            builder.setSpan(new ForegroundColorSpan(Color.RED), startIndex, startIndex + definition.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            tvListaPalabras.setText(builder);
        }
    }
}
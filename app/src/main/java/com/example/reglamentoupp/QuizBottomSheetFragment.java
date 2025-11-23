package com.example.reglamentoupp;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.button.MaterialButton; // Importante para setStrokeColor
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class QuizBottomSheetFragment extends BottomSheetDialogFragment implements View.OnClickListener {

    private static final String ARG_CATEGORIA = "categoria";
    private static final String TAG = "QuizBottomSheet";
    private String categoriaJuego;
    private Pregunta preguntaActual;
    private FirebaseFirestore mStore;
    private OnQuizCompleteListener mListener;

    private TextView tvPregunta, tvQuizTitle, tvQuizFeedback;
    // Cambiamos a MaterialButton para usar métodos específicos de borde
    private MaterialButton btnOpcionA, btnOpcionB, btnOpcionC;
    private ProgressBar progressBar;
    private LinearLayout optionsContainer;

    // Interfaz para comunicarnos con GameLevelActivity
    public interface OnQuizCompleteListener {
        void onQuizComplete(int puntos, boolean esCorrecto);
    }

    public static QuizBottomSheetFragment newInstance(String categoria) {
        QuizBottomSheetFragment fragment = new QuizBottomSheetFragment();
        Bundle args = new Bundle();
        args.putString(ARG_CATEGORIA, categoria);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        // La actividad que hospeda (GameLevelActivity) debe implementar esta interfaz
        if (context instanceof OnQuizCompleteListener) {
            mListener = (OnQuizCompleteListener) context;
        } else {
            throw new RuntimeException(context.toString() + " must implement OnQuizCompleteListener");
        }
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            categoriaJuego = getArguments().getString(ARG_CATEGORIA);
        }
        mStore = FirebaseFirestore.getInstance();
        setCancelable(false); // Evita que el usuario cierre el quiz a la mitad
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_quiz_bottom_sheet, container, false);

        tvPregunta = view.findViewById(R.id.tv_quiz_pregunta);
        tvQuizTitle = view.findViewById(R.id.tv_quiz_title);
        tvQuizFeedback = view.findViewById(R.id.tv_quiz_feedback);

        // Casting a MaterialButton
        btnOpcionA = view.findViewById(R.id.btn_opcion_a);
        btnOpcionB = view.findViewById(R.id.btn_opcion_b);
        btnOpcionC = view.findViewById(R.id.btn_opcion_c);

        progressBar = view.findViewById(R.id.progress_bar_quiz);
        optionsContainer = view.findViewById(R.id.quiz_options_container);

        btnOpcionA.setOnClickListener(this);
        btnOpcionB.setOnClickListener(this);
        btnOpcionC.setOnClickListener(this);

        tvQuizTitle.setText("Quiz: " + categoriaJuego);
        loadQuestion();

        return view;
    }

    private void loadQuestion() {
        setLoading(true);

        mStore.collection("preguntas")
                .whereEqualTo("categoria", categoriaJuego)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (queryDocumentSnapshots.isEmpty()) {
                        Log.w(TAG, "No se encontraron preguntas para la categoría: " + categoriaJuego);
                        Toast.makeText(getContext(), "No hay preguntas para este nivel.", Toast.LENGTH_SHORT).show();
                        dismiss();
                        return;
                    }

                    List<Pregunta> preguntas = new ArrayList<>();
                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        preguntas.add(doc.toObject(Pregunta.class));
                    }

                    // Selecciona una pregunta al azar
                    Collections.shuffle(preguntas);
                    preguntaActual = preguntas.get(0);
                    displayQuestion();

                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error al cargar preguntas: ", e);
                    Toast.makeText(getContext(), "Error al cargar el quiz.", Toast.LENGTH_SHORT).show();
                    dismiss();
                });
    }

    private void setLoading(boolean isLoading) {
        if (isLoading) {
            progressBar.setVisibility(View.VISIBLE);
            optionsContainer.setVisibility(View.GONE);
            tvPregunta.setVisibility(View.GONE);
        } else {
            progressBar.setVisibility(View.GONE);
            optionsContainer.setVisibility(View.VISIBLE);
            tvPregunta.setVisibility(View.VISIBLE);
        }
    }

    private void displayQuestion() {
        if (preguntaActual == null) return;

        tvPregunta.setText(preguntaActual.getPregunta());
        btnOpcionA.setText(preguntaActual.getOpcionA());
        btnOpcionB.setText(preguntaActual.getOpcionB());
        btnOpcionC.setText(preguntaActual.getOpcionC());

        // Restaurar estado visual de los botones
        resetButtonStyles();
        setLoading(false);
    }

    @Override
    public void onClick(View v) {
        if (preguntaActual == null) return;

        MaterialButton clickedButton = (MaterialButton) v;
        String respuestaElegida = clickedButton.getText().toString();
        String respuestaCorrecta = preguntaActual.getRespuestaCorrecta();

        // Deshabilitar botones para evitar doble clic
        setButtonsEnabled(false);

        if (respuestaElegida.equals(respuestaCorrecta)) {
            // --- Respuesta Correcta ---
            showFeedback(true, clickedButton);
            mListener.onQuizComplete(10, true); // Enviar 10 puntos
        } else {
            // --- Respuesta Incorrecta ---
            showFeedback(false, clickedButton);
            mListener.onQuizComplete(0, false); // Enviar 0 puntos
        }

        // Cerrar el panel después de 2 segundos
        new Handler(Looper.getMainLooper()).postDelayed(this::dismiss, 2000);
    }

    // --- MÉTODOS CORREGIDOS (Solución al error de compilación y colores) ---

    private void showFeedback(boolean isCorrect, MaterialButton clickedButton) {
        tvQuizFeedback.setVisibility(View.VISIBLE);

        // Convertir 2dp a pixeles para el borde
        int strokeWidthPx = (int) (2 * getResources().getDisplayMetrics().density);

        if (isCorrect) {
            tvQuizFeedback.setText("¡Correcto!");
            tvQuizFeedback.setTextColor(ContextCompat.getColor(getContext(), R.color.game_success));

            // Estilo CORRECTO (Fondo verde claro, Texto verde oscuro, Borde verde)
            clickedButton.setBackgroundTintList(ContextCompat.getColorStateList(getContext(), R.color.game_success_bg));
            clickedButton.setTextColor(ContextCompat.getColor(getContext(), R.color.game_success));
            clickedButton.setStrokeColor(ContextCompat.getColorStateList(getContext(), R.color.game_success));
            clickedButton.setStrokeWidth(strokeWidthPx);

        } else {
            tvQuizFeedback.setText("Incorrecto");
            tvQuizFeedback.setTextColor(ContextCompat.getColor(getContext(), R.color.game_fail));

            // Estilo INCORRECTO (Fondo rojo claro, Texto rojo, Borde rojo)
            clickedButton.setBackgroundTintList(ContextCompat.getColorStateList(getContext(), R.color.game_fail_bg));
            clickedButton.setTextColor(ContextCompat.getColor(getContext(), R.color.game_fail));
            clickedButton.setStrokeColor(ContextCompat.getColorStateList(getContext(), R.color.game_fail));
            clickedButton.setStrokeWidth(strokeWidthPx);

            // RESALTAR LA RESPUESTA CORRECTA AUTOMÁTICAMENTE
            MaterialButton btnCorrecto = null;
            if (btnOpcionA.getText().toString().equals(preguntaActual.getRespuestaCorrecta())) btnCorrecto = btnOpcionA;
            else if (btnOpcionB.getText().toString().equals(preguntaActual.getRespuestaCorrecta())) btnCorrecto = btnOpcionB;
            else if (btnOpcionC.getText().toString().equals(preguntaActual.getRespuestaCorrecta())) btnCorrecto = btnOpcionC;

            if (btnCorrecto != null) {
                btnCorrecto.setBackgroundTintList(ContextCompat.getColorStateList(getContext(), R.color.game_success_bg));
                btnCorrecto.setTextColor(ContextCompat.getColor(getContext(), R.color.game_success));
                btnCorrecto.setStrokeColor(ContextCompat.getColorStateList(getContext(), R.color.game_success));
                btnCorrecto.setStrokeWidth(strokeWidthPx);
            }
        }
    }

    private void resetButtonStyles() {
        setButtonsEnabled(true);
        tvQuizFeedback.setVisibility(View.GONE);

        MaterialButton[] buttons = {btnOpcionA, btnOpcionB, btnOpcionC};
        for (MaterialButton btn : buttons) {
            // RESTAURAR ESTILO ORIGINAL (Fondo Morado, Texto Blanco, Sin borde)
            btn.setBackgroundTintList(ContextCompat.getColorStateList(getContext(), R.color.upp_primary));
            btn.setTextColor(ContextCompat.getColor(getContext(), R.color.white));
            btn.setStrokeWidth(0); // Eliminar el borde de correcto/incorrecto
        }
    }

    private void setButtonsEnabled(boolean enabled) {
        btnOpcionA.setEnabled(enabled);
        btnOpcionB.setEnabled(enabled);
        btnOpcionC.setEnabled(enabled);
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }
}
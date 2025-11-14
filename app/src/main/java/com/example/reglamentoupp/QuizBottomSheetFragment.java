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
    private Button btnOpcionA, btnOpcionB, btnOpcionC;
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

        Button clickedButton = (Button) v;
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

    private void showFeedback(boolean isCorrect, Button clickedButton) {
        tvQuizFeedback.setVisibility(View.VISIBLE);

        if (isCorrect) {
            tvQuizFeedback.setText("¡Correcto!");
            tvQuizFeedback.setTextColor(ContextCompat.getColor(getContext(), R.color.game_success));
            clickedButton.setBackground(ContextCompat.getDrawable(getContext(), R.drawable.button_quiz_correct));
            clickedButton.setTextColor(ContextCompat.getColor(getContext(), R.color.game_success));
        } else {
            tvQuizFeedback.setText("Incorrecto");
            tvQuizFeedback.setTextColor(ContextCompat.getColor(getContext(), R.color.game_fail));
            clickedButton.setBackground(ContextCompat.getDrawable(getContext(), R.drawable.button_quiz_incorrect));
            clickedButton.setTextColor(ContextCompat.getColor(getContext(), R.color.game_fail));

            // Resaltar la correcta
            if (btnOpcionA.getText().toString().equals(preguntaActual.getRespuestaCorrecta())) {
                btnOpcionA.setBackground(ContextCompat.getDrawable(getContext(), R.drawable.button_quiz_correct));
                btnOpcionA.setTextColor(ContextCompat.getColor(getContext(), R.color.game_success));
            } else if (btnOpcionB.getText().toString().equals(preguntaActual.getRespuestaCorrecta())) {
                btnOpcionB.setBackground(ContextCompat.getDrawable(getContext(), R.drawable.button_quiz_correct));
                btnOpcionB.setTextColor(ContextCompat.getColor(getContext(), R.color.game_success));
            } else if (btnOpcionC.getText().toString().equals(preguntaActual.getRespuestaCorrecta())) {
                btnOpcionC.setBackground(ContextCompat.getDrawable(getContext(), R.drawable.button_quiz_correct));
                btnOpcionC.setTextColor(ContextCompat.getColor(getContext(), R.color.game_success));
            }
        }
    }

    private void setButtonsEnabled(boolean enabled) {
        btnOpcionA.setEnabled(enabled);
        btnOpcionB.setEnabled(enabled);
        btnOpcionC.setEnabled(enabled);
    }

    private void resetButtonStyles() {
        setButtonsEnabled(true);
        tvQuizFeedback.setVisibility(View.GONE);

        Button[] buttons = {btnOpcionA, btnOpcionB, btnOpcionC};
        for (Button btn : buttons) {
            btn.setBackground(ContextCompat.getDrawable(getContext(), R.drawable.button_quiz_default));
            btn.setTextColor(ContextCompat.getColor(getContext(), R.color.game_button_default_text));
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }
}
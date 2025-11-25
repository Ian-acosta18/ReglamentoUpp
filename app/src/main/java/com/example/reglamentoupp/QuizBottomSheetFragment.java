package com.example.reglamentoupp;

import android.content.Context;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.button.MaterialButton;
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
    // Usamos MaterialButton para poder cambiar bordes y colores fácilmente
    private MaterialButton btnOpcionA, btnOpcionB, btnOpcionC;
    private ProgressBar progressBar;
    private LinearLayout optionsContainer;

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
        setCancelable(false);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_quiz_bottom_sheet, container, false);

        tvPregunta = view.findViewById(R.id.tv_quiz_pregunta);
        tvQuizTitle = view.findViewById(R.id.tv_quiz_title);
        tvQuizFeedback = view.findViewById(R.id.tv_quiz_feedback);

        // Inicializamos como MaterialButton
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
                        dismiss();
                        return;
                    }
                    List<Pregunta> preguntas = new ArrayList<>();
                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        preguntas.add(doc.toObject(Pregunta.class));
                    }
                    Collections.shuffle(preguntas);
                    preguntaActual = preguntas.get(0);
                    displayQuestion();
                })
                .addOnFailureListener(e -> {
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

        resetButtonStyles(); // Esto pondrá los colores por defecto (Morado + Blanco)
        setLoading(false);
    }

    @Override
    public void onClick(View v) {
        if (preguntaActual == null) return;
        MaterialButton clickedButton = (MaterialButton) v;
        String respuestaElegida = clickedButton.getText().toString();
        String respuestaCorrecta = preguntaActual.getRespuestaCorrecta();

        setButtonsEnabled(false);

        if (respuestaElegida.equals(respuestaCorrecta)) {
            showFeedback(true, clickedButton);
            mListener.onQuizComplete(10, true);
        } else {
            showFeedback(false, clickedButton);
            mListener.onQuizComplete(0, false);
        }

        new Handler(Looper.getMainLooper()).postDelayed(this::dismiss, 2000);
    }

    // ================================================================
    //  CORRECCIÓN DE COLORES AQUÍ
    // ================================================================

    private void showFeedback(boolean isCorrect, MaterialButton clickedButton) {
        tvQuizFeedback.setVisibility(View.VISIBLE);

        if (isCorrect) {
            tvQuizFeedback.setText("¡Correcto!");
            tvQuizFeedback.setTextColor(ContextCompat.getColor(getContext(), R.color.game_success)); // Texto verde

            // FONDO VERDE - TEXTO BLANCO
            setButtonColor(clickedButton, R.color.game_success, R.color.white);

        } else {
            tvQuizFeedback.setText("Incorrecto");
            tvQuizFeedback.setTextColor(ContextCompat.getColor(getContext(), R.color.game_fail)); // Texto rojo

            // FONDO ROJO - TEXTO BLANCO
            setButtonColor(clickedButton, R.color.game_fail, R.color.white);

            // Buscar la respuesta correcta para pintarla de verde
            highlightCorrectAnswer();
        }
    }

    private void highlightCorrectAnswer() {
        MaterialButton correctBtn = null;
        if (btnOpcionA.getText().toString().equals(preguntaActual.getRespuestaCorrecta())) correctBtn = btnOpcionA;
        else if (btnOpcionB.getText().toString().equals(preguntaActual.getRespuestaCorrecta())) correctBtn = btnOpcionB;
        else if (btnOpcionC.getText().toString().equals(preguntaActual.getRespuestaCorrecta())) correctBtn = btnOpcionC;

        if (correctBtn != null) {
            // FONDO VERDE - TEXTO BLANCO
            setButtonColor(correctBtn, R.color.game_success, R.color.white);
        }
    }

    private void resetButtonStyles() {
        setButtonsEnabled(true);
        tvQuizFeedback.setVisibility(View.GONE);

        MaterialButton[] buttons = {btnOpcionA, btnOpcionB, btnOpcionC};
        for (MaterialButton btn : buttons) {
            // ESTADO NORMAL: FONDO MORADO (Primary) - TEXTO BLANCO
            setButtonColor(btn, R.color.upp_primary, R.color.white);
            btn.setStrokeWidth(0);
        }
    }

    // Método auxiliar para asegurar que el contraste siempre sea correcto
    private void setButtonColor(MaterialButton btn, int bgColorRes, int textColorRes) {
        Context ctx = getContext();
        if (ctx == null) return;

        // Establece el color de fondo (Tint)
        btn.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(ctx, bgColorRes)));

        // Establece el color del texto
        btn.setTextColor(ContextCompat.getColor(ctx, textColorRes));

        // Nos aseguramos de que el icono (si tuviera) también se pinte
        btn.setIconTint(ColorStateList.valueOf(ContextCompat.getColor(ctx, textColorRes)));
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
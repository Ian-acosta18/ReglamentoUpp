package com.example.reglamentoupp;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
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

import com.airbnb.lottie.LottieAnimationView;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class QuizBottomSheetFragment extends BottomSheetDialogFragment implements View.OnClickListener {

    private static final String ARG_CATEGORIA = "categoria";
    private String categoriaJuego;
    private Pregunta preguntaActual;
    private FirebaseFirestore mStore;
    private OnQuizCompleteListener mListener;

    // UI
    private TextView tvPregunta, tvQuizTitle, tvQuizFeedback;
    private MaterialButton btnOpcionA, btnOpcionB, btnOpcionC;
    private ProgressBar progressBar;
    private LinearLayout optionsContainer;
    private LottieAnimationView lottieFeedback;

    // Audio
    private MediaPlayer mediaPlayer;

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
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_quiz_bottom_sheet, container, false);

        tvPregunta = view.findViewById(R.id.tv_quiz_pregunta);
        tvQuizTitle = view.findViewById(R.id.tv_quiz_title);
        tvQuizFeedback = view.findViewById(R.id.tv_quiz_feedback);
        lottieFeedback = view.findViewById(R.id.lottieFeedback);

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
                        Toast.makeText(getContext(), "No hay preguntas para esta categoría", Toast.LENGTH_SHORT).show();
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
                    Toast.makeText(getContext(), "Error al cargar.", Toast.LENGTH_SHORT).show();
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

        resetButtonStyles();
        setLoading(false);
    }

    @Override
    public void onClick(View v) {
        if (preguntaActual == null) return;
        MaterialButton clickedButton = (MaterialButton) v;
        String respuestaElegida = clickedButton.getText().toString();
        String respuestaCorrecta = preguntaActual.getRespuestaCorrecta();

        setButtonsEnabled(false);

        boolean esCorrecto = respuestaElegida.equals(respuestaCorrecta);
        handleResult(esCorrecto, clickedButton);

        // Notificar a la actividad principal
        if (mListener != null) {
            mListener.onQuizComplete(esCorrecto ? 10 : 0, esCorrecto);
        }

        // Cerrar automáticamente después de 2.5 segundos
        new Handler(Looper.getMainLooper()).postDelayed(this::dismiss, 2500);
    }

    private void handleResult(boolean isCorrect, MaterialButton clickedButton) {
        tvQuizFeedback.setVisibility(View.VISIBLE);
        lottieFeedback.setVisibility(View.VISIBLE);
        tvPregunta.setVisibility(View.GONE); // Ocultar pregunta para dar espacio a la animación

        if (isCorrect) {
            // --- RESPUESTA CORRECTA ---
            playSound(R.raw.correct_ding);

            // UI Feedback
            tvQuizFeedback.setText("¡Excelente! Respuesta Correcta");
            tvQuizFeedback.setTextColor(ContextCompat.getColor(getContext(), R.color.game_success));

            // Animación
            lottieFeedback.setAnimation(R.raw.happy_sun);
            lottieFeedback.playAnimation();

            // Estilo Botón
            setButtonStyle(clickedButton, R.color.game_success, R.color.white, R.drawable.ic_check_circle);

        } else {
            // --- RESPUESTA INCORRECTA ---
            playSound(R.raw.megaman_x_error);

            // UI Feedback
            tvQuizFeedback.setText("¡Oh no! Respuesta Incorrecta");
            tvQuizFeedback.setTextColor(ContextCompat.getColor(getContext(), R.color.game_fail));

            // Animación
            lottieFeedback.setAnimation(R.raw.angry_thunderstorm);
            lottieFeedback.playAnimation();

            // Estilo Botón (Rojo)
            setButtonStyle(clickedButton, R.color.game_fail, R.color.white, R.drawable.ic_prohibiciones); // Ojo: usa un icono de error si tienes, sino el de prohibiciones funciona

            // Mostrar cuál era la correcta
            highlightCorrectAnswer();
        }
    }

    private void highlightCorrectAnswer() {
        MaterialButton correctBtn = null;
        if (btnOpcionA.getText().toString().equals(preguntaActual.getRespuestaCorrecta())) correctBtn = btnOpcionA;
        else if (btnOpcionB.getText().toString().equals(preguntaActual.getRespuestaCorrecta())) correctBtn = btnOpcionB;
        else if (btnOpcionC.getText().toString().equals(preguntaActual.getRespuestaCorrecta())) correctBtn = btnOpcionC;

        if (correctBtn != null) {
            setButtonStyle(correctBtn, R.color.game_success, R.color.white, R.drawable.ic_check_circle);
        }
    }

    private void playSound(int soundResId) {
        try {
            if (mediaPlayer != null) {
                mediaPlayer.release();
            }
            mediaPlayer = MediaPlayer.create(getContext(), soundResId);
            if (mediaPlayer != null) {
                mediaPlayer.start();
                mediaPlayer.setOnCompletionListener(MediaPlayer::release);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void resetButtonStyles() {
        setButtonsEnabled(true);
        tvQuizFeedback.setVisibility(View.GONE);
        lottieFeedback.setVisibility(View.GONE);
        tvPregunta.setVisibility(View.VISIBLE);

        MaterialButton[] buttons = {btnOpcionA, btnOpcionB, btnOpcionC};
        for (MaterialButton btn : buttons) {
            // Estilo por defecto (Outlined)
            btn.setBackgroundTintList(ColorStateList.valueOf(Color.WHITE));
            btn.setTextColor(ContextCompat.getColor(getContext(), R.color.upp_primary_dark));
            btn.setStrokeColor(ColorStateList.valueOf(ContextCompat.getColor(getContext(), R.color.upp_primary)));
            btn.setStrokeWidth(dpToPx(1));
            btn.setIcon(ContextCompat.getDrawable(getContext(), R.drawable.ic_check_circle)); // Icono por defecto (círculo vacío sería mejor si tuvieras)
            btn.setIconTint(ColorStateList.valueOf(ContextCompat.getColor(getContext(), R.color.upp_primary_light)));
        }
    }

    private void setButtonStyle(MaterialButton btn, int bgColorRes, int textColorRes, int iconRes) {
        if (getContext() == null) return;
        btn.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(getContext(), bgColorRes)));
        btn.setTextColor(ContextCompat.getColor(getContext(), textColorRes));
        btn.setStrokeWidth(0);
        btn.setIcon(ContextCompat.getDrawable(getContext(), iconRes));
        btn.setIconTint(ColorStateList.valueOf(ContextCompat.getColor(getContext(), textColorRes)));
    }

    private void setButtonsEnabled(boolean enabled) {
        btnOpcionA.setEnabled(enabled);
        btnOpcionB.setEnabled(enabled);
        btnOpcionC.setEnabled(enabled);
    }

    private int dpToPx(int dp) {
        float density = getResources().getDisplayMetrics().density;
        return Math.round((float) dp * density);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mediaPlayer != null) {
            mediaPlayer.release();
            mediaPlayer = null;
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }
}
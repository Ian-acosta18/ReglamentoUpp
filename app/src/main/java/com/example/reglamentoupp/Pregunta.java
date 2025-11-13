package com.example.reglamentoupp;



public class Pregunta {

    // Asumiendo que estos son tus campos en Firestore
    private String pregunta;
    private String opcionA;
    private String opcionB;
    private String opcionC;
    private String respuestaCorrecta;
    private String categoria;

    // Constructor vacío (requerido por Firestore para .toObject())
    public Pregunta() {}

    // --- Getters (Requeridos por Firestore) ---
    public String getPregunta() {
        return pregunta;
    }

    public String getOpcionA() {
        return opcionA;
    }

    public String getOpcionB() {
        return opcionB;
    }

    public String getOpcionC() {
        return opcionC;
    }

    public String getRespuestaCorrecta() {
        return respuestaCorrecta;
    }

    public String getCategoria() {
        return categoria;
    }
}

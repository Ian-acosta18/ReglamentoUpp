package com.example.reglamentoupp;

public class Pregunta {

    private String pregunta;
    private String opcionA;
    private String opcionB;
    private String opcionC;
    private String respuestaCorrecta;
    private String categoria;

    // Constructor vacío (requerido por Firestore para .toObject())
    public Pregunta() {}

    // --- NUEVO CONSTRUCTOR ---
    // (Este lo usaremos en nuestro script para crear las preguntas)
    public Pregunta(String categoria, String pregunta, String opcionA, String opcionB, String opcionC, String respuestaCorrecta) {
        this.categoria = categoria;
        this.pregunta = pregunta;
        this.opcionA = opcionA;
        this.opcionB = opcionB;
        this.opcionC = opcionC;
        this.respuestaCorrecta = respuestaCorrecta;
    }

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
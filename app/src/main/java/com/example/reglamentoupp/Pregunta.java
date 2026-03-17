package com.example.reglamentoupp;

public class Pregunta {

    private String pregunta;
    private String opcionA;
    private String opcionB;
    private String opcionC;
    private String respuestaCorrecta;
    private String categoria;

    // Constructor vacío (Requerido por Firebase Firestore)
    public Pregunta() {}

    // --- CONSTRUCTOR CLAVE (Este soluciona tu error de compilación) ---
    public Pregunta(String pregunta, String opcionA, String opcionB, String opcionC, String respuestaCorrecta) {
        this.pregunta = pregunta;
        this.opcionA = opcionA;
        this.opcionB = opcionB;
        this.opcionC = opcionC;
        this.respuestaCorrecta = respuestaCorrecta;
        this.categoria = "Reglamento UPP"; // Categoría por defecto
    }

    public String getPregunta() { return pregunta; }
    public String getOpcionA() { return opcionA; }
    public String getOpcionB() { return opcionB; }
    public String getOpcionC() { return opcionC; }
    public String getRespuestaCorrecta() { return respuestaCorrecta; }
    public String getCategoria() { return categoria; }
}
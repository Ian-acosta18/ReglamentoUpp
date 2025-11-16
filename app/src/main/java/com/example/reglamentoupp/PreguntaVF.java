package com.example.reglamentoupp;

public class PreguntaVF {
    private String afirmacion;
    private boolean respuesta; // true para Verdadero, false para Falso

    public PreguntaVF() {} // Constructor vacío para Firestore

    public PreguntaVF(String afirmacion, boolean respuesta) {
        this.afirmacion = afirmacion;
        this.respuesta = respuesta;
    }

    public String getAfirmacion() {
        return afirmacion;
    }

    public boolean isRespuesta() {
        return respuesta;
    }
}
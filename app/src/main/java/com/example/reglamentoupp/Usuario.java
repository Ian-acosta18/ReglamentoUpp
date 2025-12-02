package com.example.reglamentoupp;

public class Usuario {
    private String nombre;
    private String apellidos;
    private long puntaje;
    private String fotoUrl; // Nuevo campo

    // Constructor vacío requerido por Firestore
    public Usuario() {}

    public Usuario(String nombre, String apellidos, long puntaje, String fotoUrl) {
        this.nombre = nombre;
        this.apellidos = apellidos;
        this.puntaje = puntaje;
        this.fotoUrl = fotoUrl;
    }

    public String getNombre() { return nombre; }
    public String getApellidos() { return apellidos; }
    public long getPuntaje() { return puntaje; }
    public String getFotoUrl() { return fotoUrl; } // Nuevo getter
}
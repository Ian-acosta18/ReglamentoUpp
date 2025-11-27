package com.example.reglamentoupp;

public class Usuario {
    private String nombre;
    private String apellidos;
    private long puntaje;

    // Constructor vacío requerido por Firestore
    public Usuario() {}

    public Usuario(String nombre, String apellidos, long puntaje) {
        this.nombre = nombre;
        this.apellidos = apellidos;
        this.puntaje = puntaje;
    }

    public String getNombre() { return nombre; }
    public String getApellidos() { return apellidos; }
    public long getPuntaje() { return puntaje; }
}
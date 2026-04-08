package com.example.reglamentoupp;

public class Usuario {
    private String nombre;
    private String apellidos;
    private String email;
    private String telefono;
    private int puntaje;
    private int nivelDesbloqueado; // Coincide con Firestore
    private String fotoUrl;        // Coincide con Firestore

    // Constructor vacío obligatorio para Firebase
    public Usuario() {
    }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getApellidos() { return apellidos; }
    public void setApellidos(String apellidos) { this.apellidos = apellidos; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getTelefono() { return telefono; }
    public void setTelefono(String telefono) { this.telefono = telefono; }

    public int getPuntaje() { return puntaje; }
    public void setPuntaje(int puntaje) { this.puntaje = puntaje; }

    public int getNivelDesbloqueado() { return nivelDesbloqueado; }
    public void setNivelDesbloqueado(int nivelDesbloqueado) { this.nivelDesbloqueado = nivelDesbloqueado; }

    public String getFotoUrl() { return fotoUrl; }
    public void setFotoUrl(String fotoUrl) { this.fotoUrl = fotoUrl; }
}
package com.example.reglamentoupp;

public class Usuario {
    private String nombre;
    private String correo;
    private int puntaje;
    private String fotoPerfilUrl;
    private int nivel;

    // Constructor vacío obligatorio para Firebase
    public Usuario() {
    }

    public Usuario(String nombre, String correo, int puntaje, String fotoPerfilUrl, int nivel) {
        this.nombre = nombre;
        this.correo = correo;
        this.puntaje = puntaje;
        this.fotoPerfilUrl = fotoPerfilUrl;
        this.nivel = nivel;
    }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getCorreo() { return correo; }
    public void setCorreo(String correo) { this.correo = correo; }

    public int getPuntaje() { return puntaje; }
    public void setPuntaje(int puntaje) { this.puntaje = puntaje; }

    public String getFotoPerfilUrl() { return fotoPerfilUrl; }
    public void setFotoPerfilUrl(String fotoPerfilUrl) { this.fotoPerfilUrl = fotoPerfilUrl; }

    public int getNivel() { return nivel; }
    public void setNivel(int nivel) { this.nivel = nivel; }
}
package com.example.reglamentoupp;

public class ReglamentoItem {
    private final String texto;
    private final int audioResId;

    public ReglamentoItem(String texto, int audioResId) {
        this.texto = texto;
        this.audioResId = audioResId;
    }

    public String getTexto() {
        return texto;
    }

    public int getAudioResId() {
        return audioResId;
    }
}
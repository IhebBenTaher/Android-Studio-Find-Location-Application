package com.example.projetmap;

public class Position {
    private int idPosition;
    private double longitude;
    private double latitude;
    private String numero;
    private String pseudo;
    public Position(int idPosition, double longitude, double latitude, String numero, String pseudo) {
        this.idPosition = idPosition;
        this.longitude = longitude;
        this.latitude = latitude;
        this.numero = numero;
        this.pseudo = pseudo;
    }

    public int getIdPosition() {
        return idPosition;
    }

    public void setIdPosition(int idPosition) {
        this.idPosition = idPosition;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public String getNumero() {
        return numero;
    }

    public void setNumero(String numero) {
        this.numero = numero;
    }

    public String getPseudo() {
        return pseudo;
    }

    public void setPseudo(String pseudo) {
        this.pseudo = pseudo;
    }
}

package pt.ipp.isep.dei.domain;

public class Locomotiva {
    private final int idLocomotiva;
    private final String modelo;
    private final String tipo; // "diesel" ou "eletrica"

    public Locomotiva(int idLocomotiva, String modelo, String tipo) {
        this.idLocomotiva = idLocomotiva;
        this.modelo = modelo;
        this.tipo = tipo;
    }

    public int getIdLocomotiva() {
        return idLocomotiva;
    }

    public String getModelo() {
        return modelo;
    }

    public String getTipo() {
        return tipo;
    }

    @Override
    public String toString() {
        return String.format("ID: %d - Modelo %s (%s)", idLocomotiva, modelo, tipo);
    }
}
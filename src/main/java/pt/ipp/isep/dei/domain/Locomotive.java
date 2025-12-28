package pt.ipp.isep.dei.domain;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.property.DoubleProperty;

public class Locomotive {

    private final int idLocomotiva;
    private final String modelo;
    private final String tipo;
    private final double powerKW;

    // --- Physics Fields ---
    private double lengthMeters;
    private double totalWeightKg; // Tara

    public Locomotive(int idLocomotiva, String modelo, String tipo, double powerKW) {
        this.idLocomotiva = idLocomotiva;
        this.modelo = modelo;
        this.tipo = tipo;
        this.powerKW = powerKW;
        // Valores default se não forem setados explicitamente
        this.lengthMeters = 20.0;
        this.totalWeightKg = 80000.0;
    }

    public int getIdLocomotiva() { return idLocomotiva; }
    public String getLocomotiveId() { return String.valueOf(idLocomotiva); }
    public String getModelo() { return modelo; }
    public String getTipo() { return tipo; }
    public double getPowerKW() { return powerKW; }
    public double getPowerKw() { return powerKW; } // Alias para compatibilidade

    public double getLengthMeters() { return lengthMeters; }
    public void setLengthMeters(double lengthMeters) { this.lengthMeters = lengthMeters; }

    public double getTotalWeightKg() { return totalWeightKg; }
    public void setTotalWeightKg(double totalWeightKg) { this.totalWeightKg = totalWeightKg; }

    public StringProperty locomotiveIdProperty() { return new SimpleStringProperty(String.valueOf(idLocomotiva)); }
    public StringProperty modelProperty() { return new SimpleStringProperty(modelo); }
    public StringProperty typeProperty() { return new SimpleStringProperty(tipo); }
    public DoubleProperty powerKWProperty() { return new SimpleDoubleProperty(powerKW); }

    @Override
    public String toString() {
        return String.format("Loco %d (%s) - %.0f kW", idLocomotiva, modelo, powerKW);
    }
}
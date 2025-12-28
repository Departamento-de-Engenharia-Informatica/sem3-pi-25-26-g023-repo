package pt.ipp.isep.dei.domain;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.property.IntegerProperty;

import java.util.ArrayList;
import java.util.List;

public class Wagon {

    // --- Railway/CRUD Fields (Immutable) ---
    private final String idWagon;
    private final int modelId;
    private final int serviceYear;

    // --- Physics/Validation Fields (Transientes) ---
    private double lengthMeters;
    private double grossWeightKg; // Tara + Carga Máxima

    // --- WMS Fields ---
    private final List<Box> boxes = new ArrayList<>();

    public Wagon(String idWagon, int modelId, int serviceYear) {
        this.idWagon = idWagon;
        this.modelId = modelId;
        this.serviceYear = serviceYear;
    }

    public Wagon(String wagonId) {
        this.idWagon = wagonId;
        this.modelId = 0;
        this.serviceYear = 0;
    }

    public String getIdWagon() { return idWagon; }
    public String getWagonId() { return idWagon; }
    public int getModelId() { return modelId; }
    public int getServiceYear() { return serviceYear; }

    public double getLengthMeters() { return lengthMeters; }
    public void setLengthMeters(double lengthMeters) { this.lengthMeters = lengthMeters; }

    public double getGrossWeightKg() { return grossWeightKg; }
    public void setGrossWeightKg(double grossWeightKg) { this.grossWeightKg = grossWeightKg; }

    public void addBox(Box b) { boxes.add(b); }
    public List<Box> getBoxes() { return boxes; }

    public StringProperty wagonIdProperty() { return new SimpleStringProperty(idWagon); }
    public StringProperty modelIdProperty() { return new SimpleStringProperty(String.valueOf(modelId)); }
    public IntegerProperty serviceYearProperty() { return new SimpleIntegerProperty(serviceYear); }

    @Override
    public String toString() {
        return String.format("Wagon %s [%.1fm, %.0ft]", idWagon, lengthMeters, grossWeightKg/1000.0);
    }
}
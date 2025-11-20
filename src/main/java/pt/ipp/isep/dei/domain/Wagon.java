package pt.ipp.isep.dei.domain;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.property.IntegerProperty;

import java.util.ArrayList;
import java.util.List;

public class Wagon {

    // --- Railway/CRUD Fields (Immutable) ---
    private final String idWagon; // <--- CORREÇÃO: ID AGORA É STRING
    private final int modelId;
    private final int serviceYear;

    // --- WMS Fields ---
    private final List<Box> boxes = new ArrayList<>();

    /**
     * Constructs a Wagon object used for CRUD/DB loading (String ID).
     */
    public Wagon(String idWagon, int modelId, int serviceYear) { // <--- CONSTRUTOR ALTERADO
        this.idWagon = idWagon;
        this.modelId = modelId;
        this.serviceYear = serviceYear;
    }

    /**
     * Constructor for WMS loading where a String ID is used (Original usage).
     */
    public Wagon(String wagonId) {
        this.idWagon = wagonId; // Não há mais conversão problemática
        this.modelId = 0;
        this.serviceYear = 0;
    }


    // --- Getters ---

    public String getIdWagon() { // <--- RETORNA STRING
        return idWagon;
    }

    public String getWagonId() { // <--- MANTIDO PARA COMPATIBILIDADE WMS (retorna String)
        return idWagon;
    }

    public int getModelId() {
        return modelId;
    }

    public int getServiceYear() {
        return serviceYear;
    }

    // --- WMS methods ---
    public void addBox(Box b) { boxes.add(b); }
    public List<Box> getBoxes() { return boxes; }

    // --- JavaFX Property Methods ---

    public StringProperty wagonIdProperty() {
        return new SimpleStringProperty(idWagon);
    }

    public StringProperty modelIdProperty() {
        return new SimpleStringProperty(String.valueOf(modelId));
    }

    public IntegerProperty serviceYearProperty() {
        return new SimpleIntegerProperty(serviceYear);
    }
}
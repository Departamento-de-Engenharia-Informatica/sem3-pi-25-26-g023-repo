package pt.ipp.isep.dei.domain;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.property.DoubleProperty;


/**
 * Represents a locomotive in the railway system.
 * <p>
 * Each locomotive has:
 * <ul>
 * <li>A unique ID (int).</li>
 * <li>A model name.</li>
 * <li>A type, either "diesel" or "electric".</li>
 * <li>A power rating in kW (double).</li>
 * </ul>
 * This class is immutable; all fields are final and can only be set via the constructor.
 */
public class Locomotive {

    private final int idLocomotiva;
    private final String modelo;
    private final String tipo; // "diesel" or "electric"
    private final double powerKW; // <--- MUDANÇA: AGORA É POTÊNCIA (kW)

    /**
     * Constructs a Locomotive object.
     *
     * @param idLocomotiva Unique identifier of the locomotive.
     * @param modelo       Model name of the locomotive.
     * @param tipo         Type of the locomotive ("diesel" or "eletrica").
     * @param powerKW      Power rating of the locomotive in kW.
     */
    public Locomotive(int idLocomotiva, String modelo, String tipo, double powerKW) {
        this.idLocomotiva = idLocomotiva;
        this.modelo = modelo;
        this.tipo = tipo;
        this.powerKW = powerKW;
    }

    /**
     * Returns the unique ID of the locomotive (como int).
     * @return Locomotive ID.
     */
    public int getIdLocomotiva() {
        return idLocomotiva;
    }

    /**
     * Returns the unique ID of the locomotive (como String, para UI).
     * @return Locomotive ID como String.
     */
    public String getLocomotiveId() {
        return String.valueOf(idLocomotiva);
    }

    /**
     * Returns the model name of the locomotive.
     * @return Model name.
     */
    public String getModelo() {
        return modelo;
    }

    /**
     * Returns the type of the locomotive ("diesel" or "electric").
     * @return Type as a string.
     */
    public String getTipo() {
        return tipo;
    }

    /**
     * Returns the power rating of the locomotive in kW.
     * @return Power in kW.
     */
    public double getPowerKW() { // <--- GETTER RENOMEADO
        return powerKW;
    }

    // -------------------------------------------------------------------
    // --- MÉTODOS JavaFX Property (para TableView Binding) ---
    // -------------------------------------------------------------------

    public StringProperty locomotiveIdProperty() {
        return new SimpleStringProperty(String.valueOf(idLocomotiva));
    }

    public StringProperty modelProperty() {
        return new SimpleStringProperty(modelo);
    }

    public StringProperty typeProperty() {
        return new SimpleStringProperty(tipo);
    }

    public DoubleProperty powerKWProperty() { // <--- PROPERTY RENOMEADA
        return new SimpleDoubleProperty(powerKW);
    }

    // -------------------------------------------------------------------

    @Override
    public String toString() {
        // MUDANÇA NA SAÍDA TEXTUAL
        return String.format("ID: %d - Modelo %s (%s) - Power: %.0f kW", idLocomotiva, modelo, tipo, powerKW);
    }
}
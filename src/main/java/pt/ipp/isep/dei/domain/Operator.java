package pt.ipp.isep.dei.domain;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

/**
 * Model for the OPERATOR table, compatible with JavaFX TableView.
 *
 * <p>This class uses {@link javafx.beans.property.StringProperty} for observable
 * data binding, which is essential for displaying and updating data in a
 * {@link javafx.scene.control.TableView} in JavaFX applications.</p>
 */
public class Operator {

    private final StringProperty operatorId;
    private final StringProperty name;

    /**
     * Constructs a new Operator object.
     *
     * @param operatorId The unique identifier of the operator.
     * @param name The name of the operator.
     */
    public Operator(String operatorId, String name) {
        this.operatorId = new SimpleStringProperty(operatorId);
        this.name = new SimpleStringProperty(name);
    }

    // Getters for TableView (required for JavaFX Data Binding)
    /**
     * Returns the observable property for the operator ID.
     * @return The StringProperty for operatorId.
     */
    public StringProperty operatorIdProperty() { return operatorId; }

    /**
     * Returns the observable property for the operator name.
     * @return The StringProperty for name.
     */
    public StringProperty nameProperty() { return name; }

    // Standard Getters/Setters for data manipulation
    /**
     * Returns the raw value of the operator ID.
     * @return The operator ID string.
     */
    public String getOperatorId() { return operatorId.get(); }

    /**
     * Returns the raw value of the operator name.
     * @return The operator name string.
     */
    public String getName() { return name.get(); }

    /**
     * Sets a new value for the operator name.
     * @param newName The new name for the operator.
     */
    public void setName(String newName) { this.name.set(newName); }

    /**
     * Returns a string representation of the Operator.
     * @return A formatted string containing the ID and Name.
     */
    @Override
    public String toString() {
        return String.format("%s - %s", getOperatorId(), getName());
    }
}
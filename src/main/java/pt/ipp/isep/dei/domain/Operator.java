package pt.ipp.isep.dei.domain;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

/**
 * Modelo para a tabela OPERATOR, compatível com JavaFX TableView.
 */
public class Operator {

    private final StringProperty operatorId;
    private final StringProperty name;

    public Operator(String operatorId, String name) {
        this.operatorId = new SimpleStringProperty(operatorId);
        this.name = new SimpleStringProperty(name);
    }

    // Getters para TableView
    public StringProperty operatorIdProperty() { return operatorId; }
    public StringProperty nameProperty() { return name; }

    // Getters/Setters standard para manipulação de dados
    public String getOperatorId() { return operatorId.get(); }
    public String getName() { return name.get(); }
    public void setName(String newName) { this.name.set(newName); }

    @Override
    public String toString() {
        return String.format("%s - %s", getOperatorId(), getName());
    }
}
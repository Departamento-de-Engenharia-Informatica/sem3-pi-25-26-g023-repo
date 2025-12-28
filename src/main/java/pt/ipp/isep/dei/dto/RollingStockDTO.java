package pt.ipp.isep.dei.dto;

import javafx.beans.property.*;

public class RollingStockDTO {
    private final StringProperty id;
    private final StringProperty type; // "Locomotive" ou "Wagon"
    private final StringProperty model;
    private final StringProperty status; // "PARKED" ou "IN_TRANSIT"
    private final StringProperty currentLocation; // Nome da estação atual ou Destino
    private final DoubleProperty distanceToStart; // Distância calculada

    // Objeto real (para guardares depois)
    private final Object sourceObject;

    public RollingStockDTO(String id, String type, String model, String status,
                           String currentLocation, double distance, Object sourceObject) {
        this.id = new SimpleStringProperty(id);
        this.type = new SimpleStringProperty(type);
        this.model = new SimpleStringProperty(model);
        this.status = new SimpleStringProperty(status);
        this.currentLocation = new SimpleStringProperty(currentLocation);
        this.distanceToStart = new SimpleDoubleProperty(distance);
        this.sourceObject = sourceObject;
    }

    // Getters para JavaFX
    public String getId() { return id.get(); }
    public StringProperty idProperty() { return id; }

    public String getType() { return type.get(); }
    public StringProperty typeProperty() { return type; }

    public String getModel() { return model.get(); }
    public StringProperty modelProperty() { return model; }

    public String getStatus() { return status.get(); }
    public StringProperty statusProperty() { return status; }

    public String getCurrentLocation() { return currentLocation.get(); }
    public StringProperty currentLocationProperty() { return currentLocation; }

    public double getDistanceToStart() { return distanceToStart.get(); }
    public String getFormattedDistance() { return String.format("%.2f km", distanceToStart.get()); }

    public Object getSourceObject() { return sourceObject; }
}
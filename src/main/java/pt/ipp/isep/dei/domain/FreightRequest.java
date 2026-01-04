package pt.ipp.isep.dei.domain;

public class FreightRequest {
    private final String id;
    private final int originStationId;
    private final int destinationStationId;
    private final String description;
    private final double weightTons;

    // Estado interno para o algoritmo de planeamento
    private boolean isPickedUp = false;
    private boolean isDelivered = false;

    public FreightRequest(String id, int originStationId, int destinationStationId, String description, double weightTons) {
        this.id = id;
        this.originStationId = originStationId;
        this.destinationStationId = destinationStationId;
        this.description = description;
        this.weightTons = weightTons;
    }

    // --- GETTERS (O erro "Cannot resolve method getId" resolve-se aqui) ---
    public String getId() {
        return id;
    }

    public int getOriginStationId() {
        return originStationId;
    }

    public int getDestinationStationId() {
        return destinationStationId;
    }

    public String getDescription() {
        return description;
    }

    public double getWeightTons() {
        return weightTons;
    }

    // --- Getters e Setters de Estado ---
    public boolean isPickedUp() {
        return isPickedUp;
    }

    public void setPickedUp(boolean pickedUp) {
        isPickedUp = pickedUp;
    }

    public boolean isDelivered() {
        return isDelivered;
    }

    public void setDelivered(boolean delivered) {
        isDelivered = delivered;
    }

    @Override
    public String toString() {
        return String.format("[%s] %s (From: %d -> To: %d)", id, description, originStationId, destinationStationId);
    }
}
package pt.ipp.isep.dei.domain;

/**
 * Represents a railway station with geographic coordinates
 * and projected coordinates for visualization.
 */
public record Station(int idEstacao, String nome, double latitude, double longitude, double coordX, double coordY) {
    /**
     * Constructs a Station object.
     *
     * @param idEstacao Unique identifier of the station
     * @param nome      Name of the station
     * @param latitude  Geographic latitude
     * @param longitude Geographic longitude
     * @param coordX    Projected X coordinate for visualization
     * @param coordY    Projected Y coordinate for visualization
     */
    public Station {
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Station s)) return false;
        return idEstacao == s.idEstacao;
    }

    @Override
    public int hashCode() {
        return idEstacao;
    }

    @Override
    public String toString() {
        return String.format("%s (ID: %d)", nome, idEstacao);
    }
}
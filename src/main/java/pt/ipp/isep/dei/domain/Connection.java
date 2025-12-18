package pt.ipp.isep.dei.domain;


/**
 * Represents a connection between two railway stations
 * with a distance measurement.
 */
public record Connection(Station from, Station to, double distance) {
    /**
     * Constructs a Connection between two stations.
     *
     * @param from     Starting station
     * @param to       Ending station
     * @param distance Distance in kilometers
     */
    public Connection {
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Connection c)) return false;
        return (from.equals(c.from) && to.equals(c.to)) ||
                (from.equals(c.to) && to.equals(c.from));
    }

    @Override
    public int hashCode() {
        return from.hashCode() + to.hashCode();
    }

    @Override
    public String toString() {
        return String.format("%s ↔ %s (%.2f km)", from.nome(), to.nome(), distance);
    }
}
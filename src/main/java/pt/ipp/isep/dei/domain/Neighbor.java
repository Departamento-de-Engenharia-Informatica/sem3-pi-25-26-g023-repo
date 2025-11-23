package pt.ipp.isep.dei.domain;

import pt.ipp.isep.dei.domain.EuropeanStation;

/**
 * Helper class to hold a station and its calculated distance to the target point.
 *
 * <p>This object is primarily used within the Max-Heap (PriorityQueue) during the
 * k-Nearest Neighbor search algorithm on the KD-Tree.</p>
 */
public class Neighbor {
    // The station object itself.
    private final EuropeanStation station;
    // The calculated Haversine distance from the station to the query point.
    private final double distance;

    /**
     * Constructs a new Neighbor object.
     *
     * @param station The EuropeanStation object.
     * @param distance The calculated distance to the target point.
     */
    public Neighbor(EuropeanStation station, double distance) {
        this.station = station;
        this.distance = distance;
    }

    /**
     * Returns the EuropeanStation object.
     * @return The station.
     */
    public EuropeanStation getStation() { return station; }

    /**
     * Returns the calculated distance to the target point.
     * @return The distance.
     */
    public double getDistance() { return distance; }
}
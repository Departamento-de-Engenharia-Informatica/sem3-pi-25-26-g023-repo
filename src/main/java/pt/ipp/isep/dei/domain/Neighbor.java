package pt.ipp.isep.dei.domain;

import pt.ipp.isep.dei.domain.EuropeanStation;

/**
 * Helper class to hold a station and its calculated distance to the target point.
 */
public class Neighbor {
    private final EuropeanStation station;
    private final double distance;

    public Neighbor(EuropeanStation station, double distance) {
        this.station = station;
        this.distance = distance;
    }

    public EuropeanStation getStation() { return station; }
    public double getDistance() { return distance; }
}

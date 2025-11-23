package pt.ipp.isep.dei.domain;

import java.util.Objects;
import java.util.Comparator;

/**
 * Represents a station from the European dataset, containing the ID, geographical coordinates,
 * and all filter criteria.
 */
public class EuropeanStation implements Comparable<EuropeanStation> {

    // Identification and Graph
    private final int idEstacao; // Station ID
    private final String station; // Station name

    // Filters and Coordinates
    private final String country;
    private final String timeZoneGroup;
    private final double latitude;
    private final double longitude;
    private final boolean isCity;
    private final boolean isMainStation;
    private final boolean isAirport;

    public EuropeanStation(int idEstacao, String station, String country, String timeZoneGroup, double latitude, double longitude,
                           boolean isCity, boolean isMainStation, boolean isAirport) {

        if (station == null || country == null || timeZoneGroup == null) {
            throw new IllegalArgumentException("Station, Country, and TimeZoneGroup cannot be null.");
        }
        if (latitude < -90 || latitude > 90 || longitude < -180 || longitude > 180) {
            throw new IllegalArgumentException("Invalid coordinates.");
        }

        this.idEstacao = idEstacao;
        this.station = station;
        this.country = country;
        this.timeZoneGroup = timeZoneGroup;
        this.latitude = latitude;
        this.longitude = longitude;
        this.isCity = isCity;
        this.isMainStation = isMainStation;
        this.isAirport = isAirport;
    }

    // --- General Purpose Getters ---
    public int getIdEstacao() { return idEstacao; }
    public String getStation() { return station; }
    public String getCountry() { return country; }
    public String getTimeZoneGroup() { return timeZoneGroup; }
    public double getLatitude() { return latitude; }
    public double getLongitude() { return longitude; }
    public boolean isCity() { return isCity; }
    public boolean isMainStation() { return isMainStation; }
    public boolean isAirport() { return isAirport; }

    @Override
    public int compareTo(EuropeanStation other) {
        // Primary ordering by station name
        return this.station.compareTo(other.station);
    }

    /**
     * Defines equality based on the ID and also on key properties for robustness,
     * as required by the KD-Tree (grouping by coordinates).
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        EuropeanStation that = (EuropeanStation) o;

        // Compares ID, coordinates and name/country for robust equality
        return idEstacao == that.idEstacao &&
                Double.compare(that.latitude, latitude) == 0 &&
                Double.compare(that.longitude, longitude) == 0 &&
                station.equals(that.station) &&
                country.equals(that.country);
    }

    /**
     * Calculates the hash code based on all relevant fields for equality.
     */
    @Override
    public int hashCode() {
        return Objects.hash(idEstacao, station, country, latitude, longitude);
    }
}
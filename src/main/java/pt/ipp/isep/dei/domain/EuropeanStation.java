package pt.ipp.isep.dei.domain;

import java.util.Objects;

/**
 * Representa uma estação do dataset europeu (USEI06).
 * Esta classe é comparável para lidar com a ordenação por nome,
 * como exigido pelos critérios de aceitação.
 */
public class EuropeanStation implements Comparable<EuropeanStation> {

    private final String station;
    private final String country;
    private final String timeZoneGroup;
    private final double latitude;
    private final double longitude;
    private final boolean isCity;
    private final boolean isMainStation;
    private final boolean isAirport;

    public EuropeanStation(String station, String country, String timeZoneGroup, double latitude, double longitude, boolean isCity, boolean isMainStation, boolean isAirport) {
        // Validação (embora a principal seja feita no loader)
        if (station == null || station.isEmpty() || country == null || country.isEmpty() || timeZoneGroup == null || timeZoneGroup.isEmpty()) {
            throw new IllegalArgumentException("Station, Country, and TimeZoneGroup cannot be null or empty.");
        }
        if (latitude < -90 || latitude > 90 || longitude < -180 || longitude > 180) {
            throw new IllegalArgumentException("Invalid coordinates.");
        }

        this.station = station;
        this.country = country;
        this.timeZoneGroup = timeZoneGroup;
        this.latitude = latitude;
        this.longitude = longitude;
        this.isCity = isCity;
        this.isMainStation = isMainStation;
        this.isAirport = isAirport;
    }

    // --- Getters ---
    public String getStation() { return station; }
    public String getCountry() { return country; }
    public String getTimeZoneGroup() { return timeZoneGroup; }
    public double getLatitude() { return latitude; }
    public double getLongitude() { return longitude; }
    public boolean isCity() { return isCity; }
    public boolean isMainStation() { return isMainStation; }
    public boolean isAirport() { return isAirport; }

    @Override
    public String toString() {
        return String.format("Station[Name=%s, Country=%s, TZG=%s, Lat=%.4f, Lon=%.4f]",
                station, country, timeZoneGroup, latitude, longitude);
    }

    /**
     * Compara estações.
     * A ordenação primária é por nome da estação (ascendente).
     * Isto é crucial para os critérios de aceitação da USEI06/07
     * que exigem que estações no mesmo local sejam ordenadas por nome.
     */
    @Override
    public int compareTo(EuropeanStation other) {
        return this.station.compareTo(other.station);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        EuropeanStation that = (EuropeanStation) o;
        return Double.compare(that.latitude, latitude) == 0 &&
                Double.compare(that.longitude, longitude) == 0 &&
                station.equals(that.station) &&
                country.equals(that.country);
    }

    @Override
    public int hashCode() {
        return Objects.hash(station, country, latitude, longitude);
    }
}
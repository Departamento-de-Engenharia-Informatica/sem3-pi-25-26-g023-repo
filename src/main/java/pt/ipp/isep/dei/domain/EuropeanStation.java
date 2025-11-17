package pt.ipp.isep.dei.domain;

import java.util.Objects;
import java.util.Comparator;

/**
 * Representa uma estação do dataset europeu, contendo o ID, coordenadas geográficas
 * e todos os critérios de filtro.
 */
public class EuropeanStation implements Comparable<EuropeanStation> {

    // Identificação e Grafo
    private final int idEstacao;
    private final String station;

    // Filtros e Coordenadas
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

    // --- Getters de Uso Geral ---
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
        // Ordenação primária por nome da estação
        return this.station.compareTo(other.station);
    }

    /**
     * Define a igualdade baseada no ID e também em propriedades chave para robustez,
     * como exigido pelo KD-Tree (agrupamento por coordenadas).
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        EuropeanStation that = (EuropeanStation) o;

        // Compara ID, coordenadas e nome/país para uma igualdade robusta
        return idEstacao == that.idEstacao &&
                Double.compare(that.latitude, latitude) == 0 &&
                Double.compare(that.longitude, longitude) == 0 &&
                station.equals(that.station) &&
                country.equals(that.country);
    }

    /**
     * Calcula o hash code com base em todos os campos relevantes para a igualdade.
     */
    @Override
    public int hashCode() {
        return Objects.hash(idEstacao, station, country, latitude, longitude);
    }
}

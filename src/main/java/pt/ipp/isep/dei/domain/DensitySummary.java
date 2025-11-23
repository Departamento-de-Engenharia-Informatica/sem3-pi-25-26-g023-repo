package pt.ipp.isep.dei.domain;

import java.util.Map;
import java.util.TreeMap;

/**
 * An immutable class that holds the density summary of stations found within a specified radius.
 * Provides statistics aggregated by country, city type, and main station status, as required by USEI10.
 */
public class DensitySummary {

    private final int totalStations;
    private final Map<String, Integer> stationsByCountry;
    private final Map<Boolean, Integer> stationsByCityType;
    private final Map<Boolean, Integer> stationsByMainStation;

    /**
     * Constructs a new DensitySummary object.
     *
     * @param totalStations The total number of stations found in the search radius.
     * @param stationsByCountry A map counting stations per country (Key: Country Code, Value: Count).
     * @param stationsByCityType A map counting stations by city status (Key: Boolean isCity, Value: Count).
     * @param stationsByMainStation A map counting stations by main station status (Key: Boolean isMainStation, Value: Count).
     */
    public DensitySummary(int totalStations, Map<String, Integer> stationsByCountry,
                          Map<Boolean, Integer> stationsByCityType,
                          Map<Boolean, Integer> stationsByMainStation) {
        this.totalStations = totalStations;
        // Use TreeMap to ensure countries are sorted alphabetically in output
        this.stationsByCountry = new TreeMap<>(stationsByCountry);
        this.stationsByCityType = stationsByCityType;
        this.stationsByMainStation = stationsByMainStation;
    }

    /**
     * Gets the total number of stations found in the radius.
     * @return The total count.
     */
    public int getTotalStations() { return totalStations; }

    /**
     * Gets the map counting stations aggregated by country.
     * @return An alphabetically sorted map of country codes to station counts.
     */
    public Map<String, Integer> getStationsByCountry() { return stationsByCountry; }

    /**
     * Gets the map counting stations aggregated by city status (true/false).
     * @return Map where true is City and false is Non-City.
     */
    public Map<Boolean, Integer> getStationsByCityType() { return stationsByCityType; }

    /**
     * Gets the map counting stations aggregated by main station status (true/false).
     * @return Map where true is Main Station and false is Regular Station.
     */
    public Map<Boolean, Integer> getStationsByMainStation() { return stationsByMainStation; }

    /**
     * Returns a formatted string representation of the density summary, typically used for console or UI display.
     *
     * @return A detailed, formatted string summary.
     */
    public String getFormattedSummary() {
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("=== DENSITY SUMMARY (USEI10) ===%n"));
        sb.append(String.format("Total stations in radius: %d%n%n", totalStations));

        sb.append("Distribution by country:\n");
        for (Map.Entry<String, Integer> entry : stationsByCountry.entrySet()) {
            double percentage = totalStations > 0 ? (double) entry.getValue() / totalStations * 100 : 0;
            sb.append(String.format("  %s: %d stations (%.1f%%)%n",
                    entry.getKey(), entry.getValue(), percentage));
        }

        sb.append("\nDistribution by city type:\n");
        int cityStations = stationsByCityType.getOrDefault(true, 0);
        int nonCityStations = stationsByCityType.getOrDefault(false, 0);
        sb.append(String.format("  City stations: %d (%.1f%%)%n",
                cityStations, totalStations > 0 ? (double) cityStations / totalStations * 100 : 0));
        sb.append(String.format("  Non-city stations: %d (%.1f%%)%n",
                nonCityStations, totalStations > 0 ? (double) nonCityStations / totalStations * 100 : 0));

        sb.append("\nDistribution by main station type:\n");
        int mainStations = stationsByMainStation.getOrDefault(true, 0);
        int regularStations = stationsByMainStation.getOrDefault(false, 0);
        sb.append(String.format("  Main stations: %d (%.1f%%)%n",
                mainStations, totalStations > 0 ? (double) mainStations / totalStations * 100 : 0));
        sb.append(String.format("  Regular stations: %d (%.1f%%)%n",
                regularStations, totalStations > 0 ? (double) regularStations / totalStations * 100 : 0));

        return sb.toString();
    }

    /**
     * Returns the formatted summary string, adhering to the Java convention.
     * @return The formatted summary string.
     */
    @Override
    public String toString() {
        return getFormattedSummary();
    }
}
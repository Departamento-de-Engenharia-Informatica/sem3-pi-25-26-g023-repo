package pt.ipp.isep.dei.domain;

import java.util.Map;
import java.util.TreeMap;

/**
 * Classe que contém o sumário de densidade das estações dentro de um raio.
 * Fornece estatísticas por país e tipo de cidade conforme USEI10.
 */
public class DensitySummary {
    private final int totalStations;
    private final Map<String, Integer> stationsByCountry;
    private final Map<Boolean, Integer> stationsByCityType;
    private final Map<Boolean, Integer> stationsByMainStation;

    public DensitySummary(int totalStations, Map<String, Integer> stationsByCountry,
                          Map<Boolean, Integer> stationsByCityType,
                          Map<Boolean, Integer> stationsByMainStation) {
        this.totalStations = totalStations;
        this.stationsByCountry = new TreeMap<>(stationsByCountry); // Ordenado por país
        this.stationsByCityType = stationsByCityType;
        this.stationsByMainStation = stationsByMainStation;
    }

    // Getters
    public int getTotalStations() { return totalStations; }
    public Map<String, Integer> getStationsByCountry() { return stationsByCountry; }
    public Map<Boolean, Integer> getStationsByCityType() { return stationsByCityType; }
    public Map<Boolean, Integer> getStationsByMainStation() { return stationsByMainStation; }

    /**
     * Retorna uma representação formatada do sumário conforme USEI10.
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

    @Override
    public String toString() {
        return getFormattedSummary();
    }
}

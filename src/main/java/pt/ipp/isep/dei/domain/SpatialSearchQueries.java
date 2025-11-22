package pt.ipp.isep.dei.domain;

import java.util.ArrayList;
import java.util.List;

/**
 * USEI08 - Spatial Search Queries
 * Provides 5 predefined spatial queries for demonstration as required.
 * This class works with SpatialSearch to showcase practical applications
 * of KD-Tree spatial searches for European railway stations.
 *
 * @version 1.0
 */
public class SpatialSearchQueries {

    private final SpatialSearch spatialSearch;

    public SpatialSearchQueries(SpatialSearch spatialSearch) {
        this.spatialSearch = spatialSearch;
    }

    /**
     * Represents the result of a spatial query with performance metrics
     */
    public static class QueryResult {
        public final String description;
        public final List<EuropeanStation> stations;
        public final long executionTimeNs;
        public final int stationsFound;

        public QueryResult(String description, List<EuropeanStation> stations, long executionTimeNs) {
            this.description = description;
            this.stations = stations;
            this.executionTimeNs = executionTimeNs;
            this.stationsFound = stations.size();
        }

        public double getExecutionTimeMs() {
            return executionTimeNs / 1_000_000.0;
        }

        @Override
        public String toString() {
            return String.format("%s: %d stations (%.2f ms)",
                    description, stationsFound, getExecutionTimeMs());
        }
    }

    // ============================================================
    // === 5 PREDEFINED QUERIES (AS REQUIRED) ===
    // ============================================================

    /**
     * QUERY 1: Todas as estações em Portugal
     * Demonstra filtro por país em área geográfica específica
     */
    public QueryResult queryAllStationsInPortugal() {
        String description = "All stations in Portugal";
        long startTime = System.nanoTime();

        List<EuropeanStation> results = spatialSearch.searchByGeographicalArea(
                36.0, 42.0,    // Latitude range covering Portugal
                -9.5, -6.0,    // Longitude range covering Portugal
                "PT",          // Country filter: Portugal
                null,          // No city filter
                null           // No main station filter
        );

        long endTime = System.nanoTime();
        return new QueryResult(description, results, endTime - startTime);
    }

    /**
     * QUERY 2: Estações principais na área de Lisboa
     * Demonstra combinação de área geográfica e filtro por tipo de estação
     */
    public QueryResult queryMainStationsInLisbon() {
        String description = "Main stations in Lisbon area";
        long startTime = System.nanoTime();

        List<EuropeanStation> results = spatialSearch.searchByGeographicalArea(
                38.70, 38.75,  // Lisbon latitude range
                -9.15, -9.10,  // Lisbon longitude range
                "PT",          // Portugal
                null,          // No city filter
                true           // Only main stations
        );

        long endTime = System.nanoTime();
        return new QueryResult(description, results, endTime - startTime);
    }

    /**
     * QUERY 3: Estações em cidades de França
     * Demonstra filtro por país e estatuto de cidade
     */
    public QueryResult queryCityStationsInFrance() {
        String description = "City stations in France";
        long startTime = System.nanoTime();

        List<EuropeanStation> results = spatialSearch.searchByGeographicalArea(
                42.0, 51.0,    // France latitude range
                -5.0, 8.0,     // France longitude range
                "FR",          // France
                true,          // Only city stations
                null           // No main station filter
        );

        long endTime = System.nanoTime();
        return new QueryResult(description, results, endTime - startTime);
    }

    /**
     * QUERY 4: Estações não-principais em Itália
     * Demonstra filtro por tipo de estação (não-principal)
     */
    public QueryResult queryNonMainStationsInItaly() {
        String description = "Non-main stations in Italy";
        long startTime = System.nanoTime();

        List<EuropeanStation> results = spatialSearch.searchByGeographicalArea(
                35.0, 47.0,    // Italy latitude range
                6.0, 18.0,     // Italy longitude range
                "IT",          // Italy
                null,          // No city filter
                false          // Only non-main stations
        );

        long endTime = System.nanoTime();
        return new QueryResult(description, results, endTime - startTime);
    }

    /**
     * QUERY 5: Todas as estações na área metropolitana de Madrid
     * Demonstra busca em área geográfica precisa
     */
    public QueryResult queryStationsInMadrid() {
        String description = "All stations in Madrid area";
        long startTime = System.nanoTime();

        List<EuropeanStation> results = spatialSearch.searchByGeographicalArea(
                40.30, 40.50,  // Madrid latitude range
                -3.80, -3.60,  // Madrid longitude range
                "ES",          // Spain
                null,          // No city filter
                null           // No main station filter
        );

        long endTime = System.nanoTime();
        return new QueryResult(description, results, endTime - startTime);
    }

    // ============================================================
    // === BATCH QUERY EXECUTION ===
    // ============================================================

    /**
     * Executes all 5 predefined queries and returns results with performance metrics
     */
    public List<QueryResult> executeAllDemoQueries() {
        List<QueryResult> allResults = new ArrayList<>();

        allResults.add(queryAllStationsInPortugal());
        allResults.add(queryMainStationsInLisbon());
        allResults.add(queryCityStationsInFrance());
        allResults.add(queryNonMainStationsInItaly());
        allResults.add(queryStationsInMadrid());

        return allResults;
    }

    /**
     * Executes a custom query with the specified parameters
     */
    public QueryResult executeCustomQuery(String description,
                                          double latMin, double latMax,
                                          double lonMin, double lonMax,
                                          String country,
                                          Boolean isCity,
                                          Boolean isMain) {
        long startTime = System.nanoTime();

        List<EuropeanStation> results = spatialSearch.searchByGeographicalArea(
                latMin, latMax, lonMin, lonMax, country, isCity, isMain
        );

        long endTime = System.nanoTime();
        return new QueryResult(description, results, endTime - startTime);
    }

    // ============================================================
    // === ANALYSIS AND STATISTICS ===
    // ============================================================

    /**
     * Generates a comprehensive performance report for the 5 queries
     */
    public String generatePerformanceReport() {
        List<QueryResult> results = executeAllDemoQueries();

        StringBuilder report = new StringBuilder();
        report.append("=== USEI08 SPATIAL SEARCH - 5 DEMO QUERIES ===\n\n");

        int totalStations = 0;
        double totalTime = 0;

        for (QueryResult result : results) {
            report.append(String.format("• %s\n", result.toString()));
            totalStations += result.stationsFound;
            totalTime += result.getExecutionTimeMs();
        }

        double avgTime = totalTime / results.size();
        double avgStations = (double) totalStations / results.size();

        report.append("\n=== SUMMARY ===\n");
        report.append(String.format("Queries executed: %d\n", results.size()));
        report.append(String.format("Total stations found: %d\n", totalStations));
        report.append(String.format("Average time per query: %.2f ms\n", avgTime));
        report.append(String.format("Average stations per query: %.1f\n", avgStations));

        report.append("\n=== KD-TREE EFFICIENCY ===\n");
        report.append("Complexity: O(√n) average case\n");
        report.append("Performance: Suitable for large datasets (64k stations)\n");

        return report.toString();
    }

    /**
     * Gets sample stations from each query for demonstration
     */
    public String getQuerySamples() {
        List<QueryResult> results = executeAllDemoQueries();

        StringBuilder samples = new StringBuilder();
        samples.append("=== SAMPLE STATIONS FROM EACH QUERY ===\n\n");

        for (QueryResult result : results) {
            samples.append(String.format("%s:\n", result.description));

            if (result.stations.isEmpty()) {
                samples.append("  No stations found\n");
            } else {
                // Show up to 3 sample stations
                result.stations.stream()
                        .limit(3)
                        .forEach(station ->
                                samples.append(String.format("  • %s (%s) - Lat: %.4f, Lon: %.4f\n",
                                        station.getStation(), station.getCountry(),
                                        station.getLatitude(), station.getLongitude()))
                        );

                if (result.stations.size() > 3) {
                    samples.append(String.format("  ... and %d more\n", result.stations.size() - 3));
                }
            }
            samples.append("\n");
        }

        return samples.toString();
    }
}
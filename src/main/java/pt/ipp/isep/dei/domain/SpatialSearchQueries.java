package pt.ipp.isep.dei.domain;

import java.util.ArrayList;
import java.util.List;

/**
 * USEI08 - Spatial Search Queries
 * Provides 5 predefined spatial queries for demonstration as required by USEI08 acceptance criteria.
 * This class works with SpatialSearch to showcase practical applications of KD-Tree spatial searches
 * for European railway stations.
 */
public class SpatialSearchQueries {

    private final SpatialSearch spatialSearch;

    /**
     * Constructs a SpatialSearchQueries instance with the specified SpatialSearch engine.
     *
     * @param spatialSearch the spatial search engine to use for queries
     */
    public SpatialSearchQueries(SpatialSearch spatialSearch) {
        this.spatialSearch = spatialSearch;
    }

    /**
     * Represents the result of a spatial query with performance metrics.
     */
    public static class QueryResult {
        public final String description;
        public final List<EuropeanStation> stations;
        public final long executionTimeNs;
        public final int stationsFound;

        /**
         * Constructs a QueryResult with execution metrics.
         *
         * @param description query description
         * @param stations list of stations found
         * @param executionTimeNs execution time in nanoseconds
         */
        public QueryResult(String description, List<EuropeanStation> stations, long executionTimeNs) {
            this.description = description;
            this.stations = stations;
            this.executionTimeNs = executionTimeNs;
            this.stationsFound = stations.size();
        }

        /**
         * Returns execution time in milliseconds.
         *
         * @return execution time in ms
         */
        public double getExecutionTimeMs() {
            return executionTimeNs / 1_000_000.0;
        }

        @Override
        public String toString() {
            return String.format("%s: %d stations (%.2f ms)",
                    description, stationsFound, getExecutionTimeMs());
        }
    }

    /**
     * Query 1: All stations in Portugal.
     * Demonstrates country filter within specific geographical area.
     *
     * @return query result with stations in Portugal
     */
    public QueryResult queryAllStationsInPortugal() {
        String description = "All stations in Portugal";
        long startTime = System.nanoTime();

        List<EuropeanStation> results = spatialSearch.searchByGeographicalArea(
                36.0, 42.0,
                -9.5, -6.0,
                "PT",
                null,
                null
        );

        long endTime = System.nanoTime();
        return new QueryResult(description, results, endTime - startTime);
    }

    /**
     * Query 2: Main stations in Lisbon area.
     * Demonstrates combination of geographical area and station type filter.
     *
     * @return query result with main stations in Lisbon area
     */
    public QueryResult queryMainStationsInLisbon() {
        String description = "Main stations in Lisbon area";
        long startTime = System.nanoTime();

        List<EuropeanStation> results = spatialSearch.searchByGeographicalArea(
                38.70, 38.75,
                -9.15, -9.10,
                "PT",
                null,
                true
        );

        long endTime = System.nanoTime();
        return new QueryResult(description, results, endTime - startTime);
    }

    /**
     * Query 3: City stations in France.
     * Demonstrates country and city status filters.
     *
     * @return query result with city stations in France
     */
    public QueryResult queryCityStationsInFrance() {
        String description = "City stations in France";
        long startTime = System.nanoTime();

        List<EuropeanStation> results = spatialSearch.searchByGeographicalArea(
                42.0, 51.0,
                -5.0, 8.0,
                "FR",
                true,
                null
        );

        long endTime = System.nanoTime();
        return new QueryResult(description, results, endTime - startTime);
    }

    /**
     * Query 4: Non-main stations in Italy.
     * Demonstrates filter for non-main stations.
     *
     * @return query result with non-main stations in Italy
     */
    public QueryResult queryNonMainStationsInItaly() {
        String description = "Non-main stations in Italy";
        long startTime = System.nanoTime();

        List<EuropeanStation> results = spatialSearch.searchByGeographicalArea(
                35.0, 47.0,
                6.0, 18.0,
                "IT",
                null,
                false
        );

        long endTime = System.nanoTime();
        return new QueryResult(description, results, endTime - startTime);
    }

    /**
     * Query 5: All stations in Madrid metropolitan area.
     * Demonstrates precise geographical area search.
     *
     * @return query result with stations in Madrid area
     */
    public QueryResult queryStationsInMadrid() {
        String description = "All stations in Madrid area";
        long startTime = System.nanoTime();

        List<EuropeanStation> results = spatialSearch.searchByGeographicalArea(
                40.30, 40.50,
                -3.80, -3.60,
                "ES",
                null,
                null
        );

        long endTime = System.nanoTime();
        return new QueryResult(description, results, endTime - startTime);
    }

    /**
     * Executes all 5 predefined queries and returns results with performance metrics.
     *
     * @return list of query results for all demo queries
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
     * Executes a custom query with the specified parameters.
     *
     * @param description query description
     * @param latMin minimum latitude
     * @param latMax maximum latitude
     * @param lonMin minimum longitude
     * @param lonMax maximum longitude
     * @param country country filter
     * @param isCity city station filter
     * @param isMain main station filter
     * @return query result with execution metrics
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

    /**
     * Generates a comprehensive performance report for the 5 queries.
     *
     * @return formatted performance report string
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
     * Gets sample stations from each query for demonstration.
     *
     * @return formatted string with sample stations
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
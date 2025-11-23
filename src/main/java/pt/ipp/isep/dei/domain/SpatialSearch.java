package pt.ipp.isep.dei.domain;

import java.util.ArrayList;
import java.util.List;

/**
 * USEI08 - Search by Geographical Area
 * Implements range search in KD-Tree for European railway stations with optional filters.
 * Provides efficient spatial queries using KD-Tree pruning to avoid full dataset scans.
 */
public record SpatialSearch(KDTree kdTree) {

    /**
     * Constructs a SpatialSearch instance with the specified KD-Tree.
     *
     * @param kdTree the KD-Tree containing European railway stations
     * @throws IllegalArgumentException if kdTree is null
     */
    public SpatialSearch {
        if (kdTree == null) {
            throw new IllegalArgumentException("KD-Tree cannot be null");
        }
    }

    /**
     * Searches for stations within specified geographical boundaries with optional filters.
     * Uses KD-Tree properties to prune search space efficiently.
     *
     * @param latMin minimum latitude boundary (-90 to 90)
     * @param latMax maximum latitude boundary (-90 to 90)
     * @param lonMin minimum longitude boundary (-180 to 180)
     * @param lonMax maximum longitude boundary (-180 to 180)
     * @param countryFilter country code filter (e.g., "PT", "ES") or null for any
     * @param isCityFilter filter for city stations (true/false) or null for any
     * @param isMainStationFilter filter for main stations (true/false) or null for any
     * @return list of EuropeanStation objects matching the criteria
     * @throws IllegalArgumentException if coordinate boundaries are invalid
     */
    public List<EuropeanStation> searchByGeographicalArea(double latMin, double latMax, double lonMin, double lonMax,
                                                          String countryFilter, Boolean isCityFilter, Boolean isMainStationFilter) {

        validateCoordinates(latMin, latMax, lonMin, lonMax);

        List<EuropeanStation> results = new ArrayList<>();
        searchInRangeRecursive(kdTree.getRoot(), latMin, latMax, lonMin, lonMax,
                countryFilter, isCityFilter, isMainStationFilter, 0, results);
        return results;
    }

    /**
     * Validates coordinate boundaries according to geographical limits.
     *
     * @param latMin minimum latitude
     * @param latMax maximum latitude
     * @param lonMin minimum longitude
     * @param lonMax maximum longitude
     * @throws IllegalArgumentException if coordinates are outside valid ranges
     */
    private void validateCoordinates(double latMin, double latMax, double lonMin, double lonMax) {
        if (latMin < -90.0 || latMax > 90.0) {
            throw new IllegalArgumentException("Invalid latitude range: [" + latMin + "," + latMax + "]");
        }
        if (lonMin < -180.0 || lonMax > 180.0) {
            throw new IllegalArgumentException("Invalid longitude range: [" + lonMin + ", " + lonMax + "]");
        }
    }

    /**
     * Recursively searches KD-Tree nodes within the specified geographical range.
     * Implements KD-Tree pruning to optimize search performance.
     *
     * @param node current KD-Tree node being processed
     * @param latMin minimum latitude boundary
     * @param latMax maximum latitude boundary
     * @param lonMin minimum longitude boundary
     * @param lonMax maximum longitude boundary
     * @param countryFilter country code filter
     * @param isCityFilter city station filter
     * @param isMainStationFilter main station filter
     * @param depth current depth in KD-Tree
     * @param results list to accumulate matching stations
     */
    private void searchInRangeRecursive(KDTree.Node node, double latMin, double latMax, double lonMin, double lonMax,
                                        String countryFilter, Boolean isCityFilter, Boolean isMainStationFilter,
                                        int depth, List<EuropeanStation> results) {

        if (node == null) {
            return;
        }

        double currentLat = node.getLatitude();
        double currentLon = node.getLongitude();
        int currentDimension = depth % 2;

        boolean inLatRange = (currentLat >= latMin && currentLat <= latMax);
        boolean inLonRange = (currentLon >= lonMin && currentLon <= lonMax);

        if (inLatRange && inLonRange) {
            for (EuropeanStation station : node.getStations()) {
                if (matchesFilters(station, countryFilter, isCityFilter, isMainStationFilter)) {
                    results.add(station);
                }
            }
        }

        if (currentDimension == 0) {
            if (latMin <= currentLat) {
                searchInRangeRecursive(node.getLeft(), latMin, latMax, lonMin, lonMax,
                        countryFilter, isCityFilter, isMainStationFilter, depth + 1, results);
            }
            if (latMax >= currentLat) {
                searchInRangeRecursive(node.getRight(), latMin, latMax, lonMin, lonMax,
                        countryFilter, isCityFilter, isMainStationFilter, depth + 1, results);
            }
        } else {
            if (lonMin <= currentLon) {
                searchInRangeRecursive(node.getLeft(), latMin, latMax, lonMin, lonMax,
                        countryFilter, isCityFilter, isMainStationFilter, depth + 1, results);
            }
            if (lonMax >= currentLon) {
                searchInRangeRecursive(node.getRight(), latMin, latMax, lonMin, lonMax,
                        countryFilter, isCityFilter, isMainStationFilter, depth + 1, results);
            }
        }
    }

    /**
     * Applies optional filters to a station. Null filters are ignored.
     *
     * @param station the station to check
     * @param countryFilter country code filter
     * @param isCityFilter city station filter
     * @param isMainStationFilter main station filter
     * @return true if station matches all specified filters
     */
    private boolean matchesFilters(EuropeanStation station, String countryFilter,
                                   Boolean isCityFilter, Boolean isMainStationFilter) {

        if (countryFilter != null && !countryFilter.equals("all")) {
            if (!countryFilter.equalsIgnoreCase(station.getCountry())) {
                return false;
            }
        }

        if (isCityFilter != null && isCityFilter != station.isCity()) {
            return false;
        }

        if (isMainStationFilter != null && isMainStationFilter != station.isMainStation()) {
            return false;
        }

        return true;
    }

    /**
     * Provides complexity analysis for the spatial search operations.
     *
     * @return formatted string with performance analysis
     */
    public String getComplexityAnalysis() {
        return String.format("""
                         USEI08 Complexity Analysis:
                         KD-Tree Properties:
                         - Height: %d
                         - Nodes: %d
                         - Balance: %s
                         
                         Time Complexity:
                         - Best case: O(log n)
                         - Average case: O(√n)
                         - Worst case: O(n)
                         
                         Space Complexity:
                         - Auxiliary: O(1)
                         - Recursion stack: O(log n)
                        """,
                kdTree.height(),
                kdTree.size(),
                kdTree.height() <= 2 * Math.log(kdTree.size()) / Math.log(2) ? "Good" : "Could be improved");
    }
}
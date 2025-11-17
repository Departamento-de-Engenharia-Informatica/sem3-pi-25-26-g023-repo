package pt.ipp.isep.dei.domain;

import java.util.ArrayList;
import java.util.List;

/**
 * USEI08 - Search by Geographical Area
 * Implements range search in KD-Tree for European railway stations
 */
public class SpatialSearch {

    private final KDTree kdTree;

    public SpatialSearch(KDTree kdTree) {
        if (kdTree == null) {
            throw new IllegalArgumentException("KD-Tree cannot be null");
        }
        this.kdTree = kdTree;
    }

    /**
     * Searches for stations within specified geographical rectangle
     * Uses KD-Tree properties to prune search space efficiently
     */
    public List<EuropeanStation> searchByGeographicalArea(double latMin, double latMax, double lonMin, double lonMax, String countryFilter, Boolean isCityFilter, Boolean isMainStationFilter) {

        validateCoordinates(latMin, latMax, lonMin, lonMax);

        List<EuropeanStation> results = new ArrayList<>();
        searchInRangeRecursive(kdTree.getRoot(), latMin, latMax, lonMin, lonMax, countryFilter, isCityFilter, isMainStationFilter, 0, results);
        return results;
    }

    /**
     * Validates coordinate boundaries according to geographical limits
     */
    private void validateCoordinates(double latMin, double latMax, double lonMin, double lonMax) {
        if (latMin < -90.0 || latMax > 90.0) {
            throw new IllegalArgumentException("Invalid latitude range: [" + latMin + "," + latMax + "]");
        }
        if (lonMin < -180.0 || lonMax > 180.0) {
            throw new IllegalArgumentException("Invalid longitude range: [" + lonMin + ", " + lonMax + "]");
        }
    }

    private void searchInRangeRecursive(KDTree.Node node, double latMin, double latMax, double lonMin, double lonMax, String countryFilter, Boolean isCityFilter, Boolean isMainStationFilter, int depth, List<EuropeanStation> results) {

        // Base case: reached null node
        if (node == null) {
            return;
        }

        double currentLat = node.getLatitude();
        double currentLon = node.getLongitude();
        int currentDimension = depth % 2; // 0 = latitude, 1 = longitude

        // Check if current node is within search rectangle
        boolean inLatRange = (currentLat >= latMin && currentLat <= latMax);
        boolean inLonRange = (currentLon >= lonMin && currentLon <= lonMax);

        // If node within range, check all stations against filters
        if (inLatRange && inLonRange) {
            for (EuropeanStation station : node.getStations()) {
                if (matchesFilters(station, countryFilter, isCityFilter, isMainStationFilter)) {
                    results.add(station);
                }
            }
        }
        // Explore subtrees based on current dimension - KD-Tree pruning
        if (currentDimension == 0) {
            // Current dimension: Latitude
            if (latMin <= currentLat) {
                searchInRangeRecursive(node.getLeft(), latMin, latMax, lonMin, lonMax, countryFilter, isCityFilter, isMainStationFilter, depth + 1, results);
            }

            if (latMax >= currentLat) {
                searchInRangeRecursive(node.getRight(), latMin, latMax, lonMin, lonMax, countryFilter, isCityFilter, isMainStationFilter, depth + 1, results);
            }
        }   else {
                // Current dimension: Longitude
                if (lonMin <= currentLon) {
                    searchInRangeRecursive(node.getLeft(), latMin, latMax, lonMin, lonMax, countryFilter, isCityFilter, isMainStationFilter, depth + 1, results);
                }
            if (lonMax >= currentLon) {
                searchInRangeRecursive(node.getRight(), latMin, latMax, lonMin, lonMax, countryFilter, isCityFilter, isMainStationFilter, depth + 1, results);
            }
        }
    }

    /**
     * Applies filters to station - null filters are ignored
     */
    private boolean matchesFilters(EuropeanStation station, String countryFilter, Boolean isCityFilter, Boolean isMainStationFilter) {

        // Country filter (case-insensitive)
        if (countryFilter != null && !countryFilter.equals("all)")) {
            if (!countryFilter.equalsIgnoreCase(station.getCountry())) {
                return false;
            }
        }

        // City station filter
        if (isCityFilter != null && isCityFilter != station.isCity()) {
            return false;
        }

        // Main station filter
        if (isMainStationFilter != null && isMainStationFilter != station.isMainStation()) {
            return false;
        }

        return true;

    }

    public String getComplexityAnalysis() {
        return String.format("""
                        USEI08 Complexity Analysis:
                        KD-Tree Properties:
                        - Height: %d
                        - Nodes: %d
                        - Balance: %s
                                        
                        Time Complexity:
                        - Best case: O(log n)
                        - Average case: o(√n)
                        - Worst case: 0(n)
                                        
                        Space Complexity:
                        - Auxiliary: O(1)
                        - Recursion stack: O(log n)
                        """,
                kdTree.height(),
                kdTree.size(),
                kdTree.height() <= 2 * Math.log(kdTree.size()) / Math.log(2) ? "Good" : "Could be improved");
    }

    public KDTree getKdTree() {
        return kdTree;
    }
}
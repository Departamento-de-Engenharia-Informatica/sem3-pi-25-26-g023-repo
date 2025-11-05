package pt.ipp.isep.dei.domain;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Implements a balanced 2D-Tree (KD-Tree) for storing EuropeanStation objects.
 * This class supports efficient spatial queries including range searches and proximity searches.
 *
 * <p>This implementation ensures balanced tree construction and handles stations with identical coordinates
 * by storing them in the same node, sorted by station name.</p>
 *
 * @version 2.0
 */
public class KDTree {

    /**
     * Represents a node in the KD-Tree containing stations with identical coordinates.
     */
    private static class Node {
        private final List<EuropeanStation> stations;
        private Node left;
        private Node right;
        private final double latitude;
        private final double longitude;

        /**
         * Constructs a new KD-Tree node with the specified stations.
         * Stations are sorted by name to ensure deterministic ordering.
         *
         * @param stationsInNode list of stations with identical coordinates to be stored in this node
         * @throws IllegalArgumentException if stationsInNode is empty
         */
        public Node(List<EuropeanStation> stationsInNode) {
            if (stationsInNode.isEmpty()) {
                throw new IllegalArgumentException("Cannot create a tree node with an empty station list.");
            }

            // Ensure stations are sorted by name (using natural ordering)
            stationsInNode.sort(null);
            this.stations = stationsInNode;

            // Use coordinates from the first station (all stations have same coordinates)
            this.latitude = stationsInNode.get(0).getLatitude();
            this.longitude = stationsInNode.get(0).getLongitude();
            this.left = null;
            this.right = null;
        }

        /**
         * Returns the coordinate value for the specified dimension at the given depth.
         *
         * @param depth the current depth in the tree (used to determine splitting dimension)
         * @return the latitude if depth is even, longitude if depth is odd
         */
        public double getCoordinate(int depth) {
            return (depth % 2 == 0) ? latitude : longitude;
        }
    }

    private Node root;
    private int size;

    /**
     * Constructs an empty KD-Tree.
     */
    public KDTree() {
        this.root = null;
        this.size = 0;
    }

    /**
     * Builds a balanced KD-Tree from pre-sorted lists of stations by latitude and longitude.
     * This method uses a recursive median-based approach to ensure tree balance.
     *
     * @param stationsByLat list of stations sorted by latitude in ascending order
     * @param stationsByLon list of stations sorted by longitude in ascending order
     * @throws IllegalArgumentException if input lists are null, empty, or have different sizes
     */
    public void buildBalanced(List<EuropeanStation> stationsByLat, List<EuropeanStation> stationsByLon) {
        if (stationsByLat == null || stationsByLon == null ||
                stationsByLat.isEmpty() || stationsByLon.isEmpty() ||
                stationsByLat.size() != stationsByLon.size()) {

            throw new IllegalArgumentException("Input lists for 2D-Tree are invalid, empty, or have different sizes.");
        }

        List<EuropeanStation> stationsLat = new ArrayList<>(stationsByLat);
        List<EuropeanStation> stationsLon = new ArrayList<>(stationsByLon);
        this.root = buildBalancedRecursive(stationsLat, stationsLon, 0);
    }

    /**
     * Recursively builds a balanced KD-Tree node using median partitioning.
     *
     * @param stationsByLat stations sorted by latitude for the current partition
     * @param stationsByLon stations sorted by longitude for the current partition
     * @param depth current depth in the tree (determines splitting dimension)
     * @return the root node of the constructed subtree, or null if partition is empty
     */
    private Node buildBalancedRecursive(List<EuropeanStation> stationsByLat, List<EuropeanStation> stationsByLon, int depth) {
        if (stationsByLat.isEmpty()) {
            return null;
        }

        this.size++;
        int dim = depth % 2; // 0 for latitude, 1 for longitude

        // Select the appropriate list based on current dimension
        List<EuropeanStation> mainList = (dim == 0) ? stationsByLat : stationsByLon;
        int medianIndex = (mainList.size() - 1) / 2;
        EuropeanStation medianStation = mainList.get(medianIndex);

        double medianLat = medianStation.getLatitude();
        double medianLon = medianStation.getLongitude();

        // Collect all stations with identical coordinates to the median station
        List<EuropeanStation> nodeStations = mainList.stream()
                .filter(s -> Double.compare(s.getLatitude(), medianLat) == 0 &&
                        Double.compare(s.getLongitude(), medianLon) == 0)
                .collect(Collectors.toList());

        Node node = new Node(nodeStations);

        // Partition both lists for left and right subtrees
        List<EuropeanStation> leftLat = new ArrayList<>();
        List<EuropeanStation> rightLat = new ArrayList<>();
        List<EuropeanStation> leftLon = new ArrayList<>();
        List<EuropeanStation> rightLon = new ArrayList<>();

        // Partition latitude-sorted list
        for (EuropeanStation station : stationsByLat) {
            // Skip stations that are already included in the current node
            if (Double.compare(station.getLatitude(), medianLat) == 0 &&
                    Double.compare(station.getLongitude(), medianLon) == 0) {
                continue;
            }

            // Partition based on current dimension
            if ((dim == 0 && Double.compare(station.getLatitude(), medianLat) < 0) ||
                    (dim == 1 && Double.compare(station.getLongitude(), medianLon) < 0)) {
                leftLat.add(station);
            } else {
                rightLat.add(station);
            }
        }

        // Partition longitude-sorted list
        for (EuropeanStation station : stationsByLon) {
            // Skip stations that are already included in the current node
            if (Double.compare(station.getLatitude(), medianLat) == 0 &&
                    Double.compare(station.getLongitude(), medianLon) == 0) {
                continue;
            }

            // Partition based on current dimension
            if ((dim == 0 && Double.compare(station.getLatitude(), medianLat) < 0) ||
                    (dim == 1 && Double.compare(station.getLongitude(), medianLon) < 0)) {
                leftLon.add(station);
            } else {
                rightLon.add(station);
            }
        }

        // Recursively build left and right subtrees
        node.left = buildBalancedRecursive(leftLat, leftLon, depth + 1);
        node.right = buildBalancedRecursive(rightLat, rightLon, depth + 1);

        return node;
    }

    /**
     * Searches for all stations within a specified geographical rectangle with optional filters.
     * This method uses the KD-Tree structure for efficient range queries.
     *
     * @param latMin minimum latitude of the search area (inclusive)
     * @param latMax maximum latitude of the search area (inclusive)
     * @param lonMin minimum longitude of the search area (inclusive)
     * @param lonMax maximum longitude of the search area (inclusive)
     * @param countryFilter country code filter (e.g., "PT", "ES"), or null for all countries
     * @param isCityFilter filter for city stations (true/false), or null for all types
     * @param isMainStationFilter filter for main stations (true/false), or null for all types
     * @return list of stations matching the search criteria and filters
     */
    public List<EuropeanStation> searchInRange(
            double latMin, double latMax,
            double lonMin, double lonMax,
            String countryFilter,
            Boolean isCityFilter,
            Boolean isMainStationFilter) {

        List<EuropeanStation> results = new ArrayList<>();
        searchInRangeRecursive(root, latMin, latMax, lonMin, lonMax, countryFilter,
                isCityFilter, isMainStationFilter, 0, results);
        return results;
    }

    /**
     * Recursive helper method for range search.
     *
     * @param node current node being processed
     * @param latMin minimum latitude boundary
     * @param latMax maximum latitude boundary
     * @param lonMin minimum longitude boundary
     * @param lonMax maximum longitude boundary
     * @param countryFilter country filter
     * @param isCityFilter city station filter
     * @param isMainStationFilter main station filter
     * @param depth current depth in tree
     * @param results list to accumulate matching stations
     */
    private void searchInRangeRecursive(
            Node node,
            double latMin, double latMax,
            double lonMin, double lonMax,
            String countryFilter,
            Boolean isCityFilter,
            Boolean isMainStationFilter,
            int depth,
            List<EuropeanStation> results) {

        if (node == null) {
            return;
        }

        double currentLat = node.latitude;
        double currentLon = node.longitude;
        int dim = depth % 2; // 0 = latitude, 1 = longitude

        // Check if current node's coordinates are within the search rectangle
        boolean inLatRange = (currentLat >= latMin && currentLat <= latMax);
        boolean inLonRange = (currentLon >= lonMin && currentLon <= lonMax);

        // If node is within range, check all its stations against filters
        if (inLatRange && inLonRange) {
            for (EuropeanStation station : node.stations) {
                if (matchesFilters(station, countryFilter, isCityFilter, isMainStationFilter)) {
                    results.add(station);
                }
            }
        }

        // Determine which subtrees to explore based on current dimension and search boundaries
        if (dim == 0) { // Splitting dimension: latitude
            if (latMin <= currentLat) {
                searchInRangeRecursive(node.left, latMin, latMax, lonMin, lonMax,
                        countryFilter, isCityFilter, isMainStationFilter,
                        depth + 1, results);
            }
            if (latMax >= currentLat) {
                searchInRangeRecursive(node.right, latMin, latMax, lonMin, lonMax,
                        countryFilter, isCityFilter, isMainStationFilter,
                        depth + 1, results);
            }
        } else { // Splitting dimension: longitude
            if (lonMin <= currentLon) {
                searchInRangeRecursive(node.left, latMin, latMax, lonMin, lonMax,
                        countryFilter, isCityFilter, isMainStationFilter,
                        depth + 1, results);
            }
            if (lonMax >= currentLon) {
                searchInRangeRecursive(node.right, latMin, latMax, lonMin, lonMax,
                        countryFilter, isCityFilter, isMainStationFilter,
                        depth + 1, results);
            }
        }
    }

    /**
     * Checks if a station matches all specified filters.
     *
     * @param station the station to check
     * @param countryFilter country code filter
     * @param isCityFilter city station filter
     * @param isMainStationFilter main station filter
     * @return true if the station matches all filters, false otherwise
     */
    private boolean matchesFilters(EuropeanStation station,
                                   String countryFilter,
                                   Boolean isCityFilter,
                                   Boolean isMainStationFilter) {

        // Apply country filter if specified
        if (countryFilter != null && !countryFilter.equals("all")) {
            if (!countryFilter.equalsIgnoreCase(station.getCountry())) {
                return false;
            }
        }

        // Apply city station filter if specified
        if (isCityFilter != null) {
            if (isCityFilter != station.isCity()) {
                return false;
            }
        }

        // Apply main station filter if specified
        if (isMainStationFilter != null) {
            if (isMainStationFilter != station.isMainStation()) {
                return false;
            }
        }

        return true;
    }

    /**
     * Returns the number of nodes in the KD-Tree.
     *
     * @return the total number of nodes in the tree
     */
    public int size() {
        return this.size;
    }

    /**
     * Calculates the height of the KD-Tree.
     * The height is defined as the number of edges on the longest path from root to leaf.
     *
     * @return the height of the tree, or -1 if the tree is empty
     */
    public int height() {
        return heightRecursive(root);
    }

    /**
     * Recursive helper method to calculate tree height.
     *
     * @param node current node
     * @return height of the subtree rooted at this node
     */
    private int heightRecursive(Node node) {
        if (node == null) {
            return -1;
        }
        return 1 + Math.max(heightRecursive(node.left), heightRecursive(node.right));
    }

    /**
     * Analyzes the distribution of bucket sizes in the KD-Tree.
     * A bucket refers to a node containing multiple stations with identical coordinates.
     *
     * @return a map where keys are bucket sizes and values are the count of nodes with that size
     */
    public Map<Integer, Integer> getBucketSizes() {
        Map<Integer, Integer> bucketSizes = new HashMap<>();
        getBucketSizesRecursive(root, bucketSizes);
        return bucketSizes;
    }

    /**
     * Recursive helper method to collect bucket size statistics.
     *
     * @param node current node being processed
     * @param bucketSizes map to accumulate bucket size counts
     */
    private void getBucketSizesRecursive(Node node, Map<Integer, Integer> bucketSizes) {
        if (node == null) {
            return;
        }
        int bucketSize = node.stations.size();
        bucketSizes.put(bucketSize, bucketSizes.getOrDefault(bucketSize, 0) + 1);
        getBucketSizesRecursive(node.left, bucketSizes);
        getBucketSizesRecursive(node.right, bucketSizes);
    }
}
package pt.ipp.isep.dei.domain;

import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;
import java.util.Set;
import java.util.HashSet;
import java.util.stream.Collectors;

/**
 * Implements a balanced K-Dimensional Tree (KD-Tree) specifically for 2D spatial indexing
 * of European Stations based on Latitude and Longitude (USEI07).
 * The tree uses a bulk-build approach with median partitioning to ensure balance and
 * utilizes a bucket system to handle multiple stations with identical coordinates.
 */
public class KDTree {

    /**
     * Represents a node in the KD-Tree, acting as a bucket for stations with identical coordinates.
     */
    public static class Node {
        private final List<EuropeanStation> stations;
        private Node left;
        private Node right;
        private final double latitude;
        private final double longitude;
        private final int depth;

        /**
         * Constructs a new KD-Tree node (bucket).
         *
         * @param stationsInNode list of stations with identical coordinates to be stored in this node.
         * @param depth current depth in the tree, determining the splitting dimension.
         */
        public Node(List<EuropeanStation> stationsInNode, int depth) {
            if (stationsInNode.isEmpty()) {
                throw new IllegalArgumentException("Cannot create a tree node with an empty station list.");
            }

            // Secondary ordering by name (using EuropeanStation's compareTo)
            stationsInNode.sort(null);
            this.stations = stationsInNode;

            this.latitude = stationsInNode.get(0).getLatitude();
            this.longitude = stationsInNode.get(0).getLongitude();
            this.left = null;
            this.right = null;
            this.depth = depth;
        }

        // Getters for construction and search logic
        public List<EuropeanStation> getStations() { return stations; }
        public Node getLeft() { return left; }
        public Node getRight() { return right; }
        public double getLatitude() { return latitude; }
        public double getLongitude() { return longitude; }

        /**
         * Returns the coordinate value for the specified dimension (0: latitude, 1: longitude).
         * @param dim The dimension index (0 for Latitude, 1 for Longitude).
         */
        public double getCoordinate(int dim) { return (dim == 0) ? latitude : longitude; }

        /**
         * Returns the current depth in the tree.
         * @return The depth.
         */
        public int getDepth() { return depth; }
    }

    private Node root;
    private int size; // Total number of EuropeanStation objects stored in the tree

    /**
     * Constructs an empty KD-Tree.
     */
    public KDTree() {
        this.root = null;
        this.size = 0;
    }

    /**
     * Returns the root node of the KD-Tree.
     * @return The root node.
     */
    public Node getRoot() {
        return this.root;
    }


    /**
     * Builds a balanced KD-Tree from pre-sorted lists of stations by latitude and longitude.
     * This is the core implementation for USEI07.
     * Time Complexity: O(N log N) - N elements x log N recursion levels (assuming lists are already sorted).
     *
     * @param stationsByLat List of stations sorted by latitude.
     * @param stationsByLon List of stations sorted by longitude.
     */
    public void buildBalanced(List<EuropeanStation> stationsByLat, List<EuropeanStation> stationsByLon) {
        if (stationsByLat == null || stationsByLat.isEmpty()) return;

        // Reset size and copy lists to allow modifications within recursive calls
        this.size = 0;
        List<EuropeanStation> stationsLat = new ArrayList<>(stationsByLat);
        List<EuropeanStation> stationsLon = new ArrayList<>(stationsByLon);
        this.root = buildBalancedRecursive(stationsLat, stationsLon, 0);
    }

    /**
     * Recursively builds a balanced KD-Tree node using median partitioning (k-d partitioning).
     * Time Complexity: O(N) per level (dominated by list splitting) resulting in O(N log N) total complexity.
     *
     * @param stationsByLat List of stations sorted by latitude.
     * @param stationsByLon List of stations sorted by longitude.
     * @param depth Current depth in the tree.
     * @return The constructed node.
     */
    private Node buildBalancedRecursive(List<EuropeanStation> stationsByLat, List<EuropeanStation> stationsByLon, int depth) {
        if (stationsByLat.isEmpty()) {
            return null;
        }

        int dim = depth % 2; // 0 for latitude (X-axis split), 1 for longitude (Y-axis split)

        List<EuropeanStation> mainList = (dim == 0) ? stationsByLat : stationsByLon;
        int medianIndex = (mainList.size() - 1) / 2;
        EuropeanStation medianStation = mainList.get(medianIndex);

        double medianLat = medianStation.getLatitude();
        double medianLon = medianStation.getLongitude();

        // 1. Collect the bucket of stations with identical coordinates to the median station
        List<EuropeanStation> nodeStations = mainList.stream()
                .filter(s -> Double.compare(s.getLatitude(), medianLat) == 0 &&
                        Double.compare(s.getLongitude(), medianLon) == 0)
                .collect(Collectors.toList());

        this.size += nodeStations.size();

        Node node = new Node(nodeStations, depth);

        // Uses a Set for O(1) membership check during the partition step.
        Set<EuropeanStation> nodeStationsSet = new HashSet<>(nodeStations);

        // 2. Optimized Partition Logic
        List<EuropeanStation> leftLat = new ArrayList<>();
        List<EuropeanStation> rightLat = new ArrayList<>();
        List<EuropeanStation> leftLon = new ArrayList<>();
        List<EuropeanStation> rightLon = new ArrayList<>();

        double cutCoordinate = (dim == 0) ? medianLat : medianLon;

        // Partition the list sorted by Latitude (stationsByLat) - O(N) cost
        for (EuropeanStation station : stationsByLat) {
            // Skip stations already assigned to the node's bucket
            if (nodeStationsSet.contains(station)) continue;

            double coordToCompare = (dim == 0) ? station.getLatitude() : station.getLongitude();

            // The 'median' station is implicitly the cut, so we partition based on the cut coordinate.
            if (coordToCompare < cutCoordinate) {
                leftLat.add(station);
            } else {
                rightLat.add(station);
            }
        }

        // Partition the list sorted by Longitude (stationsByLon) - O(N) cost
        for (EuropeanStation station : stationsByLon) {
            // Skip stations already assigned to the node's bucket
            if (nodeStationsSet.contains(station)) continue;

            double coordToCompare = (dim == 0) ? station.getLatitude() : station.getLongitude();

            if (coordToCompare < cutCoordinate) {
                leftLon.add(station);
            } else {
                rightLon.add(station);
            }
        }

        // 3. Recursive calls
        node.left = buildBalancedRecursive(leftLat, leftLon, depth + 1);
        node.right = buildBalancedRecursive(rightLat, rightLon, depth + 1);

        return node;
    }

    /**
     * Returns the total number of EuropeanStation objects stored across all nodes (buckets) in the KD-Tree.
     * @return The total station count.
     */
    public int size() {
        return this.size;
    }

    /**
     * Calculates the height of the KD-Tree (maximum depth from root).
     * Time Complexity: O(N) in the worst case (needs to traverse all nodes).
     * @return The height of the tree.
     */
    public int height() {
        return heightRecursive(root);
    }

    private int heightRecursive(Node node) {
        if (node == null) {
            return -1;
        }
        return 1 + Math.max(heightRecursive(node.left), heightRecursive(node.right));
    }

    /**
     * Analyzes the distribution of bucket sizes (stations per node) in the KD-Tree.
     * @return A map where the key is the bucket size (number of stations) and the value is the count of nodes with that size.
     */
    public Map<Integer, Integer> getBucketSizes() {
        Map<Integer, Integer> bucketSizes = new HashMap<>();
        getBucketSizesRecursive(root, bucketSizes);
        return bucketSizes;
    }

    private void getBucketSizesRecursive(Node node, Map<Integer, Integer> bucketSizes) {
        if (node == null) {
            return;
        }
        int bucketSize = node.stations.size();
        bucketSizes.put(bucketSize, bucketSizes.getOrDefault(bucketSize, 0) + 1);
        getBucketSizesRecursive(node.left, bucketSizes);
        getBucketSizesRecursive(node.right, bucketSizes);
    }

    // --- Search Methods (USEI08, USEI10) ---

    /**
     * Finds the N nearest stations to a target coordinate, optionally applying a time zone filter.
     * (Requires the NearestNFinder class for implementation, as seen in the original code structure).
     */
    public List<EuropeanStation> findNearestN(
            double targetLat, double targetLon, int N, String timeZoneFilter) {

        if (this.root == null) {
            return new ArrayList<>();
        }

        // Delegates search logic to the NearestNFinder class
        NearestNFinder finder = new NearestNFinder(N, timeZoneFilter, targetLat, targetLon);

        finder.search(this.root);

        return finder.getResults();
    }

    /**
     * Finds all stations within a specified radius of a target coordinate (Radius Search).
     * Time Complexity: O(sqrt(N) + K) on average for a balanced 2D tree (where K is the number of results).
     *
     * @param targetLat Target point Latitude.
     * @param targetLon Target point Longitude.
     * @param radiusKm Search radius in kilometers.
     * @return List of stations within the specified radius.
     */
    public List<EuropeanStation> radiusSearch(double targetLat, double targetLon, double radiusKm) {
        List<EuropeanStation> results = new ArrayList<>();
        radiusSearchRecursive(root, targetLat, targetLon, radiusKm, results, 0);
        return results;
    }

    /**
     * Recursive method to search for stations within the specified radius, using pruning (PODA).
     */
    private void radiusSearchRecursive(Node node, double targetLat, double targetLon,
                                       double radiusKm, List<EuropeanStation> results, int depth) {
        if (node == null) {
            return;
        }

        // 1. Check and add the current node (Bucket)
        double distanceToNode = GeoDistance.haversine(targetLat, targetLon,
                node.getLatitude(), node.getLongitude());

        // If the node is within the radius, add all its stations
        if (distanceToNode <= radiusKm) {
            results.addAll(node.getStations());
        }

        // 2. Determine Subtrees and Pruning
        int dim = depth % 2;
        double targetCoord = (dim == 0) ? targetLat : targetLon;
        double nodeCoord = node.getCoordinate(dim);

        // Determine which subtree to explore first (closer)
        Node closerSubtree, fartherSubtree;
        if (targetCoord < nodeCoord) {
            closerSubtree = node.getLeft();
            fartherSubtree = node.getRight();
        } else {
            closerSubtree = node.getRight();
            fartherSubtree = node.getLeft();
        }

        // A. Always explore the closer subtree
        radiusSearchRecursive(closerSubtree, targetLat, targetLon, radiusKm, results, depth + 1);

        // B. Pruning condition: If the splitting plane intersects the search circle (radius),
        // the farther subtree must also be explored.

        double pointOnPlaneLat, pointOnPlaneLon;

        if (dim == 0) { // Cortamos por Latitude (dim=0). Plano é uma linha de Latitude constante.
            pointOnPlaneLat = nodeCoord;   // Latitude do plano de corte
            pointOnPlaneLon = targetLon;   // Longitude é a do ponto alvo
        } else { // Cortamos por Longitude (dim=1). Plano é uma linha de Longitude constante.
            pointOnPlaneLat = targetLat;   // Latitude é a do ponto alvo
            pointOnPlaneLon = nodeCoord;   // Longitude do plano de corte
        }

        // Calculate the minimum distance from the target point to the splitting plane
        double minDistanceToPlaneKm = GeoDistance.haversine(
                targetLat, targetLon,
                pointOnPlaneLat, pointOnPlaneLon
        );
        // If the distance to the splitting plane is less than or equal to the radius,
        // explore the farther subtree.
        if (minDistanceToPlaneKm <= radiusKm) {
            radiusSearchRecursive(fartherSubtree, targetLat, targetLon, radiusKm, results, depth + 1);
        }
    }
}
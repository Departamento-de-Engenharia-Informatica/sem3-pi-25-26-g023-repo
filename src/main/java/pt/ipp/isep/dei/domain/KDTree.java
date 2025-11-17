package pt.ipp.isep.dei.domain;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Implements a balanced 2D-Tree (KD-Tree) for storing EuropeanStation objects.
 * This class provides the underlying data structure for spatial indexing.
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
    public static class Node {
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

        /**
         * Returns the list of stations in this node.
         *
         * @return list of stations with identical coordinates
         */
        public List<EuropeanStation> getStations() {
            return stations;
        }

        /**
         * Returns the left child node.
         *
         * @return left child node, or null if no left child
         */
        public Node getLeft() {
            return left;
        }

        /**
         * Returns the right child node.
         *
         * @return right child node, or null if no right child
         */
        public Node getRight() {
            return right;
        }

        /**
         * Returns the latitude coordinate of this node.
         *
         * @return latitude value
         */
        public double getLatitude() {
            return latitude;
        }

        /**
         * Returns the longitude coordinate of this node.
         *
         * @return longitude value
         */
        public double getLongitude() {
            return longitude;
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
     * Returns the root node of the KD-Tree.
     *
     * @return root node of the tree, or null if tree is empty
     */
    public Node getRoot() {
        return this.root;
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
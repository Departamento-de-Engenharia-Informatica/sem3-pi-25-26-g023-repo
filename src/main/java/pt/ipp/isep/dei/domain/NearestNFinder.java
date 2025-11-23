package pt.ipp.isep.dei.domain;

import java.util.PriorityQueue;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.ArrayList;

/**
 * Implements the recursive k-Nearest Neighbor search algorithm on the KD-Tree.
 *
 * <p>It uses a Max-Heap (PriorityQueue with reversed ordering) to efficiently maintain the N closest neighbors
 * (update cost O(log N)).</p>
 * <p>It implements KD-Tree **Pruning** (PODA in Portuguese) optimization.</p>
 * <p>Complexity: O(log N) in the average case for a balanced tree.</p>
 */
public class NearestNFinder {

    // Max-Heap: A PriorityQueue with Comparator.reversed() is a Max-Heap.
    // The element at the top (peek) is the furthest among the N found neighbors.
    private final PriorityQueue<Neighbor> nearestNeighbors;
    private final int N;
    private final String filterTimeZone;
    private final double targetLat;
    private final double targetLon;

    /**
     * Constructor for the Finder. Initializes the Max-Heap for N elements.
     */
    public NearestNFinder(int N, String filterTimeZone, double targetLat, double targetLon) {
        this.N = N;
        this.filterTimeZone = filterTimeZone;
        this.targetLat = targetLat;
        this.targetLon = targetLon;

        // Max-Heap: Orders by the largest (reversed) so the most distant element is at the top (peek).
        this.nearestNeighbors = new PriorityQueue<>(N, Comparator.comparingDouble(Neighbor::getDistance).reversed());
    }

    /**
     * Recursive search method in the KD-Tree (k-Nearest Neighbor Search).
     * Optimization: Uses **Pruning** (PODA) by comparing the maximum distance in the heap with the splitting plane.
     * @param node The current KD-Tree node (or bucket) to be processed.
     */
    public void search(KDTree.Node node) {
        if (node == null) return;

        // 1. Node (Bucket) Processing
        for (EuropeanStation station : node.getStations()) {

            // Apply the FILTER (Acceptance Criterion)
            if (filterTimeZone == null || station.getTimeZoneGroup().equalsIgnoreCase(filterTimeZone)) {

                double distance = GeoDistance.haversine(targetLat, targetLon, station.getLatitude(), station.getLongitude());

                // Max-Heap Logic
                if (nearestNeighbors.size() < N) {
                    // Cost: O(log N)
                    nearestNeighbors.add(new Neighbor(station, distance));
                } else if (distance < nearestNeighbors.peek().getDistance()) {
                    // Cost: O(log N) (poll + add)
                    nearestNeighbors.poll(); // Remove the furthest neighbor
                    nearestNeighbors.add(new Neighbor(station, distance)); // Add the new closest neighbor
                }
            }
        }

        // 2. Subtree Determination and Pruning
        int dim = node.getDepth() % 2;
        double targetCoord = (dim == 0) ? targetLat : targetLon;
        double nodeCoord = node.getCoordinate(dim);

        // Determine the closer and the farther subtree
        KDTree.Node closerSubtree = (targetCoord < nodeCoord) ? node.getLeft() : node.getRight();
        KDTree.Node fartherSubtree = (targetCoord < nodeCoord) ? node.getRight() : node.getLeft();

        // A. Always explore the closer subtree first
        search(closerSubtree);

        // B. Pruning Logic
        // If N neighbors haven't been found yet, the other side must be explored (no maxDistanceInQueue yet)
        if (nearestNeighbors.size() < N) {
            search(fartherSubtree);
            return;
        }

        // maxDistanceInQueue is the Haversine distance of the N-th furthest neighbor.
        double maxDistanceInQueue = nearestNeighbors.peek().getDistance();
        // coordDiff is the minimum distance from the target point to the cutting plane (split axis)
        double coordDiff = Math.abs(targetCoord - nodeCoord);

        // PRUNING Condition: The distance from the cutting plane to the target point is less than the
        // current search radius (maxDistanceInQueue).
        // If `coordDiff` (minimum distance we could find on the other side) is less than the
        // `maxDistanceInQueue` (the worst result we currently have), the farther subtree must be explored.
        if (coordDiff < maxDistanceInQueue) {
            search(fartherSubtree);
        }
    }

    /**
     * Retrieves the final results, sorted by ascending distance.
     * Cost: O(N log N) for the final sorting.
     */
    public List<EuropeanStation> getResults() {
        List<Neighbor> sortedNeighbors = new ArrayList<>(nearestNeighbors);

        // Sort the final list (Heap to List) by ascending distance
        return sortedNeighbors.stream()
                .sorted(Comparator.comparingDouble(Neighbor::getDistance))
                .map(Neighbor::getStation)
                .collect(Collectors.toList());
    }
}
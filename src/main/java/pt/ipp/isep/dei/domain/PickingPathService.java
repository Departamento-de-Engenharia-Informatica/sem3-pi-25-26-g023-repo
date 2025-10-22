package pt.ipp.isep.dei.domain;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Service responsible for computing optimal or heuristic picking paths
 * for warehouse operations based on a given {@link PickingPlan}.
 * <p>
 * Two strategies are currently implemented:
 * <ul>
 *     <li><b>Strategy A (Deterministic Sweep)</b>: visits bays in sorted order by aisle and bay.</li>
 *     <li><b>Strategy B (Nearest Neighbour)</b>: applies a greedy heuristic by visiting the nearest valid bay at each step.</li>
 * </ul>
 * </p>
 * <p>
 * This class also defines an inner static class {@link PathResult} that encapsulates
 * both the computed path (list of {@link BayLocation}) and the total distance.
 * </p>
 */
public class PickingPathService {

    /** Constant reference to the entrance location (0,0). */
    private static final BayLocation ENTRANCE = BayLocation.entrance();

    /**
     * Represents the result of a computed picking path.
     * <p>
     * Contains both the ordered list of {@link BayLocation}s (the path)
     * and the total travel distance in arbitrary units.
     * </p>
     */
    public static class PathResult {
        /** Ordered list of locations representing the path. */
        public final List<BayLocation> path;

        /** Total computed distance for the path. */
        public final double totalDistance;

        /**
         * Constructs a {@code PathResult} with a path and its total distance.
         * <p>
         * The provided path list is defensively copied to prevent external modification.
         * </p>
         *
         * @param path           the ordered list of {@link BayLocation}s
         * @param totalDistance  the total distance for the path
         */
        public PathResult(List<BayLocation> path, double totalDistance) {
            this.path = (path != null) ? new ArrayList<>(path) : new ArrayList<>();
            this.totalDistance = totalDistance;
        }

        /**
         * Returns a formatted string representation of the path and its total distance.
         * <ul>
         *     <li>If the path is empty or null, a placeholder message is returned.</li>
         *     <li>Distances are formatted with two decimal places, and NaN/Infinity are handled gracefully.</li>
         * </ul>
         *
         * @return a human-readable string describing the path and distance
         */
        @Override
        public String toString() {
            String pathString;
            if (path == null || path.isEmpty()) {
                pathString = "Path is empty or null";
            } else {
                pathString = path.stream()
                        .filter(Objects::nonNull)
                        .map(BayLocation::toString)
                        .collect(Collectors.joining(" -> "));
            }

            String distString = Double.isNaN(totalDistance) ? "Not Calculated (NaN)" :
                    Double.isInfinite(totalDistance) ? "Infinite/Unreachable" :
                            String.format("%.2f", totalDistance);

            return "Path: " + pathString +
                    "\nTotal Distance: " + distString;
        }
    }

    /**
     * Calculates the picking paths for a given {@link PickingPlan}, applying both
     * deterministic and heuristic strategies.
     * <p>
     * Returns a map containing results for both strategies:
     * <ul>
     *     <li>"Strategy A (Deterministic Sweep)"</li>
     *     <li>"Strategy B (Nearest Neighbour)"</li>
     * </ul>
     * If the plan or its data is invalid, placeholder results are returned.
     * </p>
     *
     * @param plan the picking plan containing trolleys and their assignments
     * @return a map associating strategy names with their corresponding {@link PathResult}
     */
    public Map<String, PathResult> calculatePickingPaths(PickingPlan plan) {
        Map<String, PathResult> results = new HashMap<>();
        results.put("Strategy A (Deterministic Sweep)", new PathResult(List.of(ENTRANCE), 0.0));
        results.put("Strategy B (Nearest Neighbour)", new PathResult(List.of(ENTRANCE), 0.0));

        if (plan == null || plan.getTrolleys() == null || plan.getTrolleys().isEmpty()) {
            System.out.println("⚠️ Picking plan is empty or null.");
            return results;
        }

        // 1. Extract unique and valid bay locations
        Set<BayLocation> uniqueValidBays = new HashSet<>();

        for (Trolley trolley : plan.getTrolleys()) {
            if (trolley == null || trolley.getAssignments() == null) continue;
            for (PickingAssignment assignment : trolley.getAssignments()) {
                if (assignment == null) continue;

                BayLocation loc = new BayLocation(assignment);
                if (loc.isValid()) {
                    uniqueValidBays.add(loc);
                }
            }
        }

        List<BayLocation> sortedUniqueValidBays = uniqueValidBays.stream()
                .filter(Objects::nonNull)
                .sorted()
                .collect(Collectors.toList());

        if (sortedUniqueValidBays.isEmpty()) {
            System.out.println("ℹ️ No valid bays found in this picking plan.");
            return results;
        }

        System.out.printf("  ➡️  Calculating routes for %d unique valid locations.%n", sortedUniqueValidBays.size());

        // 2. Compute both strategies with error handling
        try {
            results.put("Strategy A (Deterministic Sweep)", calculateStrategyA(new ArrayList<>(sortedUniqueValidBays)));
        } catch (Exception e) {
            System.err.println("❌ Error calculating Strategy A: " + e.getMessage());
            e.printStackTrace();
            results.put("Strategy A (Deterministic Sweep)", new PathResult(List.of(ENTRANCE), Double.NaN));
        }
        try {
            results.put("Strategy B (Nearest Neighbour)", calculateStrategyB(new ArrayList<>(sortedUniqueValidBays)));
        } catch (Exception e) {
            System.err.println("❌ Error calculating Strategy B: " + e.getMessage());
            e.printStackTrace();
            results.put("Strategy B (Nearest Neighbour)", new PathResult(List.of(ENTRANCE), Double.NaN));
        }

        return results;
    }

    /**
     * Strategy A – Deterministic Sweep.
     * <p>
     * Visits all valid bays in ascending order by aisle, then bay.
     * The route always starts at the warehouse entrance (0,0).
     * </p>
     *
     * @param sortedBays the list of sorted bay locations
     * @return a {@link PathResult} containing the computed path and distance
     */
    private PathResult calculateStrategyA(List<BayLocation> sortedBays) {
        List<BayLocation> path = new ArrayList<>();
        path.add(ENTRANCE);
        path.addAll(sortedBays);

        double totalDistance = calculateTotalDistance(path);
        return new PathResult(path, totalDistance);
    }

    /**
     * Strategy B – Nearest Neighbour heuristic.
     * <p>
     * Iteratively visits the nearest remaining bay from the current location,
     * starting from the warehouse entrance.
     * </p>
     *
     * @param bays the list of bays to visit
     * @return a {@link PathResult} with the computed path and total distance
     */
    private PathResult calculateStrategyB(List<BayLocation> bays) {
        List<BayLocation> path = new ArrayList<>();
        path.add(ENTRANCE);

        BayLocation currentLocation = ENTRANCE;
        Set<BayLocation> remainingBays = new HashSet<>(bays);
        int iteration = 0;
        final int MAX_ITERATIONS = bays.size() + 5;

        while (!remainingBays.isEmpty() && iteration < MAX_ITERATIONS) {
            iteration++;
            BayLocation nearest = null;
            double minDistance = Double.POSITIVE_INFINITY;

            // Find nearest valid bay
            for (BayLocation potentialNext : remainingBays) {
                if (potentialNext == null) continue;
                double distance = calculateDistance(currentLocation, potentialNext);

                if (distance < minDistance) {
                    minDistance = distance;
                    nearest = potentialNext;
                } else if (distance == minDistance && nearest != null && potentialNext.compareTo(nearest) < 0) {
                    nearest = potentialNext;
                }
            }

            if (nearest != null && !Double.isInfinite(minDistance)) {
                path.add(nearest);
                currentLocation = nearest;
                remainingBays.remove(nearest);
            } else {
                if (!remainingBays.isEmpty()) {
                    System.err.printf("❌ Error B: No reachable neighbour found at iteration %d from %s among %d remaining: %s%n",
                            iteration, currentLocation, remainingBays.size(), remainingBays);
                    return new PathResult(path, Double.POSITIVE_INFINITY);
                }
                break;
            }
        }

        if (iteration >= MAX_ITERATIONS) {
            System.err.println("❌ Error B: Maximum iterations reached.");
            return new PathResult(path, Double.POSITIVE_INFINITY);
        }

        double totalDistance = calculateTotalDistance(path);
        return new PathResult(path, totalDistance);
    }

    /**
     * Computes the distance between two bay locations based on warehouse layout logic.
     * <ul>
     *     <li>Distances along the same aisle are computed as the absolute bay difference.</li>
     *     <li>Moving between aisles adds a fixed cost (×3 per aisle difference).</li>
     *     <li>The entrance (0,0) is handled as a special case.</li>
     * </ul>
     * Invalid locations (negative values) result in {@code Double.POSITIVE_INFINITY}.
     *
     * @param c1 the first location
     * @param c2 the second location
     * @return the computed distance between {@code c1} and {@code c2}
     */
    private double calculateDistance(BayLocation c1, BayLocation c2) {
        if (c1 == null || c2 == null) return Double.POSITIVE_INFINITY;

        int a1 = c1.getAisle();
        int b1 = c1.getBay();
        int a2 = c2.getAisle();
        int b2 = c2.getBay();

        boolean c1ValidForCalc = (a1 >= 0 && b1 >= 0);
        boolean c2ValidForCalc = (a2 >= 0 && b2 >= 0);

        if (!c1ValidForCalc || !c2ValidForCalc) {
            return Double.POSITIVE_INFINITY;
        }

        if (a1 == 0 && b1 == 0) {
            return Math.abs(a2) * 3.0 + Math.abs(b2);
        }
        if (a2 == 0 && b2 == 0) {
            return Math.abs(b1) + Math.abs(a1) * 3.0;
        }

        if (a1 == a2) {
            return Math.abs(b1 - b2);
        } else {
            return b1 + Math.abs(a1 - a2) * 3.0 + b2;
        }
    }

    /**
     * Computes the total distance for a given path, summing all segment distances.
     * <p>
     * If any segment is invalid or infinite, the result will be {@code Double.POSITIVE_INFINITY}.
     * </p>
     *
     * @param path the ordered list of bay locations representing the path
     * @return the total computed distance, or infinity if invalid
     */
    private double calculateTotalDistance(List<BayLocation> path) {
        double totalDistance = 0;
        if (path == null || path.size() < 2) return 0;

        for (int i = 0; i < path.size() - 1; i++) {
            BayLocation current = path.get(i);
            BayLocation next = path.get(i + 1);

            if (current == null || next == null) {
                System.err.printf("❌ Error TotalDistance: Null point in path [%d or %d]%n", i, i + 1);
                return Double.POSITIVE_INFINITY;
            }

            double segmentDistance = calculateDistance(current, next);

            if (Double.isInfinite(segmentDistance) || Double.isNaN(segmentDistance)) {
                System.err.printf("❌ Error TotalDistance: Invalid segment (%s -> %s)%n", current, next);
                return Double.POSITIVE_INFINITY;
            }
            totalDistance += segmentDistance;
        }
        return totalDistance;
    }
}

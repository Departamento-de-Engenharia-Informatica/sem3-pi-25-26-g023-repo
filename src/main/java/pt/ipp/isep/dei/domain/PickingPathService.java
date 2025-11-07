package pt.ipp.isep.dei.domain;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Service responsible for computing optimal or heuristic picking paths
 * for warehouse operations based on a given {@link PickingPlan}.
 * (Versão 2.0 - "Bonito" toString e outputs de log)
 */
public class PickingPathService {

    private static final String ANSI_RESET = "\u001B[0m";
    private static final String ANSI_RED = "\u001B[31m";
    private static final String ANSI_YELLOW = "\u001B[33m";
    private static final String ANSI_CYAN = "\u001B[36m";
    private static final String ANSI_BOLD = "\u001B[1m";
    private static final String ANSI_ITALIC = "\u001B[3m";

    /**
     * Constant reference to the entrance location (0,0).
     */
    private static final BayLocation ENTRANCE = BayLocation.entrance();

    /**
     * Calculates the picking paths, now with "bonito" logging.
     */
    public Map<String, PathResult> calculatePickingPaths(PickingPlan plan) {
        Map<String, PathResult> results = new HashMap<>();
        results.put("Strategy A (Deterministic Sweep)", new PathResult(List.of(ENTRANCE), 0.0));
        results.put("Strategy B (Nearest Neighbour)", new PathResult(List.of(ENTRANCE), 0.0));

        if (plan == null || plan.getTrolleys() == null || plan.getTrolleys().isEmpty()) {
            // --- ALTERAÇÃO (Output "Bonito") ---
            System.out.println(ANSI_YELLOW + "⚠️ Picking plan is empty or null. No paths to calculate." + ANSI_RESET);
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
            // --- ALTERAÇÃO (Output "Bonito") ---
            System.out.println(ANSI_YELLOW + "ℹ️ No valid bays found in this picking plan. Nothing to route." + ANSI_RESET);
            return results;
        }

        // --- ALTERAÇÃO (Output "Bonito") ---
        System.out.printf("  %s➡️  Calculating routes for %d unique valid locations...%s%n",
                ANSI_ITALIC, sortedUniqueValidBays.size(), ANSI_RESET);

        // 2. Compute both strategies with error handling
        try {
            results.put("Strategy A (Deterministic Sweep)", calculateStrategyA(new ArrayList<>(sortedUniqueValidBays)));
        } catch (Exception e) {
            // --- ALTERAÇÃO (Output "Bonito") ---
            System.err.println(ANSI_RED + ANSI_BOLD + "❌ Error calculating Strategy A: " + ANSI_RESET + ANSI_RED + e.getMessage() + ANSI_RESET);
            e.printStackTrace(System.err);
            results.put("Strategy A (Deterministic Sweep)", new PathResult(List.of(ENTRANCE), Double.NaN));
        }
        try {
            results.put("Strategy B (Nearest Neighbour)", calculateStrategyB(new ArrayList<>(sortedUniqueValidBays)));
        } catch (Exception e) {
            // --- ALTERAÇÃO (Output "Bonito") ---
            System.err.println(ANSI_RED + ANSI_BOLD + "❌ Error calculating Strategy B: " + ANSI_RESET + ANSI_RED + e.getMessage() + ANSI_RESET);
            e.printStackTrace(System.err);
            results.put("Strategy B (Nearest Neighbour)", new PathResult(List.of(ENTRANCE), Double.NaN));
        }

        return results;
    }

    /**
     * Strategy A – Deterministic Sweep.
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
                    // --- ALTERAÇÃO (Output "Bonito") ---
                    System.err.printf(ANSI_RED + "❌ Error B: No reachable neighbour found at iteration %d from %s among %d remaining: %s%n" + ANSI_RESET,
                            iteration, currentLocation, remainingBays.size(), remainingBays);
                    return new PathResult(path, Double.POSITIVE_INFINITY);
                }
                break;
            }
        }

        if (iteration >= MAX_ITERATIONS) {
            // --- ALTERAÇÃO (Output "Bonito") ---
            System.err.println(ANSI_RED + "❌ Error B: Maximum iterations reached. Aborting." + ANSI_RESET);
            return new PathResult(path, Double.POSITIVE_INFINITY);
        }

        double totalDistance = calculateTotalDistance(path);
        return new PathResult(path, totalDistance);
    }

    /**
     * Computes the distance between two bay locations.
     * (Lógica mantida)
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
     * Computes the total distance for a given path.
     * (Lógica mantida, outputs de erro melhorados)
     */
    private double calculateTotalDistance(List<BayLocation> path) {
        double totalDistance = 0;
        if (path == null || path.size() < 2) return 0;

        for (int i = 0; i < path.size() - 1; i++) {
            BayLocation current = path.get(i);
            BayLocation next = path.get(i + 1);

            if (current == null || next == null) {
                // --- ALTERAÇÃO (Output "Bonito") ---
                System.err.printf(ANSI_RED + "❌ Error TotalDistance: Null point in path [%d or %d]%n" + ANSI_RESET, i, i + 1);
                return Double.POSITIVE_INFINITY;
            }

            double segmentDistance = calculateDistance(current, next);

            if (Double.isInfinite(segmentDistance) || Double.isNaN(segmentDistance)) {
                // --- ALTERAÇÃO (Output "Bonito") ---
                System.err.printf(ANSI_RED + "❌ Error TotalDistance: Invalid segment (%s -> %s)%n" + ANSI_RESET, current, next);
                return Double.POSITIVE_INFINITY;
            }
            totalDistance += segmentDistance;
        }
        return totalDistance;
    }

    /**
     * Represents the result of a computed picking path.
     * (Versão 2.0 - "Bonito" toString)
     */
    public static class PathResult {
        // --- Códigos de Cores (Privados para a classe interna) ---
        private static final String C_RESET = "\u001B[0m";
        private static final String C_CYAN = "\u001B[36m";
        private static final String C_BOLD = "\u001B[1m";
        private static final String C_ITALIC = "\u001B[3m";
        private static final String C_YELLOW = "\u001B[33m";

        public final List<BayLocation> path;
        public final double totalDistance;

        public PathResult(List<BayLocation> path, double totalDistance) {
            this.path = (path != null) ? new ArrayList<>(path) : new ArrayList<>();
            this.totalDistance = totalDistance;
        }

        // -----------------------------------------------------------------
        // --- ALTERAÇÃO (Output "Bonito") ---
        // -----------------------------------------------------------------
        @Override
        public String toString() {
            String pathString;
            if (path == null || path.isEmpty() || (path.size() == 1 && path.get(0).equals(ENTRANCE))) {
                pathString = C_ITALIC + "Path is empty or only contains entrance." + C_RESET;
            } else {
                pathString = path.stream()
                        .filter(Objects::nonNull)
                        .map(loc -> {
                            // Destaca a ENTRADA
                            if (loc.equals(ENTRANCE)) {
                                return C_BOLD + C_YELLOW + loc + C_RESET;
                            }
                            return loc.toString();
                        })
                        .collect(Collectors.joining(C_CYAN + " -> " + C_RESET));
            }

            String distString = Double.isNaN(totalDistance) ? "Not Calculated (NaN)" :
                    Double.isInfinite(totalDistance) ? "Infinite/Unreachable" :
                            String.format("%.2f", totalDistance);

            return String.format(
                    "  %sPath:%s %s\n" +
                            "  %sTotal Distance:%s %s%.2f units%s",
                    C_BOLD, C_RESET, pathString,
                    C_BOLD, C_RESET, C_BOLD + C_YELLOW, totalDistance, C_RESET
            );
        }
    }
}
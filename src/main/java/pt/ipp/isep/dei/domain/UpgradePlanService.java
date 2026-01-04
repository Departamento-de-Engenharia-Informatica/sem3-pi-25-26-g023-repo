package pt.ipp.isep.dei.domain;

import java.util.*;

/**
 * Service responsible for planning the upgrade of railway lines.
 * <p>
 * This class builds a directed graph of station dependencies and attempts to determine
 * a topological order for the upgrades. It includes functionality to detect circular
 * dependencies (cycles) which prevent a linear upgrade sequence and generates
 * visual reports (DOT/SVG).
 * </p>
 */

public class UpgradePlanService {

    private final Map<Integer, List<Integer>> adjacencies = new HashMap<>();
    private final Map<Integer, Integer> inDegree = new HashMap<>();

    private List<Integer> lastOrderedList = new ArrayList<>();
    private Map<Integer, Integer> lastRemainingInDegrees = new HashMap<>();

    /**
     * Adds a directed dependency between two stations.
     * <p>
     * Indicates that the station {@code fromId} must be upgraded before the station {@code toId}.
     * This method updates the adjacency list and the in-degree count of the destination station.
     * </p>
     *
     * @param fromId The ID of the station that must be upgraded first (source).
     * @param toId   The ID of the station that depends on the source (destination).
     */
    public void addDependency(int fromId, int toId) {
        adjacencies.putIfAbsent(fromId, new ArrayList<>());

        if (!adjacencies.get(fromId).contains(toId)) {
            adjacencies.get(fromId).add(toId);
            inDegree.put(toId, inDegree.getOrDefault(toId, 0) + 1);
            inDegree.putIfAbsent(fromId, 0);
        }
    }

    /**
     * USEI11 - Computes the topological order of the upgrades or identifies cycles.
     * <p>
     * This method uses Kahn's Algorithm to sort the dependency graph. If the graph contains
     * cycles (circular dependencies), the sort will be incomplete, and the remaining nodes
     * will be identified as blocked.
     * </p>
     *
     * @return A formatted String report containing the upgrade order, detected cycles (if any),
     * and performance metrics.
     */
    public String computeAndFormatUpgradePlan() {
        long startTime = System.nanoTime();

        Queue<Integer> queue = new LinkedList<>();
        List<Integer> resultOrder = new ArrayList<>();
        Map<Integer, Integer> tempInDegree = new HashMap<>(inDegree);

        for (Map.Entry<Integer, Integer> entry : tempInDegree.entrySet()) {
            if (entry.getValue() == 0) {
                queue.add(entry.getKey());
            }
        }

        while (!queue.isEmpty()) {
            int current = queue.poll();
            resultOrder.add(current);

            if (adjacencies.containsKey(current)) {
                for (int neighbor : adjacencies.get(current)) {
                    tempInDegree.put(neighbor, tempInDegree.get(neighbor) - 1);
                    if (tempInDegree.get(neighbor) == 0) {
                        queue.add(neighbor);
                    }
                }
            }
        }

        this.lastOrderedList = new ArrayList<>(resultOrder);
        this.lastRemainingInDegrees = new HashMap<>(tempInDegree);

        long endTime = System.nanoTime();
        double durationMs = (endTime - startTime) / 1_000_000.0;

        return formatFinalOutput(resultOrder, tempInDegree, durationMs);
    }

    /**
     * Generates a Graphviz DOT file to visualize the upgrade dependencies.
     * <p>
     * This method applies color coding to the nodes based on the result of the analysis:
     * <ul>
     * <li><b>Green:</b> Stations that can be upgraded (part of the valid topological order).</li>
     * <li><b>Red:</b> Stations blocked by circular dependencies (Cycles).</li>
     * </ul>
     * </p>
     *
     * @param filename The name (or path) of the .dot file to be created.
     */
    public void generateUpgradeDiagram(String filename) {
        try (java.io.PrintWriter writer = new java.io.PrintWriter(filename)) {
            writer.println("digraph BelgiumUpgradePlan {");
            writer.println("    rankdir=LR;");
            writer.println("    node [fontname=\"Arial\", style=filled];");
            writer.println("    edge [color=\"#555555\", penwidth=1.0, arrowsize=0.8];");

            Set<Integer> allStations = new HashSet<>();
            allStations.addAll(adjacencies.keySet());
            allStations.addAll(inDegree.keySet());
            allStations.addAll(lastRemainingInDegrees.keySet());

            for (Integer id : allStations) {
                int remaining = lastRemainingInDegrees.getOrDefault(id, 0);

                if (remaining > 0) {
                    writer.printf("    \"%d\" [fillcolor=\"#ff9999\", color=\"#cc0000\", label=\"ST %d\\n(Ciclo: %d)\", shape=doublecircle];\n",
                            id, id, remaining);
                } else {
                    writer.printf("    \"%d\" [fillcolor=\"#ccffcc\", color=\"#006600\", label=\"ST %d\", shape=ellipse];\n",
                            id, id);
                }
            }

            for (Map.Entry<Integer, List<Integer>> entry : adjacencies.entrySet()) {
                int from = entry.getKey();
                for (int to : entry.getValue()) {
                    writer.printf("    \"%d\" -> \"%d\";\n", from, to);
                }
            }

            writer.println("}");
            System.out.println("   [Visual] Ficheiro DOT atualizado: " + filename);

        } catch (java.io.IOException e) {
            System.err.println("   [Erro] Não foi possível gerar o diagrama: " + e.getMessage());
        }
    }

    /**
     * Registers a station in the graph structures.
     * <p>
     * Ensures the station exists in the internal maps, initializing its adjacency list
     * and in-degree counter if necessary.
     * </p>
     *
     * @param stationId The unique identifier of the station.
     */
    public void registerStation(int stationId) {
        inDegree.putIfAbsent(stationId, 0);
        adjacencies.putIfAbsent(stationId, new ArrayList<>());
    }

    /**
     * Formats the final analysis report into a readable string.
     *
     * @param order            The resulting list of ordered stations.
     * @param remainingDegrees The map of in-degrees remaining after the sort (used to detect cycles).
     * @param time             The execution time in milliseconds.
     * @return A formatted string containing the report.
     */
    private String formatFinalOutput(List<Integer> order, Map<Integer, Integer> remainingDegrees, double time) {
        StringBuilder sb = new StringBuilder();
        sb.append("\n" + "=".repeat(60) + "\n");
        sb.append("📊 USEI11 - DIRECTED LINE UPGRADE PLAN REPORT\n");
        sb.append("=".repeat(60) + "\n");

        if (order.size() == inDegree.size()) {
            sb.append("✅ SUCCESS: No cycles detected. Optimal upgrade order found.\n\n");
            sb.append("RANKING DE UPGRADE:\n");
            for (int i = 0; i < order.size(); i++) {
                sb.append(String.format("   %dº -> Station ID: %d\n", i + 1, order.get(i)));
            }
        }
        else {
            sb.append("⚠️ WARNING: Directed dependencies contain cycles!\n");
            sb.append("The following stations cannot be ordered due to circular dependencies:\n");
            remainingDegrees.forEach((id, degree) -> {
                if (degree > 0) sb.append(String.format("   • Station ID: %d (Remaining dependencies: %d)\n", id, degree));
            });
        }

        sb.append("\n" + "-".repeat(60) + "\n");
        sb.append(String.format("⏱️  Temporal Analysis: %.4f ms\n", time));
        sb.append("📂 Complexity: O(V + E)\n");
        sb.append("=".repeat(60) + "\n");

        return sb.toString();
    }

    /**
     * Converts a DOT file to an SVG image using the external Graphviz 'dot' command.
     * <p>
     * Note: This method requires Graphviz to be installed and available in the system's PATH.
     * </p>
     *
     * @param dotFile The path to the source DOT file.
     * @param svgFile The path where the output SVG file will be saved.
     */
    public void generateSVG(String dotFile, String svgFile) {
        try {
            ProcessBuilder pb = new ProcessBuilder("dot", "-Tsvg", dotFile, "-o", svgFile);
            Process process = pb.start();
            if (process.waitFor() == 0) {
                System.out.println("   [Visual] SVG diagram successfully generated: " + svgFile);
            }
        } catch (Exception e) {
            System.err.println("   [Note] SVG could not be generated. Ensure 'dot' command is in your PATH.");
        }
    }

}
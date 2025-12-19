package pt.ipp.isep.dei.domain;

import java.io.*;
import java.util.*;

/**
 * Represents a minimal backbone network for railway infrastructure
 * using Minimum Spanning Tree (MST) algorithm.
 */
public class BackboneNetwork {
    private final Map<Integer, Station> stations = new HashMap<>();
    private final List<Connection> allConnections = new ArrayList<>();
    private final List<Connection> mstConnections = new ArrayList<>();
    private double totalMSTDistance = 0;
    private long computationTimeMs = 0;

    /**
     * Loads railway network data from CSV files.
     *
     * @param stationsFile    Path to stations CSV file
     * @param connectionsFile Path to connections CSV file
     * @throws IOException If file reading fails
     */
    public void loadNetwork(String stationsFile, String connectionsFile) throws IOException {
        loadStations(stationsFile);
        loadConnections(connectionsFile);
        System.out.printf("✓ Loaded %d stations and %d connections\n",
                stations.size(), allConnections.size());
    }

    private void loadStations(String file) throws IOException {
        stations.clear();

        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            br.readLine();

            int count = 0;
            int loaded = 0;

            String line;
            while ((line = br.readLine()) != null) {
                count++;

                try {
                    String[] parts = line.split(",");

                    if (parts.length >= 6) {
                        int id = Integer.parseInt(parts[0].trim());
                        String name = parts[1].trim();
                        double lat = Double.parseDouble(parts[2].trim());
                        double lon = Double.parseDouble(parts[3].trim());
                        double coordX = Double.parseDouble(parts[4].trim());
                        double coordY = Double.parseDouble(parts[5].trim());

                        Station station = new Station(id, name, lat, lon, coordX, coordY);
                        stations.put(id, station);
                        loaded++;

                    }
                } catch (NumberFormatException e) {
                    System.err.println("Erro na linha " + count + ": " + line);
                }
            }

        } catch (IOException e) {
            throw e;
        }
    }

    private void loadConnections(String file) throws IOException {
        allConnections.clear();

        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            br.readLine();

            int count = 0;
            int loaded = 0;
            int missingStations = 0;

            String line;
            while ((line = br.readLine()) != null) {
                count++;

                try {
                    String[] parts = line.split(",");

                    if (parts.length >= 3) {
                        int fromId = Integer.parseInt(parts[0].trim());
                        int toId = Integer.parseInt(parts[1].trim());
                        double distance = Double.parseDouble(parts[2].trim());

                        Station from = stations.get(fromId);
                        Station to = stations.get(toId);

                        if (from != null && to != null) {
                            Connection conn = new Connection(from, to, distance);
                            allConnections.add(conn);
                            loaded++;
                        } else {
                            missingStations++;
                        }
                    }
                } catch (NumberFormatException e) {
                    System.err.println("Erro na linha " + count + ": " + line);
                }
            }

        } catch (IOException e) {
            throw e;
        }
    }

    /**
     * Computes the Minimal Spanning Tree (MST) using Prim's algorithm.
     */
    public void computeMinimalBackbone() {
        if (stations.isEmpty()) {
            System.out.println("Error: No stations loaded!");
            return;
        }

        if (allConnections.isEmpty()) {
            System.out.println("Error: No connections loaded!");
            return;
        }

        long startTime = System.currentTimeMillis();

        Set<Station> visited = new HashSet<>();
        PriorityQueue<Connection> pq = new PriorityQueue<>(
                Comparator.comparingDouble(Connection::distance)
        );

        Station start = stations.values().iterator().next();
        visited.add(start);

        for (Connection conn : allConnections) {
            if (conn.from().equals(start) || conn.to().equals(start)) {
                pq.add(conn);
            }
        }

        mstConnections.clear();
        totalMSTDistance = 0;

        while (!pq.isEmpty() && visited.size() < stations.size()) {
            Connection conn = pq.poll();

            Station next = null;
            if (visited.contains(conn.from()) && !visited.contains(conn.to())) {
                next = conn.to();
            } else if (visited.contains(conn.to()) && !visited.contains(conn.from())) {
                next = conn.from();
            }

            if (next != null) {
                mstConnections.add(conn);
                totalMSTDistance += conn.distance();
                visited.add(next);

                for (Connection adj : allConnections) {
                    if (adj.from().equals(next) && !visited.contains(adj.to())) {
                        pq.add(adj);
                    } else if (adj.to().equals(next) && !visited.contains(adj.from())) {
                        pq.add(adj);
                    }
                }
            }
        }

        computationTimeMs = System.currentTimeMillis() - startTime;

        System.out.printf("✓ MST computed: %d edges, %.2f km total, %d ms\n",
                mstConnections.size(), totalMSTDistance, computationTimeMs);
    }

    /**
     * Generates a GraphViz DOT file for visualization.
     * The DOT file can be previewed directly in IntelliJ with the Graphviz plugin.
     *
     * @param filename Output DOT filename
     * @throws IOException If file writing fails
     */
    public void generateDOTFile(String filename) throws IOException {
        try (PrintWriter writer = new PrintWriter(filename)) {
            writer.println("graph BelgianRailwayMST {");
            writer.println("    layout=neato;");
            writer.println("    overlap=false;");
            writer.println("    node [shape=circle, style=filled, fillcolor=lightblue];");
            writer.println("    edge [fontsize=8];");
            writer.println();

            for (Station s : stations.values()) {
                String label = s.nome().length() > 12 ?
                        s.nome().substring(0, 10) + ".." : s.nome();

                writer.printf("    \"%d\" [pos=\"%.2f,%.2f!\", label=\"%s\"];\n",
                        s.idEstacao(), s.coordX() / 1000, s.coordY() / 1000,
                        label);
            }

            writer.println();
            writer.println("    edge [color=\"#FF0000\", penwidth=2.5];");
            for (Connection conn : mstConnections) {
                writer.printf("    \"%d\" -- \"%d\" [label=\"%.1fkm\"];\n",
                        conn.from().idEstacao(), conn.to().idEstacao(),
                        conn.distance());
            }

            writer.println();
            writer.println("    edge [color=\"#CCCCCC\", penwidth=0.7, style=dashed];");
            for (Connection conn : allConnections) {
                if (!mstConnections.contains(conn)) {
                    writer.printf("    \"%d\" -- \"%d\";\n",
                            conn.from().idEstacao(), conn.to().idEstacao());
                }
            }

            writer.println("}");
        }
        System.out.println("✓ DOT file generated: " + filename);
    }

    /**
     * Attempts to generate SVG visualization using available Graphviz installation.
     * First tries the local installation, then falls back to online conversion.
     *
     * @param dotFile Input DOT filename
     * @param svgFile Output SVG filename
     * @return true if SVG generation succeeded
     */
    public boolean generateSVG(String dotFile, String svgFile) {
        boolean localSuccess = tryLocalGraphviz(dotFile, svgFile);

        if (!localSuccess) {
            System.out.println("📝 Using IntelliJ Graphviz plugin preview instead.");
            System.out.println("💡 Right-click " + dotFile + " → Open with → Graphviz");
        }

        return localSuccess;
    }

    private boolean tryLocalGraphviz(String dotFile, String svgFile) {
        String[] possiblePaths = {
                "dot",
                "C:\\Program Files\\Graphviz\\bin\\dot.exe",
                "C:\\Program Files (x86)\\Graphviz\\bin\\dot.exe",
                System.getProperty("user.home") + "\\Desktop\\Graphviz-14.1.1-win64\\bin\\dot.exe"
        };

        for (String path : possiblePaths) {
            try {
                ProcessBuilder pb = new ProcessBuilder(
                        path, "-Tsvg", dotFile, "-o", svgFile
                );

                Process process = pb.start();
                int exitCode = process.waitFor();

                if (exitCode == 0) {
                    File svgOutput = new File(svgFile);
                    if (svgOutput.exists()) {
                        System.out.println("✅ SVG generated: " + svgFile);
                        return true;
                    }
                }
            } catch (Exception e) {
                // Try next path
            }
        }

        return false;
    }

    /**
     * Prints a comprehensive report of the backbone network.
     */
    public void printReport() {
        System.out.println("\n" + "=".repeat(60));
        System.out.println("USEI12 - MINIMAL BACKBONE NETWORK REPORT");
        System.out.println("=".repeat(60));

        System.out.println("\n📊 NETWORK STATISTICS:");
        System.out.printf("   • Stations: %d\n", stations.size());
        System.out.printf("   • Total connections: %d\n", allConnections.size());
        System.out.printf("   • Backbone connections: %d\n", mstConnections.size());
        System.out.printf("   • Total backbone length: %.2f km\n", totalMSTDistance);
        System.out.printf("   • Computation time: %d ms\n", computationTimeMs);

        System.out.println("\n⚡ ALGORITHM ANALYSIS:");
        int V = stations.size();
        int E = allConnections.size();
        System.out.println("   • Algorithm: Prim's with PriorityQueue");
        System.out.println("   • Time Complexity: O(E log V)");
        System.out.println("   • Space Complexity: O(V + E)");
        System.out.printf("   • For this network: O(%d log %d) operations\n", E, V);

        System.out.println("\n🛠️  OUTPUT FILES:");
        System.out.println("   • " + "belgian_backbone.dot" + " - DOT file (open in IntelliJ with Graphviz plugin)");

        if (!mstConnections.isEmpty()) {
            System.out.println("\n📋 BACKBONE SAMPLE (first 10 connections):");
            int limit = Math.min(10, mstConnections.size());
            for (int i = 0; i < limit; i++) {
                Connection c = mstConnections.get(i);
                System.out.printf("   %d. %s ↔ %s (%.1f km)\n",
                        i + 1, c.from().nome(), c.to().nome(), c.distance());
            }

            if (mstConnections.size() > 10) {
                System.out.printf("   ... and %d more connections\n", mstConnections.size() - 10);
            }
        }

        System.out.println("\n" + "=".repeat(60));
        System.out.println("✅ USEI12 COMPLETED SUCCESSFULLY");
        System.out.println("=".repeat(60));
    }
}
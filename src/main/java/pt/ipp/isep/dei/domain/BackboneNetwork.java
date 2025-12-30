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

    private double minX = Double.MAX_VALUE;
    private double maxX = Double.MIN_VALUE;
    private double minY = Double.MAX_VALUE;
    private double maxY = Double.MIN_VALUE;

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
        minX = Double.MAX_VALUE;
        maxX = Double.MIN_VALUE;
        minY = Double.MAX_VALUE;
        maxY = Double.MIN_VALUE;

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

                        minX = Math.min(minX, coordX);
                        maxX = Math.max(maxX, coordX);
                        minY = Math.min(minY, coordY);
                        maxY = Math.max(maxY, coordY);
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

        long computationTimeMs = System.currentTimeMillis() - startTime;

        System.out.printf("✓ MST computed: %d edges, %.2f km total, %d ms\n",
                mstConnections.size(), totalMSTDistance, computationTimeMs);

        if (visited.size() < stations.size()) {
            System.out.printf("⚠️  Warning: %d station(s) are isolated or unreachable\n",
                    stations.size() - visited.size());
        }
    }

    /**
     * Generates a GraphViz DOT file for visualization.
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

                double normX = 100 * (s.coordX() - minX) / (maxX - minX);
                double normY = 100 * (s.coordY() - minY) / (maxY - minY);

                writer.printf("    \"%d\" [pos=\"%.2f,%.2f!\", label=\"%s\"];\n",
                        s.idEstacao(), normX, normY, label);
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
     * Attempts to generate SVG visualization using Graphviz.
     *
     * @param dotFile Input DOT filename
     * @param svgFile Output SVG filename
     * @return true if SVG generation succeeded
     */
    public boolean generateSVG(String dotFile, String svgFile) {
        // Tenta gerar SVG com dot
        try {
            ProcessBuilder pb = new ProcessBuilder("dot", "-Tsvg", dotFile, "-o", svgFile);
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
            // Ignora erros
        }

        // Se não conseguiu, não mostra mensagem de erro
        // O utilizador pode usar o plugin do IntelliJ
        return false;
    }

    /**
     * Prints a simple completion message.
     */
    public void printReport() {
        System.out.println("\n" + "=".repeat(60));
        System.out.println("✅ USEI12 COMPLETED SUCCESSFULLY");
        System.out.println("=".repeat(60));
        System.out.println("Output files generated:");
        System.out.println("  • belgian_backbone.dot - DOT file for visualization");
        System.out.println("\nTo visualize the network:");
        System.out.println("  1. Open 'belgian_backbone.dot' in IntelliJ");
        System.out.println("  2. Right-click → Open with → Graphviz");
    }
}
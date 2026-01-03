package pt.ipp.isep.dei.domain;

import java.util.*;

public class UpgradePlanService {

    private final Map<Integer, List<Integer>> adjacencies = new HashMap<>();
    private final Map<Integer, Integer> inDegree = new HashMap<>();

    private List<Integer> lastOrderedList = new ArrayList<>();
    private Map<Integer, Integer> lastRemainingInDegrees = new HashMap<>();

    public void addDependency(int fromId, int toId) {
        adjacencies.putIfAbsent(fromId, new ArrayList<>());

        // Só adiciona se a ligação ainda não existir (evita duplicados no diagrama)
        if (!adjacencies.get(fromId).contains(toId)) {
            adjacencies.get(fromId).add(toId);
            inDegree.put(toId, inDegree.getOrDefault(toId, 0) + 1);
            inDegree.putIfAbsent(fromId, 0);
        }
    }

    /**
     * USEI11 - Computa a ordem topológica ou identifica ciclos.
     */
    public String computeAndFormatUpgradePlan() {
        long startTime = System.nanoTime();

        Queue<Integer> queue = new LinkedList<>();
        List<Integer> resultOrder = new ArrayList<>();
        Map<Integer, Integer> tempInDegree = new HashMap<>(inDegree);

        // 1. Identificar estações de partida (sem dependências)
        for (Map.Entry<Integer, Integer> entry : tempInDegree.entrySet()) {
            if (entry.getValue() == 0) {
                queue.add(entry.getKey());
            }
        }

        // 2. Algoritmo de Kahn
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

        // --- AQUI ESTÁ A CHAVE: Guardar para o gráfico ---
        this.lastOrderedList = new ArrayList<>(resultOrder);
        this.lastRemainingInDegrees = new HashMap<>(tempInDegree);

        long endTime = System.nanoTime();
        double durationMs = (endTime - startTime) / 1_000_000.0;

        return formatFinalOutput(resultOrder, tempInDegree, durationMs);
    }
    /**
     * Gera um ficheiro DOT para visualização das dependências de upgrade.
     */
    /**
     * Gera um ficheiro DOT inteligente com distinção de cores:
     * Verde = Ordem de upgrade válida
     * Vermelho = Bloqueado em Ciclo
     */
    /**
     * Gera um ficheiro DOT onde as estações bloqueadas em ciclos aparecem a VERMELHO.
     */
    public void generateUpgradeDiagram(String filename) {
        try (java.io.PrintWriter writer = new java.io.PrintWriter(filename)) {
            writer.println("digraph BelgiumUpgradePlan {");
            writer.println("    rankdir=LR;"); // Desenha da esquerda para a direita
            writer.println("    node [fontname=\"Arial\", style=filled];");
            writer.println("    edge [color=\"#555555\", penwidth=1.0, arrowsize=0.8];");

            // 1. Juntar TODOS os IDs de estações conhecidas (para não escapar nenhuma)
            Set<Integer> allStations = new HashSet<>();
            allStations.addAll(adjacencies.keySet());
            allStations.addAll(inDegree.keySet());
            allStations.addAll(lastRemainingInDegrees.keySet());

            // 2. Desenhar cada nó com a cor certa baseada no resultado do algoritmo
            for (Integer id : allStations) {
                // Se sobrar dependências (> 0), é porque faz parte de um ciclo -> VERMELHO
                int remaining = lastRemainingInDegrees.getOrDefault(id, 0);

                if (remaining > 0) {
                    // CÓDIGO VERMELHO (Blocked/Cycle)
                    writer.printf("    \"%d\" [fillcolor=\"#ff9999\", color=\"#cc0000\", label=\"ST %d\\n(Ciclo: %d)\", shape=doublecircle];\n",
                            id, id, remaining);
                } else {
                    // CÓDIGO VERDE (OK)
                    writer.printf("    \"%d\" [fillcolor=\"#ccffcc\", color=\"#006600\", label=\"ST %d\", shape=ellipse];\n",
                            id, id);
                }
            }

            // 3. Desenhar as ligações (arestas)
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
    // Permite registar uma estação mesmo que ela ainda não tenha dependências (setas)
    public void registerStation(int stationId) {
        inDegree.putIfAbsent(stationId, 0);
        adjacencies.putIfAbsent(stationId, new ArrayList<>());
    }

    private String formatFinalOutput(List<Integer> order, Map<Integer, Integer> remainingDegrees, double time) {
        StringBuilder sb = new StringBuilder();
        sb.append("\n" + "=".repeat(60) + "\n");
        sb.append("📊 USEI11 - DIRECTED LINE UPGRADE PLAN REPORT\n");
        sb.append("=".repeat(60) + "\n");

        // Caso 1: Grafo sem ciclos (Ordenação completa)
        if (order.size() == inDegree.size()) {
            sb.append("✅ SUCCESS: No cycles detected. Optimal upgrade order found.\n\n");
            sb.append("RANKING DE UPGRADE:\n");
            for (int i = 0; i < order.size(); i++) {
                sb.append(String.format("   %dº -> Station ID: %d\n", i + 1, order.get(i)));
            }
        }
        // Caso 2: Ciclos detetados
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
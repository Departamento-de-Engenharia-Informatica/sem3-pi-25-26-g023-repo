package pt.ipp.isep.dei.domain;

import java.util.*;
import java.util.stream.Collectors;

public class PickingPathService {

    // Usa a fábrica estática para criar a ENTRANCE
    private static final BayLocation ENTRANCE = BayLocation.entrance();

    public static class PathResult {
        public final List<BayLocation> path;
        public final double totalDistance;

        public PathResult(List<BayLocation> path, double totalDistance) {
            // Garante que a lista nunca é nula
            this.path = (path != null) ? new ArrayList<>(path) : new ArrayList<>();
            this.totalDistance = totalDistance;
        }

        @Override
        public String toString() {
            // Verifica se o path está vazio ou contém apenas ENTRANCE antes de formatar
            String pathString;
            if (path == null || path.isEmpty()) {
                pathString = "Path is empty or null";
            } else {
                pathString = path.stream()
                        .filter(Objects::nonNull) // Filtra nulos na lista
                        .map(BayLocation::toString) // Converte para string usando o toString() corrigido
                        .collect(Collectors.joining(" -> "));
            }

            // Formata a distância, tratando NaN e Infinito
            String distString = Double.isNaN(totalDistance) ? "Not Calculated (NaN)" :
                    Double.isInfinite(totalDistance) ? "Infinite/Unreachable" :
                            String.format("%.2f", totalDistance);

            return "Path: " + pathString +
                    "\nTotal Distance: " + distString;
        }
    }


    public Map<String, PathResult> calculatePickingPaths(PickingPlan plan) {
        Map<String, PathResult> results = new HashMap<>();
        // Inicializa com placeholders válidos (path com ENTRANCE, distância 0)
        results.put("Strategy A (Deterministic Sweep)", new PathResult(List.of(ENTRANCE), 0.0));
        results.put("Strategy B (Nearest Neighbour)", new PathResult(List.of(ENTRANCE), 0.0));


        if (plan == null || plan.getTrolleys() == null || plan.getTrolleys().isEmpty()) {
            System.out.println("⚠️ Picking plan está vazio ou nulo.");
            return results; // Retorna placeholders
        }

        // 1. Extrair locais únicos e válidos
        Set<BayLocation> uniqueValidBays = new HashSet<>();

        for (Trolley trolley : plan.getTrolleys()) {
            if (trolley == null || trolley.getAssignments() == null) continue;
            for (PickingAssignment assignment : trolley.getAssignments()) {
                if (assignment == null) continue;

                BayLocation loc = new BayLocation(assignment); // Usa o construtor público que valida

                if (loc.isValid()) {
                    uniqueValidBays.add(loc);
                } else {
                    // O aviso silencioso está em BayLocation.safeParseInt (agora ativado)
                    // Se quiser ver os erros de parsing, descomente as linhas em BayLocation.java
                }
            }
        }

        List<BayLocation> sortedUniqueValidBays = uniqueValidBays.stream()
                .filter(Objects::nonNull) // Segurança extra
                .sorted() // Usa compareTo da BayLocation
                .collect(Collectors.toList());

        if (sortedUniqueValidBays.isEmpty()) {
            System.out.println("ℹ️ Nenhuma bay VÁLIDA para visitar encontrada neste picking plan.");
            // Retorna placeholders indicando que não há rota (Path=[ENTRANCE], Dist=0)
            return results;
        }

        System.out.printf("  ➡️  A calcular rotas para %d localizações únicas válidas.%n", sortedUniqueValidBays.size());

        // 2. Calcular ambas as estratégias
        try {
            results.put("Strategy A (Deterministic Sweep)", calculateStrategyA(new ArrayList<>(sortedUniqueValidBays))); // Passa cópia
        } catch (Exception e) {
            System.err.println("❌ Erro ao calcular Estratégia A: " + e.getMessage());
            e.printStackTrace();
            results.put("Strategy A (Deterministic Sweep)", new PathResult(new ArrayList<>(List.of(ENTRANCE)), Double.NaN)); // Indica erro
        }
        try {
            results.put("Strategy B (Nearest Neighbour)", calculateStrategyB(new ArrayList<>(sortedUniqueValidBays))); // Passa cópia
        } catch (Exception e) {
            System.err.println("❌ Erro ao calcular Estratégia B: " + e.getMessage());
            e.printStackTrace();
            results.put("Strategy B (Nearest Neighbour)", new PathResult(new ArrayList<>(List.of(ENTRANCE)), Double.NaN)); // Indica erro
        }

        return results;
    }

    // Estratégia A: Deterministic Sweep
    private PathResult calculateStrategyA(List<BayLocation> sortedBays) {
        List<BayLocation> path = new ArrayList<>();
        path.add(ENTRANCE); // Começa sempre na entrada
        path.addAll(sortedBays); // Adiciona as bays já ordenadas (aisle, depois bay)

        double totalDistance = calculateTotalDistance(path);
        return new PathResult(path, totalDistance);
    }

    // Estratégia B: Nearest Neighbour
    private PathResult calculateStrategyB(List<BayLocation> bays) {
        List<BayLocation> path = new ArrayList<>();
        path.add(ENTRANCE); // Começa na entrada

        BayLocation currentLocation = ENTRANCE;
        Set<BayLocation> remainingBays = new HashSet<>(bays); // Conjunto de bays válidas a visitar
        int iteration = 0;
        final int MAX_ITERATIONS = bays.size() + 5; // Limite de segurança

        while (!remainingBays.isEmpty() && iteration < MAX_ITERATIONS) {
            iteration++;
            BayLocation nearest = null;
            double minDistance = Double.POSITIVE_INFINITY;

            // Encontra o vizinho mais próximo entre os restantes
            for (BayLocation potentialNext : remainingBays) {
                if (potentialNext == null) continue; // Safety check
                double distance = calculateDistance(currentLocation, potentialNext);

                if (distance < minDistance) {
                    minDistance = distance;
                    nearest = potentialNext;
                }
                // Desempate: Se distâncias iguais, prefere aisle menor, depois bay menor (usando compareTo)
                else if (distance == minDistance && nearest != null && potentialNext.compareTo(nearest) < 0) {
                    nearest = potentialNext; // Update nearest based on compareTo
                }
            }

            // Se encontrou um vizinho válido e alcançável
            if (nearest != null && !Double.isInfinite(minDistance)) {
                path.add(nearest);
                currentLocation = nearest; // Atualiza a localização atual
                remainingBays.remove(nearest); // Remove o escolhido do conjunto
            } else {
                // Se não encontrou (ou distância é infinita), algo está errado
                if (!remainingBays.isEmpty()) {
                    System.err.printf("❌ Erro B: Não encontrou vizinho alcançável na iteração %d a partir de %s entre os %d restantes: %s%n",
                            iteration, currentLocation, remainingBays.size(), remainingBays);
                    return new PathResult(path, Double.POSITIVE_INFINITY); // Indica erro
                }
                // Se remainingBays ficou vazio, terminamos corretamente
                break;
            }
        }

        if (iteration >= MAX_ITERATIONS) {
            System.err.println("❌ Erro B: Atingido limite de iterações.");
            return new PathResult(path, Double.POSITIVE_INFINITY);
        }

        double totalDistance = calculateTotalDistance(path);
        return new PathResult(path, totalDistance);
    }

    // Função de cálculo de distância D
    private double calculateDistance(BayLocation c1, BayLocation c2) {
        if (c1 == null || c2 == null ) return Double.POSITIVE_INFINITY;

        int a1 = c1.getAisle();
        int b1 = c1.getBay();
        int a2 = c2.getAisle();
        int b2 = c2.getBay();

        // Considera (0,0) como válido para cálculo, mas outros negativos são inválidos
        boolean c1ValidForCalc = (a1 >= 0 && b1 >= 0);
        boolean c2ValidForCalc = (a2 >= 0 && b2 >= 0);

        if (!c1ValidForCalc || !c2ValidForCalc) {
            return Double.POSITIVE_INFINITY;
        }

        // Caso especial: De ou Para a entrada (0,0)
        if (a1 == 0 && b1 == 0) { // De ENTRANCE para (a2, b2)
            return Math.abs(a2) * 3.0 + Math.abs(b2); // Usa abs por segurança
        }
        if (a2 == 0 && b2 == 0) { // De (a1, b1) para ENTRANCE
            return Math.abs(b1) + Math.abs(a1) * 3.0; // Usa abs por segurança
        }

        // Caso normal: Entre duas bays válidas
        if (a1 == a2) { // Mesmo aisle
            return Math.abs(b1 - b2);
        } else { // Aisles diferentes
            return b1 + Math.abs(a1 - a2) * 3.0 + b2;
        }
    }

    // Calcula a distância total de um percurso
    private double calculateTotalDistance(List<BayLocation> path) {
        double totalDistance = 0;
        if (path == null || path.size() < 2) return 0; // Se não há percurso ou só 1 ponto, distância é 0

        for (int i = 0; i < path.size() - 1; i++) {
            BayLocation current = path.get(i);
            BayLocation next = path.get(i + 1);

            if (current == null || next == null) {
                System.err.printf("❌ Erro TotalDistance: Ponto nulo no path [%d ou %d]%n", i, i+1);
                return Double.POSITIVE_INFINITY; // Indica erro
            }

            double segmentDistance = calculateDistance(current, next);

            if (Double.isInfinite(segmentDistance) || Double.isNaN(segmentDistance)) {
                System.err.printf("❌ Erro TotalDistance: Segmento inválido (%s -> %s) resultou em distância Inválida/Infinita%n", current, next);
                return Double.POSITIVE_INFINITY; // Propaga o erro
            }
            totalDistance += segmentDistance;
        }
        return totalDistance;
    }
}
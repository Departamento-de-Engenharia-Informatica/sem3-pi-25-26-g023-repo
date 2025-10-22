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
        System.out.println("\n--- DEBUG USEI04: ENTRANDO EM calculatePickingPaths ---"); // Linha de debug

        Map<String, PathResult> results = new HashMap<>();
        // Inicializa com placeholders válidos (path com ENTRANCE, distância 0)
        results.put("Strategy A (Deterministic Sweep)", new PathResult(List.of(ENTRANCE), 0.0));
        results.put("Strategy B (Nearest Neighbour)", new PathResult(List.of(ENTRANCE), 0.0));


        if (plan == null || plan.getTrolleys() == null || plan.getTrolleys().isEmpty()) {
            System.out.println("⚠️ Picking plan está vazio ou nulo.");
            System.out.println("--- DEBUG USEI04: SAINDO (Plano Vazio) ---"); // Linha de debug
            return results; // Retorna placeholders
        }

        // 1. Extrair locais únicos e válidos
        Set<BayLocation> uniqueValidBays = new HashSet<>();
        int assignmentCount = 0;

        System.out.println("  DEBUG: Iniciando extração (loops)..."); // Linha de debug
        for (Trolley trolley : plan.getTrolleys()) {
            if (trolley == null || trolley.getAssignments() == null) continue;
            for (PickingAssignment assignment : trolley.getAssignments()) {
                assignmentCount++;
                if (assignment == null) continue;

                // *** Adicionar linhas de debug aqui ***
                System.out.println("  DEBUG: Lendo Assignment Aisle='" + assignment.getAisle() + "', Bay='" + assignment.getBay() + "' | Order: " + assignment.getOrderId() + " Line: " + assignment.getLineNo());
                BayLocation loc = new BayLocation(assignment); // Usa o construtor público que valida
                System.out.println("  DEBUG: Criado BayLocation: " + loc + ", Válido? " + loc.isValid());
                // *** Fim das linhas de debug adicionadas ***

                if (loc.isValid()) {
                    uniqueValidBays.add(loc);
                } else {
                    // O aviso silencioso está em BayLocation.safeParseInt (agora ativado)
                }
            }
        }
        System.out.println("  DEBUG: Extração concluída."); // Linha de debug
        System.out.printf("  DEBUG: Assignments processados: %d%n", assignmentCount); // Linha de debug
        System.out.printf("  DEBUG: Total ÚNICAS e VÁLIDAS: %d%n", uniqueValidBays.size()); // Linha de debug
        List<BayLocation> sortedUniqueValidBays = uniqueValidBays.stream()
                .filter(Objects::nonNull) // Segurança extra
                .sorted() // Usa compareTo da BayLocation
                .collect(Collectors.toList());
        System.out.println("  DEBUG: Lista FINAL ÚNICAS e VÁLIDAS (Ordenada): " + sortedUniqueValidBays); // Linha de debug

        if (sortedUniqueValidBays.isEmpty()) {
            System.out.println("ℹ️ Nenhuma bay VÁLIDA para visitar encontrada neste picking plan.");
            System.out.println("--- DEBUG USEI04: SAINDO (Nenhuma Bay Válida) ---"); // Linha de debug
            // Retorna placeholders indicando que não há rota (Path=[ENTRANCE], Dist=0)
            return results;
        }

        System.out.printf("  ➡️  A calcular rotas para %d localizações únicas válidas.%n", sortedUniqueValidBays.size());

        // 2. Calcular ambas as estratégias
        try {
            results.put("Strategy A (Deterministic Sweep)", calculateStrategyA(new ArrayList<>(sortedUniqueValidBays))); // Passa cópia da lista ordenada
        } catch (Exception e) {
            System.err.println("❌ Erro ao calcular Estratégia A: " + e.getMessage());
            e.printStackTrace();
            results.put("Strategy A (Deterministic Sweep)", new PathResult(new ArrayList<>(List.of(ENTRANCE)), Double.NaN)); // Indica erro
        }
        try {
            results.put("Strategy B (Nearest Neighbour)", calculateStrategyB(new ArrayList<>(sortedUniqueValidBays))); // Passa cópia da lista ordenada
        } catch (Exception e) {
            System.err.println("❌ Erro ao calcular Estratégia B: " + e.getMessage());
            e.printStackTrace();
            results.put("Strategy B (Nearest Neighbour)", new PathResult(new ArrayList<>(List.of(ENTRANCE)), Double.NaN)); // Indica erro
        }


        System.out.println("--- DEBUG USEI04: SAINDO (Rotas Calculadas) ---"); // Linha de debug
        return results;
    }

    // Estratégia A: Deterministic Sweep
    private PathResult calculateStrategyA(List<BayLocation> sortedBays) {
        System.out.println("  DEBUG (A): Iniciando Estratégia A..."); // Linha de debug
        List<BayLocation> path = new ArrayList<>();
        path.add(ENTRANCE); // Começa sempre na entrada
        path.addAll(sortedBays); // Adiciona as bays já ordenadas (aisle, depois bay)

        double totalDistance = calculateTotalDistance(path);
        System.out.println("  DEBUG (A): Cálculo concluído."); // Linha de debug
        return new PathResult(path, totalDistance);
    }

    // Estratégia B: Nearest Neighbour
    private PathResult calculateStrategyB(List<BayLocation> bays) {
        System.out.println("  DEBUG (B): Iniciando Estratégia B..."); // Linha de debug
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
            System.out.printf("  DEBUG (B) Iter %d: Current: %s, Remaining (%d): %s%n", iteration, currentLocation, remainingBays.size(), remainingBays); // Linha de debug

            // Encontra o vizinho mais próximo entre os restantes
            for (BayLocation potentialNext : remainingBays) {
                if (potentialNext == null) continue; // Safety check
                double distance = calculateDistance(currentLocation, potentialNext);
                System.out.printf("  DEBUG (B) Iter %d: Dist %s -> %s = %.2f%n", iteration, currentLocation, potentialNext, distance); // Linha de debug


                if (distance < minDistance) {
                    minDistance = distance;
                    nearest = potentialNext;
                    System.out.printf("  DEBUG (B) Iter %d: Novo nearest provisório: %s (Dist: %.2f)%n", iteration, nearest, minDistance); // Linha de debug
                }
                // Desempate: Se distâncias iguais, prefere aisle menor, depois bay menor (usando compareTo)
                else if (distance == minDistance && nearest != null && potentialNext.compareTo(nearest) < 0) {
                    System.out.printf("  DEBUG (B) Iter %d: Empate! %s é preferível a %s (Dist: %.2f)%n", iteration, potentialNext, nearest, distance); // Linha de debug
                    nearest = potentialNext; // Update nearest based on compareTo
                }
            }

            // Se encontrou um vizinho válido e alcançável
            if (nearest != null && !Double.isInfinite(minDistance)) {
                System.out.printf("  DEBUG (B) Iter %d: Nearest definitivo escolhido: %s (Dist: %.2f)%n", iteration, nearest, minDistance); // Linha de debug
                path.add(nearest);
                currentLocation = nearest; // Atualiza a localização atual
                remainingBays.remove(nearest); // Remove o escolhido do conjunto
            } else {
                // Se não encontrou (ou distância é infinita), algo está errado
                if (!remainingBays.isEmpty()) {
                    System.err.printf("❌ Erro B: Não encontrou vizinho alcançável na iteração %d a partir de %s entre os %d restantes: %s%n",
                            iteration, currentLocation, remainingBays.size(), remainingBays);
                    // Imprimir distâncias pode ajudar a depurar
                    for(BayLocation rem : remainingBays) {
                        System.err.printf("   Dist %s -> %s = %.2f%n", currentLocation, rem, calculateDistance(currentLocation, rem));
                    }
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
        System.out.println("  DEBUG (B): Cálculo concluído."); // Linha de debug
        return new PathResult(path, totalDistance);
    }

    // Função de cálculo de distância D - CORRIGIDA para tratar (0,0) corretamente
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
            System.err.printf("DEBUG distance: Loc inválida c1(%d,%d) ou c2(%d,%d)%n", a1, b1, a2, b2); // Linha de debug
            return Double.POSITIVE_INFINITY;
        }

        // Caso especial: De ou Para a entrada (0,0)
        if (a1 == 0 && b1 == 0) { // De ENTRANCE para (a2, b2)
            return Math.abs(a2) * 3.0 + Math.abs(b2); // Usa abs por segurança
        }
        if (a2 == 0 && b2 == 0) { // De (a1, b1) para ENTRANCE
            return Math.abs(b1) + Math.abs(a1) * 3.0; // Usa abs por segurança
        }

        // Caso normal: Entre duas bays (a1>0, b1>0, a2>0, b2>0 assumido aqui devido a isValid())
        if (a1 == a2) { // Mesmo aisle
            return Math.abs(b1 - b2);
        } else { // Aisles diferentes
            return b1 + Math.abs(a1 - a2) * 3.0 + b2; // b1 e b2 são > 0 aqui
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
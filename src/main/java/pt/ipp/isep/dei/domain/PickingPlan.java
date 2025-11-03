package pt.ipp.isep.dei.domain;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Represents a picking plan for warehouse order fulfillment.
 * (Versão 2.1 - Corrigido o bug String.format)
 */
public class PickingPlan {

    // --- Códigos de Cores ANSI (Apenas os necessários) ---
    private static final String ANSI_RESET = "\u001B[0m";
    private static final String ANSI_RED = "\u001B[31m"; // Para utilização > 90%
    private static final String ANSI_GREEN = "\u001B[32m";
    private static final String ANSI_YELLOW = "\u001B[33m";
    private static final String ANSI_CYAN = "\u001B[36m";
    private static final String ANSI_BOLD = "\u001B[1m";

    private final String id;
    private final List<Trolley> trolleys;
    private final HeuristicType heuristic;
    private final double trolleyCapacity;

    /**
     * Creates a new picking plan.
     */
    public PickingPlan(String id, HeuristicType heuristic, double trolleyCapacity) {
        this.id = id;
        this.heuristic = heuristic;
        this.trolleyCapacity = trolleyCapacity;
        this.trolleys = new ArrayList<>();
    }

    /** Adds a trolley to the picking plan. */
    public void addTrolley(Trolley trolley) {
        trolleys.add(trolley);
    }

    /** Returns the total number of trolleys in the plan. */
    public int getTotalTrolleys() { return trolleys.size(); }

    /** Returns the total weight of all trolleys. */
    public double getTotalWeight() {
        return trolleys.stream().mapToDouble(Trolley::getCurrentWeight).sum();
    }

    /** Returns the average utilization percentage of all trolleys. */
    public double getAverageUtilization() {
        return trolleys.stream()
                .mapToDouble(Trolley::getUtilization)
                .average()
                .orElse(0.0);
    }

    // -----------------------------------------------------------------
    // --- CORREÇÃO (Linha 75 - removido o %s extra) ---
    // -----------------------------------------------------------------
    /**
     * Returns a "pretty" summary string of the picking plan.
     */
    public String getSummary() {
        // Formata a utilização com cores (Vermelho se > 90%, Amarelo se > 75%, Verde caso contrário)
        double avgUtil = getAverageUtilization();
        String utilColor = (avgUtil > 90) ? ANSI_RED : (avgUtil > 75 ? ANSI_YELLOW : ANSI_GREEN);

        // O %s extra foi removido da última linha
        return String.format(
                "  Plan ID: %s%s%s\n" +
                        "  Heuristic Used: %s%s%s\n" +
                        "  Trolley Capacity: %s%.2f kg%s\n" +
                        "  " + ANSI_BOLD + "--------------------------------------\n" + ANSI_RESET +
                        "  Total Trolleys: %s%d%s\n" +
                        "  Total Weight: %s%.2f kg%s\n" +
                        "  Avg. Utilization: %s%.1f%%%s", // <-- LINHA CORRIGIDA
                ANSI_BOLD, id, ANSI_RESET,
                ANSI_CYAN, heuristic, ANSI_RESET,
                ANSI_YELLOW, trolleyCapacity, ANSI_RESET,
                ANSI_BOLD + ANSI_CYAN, getTotalTrolleys(), ANSI_RESET,
                ANSI_BOLD, getTotalWeight(), ANSI_RESET,
                ANSI_BOLD + utilColor, avgUtil, ANSI_RESET // <-- Argumentos agora correspondem
        );
    }

    // Getters
    public String getId() { return id; }
    public List<Trolley> getTrolleys() { return new ArrayList<>(trolleys); }
    public HeuristicType getHeuristic() { return heuristic; }
    public double getTrolleyCapacity() { return trolleyCapacity; }

    /** Returns string representation of the picking plan. */
    @Override
    public String toString() {
        return getSummary(); // Chama o método "bonito"
    }
}
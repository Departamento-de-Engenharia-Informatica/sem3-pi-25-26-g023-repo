package pt.ipp.isep.dei.domain;

/**
 * Factory para criar instâncias de heurísticas de packing
 * conforme o tipo selecionado pelo utilizador.
 */
public class PackingHeuristicFactory {

    /**
     * Cria uma instância da heurística de packing baseada no tipo
     */
    public static PackingHeuristic createHeuristic(HeuristicType type) {
        return switch (type) {
            case FIRST_FIT -> new FirstFitHeuristic();
            case FIRST_FIT_DECREASING -> new FirstFitDecreasingHeuristic();
            case BEST_FIT_DECREASING -> new BestFitDecreasingHeuristic();
        };
    }

    /**
     * Cria heurística a partir do nome (para flexibilidade)
     */
    public static PackingHeuristic createHeuristic(String heuristicName) {
        try {
            HeuristicType type = HeuristicType.valueOf(heuristicName.toUpperCase());
            return createHeuristic(type);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Heurística desconhecida: " + heuristicName +
                    ". Use: FIRST_FIT, FIRST_FIT_DECREASING, BEST_FIT_DECREASING");
        }
    }

    /**
     * Retorna descrição de todas as heurísticas disponíveis
     */
    public static String getAvailableHeuristics() {
        return """
            Heurísticas disponíveis:
            - FIRST_FIT: Primeiro que cabe
            - FIRST_FIT_DECREASING: Maiores primeiro, depois primeiro que cabe  
            - BEST_FIT_DECREASING: Maiores primeiro, depois melhor encaixe
            """;
    }

    /**
     * Valida se um tipo de heurística é suportado
     */
    public static boolean isValidHeuristic(HeuristicType type) {
        return type != null;
    }

    /**
     * Retorna todos os tipos de heurística disponíveis
     */
    public static HeuristicType[] getAllHeuristicTypes() {
        return HeuristicType.values();
    }
}

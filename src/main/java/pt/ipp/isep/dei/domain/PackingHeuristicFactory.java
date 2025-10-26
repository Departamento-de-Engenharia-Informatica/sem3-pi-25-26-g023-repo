package pt.ipp.isep.dei.domain;

/**
 * Factory for creating packing heuristic instances.
 */
public class PackingHeuristicFactory {

    /**
     * Creates a packing heuristic instance based on the specified type.
     */
    public static PackingHeuristic createHeuristic(HeuristicType type) {
        return switch (type) {
            case FIRST_FIT -> new FirstFitHeuristic();
            case FIRST_FIT_DECREASING -> new FirstFitDecreasingHeuristic();
            case BEST_FIT_DECREASING -> new BestFitDecreasingHeuristic();
        };
    }

}
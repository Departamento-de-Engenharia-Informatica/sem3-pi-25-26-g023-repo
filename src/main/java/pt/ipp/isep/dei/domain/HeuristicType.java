package pt.ipp.isep.dei.domain;

/**
 * Represents the available packing heuristics for trolley assignment.
 *
 * FIRST_FIT - Place items in the first trolley where they fit
 * FIRST_FIT_DECREASING - Sort by weight descending, then use first fit
 * BEST_FIT_DECREASING - Sort by weight descending, then use best fit
 */
public enum HeuristicType {
    /** Place items in first available trolley with capacity */
    FIRST_FIT,

    /** Sort items by weight descending, then use first fit */
    FIRST_FIT_DECREASING,

    /** Sort items by weight descending, then use best fit */
    BEST_FIT_DECREASING
}

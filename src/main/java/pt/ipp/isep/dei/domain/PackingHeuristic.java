package pt.ipp.isep.dei.domain;

import java.util.List;

/**
 * Interface for packing heuristics.
 */
public interface PackingHeuristic {

    /**
     * Packs assignments into trolleys respecting capacity limits.
     */
    List<Trolley> packItems(List<PickingAssignment> assignments, double capacity);
}
package pt.ipp.isep.dei.domain;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Implements the First-Fit Decreasing (FFD) heuristic for packing items
 * into trolleys with a fixed capacity.
 * <p>
 * This algorithm first sorts all {@link PickingAssignment} objects by
 * total weight in descending order and then applies the
 * {@link FirstFitHeuristic} to assign them to trolleys.
 * </p>
 */
public class FirstFitDecreasingHeuristic implements PackingHeuristic {

    /**
     * @param assignments the list of picking assignments to pack
     * @param capacity the maximum capacity of each trolley
     * @return a list of trolleys containing the packed assignments
     */
    @Override
    public List<Trolley> packItems(List<PickingAssignment> assignments, double capacity) {
        // Sort by total weight in descending order
        List<PickingAssignment> sorted = new ArrayList<>(assignments);
        sorted.sort(Comparator.comparingDouble(PickingAssignment::getTotalWeight).reversed());

        // Apply standard First Fit packing
        return new FirstFitHeuristic().packItems(sorted, capacity);
    }
}


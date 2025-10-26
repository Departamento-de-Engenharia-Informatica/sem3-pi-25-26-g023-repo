package pt.ipp.isep.dei.domain;

import java.util.ArrayList;
import java.util.List;

/**
 * Implements the First-Fit heuristic for packing items into trolleys.
 * The algorithm places each {@link PickingAssignment} into the first
 * {@link Trolley} that has enough remaining capacity. If no existing
 * trolley can fit the assignment, a new one is created.
 */
public class FirstFitHeuristic implements PackingHeuristic {

    /**
     * Packs the given picking assignments into trolleys using the
     * First-Fit strategy.
     * @param assignments the list of picking assignments to pack
     * @param capacity    the maximum capacity of each trolley
     * @return a list of trolleys containing the packed assignments
     */
    @Override
    public List<Trolley> packItems(List<PickingAssignment> assignments, double capacity) {
        List<Trolley> trolleys = new ArrayList<>();

        for (PickingAssignment assignment : assignments) {
            boolean added = false;

            // Try to add the assignment to an existing trolley
            for (Trolley trolley : trolleys) {
                if (trolley.addAssignment(assignment)) {
                    added = true;
                    break;
                }
            }

            // If it doesn't fit, create a new trolley
            if (!added) {
                Trolley newTrolley = new Trolley("T" + (trolleys.size() + 1), capacity);
                newTrolley.addAssignment(assignment);
                trolleys.add(newTrolley);
            }
        }

        return trolleys;
    }
}


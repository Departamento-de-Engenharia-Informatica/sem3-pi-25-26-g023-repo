package pt.ipp.isep.dei.domain;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;


public class BestFitDecreasingHeuristic implements PackingHeuristic {

    /**
     * @param assignments the list of picking assignments to pack
     * @param capacity the maximum capacity of each trolley
     * @return a list of trolleys with the packed assignments
     */
    @Override
    public List<Trolley> packItems(List<PickingAssignment> assignments, double capacity) {

        // Sort assignments by weight descending
        List<PickingAssignment> sorted = new ArrayList<>(assignments);
        sorted.sort(Comparator.comparingDouble(PickingAssignment::getTotalWeight).reversed());

        List<Trolley> trolleys = new ArrayList<>();

        for (PickingAssignment assignment : sorted) {
            Trolley bestTrolley = null;
            double minRemaining = Double.MAX_VALUE;

            // Find the trolley that fits best
            for (Trolley trolley : trolleys) {
                double remaining = trolley.getRemainingCapacity() - assignment.getTotalWeight();
                if (remaining >= 0 && remaining < minRemaining) {
                    minRemaining = remaining;
                    bestTrolley = trolley;
                }
            }

            // Add to existing trolley or create a new one
            if (bestTrolley != null) {
                bestTrolley.addAssignment(assignment);
            } else {
                Trolley newTrolley = new Trolley("T" + (trolleys.size() + 1), capacity);
                newTrolley.addAssignment(assignment);
                trolleys.add(newTrolley);
            }
        }

        return trolleys;
    }
}

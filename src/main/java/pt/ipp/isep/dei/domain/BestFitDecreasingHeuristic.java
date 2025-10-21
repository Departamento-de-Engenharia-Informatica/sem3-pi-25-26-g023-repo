package pt.ipp.isep.dei.domain;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class BestFitDecreasingHeuristic implements PackingHeuristic {

    @Override
    public List<Trolley> packItems(List<PickingAssignment> assignments, double capacity) {
        // Ordenar por peso descendente
        List<PickingAssignment> sorted = new ArrayList<>(assignments);
        sorted.sort(Comparator.comparingDouble(PickingAssignment::getTotalWeight).reversed());

        List<Trolley> trolleys = new ArrayList<>();

        for (PickingAssignment assignment : sorted) {
            Trolley bestTrolley = null;
            double minRemaining = Double.MAX_VALUE;

            // Encontrar o trolley com menor espaço restante onde cabe
            for (Trolley trolley : trolleys) {
                double remaining = trolley.getRemainingCapacity() - assignment.getTotalWeight();
                if (remaining >= 0 && remaining < minRemaining) {
                    minRemaining = remaining;
                    bestTrolley = trolley;
                }
            }

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
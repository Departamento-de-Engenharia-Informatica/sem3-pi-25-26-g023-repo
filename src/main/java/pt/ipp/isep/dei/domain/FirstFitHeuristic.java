package pt.ipp.isep.dei.domain;

import java.util.ArrayList;
import java.util.List;

public class FirstFitHeuristic implements PackingHeuristic {

    @Override
    public List<Trolley> packItems(List<PickingAssignment> assignments, double capacity) {
        List<Trolley> trolleys = new ArrayList<>();

        for (PickingAssignment assignment : assignments) {
            boolean added = false;

            // Tentar adicionar a um trolley existente
            for (Trolley trolley : trolleys) {
                if (trolley.addAssignment(assignment)) {
                    added = true;
                    break;
                }
            }

            // Se não couber, criar novo trolley
            if (!added) {
                Trolley newTrolley = new Trolley("T" + (trolleys.size() + 1), capacity);
                newTrolley.addAssignment(assignment);
                trolleys.add(newTrolley);
            }
        }

        return trolleys;
    }
}

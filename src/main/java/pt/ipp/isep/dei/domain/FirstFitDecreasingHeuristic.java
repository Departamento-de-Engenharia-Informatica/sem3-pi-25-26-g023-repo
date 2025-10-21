package pt.ipp.isep.dei.domain;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class FirstFitDecreasingHeuristic implements PackingHeuristic {

    @Override
    public List<Trolley> packItems(List<PickingAssignment> assignments, double capacity) {
        // Ordenar por peso descendente
        List<PickingAssignment> sorted = new ArrayList<>(assignments);
        sorted.sort(Comparator.comparingDouble(PickingAssignment::getTotalWeight).reversed());

        // Aplicar First Fit normal
        return new FirstFitHeuristic().packItems(sorted, capacity);
    }
}

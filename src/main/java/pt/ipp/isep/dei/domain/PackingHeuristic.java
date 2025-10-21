package pt.ipp.isep.dei.domain;

import java.util.List;

/**
 * Interface para todas as heurísticas de packing
 */
public interface PackingHeuristic {

    /**
     * Agrupa assignments em trolleys respeitando a capacidade
     */
    List<Trolley> packItems(List<PickingAssignment> assignments, double capacity);
}
package pt.ipp.isep.dei.domain;

import java.util.Collections;
import java.util.List;

/**
 * Representa um caminho completo na rede ferroviária, composto por múltiplos segmentos.
 */
public class RailwayPath {
    private final List<SegmentoLinha> segments;
    private final double totalDistance;
    private final double totalTimeHours;

    public RailwayPath(List<SegmentoLinha> segments, double totalDistance, double totalTimeHours) {
        this.segments = segments;
        this.totalDistance = totalDistance;
        this.totalTimeHours = totalTimeHours;
    }

    public List<SegmentoLinha> getSegments() {
        return Collections.unmodifiableList(segments);
    }

    public double getTotalDistance() {
        return totalDistance;
    }

    public double getTotalTimeHours() {
        return totalTimeHours;
    }

    public long getTotalTimeMinutes() {
        return Math.round(totalTimeHours * 60);
    }

    public boolean isEmpty() {
        return segments.isEmpty();
    }
}
package pt.ipp.isep.dei.domain;

import java.util.Collections;
import java.util.List;

/**
 * Represents a complete path in the railway network.
 * Contains the sequence of line segments, total distance and travel time.
 */
public class RailwayPath {
    private final List<LineSegment> segments;
    private final double totalDistance;
    private final double totalTimeHours;

    /**
     * Creates a new railway path.
     * @param segments the list of line segments in the path
     * @param totalDistance the total distance in kilometers
     * @param totalTimeHours the total travel time in hours
     */
    public RailwayPath(List<LineSegment> segments, double totalDistance, double totalTimeHours) {
        this.segments = segments;
        this.totalDistance = totalDistance;
        this.totalTimeHours = totalTimeHours;
    }

    /**
     * @return unmodifiable list of segments in this path
     */
    public List<LineSegment> getSegments() {

        return Collections.unmodifiableList(segments);
    }

    /**
     * @return total distance of the path in kilometers
     */
    public double getTotalDistance() {

        return totalDistance;
    }

    /**
     * @return total travel time in hours
     */
    public double getTotalTimeHours() {
        return totalTimeHours;
    }

    /**
     * @return total travel time converted to minutes
     */
    public long getTotalTimeMinutes() {

        return Math.round(totalTimeHours * 60);
    }

    /**
     * @return true if this path contains no segments
     */
    public boolean isEmpty() {

        return segments.isEmpty();
    }

    /**
     * @return string representation of the path
     */
    @Override
    public String toString() {
        return String.format("RailwayPath[%d segments, %.1f km, %.1f hours]",
                segments.size(), totalDistance, totalTimeHours);
    }
}
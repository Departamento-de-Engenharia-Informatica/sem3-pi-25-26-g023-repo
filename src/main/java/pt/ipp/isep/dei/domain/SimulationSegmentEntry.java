// File: pt.ipp.isep.dei.domain.SimulationSegmentEntry.java
package pt.ipp.isep.dei.domain;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.Duration;

/**
 * DTO that stores the result of a train passing through a segment,
 * including entry and exit times and the calculated speeds.
 *
 * <p>This information is vital for conflict detection and resolution in the scheduler service.</p>
 */
public class SimulationSegmentEntry {
    private final String trainId;
    private final LineSegment segment;
    private final LocalDateTime entryTime;
    private final LocalDateTime exitTime;
    private final double maxSpeedAllowedKmh; // Vseg (segment speed limit)
    private final double calculatedSpeedKmh; // Vcalc (min(Vseg, Vmax_train))
    private final String startFacilityName;
    private final String endFacilityName;

    // Constant for time formatting (HH:mm)
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");

    /**
     * Constructs a SimulationSegmentEntry DTO.
     */
    public SimulationSegmentEntry(String trainId, LineSegment segment, LocalDateTime entryTime, LocalDateTime exitTime,
                                  double maxSpeedAllowedKmh, double calculatedSpeedKmh,
                                  String startFacilityName, String endFacilityName) {
        this.trainId = trainId;
        this.segment = segment;
        this.entryTime = entryTime;
        this.exitTime = exitTime;
        this.maxSpeedAllowedKmh = maxSpeedAllowedKmh;
        this.calculatedSpeedKmh = calculatedSpeedKmh;
        this.startFacilityName = startFacilityName;
        this.endFacilityName = endFacilityName;
    }

    // Getters necessary for conflict logic and output
    /** Returns the ID of the train. */
    public String getTrainId() { return trainId; }
    /** Returns the {@link LineSegment} object. */
    public LineSegment getSegment() { return segment; }
    /** Returns the time the train enters the segment. */
    public LocalDateTime getEntryTime() { return entryTime; }
    /** Returns the time the train exits the segment. */
    public LocalDateTime getExitTime() { return exitTime; }
    /** Returns the effective calculated speed (min of train Vmax and segment Vmax). */
    public double getCalculatedSpeedKmh() { return calculatedSpeedKmh; }
    /** Returns the name of the starting facility/station. */
    public String getStartFacilityName() { return startFacilityName; }
    /** Returns the name of the ending facility/station. */
    public String getEndFacilityName() { return endFacilityName; }
    /** Returns the segment ID from the LineSegment. */
    public String getSegmentId() { return segment.getSegmentId(); }


    /**
     * Formats the segment passage data for table output.
     * NEW FORMAT: Segment | From Facility | To Facility | Type | Length (km) | Entry | Exit | Speed (Calc/Allowed)
     *
     * @return A formatted string suitable for display in a console table.
     */
    public String toTableString() {
        String type = segment.getNumberTracks() > 1 ? "Double" : "Single";
        double lengthKm = segment.getComprimento(); // Assuming getComprimento returns length in km.

        return String.format("%-7s\t%-18s\t%-18s\t%-6s\t%7.1f km\t%8s\t%8s\t%10.0f/%-3.0f",
                segment.getSegmentId(),
                startFacilityName.substring(0, Math.min(startFacilityName.length(), 16)), // Truncate if necessary
                endFacilityName.substring(0, Math.min(endFacilityName.length(), 16)),     // Truncate if necessary
                type,
                lengthKm,
                entryTime.toLocalTime().format(TIME_FORMATTER),
                exitTime.toLocalTime().format(TIME_FORMATTER),
                calculatedSpeedKmh,
                maxSpeedAllowedKmh);
    }
}
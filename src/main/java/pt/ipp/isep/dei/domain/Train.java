package pt.ipp.isep.dei.domain;

import java.time.LocalDateTime;

/**
 * Represents a scheduled train trip instance in the TRAIN table.
 *
 * <p>This class holds the static parameters defining a train trip,
 * including its identifier, operator, times, facilities, and assigned resources.</p>
 */
public class Train {
    private final String trainId;
    private final String operatorId;
    private final LocalDateTime departureTime;
    private final int startFacilityId; // ID of the Starting Facility
    private final int endFacilityId;   // ID of the Destination Facility
    private final String locomotiveId; // ID of the primary locomotive
    private final String routeId;      // ID of the Planned Route

    /**
     * Constructs a new Train object.
     */
    public Train(String trainId, String operatorId, LocalDateTime departureTime, int startFacilityId, int endFacilityId, String locomotiveId, String routeId) {
        this.trainId = trainId;
        this.operatorId = operatorId;
        this.departureTime = departureTime;
        this.startFacilityId = startFacilityId;
        this.endFacilityId = endFacilityId;
        this.locomotiveId = locomotiveId;
        this.routeId = routeId;
    }

    /** Returns the unique identifier of the train trip. */
    public String getTrainId() { return trainId; }
    /** Returns the scheduled departure time. */
    public LocalDateTime getDepartureTime() { return departureTime; }
    /** Returns the ID of the starting facility. */
    public int getStartFacilityId() { return startFacilityId; }
    /** Returns the ID of the destination facility. */
    public int getEndFacilityId() { return endFacilityId; }
    /** Returns the ID of the primary locomotive. */
    public String getLocomotiveId() { return locomotiveId; }
    /** Returns the ID of the planned route. */
    public String getRouteId() { return routeId; } // NEW GETTER
    // IN pt.ipp.isep.dei.domain.Train.java
    /** Returns the ID of the operating company. */
    public String getOperatorId() {
        return operatorId;
    }
}
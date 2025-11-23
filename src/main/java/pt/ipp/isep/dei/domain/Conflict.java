package pt.ipp.isep.dei.domain;

import java.time.LocalDateTime;

/**
 * Represents a resolved conflict in the railway scheduling system (USLP07).
 * This immutable record stores details about two trips that would have occupied
 * the same single-track segment simultaneously and documents the resolution applied,
 * specifically the logistic delay imposed on the lower-priority trip (tripId2).
 */
public class Conflict {

    /**
     * The unique identifier of the high-priority train trip (The trip that proceeds).
     */
    public final String tripId1;

    /**
     * The unique identifier of the low-priority train trip (The trip that was delayed).
     */
    public final String tripId2;

    /**
     * The facility ID (Station ID) where the low-priority trip (tripId2) was instructed to wait.
     */
    private final int safeWaitFacilityId;

    /**
     * The specific time when the two trips were scheduled to meet/conflict before resolution.
     */
    public final LocalDateTime scheduledMeetTime;

    /**
     * The amount of delay (in minutes) applied to the low-priority trip (tripId2)
     * to resolve the conflict.
     */
    public final long delayMinutes;

    /**
     * A brief description of the segment, tracks, and nature of the conflict resolution.
     */
    public final String description;

    /**
     * Constructs a new Conflict record documenting a scheduling resolution.
     *
     * @param tripId1 The ID of the prioritized trip.
     * @param tripId2 The ID of the delayed trip.
     * @param safeWaitFacilityId The Facility ID where tripId2 was scheduled to wait.
     * @param scheduledMeetTime The originally calculated time of conflict/meeting.
     * @param delayMinutes The duration of the delay imposed on tripId2.
     * @param description The textual description of the resolution.
     */
    public Conflict(String tripId1, String tripId2, int safeWaitFacilityId, LocalDateTime scheduledMeetTime, long delayMinutes, String description) {
        this.tripId1 = tripId1;
        this.tripId2 = tripId2;
        this.safeWaitFacilityId = safeWaitFacilityId;
        this.scheduledMeetTime = scheduledMeetTime;
        this.delayMinutes = delayMinutes;
        this.description = description;
    }

    /**
     * Gets the Facility ID where the delayed trip was instructed to wait.
     *
     * @return The ID of the waiting facility/station.
     */
    public int getSafeWaitFacilityId() {
        return safeWaitFacilityId;
    }

    /**
     * Provides a formatted string representation of the resolved conflict, suitable for logging.
     *
     * @return A detailed string describing the conflict resolution.
     */
    @Override
    public String toString() {
        return String.format("Trip ID %s delayed (Waiting at Facility ID %d, Safe Entry Time: %s). Delay applied: %d min. Resolution: %s",
                tripId2, safeWaitFacilityId, scheduledMeetTime.toLocalTime(), delayMinutes, description);
    }
}
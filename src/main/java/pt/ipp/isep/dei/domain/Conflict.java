package pt.ipp.isep.dei.domain;

import java.time.LocalDateTime;

/** Representa um conflito de cruzamento resolvido. */
public class Conflict {
    public final String tripId1;
    public final String tripId2;
    public final int locationStationId;
    public final LocalDateTime scheduledMeetTime;
    public final long delayMinutes;
    public final String resolution;

    public Conflict(String tripId1, String tripId2, int locationStationId, LocalDateTime scheduledMeetTime, long delayMinutes, String resolution) {
        this.tripId1 = tripId1;
        this.tripId2 = tripId2;
        this.locationStationId = locationStationId;
        this.scheduledMeetTime = scheduledMeetTime;
        this.delayMinutes = delayMinutes;
        this.resolution = resolution;
    }

    @Override
    public String toString() {
        return String.format(
                "%sTrip ID %s held at Station %d (Meet Time: %s). Delay imposed: %d min. Resolution: %s%s",
                "\u001B[31m", // ANSI_RED
                tripId2.equals(tripId1) ? tripId1 : tripId2, // Simplificando qual viagem foi atrasada
                locationStationId,
                scheduledMeetTime.toLocalTime(),
                delayMinutes,
                resolution,
                "\u001B[0m" // ANSI_RESET
        );
    }
}
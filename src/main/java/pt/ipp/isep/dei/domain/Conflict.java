// File: pt.ipp.isep.dei.domain.Conflict.java (Exemplo)
package pt.ipp.isep.dei.domain;

import java.time.LocalDateTime;

public class Conflict {

    public final String tripId1; // Trip Prioritário
    public final String tripId2; // Trip Atrasado
    private final int safeWaitFacilityId; // <--- CAMPO PRIVADO/FINAL
    public final LocalDateTime scheduledMeetTime;
    public final long delayMinutes;
    public final String description;

    public Conflict(String tripId1, String tripId2, int safeWaitFacilityId, LocalDateTime scheduledMeetTime, long delayMinutes, String description) {
        this.tripId1 = tripId1;
        this.tripId2 = tripId2;
        this.safeWaitFacilityId = safeWaitFacilityId;
        this.scheduledMeetTime = scheduledMeetTime;
        this.delayMinutes = delayMinutes;
        this.description = description;
    }

    // --- GETTER NECESSÁRIO ---
    public int getSafeWaitFacilityId() {
        return safeWaitFacilityId;
    }

    // ... (Método toString() que o SchedulerController usa para o log) ...
    @Override
    public String toString() {
        return String.format("Trip ID %s delayed (Waiting at Station ID %d, Safe Entry: %s). Delay applied: %d min. Resolution: %s",
                tripId2, safeWaitFacilityId, scheduledMeetTime.toLocalTime(), delayMinutes, description);
    }
}
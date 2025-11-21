package pt.ipp.isep.dei.domain;

import java.util.ArrayList;
import java.util.List;

public class SchedulerResult {
    public final List<TrainTrip> scheduledTrips = new ArrayList<>();
    public final List<Conflict> resolvedConflicts = new ArrayList<>();

    public void addTrip(TrainTrip trip) { scheduledTrips.add(trip); }
    public void addConflict(Conflict conflict) { resolvedConflicts.add(conflict); }
}
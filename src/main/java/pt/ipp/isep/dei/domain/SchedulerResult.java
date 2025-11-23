package pt.ipp.isep.dei.domain;

import java.util.ArrayList;
import java.util.List;

/**
 * Data container class that holds the results produced by the scheduling algorithm.
 *
 * <p>It stores a list of successfully scheduled {@link TrainTrip} objects and
 * a list of {@link Conflict} objects that were identified and resolved during the process.</p>
 */
public class SchedulerResult {
    // List of train trips that were successfully scheduled.
    public final List<TrainTrip> scheduledTrips = new ArrayList<>();
    // List of conflicts (e.g., resource or time clashes) that were resolved.
    public final List<Conflict> resolvedConflicts = new ArrayList<>();

    /**
     * Adds a successfully scheduled train trip to the results.
     * @param trip The {@link TrainTrip} to add.
     */
    public void addTrip(TrainTrip trip) { scheduledTrips.add(trip); }

    /**
     * Adds a resolved conflict to the results.
     * @param conflict The {@link Conflict} to add.
     */
    public void addConflict(Conflict conflict) { resolvedConflicts.add(conflict); }
}
package pt.ipp.isep.dei.domain;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Represents an instance of a train trip to be scheduled.
 *
 * <p>It holds the static trip parameters (route, rolling stock) and stores the
 * dynamic results calculated by the scheduler (speeds, times, and segment-by-segment simulation data).</p>
 */
public class TrainTrip {
    private final String tripId;
    private final LocalDateTime departureTime;
    private final List<LineSegment> route;
    private final List<Locomotive> locomotives;
    private final List<Wagon> wagons;

    // Scheduler Results
    private double totalWeightKg = 0;
    private double combinedPowerKw = 0;
    private double maxTrainSpeed = 0.0; // <--- NEW: Calculated Maximum Train Speed (based on power/weight)
    private double totalTravelTimeHours = 0;
    private final Map<Integer, LocalDateTime> passageTimes = new HashMap<>(); // Station ID -> Passage Time
    private final List<SimulationSegmentEntry> segmentEntries; // <--- NEW: List of detailed simulation results

    /**
     * Constructs a new TrainTrip instance.
     */
    public TrainTrip(String tripId, LocalDateTime departureTime, List<LineSegment> route, List<Locomotive> locomotives, List<Wagon> wagons) {
        this.tripId = tripId;
        this.departureTime = departureTime;
        this.route = route != null ? route : Collections.emptyList();
        this.locomotives = locomotives != null ? locomotives : Collections.emptyList();
        this.wagons = wagons != null ? wagons : Collections.emptyList();
        this.segmentEntries = new ArrayList<>(); // <--- INITIALIZATION
    }

    // Getters
    /** Returns the unique identifier of the trip. */
    public String getTripId() { return tripId; }
    /** Returns the scheduled departure time. */
    public LocalDateTime getDepartureTime() { return departureTime; }
    /** Returns the list of segments that define the route. */
    public List<LineSegment> getRoute() { return route; }
    /** Returns the list of wagons attached to the trip. */
    public List<Wagon> getWagons() { return wagons; }
    /** Returns the list of locomotives used for traction. */
    public List<Locomotive> getLocomotives() { return locomotives; }

    // Setters/Getters for Results
    /** Sets the passage time at a specific station ID. */
    public void setPassageTime(int stationId, LocalDateTime time) { passageTimes.put(stationId, time); }
    /** Returns the map of passage times at key facilities (stations/facilities). */
    public Map<Integer, LocalDateTime> getPassageTimes() { return passageTimes; }
    /** Sets the total simulated travel time in hours. */
    public void setTotalTravelTimeHours(double totalTravelTimeHours) { this.totalTravelTimeHours = totalTravelTimeHours; }
    /** Returns the total simulated travel time in hours. */
    public double getTotalTravelTimeHours() { return totalTravelTimeHours; }

    /** Returns the detailed simulation results segment by segment. */
    public List<SimulationSegmentEntry> getSegmentEntries() { return segmentEntries; } // <--- NEW GETTER
    /** Adds a segment simulation result entry. */
    public void addSegmentEntry(SimulationSegmentEntry entry) { this.segmentEntries.add(entry); } // <--- NEW METHOD

    // Calculated properties
    /** Sets the total calculated weight of the train (rolling stock + load) in kg. */
    public void setTotalWeightKg(double totalWeightKg) { this.totalWeightKg = totalWeightKg; }
    /** Returns the total calculated weight of the train in kg. */
    public double getTotalWeightKg() { return totalWeightKg; }
    /** Sets the combined power of all locomotives in kW. */
    public void setCombinedPowerKw(double combinedPowerKw) { this.combinedPowerKw = combinedPowerKw; }
    /** Returns the combined power of all locomotives in kW. */
    public double getCombinedPowerKw() { return combinedPowerKw; }

    /** Returns the maximum speed the train can reach based on its power-to-weight ratio. */
    public double getMaxTrainSpeed() { return maxTrainSpeed; } // <--- NEW GETTER
    /** Sets the maximum speed the train can reach based on its power-to-weight ratio. */
    public void setMaxTrainSpeed(double maxTrainSpeed) { this.maxTrainSpeed = maxTrainSpeed; } // <--- NEW SETTER
}
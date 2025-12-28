package pt.ipp.isep.dei.domain;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class TrainTrip {
    private String tripId;
    private LocalDateTime departureTime;
    private LocalDateTime arrivalTime;
    private List<LineSegment> route;
    private List<Locomotive> locomotives;
    private List<Wagon> wagons;
    private List<SimulationSegmentEntry> segmentEntries;

    // Performance Metrics
    private double totalTravelTimeHours;
    private double maxTrainSpeed; // Calculated based on physics
    private double combinedPowerKw;
    private double totalWeightKg;

    // --- NOVO CAMPO: Log de Cálculo Físico ---
    private String physicsCalculationLog = "";
    // -----------------------------------------

    public TrainTrip(String tripId, LocalDateTime departureTime, List<LineSegment> route,
                     List<Locomotive> locomotives, List<Wagon> wagons) {
        this.tripId = tripId;
        this.departureTime = departureTime;
        this.route = route;
        this.locomotives = locomotives;
        this.wagons = wagons;
        this.segmentEntries = new ArrayList<>();
    }

    public String getTripId() { return tripId; }
    public LocalDateTime getDepartureTime() { return departureTime; }
    public void setDepartureTime(LocalDateTime departureTime) { this.departureTime = departureTime; }

    public List<LineSegment> getRoute() { return route; }
    public List<Locomotive> getLocomotives() { return locomotives; }
    public List<Wagon> getWagons() { return wagons; }

    public List<SimulationSegmentEntry> getSegmentEntries() { return segmentEntries; }

    public void addSegmentEntry(SimulationSegmentEntry entry) {
        this.segmentEntries.add(entry);
    }

    public double getTotalTravelTimeHours() { return totalTravelTimeHours; }
    public void setTotalTravelTimeHours(double totalTravelTimeHours) { this.totalTravelTimeHours = totalTravelTimeHours; }

    public double getMaxTrainSpeed() { return maxTrainSpeed; }
    public void setMaxTrainSpeed(double maxTrainSpeed) { this.maxTrainSpeed = maxTrainSpeed; }

    public double getCombinedPowerKw() { return combinedPowerKw; }
    public void setCombinedPowerKw(double combinedPowerKw) { this.combinedPowerKw = combinedPowerKw; }

    public double getTotalWeightKg() { return totalWeightKg; }
    public void setTotalWeightKg(double totalWeightKg) { this.totalWeightKg = totalWeightKg; }

    // --- GETTER E SETTER PARA O LOG ---
    public String getPhysicsCalculationLog() {
        return physicsCalculationLog;
    }

    public void setPhysicsCalculationLog(String physicsCalculationLog) {
        this.physicsCalculationLog = physicsCalculationLog;
    }
    // ----------------------------------

    // Helper to set passage time on a station in the route (simplified)
    public void setPassageTime(int stationId, LocalDateTime time) {
        // This could be used to update internal state if needed
    }
}
// File: pt.ipp.isep.dei.domain.TrainTrip.java
package pt.ipp.isep.dei.domain;

import java.time.LocalDateTime;
import java.util.ArrayList; // <--- NOVO IMPORT
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/** Representa uma instância de uma viagem de comboio a ser escalonada. */
public class TrainTrip {
    private final String tripId;
    private final LocalDateTime departureTime;
    private final List<LineSegment> route;
    private final List<Locomotive> locomotives;
    private final List<Wagon> wagons;

    // Resultados do Scheduler
    private double totalWeightKg = 0;
    private double combinedPowerKw = 0;
    private double maxTrainSpeed = 0.0; // <--- NOVO: Velocidade Máxima Calculada do Comboio
    private double totalTravelTimeHours = 0;
    private final Map<Integer, LocalDateTime> passageTimes = new HashMap<>(); // Estação ID -> Hora de passagem
    private final List<SimulationSegmentEntry> segmentEntries; // <--- NOVO: Lista de resultados detalhados

    public TrainTrip(String tripId, LocalDateTime departureTime, List<LineSegment> route, List<Locomotive> locomotives, List<Wagon> wagons) {
        this.tripId = tripId;
        this.departureTime = departureTime;
        this.route = route != null ? route : Collections.emptyList();
        this.locomotives = locomotives != null ? locomotives : Collections.emptyList();
        this.wagons = wagons != null ? wagons : Collections.emptyList();
        this.segmentEntries = new ArrayList<>(); // <--- INICIALIZAÇÃO
    }

    // Getters
    public String getTripId() { return tripId; }
    public LocalDateTime getDepartureTime() { return departureTime; }
    public List<LineSegment> getRoute() { return route; }
    public List<Wagon> getWagons() { return wagons; }
    public List<Locomotive> getLocomotives() { return locomotives; }

    // Setters/Getters para Resultados
    public void setPassageTime(int stationId, LocalDateTime time) { passageTimes.put(stationId, time); }
    public Map<Integer, LocalDateTime> getPassageTimes() { return passageTimes; }
    public void setTotalTravelTimeHours(double totalTravelTimeHours) { this.totalTravelTimeHours = totalTravelTimeHours; }
    public double getTotalTravelTimeHours() { return totalTravelTimeHours; }

    public List<SimulationSegmentEntry> getSegmentEntries() { return segmentEntries; } // <--- NOVO GETTER
    public void addSegmentEntry(SimulationSegmentEntry entry) { this.segmentEntries.add(entry); } // <--- NOVO MÉTODO

    // Propriedades calculadas
    public void setTotalWeightKg(double totalWeightKg) { this.totalWeightKg = totalWeightKg; }
    public double getTotalWeightKg() { return totalWeightKg; }
    public void setCombinedPowerKw(double combinedPowerKw) { this.combinedPowerKw = combinedPowerKw; }
    public double getCombinedPowerKw() { return combinedPowerKw; }

    public double getMaxTrainSpeed() { return maxTrainSpeed; } // <--- NOVO GETTER
    public void setMaxTrainSpeed(double maxTrainSpeed) { this.maxTrainSpeed = maxTrainSpeed; } // <--- NOVO SETTER
}
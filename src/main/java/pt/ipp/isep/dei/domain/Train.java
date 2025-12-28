package pt.ipp.isep.dei.domain;

import java.time.LocalDateTime;
import java.util.List; // Importar List

public class Train {
    // ... (Atributos existentes mantêm-se) ...
    private final String trainId;
    private final String operatorId;
    private final LocalDateTime departureTime;
    private final int startFacilityId;
    private final int endFacilityId;
    private final String locomotiveId;
    private final String routeId;

    // --- NOVO CAMPO ---
    private List<Wagon> wagons;
    // ------------------

    public Train(String trainId, String operatorId, LocalDateTime departureTime, int startFacilityId, int endFacilityId, String locomotiveId, String routeId) {
        this.trainId = trainId;
        this.operatorId = operatorId;
        this.departureTime = departureTime;
        this.startFacilityId = startFacilityId;
        this.endFacilityId = endFacilityId;
        this.locomotiveId = locomotiveId;
        this.routeId = routeId;
    }

    // ... (Getters existentes mantêm-se) ...
    public String getTrainId() { return trainId; }
    public LocalDateTime getDepartureTime() { return departureTime; }
    public int getStartFacilityId() { return startFacilityId; }
    public int getEndFacilityId() { return endFacilityId; }
    public String getLocomotiveId() { return locomotiveId; }
    public String getRouteId() { return routeId; }
    public String getOperatorId() { return operatorId; }

    // --- NOVOS MÉTODOS ---
    public List<Wagon> getWagons() { return wagons; }
    public void setWagons(List<Wagon> wagons) { this.wagons = wagons; }
    // ---------------------
}
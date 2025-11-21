// File: pt.ipp.isep.dei.domain.Train.java
package pt.ipp.isep.dei.domain;

import java.time.LocalDateTime;

/** Representa uma viagem de comboio agendada na tabela TRAIN. */
public class Train {
    private final String trainId;
    private final String operatorId;
    private final LocalDateTime departureTime;
    private final int startFacilityId; // ID da Facility de Partida
    private final int endFacilityId;   // ID da Facility de Chegada
    private final String locomotiveId; // ID da locomotiva principal
    private final String routeId;      // ID da Rota Planeada

    public Train(String trainId, String operatorId, LocalDateTime departureTime, int startFacilityId, int endFacilityId, String locomotiveId, String routeId) {
        this.trainId = trainId;
        this.operatorId = operatorId;
        this.departureTime = departureTime;
        this.startFacilityId = startFacilityId;
        this.endFacilityId = endFacilityId;
        this.locomotiveId = locomotiveId;
        this.routeId = routeId;
    }

    public String getTrainId() { return trainId; }
    public LocalDateTime getDepartureTime() { return departureTime; }
    public int getStartFacilityId() { return startFacilityId; }
    public int getEndFacilityId() { return endFacilityId; }
    public String getLocomotiveId() { return locomotiveId; }
    public String getRouteId() { return routeId; } // NOVO GETTER
}
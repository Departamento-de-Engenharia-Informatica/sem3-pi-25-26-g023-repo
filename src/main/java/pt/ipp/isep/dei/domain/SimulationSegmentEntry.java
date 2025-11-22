// File: pt.ipp.isep.dei.domain.SimulationSegmentEntry.java
package pt.ipp.isep.dei.domain;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.Duration;

/**
 * DTO que armazena o resultado da passagem de um comboio num segmento,
 * incluindo os tempos de entrada e saída e as velocidades calculadas.
 */
public class SimulationSegmentEntry {
    private final String trainId;
    private final LineSegment segment;
    private final LocalDateTime entryTime;
    private final LocalDateTime exitTime;
    private final double maxSpeedAllowedKmh; // Vseg (do segmento)
    private final double calculatedSpeedKmh; // Vcalc (min(Vseg, Vmax_train))
    private final String startFacilityName;
    private final String endFacilityName;

    // Constante para formatação de tempo (HH:mm)
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");

    public SimulationSegmentEntry(String trainId, LineSegment segment, LocalDateTime entryTime, LocalDateTime exitTime,
                                  double maxSpeedAllowedKmh, double calculatedSpeedKmh,
                                  String startFacilityName, String endFacilityName) {
        this.trainId = trainId;
        this.segment = segment;
        this.entryTime = entryTime;
        this.exitTime = exitTime;
        this.maxSpeedAllowedKmh = maxSpeedAllowedKmh;
        this.calculatedSpeedKmh = calculatedSpeedKmh;
        this.startFacilityName = startFacilityName;
        this.endFacilityName = endFacilityName;
    }

    // Getters necessários para a lógica de conflitos e output
    public String getTrainId() { return trainId; }
    public LineSegment getSegment() { return segment; }
    public LocalDateTime getEntryTime() { return entryTime; }
    public LocalDateTime getExitTime() { return exitTime; }
    public double getCalculatedSpeedKmh() { return calculatedSpeedKmh; }
    public String getStartFacilityName() { return startFacilityName; }
    public String getEndFacilityName() { return endFacilityName; }
    public String getSegmentId() { return segment.getSegmentId(); }


    /**
     * Formata os dados da passagem de um segmento para a tabela de output.
     * NOVO FORMATO: Segment | From Facility | To Facility | Type | Length (km) | Entry | Exit | Speed (C/A)
     */
    public String toTableString() {
        String type = segment.getNumberTracks() > 1 ? "Double" : "Single";
        double lengthKm = segment.getLengthM();

        return String.format("%-7s\t%-18s\t%-18s\t%-6s\t%7.1f km\t%8s\t%8s\t%10.0f/%-3.0f",
                segment.getSegmentId(),
                startFacilityName.substring(0, Math.min(startFacilityName.length(), 16)), // Trunca se necessário
                endFacilityName.substring(0, Math.min(endFacilityName.length(), 16)),     // Trunca se necessário
                type,
                lengthKm,
                entryTime.toLocalTime().format(TIME_FORMATTER),
                exitTime.toLocalTime().format(TIME_FORMATTER),
                calculatedSpeedKmh,
                maxSpeedAllowedKmh);
    }
}
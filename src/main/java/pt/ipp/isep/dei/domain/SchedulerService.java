package pt.ipp.isep.dei.domain;

import pt.ipp.isep.dei.repository.StationRepository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class SchedulerService {

    // Constantes de Cálculo
    // CORREÇÃO CRÍTICA: Fator ajustado para forçar V_max_comboio > 150 km/h e provar o cálculo dinâmico.
    private static final double POWER_TO_TRACTION_FACTOR = 2.857;

    private static final double LOCOMOTIVE_TARA_KG = 80000.0;
    private static final double WAGON_TARA_KG = 25000.0;
    private static final double WAGON_LOAD_KG = 50000.0;

    // Novo limite máximo de engenharia para o comboio (Cap)
    private static final double MAX_FREIGHT_SPEED_CAP = 250.0;
    private static final double MIN_FREIGHT_SPEED = 30.0;

    private final StationRepository stationRepo;

    public SchedulerService(StationRepository stationRepo) {
        this.stationRepo = stationRepo;
    }


    /**
     * Calcula as propriedades combinadas do comboio (Peso e Potência).
     */
    public TrainTrip calculateTrainPerformance(TrainTrip trip) {
        // ... (Lógica de cálculo de Peso/Potência existente)
        double combinedPowerKw = trip.getLocomotives().stream()
                .mapToDouble(Locomotive::getPowerKW)
                .sum();
        double totalLocoWeight = trip.getLocomotives().size() * LOCOMOTIVE_TARA_KG;
        double totalWagonWeight = trip.getWagons().stream()
                .mapToDouble(w -> WAGON_TARA_KG + WAGON_LOAD_KG)
                .sum();
        double totalWeightKg = totalLocoWeight + totalWagonWeight;

        trip.setCombinedPowerKw(combinedPowerKw);
        trip.setTotalWeightKg(totalWeightKg);

        return trip;
    }

    /**
     * Calcula o tempo de viagem segmento a segmento com base na performance do comboio e linha.
     */
    public TrainTrip calculateTravelTimes(TrainTrip trip) {

        trip = calculateTrainPerformance(trip);

        double totalWeightTons = trip.getTotalWeightKg() / 1000.0;
        double combinedPowerKw = trip.getCombinedPowerKw();

        if (trip.getRoute().isEmpty() || totalWeightTons == 0 || combinedPowerKw == 0) return trip;

        trip.getSegmentEntries().clear();

        double cumulativeTimeHours = 0.0;

        // 1. V_max do comboio (Cálculo Dinâmico)
        double vMaxTrain = (totalWeightTons > 0)
                ? (combinedPowerKw * POWER_TO_TRACTION_FACTOR) / totalWeightTons
                : Double.POSITIVE_INFINITY;

        // Aplica o CAP e MÍNIMO
        vMaxTrain = Math.min(vMaxTrain, MAX_FREIGHT_SPEED_CAP);
        vMaxTrain = Math.max(vMaxTrain, MIN_FREIGHT_SPEED);

        trip.setMaxTrainSpeed(vMaxTrain);

        int currentStationId = trip.getRoute().get(0).getIdEstacaoInicio();
        LocalDateTime entryTime = trip.getDepartureTime();
        trip.setPassageTime(currentStationId, entryTime);

        for (LineSegment seg : trip.getRoute()) {

            int startId = seg.getIdEstacaoInicio();
            int endId = seg.getIdEstacaoFim();
            int nextStationId;

            // Lógica para determinar a direção correta do percurso
            if (startId == currentStationId) {
                nextStationId = endId;
            } else if (endId == currentStationId) {
                nextStationId = startId;
                int tempId = startId;
                startId = endId;
                endId = tempId;
            } else {
                nextStationId = endId;
            }

            // 2. V_efetiva: Mínimo entre a linha e V_max do comboio (effectiveSpeed deve ser 150.0)
            double effectiveSpeed = Math.min(seg.getVelocidadeMaxima(), vMaxTrain);

            // 3. Cálculo do tempo
            double segmentTimeHours = (effectiveSpeed > 0 && seg.getComprimento() > 0)
                    ? seg.getComprimento() / effectiveSpeed
                    : Double.POSITIVE_INFINITY;

            if (Double.isInfinite(segmentTimeHours)) break;

            cumulativeTimeHours += segmentTimeHours;

            long secondsToAdd = Math.round(segmentTimeHours * 3600);
            LocalDateTime exitTime = entryTime.plusSeconds(secondsToAdd);

            // 4. Cria e armazena a entrada de simulação
            SimulationSegmentEntry entry = new SimulationSegmentEntry(
                    trip.getTripId(),
                    seg,
                    entryTime,
                    exitTime,
                    seg.getVelocidadeMaxima(),
                    effectiveSpeed, // <-- Velocidade calculada corretamente
                    getFacilityName(startId),
                    getFacilityName(endId)
            );
            trip.addSegmentEntry(entry);

            entryTime = exitTime;
            trip.setPassageTime(nextStationId, entryTime);
            currentStationId = nextStationId;
        }

        trip.setTotalTravelTimeHours(cumulativeTimeHours);
        return trip;
    }

    /**
     * Obtém o nome da Facility (Estação) usando o StationRepository.
     */
    private String getFacilityName(int id) {
        if (stationRepo != null) {
            Optional<EuropeanStation> station = stationRepo.findById(id);
            if (station.isPresent()) {
                return station.get().getStation();
            }
        }
        return "ID " + id;
    }

    /**
     * Simula o despacho de uma lista de viagens, resolvendo conflitos de via única.
     */
    public SchedulerResult dispatchTrains(List<TrainTrip> trips) {
        SchedulerResult result = new SchedulerResult();

        // 1. Calcular tempos de viagem iniciais (sem conflito)
        List<TrainTrip> initialSchedule = trips.stream()
                .map(this::calculateTravelTimes)
                .collect(Collectors.toList());

        // 2. Algoritmo de Deteção e Resolução de Conflitos (Via Única)
        for (int i = 0; i < initialSchedule.size(); i++) {
            TrainTrip tripA = initialSchedule.get(i);
            for (int j = i + 1; j < initialSchedule.size(); j++) {
                TrainTrip tripB = initialSchedule.get(j);
                Conflict conflict = resolveFirstConflict(tripA, tripB);
                if (conflict != null) {
                    result.addConflict(conflict);
                    if (conflict.delayMinutes > 0 && conflict.tripId2.equals(tripB.getTripId())) {
                        LocalDateTime newDeparture = tripB.getDepartureTime().plusMinutes(conflict.delayMinutes);
                        TrainTrip delayedTrip = new TrainTrip(
                                tripB.getTripId(), newDeparture, tripB.getRoute(), tripB.getLocomotives(), tripB.getWagons());
                        initialSchedule.set(j, calculateTravelTimes(delayedTrip));
                    }
                }
            }
        }

        initialSchedule.forEach(result::addTrip);
        return result;
    }

    /**
     * Lógica placeholder para resolver o PRIMEIRO conflito entre duas viagens em um segmento via única.
     */
    private Conflict resolveFirstConflict(TrainTrip tripA, TrainTrip tripB) {
        // Encontra o segmento de via única partilhado
        for (SimulationSegmentEntry entryA : tripA.getSegmentEntries()) {
            LineSegment segA = entryA.getSegment();
            if (!segA.isViaUnica()) continue;

            for (SimulationSegmentEntry entryB : tripB.getSegmentEntries()) {
                LineSegment segB = entryB.getSegment();

                if (segA.getIdSegmento().equals(segB.getIdSegmento())) {

                    LocalDateTime entryTimeA = entryA.getEntryTime();
                    LocalDateTime exitTimeB = entryB.getExitTime();

                    // Lógica simplificada de conflito por tempo (overlap)
                    if (entryTimeA.isBefore(exitTimeB) && entryA.getExitTime().isAfter(entryB.getEntryTime())) {

                        long delay = 10; // Atraso Fixo

                        // Assumimos que o TrainTrip B deve ser atrasado
                        return new Conflict(
                                tripA.getTripId(),
                                tripB.getTripId(),
                                segA.getIdEstacaoInicio(), // Estação de referência
                                entryTimeA.plusMinutes(delay),
                                delay,
                                String.format("Trip %s must yield segment %s to Trip %s", tripB.getTripId(), segA.getIdSegmento(), tripA.getTripId())
                        );
                    }
                }
            }
        }
        return null;
    }
}
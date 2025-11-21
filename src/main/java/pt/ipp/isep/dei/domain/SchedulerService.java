package pt.ipp.isep.dei.domain;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class SchedulerService {

    // Constantes de Cálculo (Usar valores realistas/placeholders)
    private static final double POWER_TO_TRACTION_FACTOR = 0.55;
    private static final double LOCOMOTIVE_TARA_KG = 80000.0;
    private static final double WAGON_TARA_KG = 25000.0;
    private static final double WAGON_LOAD_KG = 50000.0; // Assumido que os vagões estão carregados (Carga)


    public SchedulerService() {
        // A injeção real ocorreria aqui.
    }


    /**
     * Calcula as propriedades combinadas do comboio (Peso e Potência).
     */
    public TrainTrip calculateTrainPerformance(TrainTrip trip) {

        // 1. Potência Combinada
        double combinedPowerKw = trip.getLocomotives().stream()
                .mapToDouble(Locomotive::getPowerKW)
                .sum();

        // 2. Peso Total (KG): (Locomotivas * Tara) + (Vagões * (Tara + Carga))
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

        double cumulativeTimeHours = 0.0;
        LocalDateTime currentTime = trip.getDepartureTime();

        // Estação inicial
        int startStationId = trip.getRoute().get(0).getIdEstacaoInicio();
        trip.setPassageTime(startStationId, currentTime);

        for (LineSegment seg : trip.getRoute()) {

            // 1. V_max do comboio (simplificação: V_max_comboio [km/h] ≈ Power [kW] * Fator_Tração / Peso [toneladas])
            double vMaxTrain = (totalWeightTons > 0) ? (combinedPowerKw * POWER_TO_TRACTION_FACTOR) / totalWeightTons : Double.POSITIVE_INFINITY;

            // 2. V_efetiva: Mínimo entre a linha e V_max do comboio
            double effectiveSpeed = Math.min(seg.getVelocidadeMaxima(), vMaxTrain);

            // 3. Cálculo do tempo
            double segmentTimeHours = (effectiveSpeed > 0 && seg.getComprimento() > 0)
                    ? seg.getComprimento() / effectiveSpeed
                    : Double.POSITIVE_INFINITY;

            if (Double.isInfinite(segmentTimeHours)) break;

            cumulativeTimeHours += segmentTimeHours;

            long secondsToAdd = Math.round(segmentTimeHours * 3600);
            currentTime = currentTime.plusSeconds(secondsToAdd);

            int nextStationId = (seg.getIdEstacaoInicio() == startStationId) ? seg.getIdEstacaoFim() : seg.getIdEstacaoInicio();
            trip.setPassageTime(nextStationId, currentTime);
            startStationId = nextStationId;
        }

        trip.setTotalTravelTimeHours(cumulativeTimeHours);
        return trip;
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

                    // Aplica o atraso e recalcula a viagem atrasada (sempre Trip B neste mock)
                    if (conflict.delayMinutes > 0 && conflict.tripId2.equals(tripB.getTripId())) {
                        LocalDateTime newDeparture = tripB.getDepartureTime().plusMinutes(conflict.delayMinutes);

                        TrainTrip delayedTrip = new TrainTrip(
                                tripB.getTripId(), newDeparture, tripB.getRoute(), tripB.getLocomotives(), tripB.getWagons());

                        // Recalcula para atualizar os tempos de passagem
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
        for (int i = 0; i < tripA.getRoute().size(); i++) {
            LineSegment segA = tripA.getRoute().get(i);

            for (int j = 0; j < tripB.getRoute().size(); j++) {
                LineSegment segB = tripB.getRoute().get(j);

                // CRÍTICO: Se os segmentos são o mesmo (pelo ID) E é via única
                if (segA.getIdSegmento() == segB.getIdSegmento() && segA.isViaUnica()) {

                    // Encontra a hora de chegada ao segmento/estação anterior (simplificado)
                    int startIdA = (i == 0) ? segA.getIdEstacaoInicio() : segA.getIdEstacaoInicio(); // Assumimos que o primeiro segmento passa pela estação A

                    LocalDateTime timeStartA = tripA.getPassageTimes().get(startIdA);

                    if (timeStartA == null) continue;

                    // SIMPLIFICAÇÃO: Atrasamos a Trip B por 10 minutos (Regra FIFO no segmento)
                    long delay = 10;

                    return new Conflict(
                            tripA.getTripId(),
                            tripB.getTripId(),
                            startIdA,
                            timeStartA.plusMinutes(delay),
                            delay,
                            "TB002 must hold at Station " + startIdA + " for TA001 to pass."
                    );
                }
            }
        }
        return null;
    }
}
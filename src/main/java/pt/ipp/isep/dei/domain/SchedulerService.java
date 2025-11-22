package pt.ipp.isep.dei.domain;

import pt.ipp.isep.dei.repository.FacilityRepository;
import pt.ipp.isep.dei.repository.StationRepository;
import java.time.LocalDateTime;
import java.time.Duration;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.Comparator;
import pt.ipp.isep.dei.repository.SegmentLineRepository; // Adicionado para usar o prefixo INVERSE_ID_PREFIX, se necessário

public class SchedulerService {

    // Constantes de Cálculo
    private static final double POWER_TO_TRACTION_FACTOR = 2.857;

    private static final double LOCOMOTIVE_TARA_KG = 80000.0;
    private static final double WAGON_TARA_KG = 25000.0;
    private static final double WAGON_LOAD_KG = 50000.0;

    // Novo limite máximo de engenharia para o comboio (Cap)
    private static final double MAX_FREIGHT_SPEED_CAP = 250.0;
    private static final double MIN_FREIGHT_SPEED = 30.0;

    private final StationRepository stationRepo;
    private final FacilityRepository facilityRepo;

    public SchedulerService(StationRepository stationRepo, FacilityRepository facilityRepo) {
        this.stationRepo = stationRepo;
        this.facilityRepo = facilityRepo;
    }


    /**
     * Calcula as propriedades combinadas do comboio (Peso e Potência).
     */
    public TrainTrip calculateTrainPerformance(TrainTrip trip) {
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
     * (Método não alterado)
     */
    public TrainTrip calculateTravelTimes(TrainTrip trip) {
        // Implementação do calculateTravelTimes...

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
                    effectiveSpeed,
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
     * Obtém o nome da Facility (Estação) usando o FacilityRepository (mais abrangente).
     * (Método não alterado)
     */
    private String getFacilityName(int id) {
        // Implementação do getFacilityName...
        if (facilityRepo != null) {
            Optional<String> name = facilityRepo.findNameById(id);
            if (name.isPresent()) {
                return name.get();
            }
        }
        if (stationRepo != null) {
            Optional<EuropeanStation> station = stationRepo.findById(id);
            if (station.isPresent()) {
                return station.get().getStation();
            }
        }
        return "ID " + id;
    }

    /**
     * Procura o último ponto seguro (estação/facility com acesso a via dupla) antes
     * do segmento onde ocorre o conflito.
     * (Método não alterado)
     */
    private int findLastSafeWaitingPointId(TrainTrip trip, int conflictSegmentIndex) {
        // Implementação do findLastSafeWaitingPointId...
        int defaultWaitId = trip.getRoute().get(conflictSegmentIndex).getIdEstacaoInicio();

        for (int k = conflictSegmentIndex - 1; k >= 0; k--) {
            LineSegment previousSeg = trip.getRoute().get(k);
            int potentialWaitId = previousSeg.getIdEstacaoFim();

            if (previousSeg.getNumberTracks() > 1) {
                return potentialWaitId;
            }
        }
        return defaultWaitId;
    }


    /**
     * Simula o despacho de uma lista de viagens, resolvendo conflitos de via única.
     * (Método não alterado)
     */
    public SchedulerResult dispatchTrains(List<TrainTrip> trips) {
        // Implementação do dispatchTrains...
        SchedulerResult result = new SchedulerResult();

        // 1. OTIMIZAÇÃO CRÍTICA: Ordenar os comboios pela hora de partida original.
        List<TrainTrip> schedule = trips.stream()
                .sorted(Comparator.comparing(TrainTrip::getDepartureTime))
                .collect(Collectors.toList());

        // 2. Calcular tempos de viagem iniciais e aplicar atrasos em cascata
        schedule = schedule.stream()
                .map(this::calculateTravelTimes)
                .collect(Collectors.toList());

        // 3. Algoritmo de Deteção e Resolução de Conflitos (Via Única)
        for (int i = 0; i < schedule.size(); i++) {
            TrainTrip tripA = schedule.get(i);
            for (int j = i + 1; j < schedule.size(); j++) {
                TrainTrip tripB = schedule.get(j);

                // O comboio TripA (prioritário, mais cedo) é comparado com TripB (atrasado, mais tarde)
                Conflict conflict = resolveFirstConflict(tripA, tripB);

                if (conflict != null) {
                    result.addConflict(conflict);

                    // Lógica para atrasar e recalcular a rota da Trip B (o segundo no par é sempre o atrasado)
                    if (conflict.delayMinutes > 0 && conflict.tripId2.equals(tripB.getTripId())) {
                        LocalDateTime newDeparture = tripB.getDepartureTime().plusMinutes(conflict.delayMinutes);
                        TrainTrip delayedTrip = new TrainTrip(
                                tripB.getTripId(), newDeparture, tripB.getRoute(), tripB.getLocomotives(), tripB.getWagons());

                        // Recalcula e substitui na lista
                        delayedTrip = calculateTravelTimes(delayedTrip);
                        schedule.set(j, delayedTrip);

                        // Atualiza a referência local para o próximo loop de conflitos
                        tripB = delayedTrip;
                    }
                }
            }
        }

        // 4. Adicionar horários finais (com atrasos) ao resultado
        schedule.forEach(result::addTrip);
        return result;
    }

    // Método auxiliar para obter o ID base do segmento físico
    private String getPhysicalSegmentId(String segmentId) {
        if (segmentId.startsWith(SegmentLineRepository.INVERSE_ID_PREFIX)) {
            // Remove o prefixo INV_ para obter o ID físico
            return segmentId.substring(SegmentLineRepository.INVERSE_ID_PREFIX.length());
        }
        return segmentId;
    }

    /**
     * Lógica para resolver o PRIMEIRO conflito entre duas viagens em um segmento via única,
     * calculando o atraso MÍNIMO necessário.
     */
    private Conflict resolveFirstConflict(TrainTrip tripA, TrainTrip tripB) {
        // Encontra o segmento de via única partilhado
        for (int i = 0; i < tripA.getSegmentEntries().size(); i++) {
            SimulationSegmentEntry entryA = tripA.getSegmentEntries().get(i);
            LineSegment segA = entryA.getSegment();
            // Continuar apenas se o segmento A for via única
            if (!segA.isViaUnica()) continue;

            // Obtém o ID físico do segmento A (sem INV_)
            String physicalIdA = getPhysicalSegmentId(segA.getIdSegmento());

            for (int j = 0; j < tripB.getSegmentEntries().size(); j++) {
                SimulationSegmentEntry entryB = tripB.getSegmentEntries().get(j);
                LineSegment segB = entryB.getSegment();

                // (Opcional) Poderíamos verificar se B também é via única, mas se A for, é suficiente para colisão.

                // Obtém o ID físico do segmento B (sem INV_)
                String physicalIdB = getPhysicalSegmentId(segB.getIdSegmento());

                // Condição CORRIGIDA: Verifica se ambos os comboios estão a usar a MESMA via física.
                if (physicalIdA.equals(physicalIdB)) {

                    LocalDateTime exitTimeA = entryA.getExitTime(); // Tempo que A liberta o segmento
                    LocalDateTime entryTimeB = entryB.getEntryTime(); // Tempo que B tentaria entrar

                    // Condição de Conflito: A Trip A (prioritária) ainda está no segmento quando B tenta entrar
                    if (exitTimeA.isAfter(entryTimeB)) {

                        // CÁLCULO DINÂMICO DO ATRASO MÍNIMO: Tempo que B tem de esperar
                        Duration waitDuration = Duration.between(entryTimeB, exitTimeA);

                        // Conversão para minutos, arredondando para cima
                        long delaySeconds = waitDuration.getSeconds();
                        long delayMinutes = (long) Math.ceil(delaySeconds / 60.0);

                        // Adiciona 1 minuto de buffer e garante pelo menos 1 min de atraso
                        delayMinutes = Math.max(1, delayMinutes + 1);

                        long delay = delayMinutes;

                        // O 'scheduledMeetTime' é o tempo de entrada SEGURO de B no segmento
                        LocalDateTime safeEntryTime = entryB.getEntryTime().plusMinutes(delay);

                        // ENCONTRAR PONTO DE ESPERA SEGURO (VIA DUPLA)
                        int safeWaitFacilityId = findLastSafeWaitingPointId(tripB, j);
                        String safeWaitFacilityName = getFacilityName(safeWaitFacilityId);


                        // Retorna o objeto Conflict com o atraso calculado
                        return new Conflict(
                                tripA.getTripId(),
                                tripB.getTripId(),
                                safeWaitFacilityId, // Estação de espera real
                                safeEntryTime,
                                delay,
                                String.format("Trip %s must yield segment %s to Trip %s (Waiting %d min at %s)",
                                        tripB.getTripId(), physicalIdA, tripA.getTripId(), delay, safeWaitFacilityName)
                        );
                    }
                }
            }
        }
        return null;
    }
}
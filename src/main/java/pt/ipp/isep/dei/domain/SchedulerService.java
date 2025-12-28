package pt.ipp.isep.dei.domain;

import pt.ipp.isep.dei.repository.FacilityRepository;
import pt.ipp.isep.dei.repository.StationRepository;
import pt.ipp.isep.dei.repository.SegmentLineRepository;

import java.time.LocalDateTime;
import java.time.Duration;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.Comparator;

public class SchedulerService {

    private static final double POWER_WEIGHT_FACTOR = 15.0;
    private static final double LOCOMOTIVE_TARA_KG = 80000.0;
    private static final double WAGON_TARA_KG = 25000.0;

    private static final double MAX_FREIGHT_SPEED_CAP = 120.0;
    private static final double MIN_FREIGHT_SPEED = 10.0;

    private final StationRepository stationRepo;
    private final FacilityRepository facilityRepo;

    public SchedulerService(StationRepository stationRepo, FacilityRepository facilityRepo) {
        this.stationRepo = stationRepo;
        this.facilityRepo = facilityRepo;
    }

    /**
     * Calcula o peso total SOMANDO O PESO REAL DA CARGA (Caixas).
     */
    public TrainTrip calculateTrainPerformance(TrainTrip trip) {
        double combinedPowerKw = trip.getLocomotives().stream()
                .mapToDouble(Locomotive::getPowerKW)
                .sum();

        double totalLocoWeight = trip.getLocomotives().size() * LOCOMOTIVE_TARA_KG;
        double totalWagonWeight = 0.0;

        for (Wagon w : trip.getWagons()) {
            double wagonWeight = WAGON_TARA_KG;
            double cargoWeight = w.getBoxes().stream()
                    .mapToDouble(Box::getQtyAvailable)
                    .sum();

            wagonWeight += cargoWeight;
            totalWagonWeight += wagonWeight;
        }

        double totalWeightKg = totalLocoWeight + totalWagonWeight;

        trip.setCombinedPowerKw(combinedPowerKw);
        trip.setTotalWeightKg(totalWeightKg);

        return trip;
    }

    public TrainTrip calculateTravelTimes(TrainTrip trip) {
        trip = calculateTrainPerformance(trip);

        double totalWeightKg = trip.getTotalWeightKg();
        double combinedPowerKw = trip.getCombinedPowerKw();

        if (trip.getRoute().isEmpty() || totalWeightKg == 0) return trip;

        trip.getSegmentEntries().clear();
        double cumulativeTimeHours = 0.0;

        // --- CÁLCULO DE COMPONENTES DE PESO (Para o Log) ---
        double locosTare = trip.getLocomotives().size() * LOCOMOTIVE_TARA_KG;
        double wagonsTare = trip.getWagons().size() * WAGON_TARA_KG;
        double cargoWeightKg = totalWeightKg - locosTare - wagonsTare; // O que sobra é Carga

        double weightTons = totalWeightKg / 1000.0;
        double cargoTons = cargoWeightKg / 1000.0;

        // Cálculo Físico da Velocidade
        double vTheoretical = (weightTons > 0) ? (combinedPowerKw / weightTons) * POWER_WEIGHT_FACTOR : 0.0;

        double vFinal = Math.min(vTheoretical, MAX_FREIGHT_SPEED_CAP);
        vFinal = Math.max(vFinal, MIN_FREIGHT_SPEED);

        trip.setMaxTrainSpeed(vFinal);

        // --- LOG ATUALIZADO COM CARGA EXPLÍCITA ---
        // Ex: [PHYSICS] Mass: 455t (Cargo: 210t) | 5600kW / 455t * 15,0 = 185 km/h...
        String log = String.format("   [PHYSICS] Mass: %.0ft (Cargo: %.0ft) | %.0fkW / %.0ft * %.1f = %.0f km/h (Theory) -> FINAL: %.0f km/h",
                weightTons, cargoTons, combinedPowerKw, weightTons, POWER_WEIGHT_FACTOR, vTheoretical, vFinal);

        trip.setPhysicsCalculationLog(log);
        // ------------------------------------------

        int currentStationId = trip.getRoute().get(0).getIdEstacaoInicio();
        LocalDateTime entryTime = trip.getDepartureTime();
        trip.setPassageTime(currentStationId, entryTime);

        for (LineSegment seg : trip.getRoute()) {
            int startId = seg.getIdEstacaoInicio();
            int endId = seg.getIdEstacaoFim();
            int nextStationId = (startId == currentStationId) ? endId : ((endId == currentStationId) ? startId : endId);

            double effectiveSpeed = Math.min(seg.getVelocidadeMaxima(), vFinal);
            double segmentTimeHours = (effectiveSpeed > 0 && seg.getComprimento() > 0) ? seg.getComprimento() / effectiveSpeed : 0;

            cumulativeTimeHours += segmentTimeHours;
            long secondsToAdd = Math.round(segmentTimeHours * 3600);
            LocalDateTime exitTime = entryTime.plusSeconds(secondsToAdd);

            trip.addSegmentEntry(new SimulationSegmentEntry(
                    trip.getTripId(), seg, entryTime, exitTime,
                    seg.getVelocidadeMaxima(), effectiveSpeed,
                    getFacilityName(startId), getFacilityName(endId)
            ));

            entryTime = exitTime;
            trip.setPassageTime(nextStationId, entryTime);
            currentStationId = nextStationId;
        }
        trip.setTotalTravelTimeHours(cumulativeTimeHours);
        return trip;
    }

    // Métodos auxiliares
    private String getFacilityName(int id) {
        if(facilityRepo!=null) return facilityRepo.findNameById(id).orElse(""+id);
        return ""+id;
    }

    public SchedulerResult dispatchTrains(List<TrainTrip> trips) {
        SchedulerResult result = new SchedulerResult();
        List<TrainTrip> schedule = trips.stream()
                .sorted(Comparator.comparing(TrainTrip::getDepartureTime))
                .collect(Collectors.toList());

        schedule = schedule.stream().map(this::calculateTravelTimes).collect(Collectors.toList());

        for (int i = 0; i < schedule.size(); i++) {
            TrainTrip tripA = schedule.get(i);
            for (int j = i + 1; j < schedule.size(); j++) {
                TrainTrip tripB = schedule.get(j);
                Conflict conflict = resolveFirstConflict(tripA, tripB);
                if (conflict != null) {
                    result.addConflict(conflict);
                    if (conflict.delayMinutes > 0 && conflict.tripId2.equals(tripB.getTripId())) {
                        LocalDateTime newDeparture = tripB.getDepartureTime().plusMinutes(conflict.delayMinutes);
                        TrainTrip delayedTrip = new TrainTrip(tripB.getTripId(), newDeparture, tripB.getRoute(), tripB.getLocomotives(), tripB.getWagons());
                        delayedTrip = calculateTravelTimes(delayedTrip);
                        schedule.set(j, delayedTrip);
                        tripB = delayedTrip;
                    }
                }
            }
        }
        schedule.forEach(result::addTrip);
        return result;
    }

    private String getPhysicalSegmentId(String segmentId) {
        if (segmentId.startsWith(SegmentLineRepository.INVERSE_ID_PREFIX)) {
            return segmentId.substring(SegmentLineRepository.INVERSE_ID_PREFIX.length());
        }
        return segmentId;
    }

    private Conflict resolveFirstConflict(TrainTrip tripA, TrainTrip tripB) {
        for (int i = 0; i < tripA.getSegmentEntries().size(); i++) {
            SimulationSegmentEntry entryA = tripA.getSegmentEntries().get(i);
            LineSegment segA = entryA.getSegment();
            if (!segA.isViaUnica()) continue;
            String physA = getPhysicalSegmentId(segA.getIdSegmento());

            for (int j = 0; j < tripB.getSegmentEntries().size(); j++) {
                SimulationSegmentEntry entryB = tripB.getSegmentEntries().get(j);
                LineSegment segB = entryB.getSegment();
                String physB = getPhysicalSegmentId(segB.getIdSegmento());

                if (physA.equals(physB)) {
                    if (entryA.getExitTime().isAfter(entryB.getEntryTime())) {
                        long delay = Duration.between(entryB.getEntryTime(), entryA.getExitTime()).toMinutes() + 1; // +1 min buffer
                        int waitId = findLastSafeWaitingPointId(tripB, j);
                        return new Conflict(
                                tripA.getTripId(), tripB.getTripId(), waitId,
                                entryB.getEntryTime().plusMinutes(delay), delay,
                                String.format("Trip %s yields to %s at %s (%d min)", tripB.getTripId(), tripA.getTripId(), getFacilityName(waitId), delay)
                        );
                    }
                }
            }
        }
        return null;
    }

    private int findLastSafeWaitingPointId(TrainTrip trip, int conflictSegmentIndex) {
        int defaultWaitId = trip.getRoute().get(conflictSegmentIndex).getIdEstacaoInicio();
        for (int k = conflictSegmentIndex - 1; k >= 0; k--) {
            LineSegment previousSeg = trip.getRoute().get(k);
            if (previousSeg.getNumberTracks() > 1) {
                return previousSeg.getIdEstacaoFim();
            }
        }
        return defaultWaitId;
    }
}
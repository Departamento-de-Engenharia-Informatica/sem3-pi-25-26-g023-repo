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

/**
 * Service responsible for calculating train travel performance, simulating route times,
 * and resolving single-track conflicts in the scheduling process.
 */
public class SchedulerService {

    // --- Calculation Constants (Simplified) ---
    // Fator "Simples" solicitado: (Potência / Peso) * 15
    private static final double POWER_WEIGHT_FACTOR = 15.0;

    private static final double LOCOMOTIVE_TARA_KG = 80000.0;
    private static final double WAGON_TARA_KG = 25000.0;
    private static final double WAGON_LOAD_KG = 50000.0;

    // Engineering Limits
    private static final double MAX_FREIGHT_SPEED_CAP = 120.0; // Ajustado para 120 (Standard Mercadorias)
    private static final double MIN_FREIGHT_SPEED = 10.0;

    private final StationRepository stationRepo;
    private final FacilityRepository facilityRepo;

    /**
     * Constructs a new SchedulerService.
     * @param stationRepo Repository for European Stations.
     * @param facilityRepo Repository for Facilities (Stations/Intersections).
     */
    public SchedulerService(StationRepository stationRepo, FacilityRepository facilityRepo) {
        this.stationRepo = stationRepo;
        this.facilityRepo = facilityRepo;
    }

    /**
     * Calculates the combined properties of the train (Weight and Power).
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
     * Calculates the travel time segment by segment based on the train and line performance.
     * Includes the NEW Super Condensed Physics Log.
     */
    public TrainTrip calculateTravelTimes(TrainTrip trip) {
        trip = calculateTrainPerformance(trip);

        double totalWeightKg = trip.getTotalWeightKg();
        double combinedPowerKw = trip.getCombinedPowerKw();

        if (trip.getRoute().isEmpty() || totalWeightKg == 0 || combinedPowerKw == 0) return trip;

        trip.getSegmentEntries().clear();
        double cumulativeTimeHours = 0.0;

        // --- 1. NEW SIMPLIFIED CALCULATION ---
        double weightTons = totalWeightKg / 1000.0;

        // Formula: (Power / Weight_Tons) * Factor
        double vTheoretical = (weightTons > 0)
                ? (combinedPowerKw / weightTons) * POWER_WEIGHT_FACTOR
                : 0.0;

        // Apply CAP and MINIMUM
        double vFinal = Math.min(vTheoretical, MAX_FREIGHT_SPEED_CAP);
        vFinal = Math.max(vFinal, MIN_FREIGHT_SPEED);

        trip.setMaxTrainSpeed(vFinal);

        // --- 2. GENERATE SUPER CONDENSED LOG ---
        // Ex: [PHYSICS] 5600kW / 80t * 15.0 = 1050 km/h (Theory) | Limit: 120 km/h -> FINAL: 120 km/h
        String log = String.format("   [PHYSICS] %.0fkW / %.0ft * %.1f = %.0f km/h (Theory) | Limit: %.0f km/h -> FINAL: %.0f km/h",
                combinedPowerKw, weightTons, POWER_WEIGHT_FACTOR, vTheoretical, MAX_FREIGHT_SPEED_CAP, vFinal);

        trip.setPhysicsCalculationLog(log);
        // ---------------------------------------

        int currentStationId = trip.getRoute().get(0).getIdEstacaoInicio();
        LocalDateTime entryTime = trip.getDepartureTime();
        trip.setPassageTime(currentStationId, entryTime);

        for (LineSegment seg : trip.getRoute()) {

            int startId = seg.getIdEstacaoInicio();
            int endId = seg.getIdEstacaoFim();
            int nextStationId;

            // Logic to determine the correct direction of the route
            if (startId == currentStationId) {
                nextStationId = endId;
            } else if (endId == currentStationId) {
                nextStationId = startId;
                int tempId = startId; startId = endId; endId = tempId;
            } else {
                nextStationId = endId;
            }

            // 3. Effective Speed: Minimum between line speed and train V_max
            double effectiveSpeed = Math.min(seg.getVelocidadeMaxima(), vFinal);

            // 4. Time Calculation
            double segmentTimeHours = (effectiveSpeed > 0 && seg.getComprimento() > 0)
                    ? seg.getComprimento() / effectiveSpeed
                    : Double.POSITIVE_INFINITY;

            if (Double.isInfinite(segmentTimeHours)) break;

            cumulativeTimeHours += segmentTimeHours;

            long secondsToAdd = Math.round(segmentTimeHours * 3600);
            LocalDateTime exitTime = entryTime.plusSeconds(secondsToAdd);

            // 5. Create and store the simulation entry
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

    // --- Helper Methods (Unchanged) ---

    private String getFacilityName(int id) {
        if (facilityRepo != null) {
            Optional<String> name = facilityRepo.findNameById(id);
            if (name.isPresent()) return name.get();
        }
        if (stationRepo != null) {
            Optional<EuropeanStation> station = stationRepo.findById(id);
            if (station.isPresent()) return station.get().getStation();
        }
        return "ID " + id;
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

    public SchedulerResult dispatchTrains(List<TrainTrip> trips) {
        SchedulerResult result = new SchedulerResult();

        List<TrainTrip> schedule = trips.stream()
                .sorted(Comparator.comparing(TrainTrip::getDepartureTime))
                .collect(Collectors.toList());

        // Calculate initial travel times
        schedule = schedule.stream()
                .map(this::calculateTravelTimes)
                .collect(Collectors.toList());

        // Conflict Detection
        for (int i = 0; i < schedule.size(); i++) {
            TrainTrip tripA = schedule.get(i);
            for (int j = i + 1; j < schedule.size(); j++) {
                TrainTrip tripB = schedule.get(j);

                Conflict conflict = resolveFirstConflict(tripA, tripB);

                if (conflict != null) {
                    result.addConflict(conflict);

                    if (conflict.delayMinutes > 0 && conflict.tripId2.equals(tripB.getTripId())) {
                        LocalDateTime newDeparture = tripB.getDepartureTime().plusMinutes(conflict.delayMinutes);
                        TrainTrip delayedTrip = new TrainTrip(
                                tripB.getTripId(), newDeparture, tripB.getRoute(), tripB.getLocomotives(), tripB.getWagons());

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

            String physicalIdA = getPhysicalSegmentId(segA.getIdSegmento());

            for (int j = 0; j < tripB.getSegmentEntries().size(); j++) {
                SimulationSegmentEntry entryB = tripB.getSegmentEntries().get(j);
                LineSegment segB = entryB.getSegment();
                String physicalIdB = getPhysicalSegmentId(segB.getIdSegmento());

                if (physicalIdA.equals(physicalIdB)) {
                    LocalDateTime exitTimeA = entryA.getExitTime();
                    LocalDateTime entryTimeB = entryB.getEntryTime();

                    if (exitTimeA.isAfter(entryTimeB)) {
                        Duration waitDuration = Duration.between(entryTimeB, exitTimeA);
                        long delayMinutes = (long) Math.ceil(waitDuration.getSeconds() / 60.0);
                        delayMinutes = Math.max(1, delayMinutes + 1);

                        int safeWaitFacilityId = findLastSafeWaitingPointId(tripB, j);
                        String safeWaitFacilityName = getFacilityName(safeWaitFacilityId);

                        return new Conflict(
                                tripA.getTripId(),
                                tripB.getTripId(),
                                safeWaitFacilityId,
                                entryB.getEntryTime().plusMinutes(delayMinutes),
                                delayMinutes,
                                String.format("Trip %s must yield segment %s to Trip %s (Waiting %d min at %s)",
                                        tripB.getTripId(), physicalIdA, tripA.getTripId(), delayMinutes, safeWaitFacilityName)
                        );
                    }
                }
            }
        }
        return null;
    }
}
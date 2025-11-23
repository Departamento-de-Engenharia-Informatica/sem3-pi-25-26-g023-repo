package pt.ipp.isep.dei.domain;

import pt.ipp.isep.dei.repository.FacilityRepository;
import pt.ipp.isep.dei.repository.StationRepository;
import java.time.LocalDateTime;
import java.time.Duration;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.Comparator;
import pt.ipp.isep.dei.repository.SegmentLineRepository; // Added to use the INVERSE_ID_PREFIX, if necessary

/**
 * Service responsible for calculating train travel performance, simulating route times,
 * and resolving single-track conflicts in the scheduling process.
 */
public class SchedulerService {

    // Calculation Constants
    private static final double POWER_TO_TRACTION_FACTOR = 2.857;

    private static final double LOCOMOTIVE_TARA_KG = 80000.0;
    private static final double WAGON_TARA_KG = 25000.0;
    private static final double WAGON_LOAD_KG = 50000.0;

    // New maximum engineering speed limit for the train (Cap)
    private static final double MAX_FREIGHT_SPEED_CAP = 250.0;
    private static final double MIN_FREIGHT_SPEED = 30.0;

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
     *
     * @param trip The trip to calculate performance for.
     * @return The updated TrainTrip object.
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
     * (Unchanged method)
     *
     * @param trip The trip to simulate.
     * @return The updated TrainTrip object with segment entries and calculated times.
     */
    public TrainTrip calculateTravelTimes(TrainTrip trip) {
        // Implementation of calculateTravelTimes...

        trip = calculateTrainPerformance(trip);

        double totalWeightTons = trip.getTotalWeightKg() / 1000.0;
        double combinedPowerKw = trip.getCombinedPowerKw();

        if (trip.getRoute().isEmpty() || totalWeightTons == 0 || combinedPowerKw == 0) return trip;

        trip.getSegmentEntries().clear();

        double cumulativeTimeHours = 0.0;

        // 1. Train V_max (Dynamic Calculation)
        double vMaxTrain = (totalWeightTons > 0)
                ? (combinedPowerKw * POWER_TO_TRACTION_FACTOR) / totalWeightTons
                : Double.POSITIVE_INFINITY;

        // Apply CAP and MINIMUM
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

            // Logic to determine the correct direction of the route
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

            // 2. Effective Speed (V_effective): Minimum between line speed and train V_max
            double effectiveSpeed = Math.min(seg.getVelocidadeMaxima(), vMaxTrain);

            // 3. Time Calculation
            double segmentTimeHours = (effectiveSpeed > 0 && seg.getComprimento() > 0)
                    ? seg.getComprimento() / effectiveSpeed
                    : Double.POSITIVE_INFINITY;

            if (Double.isInfinite(segmentTimeHours)) break;

            cumulativeTimeHours += segmentTimeHours;

            long secondsToAdd = Math.round(segmentTimeHours * 3600);
            LocalDateTime exitTime = entryTime.plusSeconds(secondsToAdd);

            // 4. Create and store the simulation entry
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
     * Gets the name of the Facility (Station) using the FacilityRepository (more comprehensive).
     * (Unchanged method)
     *
     * @param id The facility ID.
     * @return The name of the facility or a default string if not found.
     */
    private String getFacilityName(int id) {
        // Implementation of getFacilityName...
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
     * Finds the last safe waiting point (station/facility with access to a double track)
     * before the segment where the conflict occurs.
     * (Unchanged method)
     *
     * @param trip The trip to analyze.
     * @param conflictSegmentIndex The index of the segment where the conflict is detected.
     * @return The ID of the safest waiting facility.
     */
    private int findLastSafeWaitingPointId(TrainTrip trip, int conflictSegmentIndex) {
        // Implementation of findLastSafeWaitingPointId...
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
     * Simulates the dispatch of a list of trips, resolving single-track conflicts.
     * (Unchanged method)
     *
     * @param trips The list of train trips to schedule.
     * @return A {@link SchedulerResult} containing the final scheduled trips and resolved conflicts.
     */
    public SchedulerResult dispatchTrains(List<TrainTrip> trips) {
        // Implementation of dispatchTrains...
        SchedulerResult result = new SchedulerResult();

        // 1. CRITICAL OPTIMIZATION: Sort trains by original departure time.
        List<TrainTrip> schedule = trips.stream()
                .sorted(Comparator.comparing(TrainTrip::getDepartureTime))
                .collect(Collectors.toList());

        // 2. Calculate initial travel times and apply cascading delays
        schedule = schedule.stream()
                .map(this::calculateTravelTimes)
                .collect(Collectors.toList());

        // 3. Conflict Detection and Resolution Algorithm (Single Track)
        for (int i = 0; i < schedule.size(); i++) {
            TrainTrip tripA = schedule.get(i);
            for (int j = i + 1; j < schedule.size(); j++) {
                TrainTrip tripB = schedule.get(j);

                // TripA (prioritized, earlier) is compared with TripB (delayed, later)
                Conflict conflict = resolveFirstConflict(tripA, tripB);

                if (conflict != null) {
                    result.addConflict(conflict);

                    // Logic to delay and recalculate the route of Trip B (the second in the pair is always the one delayed)
                    if (conflict.delayMinutes > 0 && conflict.tripId2.equals(tripB.getTripId())) {
                        LocalDateTime newDeparture = tripB.getDepartureTime().plusMinutes(conflict.delayMinutes);
                        TrainTrip delayedTrip = new TrainTrip(
                                tripB.getTripId(), newDeparture, tripB.getRoute(), tripB.getLocomotives(), tripB.getWagons());

                        // Recalculate and substitute in the list
                        delayedTrip = calculateTravelTimes(delayedTrip);
                        schedule.set(j, delayedTrip);

                        // Update the local reference for the next conflict loop
                        tripB = delayedTrip;
                    }
                }
            }
        }

        // 4. Add final schedules (with delays) to the result
        schedule.forEach(result::addTrip);
        return result;
    }

    // Helper method to get the physical base ID of the segment
    private String getPhysicalSegmentId(String segmentId) {
        if (segmentId.startsWith(SegmentLineRepository.INVERSE_ID_PREFIX)) {
            // Remove the INV_ prefix to get the physical ID
            return segmentId.substring(SegmentLineRepository.INVERSE_ID_PREFIX.length());
        }
        return segmentId;
    }

    /**
     * Logic to resolve the FIRST conflict between two trips on a single-track segment,
     * calculating the MINIMUM required delay.
     *
     * @param tripA The prioritized trip (earlier departure).
     * @param tripB The trip being checked for conflict (later departure).
     * @return A {@link Conflict} object if a conflict is found and resolved, otherwise {@code null}.
     */
    private Conflict resolveFirstConflict(TrainTrip tripA, TrainTrip tripB) {
        // Find the shared single-track segment
        for (int i = 0; i < tripA.getSegmentEntries().size(); i++) {
            SimulationSegmentEntry entryA = tripA.getSegmentEntries().get(i);
            LineSegment segA = entryA.getSegment();
            // Continue only if segment A is single track
            if (!segA.isViaUnica()) continue;

            // Get the physical ID of segment A (without INV_)
            String physicalIdA = getPhysicalSegmentId(segA.getIdSegmento());

            for (int j = 0; j < tripB.getSegmentEntries().size(); j++) {
                SimulationSegmentEntry entryB = tripB.getSegmentEntries().get(j);
                LineSegment segB = entryB.getSegment();

                // (Optional) We could check if B is also single track, but if A is, it's enough for collision.

                // Get the physical ID of segment B (without INV_)
                String physicalIdB = getPhysicalSegmentId(segB.getIdSegmento());

                // CORRECTED Condition: Check if both trains are using the SAME physical track.
                if (physicalIdA.equals(physicalIdB)) {

                    LocalDateTime exitTimeA = entryA.getExitTime(); // Time when A releases the segment
                    LocalDateTime entryTimeB = entryB.getEntryTime(); // Time when B would attempt to enter

                    // Conflict Condition: Trip A (prioritized) is still in the segment when B tries to enter
                    if (exitTimeA.isAfter(entryTimeB)) {

                        // DYNAMIC CALCULATION OF MINIMUM DELAY: Time B must wait
                        Duration waitDuration = Duration.between(entryTimeB, exitTimeA);

                        // Conversion to minutes, rounding up
                        long delaySeconds = waitDuration.getSeconds();
                        long delayMinutes = (long) Math.ceil(delaySeconds / 60.0);

                        // Add 1 minute buffer and ensure at least 1 min delay
                        delayMinutes = Math.max(1, delayMinutes + 1);

                        long delay = delayMinutes;

                        // The 'scheduledMeetTime' is the SAFE entry time for B into the segment
                        LocalDateTime safeEntryTime = entryB.getEntryTime().plusMinutes(delay);

                        // FIND SAFE WAITING POINT (DOUBLE TRACK)
                        int safeWaitFacilityId = findLastSafeWaitingPointId(tripB, j);
                        String safeWaitFacilityName = getFacilityName(safeWaitFacilityId);


                        // Return the Conflict object with the calculated delay
                        return new Conflict(
                                tripA.getTripId(),
                                tripB.getTripId(),
                                safeWaitFacilityId, // Actual waiting station
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
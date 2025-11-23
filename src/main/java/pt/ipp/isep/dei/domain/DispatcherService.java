package pt.ipp.isep.dei.domain;

import pt.ipp.isep.dei.repository.FacilityRepository;
import pt.ipp.isep.dei.repository.TrainRepository;
import pt.ipp.isep.dei.repository.LocomotiveRepository;
// import pt.ipp.isep.dei.repository.SegmentLineRepository; // No longer needed

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * Service responsible for orchestrating the train simulation process (USLP07).
 * It handles the conversion of repository objects (Train) into domain objects (TrainTrip)
 * by finding the optimal route and identifying the locomotive data.
 * The core scheduling and conflict resolution tasks are delegated to the {@code SchedulerService}.
 */
public class DispatcherService {

    private final TrainRepository trainRepo;
    private final RailwayNetworkService networkService;
    private final FacilityRepository facilityRepo;
    private final LocomotiveRepository locomotiveRepo;
    private final SchedulerService schedulerService;

    /**
     * Default maximum speed used to find the shortest path (minimum travel time) in the network service.
     */
    private static final double DEFAULT_MAX_SPEED = 1000.0;

    /**
     * Constructs the DispatcherService, injecting all necessary repositories and the core scheduling logic service.
     * * @param trainRepo The train repository.
     * @param networkService The railway network service for route finding.
     * @param facilityRepo The facility repository.
     * @param locomotiveRepo The locomotive repository.
     * @param schedulerService The scheduling and conflict resolution service.
     */
    public DispatcherService(
            TrainRepository trainRepo,
            RailwayNetworkService networkService,
            FacilityRepository facilityRepo,
            LocomotiveRepository locomotiveRepo,
            SchedulerService schedulerService) {

        this.trainRepo = trainRepo;
        this.networkService = networkService;
        this.facilityRepo = facilityRepo;
        this.locomotiveRepo = locomotiveRepo;
        this.schedulerService = schedulerService;
    }

    // =================================================================================
    // 1. Core Scheduling Method
    // =================================================================================

    /**
     * Prepares the trips (TrainTrip) by finding their routes and delegates the full simulation,
     * including time calculation and conflict resolution, to the SchedulerService.
     * * @param trainsToSimulate List of Train repository objects to simulate.
     * @return A {@code SchedulerResult} containing the final schedules and resolved conflicts.
     */
    public SchedulerResult scheduleTrains(List<Train> trainsToSimulate) {
        List<TrainTrip> initialTrips = new ArrayList<>();

        // 1. Convert Trains into TrainTrips
        for (Train train : trainsToSimulate) {
            Optional<TrainTrip> trip = createTrainTrip(train);
            trip.ifPresent(initialTrips::add);
        }

        if (initialTrips.isEmpty()) {
            return new SchedulerResult();
        }

        // 2. Delegate the complete simulation (time calculation + conflict resolution)
        // to the SchedulerService. The SchedulerService returns the recalculated schedules.
        return schedulerService.dispatchTrains(initialTrips);
    }

    /**
     * Converts a Train repository object into a TrainTrip domain object by finding the route and loading the locomotive data.
     * * Route/Locomotive failures are typically handled or logged by the calling layer (UI/Controller)
     * upon checking the Optional result.
     *
     * @param train The train to process.
     * @return An Optional containing the {@code TrainTrip} if a valid route is found, otherwise {@code Optional.empty()}.
     */
    private Optional<TrainTrip> createTrainTrip(Train train) {
        // Find the route.
        List<LineSegment> route = findRouteForTrain(train);

        if (route.isEmpty()) {
            return Optional.empty();
        }

        // Find the locomotive to build the TrainTrip.
        List<Locomotive> locomotives = Collections.emptyList();
        try {
            int locoId = Integer.parseInt(train.getLocomotiveId());
            Optional<Locomotive> optLoco = locomotiveRepo.findById(locoId);
            if (optLoco.isPresent()) {
                locomotives = Collections.singletonList(optLoco.get());
            }
        } catch (NumberFormatException e) {
            // Error handling print removed (handled by UI layer).
        }

        // Create the TrainTrip. The calculation of weight/power and Vmax is handled by the SchedulerService.
        TrainTrip trip = new TrainTrip(
                train.getTrainId(),
                train.getDepartureTime(),
                route,
                locomotives,
                Collections.emptyList() // Assuming 0 wagons for simplification or use the Wagon repo if applicable.
        );

        return Optional.of(trip);
    }

    /**
     * Uses the RailwayNetworkService to find the complete route (based on the train's start and end facilities).
     * The fastest path (shortest time) is prioritized by using a high speed assumption.
     *
     * @param train The train whose route is to be found.
     * @return The list of {@code LineSegment}s forming the route, or an empty list if no path is found.
     */
    private List<LineSegment> findRouteForTrain(Train train) {
        // Find the fastest/shortest path, using a high speed to prioritize minimum time
        RailwayPath path = networkService.findFastestPath(
                train.getStartFacilityId(),
                train.getEndFacilityId(),
                DEFAULT_MAX_SPEED
        );

        return (path != null) ? path.getSegments() : Collections.emptyList();
    }
}
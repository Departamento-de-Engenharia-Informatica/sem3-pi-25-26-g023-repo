package pt.ipp.isep.dei.UI;

import pt.ipp.isep.dei.controller.TravelTimeController;
import pt.ipp.isep.dei.controller.SchedulerController;
import pt.ipp.isep.dei.domain.*;
import pt.ipp.isep.dei.repository.StationRepository;
import pt.ipp.isep.dei.repository.LocomotiveRepository;
import pt.ipp.isep.dei.repository.TrainRepository;
import pt.ipp.isep.dei.repository.FacilityRepository;
import pt.ipp.isep.dei.domain.SpatialSearchQueries;
import pt.ipp.isep.dei.domain.PickingPathService;
import pt.ipp.isep.dei.domain.HeuristicType;
import pt.ipp.isep.dei.domain.RadiusSearch;
import pt.ipp.isep.dei.domain.StationDistance;
import pt.ipp.isep.dei.domain.DensitySummary;
import pt.ipp.isep.dei.domain.SchedulerResult;
import pt.ipp.isep.dei.domain.TrainTrip;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.InputMismatchException;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.time.Duration;
import java.time.format.DateTimeFormatter;
import java.time.LocalDateTime;
import java.util.stream.Collectors;

/**
 * Main User Interface for the Logistics on Rails application.
 * Provides a comprehensive menu system for accessing all functionality
 * including warehouse operations, spatial queries, and train scheduling.
 */
public class CargoHandlingUI implements Runnable {

    // ANSI Color Codes for console output formatting
    public static final String ANSI_RESET = "\u001B[0m";
    public static final String ANSI_RED = "\u001B[31m";
    public static final String ANSI_GREEN = "\u001B[32m";
    public static final String ANSI_YELLOW = "\u001B[33m";
    public static final String ANSI_BLUE = "\u001B[34m";
    public static final String ANSI_PURPLE = "\u001B[35m";
    public static final String ANSI_CYAN = "\u001B[36m";
    public static final String ANSI_BOLD = "\u001B[1m";
    public static final String ANSI_ITALIC = "\u001B[3m";

    // Core application components
    private final WMS wms;
    private final InventoryManager manager;
    private final List<Wagon> wagons;
    private final TravelTimeController travelTimeController;
    private final StationRepository estacaoRepo;
    private final LocomotiveRepository locomotivaRepo;
    private final StationIndexManager stationIndexManager;
    private final KDTree spatialKDTree;
    private final SpatialSearch spatialSearchEngine;
    private final SchedulerController schedulerController;
    private final DispatcherService dispatcherService;
    private final FacilityRepository facilityRepo;
    private final TrainRepository trainRepo = new TrainRepository();

    // State management for workflow operations
    private AllocationResult lastAllocationResult = null;
    private PickingPlan lastPickingPlan = null;
    private final Scanner scanner;

    /**
     * Constructs a new CargoHandlingUI with all required dependencies.
     */
    public CargoHandlingUI(WMS wms, InventoryManager manager, List<Wagon> wagons,
                           TravelTimeController travelTimeController, StationRepository estacaoRepo,
                           LocomotiveRepository locomotivaRepo,
                           StationIndexManager stationIndexManager,
                           KDTree spatialKDTree,
                           SpatialSearch spatialSearchEngine,
                           SchedulerController schedulerController,
                           DispatcherService dispatcherService,
                           FacilityRepository facilityRepo) {
        this.wms = wms;
        this.manager = manager;
        this.wagons = wagons;
        this.travelTimeController = travelTimeController;
        this.estacaoRepo = estacaoRepo;
        this.locomotivaRepo = locomotivaRepo;
        this.stationIndexManager = stationIndexManager;
        this.spatialKDTree = spatialKDTree;
        this.spatialSearchEngine = spatialSearchEngine;
        this.schedulerController = schedulerController;
        this.dispatcherService = dispatcherService;
        this.facilityRepo = facilityRepo;
        this.scanner = new Scanner(System.in);
    }

    /**
     * Main execution loop for the user interface.
     */
    @Override
    public void run() {
        int option = -1;

        do {
            showMenu();
            try {
                option = readInt(0, 14, ANSI_BOLD + "Option: " + ANSI_RESET);

                try {
                    handleOption(option);
                } catch (Exception e) {
                    showError("An unexpected error occurred: " + e.getMessage());
                    System.err.println(ANSI_ITALIC + "Stack trace (for debug):");
                    e.printStackTrace(System.err);
                    System.err.println(ANSI_RESET);
                }

            } catch (InputMismatchException e) {
                showError("Invalid input. Please enter a number.");
                scanner.nextLine();
            } catch (Exception e) {
                showError("Fatal UI Error: " + e.getMessage());
                option = 0;
            }

            if (option != 0) {
                promptEnterKey();
            }

        } while (option != 0);

        System.out.println(ANSI_CYAN + "\nClosing scanner. Goodbye! 👋" + ANSI_RESET);
        scanner.close();
    }

    /**
     * Displays the main menu with all available options organized by functionality.
     */
    private void showMenu() {
        System.out.print("\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n");

        System.out.println(ANSI_BOLD + ANSI_BLUE + "==========================================================" + ANSI_RESET);
        System.out.println(ANSI_BOLD + ANSI_BLUE + "      🚆 LOGISTICS ON RAILS - G023 MAIN MENU 🚆      " + ANSI_RESET);
        System.out.println(ANSI_BOLD + ANSI_BLUE + "==========================================================" + ANSI_RESET);

        System.out.println("\n" + ANSI_BOLD + ANSI_PURPLE + "--- Warehouse Setup (Sprint 1) ---" + ANSI_RESET);
        System.out.println(ANSI_GREEN + " 1. " + ANSI_RESET + "[USEI01] Unload Wagons (Status: " + ANSI_ITALIC + "Loaded on startup" + ANSI_RESET + ")");
        System.out.println(ANSI_GREEN + " 2. " + ANSI_RESET + "[USEI05] Process Quarantine Returns");

        System.out.println("\n" + ANSI_BOLD + ANSI_PURPLE + "--- Picking Workflow (Sprint 1) ---" + ANSI_RESET);
        System.out.println(ANSI_GREEN + " 3. " + ANSI_RESET + "[USEI02] Allocate Orders");
        System.out.println(ANSI_GREEN + " 4. " + ANSI_RESET + "[USEI03] Pack Trolleys " + ANSI_ITALIC + "(Run US02 first)" + ANSI_RESET);
        System.out.println(ANSI_GREEN + " 5. " + ANSI_RESET + "[USEI04] Calculate Pick Path " + ANSI_ITALIC + "(Run US03 first)" + ANSI_RESET);

        System.out.println("\n" + ANSI_BOLD + ANSI_PURPLE + "--- Railway & Station Ops (S1, S2 & S3) ---" + ANSI_RESET);
        System.out.println(ANSI_GREEN + " 6. " + ANSI_RESET + "[USLP03] Calculate Train Travel Time (S1)");
        System.out.println(ANSI_GREEN + " 7. " + ANSI_RESET + "[USEI06] Query European Station Index (S2)");
        System.out.println(ANSI_GREEN + " 8. " + ANSI_RESET + "[USEI07] Build & Analyze 2D-Tree (S2)");
        System.out.println(ANSI_GREEN + " 9. " + ANSI_RESET + "[USEI08] Spatial Queries - Search by Area (S2)");
        System.out.println(ANSI_GREEN + "10. " + ANSI_RESET + "[USEI09] Proximity Search - Nearest N (S2)");
        System.out.println(ANSI_GREEN + "11. " + ANSI_RESET + "[USEI10] Radius Search & Density Summary (S2)");
        System.out.println(ANSI_GREEN + "14. " + ANSI_RESET + "[USLP07] Run Full Simulation & Conflicts (S3)");

        System.out.println("\n" + ANSI_BOLD + ANSI_PURPLE + "--- System Information ---" + ANSI_RESET);
        System.out.println(ANSI_GREEN + "12. " + ANSI_RESET + "View Current Inventory");
        System.out.println(ANSI_GREEN + "13. " + ANSI_RESET + "View Warehouse Info");

        System.out.println("\n" + ANSI_BOLD + "----------------------------------------------------------" + ANSI_RESET);

        String allocStatus = (lastAllocationResult != null && !lastAllocationResult.allocations.isEmpty()) ?
                ANSI_GREEN + String.format("GENERATED (%d allocs)", lastAllocationResult.allocations.size()) : ANSI_YELLOW + "NOT-RUN";
        String planStatus = (lastPickingPlan != null) ?
                ANSI_GREEN + String.format("GENERATED (%d trolleys)", lastPickingPlan.getTotalTrolleys()) : ANSI_YELLOW + "NOT-RUN";
        System.out.println(ANSI_BOLD + "   Status: [Allocations: " + allocStatus + ANSI_BOLD + "] [Picking Plan: " + planStatus + ANSI_BOLD + "]" + ANSI_RESET);

        System.out.println(ANSI_YELLOW + "  0. " + ANSI_RESET + "Exit System");
        System.out.println(ANSI_BOLD + "----------------------------------------------------------" + ANSI_RESET);
    }

    /**
     * Handles the menu option selected by the user.
     */
    private void handleOption(int option) {
        switch (option) {
            case 1:
                handleUnloadWagons();
                break;
            case 2:
                handleProcessReturns();
                break;
            case 3:
                handleAllocateOrders();
                break;
            case 4:
                handlePackTrolleys();
                break;
            case 5:
                handleCalculatePickingPath();
                break;
            case 6:
                handleCalculateTravelTime();
                break;
            case 7:
                handleQueryStationIndex();
                break;
            case 8:
                handleBuild2DTree();
                break;
            case 9:
                handleSpatialQueries();
                break;
            case 10:
                handleNearestNQuery();
                break;
            case 11:
                handleRadiusSearch();
                break;
            case 12:
                handleViewInventory();
                break;
            case 13:
                handleViewWarehouseInfo();
                break;
            case 14:
                handleRunFullSimulation();
                break;
            case 0:
                System.out.println(ANSI_CYAN + "\nExiting Cargo Handling Menu... 👋" + ANSI_RESET);
                break;
            default:
                showError("Invalid option. Please select a valid number from the menu.");
                break;
        }
    }

    /**
     * Handles the full train simulation with conflict analysis.
     */
    private void handleRunFullSimulation() {
        showInfo("--- [USLP07] Full Simulation & Conflict Analysis ---");

        try {
            List<Train> allTrains = trainRepo.findAll();
            if (allTrains.isEmpty()) {
                showError("No scheduled Trains found in the database.");
                return;
            }

            System.out.println(ANSI_BOLD + "\n--- 1. Select Trains for Simulation ---" + ANSI_RESET);
            allTrains.forEach(t -> {
                String startName = facilityRepo.findNameById(t.getStartFacilityId()).orElse("F" + t.getStartFacilityId());
                String endName = facilityRepo.findNameById(t.getEndFacilityId()).orElse("F" + t.getEndFacilityId());
                String status = String.format("Route: %s -> %s | Departure: %s | Loco: %s",
                        startName, endName, t.getDepartureTime().toLocalTime(), t.getLocomotiveId());
                System.out.printf(ANSI_CYAN + "   [%s] %s%n" + ANSI_RESET, t.getTrainId(), status);
            });

            String trainIdsStr = readString(ANSI_BOLD + "Enter Train IDs to simulate (e.g., 5421,5437) [c=Cancel]: " + ANSI_RESET);
            if (isCancel(trainIdsStr)) {
                showInfo("Simulation cancelled by user.");
                return;
            }

            List<String> selectedIds = Arrays.stream(trainIdsStr.split(","))
                    .map(String::trim)
                    .toList();

            List<Train> trainsToSimulate = allTrains.stream()
                    .filter(t -> selectedIds.contains(t.getTrainId()))
                    .toList();

            if (trainsToSimulate.isEmpty()) {
                showError("No valid Train IDs selected. Cannot run simulation.");
                return;
            }

            System.out.printf(ANSI_CYAN + "\nExecuting Schedule calculation for %d selected trains...%n" + ANSI_RESET, trainsToSimulate.size());

            SchedulerResult schedulerResult = dispatcherService.scheduleTrains(trainsToSimulate);

            Map<String, TrainTrip> scheduledTripsMap = schedulerResult.scheduledTrips.stream()
                    .collect(Collectors.toMap(
                            TrainTrip::getTripId,
                            trip -> trip,
                            (existing, replacement) -> existing
                    ));

            if (scheduledTripsMap.isEmpty()) {
                showError("No valid scheduled trips could be simulated for the selected trains (check routes).");
                return;
            }

            showSuccess("Full simulation completed successfully for " + scheduledTripsMap.size() + " trains!");
            printSimulationTimetables(scheduledTripsMap, schedulerResult.resolvedConflicts);

            List<String> conflictReport = schedulerResult.resolvedConflicts.stream()
                    .map(c -> c.toString())
                    .collect(Collectors.toList());

            printConflictReport(conflictReport);

        } catch (Exception e) {
            showError("Failed to execute full simulation (USLP07): " + e.getMessage());
            System.err.println(ANSI_ITALIC + "Stack trace (for debug):");
            e.printStackTrace(System.err);
            System.err.println(ANSI_RESET);
        }
    }

    /**
     * Prints detailed segment timetables for all simulated trains.
     */
    private void printSimulationTimetables(Map<String, TrainTrip> scheduledTripsMap, List<Conflict> conflicts) {
        System.out.println("\n" + ANSI_BOLD + ANSI_BLUE + "=========================================================================================" + ANSI_RESET);
        System.out.println(ANSI_BOLD + "                            DETAILED SEGMENT TIMETABLE (SIMULATION OUTPUT) " + ANSI_RESET);
        System.out.println(ANSI_BOLD + ANSI_BLUE + "=========================================================================================" + ANSI_RESET);

        DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");
        Map<String, Map<Integer, Long>> delayPoints = new HashMap<>();

        for (Conflict c : conflicts) {
            String delayedTripId = c.tripId2;
            int waitFacilityId = c.getSafeWaitFacilityId();
            long delay = c.delayMinutes;
            delayPoints.computeIfAbsent(delayedTripId, k -> new HashMap<>())
                    .merge(waitFacilityId, delay, Long::sum);
        }

        for (Map.Entry<String, TrainTrip> entry : scheduledTripsMap.entrySet()) {
            TrainTrip trip = entry.getValue();
            String trainId = trip.getTripId();
            List<SimulationSegmentEntry> timetable = trip.getSegmentEntries();

            if (timetable.isEmpty()) continue;

            Train originalTrain = trainRepo.findById(trainId).orElse(null);
            if (originalTrain == null) continue;

            LocalDateTime initialDeparture = originalTrain.getDepartureTime();
            LocalDateTime actualDeparture = trip.getDepartureTime();
            LocalDateTime currentTime = initialDeparture;

            String locoInfo = "N/A (0 kW)";
            double maxCalculatedSpeed = trip.getMaxTrainSpeed();

            if (originalTrain.getLocomotiveId() != null) {
                try {
                    Locomotive loco = locomotivaRepo.findById(Integer.parseInt(originalTrain.getLocomotiveId())).orElse(null);
                    if (loco != null) {
                        locoInfo = String.format("%s (%.0f kW)", loco.getLocomotiveId(), loco.getPowerKW());
                    }
                } catch (NumberFormatException e) {
                    locoInfo = originalTrain.getLocomotiveId() + " (Power N/A)";
                }
            }

            String originalDepartureStr = initialDeparture.toLocalTime().format(timeFormatter);
            String departureDisplay = originalDepartureStr;

            String speedDisplay = (maxCalculatedSpeed > 0 && maxCalculatedSpeed != Double.POSITIVE_INFINITY)
                    ? String.format("%.0f km/h", maxCalculatedSpeed)
                    : "N/A";

            System.out.printf(ANSI_BOLD + "\nTrain %s — Final Departure %s%n" + ANSI_RESET, trainId, departureDisplay);
            System.out.printf(ANSI_ITALIC + "   Composition: Locomotive %s | Max Calculated Speed: %s%n" + ANSI_RESET, locoInfo, speedDisplay);

            System.out.println(ANSI_BOLD + ANSI_CYAN +
                    "ID\tFROM FACILITY\t\tTO FACILITY\t\tTYPE\tLENGTH\t\tENTRY\t\tEXIT\t\tSPEED (C/A)" + ANSI_RESET);
            System.out.println("-".repeat(95));

            for (int i = 0; i < timetable.size(); i++) {
                SimulationSegmentEntry segment = timetable.get(i);
                int startFacilityId = segment.getSegment().getIdEstacaoInicio();
                Map<Integer, Long> tripDelays = delayPoints.getOrDefault(trainId, Map.of());
                long accumulatedDelayAtThisFacility = tripDelays.getOrDefault(startFacilityId, 0L);

                if (accumulatedDelayAtThisFacility > 0) {
                    String facilityName = segment.getStartFacilityName();
                    LocalDateTime delayStart = currentTime;
                    currentTime = currentTime.plusMinutes(accumulatedDelayAtThisFacility);
                    LocalDateTime delayEnd = currentTime;

                    System.out.printf(ANSI_YELLOW +
                                    ANSI_BOLD + "DELAY\t%-20s\t%-20s\t%-5s\t%s%4d min\t%s\t%s\t%s%n" + ANSI_RESET,
                            facilityName.substring(0, Math.min(facilityName.length(), 16)),
                            facilityName.substring(0, Math.min(facilityName.length(), 16)),
                            "WAIT",
                            "",
                            accumulatedDelayAtThisFacility,
                            delayStart.toLocalTime().format(timeFormatter),
                            delayEnd.toLocalTime().format(timeFormatter),
                            "0/0"
                    );
                    delayPoints.get(trainId).remove(startFacilityId);
                }

                double segmentTimeHours = segment.getSegment().getComprimento() / segment.getCalculatedSpeedKmh();
                long secondsToAdd = Math.round(segmentTimeHours * 3600);
                LocalDateTime entryVisual = currentTime;
                LocalDateTime exitVisual = currentTime.plusSeconds(secondsToAdd);

                System.out.printf("%-7s\t%-20s\t%-20s\t%-6s\t%7.1f km\t%8s\t%8s\t%10.0f/%-3.0f%n",
                        segment.getSegmentId(),
                        segment.getStartFacilityName().substring(0, Math.min(segment.getStartFacilityName().length(), 16)),
                        segment.getEndFacilityName().substring(0, Math.min(segment.getEndFacilityName().length(), 16)),
                        segment.getSegment().getNumberTracks() > 1 ? "Double" : "Single",
                        segment.getSegment().getComprimento(),
                        entryVisual.toLocalTime().format(timeFormatter),
                        exitVisual.toLocalTime().format(timeFormatter),
                        segment.getCalculatedSpeedKmh(),
                        segment.getSegment().getVelocidadeMaxima()
                );

                currentTime = exitVisual;
            }
            System.out.println("-".repeat(95));
        }
    }

    /**
     * Prints conflict analysis report showing resolved scheduling conflicts.
     */
    private void printConflictReport(List<String> conflictReport) {
        System.out.println("\n" + ANSI_BOLD + ANSI_RED + "==========================================================" + ANSI_RESET);
        System.out.println(ANSI_BOLD + "           CONFLICT & CROSSING ANALYSIS " + ANSI_RESET);
        System.out.println(ANSI_BOLD + ANSI_RED + "==========================================================" + ANSI_RESET);

        if (conflictReport.isEmpty()) {
            System.out.println(ANSI_GREEN + "No single-track conflicts detected in the current schedule." + ANSI_RESET);
        } else {
            int numConflicts = conflictReport.size();
            System.out.printf(ANSI_RED + "Found %d conflict event(s) (All conflicts were resolved by delay):%n" + ANSI_RESET, numConflicts);
            for (String reportLine : conflictReport) {
                System.out.println(reportLine);
            }
        }
    }

    /**
     * Handles spatial queries menu and operations.
     */
    private void handleSpatialQueries() {
        showInfo("--- [USEI08] Spatial Queries - Search by Geographical Area ---");

        boolean back = false;
        while (!back) {
            System.out.println("\n" + ANSI_BOLD + "Spatial Queries Menu:" + ANSI_RESET);
            System.out.println(ANSI_GREEN + "1. " + ANSI_RESET + "Search stations in geographical area");
            System.out.println(ANSI_GREEN + "2. " + ANSI_RESET + "Execute demo queries");
            System.out.println(ANSI_GREEN + "3. " + ANSI_RESET + "Show KD-Tree statistics");
            System.out.println(ANSI_YELLOW + "0. " + ANSI_RESET + "Back to main menu");

            int choice = readInt(0, 3, ANSI_BOLD + "Choose option: " + ANSI_RESET);

            switch (choice) {
                case 1:
                    executeSpatialSearch();
                    break;
                case 2:
                    executeDemoQueries();
                    break;
                case 3:
                    showKDTreeStats();
                    break;
                case 0:
                    back = true;
                    break;
                default:
                    showError("Invalid option!");
            }
        }
    }

    /**
     * Handles nearest neighbor proximity search.
     */
    private void handleNearestNQuery() {
        showInfo("--- [USEI09] Proximity Search (Nearest-N with Filters) ---");

        try {
            System.out.println(ANSI_ITALIC + "Enter Target Coordinates (Haversine distance will be used):" + ANSI_RESET);
            double targetLat = readDouble("Target Latitude [-90 to 90]: ", -90.0, 90.0);
            double targetLon = readDouble("Target Longitude [-180 to 180]: ", -180.0, 180.0);

            int N = readInt(1, 100, ANSI_BOLD + "Enter N (Number of nearest stations, max 100): " + ANSI_RESET);

            String timeZoneFilter = readString("Time Zone Group filter (e.g., CET, EET, or press Enter for ANY): ");
            String filter = timeZoneFilter.isEmpty() ? null : timeZoneFilter.toUpperCase();

            showInfo(String.format("Executing Nearest-N search for N=%d...", N));
            long startTime = System.nanoTime();

            List<EuropeanStation> results = spatialKDTree.findNearestN(
                    targetLat, targetLon, N, filter
            );

            long endTime = System.nanoTime();

            System.out.printf("\n" + ANSI_BOLD + "Found %d nearest stations (%.2f ms)%n" + ANSI_RESET,
                    results.size(), (endTime - startTime) / 1_000_000.0);

            if (results.isEmpty()) {
                showInfo("No stations found matching the criteria.");
            } else {
                System.out.println("\n" + ANSI_BOLD + "--- TOP " + results.size() + " NEAREST STATIONS (Haversine Distance) ---" + ANSI_RESET);

                int i = 1;
                for (EuropeanStation s : results) {
                    double distance = GeoDistance.haversine(targetLat, targetLon, s.getLatitude(), s.getLongitude());
                    System.out.printf("%s %2d. %s | Distance: %s%.2f km%s %n",
                            ANSI_CYAN, i++, formatStationDisplay(s), ANSI_YELLOW, distance, ANSI_RESET);
                }
            }

        } catch (Exception e) {
            showError("Error executing Nearest-N search (USEI09): " + e.getMessage());
        }
    }

    /**
     * Reads a double value from user input with validation.
     */
    private double readDouble(String prompt, double min, double max) {
        System.out.print(prompt);
        while (true) {
            try {
                String line = scanner.nextLine();
                if (isCancel(line)) {
                    return 0.0;
                }
                double value = Double.parseDouble(line.replace(',', '.'));
                if (value >= min && value <= max) {
                    return value;
                } else {
                    System.out.print(ANSI_RED + String.format("Invalid input. Please enter a value between %.2f and %.2f.%n" + ANSI_RESET + prompt, min, max));
                }
            } catch (NumberFormatException e) {
                System.out.print(ANSI_RED + "Invalid input. Please enter a valid number." + ANSI_RESET + "\n" + prompt);
            }
        }
    }

    /**
     * Executes spatial search based on user input parameters.
     */
    private void executeSpatialSearch() {
        try {
            System.out.println("\n" + ANSI_BOLD + "--- Search Stations in Geographical Area ---" + ANSI_RESET);

            System.out.println(ANSI_ITALIC + "Enter geographical boundaries:" + ANSI_RESET);
            double latMin = readDouble("Minimum latitude [-90 to 90]: ", -90.0, 90.0);
            double latMax = readDouble("Maximum latitude [-90 to 90]: ", -90.0, 90.0);
            double lonMin = readDouble("Minimum longitude [-180 to 180]: ", -180.0, 180.0);
            double lonMax = readDouble("Maximum longitude [-180 to 180]: ", -180.0, 180.0);

            if (latMin > latMax || lonMin > lonMax) {
                showError("Invalid boundaries: min cannot be greater than max.");
                return;
            }

            System.out.println("\n" + ANSI_ITALIC + "Filters (press Enter to skip):" + ANSI_RESET);
            String country = readString("Country code (e.g., PT, ES, FR): ");
            String cityFilter = readString("City stations only? (true/false/any): ");
            String mainFilter = readString("Main stations only? (true/false/any): ");

            Boolean isCity = parseOptionalBoolean(cityFilter);
            Boolean isMain = parseOptionalBoolean(mainFilter);

            showInfo("Executing spatial search with USEI08 engine...");
            long startTime = System.nanoTime();

            List<EuropeanStation> results = spatialSearchEngine.searchByGeographicalArea(
                    latMin, latMax, lonMin, lonMax,
                    country.isEmpty() ? null : country.toUpperCase(),
                    isCity, isMain
            );

            long endTime = System.nanoTime();

            System.out.printf("\n" + ANSI_BOLD + "Found %d stations (%.2f ms)%n" + ANSI_RESET,
                    results.size(), (endTime - startTime) / 1_000_000.0);

            if (results.isEmpty()) {
                showInfo("No stations found matching the criteria.");
            } else {
                System.out.println("\n" + ANSI_BOLD + "First 10 results:" + ANSI_RESET);
                results.stream().limit(10).forEach(station ->
                        System.out.println("  • " + formatStationDisplay(station))
                );

                if (results.size() > 10) {
                    System.out.println("  ... and " + (results.size() - 10) + " more");
                }

                String seeAll = readString("\nShow all results? (y/N): ");
                if (seeAll.trim().equalsIgnoreCase("y")) {
                    showPaginatedResults(results);
                }
            }

        } catch (Exception e) {
            showError("Error executing spatial search: " + e.getMessage());
        }
    }

    /**
     * Executes the 5 required demo queries using SpatialSearchQueries class.
     */
    private void executeDemoQueries() {
        System.out.println("\n" + ANSI_BOLD + "--- USEI08 - 5 Required Demo Queries ---" + ANSI_RESET);

        try {
            SpatialSearchQueries queries = new SpatialSearchQueries(spatialSearchEngine);
            System.out.println(ANSI_ITALIC + "Executing 5 predefined spatial queries as required..." + ANSI_RESET);

            List<SpatialSearchQueries.QueryResult> results = queries.executeAllDemoQueries();

            System.out.println("\n" + ANSI_BOLD + "QUERY RESULTS:" + ANSI_RESET);
            for (SpatialSearchQueries.QueryResult result : results) {
                System.out.printf("• %s\n", result.toString());
            }

            System.out.println("\n" + ANSI_BOLD + "PERFORMANCE REPORT:" + ANSI_RESET);
            System.out.println(queries.generatePerformanceReport());

            System.out.println(ANSI_BOLD + "SAMPLE STATIONS:" + ANSI_RESET);
            System.out.println(queries.getQuerySamples());

            showSuccess("5 required demo queries completed successfully!");

        } catch (Exception e) {
            showError("Error in demo queries: " + e.getMessage());
        }
    }

    /**
     * Displays KD-Tree statistics and performance information.
     */
    private void showKDTreeStats() {
        System.out.println("\n" + ANSI_BOLD + "--- KD-Tree & USEI08 Statistics ---" + ANSI_RESET);

        KDTree tree = spatialSearchEngine.kdTree();

        System.out.println("KD-Tree Properties:");
        System.out.println("  • Size: " + ANSI_CYAN + tree.size() + ANSI_RESET + " nodes");
        System.out.println("  • Height: " + ANSI_CYAN + tree.height() + ANSI_RESET);
        System.out.println("  • Bucket distribution: " + ANSI_CYAN + tree.getBucketSizes() + ANSI_RESET);

        System.out.println("\n" + ANSI_BOLD + "USEI08 Performance Analysis:" + ANSI_RESET);
        System.out.println(spatialSearchEngine.getComplexityAnalysis());
    }

    /**
     * Handles wagon unloading operations.
     */
    private void handleUnloadWagons() {
        showInfo("--- [USEI01] Unload Wagons ---");
        System.out.println(" 1. Unload ALL wagons");
        System.out.println(" 2. Select wagons manually");
        System.out.println(ANSI_YELLOW + " 0. Cancel" + ANSI_RESET);

        int sub = readInt(0, 2, "Option: ");

        if (sub == 1) {
            wms.unloadWagons(wagons);
            showSuccess("All wagons have been processed.");
        } else if (sub == 2) {
            for (int i = 0; i < wagons.size(); i++) {
                System.out.printf(" %d. Wagon %s (%d boxes)%n",
                        i + 1, wagons.get(i).getWagonId(), wagons.get(i).getBoxes().size());
            }
            String choicesStr = readString("Enter wagon numbers (comma-separated) [c=Cancel]: ");
            if (isCancel(choicesStr)) {
                showInfo("Unloading cancelled.");
                return;
            }

            String[] choices = choicesStr.split(",");
            List<Wagon> selected = new ArrayList<>();
            for (String c : choices) {
                try {
                    int idx = Integer.parseInt(c.trim()) - 1;
                    if (idx >= 0 && idx < wagons.size()) {
                        selected.add(wagons.get(idx));
                    }
                } catch (NumberFormatException ignored) {}
            }
            wms.unloadWagons(selected);
            showSuccess("Selected wagons have been processed.");
        } else {
            showInfo("Unloading cancelled.");
        }
    }

    /**
     * Handles quarantine returns processing.
     */
    private void handleProcessReturns() {
        showInfo("--- [USEI05] Process Quarantine Returns (LIFO) ---");
        wms.processReturns();
        showSuccess("Return processing complete.");
        showInfo("Check 'audit.log' for details.");
    }

    /**
     * Handles order allocation operations.
     */
    private void handleAllocateOrders() {
        showInfo("--- [USEI02] Allocate Open Orders ---");

        List<Order> orders;
        try {
            orders = manager.loadOrders(
                    "src/main/java/pt/ipp.isep.dei/FicheirosCSV/orders.csv",
                    "src/main/java/pt/ipp.isep.dei/FicheirosCSV/order_lines.csv"
            );
        } catch (Exception e) {
            showError("Failed to load orders: " + e.getMessage());
            return;
        }

        List<Box> currentInventoryState = new ArrayList<>(manager.getInventory().getBoxes());

        if (orders.isEmpty()) {
            showError("No valid orders found to process.");
            return;
        }
        if (currentInventoryState.isEmpty()) {
            showError("Inventory is empty. Cannot allocate orders.");
            return;
        }

        System.out.printf("Data loaded: %d orders, %d boxes in inventory%n",
                orders.size(), currentInventoryState.size());

        System.out.println("\nSelect Allocation Mode:");
        System.out.println(ANSI_GREEN + " 1. " + ANSI_RESET + "STRICT (All or nothing per line)");
        System.out.println(ANSI_GREEN + " 2. " + ANSI_RESET + "PARTIAL (Allocate available stock)");
        System.out.println(ANSI_YELLOW + " 0. " + ANSI_RESET + "Cancel");
        int modeChoice = readInt(0, 2, "Option: ");

        if (modeChoice == 0) {
            showInfo("Allocation cancelled.");
            return;
        }
        OrderAllocator.Mode mode = (modeChoice == 1) ? OrderAllocator.Mode.STRICT : OrderAllocator.Mode.PARTIAL;

        OrderAllocator allocator = new OrderAllocator();
        allocator.setItems(manager.getItemsMap());

        this.lastAllocationResult = allocator.allocateOrders(orders, currentInventoryState, mode);
        this.lastPickingPlan = null;

        showSuccess("USEI02 executed successfully!");
        System.out.printf("Results: %d allocations generated, %d lines processed%n",
                lastAllocationResult.allocations.size(), lastAllocationResult.eligibilityList.size());
    }

    /**
     * Handles trolley packing operations.
     */
    private void handlePackTrolleys() {
        showInfo("--- [USEI03] Pack Allocations into Trolleys ---");

        if (this.lastAllocationResult == null || this.lastAllocationResult.allocations.isEmpty()) {
            showError("You must run [3. USEI02] Allocate Open Orders first. No valid allocations are available to be packed.");
            return;
        }

        System.out.printf("Ready to pack %d allocations.%n", this.lastAllocationResult.allocations.size());
        double capacity = readDouble(0.1, Double.MAX_VALUE, ANSI_BOLD + "Trolley capacity (kg) [0=Cancel]: " + ANSI_RESET);
        if (capacity == 0) {
            showInfo("Packing cancelled.");
            return;
        }

        System.out.println("\nAvailable Heuristics:");
        System.out.println(ANSI_GREEN + " 1. " + ANSI_RESET + "FIRST_FIT (fastest)");
        System.out.println(ANSI_GREEN + " 2. " + ANSI_RESET + "FIRST_FIT_DECREASING (Largest first, more efficient)");
        System.out.println(ANSI_GREEN + " 3. " + ANSI_RESET + "BEST_FIT_DECREASING (Best fit, optimizes space)");
        System.out.println(ANSI_YELLOW + " 0. " + ANSI_RESET + "Cancel");
        int heuristicChoice = readInt(0, 3, ANSI_BOLD + "Choose heuristic (0-3): " + ANSI_RESET);

        HeuristicType heuristic;
        switch(heuristicChoice) {
            case 1: heuristic = HeuristicType.FIRST_FIT; break;
            case 2: heuristic = HeuristicType.FIRST_FIT_DECREASING; break;
            case 3: heuristic = HeuristicType.BEST_FIT_DECREASING; break;
            case 0:
                showInfo("Packing cancelled."); return;
            default:
                showError("Invalid choice. Using FIRST_FIT by default.");
                heuristic = HeuristicType.FIRST_FIT;
                break;
        }

        showInfo("\nExecuting USEI03...");
        PickingService service = new PickingService();
        service.setItemsMap(manager.getItemsMap());

        this.lastPickingPlan = service.generatePickingPlan(
                this.lastAllocationResult.allocations,
                capacity,
                heuristic
        );

        System.out.println("\n" + ANSI_BOLD + "=".repeat(60) + ANSI_RESET);
        System.out.println(ANSI_BOLD + "           RESULTS USEI03 - Picking Plan" + ANSI_RESET);
        System.out.println(ANSI_BOLD + "=".repeat(60) + ANSI_RESET);
        System.out.println(lastPickingPlan.getSummary());
    }

    /**
     * Handles picking path calculation.
     */
    private void handleCalculatePickingPath() {
        showInfo("--- [USEI04] Calculate Picking Path ---");

        if (this.lastPickingPlan == null) {
            showError("You must run [4. USEI03] Pack Allocations into Trolleys first. No picking plan is available to calculate a path.");
            return;
        }
        if (this.lastPickingPlan.getTotalTrolleys() == 0) {
            showInfo("The current picking plan has 0 trolleys. Nothing to calculate.");
            return;
        }

        System.out.printf("Calculating paths for %d trolleys in Plan %s...%n",
                this.lastPickingPlan.getTotalTrolleys(), this.lastPickingPlan.getId());

        PickingPathService pathService = new PickingPathService();
        try {
            Map<String, PickingPathService.PathResult> pathResults = pathService.calculatePickingPaths(this.lastPickingPlan);

            if (pathResults.isEmpty()) {
                showError("Could not calculate paths (check if picking plan has valid locations).");
            } else {
                System.out.println("\n" + ANSI_BOLD + "--- Sequencing Results (USEI04) ---" + ANSI_RESET);
                pathResults.forEach((strategyName, result) -> {
                    System.out.println("\n" + ANSI_BOLD + ANSI_CYAN + strategyName + ":" + ANSI_RESET);
                    System.out.println(result);
                    System.out.println("-".repeat(40));
                });
                showSuccess("USEI04 completed successfully!");
            }

        } catch (Exception e) {
            showError("Error calculating picking paths (USEI04): " + e.getMessage());
        }
    }

    /**
     * Handles travel time calculation.
     */
    private void handleCalculateTravelTime() {
        showInfo("--- [USLP03] Calculate TravelTime ---");
        TravelTimeUI travelTimeUI = new TravelTimeUI(travelTimeController, estacaoRepo, locomotivaRepo, this.scanner);
        travelTimeUI.run();
        showSuccess("Module [USLP03] complete.");
    }

    /**
     * Handles European station index queries.
     */
    private void handleQueryStationIndex() {
        showInfo("--- [USEI06] Advanced European Station Query ---");

        System.out.println(ANSI_BOLD + "1. Select Base Search (Time Zone):" + ANSI_RESET);
        System.out.println(ANSI_GREEN + " 1. " + ANSI_RESET + "By single Time Zone Group (e.g., CET)");
        System.out.println(ANSI_GREEN + " 2. " + ANSI_RESET + "By Time Zone Window (e.g., CET to EET)");
        System.out.println(ANSI_YELLOW + " 0. " + ANSI_RESET + "Cancel");

        int choice = readInt(0, 2, "Option: ");
        List<EuropeanStation> baseResults;

        switch (choice) {
            case 1:
                String tzg = readString(ANSI_BOLD + "Enter Time Zone Group (e.g., CET) [c=Cancel]: " + ANSI_RESET);
                if (isCancel(tzg)) {
                    showInfo("Query cancelled."); return;
                }
                baseResults = stationIndexManager.getStationsByTimeZoneGroup(tzg.toUpperCase());
                break;
            case 2:
                String tzgMin = readString(ANSI_BOLD + "Enter MINIMUM Time Zone Group [c=Cancel]: " + ANSI_RESET);
                if (isCancel(tzgMin)) {
                    showInfo("Query cancelled."); return;
                }
                String tzgMax = readString(ANSI_BOLD + "Enter MAXIMUM Time Zone Group [c=Cancel]: " + ANSI_RESET);
                if (isCancel(tzgMax)) {
                    showInfo("Query cancelled."); return;
                }
                baseResults = stationIndexManager.getStationsInTimeZoneWindow(tzgMin.toUpperCase(), tzgMax.toUpperCase());
                break;
            default:
                showInfo("Query cancelled.");
                return;
        }

        if (baseResults.isEmpty()) {
            showInfo("No stations found for this time zone query. Returning to menu.");
            return;
        }

        Map<String, String> filters = new HashMap<>();
        String filterChoice;
        do {
            System.out.println(ANSI_BOLD + "\n2. Apply Advanced Filters (Optional):" + ANSI_RESET);
            System.out.printf("   %sBase results: %d stations%s%n", ANSI_CYAN, baseResults.size(), ANSI_RESET);
            System.out.println(ANSI_ITALIC + "   Current Filters:");
            System.out.println(ANSI_ITALIC + "   - Country: " + filters.getOrDefault("country", "Any"));
            System.out.println(ANSI_ITALIC + "   - Is City: " + filters.getOrDefault("isCity", "Any"));
            System.out.println(ANSI_ITALIC + "   - Is Main: " + filters.getOrDefault("isMain", "Any"));
            System.out.println(ANSI_ITALIC + "   - Is Airport: " + filters.getOrDefault("isAirport", "Any") + ANSI_RESET);

            System.out.println("\n(1) Set Country Code (e.g., PT, ES, DE)");
            System.out.println("(2) Filter by 'isCity' (True/False)");
            System.out.println("(3) Filter by 'isMainStation' (True/False)");
            System.out.println("(4) Filter by 'isAirport' (True/False)");
            System.out.println(ANSI_YELLOW + "(R) Reset all filters" + ANSI_RESET);
            System.out.println(ANSI_GREEN + "\n(S) Search & View Results" + ANSI_RESET);
            System.out.println(ANSI_YELLOW + "(C) Cancel" + ANSI_RESET);

            filterChoice = readString(ANSI_BOLD + "Choose an option [1-4, R, S, C]: " + ANSI_RESET).toUpperCase();

            switch (filterChoice) {
                case "1":
                    String country = readString("   Enter Country Code (or 'any' to clear): ");
                    if (country.equalsIgnoreCase("any")) filters.remove("country");
                    else filters.put("country", country.toUpperCase());
                    break;
                case "2":
                    String isCity = readString("   Must be a City? (T/F, or 'any' to clear): ");
                    if (isCity.equalsIgnoreCase("any")) filters.remove("isCity");
                    else filters.put("isCity", isCity.toUpperCase().startsWith("T") ? "true" : "false");
                    break;
                case "3":
                    String isMain = readString("   Must be a Main Station? (T/F, or 'any' to clear): ");
                    if (isMain.equalsIgnoreCase("any")) filters.remove("isMain");
                    else filters.put("isMain", isMain.toUpperCase().startsWith("T") ? "true" : "false");
                    break;
                case "4":
                    String isAirport = readString("   Must be an Airport? (T/F, or 'any' to clear): ");
                    if (isAirport.equalsIgnoreCase("any")) filters.remove("isAirport");
                    else filters.put("isAirport", isAirport.toUpperCase().startsWith("T") ? "true" : "false");
                    break;
                case "R":
                    filters.clear();
                    System.out.println(ANSI_YELLOW + "   All filters cleared." + ANSI_RESET);
                    break;
                case "C":
                    showInfo("Query cancelled.");
                    return;
                case "S":
                    break;
                default:
                    showError("Invalid option.");
            }
        } while (!filterChoice.equals("S"));

        showInfo("Applying filters...");
        List<EuropeanStation> filteredResults = applyAdvancedFilters(baseResults, filters);

        if (filteredResults.isEmpty()) {
            showInfo("No results found after applying filters.");
        } else {
            showPaginatedResults(filteredResults);
        }
    }

    /**
     * Handles 2D-Tree building and analysis.
     */
    private void handleBuild2DTree() {
        showInfo("--- [USEI07] Build & Analyze 2D-Tree ---");

        try {
            Map<String, Object> stats = stationIndexManager.get2DTreeStats();
            showSuccess("2D-Tree analysis complete.");

            System.out.println(ANSI_BOLD + "\n--- 2D-Tree Statistics ---" + ANSI_RESET);

            System.out.printf(ANSI_BOLD + "  Size (Nodes): %s%-10d " + ANSI_RESET,
                    ANSI_CYAN, stats.get("size"));
            System.out.printf(ANSI_BOLD + "Height: %s%d%n" + ANSI_RESET,
                    ANSI_CYAN, stats.get("height"));

            @SuppressWarnings("unchecked")
            Map<Integer, Integer> buckets = (Map<Integer, Integer>) stats.get("bucketSizes");

            System.out.println(ANSI_BOLD + "  Node Capacity (Stations per Node):" + ANSI_RESET);

            buckets.entrySet().stream()
                    .sorted(Map.Entry.comparingByKey())
                    .forEach(entry -> System.out.printf(
                            "    - %d station(s)/node : %s%d nodes%s%n",
                            entry.getKey(),
                            ANSI_CYAN, entry.getValue(), ANSI_RESET
                    ));

            System.out.println(ANSI_BOLD + "\n--- Build Analysis ---" + ANSI_RESET);
            System.out.println(ANSI_BOLD + "  Strategy:    " + ANSI_ITALIC + "Balanced build using pre-sorted lists (from USEI06)." + ANSI_RESET);
            System.out.println(ANSI_BOLD + "  Complexity:  " + ANSI_CYAN + "O(N log N)" + ANSI_RESET);

        } catch (Exception e) {
            showError("Failed to build or analyze the 2D-Tree (USEI07): " + e.getMessage());
        }
    }

    /**
     * Handles inventory viewing.
     */
    private void handleViewInventory() {
        showInfo("--- Current Inventory Contents ---");
        List<Box> boxes = manager.getInventory().getBoxes();

        if (boxes.isEmpty()) {
            showInfo("Inventory is empty.");
        } else {
            System.out.printf(ANSI_BOLD + "Displaying %d boxes (Sorted by FEFO/FIFO):%n" + ANSI_RESET, boxes.size());

            System.out.println(ANSI_BOLD + ANSI_PURPLE + "=".repeat(84) + ANSI_RESET);
            System.out.printf(ANSI_BOLD +
                            "  %-10s | %-12s | %-4s | %-12s | %-18s | %-10s %n",
                    "BOX ID", "SKU", "QTY", "EXPIRY", "RECEIVED", "LOCATION"
            );
            System.out.println(ANSI_BOLD + ANSI_PURPLE + "-".repeat(84) + ANSI_RESET);

            for (Box b : boxes) {
                System.out.println(b.toString());
            }
            System.out.println(ANSI_BOLD + ANSI_PURPLE + "=".repeat(84) + ANSI_RESET);
        }
    }

    /**
     * Handles warehouse information display.
     */
    private void handleViewWarehouseInfo() {
        showInfo("--- Warehouse Information ---");
        List<Warehouse> warehouses = manager.getWarehouses();
        if (warehouses.isEmpty()) {
            showInfo("No warehouses loaded.");
            return;
        }

        for (Warehouse wh : warehouses) {
            System.out.printf(ANSI_BOLD + "\nWarehouse: %s%n" + ANSI_RESET, wh.getWarehouseId());
            System.out.printf("   Bays: %d%n", wh.getBays().size());

            int totalCapacity = 0;
            int usedCapacity = 0;
            for (Bay bay : wh.getBays()) {
                totalCapacity += bay.getCapacityBoxes();
                usedCapacity += bay.getBoxes().size();
            }

            double percentage = (totalCapacity > 0 ? (usedCapacity * 100.0 / totalCapacity) : 0);
            String color = percentage > 85 ? ANSI_RED : (percentage > 60 ? ANSI_YELLOW : ANSI_GREEN);

            System.out.printf("   Physical Capacity: " + color + "%d/%d boxes (%.1f%% full)" + ANSI_RESET + "%n",
                    usedCapacity, totalCapacity, percentage);
            System.out.printf("   Logical Inventory Size (Total): %d boxes%n", manager.getInventory().getBoxes().size());

            String details = readString(ANSI_ITALIC + "   View bay details for this warehouse? (y/N): " + ANSI_RESET);
            if (details.trim().equalsIgnoreCase("y")) {
                printBayDetails(wh);
            }
        }
    }

    /**
     * Handles Radius Search and Density Summary operations.
     */
    private void handleRadiusSearch() {
        showInfo("--- [USEI10] Radius Search & Density Summary ---");

        try {
            System.out.println(ANSI_ITALIC + "Enter Target Coordinates:" + ANSI_RESET);
            double targetLat = readDouble("Target Latitude [-90 to 90]: ", -90.0, 90.0);
            double targetLon = readDouble("Target Longitude [-180 to 180]: ", -180.0, 180.0);

            double radiusKm = readDouble(0.1, 1000.0, ANSI_BOLD + "Search radius (km, 0.1-1000): " + ANSI_RESET);

            RadiusSearch radiusSearch = new RadiusSearch(spatialKDTree);

            showInfo(String.format("Executing radius search within %.1f km...", radiusKm));
            long startTime = System.nanoTime();

            Object[] results = radiusSearch.radiusSearchWithSummary(targetLat, targetLon, radiusKm);
            @SuppressWarnings("unchecked")
            BST<StationDistance, StationDistance> stationsTree = (BST<StationDistance, StationDistance>) results[0];
            DensitySummary summary = (DensitySummary) results[1];

            long endTime = System.nanoTime();
            double executionTimeMs = (endTime - startTime) / 1_000_000.0;

            System.out.printf("\n" + ANSI_BOLD + "USEI10 Results (%.2f ms)%n" + ANSI_RESET, executionTimeMs);

            System.out.println(summary.getFormattedSummary());

            List<StationDistance> stations = stationsTree.inOrderTraversal();
            if (!stations.isEmpty()) {
                System.out.println(ANSI_BOLD + "--- STATIONS ORDERED BY DISTANCE ---" + ANSI_RESET);

                int displayLimit = Math.min(10, stations.size());
                for (int i = 0; i < displayLimit; i++) {
                    StationDistance sd = stations.get(i);
                    System.out.printf("%s%2d.%s %s %s(%.2f km)%s%n",
                            ANSI_CYAN, i + 1, ANSI_RESET,
                            formatStationDisplay(sd.getStation()),
                            ANSI_YELLOW, sd.getDistanceKm(), ANSI_RESET);
                }

                if (stations.size() > displayLimit) {
                    System.out.printf(ANSI_ITALIC + "   ... and %d more stations%s%n",
                            stations.size() - displayLimit, ANSI_RESET);
                }
            } else {
                showInfo("No stations found within the specified radius.");
            }

            System.out.println("\n" + ANSI_BOLD + "Additional Options:" + ANSI_RESET);
            System.out.println(ANSI_GREEN + "1. " + ANSI_RESET + "Execute another radius search");
            System.out.println(ANSI_GREEN + "2. " + ANSI_RESET + "Run demo queries");
            System.out.println(ANSI_YELLOW + "0. " + ANSI_RESET + "Back to main menu");

            int choice = readInt(0, 2, ANSI_BOLD + "Choose option: " + ANSI_RESET);

            switch (choice) {
                case 1:
                    handleRadiusSearch();
                    break;
                case 2:
                    executeUSEI10DemoQueries(radiusSearch);
                    break;
                case 0:
                    break;
            }

        } catch (Exception e) {
            showError("Error executing radius search (USEI10): " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Executes predefined demo queries for radius search.
     */
    private void executeUSEI10DemoQueries(RadiusSearch radiusSearch) {
        showInfo("--- USEI10 Demo Queries ---");

        Object[][] demoQueries = {
                {"Paris, France", 48.8566, 2.3522, 50.0},
                {"Lisbon, Portugal", 38.7223, -9.1393, 30.0},
                {"Madrid, Spain", 40.4168, -3.7038, 40.0},
                {"Berlin, Germany", 52.5200, 13.4050, 25.0},
                {"Rome, Italy", 41.9028, 12.4964, 35.0}
        };

        System.out.println(ANSI_BOLD + "Executing 5 demo radius searches..." + ANSI_RESET);

        for (Object[] query : demoQueries) {
            String location = (String) query[0];
            double lat = (Double) query[1];
            double lon = (Double) query[2];
            double radius = (Double) query[3];

            System.out.println("\n" + ANSI_BOLD + location + ANSI_RESET);
            System.out.println("Coordinates: (" + lat + ", " + lon + ")");
            System.out.println("Radius: " + radius + " km");

            long startTime = System.nanoTime();
            Object[] results = radiusSearch.radiusSearchWithSummary(lat, lon, radius);
            long endTime = System.nanoTime();

            DensitySummary summary = (DensitySummary) results[1];
            @SuppressWarnings("unchecked")
            List<StationDistance> stations = ((BST<StationDistance, StationDistance>) results[0]).inOrderTraversal();

            System.out.printf("Time: %.2f ms | ", (endTime - startTime) / 1_000_000.0);
            System.out.printf("Found: %d stations | ", stations.size());
            System.out.printf("Cities: %d | ", summary.getStationsByCityType().getOrDefault(true, 0));
            System.out.printf("Main: %d%n", summary.getStationsByMainStation().getOrDefault(true, 0));

            if (!stations.isEmpty()) {
                System.out.print("   Closest: ");
                for (int i = 0; i < Math.min(3, stations.size()); i++) {
                    StationDistance sd = stations.get(i);
                    System.out.printf("%s (%.1f km) | ",
                            sd.getStation().getStation(), sd.getDistanceKm());
                }
                System.out.println();
            }

            System.out.println("─".repeat(60));
        }

        showSuccess("USEI10 demo queries completed!");
    }

    /**
     * Displays a success message.
     */
    private void showSuccess(String message) {
        System.out.println(ANSI_GREEN + ANSI_BOLD + "\nSUCCESS: " + ANSI_RESET + ANSI_GREEN + message + ANSI_RESET);
    }

    /**
     * Displays an error message.
     */
    private void showError(String message) {
        System.out.println(ANSI_RED + ANSI_BOLD + "\nERROR: " + ANSI_RESET + ANSI_RED + message + ANSI_RESET);
    }

    /**
     * Displays an informational message.
     */
    private void showInfo(String message) {
        System.out.println(ANSI_CYAN + "\n " + message + ANSI_RESET);
    }

    /**
     * Prompts user to press ENTER to continue.
     */
    private void promptEnterKey() {
        System.out.print(ANSI_ITALIC + "\n(Press ENTER to return to the menu...)" + ANSI_RESET);
        scanner.nextLine();
    }

    /**
     * Formats a station for display with icons and coordinates.
     */
    private String formatStationDisplay(EuropeanStation station) {
        return station.getStation() +
                " [" + station.getCountry() + "] " +
                "(" + String.format("%.6f", station.getLatitude()) + ", " +
                String.format("%.6f", station.getLongitude()) + ")" +
                (station.isCity() ? " " : "") +
                (station.isMainStation() ? " " : "");
    }

    /**
     * Parses optional boolean input from user.
     */
    private Boolean parseOptionalBoolean(String input) {
        if (input == null || input.trim().isEmpty() || input.equalsIgnoreCase("any")) {
            return null;
        }
        return input.trim().equalsIgnoreCase("true") || input.trim().equalsIgnoreCase("t");
    }

    /**
     * Prints a detailed table of bay details for a warehouse.
     */
    private void printBayDetails(Warehouse wh) {
        System.out.println(ANSI_BOLD + "\n   --- Bay Details for Warehouse " + wh.getWarehouseId() + " ---" + ANSI_RESET);

        System.out.printf("   %-10s | %-10s | %-18s | %s%n", "AISLE", "BAY", "CAPACITY (USED/MAX)", "VISUAL");
        System.out.println(ANSI_PURPLE + "   " + "-".repeat(60) + ANSI_RESET);

        List<Bay> sortedBays = wh.getBays().stream()
                .sorted(Comparator.comparing(Bay::getAisle).thenComparing(Bay::getBay))
                .toList();

        for (Bay bay : sortedBays) {
            int used = bay.getBoxes().size();
            int max = bay.getCapacityBoxes();
            double percentage = (max > 0) ? ((double) used / max) : 0;

            String color = (used == max) ? ANSI_RED : (used > 0 ? ANSI_GREEN : ANSI_RESET);

            int barWidth = 10;
            int filled = (int) (percentage * barWidth);
            int empty = barWidth - filled;
            String visualBar = String.format("[%s%s%s]",
                    color, "#".repeat(filled), " ".repeat(empty) + ANSI_RESET);

            System.out.printf(
                    "   %-10s | %-10s | %s%2d / %-2d boxes%s     | %s%n",
                    bay.getAisle(),
                    bay.getBay(),
                    color, used, max, ANSI_RESET,
                    visualBar
            );
        }
        System.out.println(ANSI_PURPLE + "   " + "-".repeat(60) + ANSI_RESET);
    }

    /**
     * Helper method to filter a list of stations based on the advanced filters.
     */
    private List<EuropeanStation> applyAdvancedFilters(List<EuropeanStation> stations, Map<String, String> filters) {
        if (filters.isEmpty()) {
            return stations;
        }

        return stations.stream()
                .filter(s -> {
                    if (!filters.containsKey("country")) return true;
                    return s.getCountry().equalsIgnoreCase(filters.get("country"));
                })
                .filter(s -> {
                    if (!filters.containsKey("isCity")) return true;
                    boolean mustBeCity = Boolean.parseBoolean(filters.get("isCity"));
                    return s.isCity() == mustBeCity;
                })
                .filter(s -> {
                    if (!filters.containsKey("isMain")) return true;
                    boolean mustBeMain = Boolean.parseBoolean(filters.get("isMain"));
                    return s.isMainStation() == mustBeMain;
                })
                .filter(s -> {
                    if (!filters.containsKey("isAirport")) return true;
                    boolean mustBeAirport = Boolean.parseBoolean(filters.get("isAirport"));
                    return s.isAirport() == mustBeAirport;
                })
                .sorted(Comparator.comparing(EuropeanStation::getCountry)
                        .thenComparing(EuropeanStation::getStation))
                .toList();
    }

    /**
     * Displays a list of stations in a user-friendly, paginated view.
     */
    private void showPaginatedResults(List<EuropeanStation> results) {
        int pageSize = 10;
        int totalResults = results.size();
        int totalPages = (int) Math.ceil((double) totalResults / pageSize);
        int currentPage = 0;
        String input;

        do {
            System.out.print("\n\n\n\n\n\n\n\n\n\n\n\n");

            System.out.println(ANSI_BOLD + ANSI_BLUE + "==========================================================" + ANSI_RESET);
            System.out.printf(ANSI_BOLD + ANSI_BLUE + "         Station Query Results (Page %d of %d)        %n", currentPage + 1, totalPages);
            System.out.println(ANSI_BOLD + ANSI_BLUE + "==========================================================" + ANSI_RESET);

            int startIndex = currentPage * pageSize;
            int endIndex = Math.min(startIndex + pageSize, totalResults);

            System.out.printf(ANSI_BOLD + "Showing results %d-%d of %d%n\n" + ANSI_RESET, startIndex + 1, endIndex, totalResults);

            System.out.printf(ANSI_BOLD + "  %-30s | %-7s | %-5s | %-5s | %-5s | %-15s %n",
                    "STATION NAME", "COUNTRY", "CITY?", "MAIN?", "AIR?", "TIME ZONE");
            System.out.println(ANSI_BOLD + ANSI_PURPLE + "-".repeat(82) + ANSI_RESET);

            for (int i = startIndex; i < endIndex; i++) {
                EuropeanStation s = results.get(i);
                System.out.printf("  %-30s | %s%-7s%s | %-5s | %-5s | %-5s | %-15s %n",
                        s.getStation().length() > 29 ? s.getStation().substring(0, 27) + "..." : s.getStation(),
                        ANSI_CYAN, s.getCountry(), ANSI_RESET,
                        s.isCity() ? "Yes" : "No",
                        s.isMainStation() ? "Yes" : "No",
                        s.isAirport() ? "Yes" : "No",
                        s.getTimeZoneGroup()
                );
            }
            System.out.println(ANSI_BOLD + ANSI_PURPLE + "-".repeat(82) + ANSI_RESET);

            System.out.println("\n" + ANSI_BOLD + "Controls:" + ANSI_RESET);
            String prev = (currentPage > 0) ? "[P]rev Page" : "           ";
            String next = (currentPage < totalPages - 1) ? "[N]ext Page" : "           ";
            System.out.printf("  %s   |   %s   |   [E]xit Query%n", prev, next);

            input = readString(ANSI_BOLD + "Choose an option: " + ANSI_RESET).toUpperCase();

            if (input.equals("N") && currentPage < totalPages - 1) {
                currentPage++;
            } else if (input.equals("P") && currentPage > 0) {
                currentPage--;
            }

        } while (!input.equals("E"));

        showInfo("Exited query view.");
    }

    /**
     * Reads a string input from the user.
     */
    private String readString(String prompt) {
        System.out.print(prompt);
        return scanner.nextLine();
    }

    /**
     * Reads an integer input from the user within a specified range.
     */
    private int readInt(int min, int max, String prompt) {
        System.out.print(prompt);
        while (true) {
            try {
                String line = scanner.nextLine();
                if (isCancel(line)) {
                    return 0;
                }
                int option = Integer.parseInt(line);
                if (option >= min && option <= max) {
                    return option;
                } else {
                    System.out.print(ANSI_RED + String.format("Invalid input. Please enter a number between %d and %d.%n" + ANSI_RESET + prompt, min, max));
                }
            } catch (NumberFormatException e) {
                System.out.print(ANSI_RED + "Invalid input. Please enter a number." + ANSI_RESET + "\n" + prompt);
            }
        }
    }

    /**
     * Reads a double input from the user within a specified range.
     */
    private double readDouble(double min, double max, String prompt) {
        System.out.print(prompt);
        while (true) {
            try {
                String line = scanner.nextLine();
                if (isCancel(line)) {
                    return 0.0;
                }
                double value = Double.parseDouble(line.replace(',', '.'));
                if (value >= min && value <= max) {
                    return value;
                } else {
                    System.out.print(ANSI_RED + String.format("Invalid input. Please enter a value between %.2f and %.2f.%n" + ANSI_RESET + prompt, min, max));
                }
            } catch (NumberFormatException e) {
                System.out.print(ANSI_RED + "Invalid input. Please enter a valid number." + ANSI_RESET + "\n" + prompt);
            }
        }
    }

    /**
     * Overloaded version of readDouble with default range for coordinates.
     */
    private double readDouble(String prompt) {
        return readDouble(-Double.MAX_VALUE, Double.MAX_VALUE, prompt);
    }

    /**
     * Checks if the input indicates cancellation.
     */
    private boolean isCancel(String input) {
        return input.trim().equals("0") || input.trim().equalsIgnoreCase("c");
    }
}
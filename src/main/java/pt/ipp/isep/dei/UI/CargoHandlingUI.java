package pt.ipp.isep.dei.UI;

import pt.ipp.isep.dei.controller.TravelTimeController;
import pt.ipp.isep.dei.controller.SchedulerController;
import pt.ipp.isep.dei.domain.*; // Importa RailwayFlowService e outros
import pt.ipp.isep.dei.repository.StationRepository;
import pt.ipp.isep.dei.repository.LocomotiveRepository;
import pt.ipp.isep.dei.repository.TrainRepository;
import pt.ipp.isep.dei.repository.FacilityRepository;

import java.io.BufferedReader;
import java.io.FileReader;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class CargoHandlingUI implements Runnable {

    public static final String ANSI_RESET = "\u001B[0m";
    public static final String ANSI_RED = "\u001B[31m";
    public static final String ANSI_GREEN = "\u001B[32m";
    public static final String ANSI_YELLOW = "\u001B[33m";
    public static final String ANSI_BLUE = "\u001B[34m";
    public static final String ANSI_PURPLE = "\u001B[35m";
    public static final String ANSI_CYAN = "\u001B[36m";
    public static final String ANSI_BOLD = "\u001B[1m";
    public static final String ANSI_ITALIC = "\u001B[3m";

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

    // --- ALTERAÇÃO: Usamos o RailwayFlowService dedicado à US14 ---
    private final RailwayFlowService flowService;

    private AllocationResult lastAllocationResult = null;
    private PickingPlan lastPickingPlan = null;
    private final Scanner scanner;

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

        // --- INICIALIZAÇÃO DO SERVIÇO DA US14 ---
        this.flowService = new RailwayFlowService();
    }

    @Override
    public void run() {
        int option = -1;
        do {
            showMenu();
            try {
                option = readInt(0, 17, ANSI_BOLD + "Option: " + ANSI_RESET);
                handleOption(option);
            } catch (InputMismatchException e) {
                showError("Invalid input. Please enter a number.");
                scanner.nextLine();
            } catch (Exception e) {
                showError("Fatal UI Error: " + e.getMessage());
                option = 0;
            }
            if (option != 0) promptEnterKey();
        } while (option != 0);
        scanner.close();
    }

    private void showMenu() {
        System.out.print("\n\n\n\n\n");
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
        System.out.println(ANSI_GREEN + "12. " + ANSI_RESET + "[USEI12] Minimal Backbone Network (S3)");
        System.out.println(ANSI_GREEN + "13. " + ANSI_RESET + "[USLP07] Run Full Simulation & Conflicts (S3)");
        System.out.println(ANSI_GREEN + "17. " + ANSI_RESET + "[USEI13] Rail Hub Centrality Analysis (S3)");
        System.out.println(ANSI_GREEN + "14. " + ANSI_RESET + "[USEI14] Max Throughput Analysis (Edmonds-Karp) (S3)");
        System.out.println("\n" + ANSI_BOLD + ANSI_PURPLE + "--- System Information ---" + ANSI_RESET);
        System.out.println(ANSI_GREEN + "15. " + ANSI_RESET + "View Current Inventory");
        System.out.println(ANSI_GREEN + "16. " + ANSI_RESET + "View Warehouse Info");
        System.out.println("\n" + ANSI_BOLD + "----------------------------------------------------------" + ANSI_RESET);

        String allocStatus = (lastAllocationResult != null && !lastAllocationResult.allocations.isEmpty()) ?
                ANSI_GREEN + String.format("GENERATED (%d allocs)", lastAllocationResult.allocations.size()) : ANSI_YELLOW + "NOT-RUN";
        String planStatus = (lastPickingPlan != null) ?
                ANSI_GREEN + String.format("GENERATED (%d trolleys)", lastPickingPlan.getTotalTrolleys()) : ANSI_YELLOW + "NOT-RUN";

        System.out.println(ANSI_BOLD + "   Status: [Allocations: " + allocStatus + ANSI_BOLD + "] [Picking Plan: " + planStatus + ANSI_BOLD + "]" + ANSI_RESET);
        System.out.println(ANSI_YELLOW + "  0. " + ANSI_RESET + "Exit System");
        System.out.println(ANSI_BOLD + "----------------------------------------------------------" + ANSI_RESET);
    }

    private void handleOption(int option) {
        switch (option) {
            case 1: handleUnloadWagons(); break;
            case 2: handleProcessReturns(); break;
            case 3: handleAllocateOrders(); break;
            case 4: handlePackTrolleys(); break;
            case 5: handleCalculatePickingPath(); break;
            case 6: handleCalculateTravelTime(); break;
            case 7: handleQueryStationIndex(); break;
            case 8: handleBuild2DTree(); break;
            case 9: handleSpatialQueries(); break;
            case 10: handleNearestNQuery(); break;
            case 11: handleRadiusSearch(); break;
            case 12: handleUSEI12(); break;
            case 13: handleRunFullSimulation(); break;
            case 14: handleMaxThroughput(); break;
            case 15: handleViewInventory(); break;
            case 16: handleViewWarehouseInfo(); break;
            case 17: handleRailHubAnalysis(); break;
            case 0: System.out.println(ANSI_CYAN + "\nExiting Cargo Handling Menu... 👋" + ANSI_RESET); break;
            default: showError("Invalid option. Please select a valid number from the menu."); break;
        }
    }

    private void handleRailHubAnalysis() {
        try {
            System.out.println("\n" + ANSI_BOLD + ANSI_BLUE + "==========================================================" + ANSI_RESET);
            System.out.println(ANSI_BOLD + ANSI_BLUE + "      🚆 [USEI13] RAIL HUB CENTRALITY ANALYSIS 🚆      " + ANSI_RESET);
            System.out.println(ANSI_BOLD + ANSI_BLUE + "==========================================================" + ANSI_RESET);

            // 1. Caminhos dos ficheiros (ajustados para a tua estrutura de pastas)
            String stationsFile = "src/main/java/pt/ipp/isep/dei/FicheirosCSV/stations.csv";
            String linesFile = "src/main/java/pt/ipp/isep/dei/FicheirosCSV/lines.csv";

            System.out.print(ANSI_CYAN + "Loading CSV data from FicheirosCSV... " + ANSI_RESET);
            // Carrega o grafo (Certifica-te que o CSVLoader está no teu package domain)
            Graph g = CSVLoader.load(stationsFile, linesFile);
            System.out.println(ANSI_GREEN + "Done!" + ANSI_RESET);

            System.out.println(ANSI_CYAN + "Computing network metrics (this may take a moment)..." + ANSI_RESET);

            // 2. Execução dos Algoritmos (Sempre sobre a rede total para rigor matemático)
            DegreeStrength.compute(g);
            HarmonicCloseness.compute(g);
            Betweenness.compute(g);
            HubScoreCalculator.compute(g);

            // 3. Preparar e Ordenar o Ranking
            List<StationMetrics> ranking = new ArrayList<>(g.metricsMap.values());
            // Ordena por HubScore decrescente
            ranking.sort((m1, m2) -> Double.compare(m2.hubScore, m1.hubScore));

            // 4. Interação com o Utilizador: Escolher N
            System.out.println("\n" + ANSI_YELLOW + "Total de estações processadas: " + ranking.size() + ANSI_RESET);
            System.out.print(ANSI_YELLOW + "Quantas estações deseja visualizar no ranking? " + ANSI_RESET);

            // Utiliza o teu método readInt para validar a entrada
            int n = readInt(1, ranking.size(), "Introduza um número entre 1 e " + ranking.size() + ": ");

            // 5. Apresentação dos Resultados
            System.out.println("\n" + ANSI_BOLD + String.format("%-6s | %-25s | %-4s | %-8s | %-8s | %-8s",
                    "ID", "Station Name", "Deg", "Strength", "BetwN", "HubScore") + ANSI_RESET);
            System.out.println("------------------------------------------------------------------------------------");

            for (int i = 0; i < n; i++) {
                StationMetrics m = ranking.get(i);
                System.out.printf("%-6d | %-25s | %-4d | %-8.2f | %-8.4f | " + ANSI_BOLD + ANSI_GREEN + "%-8.4f" + ANSI_RESET + "%n",
                        m.getStation().idEstacao(),
                        m.getStation().nome().length() > 25 ? m.getStation().nome().substring(0, 22) + "..." : m.getStation().nome(),
                        m.degree,
                        m.strength,
                        m.betweennessNorm,
                        m.hubScore);
            }

            // 6. Requisito da US: Análise de Complexidade para o Planeador
            System.out.println("\n" + ANSI_BOLD + "--- Expected Return: Complexity Analysis ---" + ANSI_RESET);
            System.out.println(ANSI_YELLOW + "Static Network Complexity: " + ANSI_RESET + "O(V * E + V^2 log V)");
            System.out.println("   - Baseada nos algoritmos de Brandes (Betweenness) e Dijkstra (Closeness).");
            System.out.println(ANSI_YELLOW + "Temporal Analysis Complexity: " + ANSI_RESET + "O(T * (V * E + V^2 log V))");
            System.out.println("   - Em redes dinâmicas, a complexidade escala linearmente com o número de janelas temporais (T).");

        } catch (java.io.FileNotFoundException e) {
            System.out.println(ANSI_RED + "Error: CSV files not found in src/main/java/pt/ipp/isep/dei/FicheirosCSV/" + ANSI_RESET);
        } catch (Exception e) {
            System.out.println(ANSI_RED + "An error occurred: " + e.getMessage() + ANSI_RESET);
            e.printStackTrace();
        }
    }

    // --- USEI14: Max Throughput usando RailwayFlowService ---
    private void handleMaxThroughput() {
        showInfo("--- [USEI14] Maximum Throughput (Edmonds-Karp Algorithm) ---");
        System.out.println(ANSI_ITALIC + "Calculates max flow using 'stations.csv' and 'lines.csv' dataset." + ANSI_RESET);

        // 1. Carregar CSV (Usa o flowService)
        try {
            System.out.print("Loading CSV data... ");
            String stationsFile = "src/main/java/pt/ipp/isep/dei/FicheirosCSV/stations.csv";
            String linesFile = "src/main/java/pt/ipp/isep/dei/FicheirosCSV/lines.csv";

            flowService.loadGraphFromCSV(stationsFile, linesFile);

            System.out.println(ANSI_GREEN + "Done." + ANSI_RESET);
        } catch (Exception e) {
            showError("Failed to load CSV files: " + e.getMessage());
            return;
        }

        // 2. Mostrar Opções Disponíveis
        System.out.println("\n" + ANSI_BOLD + "--- Available Stations (Sorted by ID) ---" + ANSI_RESET);

        Map<Integer, String> stations = flowService.getAllCsvStations();

        if (stations.isEmpty()) {
            showError("No stations found in the loaded CSV.");
            return;
        }

        // Imprimir em colunas (3 por linha)
        int count = 0;
        for (Map.Entry<Integer, String> entry : stations.entrySet()) {
            String entryStr = String.format("[%d] %s", entry.getKey(), entry.getValue());
            if (entryStr.length() > 38) entryStr = entryStr.substring(0, 35) + "...";
            System.out.printf("%-40s", entryStr);
            count++;
            if (count % 3 == 0) System.out.println();
        }
        System.out.println("\n" + ANSI_BOLD + "-----------------------------------------" + ANSI_RESET);

        // 3. Inputs
        int sourceId = readInt(0, Integer.MAX_VALUE, ANSI_BOLD + "Enter Source Station ID: " + ANSI_RESET);

        // Verifica no flowService
        String sourceName = flowService.getStationNameById(sourceId);

        if (sourceName == null) {
            showError("Source station ID " + sourceId + " is not in the list.");
            return;
        }
        System.out.println("   Selected Source: " + ANSI_CYAN + sourceName + ANSI_RESET);

        int sinkId = readInt(0, Integer.MAX_VALUE, ANSI_BOLD + "Enter Sink Station ID: " + ANSI_RESET);

        // Verifica no flowService
        String sinkName = flowService.getStationNameById(sinkId);

        if (sinkName == null) {
            showError("Sink station ID " + sinkId + " is not in the list.");
            return;
        }
        System.out.println("   Selected Sink: " + ANSI_CYAN + sinkName + ANSI_RESET);

        if (sourceId == sinkId) {
            showError("Source and Sink cannot be the same station.");
            return;
        }

        // 4. Executar
        System.out.println("\n" + ANSI_BOLD + "Running Edmonds-Karp Max Flow..." + ANSI_RESET);
        long startTime = System.nanoTime();

        try {
            // Chama o algoritmo no flowService
            double maxFlow = flowService.maximumThroughput(sourceId, sinkId);

            long endTime = System.nanoTime();
            double durationMs = (endTime - startTime) / 1_000_000.0;

            System.out.println("\n" + ANSI_BOLD + "--- RESULT ---" + ANSI_RESET);
            System.out.printf("Maximum Throughput from %s to %s:%n", sourceName, sinkName);
            System.out.printf(ANSI_GREEN + ANSI_BOLD + "%.0f trains/day%n" + ANSI_RESET, maxFlow);
            System.out.printf(ANSI_ITALIC + "(Calculation took %.2f ms)%n" + ANSI_RESET, durationMs);

            if (maxFlow == 0) {
                System.out.println(ANSI_YELLOW + "Warning: Max flow is 0. Check if stations are connected." + ANSI_RESET);
            }

        } catch (Exception e) {
            showError("Error calculating flow: " + e.getMessage());
        }
    }

    private void handleUSEI12() {
        showInfo("--- [USEI12] Minimal Backbone Network (Belgian Railway) ---");
        try {
            BackboneNetwork backboneNetwork = new BackboneNetwork();
            System.out.println("1️⃣  Loading Belgian railway data...");
            String stationsFile = "src/main/java/pt/ipp/isep/dei/FicheirosCSV/stations.csv";
            String connectionsFile = "src/main/java/pt/ipp/isep/dei/FicheirosCSV/lines.csv";
            try (BufferedReader br = new BufferedReader(new FileReader(stationsFile))) {
                String line = br.readLine();
                int count = 0;
                while ((line = br.readLine()) != null) count++;
                System.out.println("   Found " + count + " stations in file");
            }
            backboneNetwork.loadNetwork(stationsFile, connectionsFile);
            System.out.println("2️⃣  Computing Minimum Spanning Tree...");
            backboneNetwork.computeMinimalBackbone();
            System.out.println("3️⃣  Generating visualization...");
            backboneNetwork.generateDOTFile("belgian_backbone.dot");
            boolean svgGenerated = backboneNetwork.generateSVG("belgian_backbone.dot", "belgian_backbone.svg");
            if (svgGenerated) {
                showSuccess("✅ SVG visualization generated: belgian_backbone.svg");
            } else {
                showInfo("📝 GraphViz not installed. To generate SVG manually:");
                System.out.println("   neato -Tsvg belgian_backbone.dot -o belgian_backbone.svg");
            }
            backboneNetwork.printReport();
            showSuccess("✅ USEI12 completed successfully!");
        } catch (Exception e) {
            showError("❌ Error in USEI12: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void handleRunFullSimulation() {
        showInfo("--- [USLP07] Full Simulation & Conflict Analysis ---");
        try {
            // 1. Carregar Comboios (Agora já vêm com vagões da BD!)
            List<Train> allTrains = trainRepo.findAll();

            if (allTrains.isEmpty()) {
                showError("No scheduled Trains found in the database.");
                return;
            }

            // REMOVIDO: Bloco "Mock Wagons" gigante.
            // Agora confiamos no TrainRepository e WagonRepository.

            System.out.println(ANSI_BOLD + "\n--- 1. Select Trains for Simulation ---" + ANSI_RESET);
            allTrains.forEach(t -> {
                String startName = facilityRepo.findNameById(t.getStartFacilityId()).orElse("F" + t.getStartFacilityId());
                String endName = facilityRepo.findNameById(t.getEndFacilityId()).orElse("F" + t.getEndFacilityId());

                // Mostrar info real dos vagões carregados
                int wagonCount = (t.getWagons() != null) ? t.getWagons().size() : 0;

                String status = String.format("Route: %s -> %s | Dep: %s | Loco: %s | Wagons: %d",
                        startName, endName, t.getDepartureTime().toLocalTime(), t.getLocomotiveId(), wagonCount);
                System.out.printf(ANSI_CYAN + "   [%s] %s%n" + ANSI_RESET, t.getTrainId(), status);
            });

            String trainIdsStr = readString(ANSI_BOLD + "Enter Train IDs to simulate (e.g., 5421,5437) [c=Cancel]: " + ANSI_RESET);
            if (isCancel(trainIdsStr)) {
                showInfo("Simulation cancelled by user.");
                return;
            }

            List<String> selectedIds = Arrays.stream(trainIdsStr.split(",")).map(String::trim).toList();
            List<Train> trainsToSimulate = allTrains.stream().filter(t -> selectedIds.contains(t.getTrainId())).toList();

            if (trainsToSimulate.isEmpty()) {
                showError("No valid Train IDs selected. Cannot run simulation.");
                return;
            }

            System.out.printf(ANSI_CYAN + "\nExecuting Schedule calculation for %d selected trains...%n" + ANSI_RESET, trainsToSimulate.size());

            // O resto continua igual, usando os dados reais
            SchedulerResult schedulerResult = dispatcherService.scheduleTrains(trainsToSimulate);

            Map<String, TrainTrip> scheduledTripsMap = schedulerResult.scheduledTrips.stream()
                    .collect(Collectors.toMap(TrainTrip::getTripId, trip -> trip, (existing, replacement) -> existing));

            if (scheduledTripsMap.isEmpty()) {
                showError("No valid scheduled trips could be simulated for the selected trains (check routes).");
                return;
            }

            showSuccess("Full simulation completed successfully for " + scheduledTripsMap.size() + " trains!");

            printSimulationTimetables(scheduledTripsMap, schedulerResult.resolvedConflicts);

            List<String> conflictReport = schedulerResult.resolvedConflicts.stream().map(c -> c.toString()).collect(Collectors.toList());
            printConflictReport(conflictReport);

        } catch (Exception e) {
            showError("Failed to execute full simulation (USLP07): " + e.getMessage());
            e.printStackTrace(System.err);
        }
    }
    private void printSimulationTimetables(Map<String, TrainTrip> scheduledTripsMap, List<Conflict> conflicts) {
        System.out.println("\n" + ANSI_BOLD + ANSI_BLUE + "=========================================================================================" + ANSI_RESET);
        System.out.println(ANSI_BOLD + "                            DETAILED SEGMENT TIMETABLE (SIMULATION OUTPUT) " + ANSI_RESET);
        System.out.println(ANSI_BOLD + ANSI_BLUE + "=========================================================================================" + ANSI_RESET);

        DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");

        // Mapear atrasos para injetar na tabela visualmente
        Map<String, Map<Integer, Long>> delayPoints = new HashMap<>();
        for (Conflict c : conflicts) {
            delayPoints.computeIfAbsent(c.tripId2, k -> new HashMap<>()).merge(c.getSafeWaitFacilityId(), c.delayMinutes, Long::sum);
        }

        for (Map.Entry<String, TrainTrip> entry : scheduledTripsMap.entrySet()) {
            TrainTrip trip = entry.getValue();
            String trainId = trip.getTripId();
            List<SimulationSegmentEntry> timetable = trip.getSegmentEntries();

            if (timetable.isEmpty()) continue;

            Train originalTrain = trainRepo.findById(trainId).orElse(null);
            if (originalTrain == null) continue;

            LocalDateTime initialDeparture = originalTrain.getDepartureTime();
            LocalDateTime currentTime = initialDeparture;

            // Informação da Locomotiva
            String locoInfo = "N/A";
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
            String speedDisplay = String.format("%.0f km/h", trip.getMaxTrainSpeed());

            // 1. Cabeçalho do Comboio
            System.out.printf(ANSI_BOLD + "\nTrain %s — Final Departure %s%n" + ANSI_RESET, trainId, originalDepartureStr);
            System.out.printf(ANSI_ITALIC + "   Composition: Locomotive %s | Max Calculated Speed: %s%n" + ANSI_RESET, locoInfo, speedDisplay);

            // 2. Física Condensada (Numa linha, sem espaços extra)
            if (trip.getPhysicsCalculationLog() != null && !trip.getPhysicsCalculationLog().isEmpty()) {
                System.out.println(ANSI_CYAN + trip.getPhysicsCalculationLog() + ANSI_RESET);
            }

            // 3. Manifesto de Carga (Payload) - Nova Funcionalidade
            System.out.print(ANSI_BOLD + "   [PAYLOAD] " + ANSI_RESET);
            List<Wagon> trainWagons = trip.getWagons();

            if (trainWagons == null || trainWagons.isEmpty()) {
                System.out.println("Locomotive Only (No Wagons)");
            } else {
                System.out.println(trainWagons.size() + " Wagon(s):");
                for (Wagon w : trainWagons) {
                    String cargoStr;
                    if (w.getBoxes().isEmpty()) {
                        cargoStr = ANSI_ITALIC + "Empty" + ANSI_RESET;
                    } else {
                        // Cria um resumo das caixas: "5 Boxes [SKU1, SKU2...]"
                        String items = w.getBoxes().stream()
                                .limit(5) // Limita a 5 itens para não poluir a consola
                                .map(Box::getSku)
                                .collect(Collectors.joining(", "));

                        if (w.getBoxes().size() > 5) items += ", ...";

                        cargoStr = String.format("%d Boxes [%s]", w.getBoxes().size(), items);
                    }
                    // Imprime: • Wagon 2001 -> 5 Boxes [ItemA, ItemB]
                    System.out.printf("      • Wagon %-5s -> %s%n", w.getIdWagon(), cargoStr);
                }
            }

            // 4. Cabeçalho da Tabela
            System.out.println(ANSI_BOLD + ANSI_CYAN + "ID\tFROM FACILITY\t\tTO FACILITY\t\tTYPE\tLENGTH\t\tENTRY\t\tEXIT\t\tSPEED (C/A)" + ANSI_RESET);
            System.out.println("-".repeat(95));

            // 5. Loop dos Segmentos
            for (SimulationSegmentEntry segment : timetable) {
                int startFacilityId = segment.getSegment().getIdEstacaoInicio();
                Map<Integer, Long> tripDelays = delayPoints.getOrDefault(trainId, Map.of());

                // Injetar linha de Atraso (DELAY) se existir para esta estação
                if (tripDelays.containsKey(startFacilityId)) {
                    long delay = tripDelays.get(startFacilityId);
                    String facName = segment.getStartFacilityName();

                    System.out.printf(ANSI_YELLOW + ANSI_BOLD + "DELAY\t%-20s\t%-20s\tWAIT\t      \t%4d min\t%s\t%s\t0/0%n" + ANSI_RESET,
                            facName.substring(0, Math.min(facName.length(), 18)),
                            "...", // Destino irrelevante durante espera
                            delay,
                            currentTime.toLocalTime().format(timeFormatter),
                            currentTime.plusMinutes(delay).toLocalTime().format(timeFormatter));

                    // Aplicar atraso ao tempo corrente
                    currentTime = currentTime.plusMinutes(delay);
                    delayPoints.get(trainId).remove(startFacilityId); // Consumir o atraso
                }

                // Calcular tempo visual de saída
                double segHours = segment.getSegment().getComprimento() / segment.getCalculatedSpeedKmh();
                LocalDateTime exitVisual = currentTime.plusSeconds(Math.round(segHours * 3600));

                // Imprimir linha do Segmento
                System.out.printf("%-7s\t%-20s\t%-20s\t%-6s\t%7.1f km\t%8s\t%8s\t%10.0f/%-3.0f%n",
                        segment.getSegmentId(),
                        segment.getStartFacilityName().substring(0, Math.min(segment.getStartFacilityName().length(), 18)),
                        segment.getEndFacilityName().substring(0, Math.min(segment.getEndFacilityName().length(), 18)),
                        segment.getSegment().getNumberTracks() > 1 ? "Double" : "Single",
                        segment.getSegment().getComprimento(),
                        currentTime.toLocalTime().format(timeFormatter),
                        exitVisual.toLocalTime().format(timeFormatter),
                        segment.getCalculatedSpeedKmh(),
                        segment.getSegment().getVelocidadeMaxima());

                currentTime = exitVisual;
            }
            System.out.println("-".repeat(95));
        }
    }

    private void printConflictReport(List<String> conflictReport) {
        System.out.println("\n" + ANSI_BOLD + ANSI_RED + "==========================================================" + ANSI_RESET);
        System.out.println(ANSI_BOLD + "           CONFLICT & CROSSING ANALYSIS " + ANSI_RESET);
        System.out.println(ANSI_BOLD + ANSI_RED + "==========================================================" + ANSI_RESET);
        if (conflictReport.isEmpty()) {
            System.out.println(ANSI_GREEN + "No single-track conflicts detected in the current schedule." + ANSI_RESET);
        } else {
            System.out.printf(ANSI_RED + "Found %d conflict event(s) (All conflicts were resolved by delay):%n" + ANSI_RESET, conflictReport.size());
            for (String reportLine : conflictReport) System.out.println(reportLine);
        }
    }

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
                case 1: executeSpatialSearch(); break;
                case 2: executeDemoQueries(); break;
                case 3: showKDTreeStats(); break;
                case 0: back = true; break;
                default: showError("Invalid option!");
            }
        }
    }

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
            List<EuropeanStation> results = spatialKDTree.findNearestN(targetLat, targetLon, N, filter);
            long endTime = System.nanoTime();
            System.out.printf("\n" + ANSI_BOLD + "Found %d nearest stations (%.2f ms)%n" + ANSI_RESET, results.size(), (endTime - startTime) / 1_000_000.0);
            if (results.isEmpty()) {
                showInfo("No stations found matching the criteria.");
            } else {
                System.out.println("\n" + ANSI_BOLD + "--- TOP " + results.size() + " NEAREST STATIONS (Haversine Distance) ---" + ANSI_RESET);
                int i = 1;
                for (EuropeanStation s : results) {
                    double distance = GeoDistance.haversine(targetLat, targetLon, s.getLatitude(), s.getLongitude());
                    System.out.printf("%s %2d. %s | Distance: %s%.2f km%s %n", ANSI_CYAN, i++, formatStationDisplay(s), ANSI_YELLOW, distance, ANSI_RESET);
                }
            }
        } catch (Exception e) {
            showError("Error executing Nearest-N search (USEI09): " + e.getMessage());
        }
    }

    private double readDouble(String prompt, double min, double max) {
        System.out.print(prompt);
        while (true) {
            try {
                String line = scanner.nextLine();
                if (isCancel(line)) return 0.0;
                double value = Double.parseDouble(line.replace(',', '.'));
                if (value >= min && value <= max) return value;
                else System.out.print(ANSI_RED + String.format("Invalid input. Please enter a value between %.2f and %.2f.%n" + ANSI_RESET + prompt, min, max));
            } catch (NumberFormatException e) {
                System.out.print(ANSI_RED + "Invalid input. Please enter a valid number." + ANSI_RESET + "\n" + prompt);
            }
        }
    }

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
            List<EuropeanStation> results = spatialSearchEngine.searchByGeographicalArea(latMin, latMax, lonMin, lonMax, country.isEmpty() ? null : country.toUpperCase(), isCity, isMain);
            long endTime = System.nanoTime();
            System.out.printf("\n" + ANSI_BOLD + "Found %d stations (%.2f ms)%n" + ANSI_RESET, results.size(), (endTime - startTime) / 1_000_000.0);
            if (results.isEmpty()) {
                showInfo("No stations found matching the criteria.");
            } else {
                System.out.println("\n" + ANSI_BOLD + "First 10 results:" + ANSI_RESET);
                results.stream().limit(10).forEach(station -> System.out.println("  • " + formatStationDisplay(station)));
                if (results.size() > 10) System.out.println("  ... and " + (results.size() - 10) + " more");
                String seeAll = readString("\nShow all results? (y/N): ");
                if (seeAll.trim().equalsIgnoreCase("y")) showPaginatedResults(results);
            }
        } catch (Exception e) {
            showError("Error executing spatial search: " + e.getMessage());
        }
    }

    private void executeDemoQueries() {
        System.out.println("\n" + ANSI_BOLD + "--- USEI08 - 5 Required Demo Queries ---" + ANSI_RESET);
        try {
            SpatialSearchQueries queries = new SpatialSearchQueries(spatialSearchEngine);
            System.out.println(ANSI_ITALIC + "Executing 5 predefined spatial queries as required..." + ANSI_RESET);
            List<SpatialSearchQueries.QueryResult> results = queries.executeAllDemoQueries();
            System.out.println("\n" + ANSI_BOLD + "QUERY RESULTS:" + ANSI_RESET);
            for (SpatialSearchQueries.QueryResult result : results) System.out.printf("• %s\n", result.toString());
            System.out.println("\n" + ANSI_BOLD + "PERFORMANCE REPORT:" + ANSI_RESET);
            System.out.println(queries.generatePerformanceReport());
            System.out.println(ANSI_BOLD + "SAMPLE STATIONS:" + ANSI_RESET);
            System.out.println(queries.getQuerySamples());
            showSuccess("5 required demo queries completed successfully!");
        } catch (Exception e) {
            showError("Error in demo queries: " + e.getMessage());
        }
    }

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
            for (int i = 0; i < wagons.size(); i++) System.out.printf(" %d. Wagon %s (%d boxes)%n", i + 1, wagons.get(i).getWagonId(), wagons.get(i).getBoxes().size());
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
                    if (idx >= 0 && idx < wagons.size()) selected.add(wagons.get(idx));
                } catch (NumberFormatException ignored) {}
            }
            wms.unloadWagons(selected);
            showSuccess("Selected wagons have been processed.");
        } else {
            showInfo("Unloading cancelled.");
        }
    }

    private void handleProcessReturns() {
        showInfo("--- [USEI05] Process Quarantine Returns (LIFO) ---");
        wms.processReturns();
        showSuccess("Return processing complete.");
        showInfo("Check 'audit.log' for details.");
    }

    private void handleAllocateOrders() {
        showInfo("--- [USEI02] Allocate Open Orders ---");
        List<Order> orders;
        try {
            orders = manager.loadOrders("src/main/java/pt/ipp.isep.dei/FicheirosCSV/orders.csv", "src/main/java/pt/ipp.isep.dei/FicheirosCSV/order_lines.csv");
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
        System.out.printf("Data loaded: %d orders, %d boxes in inventory%n", orders.size(), currentInventoryState.size());
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
        System.out.printf("Results: %d allocations generated, %d lines processed%n", lastAllocationResult.allocations.size(), lastAllocationResult.eligibilityList.size());
    }

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
            case 0: showInfo("Packing cancelled."); return;
            default: showError("Invalid choice. Using FIRST_FIT by default."); heuristic = HeuristicType.FIRST_FIT; break;
        }
        showInfo("\nExecuting USEI03...");
        PickingService service = new PickingService();
        service.setItemsMap(manager.getItemsMap());
        this.lastPickingPlan = service.generatePickingPlan(this.lastAllocationResult.allocations, capacity, heuristic);
        System.out.println("\n" + ANSI_BOLD + "=".repeat(60) + ANSI_RESET);
        System.out.println(ANSI_BOLD + "           RESULTS USEI03 - Picking Plan" + ANSI_RESET);
        System.out.println(ANSI_BOLD + "=".repeat(60) + ANSI_RESET);
        System.out.println(lastPickingPlan.getSummary());
    }

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
        System.out.printf("Calculating paths for %d trolleys in Plan %s...%n", this.lastPickingPlan.getTotalTrolleys(), this.lastPickingPlan.getId());
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

    private void handleCalculateTravelTime() {
        showInfo("--- [USLP03] Calculate TravelTime ---");
        TravelTimeUI travelTimeUI = new TravelTimeUI(travelTimeController, estacaoRepo, locomotivaRepo, this.scanner);
        travelTimeUI.run();
        showSuccess("Module [USLP03] complete.");
    }

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
                if (isCancel(tzg)) { showInfo("Query cancelled."); return; }
                baseResults = stationIndexManager.getStationsByTimeZoneGroup(tzg.toUpperCase());
                break;
            case 2:
                String tzgMin = readString(ANSI_BOLD + "Enter MINIMUM Time Zone Group [c=Cancel]: " + ANSI_RESET);
                if (isCancel(tzgMin)) { showInfo("Query cancelled."); return; }
                String tzgMax = readString(ANSI_BOLD + "Enter MAXIMUM Time Zone Group [c=Cancel]: " + ANSI_RESET);
                if (isCancel(tzgMax)) { showInfo("Query cancelled."); return; }
                baseResults = stationIndexManager.getStationsInTimeZoneWindow(tzgMin.toUpperCase(), tzgMax.toUpperCase());
                break;
            default: showInfo("Query cancelled."); return;
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
                case "1": String country = readString("   Enter Country Code (or 'any' to clear): "); if (country.equalsIgnoreCase("any")) filters.remove("country"); else filters.put("country", country.toUpperCase()); break;
                case "2": String isCity = readString("   Must be a City? (T/F, or 'any' to clear): "); if (isCity.equalsIgnoreCase("any")) filters.remove("isCity"); else filters.put("isCity", isCity.toUpperCase().startsWith("T") ? "true" : "false"); break;
                case "3": String isMain = readString("   Must be a Main Station? (T/F, or 'any' to clear): "); if (isMain.equalsIgnoreCase("any")) filters.remove("isMain"); else filters.put("isMain", isMain.toUpperCase().startsWith("T") ? "true" : "false"); break;
                case "4": String isAirport = readString("   Must be an Airport? (T/F, or 'any' to clear): "); if (isAirport.equalsIgnoreCase("any")) filters.remove("isAirport"); else filters.put("isAirport", isAirport.toUpperCase().startsWith("T") ? "true" : "false"); break;
                case "R": filters.clear(); System.out.println(ANSI_YELLOW + "   All filters cleared." + ANSI_RESET); break;
                case "C": showInfo("Query cancelled."); return;
                case "S": break;
                default: showError("Invalid option.");
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

    private void handleBuild2DTree() {
        showInfo("--- [USEI07] Build & Analyze 2D-Tree ---");
        try {
            Map<String, Object> stats = stationIndexManager.get2DTreeStats();
            showSuccess("2D-Tree analysis complete.");
            System.out.println(ANSI_BOLD + "\n--- 2D-Tree Statistics ---" + ANSI_RESET);
            System.out.printf(ANSI_BOLD + "  Size (Nodes): %s%-10d " + ANSI_RESET, ANSI_CYAN, stats.get("size"));
            System.out.printf(ANSI_BOLD + "Height: %s%d%n" + ANSI_RESET, ANSI_CYAN, stats.get("height"));
            @SuppressWarnings("unchecked")
            Map<Integer, Integer> buckets = (Map<Integer, Integer>) stats.get("bucketSizes");
            System.out.println(ANSI_BOLD + "  Node Capacity (Stations per Node):" + ANSI_RESET);
            buckets.entrySet().stream().sorted(Map.Entry.comparingByKey()).forEach(entry -> System.out.printf("    - %d station(s)/node : %s%d nodes%s%n", entry.getKey(), ANSI_CYAN, entry.getValue(), ANSI_RESET));
            System.out.println(ANSI_BOLD + "\n--- Build Analysis ---" + ANSI_RESET);
            System.out.println(ANSI_BOLD + "  Strategy:    " + ANSI_ITALIC + "Balanced build using pre-sorted lists (from USEI06)." + ANSI_RESET);
            System.out.println(ANSI_BOLD + "  Complexity:  " + ANSI_CYAN + "O(N log N)" + ANSI_RESET);
        } catch (Exception e) {
            showError("Failed to build or analyze the 2D-Tree (USEI07): " + e.getMessage());
        }
    }

    private void handleViewInventory() {
        showInfo("--- Current Inventory Contents ---");
        List<Box> boxes = manager.getInventory().getBoxes();
        if (boxes.isEmpty()) {
            showInfo("Inventory is empty.");
        } else {
            System.out.printf(ANSI_BOLD + "Displaying %d boxes (Sorted by FEFO/FIFO):%n" + ANSI_RESET, boxes.size());
            System.out.println(ANSI_BOLD + ANSI_PURPLE + "=".repeat(84) + ANSI_RESET);
            System.out.printf(ANSI_BOLD + "  %-10s | %-12s | %-4s | %-12s | %-18s | %-10s %n", "BOX ID", "SKU", "QTY", "EXPIRY", "RECEIVED", "LOCATION");
            System.out.println(ANSI_BOLD + ANSI_PURPLE + "-".repeat(84) + ANSI_RESET);
            for (Box b : boxes) System.out.println(b.toString());
            System.out.println(ANSI_BOLD + ANSI_PURPLE + "=".repeat(84) + ANSI_RESET);
        }
    }

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
            System.out.printf("   Physical Capacity: " + color + "%d/%d boxes (%.1f%% full)" + ANSI_RESET + "%n", usedCapacity, totalCapacity, percentage);
            System.out.printf("   Logical Inventory Size (Total): %d boxes%n", manager.getInventory().getBoxes().size());
            String details = readString(ANSI_ITALIC + "   View bay details for this warehouse? (y/N): " + ANSI_RESET);
            if (details.trim().equalsIgnoreCase("y")) printBayDetails(wh);
        }
    }

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
                    System.out.printf("%s%2d.%s %s %s(%.2f km)%s%n", ANSI_CYAN, i + 1, ANSI_RESET, formatStationDisplay(sd.getStation()), ANSI_YELLOW, sd.getDistanceKm(), ANSI_RESET);
                }
                if (stations.size() > displayLimit) {
                    System.out.printf(ANSI_ITALIC + "   ... and %d more stations%s%n", stations.size() - displayLimit, ANSI_RESET);
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
                case 1: handleRadiusSearch(); break;
                case 2: executeUSEI10DemoQueries(radiusSearch); break;
                case 0: break;
            }
        } catch (Exception e) {
            showError("Error executing radius search (USEI10): " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void executeUSEI10DemoQueries(RadiusSearch radiusSearch) {
        showInfo("--- USEI10 Demo Queries ---");
        Object[][] demoQueries = {{"Paris, France", 48.8566, 2.3522, 50.0}, {"Lisbon, Portugal", 38.7223, -9.1393, 30.0}, {"Madrid, Spain", 40.4168, -3.7038, 40.0}, {"Berlin, Germany", 52.5200, 13.4050, 25.0}, {"Rome, Italy", 41.9028, 12.4964, 35.0}};
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
                    System.out.printf("%s (%.1f km) | ", sd.getStation().getStation(), sd.getDistanceKm());
                }
                System.out.println();
            }
            System.out.println("─".repeat(60));
        }
        showSuccess("USEI10 demo queries completed!");
    }

    private void showSuccess(String message) {
        System.out.println(ANSI_GREEN + ANSI_BOLD + "\nSUCCESS: " + ANSI_RESET + ANSI_GREEN + message + ANSI_RESET);
    }

    private void showError(String message) {
        System.out.println(ANSI_RED + ANSI_BOLD + "\nERROR: " + ANSI_RESET + ANSI_RED + message + ANSI_RESET);
    }

    private void showInfo(String message) {
        System.out.println(ANSI_CYAN + "\n " + message + ANSI_RESET);
    }

    private void promptEnterKey() {
        System.out.print(ANSI_ITALIC + "\n(Press ENTER to return to the menu...)" + ANSI_RESET);
        scanner.nextLine();
    }

    private String formatStationDisplay(EuropeanStation station) {
        return station.getStation() + " [" + station.getCountry() + "] " + "(" + String.format("%.6f", station.getLatitude()) + ", " + String.format("%.6f", station.getLongitude()) + ")" + (station.isCity() ? " " : "") + (station.isMainStation() ? " " : "");
    }

    private Boolean parseOptionalBoolean(String input) {
        if (input == null || input.trim().isEmpty() || input.equalsIgnoreCase("any")) return null;
        return input.trim().equalsIgnoreCase("true") || input.trim().equalsIgnoreCase("t");
    }

    private void printBayDetails(Warehouse wh) {
        System.out.println(ANSI_BOLD + "\n   --- Bay Details for Warehouse " + wh.getWarehouseId() + " ---" + ANSI_RESET);
        System.out.printf("   %-10s | %-10s | %-18s | %s%n", "AISLE", "BAY", "CAPACITY (USED/MAX)", "VISUAL");
        System.out.println(ANSI_PURPLE + "   " + "-".repeat(60) + ANSI_RESET);
        List<Bay> sortedBays = wh.getBays().stream().sorted(Comparator.comparing(Bay::getAisle).thenComparing(Bay::getBay)).toList();
        for (Bay bay : sortedBays) {
            int used = bay.getBoxes().size();
            int max = bay.getCapacityBoxes();
            double percentage = (max > 0) ? ((double) used / max) : 0;
            String color = (used == max) ? ANSI_RED : (used > 0 ? ANSI_GREEN : ANSI_RESET);
            int barWidth = 10;
            int filled = (int) (percentage * barWidth);
            int empty = barWidth - filled;
            String visualBar = String.format("[%s%s%s]", color, "#".repeat(filled), " ".repeat(empty) + ANSI_RESET);
            System.out.printf("   %-10s | %-10s | %s%2d / %-2d boxes%s     | %s%n", bay.getAisle(), bay.getBay(), color, used, max, ANSI_RESET, visualBar);
        }
        System.out.println(ANSI_PURPLE + "   " + "-".repeat(60) + ANSI_RESET);
    }

    private List<EuropeanStation> applyAdvancedFilters(List<EuropeanStation> stations, Map<String, String> filters) {
        if (filters.isEmpty()) return stations;
        return stations.stream().filter(s -> {
            if (!filters.containsKey("country")) return true;
            return s.getCountry().equalsIgnoreCase(filters.get("country"));
        }).filter(s -> {
            if (!filters.containsKey("isCity")) return true;
            boolean mustBeCity = Boolean.parseBoolean(filters.get("isCity"));
            return s.isCity() == mustBeCity;
        }).filter(s -> {
            if (!filters.containsKey("isMain")) return true;
            boolean mustBeMain = Boolean.parseBoolean(filters.get("isMain"));
            return s.isMainStation() == mustBeMain;
        }).filter(s -> {
            if (!filters.containsKey("isAirport")) return true;
            boolean mustBeAirport = Boolean.parseBoolean(filters.get("isAirport"));
            return s.isAirport() == mustBeAirport;
        }).sorted(Comparator.comparing(EuropeanStation::getCountry).thenComparing(EuropeanStation::getStation)).toList();
    }

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
            System.out.printf(ANSI_BOLD + "  %-30s | %-7s | %-5s | %-5s | %-5s | %-15s %n", "STATION NAME", "COUNTRY", "CITY?", "MAIN?", "AIR?", "TIME ZONE");
            System.out.println(ANSI_BOLD + ANSI_PURPLE + "-".repeat(82) + ANSI_RESET);
            for (int i = startIndex; i < endIndex; i++) {
                EuropeanStation s = results.get(i);
                System.out.printf("  %-30s | %s%-7s%s | %-5s | %-5s | %-5s | %-15s %n", s.getStation().length() > 29 ? s.getStation().substring(0, 27) + "..." : s.getStation(), ANSI_CYAN, s.getCountry(), ANSI_RESET, s.isCity() ? "Yes" : "No", s.isMainStation() ? "Yes" : "No", s.isAirport() ? "Yes" : "No", s.getTimeZoneGroup());
            }
            System.out.println(ANSI_BOLD + ANSI_PURPLE + "-".repeat(82) + ANSI_RESET);
            System.out.println("\n" + ANSI_BOLD + "Controls:" + ANSI_RESET);
            String prev = (currentPage > 0) ? "[P]prev Page" : "           ";
            String next = (currentPage < totalPages - 1) ? "[N]next Page" : "           ";
            System.out.printf("  %s   |   %s   |   [E]exit Query%n", prev, next);
            input = readString(ANSI_BOLD + "Choose an option: " + ANSI_RESET).toUpperCase();
            if (input.equals("N") && currentPage < totalPages - 1) {
                currentPage++;
            } else if (input.equals("P") && currentPage > 0) {
                currentPage--;
            }
        } while (!input.equals("E"));
        showInfo("Exited query view.");
    }

    private String readString(String prompt) {
        System.out.print(prompt);
        return scanner.nextLine();
    }

    private int readInt(int min, int max, String prompt) {
        System.out.print(prompt);
        while (true) {
            try {
                String line = scanner.nextLine();
                if (isCancel(line)) return 0;
                int option = Integer.parseInt(line);
                if (option >= min && option <= max) return option;
                else System.out.print(ANSI_RED + String.format("Invalid input. Please enter a number between %d and %d.%n" + ANSI_RESET + prompt, min, max));
            } catch (NumberFormatException e) {
                System.out.print(ANSI_RED + "Invalid input. Please enter a number." + ANSI_RESET + "\n" + prompt);
            }
        }
    }

    private double readDouble(double min, double max, String prompt) {
        System.out.print(prompt);
        while (true) {
            try {
                String line = scanner.nextLine();
                if (isCancel(line)) return 0.0;
                double value = Double.parseDouble(line.replace(',', '.'));
                if (value >= min && value <= max) return value;
                else System.out.print(ANSI_RED + String.format("Invalid input. Please enter a value between %.2f and %.2f.%n" + ANSI_RESET + prompt, min, max));
            } catch (NumberFormatException e) {
                System.out.print(ANSI_RED + "Invalid input. Please enter a valid number." + ANSI_RESET + "\n" + prompt);
            }
        }
    }

    private double readDouble(String prompt) {
        return readDouble(-Double.MAX_VALUE, Double.MAX_VALUE, prompt);
    }

    private boolean isCancel(String input) {
        return input.trim().equals("0") || input.trim().equalsIgnoreCase("c");
    }
}
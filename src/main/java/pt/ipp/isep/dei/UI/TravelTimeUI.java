package pt.ipp.isep.dei.UI;

import pt.ipp.isep.dei.controller.TravelTimeController;
import pt.ipp.isep.dei.domain.Station;
import pt.ipp.isep.dei.domain.Locomotive;
import pt.ipp.isep.dei.repository.StationRepository;
import pt.ipp.isep.dei.repository.LocomotiveRepository;

import java.util.*;

/**
 * User Interface class for handling USLP03: Calculate Travel Time.
 * (Versão 2.0 - "Bonita e User-Friendly")
 */
public class TravelTimeUI implements Runnable {

    // --- Códigos de Cores ANSI (copiados da CargoHandlingUI) ---
    public static final String ANSI_RESET = "\u001B[0m";
    public static final String ANSI_RED = "\u001B[31m";
    public static final String ANSI_GREEN = "\u001B[32m";
    public static final String ANSI_YELLOW = "\u001B[33m";
    public static final String ANSI_BLUE = "\u001B[34m";
    public static final String ANSI_CYAN = "\u001B[36m";
    public static final String ANSI_BOLD = "\u001B[1m";
    public static final String ANSI_ITALIC = "\u001B[3m";

    private final TravelTimeController controller;
    private final StationRepository stationRepo;
    private final LocomotiveRepository locomotiveRepo;
    private final Scanner scanner; // <-- MUDANÇA: Recebido por construtor

    /**
     * Construtor atualizado para receber o Scanner da UI principal.
     */
    public TravelTimeUI(TravelTimeController controller, StationRepository stationRepo,
                        LocomotiveRepository locomotiveRepo, Scanner scanner) {
        this.controller = controller;
        this.stationRepo = stationRepo;
        this.locomotiveRepo = locomotiveRepo;
        this.scanner = scanner; // <-- MUDANÇA
    }

    @Override
    public void run() {
        System.out.println(ANSI_BOLD + ANSI_BLUE + "\n" + "=".repeat(50));
        System.out.println("     USLP03 - Calculate Travel Time (Fastest Path)");
        System.out.println("=".repeat(50) + ANSI_RESET);

        try {
            // 1. List and select Departure Station
            System.out.println(ANSI_CYAN + "\n--- Available Stations ---" + ANSI_RESET);
            List<Station> stations = stationRepo.findAll();
            if (stations.isEmpty()) {
                showError("No stations found in the repository.");
                return;
            }
            stations.forEach(System.out::println); // O toString() da Station está bom

            int departureId = readInt(scanner, "➡️  Enter the ID of the DEPARTURE Station: ");

            // 1.1 Validate departure station
            if (stationRepo.findById(departureId).isEmpty()) {
                showError(String.format("Station with ID %d not found.", departureId));
                return;
            }

            // 2. List directly connected destinations
            System.out.println(ANSI_CYAN + "\n--- Directly Connected Destinations ---" + ANSI_RESET);
            List<Station> connectedStations = controller.getDirectlyConnectedStations(departureId);

            if (connectedStations.isEmpty()) {
                showInfo("No directly connected stations found from this origin.");
                return;
            }

            final Set<Integer> validDestinationIds = new HashSet<>();
            connectedStations.forEach(st -> {
                System.out.println(st); // O toString() da Station está bom
                validDestinationIds.add(st.getIdEstacao());
            });

            // 3. Ask for Arrival Station (validated)
            int arrivalId = -1;
            boolean validDestination = false;
            while (!validDestination) {
                arrivalId = readInt(scanner, "\n➡️  Enter the ID of the ARRIVAL Station (from the list above): ");

                if (arrivalId == departureId) {
                    showError("Arrival station cannot be the same as departure station.");
                } else if (validDestinationIds.contains(arrivalId)) {
                    validDestination = true;
                } else {
                    showError("Invalid ID. Please select an ID from the list of connected destinations.");
                }
            }

            // 4. List and select Locomotive
            System.out.println(ANSI_CYAN + "\n--- Available Locomotives ---" + ANSI_RESET);
            List<Locomotive> locomotives = locomotiveRepo.findAll();
            if (locomotives.isEmpty()) {
                showError("No locomotives found in the repository.");
                return;
            }
            locomotives.forEach(System.out::println); // O toString() da Locomotive está bom

            int locomotiveId = readInt(scanner, "➡️  Enter the ID of the selected Locomotive: ");

            // 5. Call Controller and print result
            showInfo("\n⚙️  Calculating fastest route...");
            String result = controller.calculateTravelTime(departureId, arrivalId, locomotiveId);

            System.out.println("\n" + ANSI_BOLD + ANSI_GREEN + "=".repeat(50));
            System.out.println("              Travel Time Result");
            System.out.println("=".repeat(50) + ANSI_RESET);
            System.out.println(result); // O controller já formata isto bem
            System.out.println(ANSI_BOLD + ANSI_GREEN + "=".repeat(50) + ANSI_RESET);

        } catch (Exception e) {
            showError("UNEXPECTED ERROR while executing USLP03: " + e.getMessage());
            e.printStackTrace(System.err);
        }
    }

    // --- Métodos de Feedback (Consistência com CargoHandlingUI) ---

    private void showError(String message) {
        System.out.println(ANSI_RED + ANSI_BOLD + "❌ ERROR: " + ANSI_RESET + ANSI_RED + message + ANSI_RESET);
    }

    private void showInfo(String message) {
        System.out.println(ANSI_CYAN + "ℹ️  " + message + ANSI_RESET);
    }

    /**
     * Helper para ler um Int de forma robusta.
     */
    private int readInt(Scanner sc, String prompt) {
        System.out.print(ANSI_BOLD + prompt + ANSI_RESET);
        while (true) {
            try {
                String line = sc.nextLine();
                return Integer.parseInt(line);
            } catch (NumberFormatException e) {
                System.out.print(ANSI_RED + "Invalid input. Please enter a number." + ANSI_RESET + "\n" + ANSI_BOLD + prompt + ANSI_RESET);
            } catch (NoSuchElementException e) {
                showError("Scanner closed unexpectedly. Exiting module.");
                return -1; // Sinal de falha
            }
        }
    }
}
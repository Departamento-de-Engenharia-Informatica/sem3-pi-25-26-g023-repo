package pt.ipp.isep.dei.UI;

import pt.ipp.isep.dei.controller.TravelTimeController;
import pt.ipp.isep.dei.domain.Station;
import pt.ipp.isep.dei.domain.Locomotive;
import pt.ipp.isep.dei.repository.StationRepository;
import pt.ipp.isep.dei.repository.LocomotiveRepository;

import java.util.*;

public class TravelTimeUI implements Runnable {

    private final TravelTimeController controller;
    private final StationRepository stationRepo;
    private final LocomotiveRepository locomotiveRepo;

    public TravelTimeUI(TravelTimeController controller, StationRepository stationRepo, LocomotiveRepository locomotiveRepo) {
        this.controller = controller;
        this.stationRepo = stationRepo;
        this.locomotiveRepo = locomotiveRepo;
    }

    @Override
    public void run() {
        Scanner scanner = new Scanner(System.in);
        System.out.println("\n" + "=".repeat(50));
        System.out.println("     USLP03 - Calculate Travel Time (Fastest Path)");
        System.out.println("=".repeat(50));

        try {
            // 1. List and select Departure Station
            System.out.println("\n--- Available Stations ---");
            List<Station> stations = stationRepo.findAll();
            if (stations.isEmpty()) {
                System.out.println("❌ ERROR: No stations found.");
                return;
            }
            stations.forEach(System.out::println);
            System.out.print("➡️  Enter the ID of the DEPARTURE Station: ");
            int departureId = scanner.nextInt();
            scanner.nextLine(); // Consume newline

            // 1.1 Validate departure station
            if (stationRepo.findById(departureId).isEmpty()) {
                System.out.printf("❌ ERROR: Station with ID %d not found.%n", departureId);
                return;
            }

            // 2. List directly connected destinations
            System.out.println("\n--- Directly Connected Destinations ---");
            List<Station> connectedStations = controller.getDirectlyConnectedStations(departureId);

            if (connectedStations.isEmpty()) {
                System.out.println("   No directly connected stations found from this origin.");
                return;
            }

            final Set<Integer> validDestinationIds = new HashSet<>();
            connectedStations.forEach(st -> {
                System.out.println(st);
                validDestinationIds.add(st.getIdEstacao());
            });

            // 3. Ask for Arrival Station (validated)
            int arrivalId = -1;
            boolean validDestination = false;
            while (!validDestination) {
                System.out.print("\n➡️  Enter the ID of the ARRIVAL Station (from the list above): ");
                try {
                    arrivalId = scanner.nextInt();
                    scanner.nextLine(); // Consume newline

                    if (arrivalId == departureId) {
                        System.out.println("❌ ERROR: Arrival station cannot be the same as departure station.");
                    } else if (validDestinationIds.contains(arrivalId)) {
                        validDestination = true;
                    } else {
                        System.out.println("❌ ERROR: Invalid ID. Please select an ID from the list of connected destinations.");
                    }
                } catch (InputMismatchException e) {
                    System.out.println("❌ ERROR: Invalid input. Please enter numbers only.");
                    scanner.nextLine(); // Clear buffer
                }
            }

            // 4. List and select Locomotive
            System.out.println("\n--- Available Locomotives ---");
            List<Locomotive> locomotives = locomotiveRepo.findAll();
            if (locomotives.isEmpty()) {
                System.out.println("❌ ERROR: No locomotives found.");
                return;
            }
            locomotives.forEach(System.out::println);
            System.out.print("➡️  Enter the ID of the selected Locomotive: ");
            int locomotiveId = scanner.nextInt();
            scanner.nextLine(); // Consume newline

            // 5. Call Controller and print result
            System.out.println("\n⚙️  Calculating fastest route...");
            String result = controller.calculateTravelTime(departureId, arrivalId, locomotiveId);

            System.out.println("\n" + "=".repeat(50));
            System.out.println("              Travel Time Result");
            System.out.println("=".repeat(50));
            System.out.println(result);
            System.out.println("=".repeat(50));

        } catch (InputMismatchException e) {
            System.out.println("\n❌ ERROR: Invalid input. Please enter numbers only.");
            scanner.nextLine(); // Clear scanner buffer
        } catch (Exception e) {
            System.out.println("\n❌ UNEXPECTED ERROR while executing USLP03: " + e.getMessage());
            e.printStackTrace();
        }
    }
}

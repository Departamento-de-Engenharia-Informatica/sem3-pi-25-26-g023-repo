package pt.ipp.isep.dei.UI;

import pt.ipp.isep.dei.controller.TravelTimeController;
import pt.ipp.isep.dei.domain.*;
import pt.ipp.isep.dei.repository.StationRepository;
import pt.ipp.isep.dei.repository.LocomotiveRepository;

import java.util.InputMismatchException;
import java.util.List;
import java.util.Scanner;

/**
 * Main User Interface for the Cargo Handling Terminal.
 * <p>
 * This class implements {@link Runnable} and serves as the central menu for the
 * application. It integrates functionalities from two main domains:
 * <ul>
 * <li><b>ESINF:</b> Warehouse Management (Unloading, Inventory, Picking Plans).</li>
 * <li><b>LAPR3:</b> Railway Network (Travel Time Calculation).</li>
 * </ul>
 * It delegates specific tasks to other UI classes like {@link PickingUI} and
 * {@link TravelTimeUI}.
 */
public class CargoHandlingUI implements Runnable {

    private final WMS wms;
    private final InventoryManager manager;
    private final List<Wagon> wagons;

    // --- Added LAPR3 components ---
    private final TravelTimeController travelTimeController;
    private final StationRepository estacaoRepo;
    private final LocomotiveRepository locomotivaRepo;
    // --- End of added components ---

    /**
     * Constructs the main Cargo Handling UI.
     * This constructor is modified to accept components from both ESINF (WMS, Manager)
     * and LAPR3 (Controller, Repositories) domains, allowing for an integrated menu.
     *
     * @param wms                   The Warehouse Management System (ESINF).
     * @param manager               The Inventory Manager (ESINF).
     * @param wagons                The list of wagons pre-loaded at startup (ESINF).
     * @param travelTimeController  The controller for travel time logic (LAPR3).
     * @param estacaoRepo           The repository for stations (LAPR3).
     * @param locomotivaRepo        The repository for locomotives (LAPR3).
     */
    public CargoHandlingUI(WMS wms, InventoryManager manager, List<Wagon> wagons,
                           TravelTimeController travelTimeController, StationRepository estacaoRepo,
                           LocomotiveRepository locomotivaRepo) {
        this.wms = wms;
        this.manager = manager;
        this.wagons = wagons;
        // --- Assignment of new components ---
        this.travelTimeController = travelTimeController;
        this.estacaoRepo = estacaoRepo;
        this.locomotivaRepo = locomotivaRepo;
    }


    /**
     * Runs the main menu loop for the Cargo Handling Terminal.
     * <p>
     * This method continuously displays the menu ({@link #showMenu()}),
     * prompts the user for an option, and processes the selection via
     * {@link #handleOption(int, Scanner)}. It handles invalid (non-integer)
     * input and continues until the user chooses to exit (option 0).
     */
    @Override
    public void run() {
        Scanner scanner = new Scanner(System.in);
        int option = -1;

        do {
            showMenu(); // Menu was updated
            try {
                System.out.print("> Please choose an option: ");
                option = scanner.nextInt();
                scanner.nextLine(); // Consume the newline
                handleOption(option, scanner); // Pass the scanner to the sub-UIs

            } catch (InputMismatchException e) {
                System.out.println("\n❌ Invalid input. Please enter a number corresponding to an option.\n");
                scanner.nextLine(); // Clear the buffer
            }

        } while (option != 0);

        // scanner.close(); // Do not close System.in
    }

    /**
     * Displays the main menu options to the console.
     * This menu includes options for both ESINF and LAPR3 functionalities.
     */
    private void showMenu() {
        System.out.println("\n=========================================");
        System.out.println("   🚂 Cargo Handling Terminal Menu   ");
        System.out.println("=========================================");
        System.out.println("--- ESINF (Warehouse) ---");
        System.out.println("1. Unload Wagons (FEFO/FIFO)");
        System.out.println("2. View Current Inventory");
        System.out.println("3. Generate Picking Plan (USEI03/04)");
        System.out.println("4. View Warehouse Information");
        System.out.println("--- LAPR3 (Railway) ---");
        System.out.println("5. Calculate Travel Time (USLP03)"); // NEW OPTION
        System.out.println("-----------------------------------------");
        System.out.println("0. Exit");
        System.out.println("=========================================");
    }

    /**
     * Handles the menu option selected by the user.
     * <p>
     * This method uses a switch statement to delegate the task to the appropriate
     * handler, either a private method within this class or a new instance
     * of a dedicated UI class (e.g., {@link PickingUI}, {@link TravelTimeUI}).
     *
     * @param option  The integer option selected by the user.
     * @param scanner The Scanner object (passed from `run()`) to be used by
     * sub-handlers like {@link #handleUnloadWagons(Scanner)}.
     */
    private void handleOption(int option, Scanner scanner) {
        switch (option) {
            case 1:
                handleUnloadWagons(scanner); // Pass the scanner
                break;

            case 2:
                System.out.println("\n--- Current Inventory Contents ---\n");
                List<Box> boxes = manager.getInventory().getBoxes();
                if (boxes.isEmpty()) {
                    System.out.println("Inventory is empty.");
                } else {
                    for (Box b : boxes) {
                        System.out.println(b);
                    }
                }
                System.out.println();
                break;

            case 3:
                System.out.println("\n--- Generate Picking Plan (USEI03/04) ---");
                // PickingUI uses its own scanner, no need to pass it
                PickingUI pickingUI = new PickingUI(manager);
                pickingUI.run();
                break;

            case 4:
                showWarehouseInfo();
                break;

            // --- NEW OPTION ---
            case 5:
                System.out.println("\n--- Calculate Travel Time (USLP03) ---");
                // The new UI needs the LAPR3 components and its controller
                TravelTimeUI travelTimeUI = new TravelTimeUI(travelTimeController, estacaoRepo, locomotivaRepo);
                travelTimeUI.run(); // This UI uses its own internal scanner
                break;

            case 0:
                System.out.println("\nExiting Cargo Handling Menu. Goodbye! 👋");
                break;

            default:
                System.out.println("\n❌ Invalid option. Please select a valid number from the menu.\n");
                break;
        }
    }

    /**
     * Handles the "Unload Wagons" functionality (Menu Option 1).
     * <p>
     * Displays a sub-menu to either unload all wagons or select specific
     * wagons manually for unloading via the {@link WMS}.
     *
     * @param sc The Scanner instance to read user sub-menu choices.
     */
    private void handleUnloadWagons(Scanner sc) { // Receives the scanner
        System.out.println("\n--- Unload Wagons ---");
        System.out.println("1. Unload ALL wagons");
        System.out.println("2. Select wagons manually");
        System.out.print("> Option: ");

        int sub = -1;
        try {
            sub = sc.nextInt(); // Uses the passed scanner
            sc.nextLine(); // Consumes newline
        } catch (InputMismatchException e) {
            System.out.println("❌ Invalid input.");
            sc.nextLine(); // Clears the buffer
            return;
        }


        if (sub == 1) {
            wms.unloadWagons(wagons);
        } else if (sub == 2) {
            for (int i = 0; i < wagons.size(); i++) {
                System.out.printf("%d. Wagon %s (%d boxes)%n",
                        i + 1, wagons.get(i).getWagonId(), wagons.get(i).getBoxes().size());
            }
            System.out.print("> Enter wagon numbers (comma-separated): ");
            String[] choices = sc.nextLine().split(",");
            List<Wagon> selected = new java.util.ArrayList<>();
            for (String c : choices) {
                try {
                    int idx = Integer.parseInt(c.trim()) - 1;
                    if (idx >= 0 && idx < wagons.size()) {
                        selected.add(wagons.get(idx));
                    }
                } catch (NumberFormatException ignored) {}
            }
            wms.unloadWagons(selected);
        } else {
            System.out.println("Invalid option.");
        }
    }

    /**
     * Displays detailed information about the loaded warehouses (Menu Option 4).
     * <p>
     * Iterates through all warehouses known to the {@link InventoryManager} and
     * prints statistics, including physical bay capacity (total vs. used) and
     * the total number of items in the logical inventory.
     */
    private void showWarehouseInfo() {
        System.out.println("\n--- Warehouse Information ---");
        List<Warehouse> warehouses = manager.getWarehouses();
        if (warehouses.isEmpty()) {
            System.out.println("No warehouses loaded.");
            return;
        }

        for (Warehouse wh : warehouses) {
            System.out.printf("\n🏭 Warehouse: %s%n", wh.getWarehouseId());
            System.out.printf("   📦 Bays: %d%n", wh.getBays().size());

            int totalCapacity = 0;
            int usedCapacity = 0;

            for (Bay bay : wh.getBays()) {
                totalCapacity += bay.getCapacityBoxes();
                usedCapacity += bay.getBoxes().size(); // Counts physical boxes in the bay
            }

            System.out.printf("   📊 Physical Capacity: %d/%d boxes (%.1f%% full)%n",
                    usedCapacity, totalCapacity,
                    (totalCapacity > 0 ? (usedCapacity * 100.0 / totalCapacity) : 0));
            System.out.printf("   ℹ️ Logical Inventory Size (Total): %d boxes%n", manager.getInventory().getBoxes().size());
        }
        System.out.println();
    }
}
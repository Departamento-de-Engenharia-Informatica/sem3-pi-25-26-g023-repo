package pt.ipp.isep.dei.UI;

import pt.ipp.isep.dei.controller.TravelTimeController;
import pt.ipp.isep.dei.domain.*;
import pt.ipp.isep.dei.repository.EstacaoRepository;
import pt.ipp.isep.dei.repository.LocomotivaRepository;

import java.util.InputMismatchException;
import java.util.List;
import java.util.Scanner;

public class CargoHandlingUI implements Runnable {

    private final WMS wms;
    private final InventoryManager manager;
    private final List<Wagon> wagons;

    // --- Componentes LAPR3 adicionados ---
    private final TravelTimeController travelTimeController;
    private final EstacaoRepository estacaoRepo;
    private final LocomotivaRepository locomotivaRepo;
    // --- Fim dos componentes adicionados ---

    // --- CONSTRUTOR MODIFICADO ---
    public CargoHandlingUI(WMS wms, InventoryManager manager, List<Wagon> wagons,
                           TravelTimeController travelTimeController, EstacaoRepository estacaoRepo,
                           LocomotivaRepository locomotivaRepo) {
        this.wms = wms;
        this.manager = manager;
        this.wagons = wagons;
        // --- Atribuição dos novos componentes ---
        this.travelTimeController = travelTimeController;
        this.estacaoRepo = estacaoRepo;
        this.locomotivaRepo = locomotivaRepo;
    }


    @Override
    public void run() {
        Scanner scanner = new Scanner(System.in);
        int option = -1;

        do {
            showMenu(); // Menu foi atualizado
            try {
                System.out.print("> Please choose an option: ");
                option = scanner.nextInt();
                scanner.nextLine(); // Consumir o newline
                handleOption(option, scanner); // Passar o scanner para as sub-UIs

            } catch (InputMismatchException e) {
                System.out.println("\n❌ Invalid input. Please enter a number corresponding to an option.\n");
                scanner.nextLine(); // Limpar o buffer
            }

        } while (option != 0);

        // scanner.close(); // Não fechar o System.in
    }

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
        System.out.println("5. Calculate Travel Time (USLP03)"); // NOVA OPÇÃO
        System.out.println("-----------------------------------------");
        System.out.println("0. Exit");
        System.out.println("=========================================");
    }

    // --- handleOption MODIFICADO ---
    private void handleOption(int option, Scanner scanner) {
        switch (option) {
            case 1:
                handleUnloadWagons(scanner); // Passar o scanner
                break;

            case 2:
                System.out.println("\n--- Current Inventory Contents ---\n");
                List<Box> boxes = manager.getInventory().getBoxes();
                if (boxes.isEmpty()) {
                    System.out.println("Inventário está vazio.");
                } else {
                    for (Box b : boxes) {
                        System.out.println(b);
                    }
                }
                System.out.println();
                break;

            case 3:
                System.out.println("\n--- Generate Picking Plan (USEI03/04) ---");
                // PickingUI usa o seu próprio scanner, não precisa passar
                PickingUI pickingUI = new PickingUI(manager);
                pickingUI.run();
                break;

            case 4:
                showWarehouseInfo();
                break;

            // --- NOVA OPÇÃO ---
            case 5:
                System.out.println("\n--- Calculate Travel Time (USLP03) ---");
                // A nova UI precisa dos componentes LAPR3 e do seu controller
                TravelTimeUI travelTimeUI = new TravelTimeUI(travelTimeController, estacaoRepo, locomotivaRepo);
                travelTimeUI.run(); // Esta UI usa o seu próprio scanner interno
                break;

            case 0:
                System.out.println("\nExiting Cargo Handling Menu. Goodbye! 👋");
                break;

            default:
                System.out.println("\n❌ Invalid option. Please select a valid number from the menu.\n");
                break;
        }
    }

    // --- handleUnloadWagons MODIFICADO ---
    private void handleUnloadWagons(Scanner sc) { // Recebe o scanner
        System.out.println("\n--- Unload Wagons ---");
        System.out.println("1. Unload ALL wagons");
        System.out.println("2. Select wagons manually");
        System.out.print("> Option: ");

        int sub = -1;
        try {
            sub = sc.nextInt(); // Usa o scanner passado
            sc.nextLine(); // Consome newline
        } catch (InputMismatchException e) {
            System.out.println("❌ Input inválido.");
            sc.nextLine(); // Limpa o buffer
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
                usedCapacity += bay.getBoxes().size(); // Conta caixas físicas na bay
            }

            System.out.printf("   📊 Physical Capacity: %d/%d boxes (%.1f%% full)%n",
                    usedCapacity, totalCapacity,
                    (totalCapacity > 0 ? (usedCapacity * 100.0 / totalCapacity) : 0));
            System.out.printf("   ℹ️ Logical Inventory Size (Total): %d boxes%n", manager.getInventory().getBoxes().size());
        }
        System.out.println();
    }
}
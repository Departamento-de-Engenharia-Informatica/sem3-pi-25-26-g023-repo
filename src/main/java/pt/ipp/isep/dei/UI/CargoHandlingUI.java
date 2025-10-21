package pt.ipp.isep.dei.UI;

import pt.ipp.isep.dei.domain.*;

import java.util.InputMismatchException;
import java.util.List;
import java.util.Scanner;

public class CargoHandlingUI implements Runnable {

    private final WMS wms;
    private final InventoryManager manager;
    private final List<Wagon> wagons;

    public CargoHandlingUI(WMS wms, InventoryManager manager, List<Wagon> wagons) {
        this.wms = wms;
        this.manager = manager;
        this.wagons = wagons;
    }

    @Override
    public void run() {
        Scanner scanner = new Scanner(System.in);
        int option = -1;

        do {
            showMenu();
            try {
                System.out.print("> Please choose an option: ");
                option = scanner.nextInt();
                handleOption(option);

            } catch (InputMismatchException e) {
                System.out.println("\n❌ Invalid input. Please enter a number corresponding to an option.\n");
                scanner.next();
            }

        } while (option != 0);

        scanner.close();
    }

    private void showMenu() {
        System.out.println("=========================================");
        System.out.println("   🚂 Cargo Handling Terminal Menu   ");
        System.out.println("=========================================");
        System.out.println("1. Unload Wagons (FEFO/FIFO)");
        System.out.println("2. View Current Inventory");
        System.out.println("-----------------------------------------");
        System.out.println("0. Exit");
        System.out.println("=========================================");
    }

    private void handleOption(int option) {
        switch (option) {
            case 1:
                Scanner sc = new Scanner(System.in);
                System.out.println("\n--- Unload Wagons ---");
                System.out.println("1. Unload ALL wagons");
                System.out.println("2. Select wagons manually");
                System.out.print("> Option: ");
                int sub = sc.nextInt();

                if (sub == 1) {
                    wms.unloadWagons(wagons);
                } else if (sub == 2) {
                    for (int i = 0; i < wagons.size(); i++) {
                        System.out.printf("%d. Wagon %s (%d boxes)%n",
                                i + 1, wagons.get(i).getWagonId(), wagons.get(i).getBoxes().size());
                    }
                    System.out.print("> Enter wagon numbers (comma-separated): ");
                    sc.nextLine();
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
                break;

            case 2:
                System.out.println("\n--- Current Inventory Contents ---\n");
                for (Box b : manager.getInventory().getBoxes()) {
                    System.out.println(b);
                }
                System.out.println();
                break;

            case 0:
                System.out.println("\nExiting Cargo Handling Menu. Goodbye! 👋");
                break;

            default:
                System.out.println("\n❌ Invalid option. Please select a valid number from the menu.\n");
                break;
        }
    }
}

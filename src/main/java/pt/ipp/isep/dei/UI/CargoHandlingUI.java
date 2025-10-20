package pt.ipp.isep.dei.UI;

import pt.ipp.isep.dei.domain.*;

import java.util.InputMismatchException;
import java.util.List;
import java.util.Scanner;

public class CargoHandlingUI implements Runnable {

    private final WMS wms;
    private final InventoryManager manager;
    private final List<Wagon> wagons;

    // Construtor que recebe as dependências principais
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
                scanner.next(); // limpa buffer
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
                System.out.println("\n--- Executing Unload Wagons functionality ---\n");
                wms.unloadWagons(wagons);
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

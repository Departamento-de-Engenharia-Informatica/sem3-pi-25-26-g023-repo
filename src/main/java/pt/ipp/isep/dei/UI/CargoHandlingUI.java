package pt.ipp.isep.dei.UI;

import java.util.InputMismatchException;
import java.util.Scanner;

/**
 * Represents the text-based user interface for the Cargo Handling functionality.
 * This class is responsible for displaying the menu, reading user input,
 * and delegating actions to the appropriate controllers or services.
 */
public class CargoHandlingUI implements Runnable {

    /**
     * The main entry point for the UI, containing the menu loop.
     */
    @Override
    public void run() {
        Scanner scanner = new Scanner(System.in);
        int option = -1; // Initialize with a non-exit value

        do {
            showMenu();
            try {
                System.out.print("> Please choose an option: ");
                option = scanner.nextInt();
                handleOption(option);

            } catch (InputMismatchException e) {
                System.out.println("\n❌ Invalid input. Please enter a number corresponding to an option.\n");
                scanner.next(); // Important: clear the invalid input from the scanner buffer
            }

        } while (option != 0);

        scanner.close();
    }

    /**
     * Displays the main menu options to the console.
     */
    private void showMenu() {
        System.out.println("=========================================");
        System.out.println("   🚂 Cargo Handling Terminal Menu   ");
        System.out.println("=========================================");
        System.out.println("1. Unload Wagon");
        System.out.println("2. Generate Pick Path");
        System.out.println("3. View Current Inventory");
        System.out.println("4. Dispatch Cargo");
        System.out.println("-----------------------------------------");
        System.out.println("0. Exit");
        System.out.println("=========================================");
    }

    /**
     * Handles the user-selected menu option by calling the appropriate functionality.
     * @param option The integer option selected by the user.
     */
    private void handleOption(int option) {
        switch (option) {
            case 1:
                // Placeholder for the actual functionality
                System.out.println("\n--- Executing Unload Wagon functionality... ---\n");
                // In a real application, you would call a controller here.
                // e.g., UnloadWagonController unloadController = new UnloadWagonController();
                // unloadController.run();
                break;
            case 2:
                System.out.println("\n--- Executing Generate Pick Path functionality... ---\n");
                break;
            case 3:
                System.out.println("\n--- Viewing Current Inventory... ---\n");
                break;
            case 4:
                System.out.println("\n--- Executing Dispatch Cargo functionality... ---\n");
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
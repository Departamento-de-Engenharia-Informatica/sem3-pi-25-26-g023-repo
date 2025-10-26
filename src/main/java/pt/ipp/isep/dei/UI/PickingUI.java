package pt.ipp.isep.dei.UI;

import pt.ipp.isep.dei.domain.*;
import java.util.ArrayList; // Necessary import for ArrayList
import java.util.List;
import java.util.Map; // Necessary import for Map
import java.util.Scanner;

/**
 * User Interface for USEI03 (Generate Picking Plan) and USEI04 (Pick Path Sequencing).
 * <p>
 * This class implements {@link Runnable} and guides the user through a multi-step process:
 * 1. (USEI02) Automatically allocates orders based on current inventory.
 * 2. (USEI03) Configures and generates an optimized picking plan (trolleys)
 * based on the allocations, trolley capacity, and a chosen heuristic.
 * 3. (USEI04) Calculates the optimal picking path/sequence for each trolley
 * using various strategies (e.g., S-Shape, Nearest Neighbor).
 * </p>
 */
public class PickingUI {
    /**
     * The manager responsible for accessing inventory, items, and loading orders.
     */
    private final InventoryManager inventoryManager;

    /**
     * Constructs a new PickingUI.
     *
     * @param inventoryManager The manager providing access to business data.
     */
    public PickingUI(InventoryManager inventoryManager) {
        this.inventoryManager = inventoryManager;
    }

    /**
     * Runs the main logic for the picking plan and path generation UI.
     * It orchestrates the execution of USEI02, USEI03, and USEI04 in sequence.
     */
    public void run() {
        Scanner scanner = new Scanner(System.in);

        System.out.println("\n" + "=".repeat(50));
        System.out.println("        USEI03 - Picking Plan Generator");
        System.out.println("=".repeat(50));

        PickingPlan plan = null; // Declare plan here to be accessible later

        try {
            // 1. FIRST: Execute USEI02 to get allocations
            System.out.println("\n📦 STEP 1: Executing USEI02 to generate allocations...");

            // Load orders
            List<Order> orders = loadOrders();
            // Get a MUTABLE copy of the inventory for allocation simulation
            // The InventoryManager class must provide access to the Inventory, which in turn manages the Boxes
            List<Box> currentInventoryState = new ArrayList<>(inventoryManager.getInventory().getBoxes()); // Creates a mutable copy

            if (orders.isEmpty()) {
                System.out.println("❌ ERROR: No valid orders to process.");
                return;
            }

            if (currentInventoryState.isEmpty()) {
                System.out.println("❌ ERROR: Inventory is empty.");
                return;
            }

            System.out.printf("✅ Data loaded: %d orders, %d boxes in inventory%n",
                    orders.size(), currentInventoryState.size());

            // Configure and run OrderAllocator
            OrderAllocator allocator = new OrderAllocator();
            allocator.setItems(inventoryManager.getItemsMap()); // Pass items for weight calculation

            // Pass the MUTABLE copy of the inventory
            AllocationResult allocationResult = allocator.allocateOrders(
                    orders, currentInventoryState, OrderAllocator.Mode.PARTIAL);

            // Check if there are valid allocations
            if (allocationResult.allocations.isEmpty()) {
                System.out.println("❌ No allocations were generated in USEI02.");
                System.out.println("\n📊 Eligibility Summary:");
                for (Eligibility e : allocationResult.eligibilityList) {
                    System.out.println("  " + e);
                }
                return;
            }

            System.out.println("✅ USEI02 executed successfully!");
            System.out.printf("📊 Results: %d allocations generated, %d lines processed%n",
                    allocationResult.allocations.size(), allocationResult.eligibilityList.size());

            // Show eligibility summary
            System.out.println("\n📋 Eligibility Summary:");
            int eligible = 0, partial = 0, undispatchable = 0;
            for (Eligibility e : allocationResult.eligibilityList) {
                System.out.println("  " + e);
                switch (e.status) {
                    case ELIGIBLE: eligible++; break;
                    case PARTIAL: partial++; break;
                    case UNDISPATCHABLE: undispatchable++; break;
                }
            }
            System.out.printf("\n📈 Statistics: ELIGIBLE=%d, PARTIAL=%d, UNDISPATCHABLE=%d%n",
                    eligible, partial, undispatchable);

            // 2. SECOND: Parameters for USEI03
            System.out.println("\n🎯 STEP 2: Configure USEI03 - Picking Plan");

            System.out.print("➡️  Trolley capacity (kg): ");
            double capacity = scanner.nextDouble();

            if (capacity <= 0) {
                System.out.println("❌ Invalid capacity. Using default value of 50kg.");
                capacity = 50.0;
            }

            System.out.println("\n🧠 Available Heuristics:");
            System.out.println("1. FIRST_FIT - First one that fits (fastest)");
            System.out.println("2. FIRST_FIT_DECREASING - Largest first (more efficient)");
            System.out.println("3. BEST_FIT_DECREASING - Best fit (optimizes space)");
            System.out.print("➡️  Choose heuristic (1-3): ");

            int heuristicChoice = scanner.nextInt();
            HeuristicType heuristic = switch(heuristicChoice) {
                case 1 -> HeuristicType.FIRST_FIT;
                case 2 -> HeuristicType.FIRST_FIT_DECREASING;
                case 3 -> HeuristicType.BEST_FIT_DECREASING;
                default -> {
                    System.out.println("⚠️  Invalid choice. Using FIRST_FIT.");
                    yield HeuristicType.FIRST_FIT;
                }
            };

            // 3. THIRD: Execute USEI03
            System.out.println("\n⚙️  STEP 3: Executing USEI03...");

            PickingService service = new PickingService();
            service.setItemsMap(inventoryManager.getItemsMap()); // Pass items to the service

            plan = service.generatePickingPlan( // Assign to the 'plan' declared outside the try
                    allocationResult.allocations,
                    capacity,
                    heuristic
            );

            // 4. FOURTH: Show USEI03 results
            System.out.println("\n" + "=".repeat(60));
            System.out.println("           📊 RESULTS USEI03 - Picking Plan");
            System.out.println("=".repeat(60));
            System.out.println(plan.getSummary());

            System.out.println("\n🛒 Details per Trolley:");
            System.out.println("-".repeat(50));

            int trolleyCount = 1;
            double totalUtilization = 0;

            for (Trolley trolley : plan.getTrolleys()) {
                System.out.printf("\n🚗 Trolley %d: %s (%.1f%% utilized)%n",
                        trolleyCount, trolley.getId(), trolley.getUtilization());
                System.out.printf("   📦 Weight: %.1f/%.1f kg | Items: %d%n",
                        trolley.getCurrentWeight(), trolley.getMaxCapacity(),
                        trolley.getAssignments().size());

                for (PickingAssignment assignment : trolley.getAssignments()) {
                    System.out.printf("   → %s | Weight: %.1f kg | Location: %s%n",
                            assignment, assignment.getTotalWeight(), assignment.getLocation());
                }
                totalUtilization += trolley.getUtilization();
                trolleyCount++;
            }

            // Calculate avgUtilization only if there are trolleys to avoid division by zero
            double avgUtilization = (plan.getTotalTrolleys() > 0) ? (totalUtilization / plan.getTotalTrolleys()) : 0.0;
            System.out.printf("\n📈 Average utilization: %.1f%%%n", avgUtilization);

            // 5. OPTION: Export to CSV
            scanner.nextLine(); // Consume the pending newline after nextInt() or nextDouble()
            System.out.print("\n💾 Export USEI03 plan to CSV? (y/n): ");
            String exportChoice = scanner.nextLine(); // Use nextLine() to read the response correctly
            if (exportChoice.equalsIgnoreCase("y")) { // Changed from 's' to 'y'
                String csv = service.exportToCSV(plan);
                System.out.println("\n📄 CSV Generated (USEI03):");
                System.out.println("=".repeat(50));
                System.out.println(csv);
                System.out.println("=".repeat(50));
            }

            System.out.println("\n✅ USEI03 completed successfully!");


            // --- START OF USEI04 INTEGRATION ---

            // 6. EXECUTE USEI04 - Pick Path Sequencing
            System.out.println("\n" + "=".repeat(60));
            System.out.println("        🚀 USEI04 - Pick Path Sequencing");
            System.out.println("=".repeat(60));

            PickingPathService pathService = new PickingPathService();
            try {
                // Pass the 'plan' generated in USEI03
                Map<String, PickingPathService.PathResult> pathResults = pathService.calculatePickingPaths(plan);

                if (pathResults.isEmpty()) {
                    System.out.println("Could not calculate paths (check if picking plan has valid locations).");
                } else {
                    System.out.println("\n--- Sequencing Results (USEI04) ---");
                    pathResults.forEach((strategyName, result) -> {
                        System.out.println("\n" + strategyName + ":");
                        System.out.println(result); // Uses the PathResult's toString()
                        System.out.println("-".repeat(40));
                    });
                    System.out.println("\n✅ USEI04 completed successfully!");
                }

            } catch (Exception e) {
                System.out.println("❌ Error calculating picking paths (USEI04): " + e.getMessage());
                e.printStackTrace(); // Print stack trace for debugging
            }

            // --- END OF USEI04 INTEGRATION ---


        } catch (Exception e) {
            System.out.println("❌ Error during global execution: " + e.getMessage());
            e.printStackTrace(); // Print the full stack trace for debugging
        } finally {
            // It's good practice to close the scanner if it's not System.in or if this is the last use
            // In the context of a larger menu, it might be better not to close it here.
            // scanner.close();
        }
    }

    /**
     * Private helper method to load orders using the {@link InventoryManager}.
     * Handles exceptions internally and returns an empty list on failure.
     *
     * @return A list of loaded {@link Order} objects, or an empty list if loading fails.
     */
    private List<Order> loadOrders() {
        try {
            // Use the InventoryManager to load orders
            // Dentro da classe PickingUI.java, método loadOrders() - CORRIGIDO
            return inventoryManager.loadOrders(
                    "src/main/java/pt/ipp/isep/dei/FicheirosCSV/orders.csv",
                    "src/main/java/pt/ipp/isep/dei/FicheirosCSV/order_lines.csv" // <-- CORRIGIDO
            );
        } catch (Exception e) {
            System.out.println("❌ Error loading orders: " + e.getMessage());
            return List.of(); // Returns an empty list in case of error
        }
    }
}
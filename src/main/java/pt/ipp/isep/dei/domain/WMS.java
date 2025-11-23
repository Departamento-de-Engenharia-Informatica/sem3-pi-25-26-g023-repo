package pt.ipp.isep.dei.domain;

import java.time.LocalDate;
import java.util.List;

/**
 * Warehouse Management System - Manages warehouse operations.
 * (Version 2.1 - "Silent Mode")
 */
public class WMS {

    /**
     * A simple object to store the unload result
     * and pass it to Main.java.
     */
    public static class UnloadResult {
        public final int totalProcessed;
        public final int fullyUnloaded;
        public final int partiallyUnloaded;
        public final int notUnloaded;
        public int totalBoxes;

        /**
         * Constructs an UnloadResult summary object.
         */
        public UnloadResult(int totalProcessed, int fully, int partially, int not, int totalBoxes) {
            this.totalProcessed = totalProcessed;
            this.fullyUnloaded = fully;
            this.partiallyUnloaded = partially;
            this.notUnloaded = not;
            this.totalBoxes = totalBoxes;
        }
    }

    private final Quarantine quarantine;
    private final Inventory inventory;
    private final AuditLog auditLog;
    private final List<Warehouse> warehouses;

    /**
     * Constructs the Warehouse Management System service.
     */
    public WMS(Quarantine quarantine, Inventory inventory, AuditLog auditLog, List<Warehouse> warehouses) {
        this.quarantine = quarantine;
        this.inventory = inventory;
        this.auditLog = auditLog;
        this.warehouses = (warehouses != null) ? warehouses : List.of();
    }

    /**
     * USEI01 - Unloads wagons.
     * (Silent Mode: Returns a summary instead of printing)
     * @param wagons list of wagons to unload
     * @return UnloadResult with the operation summary.
     */
    public UnloadResult unloadWagons(List<Wagon> wagons) {
        if (wagons == null || wagons.isEmpty()) {
            return new UnloadResult(0, 0, 0, 0, 0);
        }
        if (warehouses.isEmpty()) {
            wagons.forEach(w -> auditLog.writeLine(String.format("Wagon %s | action=NotUnloaded | reason=NoWarehousesAvailable%n", w.getWagonId())));
            return new UnloadResult(wagons.size(), 0, 0, wagons.size(), 0);
        }

        int wagonsSuccessfullyUnloaded = 0;
        int wagonsPartiallyUnloaded = 0;
        int wagonsNotUnloaded = 0;
        int totalBoxesStored = 0;

        for (Wagon w : wagons) {
            if (w == null || w.getBoxes() == null || w.getBoxes().isEmpty()) continue;

            int boxesStoredCount = 0;
            boolean anyBoxStored = false;

            for (Box b : w.getBoxes()) {
                boolean storedThisBox = false;
                for (Warehouse wh : warehouses) {
                    // Tries to store the box in the warehouse
                    if (wh.storeBox(b)) {
                        // If successful, update the inventory and counters
                        inventory.insertBoxFEFO(b);
                        storedThisBox = true;
                        anyBoxStored = true;
                        boxesStoredCount++;
                        break;
                    }
                }
                if (!storedThisBox) {
                    // SILENCED: System.out.printf("  ⚠️ Wagon %s: No space found for Box %s...%n", ...);
                }
            }

            if (boxesStoredCount == w.getBoxes().size()) {
                wagonsSuccessfullyUnloaded++;
            } else if (anyBoxStored) {
                wagonsPartiallyUnloaded++;
                auditLog.writeLine(String.format("Wagon %s | action=PartiallyUnloaded | reason=WarehousesFull%n", w.getWagonId()));
            } else {
                wagonsNotUnloaded++;
                auditLog.writeLine(String.format("Wagon %s | action=NotUnloaded | reason=AllWarehousesFull%n", w.getWagonId()));
            }
            totalBoxesStored += boxesStoredCount;
        }

        return new UnloadResult(wagons.size(), wagonsSuccessfullyUnloaded, wagonsPartiallyUnloaded, wagonsNotUnloaded, totalBoxesStored);
    }

    /**
     * USEI05 - Processes returns in quarantine.
     * (Silent Mode: Only logs to audit log)
     */
    public void processReturns() {
        // SILENCED: System.out.println("\n--- Processing Returns (USEI05) ---");
        if (quarantine == null || quarantine.isEmpty()) {
            // SILENCED: System.out.println("ℹ️ Quarantine empty. No returns to process.");
            return;
        }
        if (warehouses.isEmpty()) {
            // SILENCED: System.err.println("❌ FATAL ERROR: No warehouses to restock returned items!");
            return;
        }

        int processed = 0;
        int restocked = 0;
        int discarded = 0;

        while (!quarantine.isEmpty()) {
            Return r = quarantine.getNextReturn();
            processed++;
            if (r == null) continue;

            // SILENCED: System.out.printf("  Processing Return %s ...%n", ...);

            if (r.isRestockable()) {
                // Try to restock in one of the warehouses
                if (inventory.restock(r, warehouses)) {
                    auditLog.writeLog(r, "Restocked", r.getQty());
                    restocked++;
                } else {
                    // SILENCED: System.out.printf("  ⚠️ Item %s (Return %s) was restockable but no space...%n", ...);
                    auditLog.writeLog(r, "Discarded (No Space)", r.getQty());
                    discarded++;
                }
            } else {
                // SILENCED: System.out.printf("  Item %s (Return %s) is not restockable...%n", ...);
                // Not restockable (e.g., reason is damaged or expired)
                auditLog.writeLog(r, "Discarded", r.getQty());
                discarded++;
            }
        }

    }
    // --- GETTERS FOR THE GUI ---
    // (Add these methods to your WMS class)

    /**
     * Allows the GUI to access the inventory
     * to display statistics.
     * @return The Inventory object.
     */
    public Inventory getInventory() {
        return this.inventory;
    }

    /**
     * Allows the GUI to access the quarantine
     * to display statistics.
     * @return The Quarantine object.
     */
    public Quarantine getQuarantine() {
        return this.quarantine;
    }
}
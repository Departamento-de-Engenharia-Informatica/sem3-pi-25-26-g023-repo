package pt.ipp.isep.dei.domain;

import java.util.List;

/**
 * Warehouse Management System - Manages warehouse operations including
 * wagon unloading and return processing.
 */
public class WMS {
    private final Quarantine quarantine;
    private final Inventory inventory;
    private final AuditLog auditLog;
    private final List<Warehouse> warehouses;

    /**
     * Creates a new WMS with the specified components.
     * @param quarantine the quarantine system for returns
     * @param inventory the inventory management system
     * @param auditLog the audit logging system
     * @param warehouses list of available warehouses
     */
    public WMS(Quarantine quarantine, Inventory inventory, AuditLog auditLog, List<Warehouse> warehouses) {
        this.quarantine = quarantine;
        this.inventory = inventory;
        this.auditLog = auditLog;
        this.warehouses = (warehouses != null) ? warehouses : List.of();
    }

    /**
     * USEI01 - Unloads wagons into warehouses using FEFO/FIFO rules.
     * Processes all boxes from each wagon and stores them in available warehouse space.
     * @param wagons list of wagons to unload
     */
    public void unloadWagons(List<Wagon> wagons) {
        if (wagons == null || wagons.isEmpty()) {
            System.out.println("No wagons to unload.");
            return;
        }
        if (warehouses.isEmpty()) {
            System.err.println("❌ FATAL ERROR: No warehouses defined to store boxes!");
            wagons.forEach(w -> auditLog.writeLine(String.format("Wagon %s | action=NotUnloaded | reason=NoWarehousesAvailable%n", w.getWagonId())));
            return;
        }

        int wagonsSuccessfullyUnloaded = 0;
        int wagonsPartiallyUnloaded = 0;
        int wagonsNotUnloaded = 0;

        for (Wagon w : wagons) {
            if (w == null || w.getBoxes() == null || w.getBoxes().isEmpty()) continue;

            int boxesStoredCount = 0;
            boolean anyBoxStored = false;

            // Try to store each box in any available warehouse
            for (Box b : w.getBoxes()) {
                boolean storedThisBox = false;
                for (Warehouse wh : warehouses) {
                    if (wh.storeBox(b)) {
                        inventory.insertBoxFEFO(b);
                        storedThisBox = true;
                        anyBoxStored = true;
                        boxesStoredCount++;
                        break;
                    }
                }
                if (!storedThisBox) {
                    System.out.printf("  ⚠️ Wagon %s: No space found for Box %s (SKU %s).%n", w.getWagonId(), b.getBoxId(), b.getSku());
                }
            }

            // Determine unloading result and update counters
            if (boxesStoredCount == w.getBoxes().size()) {
                System.out.printf("✅ Wagon %s successfully unloaded (%d boxes).%n", w.getWagonId(), boxesStoredCount);
                wagonsSuccessfullyUnloaded++;
            } else if (anyBoxStored) {
                System.out.printf("⚠️ Wagon %s partially unloaded (%d of %d boxes stored - no space for remaining).%n", w.getWagonId(), boxesStoredCount, w.getBoxes().size());
                wagonsPartiallyUnloaded++;
                auditLog.writeLine(String.format("Wagon %s | action=PartiallyUnloaded | reason=WarehousesFull%n", w.getWagonId()));
            } else {
                System.out.printf("❌ Wagon %s not unloaded (no space for any boxes).%n", w.getWagonId());
                wagonsNotUnloaded++;
                auditLog.writeLine(String.format("Wagon %s | action=NotUnloaded | reason=AllWarehousesFull%n", w.getWagonId()));
            }
        }

        // Print unloading summary
        System.out.printf("%n--- Wagon Unloading Summary ---%n");
        System.out.printf("Total Wagons Processed: %d%n", wagons.size());
        System.out.printf("  Fully Unloaded:       %d%n", wagonsSuccessfullyUnloaded);
        System.out.printf("  Partially Unloaded:   %d%n", wagonsPartiallyUnloaded);
        System.out.printf("  Not Unloaded:         %d%n", wagonsNotUnloaded);
        System.out.println("-------------------------------");
    }

    /**
     * USEI05 - Processes returns in quarantine using LIFO order.
     * Inspects returns and either restocks or discards them based on condition.
     */
    public void processReturns() {
        System.out.println("\n--- Processing Returns (USEI05) ---");
        if (quarantine == null || quarantine.isEmpty()) {
            System.out.println("ℹ️ Quarantine empty. No returns to process.");
            return;
        }
        if (warehouses.isEmpty()) {
            System.err.println("❌ FATAL ERROR: No warehouses to restock returned items!");
            return;
        }

        int processed = 0;
        int restocked = 0;
        int discarded = 0;

        // Process returns in LIFO order (latest first)
        while (!quarantine.isEmpty()) {
            Return r = quarantine.getNextReturn();
            processed++;
            if (r == null) continue;

            System.out.printf("  Processing Return %s (SKU: %s, Qty: %d, Reason: %s, Expiry: %s)...%n",
                    r.getReturnId(), r.getSku(), r.getQty(), r.getReason(),
                    r.getExpiryDate() != null ? r.getExpiryDate().toLocalDate() : "N/A");

            if (r.isRestockable()) {
                if (inventory.restock(r, warehouses)) {
                    auditLog.writeLog(r, "Restocked", r.getQty());
                    restocked++;
                } else {
                    System.out.printf("  ⚠️ Item %s (Return %s) was restockable but no space available. Will be discarded.%n", r.getSku(), r.getReturnId());
                    auditLog.writeLog(r, "Discarded (No Space)", r.getQty());
                    discarded++;
                }
            } else {
                System.out.printf("  Item %s (Return %s) is not restockable. Will be discarded.%n", r.getSku(), r.getReturnId());
                auditLog.writeLog(r, "Discarded", r.getQty());
                discarded++;
            }
        }

        // Print returns processing summary
        System.out.println("\n--- Returns Processing Summary ---");
        System.out.printf("Total Returns Processed: %d%n", processed);
        System.out.printf("  Successfully Restocked: %d%n", restocked);
        System.out.printf("  Discarded:              %d%n", discarded);
        System.out.println("----------------------------------");
    }
}
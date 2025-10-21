package pt.ipp.isep.dei.domain;

import java.util.List;

public class WMS {
    private final Quarantine quarantine;
    private final Inventory inventory;
    private final AuditLog auditLog;
    private final List<Warehouse> warehouses;

    public WMS(Quarantine quarantine, Inventory inventory, AuditLog auditLog, List<Warehouse> warehouses) {
        this.quarantine = quarantine;
        this.inventory = inventory;
        this.auditLog = auditLog;
        this.warehouses = warehouses;
    }

    /** USEI01 - Unload wagons (com gestão de capacidade e múltiplos warehouses) */
    public void unloadWagons(List<Wagon> wagons) {
        for (Wagon w : wagons) {
            boolean allStored = true;

            for (Box b : w.getBoxes()) {
                boolean stored = false;

                // tenta colocar a box num warehouse (por ordem ASC)
                for (Warehouse wh : warehouses) {
                    if (wh.storeBox(b)) {
                        inventory.insertBoxFEFO(b); // insere também no inventário lógico
                        stored = true;
                        break;
                    }
                }

                if (!stored) {
                    allStored = false;
                }
            }

            if (allStored) {
                System.out.printf("✅ Wagon %s unloaded successfully.%n", w.getWagonId());
            } else {
                System.out.printf("⚠️  Wagon %s could not be fully unloaded (no space).%n", w.getWagonId());
                auditLog.writeLine(String.format("Wagon %s | action=NotUnloaded | reason=AllWarehousesFull%n", w.getWagonId()));
            }
        }
        System.out.println("All wagons processed (capacity-aware unloading).");
    }

    /** USEI05 - Returns processing */
    public void processReturns() {
        if (quarantine.isEmpty()) {
            System.out.println("No returns to process.");
            return;
        }

        while (!quarantine.isEmpty()) {
            Return r = quarantine.getNextReturn();
            if (r.isRestockable()) {
                inventory.restock(r);
                auditLog.writeLog(r, "Restocked", r.getQty());
            } else {
                auditLog.writeLog(r, "Discarded", r.getQty());
            }
        }
        System.out.println("All returns processed.");
    }
}

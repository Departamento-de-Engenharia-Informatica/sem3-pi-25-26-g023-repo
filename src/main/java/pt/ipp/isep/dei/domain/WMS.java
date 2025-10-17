package pt.ipp.isep.dei.domain;

import java.util.List;

public class WMS {
    private final Quarantine quarantine;
    private final Inventory inventory;
    private final AuditLog auditLog;

    public WMS(Quarantine quarantine, Inventory inventory, AuditLog auditLog) {
        this.quarantine = quarantine;
        this.inventory = inventory;
        this.auditLog = auditLog;
    }

    /** USEI01 - Unload wagons */
    public void unloadWagons(List<Wagon> wagons) {
        for (Wagon w : wagons) {
            w.unloadTo(inventory);
        }
        System.out.println("All wagons unloaded successfully.");
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
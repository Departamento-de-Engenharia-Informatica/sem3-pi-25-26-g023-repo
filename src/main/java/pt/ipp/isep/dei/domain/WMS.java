package pt.ipp.isep.dei.domain;

import java.time.LocalDate;
import java.util.List;

/**
 * Warehouse Management System - Manages warehouse operations.
 * (Versão 2.1 - "Modo Silencioso")
 */
public class WMS {

    // --- NOVA CLASSE INTERNA ---
    /**
     * Um objeto simples para guardar o resultado do unload
     * e passá-lo para a Main.java.
     */
    public static class UnloadResult {
        public final int totalProcessed;
        public final int fullyUnloaded;
        public final int partiallyUnloaded;
        public final int notUnloaded;
        public int totalBoxes; // Adicionado para um sumário mais completo

        public UnloadResult(int totalProcessed, int fully, int partially, int not, int totalBoxes) {
            this.totalProcessed = totalProcessed;
            this.fullyUnloaded = fully;
            this.partiallyUnloaded = partially;
            this.notUnloaded = not;
            this.totalBoxes = totalBoxes;
        }
    }
    // --- FIM DA NOVA CLASSE ---

    private final Quarantine quarantine;
    private final Inventory inventory;
    private final AuditLog auditLog;
    private final List<Warehouse> warehouses;

    public WMS(Quarantine quarantine, Inventory inventory, AuditLog auditLog, List<Warehouse> warehouses) {
        this.quarantine = quarantine;
        this.inventory = inventory;
        this.auditLog = auditLog;
        this.warehouses = (warehouses != null) ? warehouses : List.of();
    }

    /**
     * USEI01 - Unloads wagons.
     * (Modo Silencioso: Retorna um sumário em vez de imprimir)
     * @param wagons list of wagons to unload
     * @return UnloadResult com o sumário da operação.
     */
    public UnloadResult unloadWagons(List<Wagon> wagons) {
        if (wagons == null || wagons.isEmpty()) {
            // SILENCIADO: System.out.println("No wagons to unload.");
            return new UnloadResult(0, 0, 0, 0, 0);
        }
        if (warehouses.isEmpty()) {
            // SILENCIADO: System.err.println("❌ FATAL ERROR: No warehouses defined...");
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
                    if (wh.storeBox(b)) {
                        inventory.insertBoxFEFO(b);
                        storedThisBox = true;
                        anyBoxStored = true;
                        boxesStoredCount++;
                        break;
                    }
                }
                if (!storedThisBox) {
                    // SILENCIADO: System.out.printf("  ⚠️ Wagon %s: No space found for Box %s...%n", ...);
                }
            }

            // Atualiza contadores
            if (boxesStoredCount == w.getBoxes().size()) {
                // SILENCIADO: System.out.printf("✅ Wagon %s successfully unloaded...%n", ...);
                wagonsSuccessfullyUnloaded++;
            } else if (anyBoxStored) {
                // SILENCIADO: System.out.printf("⚠️ Wagon %s partially unloaded...%n", ...);
                wagonsPartiallyUnloaded++;
                auditLog.writeLine(String.format("Wagon %s | action=PartiallyUnloaded | reason=WarehousesFull%n", w.getWagonId()));
            } else {
                // SILENCIADO: System.out.printf("❌ Wagon %s not unloaded...%n", ...);
                wagonsNotUnloaded++;
                auditLog.writeLine(String.format("Wagon %s | action=NotUnloaded | reason=AllWarehousesFull%n", w.getWagonId()));
            }
            totalBoxesStored += boxesStoredCount;
        }

        // SILENCIADO: O sumário foi removido daqui.

        // RETORNA os dados para a Main
        return new UnloadResult(wagons.size(), wagonsSuccessfullyUnloaded, wagonsPartiallyUnloaded, wagonsNotUnloaded, totalBoxesStored);
    }

    /**
     * USEI05 - Processes returns in quarantine.
     * (Modo Silencioso: Imprime apenas no log)
     */
    public void processReturns() {
        // SILENCIADO: System.out.println("\n--- Processing Returns (USEI05) ---");
        if (quarantine == null || quarantine.isEmpty()) {
            // SILENCIADO: System.out.println("ℹ️ Quarantine empty. No returns to process.");
            return;
        }
        if (warehouses.isEmpty()) {
            // SILENCIADO: System.err.println("❌ FATAL ERROR: No warehouses to restock returned items!");
            return;
        }

        int processed = 0;
        int restocked = 0;
        int discarded = 0;

        while (!quarantine.isEmpty()) {
            Return r = quarantine.getNextReturn();
            processed++;
            if (r == null) continue;

            // SILENCIADO: System.out.printf("  Processing Return %s ...%n", ...);

            if (r.isRestockable()) {
                if (inventory.restock(r, warehouses)) {
                    auditLog.writeLog(r, "Restocked", r.getQty());
                    restocked++;
                } else {
                    // SILENCIADO: System.out.printf("  ⚠️ Item %s (Return %s) was restockable but no space...%n", ...);
                    auditLog.writeLog(r, "Discarded (No Space)", r.getQty());
                    discarded++;
                }
            } else {
                // SILENCIADO: System.out.printf("  Item %s (Return %s) is not restockable...%n", ...);
                auditLog.writeLog(r, "Discarded", r.getQty());
                discarded++;
            }
        }

        // SILENCIADO: O sumário foi removido daqui.
    }
}
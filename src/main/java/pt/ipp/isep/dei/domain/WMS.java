package pt.ipp.isep.dei.domain;

import java.util.List;

/**
 * Warehouse Management System - Gerencia operações do armazém
 */
public class WMS {
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
     * USEI01 - Descarrega vagões para os armazéns
     */
    public void unloadWagons(List<Wagon> wagons) {
        if (wagons == null || wagons.isEmpty()) {
            System.out.println("Nenhum vagão para descarregar.");
            return;
        }
        if (warehouses.isEmpty()) {
            System.err.println("❌ ERRO FATAL: Não há armazéns definidos para guardar as caixas!");
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
                    System.out.printf("  ⚠️ Wagon %s: Não foi encontrado espaço para Box %s (SKU %s).%n", w.getWagonId(), b.getBoxId(), b.getSku());
                }
            }

            if (boxesStoredCount == w.getBoxes().size()) {
                System.out.printf("✅ Wagon %s descarregado com sucesso (%d caixas).%n", w.getWagonId(), boxesStoredCount);
                wagonsSuccessfullyUnloaded++;
            } else if (anyBoxStored) {
                System.out.printf("⚠️ Wagon %s parcialmente descarregado (%d de %d caixas guardadas - sem espaço para as restantes).%n", w.getWagonId(), boxesStoredCount, w.getBoxes().size());
                wagonsPartiallyUnloaded++;
                auditLog.writeLine(String.format("Wagon %s | action=PartiallyUnloaded | reason=WarehousesFull%n", w.getWagonId()));
            } else {
                System.out.printf("❌ Wagon %s não descarregado (sem espaço para nenhuma caixa).%n", w.getWagonId());
                wagonsNotUnloaded++;
                auditLog.writeLine(String.format("Wagon %s | action=NotUnloaded | reason=AllWarehousesFull%n", w.getWagonId()));
            }
        }

        System.out.printf("%n--- Resumo Descarregamento Vagões ---%n");
        System.out.printf("Total Vagões Processados: %d%n", wagons.size());
        System.out.printf("  Completamente Descarregados: %d%n", wagonsSuccessfullyUnloaded);
        System.out.printf("  Parcialmente Descarregados:  %d%n", wagonsPartiallyUnloaded);
        System.out.printf("  Não Descarregados:           %d%n", wagonsNotUnloaded);
        System.out.println("------------------------------------");
    }

    /**
     * USEI05 - Processa devoluções em quarentena
     */
    public void processReturns() {
        System.out.println("\n--- Processando Devoluções (USEI05) ---");
        if (quarantine == null || quarantine.isEmpty()) {
            System.out.println("ℹ️ Quarentena vazia. Nenhuma devolução para processar.");
            return;
        }
        if (warehouses.isEmpty()) {
            System.err.println("❌ ERRO FATAL: Não há armazéns para restockar itens devolvidos!");
            return;
        }

        int processed = 0;
        int restocked = 0;
        int discarded = 0;

        while (!quarantine.isEmpty()) {
            Return r = quarantine.getNextReturn();
            processed++;
            if (r == null) continue;

            System.out.printf("  Processando Return %s (SKU: %s, Qty: %d, Reason: %s, Expiry: %s)...%n",
                    r.getReturnId(), r.getSku(), r.getQty(), r.getReason(),
                    r.getExpiryDate() != null ? r.getExpiryDate().toLocalDate() : "N/A");

            if (r.isRestockable()) {
                if (inventory.restock(r, warehouses)) {
                    auditLog.writeLog(r, "Restocked", r.getQty());
                    restocked++;
                } else {
                    System.out.printf("  ⚠️ Item %s (Return %s) era restockable mas não há espaço. Será descartado.%n", r.getSku(), r.getReturnId());
                    auditLog.writeLog(r, "Discarded (No Space)", r.getQty());
                    discarded++;
                }
            } else {
                System.out.printf("  Item %s (Return %s) não é restockable. Será descartado.%n", r.getSku(), r.getReturnId());
                auditLog.writeLog(r, "Discarded", r.getQty());
                discarded++;
            }
        }

        System.out.println("\n--- Resumo Processamento Devoluções ---");
        System.out.printf("Total Devoluções Processadas: %d%n", processed);
        System.out.printf("  Restockadas com Sucesso:    %d%n", restocked);
        System.out.printf("  Descartadas:                %d%n", discarded);
        System.out.println("---------------------------------------");
    }
}
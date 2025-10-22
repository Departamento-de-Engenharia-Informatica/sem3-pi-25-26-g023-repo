package pt.ipp.isep.dei.domain;

import java.util.List;

public class WMS {
    private final Quarantine quarantine;
    private final Inventory inventory;
    private final AuditLog auditLog;
    private final List<Warehouse> warehouses; // Já tínhamos a lista aqui

    public WMS(Quarantine quarantine, Inventory inventory, AuditLog auditLog, List<Warehouse> warehouses) {
        this.quarantine = quarantine;
        this.inventory = inventory;
        this.auditLog = auditLog;
        // Garante que a lista de warehouses não é nula
        this.warehouses = (warehouses != null) ? warehouses : List.of();
    }

    /** USEI01 - Unload wagons (com gestão de capacidade e múltiplos warehouses) */
    public void unloadWagons(List<Wagon> wagons) {
        if (wagons == null || wagons.isEmpty()) {
            System.out.println("Nenhum vagão para descarregar.");
            return;
        }
        if (warehouses.isEmpty()) {
            System.err.println("❌ ERRO FATAL: Não há armazéns definidos para guardar as caixas!");
            // Logar todos os vagões como não descarregados
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
                // Tenta colocar a box num warehouse (por ordem ASC de ID, assumindo que warehouses está ordenada)
                for (Warehouse wh : warehouses) {
                    if (wh.storeBox(b)) { // storeBox já define aisle/bay na Box 'b'
                        inventory.insertBoxFEFO(b); // Adiciona ao inventário lógico APÓS ter local físico
                        storedThisBox = true;
                        anyBoxStored = true;
                        boxesStoredCount++;
                        // Sai do loop de warehouses assim que guardar a caixa
                        break;
                    }
                }
                // Se saiu do loop de warehouses sem guardar a caixa
                if (!storedThisBox) {
                    System.out.printf("  ⚠️ Wagon %s: Não foi encontrado espaço para Box %s (SKU %s).%n", w.getWagonId(), b.getBoxId(), b.getSku());
                }
            } // Fim loop boxes do vagão

            // Reportar estado do vagão
            if (boxesStoredCount == w.getBoxes().size()) {
                System.out.printf("✅ Wagon %s descarregado com sucesso (%d caixas).%n", w.getWagonId(), boxesStoredCount);
                wagonsSuccessfullyUnloaded++;
            } else if (anyBoxStored) {
                System.out.printf("⚠️ Wagon %s parcialmente descarregado (%d de %d caixas guardadas - sem espaço para as restantes).%n", w.getWagonId(), boxesStoredCount, w.getBoxes().size());
                wagonsPartiallyUnloaded++;
                // Logar como não descarregado completamente pode ser útil
                auditLog.writeLine(String.format("Wagon %s | action=PartiallyUnloaded | reason=WarehousesFull%n", w.getWagonId()));
            } else {
                System.out.printf("❌ Wagon %s não descarregado (sem espaço para nenhuma caixa).%n", w.getWagonId());
                wagonsNotUnloaded++;
                auditLog.writeLine(String.format("Wagon %s | action=NotUnloaded | reason=AllWarehousesFull%n", w.getWagonId()));
            }
        } // Fim loop vagões

        System.out.printf("%n--- Resumo Descarregamento Vagões ---%n"); // Removido %s extra
        System.out.printf("Total Vagões Processados: %d%n", wagons.size());
        System.out.printf("  Completamente Descarregados: %d%n", wagonsSuccessfullyUnloaded);
        System.out.printf("  Parcialmente Descarregados:  %d%n", wagonsPartiallyUnloaded);
        System.out.printf("  Não Descarregados:           %d%n", wagonsNotUnloaded);
        System.out.println("------------------------------------");

    }

    /** USEI05 - Returns processing - CORRIGIDO (Chama Inventory.restock corrigido) */
    public void processReturns() {
        System.out.println("\n--- Processando Devoluções (USEI05) ---");
        if (quarantine == null || quarantine.isEmpty()) {
            System.out.println("ℹ️ Quarentena vazia. Nenhuma devolução para processar.");
            return;
        }
        if (warehouses.isEmpty()) {
            System.err.println("❌ ERRO FATAL: Não há armazéns para restockar itens devolvidos!");
            // Poderia marcar todos como descartados ou logar erro
            return;
        }

        int processed = 0;
        int restocked = 0;
        int discarded = 0;

        while (!quarantine.isEmpty()) {
            Return r = quarantine.getNextReturn();
            processed++;
            if (r == null) continue; // Segurança

            System.out.printf("  Processando Return %s (SKU: %s, Qty: %d, Reason: %s)...%n",
                    r.getReturnId(), r.getSku(), r.getQty(), r.getReason());

            if (r.isRestockable()) {
                // Tenta restockar passando a lista de warehouses
                if (inventory.restock(r, warehouses)) { // Chama o método corrigido em Inventory
                    auditLog.writeLog(r, "Restocked", r.getQty());
                    restocked++;
                } else {
                    // Se não conseguiu restockar por falta de espaço, trata como descarte
                    System.out.printf("  ⚠️ Item %s (Return %s) era restockable mas não há espaço. Será descartado.%n", r.getSku(), r.getReturnId());
                    auditLog.writeLog(r, "Discarded (No Space)", r.getQty()); // Ação modificada no log
                    discarded++;
                }
            } else {
                System.out.printf("  Item %s (Return %s) não é restockable (Reason: %s). Será descartado.%n", r.getSku(), r.getReturnId(), r.getReason());
                auditLog.writeLog(r, "Discarded", r.getQty());
                discarded++;
            }
        } // Fim while

        System.out.println("\n--- Resumo Processamento Devoluções ---");
        System.out.printf("Total Devoluções Processadas: %d%n", processed);
        System.out.printf("  Restockadas com Sucesso:    %d%n", restocked);
        System.out.printf("  Descartadas:                %d%n", discarded);
        System.out.println("---------------------------------------");
    }
}
package pt.ipp.isep.dei.domain;

import java.util.*;
import java.util.stream.Collectors;

/**
 * OrderAllocator - Responsável por alocar inventário às encomendas
 * segundo a política FEFO/FIFO, suportando modos STRICT e PARTIAL.
 */
public class OrderAllocator {

    public enum Mode { STRICT, PARTIAL }

    private Map<String, Item> items; // Referência aos items para calcular pesos

    public OrderAllocator() {
        this.items = new HashMap<>();
    }

    // Método para setar os items (chamado antes de allocateOrders)
    public void setItems(Map<String, Item> items) {
        this.items = items != null ? items : new HashMap<>();
    }

    public AllocationResult allocateOrders(List<Order> orders, List<Box> inventory, Mode mode) {
        AllocationResult result = new AllocationResult();

        if (orders == null || inventory == null) {
            System.out.println("⚠️  Orders ou inventory são null");
            return result;
        }

        if (orders.isEmpty()) {
            System.out.println("⚠️  Não há orders para processar");
            return result;
        }

        if (inventory.isEmpty()) {
            System.out.println("⚠️  O inventário está vazio");
            return result;
        }

        System.out.printf("📦 Processando %d orders com %d boxes no inventário...%n",
                orders.size(), inventory.size());

        // Ordenar encomendas por prioridade, dueDate e orderId
        orders.sort(Comparator
                .comparingInt((Order o) -> o.priority)
                .thenComparing(o -> o.dueDate)
                .thenComparing(o -> o.orderId));

        // Agrupar inventário por SKU
        Map<String, List<Box>> inventoryBySku = inventory.stream()
                .collect(Collectors.groupingBy(Box::getSku));

        // Contadores para estatísticas
        int totalLinesProcessed = 0;
        int totalAllocations = 0;

        for (Order order : orders) {
            // Ordenar as linhas da encomenda
            order.lines.sort(Comparator.comparingInt(l -> l.lineNo));

            for (OrderLine line : order.lines) {
                totalLinesProcessed++;
                int remaining = line.requestedQty;
                int allocated = 0;
                List<Allocation> lineAllocations = new ArrayList<>();

                // Obter boxes do SKU específico
                List<Box> boxesForSku = inventoryBySku.getOrDefault(line.sku, Collections.emptyList());

                if (boxesForSku.isEmpty()) {
                    System.out.printf("  ❌ SKU %s não encontrado no inventário para order %s%n",
                            line.sku, order.orderId);
                }

                // Ordenar boxes segundo FEFO/FIFO
                List<Box> sortedBoxes = boxesForSku.stream()
                        .filter(b -> b.getQtyAvailable() > 0)
                        .sorted((b1, b2) -> {
                            // FEFO: items com expiryDate vêm primeiro
                            if (b1.getExpiryDate() != null && b2.getExpiryDate() != null) {
                                int cmp = b1.getExpiryDate().compareTo(b2.getExpiryDate());
                                if (cmp != 0) return cmp;
                                // Empate: FIFO por receivedDate
                                return b1.getReceivedDate().compareTo(b2.getReceivedDate());
                            } else if (b1.getExpiryDate() != null) {
                                return -1; // b1 tem expiryDate, b2 não → b1 vem primeiro
                            } else if (b2.getExpiryDate() != null) {
                                return 1;  // b2 tem expiryDate, b1 não → b2 vem primeiro
                            } else {
                                // Ambos sem expiryDate: FIFO por receivedDate
                                return b1.getReceivedDate().compareTo(b2.getReceivedDate());
                            }
                        })
                        .collect(Collectors.toList());

                // Tentar satisfazer a linha da encomenda
                for (Box box : sortedBoxes) {
                    if (remaining <= 0) break;
                    if (box.getQtyAvailable() <= 0) continue;

                    int take = Math.min(remaining, box.getQtyAvailable());
                    if (take <= 0) continue;

                    // Calcular peso para esta alocação
                    double allocationWeight = getItemWeight(line.sku) * take;

                    // Criar registo de alocação
                    Allocation allocation = new Allocation(
                            order.orderId,
                            line.lineNo,
                            line.sku,
                            take,
                            allocationWeight,
                            box.getBoxId(),
                            box.getAisle(),
                            box.getBay()
                    );
                    lineAllocations.add(allocation);

                    // Atualizar stock da box (apenas na simulação - não persiste)
                    box.qtyAvailable -= take;
                    remaining -= take;
                    allocated += take;
                    totalAllocations++;

                    System.out.printf("  ✅ Alocado: Order %s Line %d - %d unidades de %s (Box %s)%n",
                            order.orderId, line.lineNo, take, line.sku, box.getBoxId());
                }

                // Determinar status da linha conforme o modo
                Status status;
                if (mode == Mode.STRICT) {
                    if (allocated == line.requestedQty) {
                        status = Status.ELIGIBLE;
                        System.out.printf("  🟢 ELIGIBLE: Order %s Line %d - %d/%d unidades%n",
                                order.orderId, line.lineNo, allocated, line.requestedQty);
                    } else {
                        status = Status.UNDISPATCHABLE;
                        System.out.printf("  🔴 UNDISPATCHABLE: Order %s Line %d - %d/%d unidades%n",
                                order.orderId, line.lineNo, allocated, line.requestedQty);

                        // Rollback - devolver stock às boxes
                        for (Allocation a : lineAllocations) {
                            inventoryBySku.get(line.sku).stream()
                                    .filter(b -> b.getBoxId().equals(a.boxId))
                                    .findFirst()
                                    .ifPresent(b -> b.qtyAvailable += a.qty);
                        }
                        lineAllocations.clear();
                        allocated = 0;
                    }
                } else { // Mode.PARTIAL
                    if (allocated == 0) {
                        status = Status.UNDISPATCHABLE;
                        System.out.printf("  🔴 UNDISPATCHABLE: Order %s Line %d - 0/%d unidades%n",
                                order.orderId, line.lineNo, line.requestedQty);
                    } else if (allocated < line.requestedQty) {
                        status = Status.PARTIAL;
                        System.out.printf("  🟡 PARTIAL: Order %s Line %d - %d/%d unidades%n",
                                order.orderId, line.lineNo, allocated, line.requestedQty);
                    } else {
                        status = Status.ELIGIBLE;
                        System.out.printf("  🟢 ELIGIBLE: Order %s Line %d - %d/%d unidades%n",
                                order.orderId, line.lineNo, allocated, line.requestedQty);
                    }
                }

                // Guardar resultado da linha
                Eligibility eligibility = new Eligibility(
                        order.orderId,
                        line.lineNo,
                        line.sku,
                        line.requestedQty,
                        allocated,
                        status
                );
                result.eligibilityList.add(eligibility);
                result.allocations.addAll(lineAllocations);
            }
        }

        System.out.printf("📊 USEI02 Concluído: %d linhas processadas, %d alocações geradas%n",
                totalLinesProcessed, totalAllocations);

        // Estatísticas finais
        Map<Status, Long> statusCount = result.eligibilityList.stream()
                .collect(Collectors.groupingBy(e -> e.status, Collectors.counting()));

        System.out.println("📈 Estatísticas Finais:");
        statusCount.forEach((status, count) ->
                System.out.printf("  %s: %d linhas%n", status, count));
        System.out.printf("  Total de alocações: %d%n", result.allocations.size());

        return result;
    }

    private double getItemWeight(String sku) {
        if (items == null || items.isEmpty()) {
            System.out.printf("⚠️  Mapa de items vazio ou null para SKU %s%n", sku);
            return 1.0; // Peso padrão
        }

        Item item = items.get(sku);
        if (item == null) {
            System.out.printf("⚠️  SKU %s não encontrado no mapa de items%n", sku);
            return 1.0; // Peso padrão
        }

        double weight = item.getUnitWeight();
        System.out.printf("  📦 Peso do SKU %s: %.2f kg/unidade%n", sku, weight);
        return weight;
    }

    /**
     * Método auxiliar para debug - mostra informações sobre o inventário
     */
    public void printInventoryInfo(List<Box> inventory) {
        if (inventory == null || inventory.isEmpty()) {
            System.out.println("📭 Inventário vazio");
            return;
        }

        System.out.println("📦 Informação do Inventário:");
        Map<String, List<Box>> bySku = inventory.stream()
                .collect(Collectors.groupingBy(Box::getSku));

        bySku.forEach((sku, boxes) -> {
            int totalQty = boxes.stream().mapToInt(Box::getQtyAvailable).sum();
            int boxCount = boxes.size();
            System.out.printf("  %s: %d boxes, %d unidades totais%n", sku, boxCount, totalQty);

            // Mostrar detalhes das boxes ordenadas por FEFO/FIFO
            boxes.stream()
                    .sorted((b1, b2) -> {
                        if (b1.getExpiryDate() != null && b2.getExpiryDate() != null) {
                            int cmp = b1.getExpiryDate().compareTo(b2.getExpiryDate());
                            if (cmp != 0) return cmp;
                            return b1.getReceivedDate().compareTo(b2.getReceivedDate());
                        } else if (b1.getExpiryDate() != null) return -1;
                        else if (b2.getExpiryDate() != null) return 1;
                        else return b1.getReceivedDate().compareTo(b2.getReceivedDate());
                    })
                    .forEach(box -> {
                        String expiryInfo = box.getExpiryDate() != null ?
                                box.getExpiryDate().toString() : "Sem expiry";
                        System.out.printf("    - Box %s: %d unidades, Exp: %s, Receb: %s%n",
                                box.getBoxId(), box.getQtyAvailable(), expiryInfo,
                                box.getReceivedDate().toLocalDate());
                    });
        });
    }
}
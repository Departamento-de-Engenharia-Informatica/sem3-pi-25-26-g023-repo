package pt.ipp.isep.dei.domain;

import java.util.*;
import java.util.stream.Collectors;

/**
 * OrderAllocator - Responsável por alocar inventário às encomendas
 * segundo a política FEFO/FIFO, suportando modos STRICT e PARTIAL.
 */
public class OrderAllocator {

    public OrderAllocator() {

    }

    public enum Mode { STRICT, PARTIAL }


    public AllocationResult allocateOrders(List<Order> orders, List<Box> inventory, Mode mode) {
        AllocationResult result = new AllocationResult();

        if (orders == null || inventory == null) return result;

        // Ordenar encomendas por prioridade, dueDate e orderId
        orders.sort(Comparator
                .comparingInt((Order o) -> o.priority)
                .thenComparing(o -> o.dueDate)
                .thenComparing(o -> o.orderId)); // orderId é String

        // Agrupar inventário por SKU
        Map<String, List<Box>> inventoryBySku = inventory.stream()
                .collect(Collectors.groupingBy(Box::getSku));

        for (Order order : orders) {
            // Ordenar as linhas da encomenda
            order.lines.sort(Comparator.comparingInt(l -> l.lineNo));

            for (OrderLine line : order.lines) {
                int remaining = line.requestedQty;
                int allocated = 0;
                List<Allocation> lineAllocations = new ArrayList<>();

                // Obter boxes do SKU específico
                List<Box> boxesForSku = inventoryBySku.getOrDefault(line.sku, Collections.emptyList());

                // Ordenar boxes segundo FEFO/FIFO
                List<Box> sortedBoxes = boxesForSku.stream()
                        .filter(b -> b.getQtyAvailable() > 0)
                        .sorted((b1, b2) -> {
                            if (b1.getExpiryDate() != null && b2.getExpiryDate() != null) {
                                int cmp = b1.getExpiryDate().compareTo(b2.getExpiryDate());
                                if (cmp != 0) return cmp;
                                return b1.getReceivedDate().compareTo(b2.getReceivedDate());
                            } else if (b1.getExpiryDate() != null) {
                                return -1;
                            } else if (b2.getExpiryDate() != null) {
                                return 1;
                            } else {
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
                    double weight = 0.0;



                    // Criar registo de alocação
                    lineAllocations.add(new Allocation(
                            order.orderId,
                            line.lineNo,
                            line.sku,
                            take,
                            box.getBoxId(),
                            box.getAisle(),
                            box.getBay(),
                            weight));

                    box.qtyAvailable -= take; // atualizar stock
                    remaining -= take;
                    allocated += take;
                }

                // Determinar status da linha conforme o modo
                Status status;
                if (mode == Mode.STRICT) {
                    if (allocated == line.requestedQty) {
                        status = Status.ELIGIBLE;
                    } else {
                        status = Status.UNDISPATCHABLE;
                        // rollback
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
                    if (allocated == 0) status = Status.UNDISPATCHABLE;
                    else if (allocated < line.requestedQty) status = Status.PARTIAL;
                    else status = Status.ELIGIBLE;
                }

                // Guardar resultado da linha
                result.eligibilityList.add(
                        new Eligibility(order.orderId, line.lineNo, line.sku,
                                line.requestedQty, allocated, status)
                );

                result.allocations.addAll(lineAllocations);
            }
        }
        return result;
    }
}

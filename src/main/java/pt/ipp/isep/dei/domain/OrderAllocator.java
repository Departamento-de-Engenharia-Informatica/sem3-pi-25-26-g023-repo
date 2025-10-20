package pt.ipp.isep.dei.domain;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

public class OrderAllocator {

    public enum Mode { STRICT, PARTIAL }

    public AllocationResult allocateOrders(List<Order> orders, List<Box> inventory, Mode mode) {
        AllocationResult result = new AllocationResult();

        orders.sort(Comparator
                .comparingInt((Order o) -> o.priority)
                .thenComparing(o -> o.dueDate)
                .thenComparingInt(o -> o.orderId));

        Map<String, List<Box>> inventoryBySku = inventory.stream()
                .collect(Collectors.groupingBy(b -> b.sku));

        for (Order order : orders) {
            order.lines.sort(Comparator.comparingInt(l -> l.lineNo));

            for (OrderLine line : order.lines) {
                int remaining = line.requestedQty;
                int allocated = 0;
                List<Allocation> lineAllocations = new ArrayList<>();

                List<Box> boxesForSku = inventoryBySku.getOrDefault(line.sku, Collections.emptyList());

                List<Box> sortedBoxes = boxesForSku.stream()
                        .filter(b -> b.qtyAvailable > 0)
                        .sorted((b1, b2) -> {
                            if (b1.expiryDate != null && b2.expiryDate != null) {
                                int cmp = b1.expiryDate.compareTo(b2.expiryDate);
                                if (cmp != 0) return cmp;
                                return b1.receivedDate.compareTo(b2.receivedDate);
                            } else if (b1.expiryDate != null) {
                                return -1;
                            } else if (b2.expiryDate != null) {
                                return 1;
                            } else {
                                return b1.receivedDate.compareTo(b2.receivedDate);
                            }
                        })
                        .collect(Collectors.toList());

                for (Box box : sortedBoxes) {
                    if (remaining <= 0) break;
                    if (box.qtyAvailable <= 0) continue;

                    int take = Math.min(remaining, box.qtyAvailable);
                    if (take <= 0) continue;

                    lineAllocations.add(new Allocation(order.orderId, line.lineNo, line.sku,
                            take, box.boxId, box.aisle, box.bay));

                    box.qtyAvailable -= take;
                    remaining -= take;
                    allocated += take;
                }

                Status status;
                if (mode == Mode.STRICT) {
                    if (allocated == line.requestedQty) {
                        status = Status.ELIGIBLE;
                    } else {
                        status = Status.UNDISPATCHABLE;
                        // rollback allocations
                        for (Allocation a : lineAllocations) {
                            inventoryBySku.get(line.sku).stream()
                                    .filter(b -> b.boxId.equals(a.boxId))
                                    .findFirst()
                                    .ifPresent(b -> b.qtyAvailable += a.qty);
                        }
                        lineAllocations.clear();
                        allocated = 0;
                    }
                } else {
                    if (allocated == 0) status = Status.UNDISPATCHABLE;
                    else if (allocated < line.requestedQty) status = Status.PARTIAL;
                    else status = Status.ELIGIBLE;
                }

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

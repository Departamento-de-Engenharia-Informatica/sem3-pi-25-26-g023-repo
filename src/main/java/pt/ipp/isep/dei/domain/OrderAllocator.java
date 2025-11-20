package pt.ipp.isep.dei.domain;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Allocates boxes from inventory to orders using STRICT or PARTIAL rules.
 * <p>
 * Orders are processed by priority and due date. Boxes are chosen by FEFO (expiry first)
 * or FIFO (received first). Results are returned as an {@link AllocationResult}.
 * </p>
 */
public class OrderAllocator {

    /** Allocation mode: STRICT (all or nothing) or PARTIAL (allow partial allocation). */
    public enum Mode {
        STRICT,
        PARTIAL
    } // <--- ESTE ENUM ESTAVA EM FALTA!

    private Map<String, Item> items;

    /** Creates an OrderAllocator with an empty item list. */
    public OrderAllocator() {

        this.items = new HashMap<>();
    }

    /**
     * Sets the items map (used to calculate weights).
     * @param items a map of items (can be null)
     */
    public void setItems(Map<String, Item> items) {

        this.items = items != null ? items : new HashMap<>();
    }

    /**
     * Allocates stock from boxes to order lines.
     * <p>
     * In STRICT mode, a line must be fully satisfied or it gets nothing.
     * In PARTIAL mode, partial allocations are allowed.
     * </p>
     *
     * @param orders the list of orders
     * @param inventory the list of boxes (inventory)
     * @param mode allocation mode
     * @return allocation results with eligibility and allocations
     */
    public AllocationResult allocateOrders(List<Order> orders, List<Box> inventory, Mode mode) {
        AllocationResult result = new AllocationResult();

        // Copy lists to avoid errors with List.of(...)
        List<Order> ordersToProcess = new ArrayList<>(orders == null ? Collections.emptyList() : orders);
        List<Box> stock = new ArrayList<>(inventory == null ? Collections.emptyList() : inventory);

        if (ordersToProcess.isEmpty()) {
            System.out.println("⚠️ No orders to process");
            return result;
        }

        // If no stock, mark all lines as UNDISPATCHABLE
        if (stock.isEmpty()) {
            System.out.println("⚠️ Inventory is empty");
            for (Order o : ordersToProcess) {
                for (OrderLine l : o.lines) {
                    result.eligibilityList.add(
                            new Eligibility(o.orderId, l.lineNo, l.sku, l.requestedQty, 0, Status.UNDISPATCHABLE)
                    );
                }
            }
            return result;
        }

        System.out.printf("📦 Processing %d orders with %d boxes...%n",
                ordersToProcess.size(), stock.size());

        // Sort orders by priority, due date, then ID
        ordersToProcess.sort(Comparator
                .comparingInt((Order o) -> o.priority)
                .thenComparing(o -> o.dueDate)
                .thenComparing(o -> o.orderId));

        // Group boxes by SKU
        Map<String, List<Box>> inventoryBySku = stock.stream()
                .collect(Collectors.groupingBy(Box::getSku));

        for (Order order : ordersToProcess) {
            List<OrderLine> linesSorted = order.lines.stream()
                    .sorted(Comparator.comparingInt(l -> l.lineNo))
                    .collect(Collectors.toList());

            for (OrderLine line : linesSorted) {
                int remaining = line.requestedQty;
                int allocated = 0;
                List<Allocation> lineAllocations = new ArrayList<>();

                List<Box> boxesForSku = inventoryBySku.getOrDefault(line.sku, Collections.emptyList());

                // Sort boxes (FEFO/FIFO)
                List<Box> sortedBoxes = boxesForSku.stream()
                        .filter(b -> b.getQtyAvailable() > 0)
                        .sorted(Comparator
                                .comparing((Box b) -> b.getExpiryDate() == null)
                                .thenComparing(b -> {
                                    if (b.getExpiryDate() != null)
                                        return b.getExpiryDate().atStartOfDay();
                                    else
                                        return b.getReceivedDate();
                                })
                        ).toList();

                // Allocate
                for (Box box : sortedBoxes) {
                    if (remaining <= 0) break;
                    int take = Math.min(remaining, box.getQtyAvailable());
                    if (take <= 0) continue;

                    double weight = getItemWeight(line.sku) * take;
                    lineAllocations.add(new Allocation(order.orderId, line.lineNo, line.sku, take, weight,
                            box.getBoxId(), box.getAisle(), box.getBay()));

                    box.qtyAvailable -= take;
                    remaining -= take;
                    allocated += take;
                }

                // Determine status
                Status status;
                if (mode == Mode.STRICT) {
                    if (allocated == line.requestedQty) {
                        status = Status.ELIGIBLE;
                    } else {
                        status = Status.UNDISPATCHABLE;
                        // Undo partial allocation
                        for (Allocation a : lineAllocations) {
                            inventoryBySku.getOrDefault(line.sku, Collections.emptyList()).stream()
                                    .filter(b -> b.getBoxId().equals(a.boxId))
                                    .findFirst()
                                    .ifPresent(b -> b.qtyAvailable += a.qty);
                        }
                        lineAllocations.clear();
                        allocated = 0;
                    }
                } else { // PARTIAL
                    if (allocated == 0)
                        status = Status.UNDISPATCHABLE;
                    else if (allocated < line.requestedQty)
                        status = Status.PARTIAL;
                    else
                        status = Status.ELIGIBLE;
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

    /**
     * Gets the weight of an item or returns 1.0 if not found.
     * @param sku the SKU
     * @return unit weight
     */
    private double getItemWeight(String sku) {
        if (items == null || items.isEmpty()) return 1.0;
        Item item = items.get(sku);
        return item != null ? item.getUnitWeight() : 1.0;
    }

    /**
     * Prints inventory summary by SKU.
     * @param inventory list of boxes
     */
    public void printInventoryInfo(List<Box> inventory) {
        if (inventory == null || inventory.isEmpty()) {
            System.out.println("📭 Empty inventory");
            return;
        }

        System.out.println("📦 Inventory Info:");
        Map<String, List<Box>> bySku = inventory.stream()
                .collect(Collectors.groupingBy(Box::getSku));

        bySku.forEach((sku, boxes) -> {
            int totalQty = boxes.stream().mapToInt(Box::getQtyAvailable).sum();
            System.out.printf("  %s: %d boxes, %d units%n", sku, boxes.size(), totalQty);
        });
    }
}
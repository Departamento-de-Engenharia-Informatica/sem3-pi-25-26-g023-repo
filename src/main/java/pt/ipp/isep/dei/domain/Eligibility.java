package pt.ipp.isep.dei.domain;

/**
 * Represents the allocation status of a specific order line.
 * Each {@code Eligibility} record shows how much of a requested quantity
 * was allocated and whether the line is fully eligible, partially allocated,
 * or not dispatchable.
 */
public class Eligibility {

    /** ID of the order this record belongs to. */
    public final String orderId;

    /** Line number within the order. */
    public final int lineNo;

    /** SKU (Stock Keeping Unit) of the ordered item. */
    public final String sku;

    /** Quantity originally requested in the order line. */
    public final int requestedQty;

    /** Quantity successfully allocated from stock. */
    public final int allocatedQty;

    /** Allocation status (e.g., ELIGIBLE, PARTIAL, UNDISPATCHABLE). */
    public final Status status;

    /**
     * Creates a new {@code Eligibility} record.
     *
     * @param orderId       the order ID
     * @param lineNo        the order line number
     * @param sku           the SKU code
     * @param requestedQty  the quantity requested
     * @param allocatedQty  the quantity allocated
     * @param status        the allocation status
     */
    public Eligibility(String orderId, int lineNo, String sku,
                       int requestedQty, int allocatedQty, Status status) {
        this.orderId = orderId;
        this.lineNo = lineNo;
        this.sku = sku;
        this.requestedQty = requestedQty;
        this.allocatedQty = allocatedQty;
        this.status = status;
    }

    /**
     * @return formatted string with order, line, SKU, quantities, and status
     */
    @Override
    public String toString() {
        return String.format("Order %s Line %d SKU %s | Req=%d Alloc=%d -> %s",
                orderId, lineNo, sku, requestedQty, allocatedQty, status);
    }
}


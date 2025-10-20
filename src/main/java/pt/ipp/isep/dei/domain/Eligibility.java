package pt.ipp.isep.dei.domain;

public class Eligibility {
    public final String orderId; // alterado para String
    public final int lineNo;
    public final String sku;
    public final int requestedQty;
    public final int allocatedQty;
    public final Status status;

    public Eligibility(String orderId, int lineNo, String sku,
                       int requestedQty, int allocatedQty, Status status) {
        this.orderId = orderId;
        this.lineNo = lineNo;
        this.sku = sku;
        this.requestedQty = requestedQty;
        this.allocatedQty = allocatedQty;
        this.status = status;
    }

    @Override
    public String toString() {
        return String.format("Order %s Line %d SKU %s | Req=%d Alloc=%d -> %s",
                orderId, lineNo, sku, requestedQty, allocatedQty, status);
    }
}

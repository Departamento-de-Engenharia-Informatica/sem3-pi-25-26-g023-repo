package pt.ipp.isep.dei.domain;

public class Allocation {
    public final String orderId;
    public final int lineNo;
    public final String sku;
    public final int qty;
    public final double weight; // CAMPO ADICIONADO
    public final String boxId;
    public final String aisle;
    public final String bay;

    public Allocation(String orderId, int lineNo, String sku, int qty,
                      double weight, String boxId, String aisle, String bay) {
        this.orderId = orderId;
        this.lineNo = lineNo;
        this.sku = sku;
        this.qty = qty;
        this.weight = weight;
        this.boxId = boxId;
        this.aisle = aisle;
        this.bay = bay;
    }

    @Override
    public String toString() {
        return String.format("Order %s Line %d SKU %s Qty %d Box %s (%s-%s)",
                orderId, lineNo, sku, qty, boxId, aisle, bay);
    }
}
package pt.ipp.isep.dei.domain;

public class PickingAssignment {
    private final String orderId;
    private final int lineNo;
    private final Item item;
    private final int quantity;
    private final String boxId;
    private final String aisle;
    private final String bay;
    private final double totalWeight;
    private PickingStatus status;

    public PickingAssignment(String orderId, int lineNo, Item item, int quantity,
                             String boxId, String aisle, String bay) {
        this.orderId = orderId;
        this.lineNo = lineNo;
        this.item = item;
        this.quantity = quantity;
        this.boxId = boxId;
        this.aisle = aisle;
        this.bay = bay;
        this.totalWeight = item.getUnitWeight() * quantity;
        this.status = PickingStatus.PENDING;
    }

    public String getLocation() { return aisle + "-" + bay; }
    public String getSku() { return item.getSku(); }

    // Getters
    public String getOrderId() { return orderId; }
    public int getLineNo() { return lineNo; }
    public Item getItem() { return item; }
    public int getQuantity() { return quantity; }
    public String getBoxId() { return boxId; }
    public String getAisle() { return aisle; }
    public String getBay() { return bay; }
    public double getTotalWeight() { return totalWeight; }
    public PickingStatus getStatus() { return status; }
    public void setStatus(PickingStatus status) { this.status = status; }

    @Override
    public String toString() {
        return String.format("%s-L%d: %s x%d @ %s", orderId, lineNo, getSku(), quantity, getLocation());
    }
}

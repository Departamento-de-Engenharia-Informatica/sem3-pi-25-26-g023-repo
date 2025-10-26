package pt.ipp.isep.dei.domain;

/**
 * Represents a picking assignment for warehouse order fulfillment.
 */
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

    /**
     * Creates a new picking assignment.
     */
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

    /** Returns the location as aisle-bay combination. */
    public String getLocation() { return aisle + "-" + bay; }

    /** Returns the SKU of the item. */
    public String getSku() { return item.getSku(); }

    // Getters and setters
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

    /** Returns string representation of the picking assignment. */
    @Override
    public String toString() {
        return String.format("%s-L%d: %s x%d @ %s", orderId, lineNo, getSku(), quantity, getLocation());
    }
}
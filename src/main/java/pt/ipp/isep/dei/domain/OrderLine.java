package pt.ipp.isep.dei.domain;

/**
 * Represents a single line item within a purchase order.
 * Each order line specifies a product (SKU) and the quantity requested for that product.
 * This class is immutable to ensure data integrity throughout order processing.
 */
public class OrderLine {

    /**
     * The sequential number identifying this line within the order.
     * Used to maintain the original order of line items.
     */
    public final int lineNo;

    /**
     * The Stock Keeping Unit (SKU) identifier for the product being ordered.
     * References a specific product in the inventory system.
     */
    public final String sku;

    /**
     * The quantity of the product requested in this order line.
     * Must be a positive integer value.
     */
    public final int requestedQty;

    /**
     * Constructs a new OrderLine with the specified line number, SKU, and quantity.
     *
     * @param lineNo the sequential line number within the order (must be positive)
     * @param sku the stock keeping unit identifier for the product (cannot be null or empty)
     * @param requestedQty the quantity of product requested (must be positive)
     * @throws IllegalArgumentException if lineNo or requestedQty is not positive,
     *                                  or if sku is null or empty
     */
    public OrderLine(int lineNo, String sku, int requestedQty) {
        if (lineNo <= 0) {
            throw new IllegalArgumentException("Line number must be positive");
        }
        if (sku == null || sku.trim().isEmpty()) {
            throw new IllegalArgumentException("SKU cannot be null or empty");
        }
        if (requestedQty <= 0) {
            throw new IllegalArgumentException("Requested quantity must be positive");
        }

        this.lineNo = lineNo;
        this.sku = sku.trim();
        this.requestedQty = requestedQty;
    }

    /**
     * Returns the line number of this order line.
     *
     * @return the sequential line number within the order
     */
    public int getLineNo() {

        return lineNo;
    }

    /**
     * Returns the SKU (Stock Keeping Unit) of the product in this order line.
     *
     * @return the product SKU identifier
     */
    public String getSku() {

        return sku;
    }

    /**
     * Compares this order line to the specified object for equality.
     * Two order lines are considered equal if they have the same line number, SKU, and quantity.
     *
     * @param obj the object to compare with
     * @return true if the objects are equal, false otherwise
     */

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;

        OrderLine orderLine = (OrderLine) obj;
        return lineNo == orderLine.lineNo &&
                requestedQty == orderLine.requestedQty &&
                sku.equals(orderLine.sku);
    }

    /**
     * Returns a hash code value for this order line.
     * The hash code is based on the line number, SKU, and requested quantity.
     *
     * @return a hash code value for this object
     */
    @Override
    public int hashCode() {
        int result = Integer.hashCode(lineNo);
        result = 31 * result + sku.hashCode();
        result = 31 * result + Integer.hashCode(requestedQty);
        return result;
    }

    /**
     * Returns a string representation of this order line in the format:
     * "OrderLine[lineNo=X, sku=Y, requestedQty=Z]"
     *
     * @return a string representation of the order line
     */
    @Override
    public String toString() {
        return String.format("OrderLine[lineNo=%d, sku=%s, requestedQty=%d]",
                lineNo, sku, requestedQty);
    }
}
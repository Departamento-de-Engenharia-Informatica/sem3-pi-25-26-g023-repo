package pt.ipp.isep.dei.domain;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Represents a physical box in inventory that stores a specific SKU.
 * <p>
 * Each box contains a certain quantity of items and may include
 * information about expiration date, reception date, and location
 * (aisle and bay) in the warehouse.
 * </p>
 */
public class Box implements Comparable<Box> {

    /** Unique identifier of the box. */
    public final String boxId;

    /** SKU (Stock Keeping Unit) stored in this box. */
    public final String sku;

    /** Quantity of items currently available in the box. */
    public int qtyAvailable;

    /** Expiration date of the items, or {@code null} if non-perishable. */
    public final LocalDate expiryDate;

    /** Date and time when the box was received. */
    public final LocalDateTime receivedDate;

    /** Aisle location in the warehouse. */
    private String aisle;

    /** Bay location in the warehouse. */
    private String bay;

    /**
     * Creates a box with an expiration date represented as {@link LocalDate}.
     *
     * @param boxId        unique box identifier
     * @param sku          SKU code stored in this box
     * @param qtyAvailable available quantity
     * @param expiryDate   expiration date (may be {@code null})
     * @param receivedDate date and time when the box was received
     * @param aisle        aisle where the box is located
     * @param bay          bay where the box is located
     */
    public Box(String boxId, String sku, int qtyAvailable,
               LocalDate expiryDate, LocalDateTime receivedDate,
               String aisle, String bay) {
        this.boxId = Objects.requireNonNull(boxId);
        this.sku = Objects.requireNonNull(sku);
        this.qtyAvailable = qtyAvailable;
        this.expiryDate = expiryDate;
        this.receivedDate = receivedDate;
        this.aisle = aisle;
        this.bay = bay;
    }

    /**
     * Creates a box with an expiration date represented as {@link LocalDateTime}.
     *
     * @param boxId           unique box identifier
     * @param sku             SKU code stored in this box
     * @param qtyAvailable    available quantity
     * @param expiryDateTime  expiration date and time (may be {@code null})
     * @param receivedDate    date and time when the box was received
     * @param aisle           aisle where the box is located
     * @param bay             bay where the box is located
     */
    public Box(String boxId, String sku, int qtyAvailable,
               LocalDateTime expiryDateTime, LocalDateTime receivedDate,
               String aisle, String bay) {
        this(boxId, sku, qtyAvailable,
                (expiryDateTime != null) ? expiryDateTime.toLocalDate() : null,
                receivedDate, aisle, bay);
    }

    public String getBoxId() { return boxId; }

    public String getSku() { return sku; }

    public int getQtyAvailable() { return qtyAvailable; }

    public String getAisle() { return aisle; }


    public String getBay() { return bay; }

    public LocalDate getExpiryDate() { return expiryDate; }

    public LocalDateTime getReceivedDate() { return receivedDate; }

    /** Sets the aisle location. */
    public void setAisle(String aisle) { this.aisle = aisle; }

    /** Sets the bay location. */
    public void setBay(String bay) { this.bay = bay; }

    /**
     * Compares boxes using FEFO for perishable items
     * and FIFO for non-perishable items.
     * Boxes with earlier expiration or reception dates are considered smaller.
     * @param other another box to compare
     * @return a negative value, zero, or a positive value depending on order
     */
    @Override
    public int compareTo(Box other) {
        if (other == null) return -1;

        // Perishable first
        if (this.expiryDate == null && other.expiryDate != null) return 1;
        if (this.expiryDate != null && other.expiryDate == null) return -1;

        // FEFO for perishables
        if (this.expiryDate != null && other.expiryDate != null) {
            int cmp = this.expiryDate.compareTo(other.expiryDate);
            if (cmp != 0) return cmp;
        }

        // FIFO for received date
        if (this.receivedDate != null && other.receivedDate != null) {
            int cmp = this.receivedDate.compareTo(other.receivedDate);
            if (cmp != 0) return cmp;
        } else if (this.receivedDate == null && other.receivedDate != null) {
            return 1;
        } else if (this.receivedDate != null && other.receivedDate == null) {
            return -1;
        }

        // Tie-breaker: by box ID
        return this.boxId.compareTo(other.boxId);
    }

    /**
     * Returns a readable string with box information.
     *
     * @return formatted string containing box details
     */
    @Override
    public String toString() {
        return String.format("Box{id=%s, sku=%s, qty=%d, expiry=%s, received=%s, aisle=%s, bay=%s}",
                boxId, sku, qtyAvailable,
                expiryDate != null ? expiryDate.toString() : "N/A",
                receivedDate != null ? receivedDate.toString() : "N/A",
                aisle != null ? aisle : "N/A",
                bay != null ? bay : "N/A");
    }
}

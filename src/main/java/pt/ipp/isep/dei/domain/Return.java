package pt.ipp.isep.dei.domain;

import java.time.LocalDateTime;

/**
 * Represents a product return to the warehouse.
 * Tracks returned items for quarantine and inspection processing.
 */
public class Return {
    private final String returnId;
    private final String sku;
    private final int qty;
    private final String reason;
    private final LocalDateTime timestamp;
    private final LocalDateTime expiryDate;

    /**
     * Creates a new product return.
     * @param returnId unique identifier for the return
     * @param sku product stock keeping unit
     * @param qty quantity returned
     * @param reason reason for return (Damaged, Expired, Customer remorse, Cycle count)
     * @param timestamp when the return was received
     * @param expiryDate product expiration date if applicable
     */
    public Return(String returnId, String sku, int qty, String reason, LocalDateTime timestamp, LocalDateTime expiryDate) {
        this.returnId = returnId;
        this.sku = sku;
        this.qty = qty;
        this.reason = reason;
        this.timestamp = timestamp;
        this.expiryDate = expiryDate;
    }

    // Getters
    public String getReturnId() { return returnId; }
    public String getSku() { return sku; }
    public int getQty() { return qty; }
    public String getReason() { return reason; }
    public LocalDateTime getTimestamp() { return timestamp; }
    public LocalDateTime getExpiryDate() { return expiryDate; }

    /**
     * Determines if the item can be restocked into inventory.
     * Items are restockable unless damaged, expired, or past expiry date.
     * @return true if item can be reintroduced to inventory
     */
    public boolean isRestockable() {
        // Never restock damaged or expired items
        if ("Damaged".equalsIgnoreCase(reason) || "Expired".equalsIgnoreCase(reason)) {
            return false;
        }

        // Check if product has passed its expiry date
        if (expiryDate != null && expiryDate.isBefore(LocalDateTime.now())) {
            return false;
        }

        // Customer remorse and cycle count returns can be restocked
        return true;
    }

    /**
     * @return string representation of the return
     */
    @Override
    public String toString() {
        return String.format(
                "Return[ID=%s, SKU=%s, Qty=%d, Reason=%s, Date=%s]",
                returnId, sku, qty, reason, timestamp
        );
    }
}
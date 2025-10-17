package pt.ipp.isep.dei.domain;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class Box implements Comparable<Box> {
    public final String boxId;
    public final String sku;
    public int qtyAvailable;
    public final LocalDate expiryDate;
    public final LocalDateTime receivedDate; // mudar para LocalDateTime
    public final String aisle;
    public final String bay;

    public Box(String boxId, String sku, int qtyAvailable,
               LocalDateTime expiryDate, LocalDateTime receivedDate,
               String aisle, String bay) {
        this.boxId = boxId;
        this.sku = sku;
        this.qtyAvailable = qtyAvailable;
        this.expiryDate = LocalDate.from(expiryDate);
        this.receivedDate = receivedDate;
        this.aisle = aisle;
        this.bay = bay;
    }

    public String getBoxId() { return boxId; }
    public String getSku() { return sku; }
    public int getQtyAvailable() { return qtyAvailable; }
    public String getAisle() { return aisle; }
    public String getBay() { return bay; }
    public LocalDate getExpiryDate() { return expiryDate; }
    public LocalDateTime getReceivedDate() { return receivedDate; } // LocalDateTime

    @Override
    public int compareTo(Box other) {
        LocalDateTime thisExpiry = (this.expiryDate != null) ? this.expiryDate.atStartOfDay() : null;
        LocalDateTime otherExpiry = (other.expiryDate != null) ? other.expiryDate.atStartOfDay() : null;

        if (thisExpiry == null && otherExpiry != null) return 1;
        if (thisExpiry != null && otherExpiry == null) return -1;
        if (thisExpiry != null && otherExpiry != null) {
            int cmp = thisExpiry.compareTo(otherExpiry);
            if (cmp != 0) return cmp;
        }

        LocalDateTime thisReceived = this.receivedDate;
        LocalDateTime otherReceived = other.receivedDate;

        if (thisReceived == null && otherReceived != null) return 1;
        if (thisReceived != null && otherReceived == null) return -1;
        if (thisReceived != null && otherReceived != null) return thisReceived.compareTo(otherReceived);

        return 0;
    }
}

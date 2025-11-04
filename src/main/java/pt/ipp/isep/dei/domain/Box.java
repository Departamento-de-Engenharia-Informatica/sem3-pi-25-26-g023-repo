package pt.ipp.isep.dei.domain;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Objects;

/**
 * Represents a physical box in inventory.
 */
public class Box implements Comparable<Box> {

    private static final String ANSI_RESET = "\u001B[0m";
    private static final String ANSI_YELLOW = "\u001B[33m";
    private static final String ANSI_CYAN = "\u001B[36m";
    private static final String ANSI_RED = "\u001B[31m";
    private static final String ANSI_BOLD = "\u001B[1m";

    public final String boxId;
    public final String sku;
    public int qtyAvailable;
    public final LocalDate expiryDate;
    public final LocalDateTime receivedDate;
    private String aisle;
    private String bay;

    /**
     * Creates a box with an expiration date represented as {@link LocalDate}.
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
    public void setAisle(String aisle) { this.aisle = aisle; }
    public void setBay(String bay) { this.bay = bay; }

    /**
     * Compares boxes using FEFO/FIFO logic.
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
     * Returns a "pretty" string with box information, formatted for tables.
     *
     * @return formatted string containing box details
     */
    @Override
    public String toString() {

        DateTimeFormatter expiryFmt = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        DateTimeFormatter receivedFmt = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

        String expiryStr;
        String expiryColor = "";
        if (expiryDate == null) {
            expiryStr = "N/A";
        } else {
            if (expiryDate.isBefore(LocalDate.now())) {
                expiryColor = ANSI_RED + ANSI_BOLD; // EXPIRADO
            } else if (expiryDate.isBefore(LocalDate.now().plusDays(30))) {
                expiryColor = ANSI_YELLOW; // EXPIRA EM BREVE
            }
            expiryStr = expiryDate.format(expiryFmt);
        }

        String receivedStr = (receivedDate != null) ? receivedDate.format(receivedFmt) : "N/A";
        String location = String.format("A%s-B%s", (aisle != null ? aisle : "?"), (bay != null ? bay : "?"));

        // %-10s -> Alinha à esquerda, 10 caracteres
        // %4d   -> Alinha à direita, 4 caracteres
        return String.format(
                "  %-10s | %-12s | %s%4d%s | %s%-12s%s | %-18s | %s%-10s%s",
                boxId,
                sku,
                ANSI_BOLD, qtyAvailable, ANSI_RESET,
                expiryColor, expiryStr, ANSI_RESET,
                receivedStr,
                ANSI_CYAN, location, ANSI_RESET
        );
    }
}
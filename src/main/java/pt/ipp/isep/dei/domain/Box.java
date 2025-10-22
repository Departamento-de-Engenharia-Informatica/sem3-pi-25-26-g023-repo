package pt.ipp.isep.dei.domain;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Representa uma caixa de produtos no armazém
 */
public class Box implements Comparable<Box> {
    public final String boxId;
    public final String sku;
    public int qtyAvailable;
    public final LocalDate expiryDate;
    public final LocalDateTime receivedDate;
    private String aisle;
    private String bay;

    /**
     * Cria uma caixa com data de expiração como LocalDate
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
     * Cria uma caixa com data de expiração como LocalDateTime
     */
    public Box(String boxId, String sku, int qtyAvailable,
               LocalDateTime expiryDateTime, LocalDateTime receivedDate,
               String aisle, String bay) {
        this(boxId, sku, qtyAvailable,
                (expiryDateTime != null) ? expiryDateTime.toLocalDate() : null,
                receivedDate,
                aisle, bay);
    }

    // --- getters ---
    public String getBoxId() { return boxId; }
    public String getSku() { return sku; }
    public int getQtyAvailable() { return qtyAvailable; }
    public String getAisle() { return aisle; }
    public String getBay() { return bay; }
    public LocalDate getExpiryDate() { return expiryDate; }
    public LocalDateTime getReceivedDate() { return receivedDate; }

    // --- setters para localização ---
    public void setAisle(String aisle) { this.aisle = aisle; }
    public void setBay(String bay) { this.bay = bay; }

    /**
     * Verifica se a caixa contém produtos perecíveis
     */
    public boolean isPerishable() {
        return expiryDate != null;
    }

    /**
     * Ordena caixas por FEFO (produtos perecíveis) e FIFO (não perecíveis)
     */
    @Override
    public int compareTo(Box other) {
        if (other == null) return -1;

        // Perecíveis primeiro
        if (this.expiryDate == null && other.expiryDate != null) return 1;
        if (this.expiryDate != null && other.expiryDate == null) return -1;

        // FEFO para perecíveis
        if (this.expiryDate != null && other.expiryDate != null) {
            int cmp = this.expiryDate.compareTo(other.expiryDate);
            if (cmp != 0) return cmp;
        }

        // FIFO para receção
        if (this.receivedDate != null && other.receivedDate != null) {
            int cmp = this.receivedDate.compareTo(other.receivedDate);
            if (cmp != 0) return cmp;
        } else if (this.receivedDate == null && other.receivedDate != null) {
            return 1;
        } else if (this.receivedDate != null && other.receivedDate == null) {
            return -1;
        }

        // Desempate por ID da caixa
        return this.boxId.compareTo(other.boxId);
    }

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
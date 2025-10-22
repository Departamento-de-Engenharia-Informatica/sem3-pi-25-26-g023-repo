package pt.ipp.isep.dei.domain;

import java.time.LocalDateTime;

/**
 * Representa uma devolução de produto para o armazém
 */
public class Return {
    private final String returnId;
    private final String sku;
    private final int qty;
    private final String reason;
    private final LocalDateTime timestamp;
    private final LocalDateTime expiryDate;

    public Return(String returnId, String sku, int qty, String reason, LocalDateTime timestamp, LocalDateTime expiryDate) {
        this.returnId = returnId;
        this.sku = sku;
        this.qty = qty;
        this.reason = reason;
        this.timestamp = timestamp;
        this.expiryDate = expiryDate;
    }

    public String getReturnId() {
        return returnId;
    }

    public String getSku() {
        return sku;
    }

    public int getQty() {
        return qty;
    }

    public String getReason() {
        return reason;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public LocalDateTime getExpiryDate() {
        return expiryDate;
    }

    /**
     * Determina se o item pode ser restockado
     * @return true se o item pode ser reintroduzido no inventário
     */
    public boolean isRestockable() {
        if ("Damaged".equalsIgnoreCase(reason) || "Expired".equalsIgnoreCase(reason)) {
            return false;
        }

        if (expiryDate != null && expiryDate.isBefore(LocalDateTime.now())) {
            return false;
        }

        return true;
    }

    @Override
    public String toString() {
        return "Return{" +
                "returnId='" + returnId + '\'' +
                ", sku='" + sku + '\'' +
                ", qty=" + qty +
                ", reason='" + reason + '\'' +
                ", timestamp=" + timestamp +
                ", expiryDate=" + expiryDate +
                '}';
    }
}
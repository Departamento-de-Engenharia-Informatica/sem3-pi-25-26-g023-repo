package pt.ipp.isep.dei.domain;

import java.time.LocalDateTime;

/** sku - stock keeping unit - código único do produto devolvido
   qty - quantity - número de unidades devolvidas
   lifo - last in first out - politica de processamento de quarentena (utilizada na classe quatentine)
   timestamp - data / hora de chegada - serve para ordenar devoluções
   expirydate - data de validade - serve para decidir a prioridade de expedição
   retunrid - identificador da devolução - chave única da entidade
   isrestockable() - método de regra de negócio - verifica se o item pode ser reintroduzido no stock
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

    /** Business rule: determine if item can be restocked */
    public boolean isRestockable() {
        if ("Damaged".equalsIgnoreCase(reason) || "Expired".equalsIgnoreCase(reason)) return false;
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
                '}';
    }
}


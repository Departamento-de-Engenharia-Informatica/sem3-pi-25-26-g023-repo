package pt.ipp.isep.dei.domain;

public class Allocation {
    public final String orderId;
    public final int lineNo;
    public final String sku;
    public final int qty;
    public final String boxId;
    public final String aisle;
    public final String bay;
    public final double weight;  // ← NOVO CAMPO

    public Allocation(String orderId, int lineNo, String sku, int qty,
                      String boxId, String aisle, String bay, double weight) {  // ← NOVO PARÂMETRO
        this.orderId = orderId;
        this.lineNo = lineNo;
        this.sku = sku;
        this.qty = qty;
        this.boxId = boxId;
        this.aisle = aisle;
        this.bay = bay;
        this.weight = weight;
    }

    // Método auxiliar para calcular o peso se não tiveres o weight disponível
    public static double calculateWeight(Item item, int quantity) {
        return item.getUnitWeight() * quantity;
    }

    @Override
    public String toString() {
        return String.format("Order %s Line %d SKU %s Qty %d Box %s (%s-%s) Weight %.2fkg",
                orderId, lineNo, sku, qty, boxId, aisle, bay, weight);
    }
}

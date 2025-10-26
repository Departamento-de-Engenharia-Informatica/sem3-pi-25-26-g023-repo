package pt.ipp.isep.dei.domain;

public class Allocation {

    /**
     * Represents an allocation of stock from a box to an order line.
     * Each allocation records how many units of a SKU were taken from a specific box
     * and assigned to an order line.
     */

        public final String orderId;
        public final int lineNo;
        public final String sku;
        public final int qty;
        public final double weight;
        public final String boxId;
        public final String aisle;
        public final String bay;

        /**
         * Creates a new allocation record.
         *
         * @param orderId the order ID
         * @param lineNo  the order line number
         * @param sku     the item SKU
         * @param qty     the quantity allocated
         * @param weight  the total weight of the allocation
         * @param boxId   the box ID
         * @param aisle   the aisle where the box is located
         * @param bay     the bay where the box is located
         */
        public Allocation(String orderId, int lineNo, String sku, int qty,
                          double weight, String boxId, String aisle, String bay) {
            this.orderId = orderId;
            this.lineNo = lineNo;
            this.sku = sku;
            this.qty = qty;
            this.weight = weight;
            this.boxId = boxId;
            this.aisle = aisle;
            this.bay = bay;
        }

        /**
         * @return a short text description of this allocation
         */
        @Override
        public String toString() {
            return String.format("Order %s Line %d SKU %s Qty %d Box %s (%s-%s)",
                    orderId, lineNo, sku, qty, boxId, aisle, bay);
        }
    }


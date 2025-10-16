package pt.ipp.isep.dei.domain;



public class OrderLine {
        public final int lineNo;
        public final String sku;
        public final int requestedQty;

        public OrderLine(int lineNo, String sku, int requestedQty) {
            this.lineNo = lineNo;
            this.sku = sku;
            this.requestedQty = requestedQty;
        }
    }
}

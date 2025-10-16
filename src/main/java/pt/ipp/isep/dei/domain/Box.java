package pt.ipp.isep.dei.domain;

import java.time.LocalDate;

public class Box {
        public final String boxId;
        public final String sku;
        public int qtyAvailable;
        public final LocalDate expiryDate;
        public final LocalDate receivedDate;
        public final String aisle;
        public final String bay;

        public Box(String boxId, String sku, int qtyAvailable,
                   LocalDate expiryDate, LocalDate receivedDate,
                   String aisle, String bay) {
            this.boxId = boxId;
            this.sku = sku;
            this.qtyAvailable = qtyAvailable;
            this.expiryDate = expiryDate;
            this.receivedDate = receivedDate;
            this.aisle = aisle;
            this.bay = bay;
        }

    }

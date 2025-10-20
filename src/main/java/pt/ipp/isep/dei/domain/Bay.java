package pt.ipp.isep.dei.domain;

import java.util.ArrayList;
import java.util.List;

public class Bay {
    private final String warehouseId;
    private final int aisle;
    private final int bay;
    private final int capacityBoxes;
    private final List<Box> boxes = new ArrayList<>();

    public Bay(String warehouseId, int aisle, int bay, int capacityBoxes) {
        this.warehouseId = warehouseId;
        this.aisle = aisle;
        this.bay = bay;
        this.capacityBoxes = capacityBoxes;
    }

    public boolean addBox(Box b) {
        if (boxes.size() < capacityBoxes) {
            boxes.add(b);
            return true;
        }
        return false;
    }
}

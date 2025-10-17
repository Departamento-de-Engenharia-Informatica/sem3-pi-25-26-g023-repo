package pt.ipp.isep.dei.domain;

import java.util.ArrayList;
import java.util.List;

public class Wagon {
    private final String wagonId;
    private final List<Box> boxes = new ArrayList<>();

    public Wagon(String wagonId) {
        this.wagonId = wagonId;
    }

    public void addBox(Box b) {
        boxes.add(b);
    }

    public List<Box> getBoxes() {
        return boxes;
    }

    public String getWagonId() {
        return wagonId;
    }

    /** USEI01 - unload boxes into warehouse inventory using FEFO/FIFO logic */
    public void unloadTo(Inventory inventory) {
        for (Box b : boxes) {
            inventory.insertBoxFEFO(b);
        }
        System.out.printf("Wagon %s unloaded: %d boxes inserted into inventory.%n", wagonId, boxes.size());
    }
}

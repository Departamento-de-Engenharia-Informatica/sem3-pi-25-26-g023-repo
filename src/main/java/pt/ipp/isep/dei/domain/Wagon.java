package pt.ipp.isep.dei.domain;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents a wagon containing boxes to be unloaded into warehouse inventory.
 */
public class Wagon {
    private final String wagonId;
    private final List<Box> boxes = new ArrayList<>();

    /**
     * Creates a new wagon with the specified identifier.
     * @param wagonId unique identifier for the wagon
     */
    public Wagon(String wagonId) {

        this.wagonId = wagonId;
    }

    /**
     * Adds a box to the wagon.
     * @param b the box to add
     */
    public void addBox(Box b) {

        boxes.add(b);
    }

    /**
     * @return all boxes in the wagon
     */
    public List<Box> getBoxes() {

        return boxes;
    }

    /**
     * @return the wagon identifier
     */
    public String getWagonId() {
        return wagonId;
    }
}
package pt.ipp.isep.dei.domain;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Represents a warehouse with multiple storage bays.
 * Manages box storage and organization within the warehouse.
 */
public class Warehouse {
    private final String warehouseId;
    private final List<Bay> bays = new ArrayList<>();

    /**
     * Creates a new warehouse with the specified identifier.
     * @param warehouseId unique identifier for the warehouse
     */
    public Warehouse(String warehouseId) {

        this.warehouseId = warehouseId;
    }

    /**
     * @return the warehouse identifier
     */
    public String getWarehouseId() {

        return warehouseId;
    }

    /**
     * Adds a bay to the warehouse and maintains sorted order by aisle and bay.
     * @param b the bay to add
     */
    public void addBay(Bay b) {
        bays.add(b);
        bays.sort(Comparator.comparingInt(Bay::getAisle).thenComparingInt(Bay::getBay));
    }

    /**
     * Attempts to store a box in the warehouse.
     * Searches for the first available bay with capacity and adds the box.
     * Updates the box's location if successfully stored.
     * @param box the box to store
     * @return true if box was successfully stored, false if warehouse is full
     */
    public boolean storeBox(Box box) {
        for (Bay b : bays) {
            if (b.addBox(box)) {
                // Update box location using setters
                box.setAisle(String.valueOf(b.getAisle()));
                box.setBay(String.valueOf(b.getBay()));
                return true;
            }
        }
        return false; // warehouse full
    }

    /**
     * Checks if all bays in the warehouse are at full capacity.
     * @return true if all bays are full, false otherwise
     */
    public boolean isFull() {

        return bays.stream().allMatch(b -> b.getBoxes().size() >= b.getCapacityBoxes());
    }

    /**
     * @return list of all bays in the warehouse
     */
    public List<Bay> getBays() {

        return bays;
    }
}
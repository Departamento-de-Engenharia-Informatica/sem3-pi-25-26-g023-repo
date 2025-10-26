package pt.ipp.isep.dei.domain;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

/**
 * Manages the collection of boxes in the inventory.
 * <p>
 * This class handles operations such as restocking returned items,
 * dispatching stock according to FEFO/FIFO rules,
 * and relocating boxes within warehouses.
 * </p>
 */
public class Inventory {

    /** List of boxes currently stored in the inventory. */
    private final List<Box> boxes = new ArrayList<>();

    /**
     * Creates a new {@link Box} from a product return.
     *
     * @param r the {@link Return} to convert into a box
     * @return the created box representing the returned items
     */
    public Box createBoxFromReturn(Return r) {
        Objects.requireNonNull(r);

        LocalDate expiry = (r.getExpiryDate() != null) ? r.getExpiryDate().toLocalDate() : null;
        LocalDateTime received = LocalDateTime.now();
        String boxId = "RET-" + r.getReturnId();
        return new Box(boxId, r.getSku(), r.getQty(), expiry, received, null, null);
    }

    /**
     * Inserts a box into the inventory while keeping FEFO/FIFO order.
     *
     * @param b the box to insert
     */
    public void insertBoxFEFO(Box b) {
        Objects.requireNonNull(b);
        boxes.add(b);
        Collections.sort(boxes);
    }

    /**
     * Performs a restock operation by adding a returned item back into the inventory.
     *
     * @param r           the {@link Return} to process
     * @param warehouses  the list of available warehouses
     * @return {@code true} if the restock was successful, {@code false} otherwise
     */
    public boolean restock(Return r, List<Warehouse> warehouses) {
        Box newBox = createBoxFromReturn(r);
        boolean storedPhysically = false;
        String warehouseIdWhereStored = null;

        for (Warehouse wh : warehouses) {
            if (wh.storeBox(newBox)) {
                storedPhysically = true;
                warehouseIdWhereStored = wh.getWarehouseId();
                break;
            }
        }

        if (storedPhysically) {
            insertBoxFEFO(newBox);
            System.out.printf("  ✅ Restock: Item %s (Box %s) stored in Warehouse %s, Aisle %s, Bay %s.%n",
                    newBox.getSku(), newBox.getBoxId(), warehouseIdWhereStored,
                    newBox.getAisle(), newBox.getBay());
            return true;
        } else {
            System.out.printf("  ⚠️ Restock failed: No space found for Box %s (SKU %s) in warehouses.%n",
                    newBox.getBoxId(), newBox.getSku());
            return false;
        }
    }

    /**
     * Dispatches a quantity of a given SKU following FEFO/FIFO order.
     *
     * @param sku the SKU to dispatch
     * @param qty the quantity to dispatch
     * @return the actual quantity dispatched
     */
    public int dispatch(String sku, int qty) {
        if (sku == null || qty <= 0) return 0;

        int remaining = qty;
        int dispatched = 0;

        Iterator<Box> it = boxes.iterator();
        while (it.hasNext() && remaining > 0) {
            Box b = it.next();
            if (!sku.equals(b.getSku()) || b.getAisle() == null || b.getBay() == null) {
                continue;
            }

            int take = Math.min(b.getQtyAvailable(), remaining);
            if (take > 0) {
                b.qtyAvailable -= take;
                remaining -= take;
                dispatched += take;

                if (b.getQtyAvailable() <= 0) {
                    it.remove();
                }
            }
        }
        return dispatched;
    }

    /**
     * Relocates a box to a new aisle and bay.
     *
     * @param boxId   the ID of the box to move
     * @param newAisle the new aisle
     * @param newBay   the new bay
     * @return {@code true} if relocation succeeded, {@code false} otherwise
     */
    public boolean relocate(String boxId, String newAisle, String newBay) {
        if (boxId == null || newAisle == null || newBay == null) return false;

        Box boxToRelocate = null;
        int index = -1;

        for (int i = 0; i < boxes.size(); i++) {
            if (boxId.equals(boxes.get(i).getBoxId())) {
                boxToRelocate = boxes.get(i);
                index = i;
                break;
            }
        }

        if (boxToRelocate != null) {
            boxes.remove(index);
            boxToRelocate.setAisle(newAisle);
            boxToRelocate.setBay(newBay);
            insertBoxFEFO(boxToRelocate);

            System.out.printf("  Relocate: Box %s moved to Aisle %s, Bay %s.%n", boxId, newAisle, newBay);
            return true;
        } else {
            System.out.printf("  Relocate failed: Box %s not found in inventory.%n", boxId);
            return false;
        }
    }

    /**
     * Returns an unmodifiable view of all boxes in the inventory.
     *
     * @return a read-only list of boxes
     */
    public List<Box> getBoxes() {
        return Collections.unmodifiableList(new ArrayList<>(boxes));
    }
}

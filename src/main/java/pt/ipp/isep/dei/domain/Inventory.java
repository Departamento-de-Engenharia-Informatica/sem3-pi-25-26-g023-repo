package pt.ipp.isep.dei.domain;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Inventory {
    private final List<Box> boxes = new ArrayList<>();

    /** Create a new box from a returned item */
    public Box createBoxFromReturn(Return r) {
        String boxId = "RET-" + r.getReturnId();
        return new Box(boxId, r.getSku(), r.getQty(), r.getExpiryDate(), LocalDateTime.now(), null, null);
    }

    /** Insert new box into inventory keeping FEFO/FIFO order */
    public void insertBoxFEFO(Box b) {
        boxes.add(b);
        Collections.sort(boxes);
    }

    /** Restock operation */
    public void restock(Return r) {
        Box b = createBoxFromReturn(r);
        insertBoxFEFO(b);
    }

    public List<Box> getBoxes() {
        return Collections.unmodifiableList(boxes);
    }
}

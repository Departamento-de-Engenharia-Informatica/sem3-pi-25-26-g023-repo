package pt.ipp.isep.dei.domain;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;

public class Inventory {
    private final List<Box> boxes = new ArrayList<>();

    /** Create a new box from a returned item (Return.expiryDate pode ser LocalDateTime) */
    public Box createBoxFromReturn(Return r) {
        Objects.requireNonNull(r);

        LocalDate expiry = (r.getExpiryDate() != null) ? r.getExpiryDate().toLocalDate() : null;
        LocalDateTime received = LocalDateTime.now();
        String boxId = "RET-" + r.getReturnId();
        return new Box(boxId, r.getSku(), r.getQty(), expiry, received, null, null);
    }

    /** Insert new box into inventory keeping FEFO/FIFO order */
    public void insertBoxFEFO(Box b) {
        Objects.requireNonNull(b);
        boxes.add(b);
        Collections.sort(boxes);
    }

    /** Restock operation (USEI05) */
    public void restock(Return r) {
        Box b = createBoxFromReturn(r);
        insertBoxFEFO(b);
    }

    /** Unload list of boxes (e.g., from a wagon) into inventory respecting FEFO/FIFO */
    public void unloadBoxes(List<Box> newBoxes) {
        if (newBoxes == null || newBoxes.isEmpty()) return;
        for (Box b : newBoxes) {
            insertBoxFEFO(b);
        }
    }

    /**
     * Dispatch operation - retira qty unidades do sku seguindo FEFO/FIFO.
     * Percorre as boxes por ordem FEFO (a lista está ordenada) e consome até satisfacer ou acabar.
     * Remove boxes que ficam com qtyAvailable == 0.
     *
     * @param sku sku pedido
     * @param qty quantidade a despachar
     * @return quantidade efectivamente despachada
     */
    public int dispatch(String sku, int qty) {
        if (sku == null || qty <= 0) return 0;

        int remaining = qty;

        Iterator<Box> it = boxes.iterator();
        while (it.hasNext() && remaining > 0) {
            Box b = it.next();
            if (!sku.equals(b.getSku())) continue;

            int take = Math.min(b.getQtyAvailable(), remaining);
            b.qtyAvailable -= take;
            remaining -= take;

            if (b.getQtyAvailable() <= 0) {
                it.remove();
            }
        }
        return qty - remaining;
    }

    /**
     * Relocate: atualiza aisle/bay de uma box (procura por boxId).
     * Reinsere a box com nova localização mantendo ordenação FEFO/FIFO do inventário.
     */
    public boolean relocate(String boxId, String newAisle, String newBay) {
        if (boxId == null) return false;
        for (int i = 0; i < boxes.size(); i++) {
            Box b = boxes.get(i);
            if (boxId.equals(b.getBoxId())) {

                Box relocated = new Box(b.getBoxId(), b.getSku(), b.getQtyAvailable(),
                        b.getExpiryDate(), b.getReceivedDate(), newAisle, newBay);
                boxes.remove(i);
                insertBoxFEFO(relocated);
                return true;
            }
        }
        return false;
    }

    public List<Box> getBoxes() {
        return Collections.unmodifiableList(boxes);
    }

}

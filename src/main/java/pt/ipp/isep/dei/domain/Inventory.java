package pt.ipp.isep.dei.domain;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

/**
 * Gerencia o inventário de caixas com operações FEFO/FIFO
 */
public class Inventory {
    private final List<Box> boxes = new ArrayList<>();

    /**
     * Cria uma nova caixa a partir de uma devolução
     */
    public Box createBoxFromReturn(Return r) {
        Objects.requireNonNull(r);

        LocalDate expiry = (r.getExpiryDate() != null) ? r.getExpiryDate().toLocalDate() : null;
        LocalDateTime received = LocalDateTime.now();
        String boxId = "RET-" + r.getReturnId();
        return new Box(boxId, r.getSku(), r.getQty(), expiry, received, null, null);
    }

    /**
     * Insere caixa no inventário mantendo ordem FEFO/FIFO
     */
    public void insertBoxFEFO(Box b) {
        Objects.requireNonNull(b);
        boxes.add(b);
        Collections.sort(boxes);
    }

    /**
     * Operação de restock - tenta colocar item devolvido no inventário
     * @param r Devolução a processar
     * @param warehouses Lista de armazéns disponíveis
     * @return true se o restock foi bem sucedido
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
            System.out.printf("  ✅ Restock: Item %s (Box %s) colocado em Warehouse %s, Aisle %s, Bay %s.%n",
                    newBox.getSku(), newBox.getBoxId(), warehouseIdWhereStored,
                    newBox.getAisle(), newBox.getBay());
            return true;
        } else {
            System.out.printf("  ⚠️ Restock Falhou: Não foi encontrado espaço para Box %s (SKU %s) nos armazéns.%n",
                    newBox.getBoxId(), newBox.getSku());
            return false;
        }
    }

    /**
     * Despacha quantidade de um SKU seguindo FEFO/FIFO
     * @param sku SKU a despachar
     * @param qty Quantidade a despachar
     * @return Quantidade efetivamente despachada
     */
    public int dispatch(String sku, int qty) {
        if (sku == null || qty <= 0) return 0;

        int remaining = qty;
        int dispatched = 0;

        Iterator<Box> it = boxes.iterator();
        while (it.hasNext() && remaining > 0) {
            Box b = it.next();
            if (!sku.equals(b.getSku()) || b.getAisle() == null || b.getBay() == null ) {
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
     * Relocaliza uma caixa para nova posição
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

            System.out.printf("  Relocate: Box %s movida para Aisle %s, Bay %s.%n", boxId, newAisle, newBay);
            return true;
        } else {
            System.out.printf("  Relocate Falhou: Box %s não encontrada no inventário.%n", boxId);
            return false;
        }
    }

    public List<Box> getBoxes() {
        return Collections.unmodifiableList(new ArrayList<>(boxes));
    }
}
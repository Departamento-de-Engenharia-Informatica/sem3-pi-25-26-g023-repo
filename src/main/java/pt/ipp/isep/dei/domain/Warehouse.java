package pt.ipp.isep.dei.domain;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class Warehouse {
    private final String warehouseId;
    private final List<Bay> bays = new ArrayList<>();

    public Warehouse(String warehouseId) {
        this.warehouseId = warehouseId;
    }

    public String getWarehouseId() {
        return warehouseId;
    }

    public void addBay(Bay b) {
        bays.add(b);
        bays.sort(Comparator.comparingInt(Bay::getAisle).thenComparingInt(Bay::getBay));
    }

    /**
     * Tenta adicionar uma box neste warehouse.
     * Percorre as bays por ordem e adiciona na primeira com espaço disponível.
     * Retorna true se adicionada com sucesso.
     */
    public boolean storeBox(Box box) {
        for (Bay b : bays) {
            if (b.addBox(box)) {
                // Atualiza a localização da Box através dos setters
                box.setAisle(String.valueOf(b.getAisle()));
                box.setBay(String.valueOf(b.getBay()));
                return true;
            }
        }
        return false; // warehouse cheio
    }

    public boolean isFull() {
        return bays.stream().allMatch(b -> b.getBoxes().size() >= b.getCapacityBoxes());
    }

    public List<Bay> getBays() {
        return bays;
    }
}

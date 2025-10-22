package pt.ipp.isep.dei.domain;

import java.util.ArrayList;
import java.util.List;

/**
 * Representa um vagão com caixas para descarregar no armazém
 */
public class Wagon {
    private final String wagonId;
    private final List<Box> boxes = new ArrayList<>();

    public Wagon(String wagonId) {
        this.wagonId = wagonId;
    }

    /**
     * Adiciona uma caixa ao vagão
     */
    public void addBox(Box b) {
        boxes.add(b);
    }

    /**
     * Retorna todas as caixas do vagão
     */
    public List<Box> getBoxes() {
        return boxes;
    }

    public String getWagonId() {
        return wagonId;
    }

    /**
     * Descarrega todas as caixas do vagão para o inventário usando FEFO/FIFO
     */
    public void unloadTo(Inventory inventory) {
        for (Box b : boxes) {
            inventory.insertBoxFEFO(b);
        }
        System.out.printf("Wagon %s unloaded: %d boxes inserted into inventory.%n", wagonId, boxes.size());
    }
}
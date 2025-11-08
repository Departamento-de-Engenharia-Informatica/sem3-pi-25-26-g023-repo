package pt.ipp.isep.dei.UI.gui.views;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import pt.ipp.isep.dei.domain.InventoryManager;
import pt.ipp.isep.dei.domain.WMS;

public class DashboardController {

    @FXML
    private Label statsLabel; // <-- Voltámos ao Label único

    private WMS wms;
    private InventoryManager manager;

    /**
     * Recebe os serviços do MainController.
     * Esta assinatura está correta para o teu MainController atual.
     */
    public void setServices(WMS wms, InventoryManager manager) {
        this.wms = wms;
        this.manager = manager;

        // Atualiza as estatísticas assim que os serviços são recebidos
        updateStats();
    }

    /**
     * Atualiza o Label único com todos os dados formatados.
     */
    private void updateStats() {
        if (wms != null && manager != null) {
            try {
                // 1. Obter todos os dados
                int inventoryCount = wms.getInventory().getBoxes().size();
                int quarantineCount = wms.getQuarantine().size();
                int stationCount = manager.getValidStationCount();
                int warehouseCount = manager.getWarehouses().size(); // <-- Dado novo
                int skuCount = manager.getItemsCount();             // <-- Dado novo

                // 2. Criar a String formatada com quebras de linha (%n)
                String statsText = String.format(
                        "Inventory Stock: \t%d boxes%n" +
                                "Quarantine (Returns): \t%d returns%n" +
                                "Registered Warehouses: \t%d%n" +
                                "Unique Items (SKUs): \t%d%n" +
                                "Registered Stations: \t%d",

                        inventoryCount,
                        quarantineCount,
                        warehouseCount,
                        skuCount,
                        stationCount
                );

                // 3. Definir o texto
                statsLabel.setText(statsText);

            } catch (Exception e) {
                statsLabel.setText("Error loading stats. Services might be null.");
                e.printStackTrace();
            }
        } else {
            statsLabel.setText("Could not load backend services.");
        }
    }
}

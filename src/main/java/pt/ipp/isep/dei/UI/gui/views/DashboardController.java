package pt.ipp.isep.dei.UI.gui.views;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import pt.ipp.isep.dei.domain.InventoryManager;
import pt.ipp.isep.dei.domain.WMS;

public class DashboardController {

    @FXML
    private Label statsLabel;

    private WMS wms;
    private InventoryManager manager;

    /**
     * Receives services from the MainController.
     */
    public void setServices(WMS wms, InventoryManager manager) {
        this.wms = wms;
        this.manager = manager;

        // Update stats as soon as services are received
        updateStats();
    }

    private void updateStats() {
        if (wms != null && manager != null) {
            statsLabel.setText(String.format(
                    "Inventory: %d boxes%nQuarantine: %d returns%nValid Stations: %d",
                    wms.getInventory().getBoxes().size(),
                    wms.getQuarantine().size(),
                    manager.getValidStationCount()
            ));
        } else {
            statsLabel.setText("Could not load backend services.");
        }
    }
}
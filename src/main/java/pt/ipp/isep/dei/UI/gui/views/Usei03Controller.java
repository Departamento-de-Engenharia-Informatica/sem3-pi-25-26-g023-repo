package pt.ipp.isep.dei.UI.gui.views;

import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import pt.ipp.isep.dei.UI.gui.MainController;
import pt.ipp.isep.dei.domain.*;

public class Usei03Controller {

    @FXML
    private TextField txtCapacity;
    @FXML
    private ComboBox<HeuristicType> comboHeuristic;
    @FXML
    private Button btnPack;
    @FXML
    private Label lblStatus;
    @FXML
    private TextArea txtResult;

    private MainController mainController;
    private InventoryManager manager;

    public void setServices(MainController mainController, InventoryManager manager) {
        this.mainController = mainController;
        this.manager = manager;
    }

    @FXML
    public void initialize() {
        comboHeuristic.setItems(FXCollections.observableArrayList(HeuristicType.values()));
        comboHeuristic.getSelectionModel().select(HeuristicType.FIRST_FIT);
    }

    @FXML
    void handlePackTrolleys(ActionEvent event) {
        lblStatus.setText("");
        txtResult.clear();

        AllocationResult allocResult = mainController.getLastAllocationResult();

        if (allocResult == null || allocResult.allocations.isEmpty()) {
            String errorMsg = "Error: You must run [USEI02 - Allocate Orders] first.\nNo allocations are available to pack.";
            lblStatus.setText(errorMsg);
            txtResult.setText(errorMsg);
            mainController.updateStatusPicking(false);
            mainController.showNotification(errorMsg, "error"); // ✅ Notificação Pop-up
            return;
        }

        double capacity;
        try {
            capacity = Double.parseDouble(txtCapacity.getText());
            if (capacity <= 0) throw new NumberFormatException();
        } catch (NumberFormatException e) {
            String errorMsg = "Error: Trolley Capacity must be a positive number.";
            lblStatus.setText(errorMsg);
            mainController.showNotification(errorMsg, "error"); // ✅ Notificação Pop-up
            return;
        }

        HeuristicType heuristic = comboHeuristic.getSelectionModel().getSelectedItem();
        if (heuristic == null) {
            String errorMsg = "Error: You must select a heuristic.";
            lblStatus.setText(errorMsg);
            mainController.showNotification(errorMsg, "error"); // ✅ Notificação Pop-up
            return;
        }

        String infoMsg = String.format("ℹ️  Ready to pack %d allocations (Capacity: %.1fkg, Heuristic: %s)...",
                allocResult.allocations.size(), capacity, heuristic);
        lblStatus.setText(infoMsg); // Info local
        txtResult.setText(infoMsg + "\n⚙️  Executing USEI03...\n\n");

        PickingService service = new PickingService();
        service.setItemsMap(manager.getItemsMap());

        PickingPlan pickingPlan = service.generatePickingPlan(
                allocResult.allocations,
                capacity,
                heuristic
        );

        mainController.setLastPickingPlan(pickingPlan);
        mainController.updateStatusPicking(pickingPlan != null && pickingPlan.getTotalTrolleys() > 0);

        txtResult.appendText("=" .repeat(60) + "\n");
        txtResult.appendText("           📊 RESULTS USEI03 - Picking Plan\n");
        txtResult.appendText("=" .repeat(60) + "\n");
        txtResult.appendText(pickingPlan.getSummary());

        String successMsg = String.format("Picking Plan generated! %d trolleys created.", pickingPlan.getTotalTrolleys());
        lblStatus.setText("✅ " + successMsg); // Status local
        mainController.showNotification(successMsg, "success"); // ✅ Notificação Pop-up
    }
}
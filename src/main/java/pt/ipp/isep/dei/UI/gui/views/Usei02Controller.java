package pt.ipp.isep.dei.UI.gui.views;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import pt.ipp.isep.dei.UI.gui.MainController;
import pt.ipp.isep.dei.domain.*;

import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class Usei02Controller {

    @FXML
    private RadioButton radioStrict;
    @FXML
    private RadioButton radioPartial;
    @FXML
    private ToggleGroup modeToggleGroup;
    @FXML
    private Button btnAllocate;
    @FXML
    private Label lblStatus;
    @FXML
    private TextArea txtResult;

    private final String ORDERS_FILE = Paths.get("src", "main", "java", "pt", "ipp", "isep", "dei", "FicheirosCSV", "orders.csv").toString();
    private final String ORDER_LINES_FILE = Paths.get("src", "main", "java", "pt", "ipp", "isep", "dei", "FicheirosCSV", "order_lines.csv").toString();

    private MainController mainController;
    private InventoryManager manager;
    private AllocationResult lastAllocationResult;

    public void setServices(MainController mainController, InventoryManager manager) {
        this.mainController = mainController;
        this.manager = manager;
    }

    @FXML
    void handleAllocateOrders(ActionEvent event) {
        lblStatus.setText("");
        txtResult.clear();

        mainController.setLastPickingPlan(null);
        mainController.updateStatusPicking(false);

        if (this.manager == null || this.mainController == null) {
            String errorMsg = "Error: Services (Managers) were not injected correctly.";
            lblStatus.setText(errorMsg); // Opcional: manter no label local
            mainController.showNotification(errorMsg, "error"); // ✅ Notificação Pop-up
            return;
        }

        List<Order> orders;
        try {
            orders = manager.loadOrders(ORDERS_FILE, ORDER_LINES_FILE);
        } catch (Exception e) {
            String errorMsg = "Error loading order files: " + e.getMessage();
            lblStatus.setText(errorMsg);
            mainController.showNotification(errorMsg, "error"); // ✅ Notificação Pop-up
            e.printStackTrace();
            return;
        }

        List<Box> currentInventoryState = new ArrayList<>(manager.getInventory().getBoxes());

        if (orders.isEmpty()) {
            String errorMsg = "No valid orders found to process.";
            lblStatus.setText(errorMsg);
            mainController.showNotification(errorMsg, "error"); // ✅ Notificação Pop-up
            return;
        }
        if (currentInventoryState.isEmpty()) {
            String errorMsg = "Inventory is empty. Cannot allocate orders.";
            lblStatus.setText(errorMsg);
            mainController.showNotification(errorMsg, "error"); // ✅ Notificação Pop-up
            return;
        }

        String infoMsg = String.format("ℹ️  Data loaded: %d orders, %d boxes in inventory",
                orders.size(), currentInventoryState.size());
        lblStatus.setText(infoMsg); // Status local (info) é bom
        txtResult.setText(infoMsg + "\n");

        OrderAllocator.Mode mode = radioStrict.isSelected() ?
                OrderAllocator.Mode.STRICT :
                OrderAllocator.Mode.PARTIAL;

        try {
            OrderAllocator allocator = new OrderAllocator();
            allocator.setItems(manager.getItemsMap());

            txtResult.appendText(String.format("📦 Processing %d orders with %d boxes (Mode: %s)...\n\n",
                    orders.size(), currentInventoryState.size(), mode.toString()));

            this.lastAllocationResult = allocator.allocateOrders(orders, currentInventoryState, mode);

            mainController.setLastAllocationResult(this.lastAllocationResult);
            boolean allocsGenerated = (this.lastAllocationResult != null && !this.lastAllocationResult.allocations.isEmpty());
            mainController.updateStatusAllocations(allocsGenerated);

            String resultSummary = String.format(
                    "✅ SUCCESS: USEI02 executed successfully!\n" +
                            "📊 Results: %d allocations generated, %d lines processed",
                    lastAllocationResult.allocations.size(),
                    lastAllocationResult.eligibilityList.size()
            );

            txtResult.appendText(resultSummary);

            // ✅ Notificação Pop-up
            String successMsg = String.format("Allocation complete! %d allocations generated.", lastAllocationResult.allocations.size());
            mainController.showNotification(successMsg, "success");
            lblStatus.setText("✅ Allocation complete!"); // Manter status local

        } catch (Exception e) {
            String errorMsg = "Error executing allocation: " + e.getMessage();
            lblStatus.setText(errorMsg);
            mainController.showNotification(errorMsg, "error"); // ✅ Notificação Pop-up
            e.printStackTrace();
        }
    }
}
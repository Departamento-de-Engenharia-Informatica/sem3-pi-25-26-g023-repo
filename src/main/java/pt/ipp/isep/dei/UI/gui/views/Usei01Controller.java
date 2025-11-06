package pt.ipp.isep.dei.UI.gui.views;

import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TextArea;
import javafx.scene.layout.VBox;
import pt.ipp.isep.dei.UI.gui.GuiUtils;
import pt.ipp.isep.dei.UI.gui.MainController;
import pt.ipp.isep.dei.domain.InventoryManager;
import pt.ipp.isep.dei.domain.WMS;
import pt.ipp.isep.dei.domain.Wagon;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;

public class Usei01Controller {

    // FXML elements
    @FXML
    private TextArea resultTextArea;
    @FXML
    private VBox optionsContainer;
    @FXML
    private VBox selectionContainer;
    @FXML
    private ListView<Wagon> wagonListView;

    // We still accept the MainController, in case we add other statuses later
    private MainController mainController;

    // Backend services
    private WMS wms;
    private InventoryManager manager;

    // State management
    private boolean hasBeenRun = false;
    private List<Wagon> loadedWagonsCache;

    /**
     * Receives services AND the MainController instance.
     */
    public void setServices(MainController mainController, WMS wms, InventoryManager manager) {
        this.mainController = mainController;
        this.wms = wms;
        this.manager = manager;

        wagonListView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);

        wagonListView.setCellFactory(lv -> new ListCell<Wagon>() {
            @Override
            protected void updateItem(Wagon wagon, boolean empty) {
                super.updateItem(wagon, empty);
                if (empty || wagon == null) {
                    setText(null);
                } else {
                    setText(String.format("ID: %s (Box Count: %d)",
                            wagon.getWagonId(), wagon.getBoxes().size()));
                }
            }
        });

        printToConsole("ℹ️  Operation Status: NOT-RUN");
        printToConsole("Please select an execution option above.");
    }

    /**
     * Called by the "1. Unload ALL Wagons" button.
     */
    @FXML
    void handleUnloadAll(ActionEvent event) {
        if (checkIfAlreadyRun()) return;

        try {
            clearConsole();
            printToConsole("Starting 'Unload ALL Wagons' operation...");

            printToConsole("Loading wagon data from '.../wagons.csv'...");
            List<Wagon> wagons = manager.loadWagons("src/main/java/pt/ipp/isep/dei/FicheirosCSV/wagons.csv");
            printToConsole(String.format("Found %d wagons.", wagons.size()));

            printToConsole("Processing all wagons...");
            wms.unloadWagons(wagons);

            printToConsole("\n✅ Operation Successful: All wagons have been processed.");
            printToConsole("The 'audit.log' file has been updated.");

            setHasBeenRun(true);

        } catch (FileNotFoundException e) {
            handleError("FATAL ERROR: Could not find 'wagons.csv' file.", e);
        } catch (Exception e) {
            handleError("UNEXPECTED ERROR: " + e.getMessage(), e);
        }
    }

    /**
     * Called by the "2. Select Wagons Manually" button.
     */
    @FXML
    void handleShowManualSelection(ActionEvent event) {
        if (checkIfAlreadyRun()) return;

        try {
            clearConsole();
            printToConsole("Loading wagons for manual selection...");

            if (this.loadedWagonsCache == null) {
                this.loadedWagonsCache = manager.loadWagons("src/main/java/pt/ipp/isep/dei/FicheirosCSV/wagons.csv");
            }

            wagonListView.getItems().setAll(this.loadedWagonsCache);

            printToConsole(String.format("Found %d wagons.", this.loadedWagonsCache.size()));
            printToConsole("Please select one or more wagons from the list above.");

            showPanel(selectionContainer);
            hidePanel(optionsContainer);

        } catch (FileNotFoundException e) {
            handleError("FATAL ERROR: Could not find 'wagons.csv' file.", e);
        } catch (Exception e) {
            handleError("UNEXPECTED ERROR: " + e.getMessage(), e);
        }
    }

    /**
     * Called by the "Process Selected Wagons" button in the "Selection State".
     */
    @FXML
    void handleProcessSelected(ActionEvent event) {
        if (checkIfAlreadyRun()) return;

        ObservableList<Wagon> selectedItems = wagonListView.getSelectionModel().getSelectedItems();
        List<Wagon> selectedWagons = new ArrayList<>(selectedItems);

        if (selectedWagons.isEmpty()) {
            printToConsole("\n❌ ERROR: No wagons selected. Please select at least one wagon.");
            GuiUtils.showErrorAlert("Selection Error", "No Wagons Selected", "You must select at least one wagon from the list to process.");
            return;
        }

        clearConsole();
        printToConsole(String.format("Starting processing for %d selected wagon(s)...", selectedWagons.size()));

        try {
            wms.unloadWagons(selectedWagons);

            printToConsole(String.format("\n✅ Operation Successful: %d selected wagon(s) have been processed.", selectedWagons.size()));
            printToConsole("The 'audit.log' file has been updated.");

            setHasBeenRun(true);

        } catch (Exception e) {
            handleError("UNEXPECTED ERROR: " + e.getMessage(), e);
        }
    }

    /**
     * Called by the "Cancel" button in the "Selection State".
     */
    @FXML
    void handleCancelSelection(ActionEvent event) {
        clearConsole();
        printToConsole("Manual selection cancelled.");

        wagonListView.getItems().clear();
        this.loadedWagonsCache = null;

        showPanel(optionsContainer);
        hidePanel(selectionContainer);
    }

    // --- STATE MANAGEMENT & HELPER FUNCTIONS ---

    private boolean checkIfAlreadyRun() {
        if (hasBeenRun) {
            clearConsole();
            printToConsole("❌ ERROR: The [USEI01] Unload Wagons operation has already been run for this session.");
            printToConsole("To run again, please restart the application.");
            return true;
        }
        return false;
    }

    /**
     * Marks the operation as complete and locks the UI.
     */
    private void setHasBeenRun(boolean status) {
        this.hasBeenRun = status;

        // --- MODIFICATION: Removed the call to mainController.updateStatusUnload ---

        hidePanel(optionsContainer);
        hidePanel(selectionContainer);
        printToConsole("\nOperation complete.");
        printToConsole("To run again, please restart the application.");
    }

    private void handleError(String message, Exception e) {
        printToConsole("\n" + message);
        GuiUtils.showErrorAlert("Execution Error", message, e.getMessage());
        e.printStackTrace();
    }

    private void showPanel(VBox panel) {
        panel.setVisible(true);
        panel.setManaged(true);
    }

    private void hidePanel(VBox panel) {
        panel.setVisible(false);
        panel.setManaged(false);
    }

    private void printToConsole(String text) {
        resultTextArea.appendText(text + "\n");
    }

    private void clearConsole() {
        resultTextArea.clear();
    }
}
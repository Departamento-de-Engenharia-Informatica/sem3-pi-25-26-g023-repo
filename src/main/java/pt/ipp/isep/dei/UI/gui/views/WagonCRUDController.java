package pt.ipp.isep.dei.UI.gui.views;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.TextFieldTableCell;
import pt.ipp.isep.dei.UI.gui.MainController;
import pt.ipp.isep.dei.domain.Wagon;
import pt.ipp.isep.dei.repository.DatabaseRepository;

import java.sql.SQLException;
import java.util.List;

public class WagonCRUDController {

    @FXML private TableView<Wagon> wagonTable;
    @FXML private TableColumn<Wagon, String> idColumn;
    @FXML private TableColumn<Wagon, String> modelIdColumn;
    @FXML private TableColumn<Wagon, Integer> serviceYearColumn;

    @FXML private TextField txtId;
    @FXML private TextField txtModelId;
    @FXML private TextField txtServiceYear;

    @FXML private Button btnSave;
    @FXML private Button btnDelete;
    @FXML private Label lblStatus;

    private MainController mainController;
    private DatabaseRepository dbRepo;
    private Wagon selectedWagon;

    public void setServices(MainController mainController) {
        this.mainController = mainController;
        this.dbRepo = new DatabaseRepository();
        loadData();
    }

    @FXML
    public void initialize() {
        // ID (String)
        idColumn.setCellValueFactory(cellData -> cellData.getValue().wagonIdProperty());

        // Model ID (String)
        modelIdColumn.setCellValueFactory(cellData -> cellData.getValue().modelIdProperty());
        modelIdColumn.setCellFactory(TextFieldTableCell.forTableColumn());
        modelIdColumn.setOnEditCommit(event -> {
            Wagon wagon = event.getRowValue();
            handleUpdateInline(wagon, event.getNewValue(), String.valueOf(wagon.getServiceYear()));
        });

        // Service Year (Integer - convertido para Object)
        serviceYearColumn.setCellValueFactory(cellData -> cellData.getValue().serviceYearProperty().asObject());

        // Listener para seleção de linha
        wagonTable.getSelectionModel().selectedItemProperty().addListener(
                (observable, oldValue, newValue) -> showDetails(newValue));

        wagonTable.setEditable(true);
        btnDelete.setDisable(true);
        btnSave.setText("Create");
    }

    private void showDetails(Wagon wagon) {
        this.selectedWagon = wagon;
        if (wagon != null) {
            txtId.setText(wagon.getWagonId());
            txtModelId.setText(String.valueOf(wagon.getModelId()));
            txtServiceYear.setText(String.valueOf(wagon.getServiceYear()));

            txtId.setDisable(true);
            btnDelete.setDisable(false);
            btnSave.setText("Update");
        } else {
            clearFields();
            txtId.setDisable(false);
            btnDelete.setDisable(true);
            btnSave.setText("Create");
        }
    }

    @FXML
    void handleClearSelection(ActionEvent event) {
        wagonTable.getSelectionModel().clearSelection();
        showDetails(null);
    }

    @FXML
    void handleSave(ActionEvent event) {
        String idStr = txtId.getText().trim();
        String modelIdStr = txtModelId.getText().trim();
        String serviceYearStr = txtServiceYear.getText().trim();

        if (idStr.isEmpty() || modelIdStr.isEmpty() || serviceYearStr.isEmpty()) {
            setErrorStatus("All fields (ID, Model ID, Service Year) must be filled.");
            return;
        }

        try {
            // ID é tratado como String, mas validamos se os outros são inteiros
            int modelId = Integer.parseInt(modelIdStr);
            int serviceYear = Integer.parseInt(serviceYearStr);

            if (selectedWagon == null) {
                // CREATE - CRÍTICO: Passar idStr (String)
                dbRepo.addWagon(idStr, modelId, serviceYear);
                setSuccessStatus("Wagon created successfully!");
            } else {
                // UPDATE - CRÍTICO: Passar idStr (String)
                dbRepo.updateWagon(idStr, modelId, serviceYear);
                setSuccessStatus("Wagon updated successfully!");
            }
            loadData();
            clearFields();
        } catch (NumberFormatException e) {
            setErrorStatus("Model ID and Service Year must be valid integers.");
        } catch (SQLException e) {
            handleSqlError(e, "Error saving wagon. Check if ID already exists or Model ID/Operator ID is invalid.");
        }
    }

    @FXML
    void handleDelete(ActionEvent event) {
        if (selectedWagon == null) return;
        try {
            // FIX: Passar a String ID diretamente para o repositório
            dbRepo.deleteWagon(selectedWagon.getWagonId());
            setSuccessStatus("Wagon deleted successfully!");
            loadData();
            clearFields();
        } catch (SQLException e) {
            handleSqlError(e, "Error deleting wagon. Check foreign key constraints! (e.g., in TRAIN_WAGON_USAGE).");
        }
    }

    // Método para updates inline na tabela
    private void handleUpdateInline(Wagon wagon, String newModelIdStr, String newServiceYearStr) {
        try {
            int newModelId = Integer.parseInt(newModelIdStr);
            int newServiceYear = Integer.parseInt(newServiceYearStr);

            // FIX: Passar String ID do Wagon para o Repositório
            dbRepo.updateWagon(
                    wagon.getWagonId(),
                    newModelId,
                    newServiceYear
            );
            setSuccessStatus("Wagon updated successfully!");
        } catch (NumberFormatException e) {
            setErrorStatus("Model ID and Year must be valid integers for inline edit.");
            loadData();
        } catch (SQLException e) {
            handleSqlError(e, "Error updating wagon model/year in DB.");
            loadData();
        }
    }


    private void loadData() {
        try {
            List<Wagon> wagons = dbRepo.findAllWagons();
            ObservableList<Wagon> observableList = FXCollections.observableArrayList(wagons);
            wagonTable.setItems(observableList);
            lblStatus.setText("Data loaded successfully.");
        } catch (SQLException e) {
            setErrorStatus("Failed to load data from DB: " + e.getMessage());
        }
    }

    private void clearFields() {
        txtId.clear();
        txtModelId.clear();
        txtServiceYear.clear();
        this.selectedWagon = null;
    }

    private void setErrorStatus(String message) {
        lblStatus.setText("❌ " + message);
        if (mainController != null) mainController.showNotification(message, "error");
    }

    private void setSuccessStatus(String message) {
        lblStatus.setText("✅ " + message);
        if (mainController != null) mainController.showNotification(message, "success");
    }

    private void handleSqlError(SQLException e, String customMessage) {
        System.err.println("SQL ERROR: " + e.getErrorCode() + " - " + e.getMessage());
        setErrorStatus(customMessage);
    }
}
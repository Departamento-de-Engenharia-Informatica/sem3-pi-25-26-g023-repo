package pt.ipp.isep.dei.UI.gui.views;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.TextFieldTableCell;
import pt.ipp.isep.dei.UI.gui.MainController;
import pt.ipp.isep.dei.domain.Locomotive;
import pt.ipp.isep.dei.repository.DatabaseRepository;

import java.sql.SQLException;
import java.util.List;

public class LocomotiveCRUDController {

    @FXML private TableView<Locomotive> locomotiveTable;
    @FXML private TableColumn<Locomotive, String> idColumn;
    @FXML private TableColumn<Locomotive, String> modelColumn;
    @FXML private TableColumn<Locomotive, String> typeColumn;
    @FXML private TableColumn<Locomotive, Double> powerKWColumn; // <--- RENOMEADO

    @FXML private TextField txtId;
    @FXML private TextField txtModel;
    @FXML private TextField txtType;
    @FXML private TextField txtPowerKW; // <--- RENOMEADO

    @FXML private Button btnSave;
    @FXML private Button btnDelete;
    @FXML private Label lblStatus;

    private MainController mainController;
    private DatabaseRepository dbRepo;
    private Locomotive selectedLocomotive;

    // Inicializa o repositório
    public void setServices(MainController mainController) {
        this.mainController = mainController;
        this.dbRepo = new DatabaseRepository();
        loadData();
    }

    @FXML
    public void initialize() {
        // Configuração das colunas usando os novos métodos Property
        idColumn.setCellValueFactory(cellData -> cellData.getValue().locomotiveIdProperty());

        // Coluna MODELO (Editável)
        modelColumn.setCellValueFactory(cellData -> cellData.getValue().modelProperty());
        modelColumn.setCellFactory(TextFieldTableCell.forTableColumn());
        modelColumn.setOnEditCommit(event -> {
            Locomotive locomotive = event.getRowValue();
            handleUpdateInline(locomotive, event.getNewValue(), locomotive.getTipo(), locomotive.getPowerKW());
        });

        // Coluna TIPO (Editável)
        typeColumn.setCellValueFactory(cellData -> cellData.getValue().typeProperty());
        typeColumn.setCellFactory(TextFieldTableCell.forTableColumn());
        typeColumn.setOnEditCommit(event -> {
            Locomotive locomotive = event.getRowValue();
            handleUpdateInline(locomotive, locomotive.getModelo(), event.getNewValue(), locomotive.getPowerKW());
        });

        // Coluna POWER KW
        powerKWColumn.setCellValueFactory(cellData -> cellData.getValue().powerKWProperty().asObject()); // <--- RENOMEADO

        // Listener para seleção de linha
        locomotiveTable.getSelectionModel().selectedItemProperty().addListener(
                (observable, oldValue, newValue) -> showDetails(newValue));

        // Habilitar edição da tabela
        locomotiveTable.setEditable(true);

        // Inicializar botões
        btnDelete.setDisable(true);
        btnSave.setText("Create");
    }

    private void showDetails(Locomotive locomotive) {
        this.selectedLocomotive = locomotive;
        if (locomotive != null) {
            txtId.setText(locomotive.getLocomotiveId());
            txtModel.setText(locomotive.getModelo());
            txtType.setText(locomotive.getTipo());
            // Usa o novo getter
            txtPowerKW.setText(String.valueOf(locomotive.getPowerKW())); // <--- RENOMEADO

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
        locomotiveTable.getSelectionModel().clearSelection();
        showDetails(null);
    }

    @FXML
    void handleSave(ActionEvent event) {
        String idStr = txtId.getText().trim();
        String model = txtModel.getText().trim();
        String type = txtType.getText().trim();
        String powerKWStr = txtPowerKW.getText().trim(); // <--- RENOMEADO

        if (idStr.isEmpty() || model.isEmpty() || type.isEmpty() || powerKWStr.isEmpty()) {
            setErrorStatus("All fields (ID, Model, Type, Power KW) must be filled."); // <--- MENSAGEM ALTERADA
            return;
        }

        try {
            // CONVERSÃO CRÍTICA: String para int e double
            int id = Integer.parseInt(idStr);
            double powerKW = Double.parseDouble(powerKWStr); // <--- RENOMEADO

            if (selectedLocomotive == null) {
                // CREATE - Passa a potência
                dbRepo.addLocomotive(id, model, type, powerKW);
                setSuccessStatus("Locomotive created successfully!");
            } else {
                // UPDATE - Passa a potência
                dbRepo.updateLocomotive(id, model, type, powerKW);
                setSuccessStatus("Locomotive updated successfully!");
            }
            loadData();
            clearFields();
        } catch (NumberFormatException e) {
            setErrorStatus("Power KW and ID must be valid numbers."); // <--- MENSAGEM ALTERADA
        } catch (SQLException e) {
            handleSqlError(e, "Error saving locomotive. Check if ID already exists or constraints violated.");
        }
    }

    @FXML
    void handleDelete(ActionEvent event) {
        if (selectedLocomotive == null) return;
        try {
            // Usa o getter de int, que é o tipo correto para a DB
            dbRepo.deleteLocomotive(selectedLocomotive.getIdLocomotiva());
            setSuccessStatus("Locomotive deleted successfully!");
            loadData();
            clearFields();
        } catch (SQLException e) {
            handleSqlError(e, "Error deleting locomotive. Check for foreign key constraints!");
        }
    }

    // Método para updates inline na tabela
    private void handleUpdateInline(Locomotive locomotive, String newModel, String newType, double newPowerKW) {
        try {
            // A Locomotive é imutável, então precisamos passar todos os campos para a atualização
            dbRepo.updateLocomotive(
                    locomotive.getIdLocomotiva(),
                    newModel,
                    newType,
                    newPowerKW // <--- RENOMEADO
            );
            setSuccessStatus("Locomotive updated successfully!");
        } catch (SQLException e) {
            handleSqlError(e, "Error updating locomotive in DB.");
            loadData(); // Recarrega para reverter a alteração local
        }
    }


    private void loadData() {
        try {
            List<Locomotive> locomotives = dbRepo.findAllLocomotives();
            ObservableList<Locomotive> observableList = FXCollections.observableArrayList(locomotives);
            locomotiveTable.setItems(observableList);
            lblStatus.setText("Data loaded successfully.");
        } catch (SQLException e) {
            setErrorStatus("Failed to load data from DB: " + e.getMessage());
        }
    }

    private void clearFields() {
        txtId.clear();
        txtModel.clear();
        txtType.clear();
        txtPowerKW.clear(); // <--- RENOMEADO
        this.selectedLocomotive = null;
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
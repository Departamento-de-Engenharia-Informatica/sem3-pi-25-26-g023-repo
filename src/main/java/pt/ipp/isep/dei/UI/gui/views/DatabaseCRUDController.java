package pt.ipp.isep.dei.UI.gui.views;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.TextFieldTableCell;
import pt.ipp.isep.dei.UI.gui.MainController;
import pt.ipp.isep.dei.domain.Operator;
import pt.ipp.isep.dei.repository.DatabaseRepository;

import java.sql.SQLException;
import java.util.List;

public class DatabaseCRUDController {

    @FXML private TableView<Operator> operatorTable;
    @FXML private TableColumn<Operator, String> idColumn;
    @FXML private TableColumn<Operator, String> nameColumn;

    @FXML private TextField txtId;
    @FXML private TextField txtName;
    @FXML private Button btnSave;
    @FXML private Button btnDelete;
    @FXML private Label lblStatus;

    private MainController mainController;
    private DatabaseRepository dbRepo;
    private Operator selectedOperator;

    // Inicializa o repositório
    public void setServices(MainController mainController) {
        this.mainController = mainController;
        this.dbRepo = new DatabaseRepository();
        loadData();
    }

    @FXML
    public void initialize() {
        // Configurar as colunas para o modelo Operator
        idColumn.setCellValueFactory(cellData -> cellData.getValue().operatorIdProperty());
        nameColumn.setCellValueFactory(cellData -> cellData.getValue().nameProperty());

        // Permitir edição da coluna 'Name'
        nameColumn.setCellFactory(TextFieldTableCell.forTableColumn());
        nameColumn.setOnEditCommit(event -> {
            Operator operator = event.getRowValue();
            handleUpdateName(operator, event.getNewValue());
        });

        // Listener para seleção de linha (Chama showOperatorDetails)
        operatorTable.getSelectionModel().selectedItemProperty().addListener(
                (observable, oldValue, newValue) -> showOperatorDetails(newValue));

        // Habilitar edição da tabela
        operatorTable.setEditable(true);

        // Inicializar botões
        btnDelete.setDisable(true);
        btnSave.setText("Create");
    }

    /**
     * Lógica interna para preencher os campos de texto com o operador selecionado.
     * Este é o método que o Listener da TableView chama.
     */
    private void showOperatorDetails(Operator operator) {
        this.selectedOperator = operator;
        if (operator != null) {
            txtId.setText(operator.getOperatorId());
            txtName.setText(operator.getName());
            txtId.setDisable(true); // Não permite alterar o ID após a criação
            btnDelete.setDisable(false);
            btnSave.setText("Update");
        } else {
            clearFields();
            txtId.setDisable(false);
            btnDelete.setDisable(true);
            btnSave.setText("Create");
        }
    }

    /**
     * Chamado pelo botão "Clear Selection / New" no FXML (onAction="#handleClearSelection").
     */
    @FXML
    void handleClearSelection(ActionEvent event) {
        // Limpa a seleção da tabela e chama a lógica de reset
        operatorTable.getSelectionModel().clearSelection();
        showOperatorDetails(null);
    }

    @FXML
    void handleSave(ActionEvent event) {
        String id = txtId.getText().trim();
        String name = txtName.getText().trim();

        if (id.isEmpty() || name.isEmpty()) {
            setErrorStatus("ID and Name cannot be empty.");
            return;
        }

        try {
            if (selectedOperator == null) {
                // CREATE
                dbRepo.addOperator(id, name);
                setSuccessStatus("Operator created successfully!");
            } else {
                // UPDATE (Via botão Save)
                dbRepo.updateOperator(id, name);
                setSuccessStatus("Operator updated successfully!");
            }
            loadData();
            clearFields();
        } catch (SQLException e) {
            handleSqlError(e, "Error saving operator. Check if ID already exists.");
        }
    }

    @FXML
    void handleDelete(ActionEvent event) {
        if (selectedOperator == null) return;
        try {
            dbRepo.deleteOperator(selectedOperator.getOperatorId());
            setSuccessStatus("Operator deleted successfully!");
            loadData();
            clearFields();
        } catch (SQLException e) {
            handleSqlError(e, "Error deleting operator. Check for foreign key constraints!");
        }
    }

    private void handleUpdateName(Operator operator, String newName) {
        try {
            dbRepo.updateOperator(operator.getOperatorId(), newName);
            setSuccessStatus("Name updated successfully!");
        } catch (SQLException e) {
            handleSqlError(e, "Error updating name in DB.");
            loadData(); // Recarrega para reverter a alteração local
        }
    }

    private void loadData() {
        try {
            List<Operator> operators = dbRepo.findAllOperators();
            ObservableList<Operator> observableList = FXCollections.observableArrayList(operators);
            operatorTable.setItems(observableList);
            lblStatus.setText("Data loaded successfully.");
        } catch (SQLException e) {
            setErrorStatus("Failed to load data from DB: " + e.getMessage());
        }
    }

    private void clearFields() {
        txtId.clear();
        txtName.clear();
        this.selectedOperator = null;
    }

    private void setErrorStatus(String message) {
        lblStatus.setText("❌ " + message);
        mainController.showNotification(message, "error");
    }

    private void setSuccessStatus(String message) {
        lblStatus.setText("✅ " + message);
        mainController.showNotification(message, "success");
    }

    private void handleSqlError(SQLException e, String customMessage) {
        // Exibe o erro SQL detalhado no console para debug e a mensagem customizada na UI
        System.err.println("SQL ERROR: " + e.getErrorCode() + " - " + e.getMessage());
        setErrorStatus(customMessage);
    }
}
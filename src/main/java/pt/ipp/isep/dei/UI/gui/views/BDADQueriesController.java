package pt.ipp.isep.dei.UI.gui.views;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import pt.ipp.isep.dei.DatabaseConnection.DatabaseConnection;
import pt.ipp.isep.dei.UI.gui.MainController;

import java.sql.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class BDADQueriesController {

    @FXML private TableView<Map<String, Object>> resultsTable;
    @FXML private ProgressIndicator progressIndicator;
    @FXML private DatePicker startDatePicker;
    @FXML private DatePicker endDatePicker;
    @FXML private TextField operatorIdField;
    @FXML private GridPane inputGrid;

    private MainController mainController;
    private static final DateTimeFormatter DB_DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    // Se o seu colega criou as funções num schema específico (e.g., RAILWAY_USER) e a app usa esse, o prefixo deve ser "".
    // Se ele criou noutro schema e deu EXECUTE PERMISSION, o prefixo deve ser "OUTRO_SCHEMA."
    private static final String SCHEMA_PREFIX = "";

    public void setMainController(MainController mainController) {
        this.mainController = mainController;
    }

    @FXML
    public void initialize() {
        System.out.println("[DEBUG] Initializing BDADQueriesController...");
        progressIndicator.setVisible(false);
        resultsTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        // REMOÇÃO DA RECRIAÇÃO DE FUNÇÕES.
        // As funções DEVE SER criadas e geridas pelo DBA/Colega no servidor.
        // System.out.println("[DEBUG] Creating database functions...");
        // createDatabaseFunctions();

        System.out.println("[DEBUG] Initialization complete.");
    }

    // O MÉTODO createDatabaseFunctions FOI REMOVIDO DAQUI
    /*
    private void createDatabaseFunctions() {
        // ... CÓDIGO REMOVIDO PARA EVITAR SOBRESCREVER FUNÇÕES CORRETAS ...
    }
    */

    // ============================= USBD26 =============================
    @FXML
    public void runUSBD26() {
        LocalDate startDate = startDatePicker.getValue();
        LocalDate endDate = endDatePicker.getValue();

        if (startDate == null || endDate == null || startDate.isAfter(endDate)) {
            mainController.showNotification("Error: Please select valid start and end dates.", "error");
            return;
        }

        String sql = "{ ? = call " + SCHEMA_PREFIX + "GETUNUSEDWAGONSINPERIOD(?, ?) }";
        String start = startDate.format(DB_DATE_FORMAT);
        String end = endDate.format(DB_DATE_FORMAT);
        String title = "USBD26 - Unused Wagons from " + start + " to " + end;

        System.out.println("[DEBUG] Running USBD26 with start=" + start + ", end=" + end);
        executeCallableQuery(sql, title, stmt -> {
            stmt.registerOutParameter(1, Types.REF_CURSOR);
            stmt.setString(2, start);
            stmt.setString(3, end);
        });
    }

    // ============================= USBD27 =============================
    @FXML
    public void runUSBD27() {
        String sql = "{ ? = call " + SCHEMA_PREFIX + "GETUNIVERSALGRAINWAGONS() }";
        String title = "USBD27 - Universal Grain Wagons";

        System.out.println("[DEBUG] Running USBD27...");
        executeCallableQuery(sql, title, stmt -> {
            stmt.registerOutParameter(1, Types.REF_CURSOR);
        });
    }

    // ============================= USBD28 =============================
    @FXML
    public void runUSBD28() {
        String operatorId = operatorIdField.getText().trim().toUpperCase();

        if (operatorId.isEmpty()) {
            mainController.showNotification("Error: Please enter an Operator ID.", "error");
            return;
        }

        String sql = "{ ? = call " + SCHEMA_PREFIX + "GETMULTIGAUGELOCOMOTIVES(?) }";
        String title = "USBD28 - Multi-Gauge Locomotives for " + operatorId;

        System.out.println("[DEBUG] Running USBD28 with operatorId=" + operatorId);
        executeCallableQuery(sql, title, stmt -> {
            stmt.registerOutParameter(1, Types.REF_CURSOR);
            stmt.setString(2, operatorId);
        });
    }

    // ... (executeCallableQuery e métodos de suporte permanecem os mesmos)
    // ============================= CORE LOGIC =============================
    private void executeCallableQuery(String sql, String title, CallableStatementSetter setter) {
        System.out.println("[DEBUG] Executing query: " + title);
        progressIndicator.setVisible(true);
        resultsTable.getItems().clear();
        resultsTable.getColumns().clear();
        resultsTable.setPlaceholder(new Label("Executing query: " + title + "..."));

        Task<List<Map<String, Object>>> queryTask = new Task<>() {
            @Override
            protected List<Map<String, Object>> call() throws Exception {
                System.out.println("[DEBUG] Inside Task.call() for " + title);
                return executeDBFunction(sql, setter);
            }

            @Override
            protected void succeeded() {
                List<Map<String, Object>> results = getValue();
                System.out.println("[DEBUG] Query succeeded: " + results.size() + " rows");

                progressIndicator.setVisible(false);
                updateTable(results);

                mainController.showNotification(
                        String.format("Query %s finished successfully. %d rows found.", title, results.size()), "success");
            }

            @Override
            protected void failed() {
                progressIndicator.setVisible(false);
                String error = "DB Query Failed: " + getException().getMessage();
                resultsTable.setPlaceholder(new Label("ERROR: " + error));
                mainController.showNotification(error, "error");
                getException().printStackTrace();
                System.err.println("[ERROR] Query failed: " + title);
            }
        };

        new Thread(queryTask).start();
    }

    @FunctionalInterface
    private interface CallableStatementSetter {
        void setParameters(CallableStatement stmt) throws SQLException;
    }

    private List<Map<String, Object>> executeDBFunction(String sql, CallableStatementSetter setter) throws SQLException {
        List<Map<String, Object>> results = new ArrayList<>();

        System.out.println("[DEBUG] Preparing callable statement: " + sql);
        try (Connection conn = DatabaseConnection.getConnection();
             CallableStatement stmt = conn.prepareCall(sql)) {

            setter.setParameters(stmt);
            System.out.println("[DEBUG] Parameters set, executing statement...");
            stmt.execute();
            System.out.println("[DEBUG] Statement executed successfully, fetching results...");

            try (ResultSet rs = (ResultSet) stmt.getObject(1)) {
                ResultSetMetaData metaData = rs.getMetaData();
                int columnCount = metaData.getColumnCount();
                System.out.println("[DEBUG] ResultSet metadata columns: " + columnCount);

                while (rs.next()) {
                    Map<String, Object> row = new HashMap<>();
                    for (int i = 1; i <= columnCount; i++) {
                        String columnName = metaData.getColumnLabel(i);
                        Object value = rs.getObject(i);
                        row.put(columnName, value);
                        // [DEBUG] Removido para otimizar o output da consola: System.out.println("[DEBUG] Row value: " + columnName + "=" + value);
                    }
                    results.add(row);
                }
            }
        }
        System.out.println("[DEBUG] Total rows fetched: " + results.size());
        return results;
    }

    private void updateTable(List<Map<String, Object>> data) {
        if (data == null || data.isEmpty()) {
            resultsTable.setPlaceholder(new Label("No data found for this query."));
            resultsTable.getColumns().clear();
            System.out.println("[DEBUG] No data found to display in table.");
            return;
        }

        resultsTable.getColumns().clear();
        Map<String, Object> sampleRow = data.get(0);

        for (String columnName : sampleRow.keySet()) {
            TableColumn<Map<String, Object>, String> column = new TableColumn<>(columnName);
            column.setCellValueFactory(cellData -> {
                Object value = cellData.getValue().get(columnName);
                return new javafx.beans.property.SimpleStringProperty(value != null ? value.toString() : "NULL");
            });
            column.setMinWidth(120);
            resultsTable.getColumns().add(column);
        }

        ObservableList<Map<String, Object>> observableData = FXCollections.observableArrayList(data);
        resultsTable.setItems(observableData);
        resultsTable.setPlaceholder(new Label("Query executed successfully. " + data.size() + " rows found."));
        System.out.println("[DEBUG] Table updated with " + data.size() + " rows.");
    }
}
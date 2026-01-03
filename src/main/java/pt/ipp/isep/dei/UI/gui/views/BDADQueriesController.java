package pt.ipp.isep.dei.UI.gui.views;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import pt.ipp.isep.dei.DatabaseConnection.DatabaseConnection;
import pt.ipp.isep.dei.UI.gui.MainController;

import java.sql.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class BDADQueriesController {

    // ==================== COMPONENTES PARTILHADOS ====================
    @FXML private TableView<Map<String, Object>> resultsTable;
    @FXML private TextArea txtOutput;
    @FXML private ProgressIndicator progressIndicator;
    private MainController mainController;

    // ==================== US ANTIGAS (USBD26-28) ====================
    @FXML private DatePicker startDatePicker;
    @FXML private DatePicker endDatePicker;
    @FXML private ComboBox<String> cmbOperator28; // Alterado para ComboBox

    // ==================== US NOVAS (USBD33-41) ====================

    // USBD33
    @FXML private ComboBox<String> cmbTrain33; // Alterado
    @FXML private Label lblResult33;

    // USBD36
    @FXML private ComboBox<String> cmbFacility36; // Alterado
    @FXML private TextField txtBuildingId36;
    @FXML private TextField txtBuildingName36;
    @FXML private TextField txtBuildingType36;

    // USBD38
    @FXML private TextField txtGaugeVal38;
    @FXML private TextField txtGaugeName38;
    @FXML private TextField txtGaugeDesc38;

    // USBD39
    @FXML private ComboBox<String> cmbTrain39; // Alterado
    @FXML private ComboBox<String> cmbLoco39;  // Alterado

    // USBD41
    @FXML private ComboBox<String> cmbTrain41; // Alterado
    @FXML private TextField txtFreightId41;

    // Constantes
    private static final DateTimeFormatter DB_DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final String SCHEMA_PREFIX = "";

    public void setMainController(MainController mainController) {
        this.mainController = mainController;
    }

    @FXML
    public void initialize() {
        System.out.println("[DEBUG] Initializing BDADQueriesController...");
        if (progressIndicator != null) progressIndicator.setVisible(false);
        if (resultsTable != null) resultsTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        // POPULAR AS LISTAS AO INICIAR
        populateDropdowns();
    }

    /**
     * Vai buscar IDs à base de dados para preencher os ComboBoxes e facilitar a vida ao utilizador.
     */
    private void populateDropdowns() {
        // 1. Comboios (Para US33, US39, US41)
        List<String> trains = fetchColumnList("SELECT train_id FROM TRAIN ORDER BY train_id ASC", "train_id");
        fillCombo(trains, cmbTrain33, cmbTrain39, cmbTrain41);

        // 2. Instalações (Para US36)
        List<String> facilities = fetchColumnList("SELECT facility_id FROM FACILITY ORDER BY facility_id ASC", "facility_id");
        fillCombo(facilities, cmbFacility36);

        // 3. Locomotivas (Para US39)
        List<String> locos = fetchColumnList("SELECT stock_id FROM LOCOMOTIVE ORDER BY stock_id ASC", "stock_id");
        fillCombo(locos, cmbLoco39);

        // 4. Operadores (Para US28)
        // Assumindo que existe uma tabela OPERATOR ou que se pode obter do WAGON/LOCOMOTIVE
        // Se a tabela OPERATOR não existir, tenta obter de WAGON distinct operator_id
        List<String> operators = fetchColumnList("SELECT DISTINCT operator_id FROM WAGON WHERE operator_id IS NOT NULL", "operator_id");
        fillCombo(operators, cmbOperator28);
    }

    private void fillCombo(List<String> data, ComboBox<String>... boxes) {
        ObservableList<String> list = FXCollections.observableArrayList(data);
        for (ComboBox<String> box : boxes) {
            if (box != null) {
                box.setItems(list);
            }
        }
    }

    private List<String> fetchColumnList(String query, String colName) {
        List<String> list = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(query);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(rs.getString(colName));
            }
        } catch (Exception e) {
            log("Error fetching list for " + colName + ": " + e.getMessage());
        }
        return list;
    }

    // ==================== USBD26 (Reports) ====================
    @FXML
    public void runUSBD26() {
        if (startDatePicker == null || endDatePicker == null) return;
        LocalDate startDate = startDatePicker.getValue();
        LocalDate endDate = endDatePicker.getValue();

        if (startDate == null || endDate == null || startDate.isAfter(endDate)) {
            showNotification("Error: Please select valid start and end dates.", "error");
            return;
        }

        String sql = "{ ? = call " + SCHEMA_PREFIX + "GETUNUSEDWAGONSINPERIOD(?, ?) }";
        String start = startDate.format(DB_DATE_FORMAT);
        String end = endDate.format(DB_DATE_FORMAT);
        String title = "USBD26 - Unused Wagons";

        executeCallableQuery(sql, title, stmt -> {
            stmt.registerOutParameter(1, Types.REF_CURSOR);
            stmt.setString(2, start);
            stmt.setString(3, end);
        });
    }

    // ==================== USBD27 ====================
    @FXML
    public void runUSBD27() {
        String sql = "{ ? = call " + SCHEMA_PREFIX + "GETUNIVERSALGRAINWAGONS() }";
        executeCallableQuery(sql, "USBD27 - Grain Wagons", stmt -> {
            stmt.registerOutParameter(1, Types.REF_CURSOR);
        });
    }

    // ==================== USBD28 ====================
    @FXML
    public void runUSBD28() {
        String operatorId = cmbOperator28.getValue(); // Agora vem do ComboBox
        if (operatorId == null || operatorId.isEmpty()) {
            showNotification("Select an Operator first.", "error");
            return;
        }
        String sql = "{ ? = call " + SCHEMA_PREFIX + "GETMULTIGAUGELOCOMOTIVES(?) }";
        executeCallableQuery(sql, "USBD28 - Locomotives for " + operatorId, stmt -> {
            stmt.registerOutParameter(1, Types.REF_CURSOR);
            stmt.setString(2, operatorId);
        });
    }

    // ==================== NOVAS US (PROCEDURES/FUNCTIONS) ====================

    private void log(String message) {
        if (txtOutput != null) txtOutput.appendText(message + "\n");
        System.out.println("[BDDAD] " + message);
    }

    // USBD33
    @FXML
    void handleUSBD33(ActionEvent event) {
        String trainId = cmbTrain33.getValue();
        if (trainId == null) { log("Select a train first."); return; }

        try (Connection conn = DatabaseConnection.getConnection();
             CallableStatement cstmt = conn.prepareCall("{? = call get_train_length(?)}")) {
            cstmt.registerOutParameter(1, Types.NUMERIC);
            cstmt.setString(2, trainId);
            cstmt.execute();
            double length = cstmt.getDouble(1);
            lblResult33.setText(String.format("Result: %.2f m", length));
            log("Train " + trainId + " Length: " + length + "m");
        } catch (SQLException e) {
            log("Error US33: " + e.getMessage());
        }
    }

    // USBD36
    @FXML
    void handleUSBD36(ActionEvent event) {
        try {
            int buildId = Integer.parseInt(txtBuildingId36.getText());
            String name = txtBuildingName36.getText();
            String type = txtBuildingType36.getText();
            String facStr = cmbFacility36.getValue(); // ID da combo

            if (facStr == null) { log("Select a facility."); return; }
            int facId = Integer.parseInt(facStr); // Assumindo que a combo guarda o ID

            try (Connection conn = DatabaseConnection.getConnection();
                 CallableStatement cstmt = conn.prepareCall("{? = call add_building_to_facility(?, ?, ?, ?)}")) {
                cstmt.registerOutParameter(1, Types.NUMERIC);
                cstmt.setInt(2, buildId);
                cstmt.setString(3, name);
                cstmt.setString(4, type);
                cstmt.setInt(5, facId);
                cstmt.execute();
                log("Success: Building added.");
                txtBuildingId36.clear(); txtBuildingName36.clear();
            }
        } catch (Exception e) {
            log("Error US36: " + e.getMessage());
        }
    }

    // USBD38
    @FXML
    void handleUSBD38(ActionEvent event) {
        try {
            int mm = Integer.parseInt(txtGaugeVal38.getText());
            String name = txtGaugeName38.getText();
            String desc = txtGaugeDesc38.getText();

            try (Connection conn = DatabaseConnection.getConnection();
                 CallableStatement cstmt = conn.prepareCall("{? = call add_new_gauge(?, ?, ?)}")) {
                cstmt.registerOutParameter(1, Types.VARCHAR);
                cstmt.setInt(2, mm);
                cstmt.setString(3, name);
                cstmt.setString(4, desc);
                cstmt.execute();
                log("US38 Result: " + cstmt.getString(1));
            }
        } catch (Exception e) {
            log("Error US38: " + e.getMessage());
        }
    }

    // USBD39
    @FXML
    void handleUSBD39(ActionEvent event) {
        String trainId = cmbTrain39.getValue();
        String locoId = cmbLoco39.getValue();
        if (trainId == null || locoId == null) { log("Select both Train and Loco."); return; }

        try (Connection conn = DatabaseConnection.getConnection();
             CallableStatement cstmt = conn.prepareCall("{call prc_associate_locomotive_to_train(?, ?)}")) {
            cstmt.setString(1, trainId);
            cstmt.setString(2, locoId);
            cstmt.execute();
            log("US39: Association executed (check DB/Logs).");
        } catch (SQLException e) {
            log("Error US39: " + e.getMessage());
        }
    }

    // USBD41
    @FXML
    void handleUSBD41(ActionEvent event) {
        String trainId = cmbTrain41.getValue();
        String freightStr = txtFreightId41.getText();
        if (trainId == null || freightStr.isEmpty()) { log("Select Train and enter Freight ID."); return; }

        try (Connection conn = DatabaseConnection.getConnection();
             CallableStatement cstmt = conn.prepareCall("{call prc_remove_freight_from_train(?, ?)}")) {
            cstmt.setString(1, trainId);
            cstmt.setInt(2, Integer.parseInt(freightStr));
            cstmt.execute();
            log("US41: Removal executed.");
        } catch (Exception e) {
            log("Error US41: " + e.getMessage());
        }
    }

    // ==================== HELPER TABLE LOGIC ====================
    private void showNotification(String msg, String type) {
        if (mainController != null) mainController.showNotification(msg, type);
        else log("[" + type + "] " + msg);
    }

    private void executeCallableQuery(String sql, String title, CallableStatementSetter setter) {
        if (progressIndicator != null) progressIndicator.setVisible(true);
        if (resultsTable != null) {
            resultsTable.getItems().clear();
            resultsTable.getColumns().clear();
            resultsTable.setPlaceholder(new Label("Executing " + title + "..."));
        }

        Task<List<Map<String, Object>>> task = new Task<>() {
            @Override
            protected List<Map<String, Object>> call() throws Exception {
                List<Map<String, Object>> results = new ArrayList<>();
                try (Connection conn = DatabaseConnection.getConnection();
                     CallableStatement stmt = conn.prepareCall(sql)) {
                    setter.setParameters(stmt);
                    stmt.execute();
                    try (ResultSet rs = (ResultSet) stmt.getObject(1)) {
                        ResultSetMetaData meta = rs.getMetaData();
                        int colCount = meta.getColumnCount();
                        while (rs.next()) {
                            Map<String, Object> row = new HashMap<>();
                            for (int i = 1; i <= colCount; i++) row.put(meta.getColumnLabel(i), rs.getObject(i));
                            results.add(row);
                        }
                    }
                }
                return results;
            }

            @Override
            protected void succeeded() {
                if (progressIndicator != null) progressIndicator.setVisible(false);
                updateTable(getValue());
                showNotification(title + " finished.", "success");
            }

            @Override
            protected void failed() {
                if (progressIndicator != null) progressIndicator.setVisible(false);
                log("Error: " + getException().getMessage());
                if (resultsTable != null) resultsTable.setPlaceholder(new Label("Error occurred."));
            }
        };
        new Thread(task).start();
    }

    private void updateTable(List<Map<String, Object>> data) {
        if (resultsTable == null) return;
        if (data == null || data.isEmpty()) {
            resultsTable.setPlaceholder(new Label("No results found."));
            return;
        }
        resultsTable.getColumns().clear();
        for (String colName : data.get(0).keySet()) {
            TableColumn<Map<String, Object>, String> col = new TableColumn<>(colName);
            col.setCellValueFactory(p -> new javafx.beans.property.SimpleStringProperty(
                    p.getValue().get(colName) == null ? "" : p.getValue().get(colName).toString()));
            resultsTable.getColumns().add(col);
        }
        resultsTable.setItems(FXCollections.observableArrayList(data));
    }

    @FunctionalInterface
    private interface CallableStatementSetter {
        void setParameters(CallableStatement stmt) throws SQLException;
    }
}
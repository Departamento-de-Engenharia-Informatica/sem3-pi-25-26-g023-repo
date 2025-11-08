package pt.ipp.isep.dei.UI.gui.views;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import pt.ipp.isep.dei.UI.gui.MainController;
import pt.ipp.isep.dei.domain.EuropeanStation;
import pt.ipp.isep.dei.domain.KDTree;

import java.util.List;
import java.util.stream.Collectors;

public class Usei08Controller {

    @FXML
    private TextField txtLatMin, txtLatMax, txtLonMin, txtLonMax, txtCountry;
    @FXML
    private ComboBox<String> cmbIsCity, cmbIsMain;
    @FXML
    private Button btnSearch;
    @FXML
    private Label lblStatus;
    @FXML
    private TextArea txtResult;

    private MainController mainController;
    private KDTree spatialKDTree;

    public void setServices(MainController mainController, KDTree spatialKDTree) {
        this.mainController = mainController;
        this.spatialKDTree = spatialKDTree;
    }

    @FXML
    public void initialize() {
        ObservableList<String> options = FXCollections.observableArrayList("Any", "Yes", "No");
        cmbIsCity.setItems(options);
        cmbIsMain.setItems(options);
        cmbIsCity.setValue("Any");
        cmbIsMain.setValue("Any");
    }

    @FXML
    void handleSearchArea(ActionEvent event) {
        lblStatus.setText("");
        txtResult.clear();
        lblStatus.getStyleClass().removeAll("status-label-success", "status-label-error");

        if (spatialKDTree == null) {
            setErrorStatus("Error: KDTree was not injected correctly.");
            return;
        }

        try {
            double latMin = parseDoubleField(txtLatMin, -90.0);
            double latMax = parseDoubleField(txtLatMax, 90.0);
            double lonMin = parseDoubleField(txtLonMin, -180.0);
            double lonMax = parseDoubleField(txtLonMax, 180.0);

            if (latMin > latMax || lonMin > lonMax) {
                setErrorStatus("Error: Minimum coordinates cannot be greater than maximum.");
                return;
            }

            String country = txtCountry.getText().trim();
            if (country.isEmpty()) {
                country = null;
            } else {
                country = country.toUpperCase();
            }

            Boolean isCity = parseOptionalBoolean(cmbIsCity.getValue());
            Boolean isMain = parseOptionalBoolean(cmbIsMain.getValue());

            lblStatus.setText("Searching..."); // Info local
            long startTime = System.nanoTime();
            List<EuropeanStation> results = spatialKDTree.searchInRange(
                    latMin, latMax, lonMin, lonMax,
                    country,
                    isCity, isMain
            );
            long endTime = System.nanoTime();

            double timeMs = (endTime - startTime) / 1_000_000.0;
            String successMsg = String.format("Found %d stations (%.2f ms)", results.size(), timeMs);
            setSuccessStatus(successMsg); // Isto agora também faz o pop-up

            if (results.isEmpty()) {
                txtResult.setText("--- No stations found matching these filters ---");
            } else {
                StringBuilder sb = new StringBuilder();
                results.stream().forEach(station ->
                        sb.append(formatStationDisplay(station)).append("\n")
                );
                txtResult.setText(sb.toString());
            }

        } catch (NumberFormatException e) {
            setErrorStatus("Error: Invalid coordinates. Please enter valid numbers.");
        } catch (Exception e) {
            setErrorStatus("Unexpected error during search: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // --- MÉTODOS HELPER ---

    private String formatStationDisplay(EuropeanStation station) {
        return station.getStation() +
                " [" + station.getCountry() + "] " +
                "(" + String.format("%.6f", station.getLatitude()) + ", " +
                String.format("%.6f", station.getLongitude()) + ")" +
                (station.isCity() ? " 🏙️" : "") +
                (station.isMainStation() ? " ⭐" : "");
    }

    private double parseDoubleField(TextField field, double defaultValue) throws NumberFormatException {
        String text = field.getText().trim().replace(',', '.');
        if (text.isEmpty()) {
            if(field == txtLatMin) return -90.0;
            if(field == txtLatMax) return 90.0;
            if(field == txtLonMin) return -180.0;
            if(field == txtLonMax) return 180.0;
        }
        return Double.parseDouble(text);
    }

    private Boolean parseOptionalBoolean(String value) {
        if (value.equals("Yes")) {
            return true;
        }
        if (value.equals("No")) {
            return false;
        }
        return null; // "Any"
    }

    // --- Helpers de Status (ATUALIZADOS) ---

    private void setErrorStatus(String message) {
        lblStatus.setText("❌ " + message);
        lblStatus.getStyleClass().add("status-label-error");

        // ✅ Notificação Pop-up
        if (mainController != null) {

        }
    }

    private void setSuccessStatus(String message) {
        lblStatus.setText("✅ " + message);
        lblStatus.getStyleClass().add("status-label-success");

        // ✅ Notificação Pop-up
        if (mainController != null) {

        }
    }
}
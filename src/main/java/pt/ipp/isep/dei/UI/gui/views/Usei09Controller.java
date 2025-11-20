package pt.ipp.isep.dei.UI.gui.views;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import pt.ipp.isep.dei.UI.gui.MainController;
// import pt.ipp.isep.dei.UI.gui.GuiUtils; // Já não é necessário para o padrão toastr
import pt.ipp.isep.dei.domain.EuropeanStation;
import pt.ipp.isep.dei.domain.GeoDistance;
import pt.ipp.isep.dei.domain.KDTree;

import java.util.List;

public class Usei09Controller {

    @FXML
    private TextField txtLatTarget, txtLonTarget, txtNearestN, txtTimeZoneGroup;
    @FXML
    private Button btnSearch;
    @FXML
    private Label lblStatus;
    @FXML
    private TextArea txtResult;

    private MainController mainController;
    private KDTree spatialKDTree;


    /**
     * Injeta os serviços necessários do MainController.
     */
    public void setServices(MainController mainController, KDTree spatialKDTree) {
        this.mainController = mainController;
        this.spatialKDTree = spatialKDTree;
    }

    @FXML
    public void initialize() {
        // Inicialização vazia.
    }

    @FXML
    void handleNearestNSearch(ActionEvent event) {
        lblStatus.setText("");
        txtResult.clear();
        lblStatus.getStyleClass().removeAll("status-label-success", "status-label-error");

        if (spatialKDTree == null || spatialKDTree.size() == 0) {
            handleError("Error: KDTree not loaded or empty. Run USEI06/07 first.");
            return;
        }

        try {
            // 1. Validar e Obter Inputs com verificação de limites
            double targetLat = parseCoordinateField(txtLatTarget, "Latitude", -90.0, 90.0);
            double targetLon = parseCoordinateField(txtLonTarget, "Longitude", -180.0, 180.0);
            int N = parseNField(txtNearestN);

            String timeZoneFilter = txtTimeZoneGroup.getText().trim();
            String filter = timeZoneFilter.isEmpty() ? null : timeZoneFilter.toUpperCase();

            lblStatus.setText("Searching for nearest N...");
            long startTime = System.nanoTime();

            // 2. Chamar a lógica de Domínio (KDTree)
            List<EuropeanStation> results = spatialKDTree.findNearestN(
                    targetLat, targetLon, N, filter
            );

            long endTime = System.nanoTime();

            // 3. Apresentar Resultados
            double timeMs = (endTime - startTime) / 1_000_000.0;
            String successMsg = String.format("Found %d nearest stations (%.2f ms)", results.size(), timeMs);
            setSuccessStatus(successMsg); // Usa o novo helper (padrão USEI01)

            if (results.isEmpty()) {
                txtResult.setText("--- No stations found matching the criteria ---");
            } else {
                StringBuilder sb = new StringBuilder();
                sb.append(String.format("Target: (%.4f, %.4f)\n", targetLat, targetLon));
                sb.append("Time Zone Filter: ").append(filter == null ? "None" : filter).append("\n");
                sb.append("=".repeat(80)).append("\n");

                int i = 1;
                for (EuropeanStation s : results) {
                    double distance = GeoDistance.haversine(targetLat, targetLon, s.getLatitude(), s.getLongitude());
                    sb.append(String.format("%2d. [%.2f km] %s\n",
                            i++, distance, formatStationDisplay(s)));
                }
                txtResult.setText(sb.toString());
            }

        } catch (NumberFormatException e) {
            handleError("Invalid number format. Check Latitude, Longitude, or N.");
        } catch (IllegalArgumentException e) {
            handleError("Input Error: " + e.getMessage());
        } catch (Exception e) {
            handleError("UNEXPECTED ERROR: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // --- MÉTODOS HELPER DE VALIDAÇÃO ---

    private double parseCoordinateField(TextField field, String name, double min, double max)
            throws NumberFormatException, IllegalArgumentException {
        String text = field.getText().trim().replace(',', '.');
        if (text.isEmpty()) {
            throw new IllegalArgumentException(name + " cannot be empty.");
        }
        double value = Double.parseDouble(text);
        if (value < min || value > max) {
            throw new IllegalArgumentException(name + " must be between " + min + " and " + max + ".");
        }
        return value;
    }

    private int parseNField(TextField field) throws NumberFormatException, IllegalArgumentException {
        String text = field.getText().trim();
        if (text.isEmpty()) {
            throw new IllegalArgumentException("N (Nearest Stations) cannot be empty.");
        }
        int N = Integer.parseInt(text);
        if (N < 1 || N > 100) {
            throw new IllegalArgumentException("N must be a positive integer between 1 and 100.");
        }
        return N;
    }

    // --- MÉTODOS HELPER DE DISPLAY (CORRIGIDOS PARA O PADRÃO USEI01) ---

    private String formatStationDisplay(EuropeanStation station) {
        return station.getStation() +
                " [" + station.getCountry() + "] " +
                "(" + String.format("%.4f", station.getLatitude()) + ", " +
                String.format("%.4f", station.getLongitude()) + ")" +
                " TZ: " + station.getTimeZoneGroup() +
                (station.isCity() ? " 🏙️" : "") +
                (station.isMainStation() ? " ⭐" : "");
    }

    /**
     * Padrão USEI01: Lida com erros e aciona o pop-up vermelho.
     */
    private void handleError(String message) {
        lblStatus.setText("❌ ERROR: " + message);
        lblStatus.getStyleClass().add("status-label-error");

        if (mainController != null) {
            // Padrão USEI01: Notificação pop-up vermelha (string "error")
            mainController.showNotification(message, "error");
        }
    }

    /**
     * Padrão USEI01: Lida com sucesso e aciona o pop-up verde.
     */
    private void setSuccessStatus(String message) {
        lblStatus.setText("✅ " + message);
        lblStatus.getStyleClass().add("status-label-success");

        if (mainController != null) {
            // Padrão USEI01: Notificação pop-up verde (string "success")
            mainController.showNotification(message, "success");
        }
    }
}
package pt.ipp.isep.dei.UI.gui.views;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import pt.ipp.isep.dei.UI.gui.MainController;
import pt.ipp.isep.dei.domain.PickingPathService;
import pt.ipp.isep.dei.domain.PickingPlan;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Map;

public class Usei04Controller {

    @FXML
    private Button btnCalculate;
    @FXML
    private Label lblStatus;
    @FXML
    private TextArea txtResultA;
    @FXML
    private TextArea txtResultB;

    private MainController mainController;

    public void setServices(MainController mainController) {
        this.mainController = mainController;
    }

    @FXML
    void handleCalculatePaths(ActionEvent event) {
        lblStatus.setText("");
        lblStatus.getStyleClass().removeAll("status-label-success", "status-label-error", "status-label-info");
        txtResultA.clear();
        txtResultB.clear();

        if (this.mainController == null) {
            setError("Error: MainController was not injected correctly.");
            return;
        }

        PickingPlan plan = mainController.getLastPickingPlan();

        if (plan == null) {
            setError("You must run [USEI03] Pack Trolleys first. No picking plan is available.");
            return;
        }
        if (plan.getTotalTrolleys() == 0) {
            setInfo("The current picking plan has 0 trolleys. Nothing to calculate.");
            // Nota: "Info" não tem pop-up, o que é bom.
            return;
        }

        setInfo(String.format("Calculating paths for %d trolleys in Plan %s...",
                plan.getTotalTrolleys(), plan.getId()));

        PickingPathService pathService = new PickingPathService();
        try {
            Map<String, PickingPathService.PathResult> pathResults = pathService.calculatePickingPaths(plan);

            if (pathResults.isEmpty()) {
                setError("Could not calculate paths (check if picking plan has valid locations).");
            } else {

                PickingPathService.PathResult resultA = pathResults.get("Strategy A (Deterministic Sweep)");
                if (resultA != null) {
                    String cleanTextA = cleanAnsiCodes(resultA.toString());
                    txtResultA.setText(cleanTextA.trim());
                } else {
                    txtResultA.setText("--- Failed to calculate Strategy A ---");
                }

                PickingPathService.PathResult resultB = pathResults.get("Strategy B (Nearest Neighbour)");
                if (resultB != null) {
                    String cleanTextB = cleanAnsiCodes(resultB.toString());
                    txtResultB.setText(cleanTextB.trim());
                } else {
                    txtResultB.setText("--- Failed to calculate Strategy B ---");
                }

                setSuccess("USEI04 completed successfully!");
            }

        } catch (Exception e) {
            setError("Error calculating picking paths (USEI04): " + e.getMessage());
            txtResultA.appendText("\n\nStack Trace:\n");
            e.printStackTrace(new java.io.PrintStream(new TextAreaOutputStream(txtResultA)));
        }
    }

    private String cleanAnsiCodes(String text) {
        if (text == null) {
            return "";
        }
        return text.replaceAll("\\[\\d+m", "");
    }


    // --- Helpers de Status (ATUALIZADOS) ---
    private void setError(String message) {
        lblStatus.setText("❌ " + message);
        lblStatus.getStyleClass().removeAll("status-label-success", "status-label-info");
        lblStatus.getStyleClass().add("status-label-error");

        // ✅ Notificação Pop-up
        if (mainController != null) {
            mainController.showNotification(message, "error");
        }
    }

    private void setSuccess(String message) {
        lblStatus.setText("✅ " + message);
        lblStatus.getStyleClass().removeAll("status-label-error", "status-label-info");
        lblStatus.getStyleClass().add("status-label-success");

        // ✅ Notificação Pop-up
        if (mainController != null) {
            mainController.showNotification(message, "success");
        }
    }

    // Info não precisa de pop-up, fica só no label local.
    private void setInfo(String message) {
        lblStatus.setText("ℹ️ " + message);
        lblStatus.getStyleClass().removeAll("status-label-error", "status-label-success");
        lblStatus.getStyleClass().add("status-label-info");
    }

    public static class TextAreaOutputStream extends OutputStream {
        private TextArea textArea;
        public TextAreaOutputStream(TextArea textArea) { this.textArea = textArea; }
        @Override
        public void write(byte[] b, int off, int len) throws IOException {
            String text = new String(b, off, len);
            Platform.runLater(() -> textArea.appendText(text));
        }
        @Override
        public void write(int b) throws IOException {
            write(new byte[]{(byte) b}, 0, 1);
        }
    }
}
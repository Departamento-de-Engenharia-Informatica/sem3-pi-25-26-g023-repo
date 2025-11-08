package pt.ipp.isep.dei.UI.gui.views;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import pt.ipp.isep.dei.UI.gui.MainController;
import pt.ipp.isep.dei.domain.StationIndexManager;

import java.util.Map;

public class Usei07Controller {

    @FXML
    private Button btnAnalyze;
    @FXML
    private Label lblStatus;
    @FXML
    private TextArea txtResult;

    private MainController mainController;
    private StationIndexManager stationIndexManager;

    public void setServices(MainController mainController, StationIndexManager stationIndexManager) {
        this.mainController = mainController;
        this.stationIndexManager = stationIndexManager;
    }

    @FXML
    void handleAnalyzeTree(ActionEvent event) {
        // Podes manter o lblStatus para feedback de "loading"
        lblStatus.setText("ℹ️  Analyzing 2D-Tree...");
        txtResult.clear();

        if (stationIndexManager == null) {
            // ✅ Usa a nova notificação!
            mainController.showNotification("Error: StationIndexManager was not injected.", "error");
            lblStatus.setText("Error."); // Atualiza o status local
            return;
        }

        try {
            Map<String, Object> stats = stationIndexManager.get2DTreeStats();

            // ✅ Usa a nova notificação!
            mainController.showNotification("2D-Tree analysis complete.", "success");
            lblStatus.setText("✅ Analysis complete."); // Atualiza o status local

            // 2. Formatar o output (sem alteração)
            StringBuilder sb = new StringBuilder();
            sb.append("--- [USEI07] Build & Analyze 2D-Tree ---\n");
            sb.append("✅ 2D-Tree analysis complete.\n");
            sb.append("\n--- 2D-Tree Statistics ---\n");

            sb.append(String.format("  Size (Nodes): %-10d ", stats.get("size")));
            sb.append(String.format("Height: %d%n", stats.get("height")));

            sb.append("  Node Capacity (Stations per Node):\n");

            @SuppressWarnings("unchecked")
            Map<Integer, Integer> buckets = (Map<Integer, Integer>) stats.get("bucketSizes");

            buckets.entrySet().stream()
                    .sorted(Map.Entry.comparingByKey())
                    .forEach(entry -> sb.append(String.format(
                            "    - %d station(s)/node : %d nodes%n",
                            entry.getKey(), entry.getValue()
                    )));

            sb.append("\n--- Build Analysis ---\n");
            sb.append("  Strategy:    Balanced build using pre-sorted lists (from USEI06).\n");
            sb.append("  Complexity:  O(N log N)\n");

            txtResult.setText(sb.toString());

        } catch (Exception e) {
            // ✅ Usa a nova notificação!
            mainController.showNotification("Error analyzing 2D-Tree: " + e.getMessage(), "error");
            lblStatus.setText("❌ Error."); // Atualiza o status local

            txtResult.setText("❌ ERROR: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
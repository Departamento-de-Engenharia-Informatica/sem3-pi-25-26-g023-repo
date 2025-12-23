package pt.ipp.isep.dei.UI.gui.views;

import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextArea;
import pt.ipp.isep.dei.domain.RailwayFlowService;

import java.util.Map;
import java.util.TreeMap;

public class Usei14Controller {

    @FXML
    private ComboBox<String> cmbSource;

    @FXML
    private ComboBox<String> cmbSink;

    @FXML
    private TextArea txtResult;

    private final RailwayFlowService flowService;
    private final Map<String, Integer> stationStringToIntMap;

    public Usei14Controller() {
        this.flowService = new RailwayFlowService();
        this.stationStringToIntMap = new TreeMap<>();
    }

    @FXML
    public void initialize() {
        loadData();
    }

    private void loadData() {
        try {
            // Caminhos para os ficheiros (ajusta se necessário)
            String stationsFile = "src/main/java/pt/ipp/isep/dei/FicheirosCSV/stations.csv";
            String linesFile = "src/main/java/pt/ipp/isep/dei/FicheirosCSV/lines.csv";

            flowService.loadGraphFromCSV(stationsFile, linesFile);

            // Popula as ComboBoxes
            Map<Integer, String> stations = flowService.getAllCsvStations();

            for (Map.Entry<Integer, String> entry : stations.entrySet()) {
                String display = String.format("[%d] %s", entry.getKey(), entry.getValue());
                stationStringToIntMap.put(display, entry.getKey());
                cmbSource.getItems().add(display);
                cmbSink.getItems().add(display);
            }

            // Otimização de pesquisa na ComboBox (Opcional: libraries externas fazem isto melhor, aqui fica simples)

        } catch (Exception e) {
            showAlert("Error", "Failed to load CSV data: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    void onCalculate(ActionEvent event) {
        String sourceStr = cmbSource.getValue();
        String sinkStr = cmbSink.getValue();

        if (sourceStr == null || sinkStr == null) {
            showAlert("Warning", "Please select both Source and Sink stations.");
            return;
        }

        int sourceId = stationStringToIntMap.get(sourceStr);
        int sinkId = stationStringToIntMap.get(sinkStr);

        if (sourceId == sinkId) {
            showAlert("Warning", "Source and Sink cannot be the same.");
            return;
        }

        try {
            long startTime = System.nanoTime();
            double maxFlow = flowService.maximumThroughput(sourceId, sinkId);
            long endTime = System.nanoTime();
            double durationMs = (endTime - startTime) / 1_000_000.0;

            StringBuilder sb = new StringBuilder();
            sb.append("Calculation Successful!\n");
            sb.append("--------------------------------------------------\n");
            sb.append(String.format("From: %s\n", sourceStr));
            sb.append(String.format("To:   %s\n", sinkStr));
            sb.append("--------------------------------------------------\n");
            sb.append(String.format("MAXIMUM THROUGHPUT: %.0f trains/day\n", maxFlow));
            sb.append("--------------------------------------------------\n");
            sb.append(String.format("Time elapsed: %.2f ms", durationMs));

            txtResult.setText(sb.toString());

        } catch (Exception e) {
            showAlert("Error", "Calculation failed: " + e.getMessage());
        }
    }

    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}
package pt.ipp.isep.dei.UI.gui.views;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.TextArea;
import pt.ipp.isep.dei.domain.BackboneNetwork;

import java.io.File;

public class Usei12Controller {

    @FXML
    private TextArea txtLog;

    @FXML
    void onGenerateBackbone(ActionEvent event) {
        txtLog.clear();
        appendLog("Initializing Backbone Network process...");

        try {
            BackboneNetwork backboneNetwork = new BackboneNetwork();

            String stationsFile = "src/main/java/pt/ipp/isep/dei/FicheirosCSV/stations.csv";
            String linesFile = "src/main/java/pt/ipp/isep/dei/FicheirosCSV/lines.csv";

            appendLog("Loading CSVs: " + stationsFile);
            backboneNetwork.loadNetwork(stationsFile, linesFile);
            appendLog("Network loaded.");

            appendLog("Computing Minimal Spanning Tree (Kruskal/Prim)...");
            backboneNetwork.computeMinimalBackbone();
            appendLog("MST Computed.");

            String dotFile = "belgian_backbone.dot";
            String svgFile = "belgian_backbone.svg";

            appendLog("Generating DOT file: " + dotFile);
            backboneNetwork.generateDOTFile(dotFile);

            appendLog("Attempting to generate SVG (requires GraphViz)...");
            boolean svgSuccess = backboneNetwork.generateSVG(dotFile, svgFile);

            if (svgSuccess) {
                appendLog("SUCCESS: SVG generated at " + new File(svgFile).getAbsolutePath());
                showAlert("Success", "Backbone generated successfully!\nCheck the root folder for 'belgian_backbone.svg'.");
            } else {
                appendLog("WARNING: SVG could not be generated automatically (GraphViz missing?).");
                appendLog("You can manually convert the DOT file using: neato -Tsvg " + dotFile + " -o " + svgFile);
                showAlert("Partial Success", "DOT file generated, but SVG failed.\nSee log for manual instructions.");
            }

        } catch (Exception e) {
            appendLog("ERROR: " + e.getMessage());
            e.printStackTrace();
            showAlert("Error", "Failed to generate backbone: " + e.getMessage());
        }
    }

    private void appendLog(String msg) {
        txtLog.appendText(msg + "\n");
    }

    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}
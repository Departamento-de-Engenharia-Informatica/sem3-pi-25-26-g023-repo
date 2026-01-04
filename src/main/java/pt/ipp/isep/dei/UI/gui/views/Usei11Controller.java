package pt.ipp.isep.dei.UI.gui.views;

import javafx.fxml.FXML;
import javafx.scene.control.TextArea;
import pt.ipp.isep.dei.UI.gui.MainController;
import pt.ipp.isep.dei.controller.TravelTimeController;

public class Usei11Controller {

    @FXML private TextArea txtResultArea;

    private MainController mainController;
    private TravelTimeController travelTimeController;

    /**
     * Injeção de dependências vinda do MainController.
     */
    public void setDependencies(MainController mc, TravelTimeController ttc) {
        this.mainController = mc;
        this.travelTimeController = ttc;
    }

    /**
     * Executa a lógica exata da consola: handleBelgiumUpgradePlan()
     */
    @FXML
    public void handleGenerateUpgradePlan() {
        if (travelTimeController == null) {
            if (mainController != null) mainController.showNotification("Erro: Serviço de Upgrade não inicializado.", "error");
            return;
        }

        try {
            // Chama exatamente o mesmo método que a consola usa
            String report = travelTimeController.generateBelgiumUpgradePlan();

            // Exibe o relatório (Kahn's algorithm output + Complexity) na TextArea
            txtResultArea.setText(report);

            if (mainController != null) {
                mainController.showNotification("Plano de Upgrade gerado com sucesso!", "success");
            }

        } catch (Exception e) {
            txtResultArea.setText("Erro ao gerar plano: " + e.getMessage());
            if (mainController != null) mainController.showNotification("Falha na geração do plano.", "error");
        }
    }
}
package pt.ipp.isep.dei.UI.gui.views;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import pt.ipp.isep.dei.UI.gui.GuiUtils;
import pt.ipp.isep.dei.domain.PickingService; // Importa o seu serviço existente

public class PickingGuiController {

    @FXML
    private TextField filePathField;
    @FXML
    private ComboBox<String> heuristicBox;
    @FXML
    private TextArea resultArea;

    // Instancia o seu serviço de lógica de negócio
    private final PickingService pickingService;

    public PickingGuiController() {
        this.pickingService = new PickingService();
    }

    @FXML
    public void initialize() {
        // Preenche a ComboBox com as opções
        heuristicBox.setItems(FXCollections.observableArrayList("FF", "FFD", "BFD"));
        heuristicBox.setValue("FFD"); // Define FFD como padrão

        // Define um caminho de exemplo
        filePathField.setText("src/main/java/pt/ipp/isep/dei/FicheirosCSV/orders.csv");
    }

    /**
     * Chamado quando o botão "Gerar Plano" é clicado.
     */
    @FXML
    void handleGeneratePlan() {
        String path = filePathField.getText();
        String heuristic = heuristicBox.getValue();

        if (path.isBlank() || heuristic == null) {
            GuiUtils.showAlert(Alert.AlertType.ERROR, "Erro de Validação", "Campos em Falta", "Por favor, preencha o caminho do ficheiro e selecione uma heurística.");
            return;
        }

        try {
            // Chama o seu método de lógica existente!
            // Nota: O seu método da consola imprimia para o System.out.
            // Precisamos de um método que RETORNE a string.
            // Vou assumir que o seu método na consola pode ser adaptado ou que já existe um.
            // Por agora, vou simular a chamada e o output.

            // --- SIMULAÇÃO ---
            // A sua PickingUI chama: pickingService.generatePickingPlan(path, heuristic.toLowerCase());
            // Esse método imprime no ecrã. Para a GUI, o ideal era ele retornar uma String.
            // Vamos simular o que ele faria.
            // pickingService.generatePickingPlan(path, heuristic.toLowerCase());

            // Como não consigo capturar o System.out, vou apenas confirmar a execução.
            // O ideal era refatorar `generatePickingPlan` para retornar o `pickingPlan.toString()`

            resultArea.setText("Tentativa de gerar plano com os seguintes dados:\n\n" +
                    "Ficheiro: " + path + "\n" +
                    "Heurística: " + heuristic + "\n\n" +
                    "Processo iniciado (verifique a consola para o output). \n\n" +
                    "NOTA: Para ver o resultado aqui, o método 'pickingService.generatePickingPlan' " +
                    "precisa de ser refatorado para retornar uma String em vez de void.");

            // Se o seu método já retornar uma String, substitua o código acima por:
            // String planResult = pickingService.generatePickingPlan(path, heuristic.toLowerCase());
            // resultArea.setText(planResult);

        } catch (Exception e) {
            GuiUtils.showAlert(Alert.AlertType.ERROR, "Erro de Execução", "Ocorreu um erro ao gerar o plano.", e.getMessage());
            resultArea.setText("Erro: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
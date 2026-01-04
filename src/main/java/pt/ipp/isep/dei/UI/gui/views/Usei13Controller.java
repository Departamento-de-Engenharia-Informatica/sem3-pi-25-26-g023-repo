package pt.ipp.isep.dei.UI.gui.views;

import javafx.fxml.FXML;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import pt.ipp.isep.dei.UI.gui.MainController;
import pt.ipp.isep.dei.domain.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Controller para a USEI13 - Análise de Centralidade de Hubs.
 */
public class Usei13Controller {

    @FXML private TableView<StationMetrics> tblHubs;
    @FXML private TableColumn<StationMetrics, Integer> colId;
    @FXML private TableColumn<StationMetrics, String> colName;
    @FXML private TableColumn<StationMetrics, Double> colHubScore;
    @FXML private TextField txtLimit;

    private MainController mainController;

    /**
     * Inicializa as colunas da tabela.
     * O PropertyValueFactory utiliza os métodos getStid, getStname e getHubScore.
     */
    @FXML
    public void initialize() {
        colId.setCellValueFactory(new PropertyValueFactory<>("stid"));
        colName.setCellValueFactory(new PropertyValueFactory<>("stname"));
        colHubScore.setCellValueFactory(new PropertyValueFactory<>("hubScore"));
    }

    /**
     * Injeta a dependência do controlador principal.
     */
    public void setDependencies(MainController mc) {
        this.mainController = mc;
    }

    /**
     * Calcula o ranking de hubs utilizando os algoritmos de centralidade.
     * Baseado na lógica implementada na CargoHandlingUI.
     */
    @FXML
    public void handleComputeHubs() {
        try {
            // Caminhos dos ficheiros CSV conforme a estrutura do projeto
            String stationsFile = "src/main/java/pt/ipp/isep/dei/FicheirosCSV/stations.csv";
            String linesFile = "src/main/java/pt/ipp/isep/dei/FicheirosCSV/lines.csv";

            // Carregamento do grafo
            Graph g = CSVLoader.load(stationsFile, linesFile);

            // Cálculo das métricas de centralidade
            DegreeStrength.compute(g);
            HarmonicCloseness.compute(g);
            Betweenness.compute(g);
            HubScoreCalculator.compute(g);

            // Ordenação dos resultados pelo Hub Score descendente
            List<StationMetrics> ranking = new ArrayList<>(g.metricsMap.values());
            ranking.sort((m1, m2) -> Double.compare(m2.hubScore, m1.hubScore));

            // Aplicação de limite (N) se fornecido pelo utilizador
            if (txtLimit != null && !txtLimit.getText().trim().isEmpty()) {
                try {
                    int n = Integer.parseInt(txtLimit.getText().trim());
                    if (n > 0 && n < ranking.size()) {
                        ranking = ranking.subList(0, n);
                    }
                } catch (NumberFormatException e) {
                    if (mainController != null) {
                        mainController.showNotification("Limite inválido. Mostrando todos os resultados.", "info");
                    }
                }
            }

            // Atualização da UI
            tblHubs.getItems().setAll(ranking);

            if (mainController != null) {
                mainController.showNotification("Ranking de Hubs calculado com sucesso!", "success");
            }

        } catch (Exception e) {
            e.printStackTrace();
            if (mainController != null) {
                mainController.showNotification("Erro ao calcular: " + e.getMessage(), "error");
            }
        }
    }
}
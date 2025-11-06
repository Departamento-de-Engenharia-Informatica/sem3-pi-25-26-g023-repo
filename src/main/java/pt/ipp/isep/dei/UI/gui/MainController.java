package pt.ipp.isep.dei.UI.gui;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;

import java.io.IOException;
import java.net.URL;

/**
 * Controlador principal da aplicação (Versão Simplificada).
 * Gere a navegação lateral e carrega os "sub-ecrãs" na área central.
 */
public class MainController {

    // Estas variáveis SÃO OBRIGATÓRIAS e têm de ter um fx:id no FXML
    @FXML
    private BorderPane mainPane;
    @FXML
    private AnchorPane centerContentPane; // <-- O que estava a dar erro (NullPointerException)
    @FXML
    private Label statusLabel;

    /**
     * Chamado automaticamente quando o FXML é carregado.
     * Carrega o ecrã inicial do dashboard.
     */
    @FXML
    public void initialize() {
        statusLabel.setText("Bem-vindo ao Projeto Integrado G023!");
        // Carrega o dashboard como ecrã inicial
        loadView("dashboard-view.fxml");
    }

    /**
     * Carrega uma nova vista FXML na área de conteúdo central (centerContentPane).
     * @param fxmlFileName O nome do ficheiro FXML (ex: "dashboard-view.fxml")
     */
    private void loadView(String fxmlFileName) {
        try {
            // Usa o ClassLoader para encontrar o FXML na raiz de /resources
            URL fxmlUrl = getClass().getClassLoader().getResource(fxmlFileName);
            if (fxmlUrl == null) {
                System.err.println("Erro Crítico: Não foi possível encontrar o FXML: " + fxmlFileName);
                statusLabel.setText("Erro: Não foi possível encontrar a vista " + fxmlFileName);
                return;
            }

            Parent view = FXMLLoader.load(fxmlUrl);

            // Limpa o conteúdo antigo e adiciona a nova vista
            // Esta é a linha que estava a falhar
            centerContentPane.getChildren().clear();
            centerContentPane.getChildren().add(view);

            // Faz com que a nova vista preencha todo o AnchorPane
            AnchorPane.setTopAnchor(view, 0.0);
            AnchorPane.setBottomAnchor(view, 0.0);
            AnchorPane.setLeftAnchor(view, 0.0);
            AnchorPane.setRightAnchor(view, 0.0);

        } catch (IOException e) {
            System.err.println("Falha ao carregar a vista: " + fxmlFileName);
            e.printStackTrace();
            statusLabel.setText("Erro ao carregar ecrã.");
        }
    }

    // --- MÉTODOS LIGADOS AOS BOTÕES DO FXML ---

    @FXML
    void handleShowDashboard(ActionEvent event) {
        statusLabel.setText("A mostrar o Dashboard");
        loadView("dashboard-view.fxml");
    }

    // Apaguei os métodos handleShowESINF, handleShowLAPR3, etc.
    // para corresponder ao FXML simplificado.
}
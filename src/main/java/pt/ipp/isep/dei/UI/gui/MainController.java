package pt.ipp.isep.dei.UI.gui;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.scene.text.Text;

public class MainController {

    // Referência ao painel principal, para podermos trocar o conteúdo central
    @FXML
    private BorderPane mainPane;

    // Um painel "placeholder" para o centro
    @FXML
    private Pane centerContentPane;

    // Referência a um Label no FXML
    @FXML
    private Label statusLabel;

    /**
     * Este método é chamado automaticamente pelo JavaFX depois do FXML ser carregado.
     * Perfeito para inicializações.
     */
    @FXML
    public void initialize() {
        statusLabel.setText("Bem-vindo ao Projeto Integrado G023!");

        // Exemplo de como carregar um "dashboard" inicial
        Text welcomeText = new Text("Selecione uma opção no menu lateral.");
        welcomeText.setStyle("-fx-font-size: 20px; -fx-fill: -fx-text-base-color;");
        welcomeText.layoutXProperty().bind(centerContentPane.widthProperty().subtract(welcomeText.layoutBoundsProperty().get().getWidth()).divide(2));
        welcomeText.layoutYProperty().bind(centerContentPane.heightProperty().subtract(welcomeText.layoutBoundsProperty().get().getHeight()).divide(2));

        centerContentPane.getChildren().add(welcomeText);
    }

    // --- MÉTODOS LIGADOS AOS BOTÕES DO FXML ---
    // O nome 'handleShowUSEI01' deve corresponder ao 'onAction' no FXML

    @FXML
    void handleShowDashboard(ActionEvent event) {
        // Aqui pode carregar um FXML de dashboard no centro
        statusLabel.setText("A mostrar o Dashboard");
        // Exemplo: mainPane.setCenter(novoPainel);
        centerContentPane.getChildren().clear(); // Limpa o conteúdo
        // Adiciona texto de exemplo
        Text t = new Text("Conteúdo do Dashboard");
        t.setStyle("-fx-font-size: 16px; -fx-fill: -fx-text-base-color;");
        t.setLayoutX(20);
        t.setLayoutY(40);
        centerContentPane.getChildren().add(t);
    }

    @FXML
    void handleShowESINF(ActionEvent event) {
        // Aqui pode carregar um FXML de ESINF no centro
        statusLabel.setText("Funcionalidades ESINF");
        centerContentPane.getChildren().clear();
        Text t = new Text("Carregar aqui o FXML da USEI01, USEI02, etc.");
        t.setStyle("-fx-font-size: 16px; -fx-fill: -fx-text-base-color;");
        t.setLayoutX(20);
        t.setLayoutY(40);
        centerContentPane.getChildren().add(t);
    }

    @FXML
    void handleShowLAPR3(ActionEvent event) {
        // Aqui pode carregar um FXML de LAPR3 no centro
        statusLabel.setText("Funcionalidades LAPR3");
        centerContentPane.getChildren().clear();

        // Exemplo: Ligar à sua lógica existente
        // TravelTimeController ttc = new TravelTimeController();
        // String result = ttc.getTravelTime(...)

        Text t = new Text("Carregar aqui o FXML da USLP03 (Travel Time)");
        t.setStyle("-fx-font-size: 16px; -fx-fill: -fx-text-base-color;");
        t.setLayoutX(20);
        t.setLayoutY(40);
        centerContentPane.getChildren().add(t);
    }

    @FXML
    void handleShowBDDAD(ActionEvent event) {
        statusLabel.setText("Funcionalidades BDDAD");
        centerContentPane.getChildren().clear();
        Text t = new Text("Carregar aqui o FXML de consulta à BD");
        t.setStyle("-fx-font-size: 16px; -fx-fill: -fx-text-base-color;");
        t.setLayoutX(20);
        t.setLayoutY(40);
        centerContentPane.getChildren().add(t);
    }
}
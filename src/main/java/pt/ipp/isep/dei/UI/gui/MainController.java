package pt.ipp.isep.dei.UI.gui;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;

// Imports de todo o teu backend
import pt.ipp.isep.dei.UI.gui.views.TravelTimeGUIController;
import pt.ipp.isep.dei.controller.TravelTimeController;
import pt.ipp.isep.dei.domain.InventoryManager;
import pt.ipp.isep.dei.domain.KDTree;
import pt.ipp.isep.dei.domain.StationIndexManager;
import pt.ipp.isep.dei.domain.WMS;

import java.io.IOException;
import java.net.URL;

public class MainController {

    @FXML
    private BorderPane mainPane;
    @FXML
    private AnchorPane centerContentPane;
    @FXML
    private Label statusLabel;

    // --- CAMPOS PARA GUARDAR OS SERVIÇOS DE BACKEND ---
    private WMS wms;
    private InventoryManager manager;
    private TravelTimeController travelTimeController;
    private StationIndexManager stationIndexManager;
    private KDTree spatialKDTree;

    /**
     * Este método é chamado pela MainApplication para "injetar" os serviços.
     */
    public void setBackendServices(WMS wms, InventoryManager manager,
                                   TravelTimeController ttc,
                                   StationIndexManager sim, KDTree kdt) {
        this.wms = wms;
        this.manager = manager;
        this.travelTimeController = ttc;
        this.stationIndexManager = sim;
        this.spatialKDTree = kdt;

        // Atualiza o status com dados reais
        statusLabel.setText(String.format("Bem-vindo! %d itens, %d caixas em inventário.",
                manager.getItemsCount(), wms.getInventory().getBoxes().size()));
    }

    @FXML
    public void initialize() {
        statusLabel.setText("A carregar serviços...");
        // Em vez de carregar um FXML estático, chama o método que constrói o menu dinâmico
        // NOTA: Tivemos de o fazer "à força" com 'Platform.runLater'
        // para garantir que o 'centerContentPane' não é nulo.
        javafx.application.Platform.runLater(() -> {
            handleShowDashboard(null); // O 'null' é porque não vem de um clique
        });
    }

    /**
     * Carrega uma nova vista FXML na área de conteúdo central.
     * Este método é agora usado pelos *outros* botões (LAPR3, ESINF, etc.)
     */
    private void loadView(String fxmlFileName, Object backendService) {
        try {
            // Usa o ClassLoader para encontrar o FXML na raiz de /resources
            URL fxmlUrl = getClass().getClassLoader().getResource(fxmlFileName);
            if (fxmlUrl == null) {
                System.err.println("Erro Crítico: Não foi possível encontrar o FXML: " + fxmlFileName);
                statusLabel.setText("Erro: Não foi possível encontrar a vista " + fxmlFileName);
                return;
            }

            FXMLLoader loader = new FXMLLoader(fxmlUrl);
            Parent view = loader.load();

            // --- INJEÇÃO DE DEPENDÊNCIA (Para o sub-ecrã) ---
            if (backendService != null) {
                if (backendService instanceof TravelTimeController && loader.getController() instanceof TravelTimeGUIController) {
                    TravelTimeGUIController controller = loader.getController();
                    controller.setBackend((TravelTimeController) backendService);
                }
                // (Adicionar mais 'else if' para outros módulos)
            }
            // --- FIM DA INJEÇÃO ---

            centerContentPane.getChildren().clear();
            centerContentPane.getChildren().add(view);
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
        centerContentPane.getChildren().clear();

        // 1. Cria o VBox que vai conter o menu
        VBox menuContainer = new VBox(10); // 10px de espaçamento
        menuContainer.setPadding(new Insets(30));
        menuContainer.getStyleClass().add("dashboard-menu"); // Para o CSS

        // 2. Adiciona o Título
        Label title = new Label("Dashboard & Ações Rápidas");
        title.getStyleClass().add("dashboard-title");

        // 3. Adiciona os Stats (usando o backend)
        Label statsLabel = new Label(String.format("Inventário: %d caixas | Quarentena: %d devoluções | Estações: %d",
                wms.getInventory().getBoxes().size(),
                wms.getQuarantine().size(),
                manager.getValidStationCount()));
        statsLabel.getStyleClass().add("dashboard-stats");

        menuContainer.getChildren().addAll(title, statsLabel);

        // 4. Constrói as secções do menu (tal como na CargoHandlingUI)
        //

        // --- Secção LAPR3 ---
        Label lapr3Title = new Label("--- Railway & Station Ops (S1 & S2) ---");
        lapr3Title.getStyleClass().add("dashboard-section-title");

        Button buttonUSLP03 = new Button("[USLP03] Calcular Tempo de Viagem (S1)");
        buttonUSLP03.setMaxWidth(Double.MAX_VALUE);
        // Ação: Chama o *outro* handler que já tínhamos!
        buttonUSLP03.setOnAction(this::handleShowLAPR3);

        Button buttonUSEI06 = new Button("[USEI06] Query European Station Index (S2)");
        buttonUSEI06.setMaxWidth(Double.MAX_VALUE);
        buttonUSEI06.setOnAction(this::handleShowESINF); // Reutiliza o ESINF por agora

        Button buttonUSEI08 = new Button("[USEI08] Spatial Queries - Search by Area (S2)");
        buttonUSEI08.setMaxWidth(Double.MAX_VALUE);
        buttonUSEI08.setOnAction(this::handleShowESINF); // Reutiliza o ESINF por agora

        menuContainer.getChildren().addAll(lapr3Title, buttonUSLP03, buttonUSEI06, buttonUSEI08);

        // --- Secção ESINF ---
        Label esinfTitle = new Label("--- Warehouse Setup & Picking (S1) ---");
        esinfTitle.getStyleClass().add("dashboard-section-title");

        Button buttonUSEI01 = new Button("[USEI01] Unload Wagons");
        buttonUSEI01.setMaxWidth(Double.MAX_VALUE);
        buttonUSEI01.setOnAction(this::handleShowESINF);

        Button buttonUSEI02 = new Button("[USEI02] Allocate Orders");
        buttonUSEI02.setMaxWidth(Double.MAX_VALUE);
        buttonUSEI02.setOnAction(this::handleShowESINF);

        Button buttonUSEI03 = new Button("[USEI03] Pack Trolleys");
        buttonUSEI03.setMaxWidth(Double.MAX_VALUE);
        buttonUSEI03.setOnAction(this::handleShowESINF);

        Button buttonUSEI04 = new Button("[USEI04] Calculate Pick Path");
        buttonUSEI04.setMaxWidth(Double.MAX_VALUE);
        buttonUSEI04.setOnAction(this::handleShowESINF);

        Button buttonUSEI05 = new Button("[USEI05] Process Quarantine Returns");
        buttonUSEI05.setMaxWidth(Double.MAX_VALUE);
        buttonUSEI05.setOnAction(this::handleShowESINF);

        menuContainer.getChildren().addAll(esinfTitle, buttonUSEI01, buttonUSEI02, buttonUSEI03, buttonUSEI04, buttonUSEI05);

        // 5. Adiciona o menu ao ecrã
        centerContentPane.getChildren().add(menuContainer);
        AnchorPane.setTopAnchor(menuContainer, 0.0);
        AnchorPane.setBottomAnchor(menuContainer, 0.0);
        AnchorPane.setLeftAnchor(menuContainer, 0.0);
        AnchorPane.setRightAnchor(menuContainer, 0.0);
    }

    @FXML
    void handleShowESINF(ActionEvent event) {
        statusLabel.setText("Funcionalidades ESINF");
        centerContentPane.getChildren().clear();

        // TODO: Criar um 'esinf-view.fxml' e o seu controlador
        // loadView("esinf-view.fxml", wms); // Passa o WMS ou o Manager

        Text t = new Text("Ecrã ESINF (Em Construção)");
        t.setStyle("-fx-font-size: 24px; -fx-fill: -fx-text-base-color;");
        centerContentPane.getChildren().add(t);
        AnchorPane.setTopAnchor(t, 50.0);
        AnchorPane.setLeftAnchor(t, 50.0);
    }

    @FXML
    void handleShowLAPR3(ActionEvent event) {
        statusLabel.setText("Funcionalidades LAPR3");
        // Carrega o FXML de LAPR3 e passa-lhe o controlador de backend
        loadView("lapr3-travel-time-view.fxml", this.travelTimeController);
    }

    @FXML
    void handleShowBDDAD(ActionEvent event) {
        statusLabel.setText("Funcionalidades BDDAD");
        centerContentPane.getChildren().clear();

        Text t = new Text("Ecrã BDDAD (Em Construção)");
        t.setStyle("-fx-font-size: 24px; -fx-fill: -fx-text-base-color;");
        centerContentPane.getChildren().add(t);
        AnchorPane.setTopAnchor(t, 50.0);
        AnchorPane.setLeftAnchor(t, 50.0);
    }
}
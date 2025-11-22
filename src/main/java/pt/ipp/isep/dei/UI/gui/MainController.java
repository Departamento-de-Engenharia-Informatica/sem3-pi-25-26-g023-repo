package pt.ipp.isep.dei.UI.gui;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import pt.ipp.isep.dei.UI.gui.views.*;
import pt.ipp.isep.dei.UI.gui.views.TrainCRUDController;
import pt.ipp.isep.dei.controller.TravelTimeController;
import pt.ipp.isep.dei.domain.*;
import javafx.animation.FadeTransition;
import javafx.animation.PauseTransition;
import javafx.util.Duration;
import pt.ipp.isep.dei.repository.FacilityRepository;
import pt.ipp.isep.dei.repository.LocomotiveRepository;
import pt.ipp.isep.dei.repository.TrainRepository;
// --- NOVOS IMPORTS NECESSÁRIOS ---
import pt.ipp.isep.dei.repository.StationRepository;
import pt.ipp.isep.dei.repository.SegmentLineRepository;

import java.io.IOException;
import java.net.URL;

public class MainController {

    @FXML
    private BorderPane mainPane;
    @FXML
    private AnchorPane centerContentPane;
    @FXML
    private Label statusLabel;

    @FXML
    private Label statusAllocations;
    @FXML
    private Label statusPicking;

    // --- Backend Services ---
    private WMS wms;
    private InventoryManager manager;
    private TravelTimeController travelTimeController;
    private StationIndexManager stationIndexManager;
    private KDTree spatialKDTree;

    // -------------------------------------------------------------
    // --- CORREÇÃO DE INICIALIZAÇÃO DE DEPENDÊNCIAS COMPLEXAS ---
    // 1. Inicializar os repositórios de base
    private TrainRepository trainRepository = new TrainRepository();
    private FacilityRepository facilityRepository = new FacilityRepository();
    private LocomotiveRepository locomotiveRepository = new LocomotiveRepository();
    private StationRepository stationRepository = new StationRepository(); // <-- NOVO!
    private SegmentLineRepository segmentLineRepository = new SegmentLineRepository(); // <-- NOVO!

    // 2. Inicializar serviços que dependem dos repositórios

    // SchedulerService exige 2 argumentos: StationRepository e FacilityRepository
    private SchedulerService schedulerService = new SchedulerService(
            this.stationRepository,
            this.facilityRepository
    );

    // RailwayNetworkService exige 2 argumentos: StationRepository e SegmentLineRepository
    private RailwayNetworkService networkService = new RailwayNetworkService(
            this.stationRepository,
            this.segmentLineRepository
    );

    // 3. Inicializar o DispatcherService com 5 argumentos (Repos e Serviços)
    private DispatcherService dispatcherService = new DispatcherService(
            this.trainRepository,
            this.networkService,
            this.facilityRepository,
            this.locomotiveRepository,
            this.schedulerService
    );
    // -------------------------------------------------------------


    // --- Global Status Flags ---
    private boolean isAllocationsRun = false;
    private boolean isPickingRun = false;

    // --- ESTADO GLOBAL (como na CargoHandlingUI) ---
    private AllocationResult lastAllocationResult = null;
    private PickingPlan lastPickingPlan = null;


    public void setBackendServices(WMS wms, InventoryManager manager,
                                   TravelTimeController ttc,
                                   StationIndexManager sim, KDTree kdt) {
        this.wms = wms;
        this.manager = manager;
        this.travelTimeController = ttc;
        this.stationIndexManager = sim;
        this.spatialKDTree = kdt;

        statusLabel.setText(String.format("Welcome! %d items, %d boxes in inventory.",
                manager.getItemsCount(), wms.getInventory().getBoxes().size()));
    }

    @FXML
    public void initialize() {
        statusLabel.setText("Loading services...");
        updateStatusHeader();

        javafx.application.Platform.runLater(() -> {
            handleShowDashboard(null);
        });
    }

    /**
     * Carrega uma nova vista FXML na área de conteúdo central.
     */
    private void loadView(String fxmlFileName, Object backendService) {
        try {
            URL fxmlUrl = getClass().getClassLoader().getResource(fxmlFileName);
            if (fxmlUrl == null) {
                System.err.println("Critical Error: Could not find FXML: " + fxmlFileName);
                statusLabel.setText("Error: Could not find view " + fxmlFileName);
                return;
            }

            FXMLLoader loader = new FXMLLoader(fxmlUrl);
            Parent view = loader.load();
            Object controller = loader.getController();

            // --- INJEÇÃO DE DEPENDÊNCIA (Atualizada) ---

            if (backendService instanceof TravelTimeController && controller instanceof TravelTimeGUIController) {
                ((TravelTimeGUIController) controller).setBackend((TravelTimeController) backendService);
            }

            else if (controller instanceof DashboardController) {
                ((DashboardController) controller).setServices(this.wms, this.manager);
            }

            else if (controller instanceof Usei01Controller) {
                ((Usei01Controller) controller).setServices(this, this.wms, this.manager);
            }

            else if (controller instanceof Usei02Controller) {
                if (backendService instanceof InventoryManager) {
                    ((Usei02Controller) controller).setServices(this, (InventoryManager) backendService);
                } else {
                    System.err.println("Erro de injeção: Usei02Controller esperava um InventoryManager.");
                }
            }

            // ✅ --- NOVO BLOCO PARA USEI03 ---
            else if (controller instanceof Usei03Controller) {
                if (backendService instanceof InventoryManager) {
                    ((Usei03Controller) controller).setServices(this, (InventoryManager) backendService);
                } else {
                    System.err.println("Erro de injeção: Usei03Controller esperava um InventoryManager.");
                }
            }
            // ✅ --- NOVO BLOCO PARA USEI04 ---
            else if (controller instanceof Usei04Controller) {
                // A USEI04 precisa do MainController para aceder ao getLastPickingPlan()
                if (backendService instanceof MainController) {
                    ((Usei04Controller) controller).setServices((MainController) backendService);
                } else {
                    System.err.println("Erro de injeção: Usei04Controller esperava um MainController.");
                }
            }

            // ✅ --- NOVO BLOCO PARA USEI05 ---
            else if (controller instanceof Usei05Controller) {
                // O Usei05Controller precisa do MainController, WMS e Manager
                ((Usei05Controller) controller).setServices(this, this.wms, this.manager);
            }
            // --- FIM DO NOVO BLOCO ---

            else if (controller instanceof Usei06Controller) {
                if (backendService instanceof StationIndexManager) {
                    ((Usei06Controller) controller).setServices(this, (StationIndexManager) backendService);
                } else {
                    System.err.println("Erro de injeção: Usei06Controller esperava um StationIndexManager.");
                }
            }
            // ✅ --- NOVO BLOCO PARA USEI07 ---
            else if (controller instanceof Usei07Controller) {
                if (backendService instanceof StationIndexManager) {
                    ((Usei07Controller) controller).setServices(this, (StationIndexManager) backendService);
                } else {
                    System.err.println("Erro de injeção: Usei07Controller esperava um StationIndexManager.");
                }
            }

            // ✅ --- NOVO BLOCO PARA USEI08 ---
            else if (controller instanceof Usei08Controller) {
                // A USEI08 precisa da KDTree
                if (backendService instanceof KDTree) {
                    ((Usei08Controller) controller).setServices(this, (KDTree) backendService);
                } else {
                    System.err.println("Erro de injeção: Usei08Controller esperava a KDTree.");
                }
            }

            // ✅ --- NOVO BLOCO PARA BDDADMainController (ADICIONADO) ---
            else if (controller instanceof BDDADMainController) {
                if (backendService instanceof MainController) {
                    // O BDDADMainController precisa do MainController para chamar loadOperatorCrudView()
                    ((BDDADMainController) controller).setServices((MainController) backendService);
                } else {
                    System.err.println("Erro de injeção: BDDADMainController esperava um MainController.");
                }
            }
            // --- FIM DO NOVO BLOCO DE INJEÇÃO ---

            // --- END OF INJECTION ---

            centerContentPane.getChildren().clear();
            centerContentPane.getChildren().add(view);
            AnchorPane.setTopAnchor(view, 0.0);
            AnchorPane.setBottomAnchor(view, 0.0);
            AnchorPane.setLeftAnchor(view, 0.0);
            AnchorPane.setRightAnchor(view, 0.0);

        } catch (IOException e) {
            System.err.println("Failed to load view: " + fxmlFileName);
            e.printStackTrace();
            statusLabel.setText("Error loading screen.");
        }
    }


    // --- MÉTODOS PÚBLICOS PARA ATUALIZAR O STATUS ---

    public void updateStatusAllocations(boolean hasRun) {
        this.isAllocationsRun = hasRun;
        updateStatusHeader();
    }

    public void updateStatusPicking(boolean hasRun) {
        this.isPickingRun = hasRun;
        updateStatusHeader();
    }


    private void updateStatusHeader() {
        if (statusAllocations == null || statusPicking == null) {
            return;
        }

        updateLabelStyle(statusAllocations, isAllocationsRun);
        updateLabelStyle(statusPicking, isPickingRun);
    }


    private void updateLabelStyle(Label label, boolean hasRun) {
        if (hasRun) {
            label.setText("RUN");
            label.getStyleClass().remove("status-not-run");
            label.getStyleClass().add("status-run");
        } else {
            label.setText("NOT-RUN");
            label.getStyleClass().remove("status-run");
            label.getStyleClass().add("status-not-run");
        }
    }


    // --- MÉTODOS PÚBLICOS PARA ESTADO GLOBAL ---
    // (Necessários para USEI02 -> USEI03 -> USEI04)

    public AllocationResult getLastAllocationResult() {
        return lastAllocationResult;
    }

    public void setLastAllocationResult(AllocationResult lastAllocationResult) {
        this.lastAllocationResult = lastAllocationResult;
    }

    public PickingPlan getLastPickingPlan() {
        return lastPickingPlan;
    }

    public void setLastPickingPlan(PickingPlan lastPickingPlan) {
        this.lastPickingPlan = lastPickingPlan;
    }


    // --- HANDLERS DE NAVEGAÇÃO ---

    @FXML
    private VBox notificationPane;

    @FXML
    public void handleShowDashboard(ActionEvent event) {
        statusLabel.setText("Showing Dashboard");
        loadView("dashboard-view.fxml", null);
    }

    @FXML
    public void handleShowUSEI01(ActionEvent event) {
        statusLabel.setText("Unload Wagons [USEI01]");
        loadView("esinf-usei01-view.fxml", null);
    }

    @FXML
    public void handleShowUSEI02(ActionEvent event) {
        statusLabel.setText("Allocate Orders [USEI02]");
        loadView("esinf-usei02-view.fxml", this.manager);
    }

    // ✅ --- NOVO HANDLER PARA USEI03 ---
    @FXML
    public void handleShowUSEI03(ActionEvent event) {
        statusLabel.setText("Pack Trolleys [USEI03]");
        // A USEI03 precisa do InventoryManager (para o itemsMap)
        loadView("esinf-usei03-view.fxml", this.manager);
    }
    // --- FIM DO NOVO HANDLER ---

    @FXML
    public void handleShowUSEI04(ActionEvent event) {
        statusLabel.setText("Pick Path Sequencing [USEI04]");

        // A LINHA IMPORTANTE É ESTA:
        // Tem de ser 'this', não pode ser 'null'.
        loadView("esinf-usei04-view.fxml", this);
    }
    @FXML
    public void handleShowUSEI05(ActionEvent event) {
        statusLabel.setText("Process Returns [USEI05]");
        loadView("esinf-usei05-view.fxml", null);
    }

    @FXML
    public void handleShowUSEI06(ActionEvent event) {
        statusLabel.setText("European Station Index [USEI06]");
        loadView("esinf-usei06-view.fxml", this.stationIndexManager);
    }

    @FXML
    public void handleShowUSEI07(ActionEvent event) {
        statusLabel.setText("Analyze 2D-Tree [USEI07]");
        // A USEI07 precisa do StationIndexManager (para obter as stats da árvore)
        loadView("esinf-usei07-view.fxml", this.stationIndexManager);
    }

    @FXML
    public void handleShowUSEI08(ActionEvent event) {
        statusLabel.setText("Spatial Queries [USEI08]");
        // A USEI08 precisa da KDTree (para as pesquisas)
        loadView("esinf-usei08-view.fxml", this.spatialKDTree);
    }

    @FXML
    void handleShowUSEI09(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/esinf-usei09-view.fxml"));

            // 1. CORREÇÃO: Mudar o tipo para BorderPane
            BorderPane view = loader.load();

            // 2. Injetar serviços no Controller (manter lógica existente)
            Usei09Controller controller = loader.getController();
            controller.setServices(this, this.spatialKDTree);

            // 3. Mostrar a vista
            centerContentPane.getChildren().setAll(view);

            // 4. ANCORAGEM: Preencher o AnchorPane pai (centerContentPane)
            AnchorPane.setTopAnchor(view, 0.0);
            AnchorPane.setBottomAnchor(view, 0.0);
            AnchorPane.setLeftAnchor(view, 0.0);
            AnchorPane.setRightAnchor(view, 0.0);

        } catch (IOException e) {
            // ... (lidar com erro de carregamento)
        }
    }

    @FXML
    public void handleShowESINF(ActionEvent event) {
        statusLabel.setText("ESINF Feature (Under Construction)");
        centerContentPane.getChildren().clear();

        Label t = new Label("ESINF Screen (Under Construction)");
        t.getStyleClass().add("view-title");
        centerContentPane.getChildren().add(t);
        AnchorPane.setTopAnchor(t, 25.0);
        AnchorPane.setLeftAnchor(t, 25.0);
    }
    @FXML
    public void handleShowSimulation(ActionEvent event) throws IOException {
        if (this.statusLabel != null) {
            this.statusLabel.setText("USLP07 - Simulação Completa e Conflitos");
        }

        FXMLLoader loader = new FXMLLoader(getClass().getResource("/lapr3-simulation-view.fxml"));
        TrainSimulationController controller = new TrainSimulationController();

        // Injetar dependências (Mantido da correção anterior)
        controller.setDependencies(
                this,
                this.facilityRepository,
                this.dispatcherService,
                this.locomotiveRepository
        );

        loader.setController(controller);
        Parent root = loader.load();

        // Inicializar controller (carregar comboios)
        controller.initController();

        // Associar ação do botão manualmente (opcional)
        controller.runButton.setOnAction(e -> controller.runSimulation());

        // <<<<<<<<<<<<<<<<< CORREÇÃO CRÍTICA DE NAVEGAÇÃO >>>>>>>>>>>>>>>>>>>
        // 1. Limpar o conteúdo anterior do AnchorPane central
        //    Isto garante que a vista USLP07 substitui qualquer outra vista.
        this.centerContentPane.getChildren().clear();

        // 2. Adicionar a nova vista ao AnchorPane central
        this.centerContentPane.getChildren().add(root);

        // 3. Ancorar o root para preencher o centerContentPane (como o loadView faz)
        AnchorPane.setTopAnchor(root, 0.0);
        AnchorPane.setBottomAnchor(root, 0.0);
        AnchorPane.setLeftAnchor(root, 0.0);
        AnchorPane.setRightAnchor(root, 0.0);

        // O 'mainPane.setCenter()' é AGORA removido.
    }

    public void loadTrainCrudView() {
        statusLabel.setText("BDDAD Features: Train CRUD");
        centerContentPane.getChildren().clear();

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/train-crud-view.fxml"));

            // 1. CARREGA O FXML. O FXMLLoader INSTANCIA AGORA O CONTROLLER (Devido ao fx:controller no FXML).
            Parent root = loader.load();

            // 2. RECUPERA A INSTÂNCIA QUE FOI CRIADA
            TrainCRUDController controller = loader.getController();

            // 3. INJETA DEPENDÊNCIAS (CORRIGIDO: PASSANDO OS 5 ARGUMENTOS)
            if (controller != null) {
                controller.setDependencies(
                        this,
                        this.trainRepository,
                        this.facilityRepository, // <--- ARGUMENTO EM FALTA
                        this.locomotiveRepository, // <--- ARGUMENTO EM FALTA
                        this.networkService // <--- ARGUMENTO EM FALTA
                );
                // 4. Inicializa o carregamento dos dados
                controller.initController();
            } else {
                // Isto não deve acontecer se o fx:controller estiver no FXML
                throw new IllegalStateException("FXMLLoader failed to get the controller instance.");
            }

            // 5. Adicionar e ancorar a nova vista
            centerContentPane.getChildren().add(root);
            AnchorPane.setTopAnchor(root, 0.0);
            AnchorPane.setLeftAnchor(root, 0.0);
            AnchorPane.setRightAnchor(root, 0.0);
            AnchorPane.setBottomAnchor(root, 0.0);

        } catch (Exception e) {
            statusLabel.setText("❌ Error loading Train CRUD view.");
            System.err.println("Error loading FXML for Train CRUD: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    public void handleShowBDADQueries(ActionEvent event) {
        statusLabel.setText("BDDAD Queries - User Stories");

        try {
            URL fxmlUrl = getClass().getClassLoader().getResource("usbd-queries-view.fxml");
            if (fxmlUrl == null) {
                this.showNotification("Error: Could not find FXML for BDDAD Queries.", "error");
                return;
            }

            FXMLLoader loader = new FXMLLoader(fxmlUrl);

            // 1. Instanciar o controller
            BDADQueriesController controller = new BDADQueriesController();
            controller.setMainController(this); // Injetar dependência

            loader.setController(controller);
            Parent root = loader.load();

            // 2. Substituir o conteúdo central (usando o padrão corrigido)
            this.centerContentPane.getChildren().clear();
            this.centerContentPane.getChildren().add(root);

            // 3. Ancorar o root
            AnchorPane.setTopAnchor(root, 0.0);
            AnchorPane.setBottomAnchor(root, 0.0);
            AnchorPane.setLeftAnchor(root, 0.0);
            AnchorPane.setRightAnchor(root, 0.0);

        } catch (IOException e) {
            this.showNotification("Failed to load BDDAD Queries view: " + e.getMessage(), "error");
            e.printStackTrace();
        }
    }


    /**
     * Tratador do menu principal BDDAD. Carrega o menu de escolha de entidades.
     */
    @FXML
    public void handleShowBDDAD(ActionEvent event) {
        statusLabel.setText("BDDAD Features Menu");
        // Carrega o novo menu de CRUDs
        loadView("bdad-main-view.fxml", this);
    }

    /**
     * Carrega o ecrã CRUD para a entidade Operator. Chamado pelo BDDADMainController.
     */
    public void loadOperatorCrudView() {
        statusLabel.setText("BDDAD Features: Operator CRUD");
        // Limpa o painel central antes de carregar a nova vista
        centerContentPane.getChildren().clear();

        try {
            // 1. Carrega o FXML
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/database-crud-view.fxml"));
            Parent root = loader.load();

            // 2. Obtém o Controller e injeta o serviço
            DatabaseCRUDController controller = loader.getController();
            // Assume que 'this' é o MainController, que é o que o DatabaseCRUDController espera.
            controller.setServices(this);

            // 3. Adiciona e ancora a nova vista para preencher o painel central
            centerContentPane.getChildren().add(root);
            AnchorPane.setTopAnchor(root, 0.0);
            AnchorPane.setLeftAnchor(root, 0.0);
            AnchorPane.setRightAnchor(root, 0.0);
            AnchorPane.setBottomAnchor(root, 0.0);

        } catch (Exception e) {
            statusLabel.setText("❌ Error loading BDDAD CRUD view.");
            System.err.println("Error loading FXML for BDDAD CRUD: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Carrega o ecrã CRUD para a entidade Locomotive. Chamado pelo BDDADMainController.
     */
    public void loadLocomotiveCrudView() {
        statusLabel.setText("BDDAD Features: Locomotive CRUD");
        centerContentPane.getChildren().clear();

        try {
            // 1. Carrega o FXML
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/locomotive-crud-view.fxml"));
            Parent root = loader.load();

            // 2. Obtém o Controller e injeta o serviço
            LocomotiveCRUDController controller = loader.getController();
            // IMPORTANTE: O nome do controller é 'LocomotiveCRUDController'
            controller.setServices(this);

            // 3. Adiciona e ancora a nova vista
            centerContentPane.getChildren().add(root);
            AnchorPane.setTopAnchor(root, 0.0);
            AnchorPane.setLeftAnchor(root, 0.0);
            AnchorPane.setRightAnchor(root, 0.0);
            AnchorPane.setBottomAnchor(root, 0.0);

        } catch (Exception e) {
            statusLabel.setText("❌ Error loading Locomotive CRUD view.");
            System.err.println("Error loading FXML for Locomotive CRUD: " + e.getMessage());
            e.printStackTrace();
        }
    }


    /**
     * Mostra uma notificação pop-up no canto superior direito.
     * Esta função pode ser chamada por qualquer controlador filho.
     *
     * @param message A mensagem a mostrar.
     * @param type "success" (verde) ou "error" (vermelho).
     */
    public void showNotification(String message, String type) {
        // 1. Criar o Label da notificação
        Label notificationLabel = new Label(message);
        notificationLabel.getStyleClass().add("notification-label");

        // 2. Definir o estilo (success ou error)
        if ("success".equals(type)) {
            notificationLabel.getStyleClass().add("notification-success");
        } else if ("error".equals(type)) {
            notificationLabel.getStyleClass().add("notification-error");
        } else {
            notificationLabel.getStyleClass().add("notification-info");
        }

        // 3. Adicionar animação de Fade-In (aparecer suavemente)
        FadeTransition fadeIn = new FadeTransition(Duration.millis(300), notificationLabel);
        fadeIn.setFromValue(0.0);
        fadeIn.setToValue(1.0);

        // 4. Criar a pausa (quanto tempo fica no ecrã)
        PauseTransition delay = new PauseTransition(Duration.seconds(4));

        // 5. Criar animação de Fade-Out (desaparecer suavemente)
        FadeTransition fadeOut = new FadeTransition(Duration.millis(500), notificationLabel);
        fadeOut.setFromValue(1.0);
        fadeOut.setToValue(0.0);

        // 6. Definir o que acontece quando o fade-out termina
        fadeOut.setOnFinished(e -> notificationPane.getChildren().remove(notificationLabel));

        // 7. Ligar tudo:
        fadeIn.setOnFinished(e -> {
            delay.play();
        });
        delay.setOnFinished(e -> {
            fadeOut.play();
        });

        // 8. Adicionar o label ao ecrã e começar a animação
        notificationPane.getChildren().add(notificationLabel);
        fadeIn.play();
    }

    public void loadWagonCrudView() {
        statusLabel.setText("BDDAD Features: Wagon CRUD");
        centerContentPane.getChildren().clear();

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/wagon-crud-view.fxml"));
            Parent root = loader.load();

            WagonCRUDController controller = loader.getController();
            controller.setServices(this);

            centerContentPane.getChildren().add(root);
            AnchorPane.setTopAnchor(root, 0.0);
            AnchorPane.setLeftAnchor(root, 0.0);
            AnchorPane.setRightAnchor(root, 0.0);
            AnchorPane.setBottomAnchor(root, 0.0);

        } catch (Exception e) {
            statusLabel.setText("❌ Error loading Wagon CRUD view.");
            System.err.println("Error loading FXML for Wagon CRUD: " + e.getMessage());
            e.printStackTrace();
        }
    }

}
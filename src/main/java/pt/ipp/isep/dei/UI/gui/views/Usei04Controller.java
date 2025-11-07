package pt.ipp.isep.dei.UI.gui.views;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import pt.ipp.isep.dei.domain.*; // Importa o teu pacote de domínio
import pt.ipp.isep.dei.domain.PickingPathService.PathResult; // Importa a classe interna

import java.util.Map;

/**
 * Controlador JavaFX para a USEI04 - Pick Path Sequencing.
 */
public class Usei04Controller {

    @FXML
    private Button calculatePathButton;

    @FXML
    private TextArea strategyAResultArea;

    @FXML
    private TextArea strategyBResultArea;

    private PickingPathService pickingPathService;

    /**
     * Chamado automaticamente quando o FXML é carregado.
     * Funciona como o construtor para o controller FXML.
     */
    @FXML
    public void initialize() {
        // Instancia o serviço que contém a lógica de negócio
        this.pickingPathService = new PickingPathService();

        // Limpa as áreas de texto ao iniciar
        strategyAResultArea.setText("Clique no botão para calcular a rota...");
        strategyBResultArea.setText("Clique no botão para calcular a rota...");
    }

    /**
     * Chamado quando o botão "calculatePathButton" é clicado.
     * (definido no FXML: onAction="#handleCalculatePath")
     */
    @FXML
    private void handleCalculatePath() {
        // Limpa resultados anteriores
        strategyAResultArea.setText("A calcular...");
        strategyBResultArea.setText("A calcular...");

        // --- DADOS MOCK (Simulados) ---
        // Na vida real, este plano viria de outro local (ex: USEI03)
        // Estamos a usar os dados do Cenário 07 dos teus testes
        PickingPlan mockPlan = createMockPickingPlan();

        // 1. Chamar o Serviço de Negócio
        Map<String, PathResult> results = pickingPathService.calculatePickingPaths(mockPlan);

        // 2. Obter os resultados
        PathResult resultA = results.get("Strategy A (Deterministic Sweep)");
        PathResult resultB = results.get("Strategy B (Nearest Neighbour)");

        // 3. Mostrar os resultados nas TextAreas
        // Usamos o .toString() "bonito" que já tinhas no PathResult!
        strategyAResultArea.setText(resultA.toString());
        strategyBResultArea.setText(resultB.toString());
    }

    /**
     * Helper method para criar um PickingPlan de teste.
     * Baseado no Cenário 07 do teu USEI04test.java.
     * @return Um PickingPlan simulado.
     */
    private PickingPlan createMockPickingPlan() {
        PickingPlan plan = new PickingPlan("PLAN_MOCK_UI", HeuristicType.FIRST_FIT, 0);
        Trolley t1 = new Trolley("T1_MOCK_UI", 100);

        // Criar Assignments (Pontos: (1,10) e (2,1))
        t1.addAssignment(createMockAssignment("ORD6", 1, "SKU_A", "1", "10")); // (1,10)
        t1.addAssignment(createMockAssignment("ORD6", 2, "SKU_B", "2", "1"));  // (2,1)

        plan.addTrolley(t1);
        return plan;
    }

    /**
     * Helper method para criar um PickingAssignment de teste.
     * @return Um PickingAssignment simulado.
     */
    private PickingAssignment createMockAssignment(String orderId, int lineNo, String sku, String aisle, String bay) {
        // Criamos um Item mock, pois é necessário pelo construtor do PickingAssignment
        Item mockItem = new Item(sku, "Mock Item " + sku, "Mock Category", "unit", 1.0);

        // O construtor do PickingAssignment que usaste nos testes
        return new PickingAssignment(orderId, lineNo, mockItem, 1, "BOX_MOCK", aisle, bay);
    }
}
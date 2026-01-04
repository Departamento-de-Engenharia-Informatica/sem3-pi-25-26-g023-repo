package pt.ipp.isep.dei.UI.gui.views;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.util.StringConverter;
import pt.ipp.isep.dei.UI.gui.MainController;
import pt.ipp.isep.dei.domain.LineSegment;
import pt.ipp.isep.dei.domain.RailwayNetworkService;
import pt.ipp.isep.dei.domain.Station;
import pt.ipp.isep.dei.repository.FacilityRepository;
import pt.ipp.isep.dei.repository.SegmentLineRepository;

import java.util.*;
import java.util.stream.Collectors;

public class Usei11Controller {

    @FXML private ComboBox<Station> stationCombo;
    @FXML private TextArea resultArea;
    @FXML private Label statusLabel;

    private RailwayNetworkService networkService;
    private MainController mainController;

    // Repositórios necessários para análise direta
    private FacilityRepository facilityRepo;
    private SegmentLineRepository segmentRepo;

    public void setDependencies(MainController mainController, RailwayNetworkService networkService) {
        this.mainController = mainController;
        this.networkService = networkService;

        // Inicializa repositórios para aceder aos dados brutos
        this.facilityRepo = new FacilityRepository();
        this.segmentRepo = new SegmentLineRepository();

        loadStations();
    }

    @FXML
    public void initialize() {
        // Nada a fazer aqui, espera por setDependencies
    }

    private void loadStations() {
        List<Station> stations = new ArrayList<>();
        Map<Integer, String> facilities = facilityRepo.findAllFacilityNames();

        // Carrega todas as estações para a ComboBox
        for (Map.Entry<Integer, String> entry : facilities.entrySet()) {
            stations.add(new Station(entry.getKey(), entry.getValue(), 0, 0, 0, 0));
        }

        // Ordenar por nome
        stations.sort(Comparator.comparing(Station::nome));

        stationCombo.setItems(FXCollections.observableArrayList(stations));
        stationCombo.setConverter(new StringConverter<>() {
            @Override public String toString(Station s) { return s == null ? "" : s.nome(); }
            @Override public Station fromString(String string) { return null; }
        });
    }

    @FXML
    public void handleAnalyzeNetwork() {
        Station selected = stationCombo.getValue();
        resultArea.clear();
        statusLabel.setText("A processar topologia da rede...");

        try {
            // 1. Calcular Componentes Conexas (Ilhas de estações interligadas)
            List<Set<Integer>> connectedComponents = calculateConnectedComponents();

            StringBuilder sb = new StringBuilder();
            sb.append("================================================\n");
            sb.append("   USEI11 - RELATÓRIO DE CONETIVIDADE DA REDE   \n");
            sb.append("================================================\n\n");

            // ANÁLISE ESPECÍFICA (Se uma estação foi escolhida)
            if (selected != null) {
                analyzeSpecificStation(selected, connectedComponents, sb);
            }
            // ANÁLISE GLOBAL (Se nenhuma estação foi escolhida)
            else {
                analyzeGlobalNetwork(connectedComponents, sb);
            }

            resultArea.setText(sb.toString());
            statusLabel.setText("Análise concluída com sucesso.");

        } catch (Exception e) {
            resultArea.setText("Erro crítico na análise: " + e.getMessage());
            statusLabel.setText("Erro.");
            e.printStackTrace();
        }
    }

    // --- LÓGICA DE APRESENTAÇÃO ---

    private void analyzeSpecificStation(Station selected, List<Set<Integer>> components, StringBuilder sb) {
        sb.append("📍 ANÁLISE LOCAL: ").append(selected.nome()).append(" (ID: ").append(selected.idEstacao()).append(")\n");
        sb.append("------------------------------------------------\n");

        // Encontrar o grupo a que a estação pertence
        Set<Integer> myGroup = null;
        for (Set<Integer> group : components) {
            if (group.contains(selected.idEstacao())) {
                myGroup = group;
                break;
            }
        }

        if (myGroup == null) {
            // Caso raro: a estação existe na BD Facilities mas não tem segmentos ligados
            sb.append("⚠️ AVISO: Esta estação está ISOLADA (sem carris ligados).\n");
            sb.append("   Tamanho do subgrafo: 1 (apenas ela própria).\n");
        } else {
            sb.append("✅ Topologia: A estação está integrada na rede.\n");
            sb.append("📊 Dimensão do Subgrafo (Cluster): ").append(myGroup.size()).append(" estações interligadas.\n");
            sb.append("\n🌍 Estações alcançáveis a partir de ").append(selected.nome()).append(":\n");

            List<String> names = getNamesForIds(myGroup);
            // Formatar lista bonita
            sb.append(formatList(names));
        }
    }

    private void analyzeGlobalNetwork(List<Set<Integer>> components, StringBuilder sb) {
        sb.append("🌍 ANÁLISE GLOBAL DA REDE FERROVIÁRIA\n");
        sb.append("------------------------------------------------\n");

        int numComponents = components.size();
        int totalStations = components.stream().mapToInt(Set::size).sum();

        sb.append("• Total de Estações com Linhas: ").append(totalStations).append("\n");
        sb.append("• Número de Partições (Ilhas): ").append(numComponents).append("\n");

        boolean isConnected = (numComponents == 1);
        sb.append("• Estado da Rede: ").append(isConnected ? "✅ TOTALMENTE CONEXA" : "❌ DESCONEXA (Fragmentada)").append("\n\n");

        sb.append("DETALHE DAS PARTIÇÕES:\n");

        int counter = 1;
        // Ordenar componentes por tamanho (maior primeiro)
        components.sort((a, b) -> b.size() - a.size());

        for (Set<Integer> group : components) {
            sb.append(String.format("\n🔹 GRUPO #%d (Tamanho: %d estações)\n", counter++, group.size()));
            List<String> names = getNamesForIds(group);
            sb.append(formatList(names));
        }
    }

    // --- ALGORITMOS DE GRAFOS (Core Logic) ---

    /**
     * Algoritmo principal: Descobre todas as componentes conexas do grafo.
     * Não depende do serviço, lê diretamente os segmentos da BD.
     */
    private List<Set<Integer>> calculateConnectedComponents() {
        // 1. Obter todos os segmentos
        List<LineSegment> segments = segmentRepo.findAll();

        // 2. Construir Grafo (Lista de Adjacências)
        Map<Integer, List<Integer>> adj = new HashMap<>();
        Set<Integer> allNodes = new HashSet<>();

        for (LineSegment s : segments) {
            int u = s.getIdEstacaoInicio();
            int v = s.getIdEstacaoFim();

            allNodes.add(u);
            allNodes.add(v);

            adj.computeIfAbsent(u, k -> new ArrayList<>()).add(v);
            adj.computeIfAbsent(v, k -> new ArrayList<>()).add(u); // Bidirecional
        }

        // 3. BFS para encontrar componentes
        List<Set<Integer>> components = new ArrayList<>();
        Set<Integer> visited = new HashSet<>();

        for (Integer node : allNodes) {
            if (!visited.contains(node)) {
                // Nova componente encontrada
                Set<Integer> component = new HashSet<>();
                bfs(node, adj, visited, component);
                components.add(component);
            }
        }

        return components;
    }

    private void bfs(int startNode, Map<Integer, List<Integer>> adj, Set<Integer> visited, Set<Integer> component) {
        Queue<Integer> queue = new LinkedList<>();
        queue.add(startNode);
        visited.add(startNode);
        component.add(startNode);

        while (!queue.isEmpty()) {
            int u = queue.poll();
            for (int v : adj.getOrDefault(u, Collections.emptyList())) {
                if (!visited.contains(v)) {
                    visited.add(v);
                    component.add(v);
                    queue.add(v);
                }
            }
        }
    }

    // --- UTILITÁRIOS ---

    private List<String> getNamesForIds(Set<Integer> ids) {
        List<String> names = new ArrayList<>();
        for (Integer id : ids) {
            names.add(facilityRepo.findNameById(id).orElse("ID:" + id));
        }
        Collections.sort(names);
        return names;
    }

    private String formatList(List<String> items) {
        StringBuilder sb = new StringBuilder();
        int col = 0;
        for (String item : items) {
            sb.append(String.format("  • %-25s", item)); // Alinhamento em colunas
            col++;
            if (col >= 2) { // 2 colunas por linha
                sb.append("\n");
                col = 0;
            }
        }
        if (col != 0) sb.append("\n");
        return sb.toString();
    }

    @FXML
    public void handleClear() {
        stationCombo.getSelectionModel().clearSelection();
        resultArea.clear();
        statusLabel.setText("Ready.");
    }
}
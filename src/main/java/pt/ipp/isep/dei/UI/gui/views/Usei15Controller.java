package pt.ipp.isep.dei.UI.gui.views;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import pt.ipp.isep.dei.UI.gui.MainController;
import pt.ipp.isep.dei.domain.*;
import java.util.*;
import java.util.stream.Collectors;

public class Usei15Controller {
    @FXML private ComboBox<Station> cmbOrigin;
    @FXML private ComboBox<Station> cmbDest;
    @FXML private TextArea txtResultArea;

    private MainController mainController;
    private Graph currentGraph;

    @FXML
    public void initialize() {
        setupConverters();
        loadInitialData();

        cmbOrigin.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                filterDestinations(newVal);
            } else {
                cmbDest.getItems().clear();
            }
        });
    }

    public void setDependencies(MainController mc) {
        this.mainController = mc;
    }

    private void setupConverters() {
        javafx.util.StringConverter<Station> converter = new javafx.util.StringConverter<>() {
            @Override public String toString(Station s) {
                return (s == null) ? "" : s.nome() + " [" + s.idEstacao() + "]";
            }
            @Override public Station fromString(String string) { return null; }
        };
        cmbOrigin.setConverter(converter);
        cmbDest.setConverter(converter);
    }

    private void loadInitialData() {
        try {
            String stFile = "src/main/java/pt/ipp/isep/dei/FicheirosCSV/stations.csv";
            String lnFile = "src/main/java/pt/ipp/isep/dei/FicheirosCSV/lines.csv";
            this.currentGraph = CSVLoader.load(stFile, lnFile);

            List<Station> stations = currentGraph.metricsMap.values().stream()
                    .map(StationMetrics::getStation)
                    .collect(Collectors.toList());

            cmbOrigin.getItems().setAll(stations);
        } catch (Exception e) {
            System.err.println("Erro ao carregar dados: " + e.getMessage());
        }
    }

    private void filterDestinations(Station origin) {
        if (currentGraph == null) return;

        Set<Integer> reachableIds = findAllReachable(origin.idEstacao());

        List<Station> reachableStations = currentGraph.metricsMap.values().stream()
                .map(StationMetrics::getStation)
                .filter(s -> s.idEstacao() != origin.idEstacao())
                .filter(s -> reachableIds.contains(s.idEstacao()))
                .collect(Collectors.toList());

        cmbDest.getItems().setAll(reachableStations);
    }

    private Set<Integer> findAllReachable(int startId) {
        Set<Integer> visited = new HashSet<>();
        Queue<Integer> queue = new LinkedList<>();
        queue.add(startId);
        visited.add(startId);

        while (!queue.isEmpty()) {
            int u = queue.poll();
            List<Edge> neighbors = currentGraph.adj.get(u);
            if (neighbors != null) {
                for (Edge e : neighbors) {
                    if (!visited.contains(e.to())) {
                        visited.add(e.to());
                        queue.add(e.to());
                    }
                }
            }
        }
        return visited;
    }

    @FXML
    public void handleRunBellmanFord() {
        Station origin = cmbOrigin.getValue();
        Station dest = cmbDest.getValue();

        if (origin == null || dest == null) {
            if (mainController != null) mainController.showNotification("Selecione origem e destino.", "error");
            return;
        }

        try {
            BellmanFord.PathResult result = BellmanFord.findPath(currentGraph, origin.idEstacao(), dest.idEstacao());

            if (result.cycle() != null) {
                txtResultArea.setText("⚠️ CICLO NEGATIVO DETETADO!\nEstações no ciclo: " + result.cycle());
                if (mainController != null) mainController.showNotification("Ciclo negativo!", "error");
            }
            else if (result.path() != null && !result.path().isEmpty()) {
                // Lógica de formatação detalhada adicionada aqui
                String detailedReport = formatDetailedPath(result.path(), result.totalCost());

                txtResultArea.setText(detailedReport);

                if (mainController != null) mainController.showNotification("Caminho calculado!", "success");
            }
        } catch (Exception e) {
            txtResultArea.setText("Erro na análise: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Gera um relatório detalhado com os custos entre cada estação.
     */
    private String formatDetailedPath(List<Integer> path, double totalCost) {
        StringBuilder sb = new StringBuilder();
        sb.append("=== Caminho de Risco Mínimo (Bellman-Ford) ===\n\n");

        double accumulatedCost = 0.0;

        for (int i = 0; i < path.size() - 1; i++) {
            int u = path.get(i);
            int v = path.get(i + 1);

            // Obtém nomes das estações
            String startName = getStationName(u);
            String endName = getStationName(v);

            // Obtém o custo da aresta específica
            double segmentCost = getEdgeWeight(u, v);
            accumulatedCost += segmentCost;

            sb.append(String.format("%d. [%s] -> [%s]\n", (i + 1), startName, endName));
            sb.append(String.format("    Risco Segmento: %.4f | Acumulado: %.4f\n", segmentCost, accumulatedCost));
        }

        sb.append("\n==============================================\n");
        sb.append("Caminho (IDs): " + path + "\n");
        sb.append(String.format("Custo Total Final: %.4f", totalCost));

        return sb.toString();
    }

    /**
     * Auxiliar para buscar o peso da aresta entre dois nós.
     */
    private double getEdgeWeight(int u, int v) {
        if (currentGraph != null && currentGraph.adj.containsKey(u)) {
            for (Edge e : currentGraph.adj.get(u)) {
                if (e.to() == v) {
                    return e.weight();
                }
            }
        }
        return 0.0;
    }

    /**
     * Auxiliar para buscar o nome da estação pelo ID.
     */
    private String getStationName(int id) {
        if (currentGraph != null && currentGraph.metricsMap.containsKey(id)) {
            return currentGraph.metricsMap.get(id).getStation().nome();
        }
        return "ID:" + id;
    }
}
package pt.ipp.isep.dei.UI.gui.views;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import pt.ipp.isep.dei.UI.gui.MainController;
import pt.ipp.isep.dei.domain.*;

import java.util.List;
import java.util.Map;

public class Usei10Controller {

    private MainController mainController;
    private RadiusSearch radiusSearchService;

    // Lista para armazenar todos os resultados da BST (já ordenados)
    private final ObservableList<StationDistance> stationData = FXCollections.observableArrayList();

    @FXML private TextField txtLat;
    @FXML private TextField txtLon;
    @FXML private TextField txtRadius;
    @FXML private Label lblStatus;
    @FXML private Label lblTotalStations;
    @FXML private VBox vboxCountrySummary; // Para barras de progresso
    @FXML private GridPane gridTypeSummary; // Para resumos City/Main
    @FXML private Pagination paginationStations;

    private static final int ITEMS_PER_PAGE = 20;

    // Campos para guardar as definições das colunas (CRUCIAL para a correção do bug de paginação)
    private List<TableColumn<StationDistance, ?>> columnDefinitions;

    /**
     * Injeção de dependências pelo MainController.
     */
    public void setServices(MainController mainController, StationIndexManager stationIndexManager) {
        this.mainController = mainController;
        this.radiusSearchService = stationIndexManager.getRadiusSearchEngine();

        if (this.radiusSearchService == null) {
            lblStatus.setText("❌ Error: Radius Search service not initialized.");
        }
    }

    @FXML
    public void initialize() {
        // 1. Definição das colunas ANTES do PageFactory
        setupTableViewColumns();

        // 2. Setup do PageFactory
        paginationStations.setPageFactory(this::createPage);

        lblStatus.setText("Enter coordinates and radius to start search.");
        lblTotalStations.setText("Total Stations: N/A");
        paginationStations.setDisable(true);
        vboxCountrySummary.getChildren().add(new Label("Run search to see country summary."));
    }

    /**
     * Define as colunas do TableView apenas uma vez (corrigindo o problema de performance e binding).
     */
    private void setupTableViewColumns() {
        // 1. Coluna Rank
        TableColumn<StationDistance, String> rankCol = new TableColumn<>("#");
        // O binding aqui usa o índice global da lista 'stationData'
        rankCol.setCellValueFactory(cellData -> {
            int index = stationData.indexOf(cellData.getValue());
            // A paginação usa uma sublista, mas o 'stationData' é a lista global
            return new javafx.beans.property.SimpleStringProperty(String.valueOf(index >= 0 ? index + 1 : ""));
        });
        rankCol.setPrefWidth(30); rankCol.setMinWidth(30); rankCol.setMaxWidth(30);
        rankCol.setSortable(false);

        // 2. Coluna Station Name
        TableColumn<StationDistance, String> nameCol = new TableColumn<>("Station Name");
        nameCol.setCellValueFactory(cellData ->
                new javafx.beans.property.SimpleStringProperty(cellData.getValue().getStation().getStation()) // Usa getStation()
        );

        // 3. Coluna Country
        TableColumn<StationDistance, String> countryCol = new TableColumn<>("Country");
        countryCol.setCellValueFactory(cellData ->
                new javafx.beans.property.SimpleStringProperty(cellData.getValue().getStation().getCountry())
        );
        countryCol.setPrefWidth(70);
        countryCol.setMinWidth(50);

        // 4. Coluna Distance
        TableColumn<StationDistance, Double> distCol = new TableColumn<>("Distance (km)");
        distCol.setCellValueFactory(new PropertyValueFactory<>("distanceKm")); // Usa distanceKm
        distCol.setCellFactory(column -> new TableCell<StationDistance, Double>() {
            @Override
            protected void updateItem(Double item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(String.format("%.2f", item));
                }
            }
        });
        distCol.setPrefWidth(120);

        // 5. Coluna Details (Icons: City/Main Station)
        TableColumn<StationDistance, String> detailsCol = new TableColumn<>("Details");
        detailsCol.setCellValueFactory(cellData -> {
            EuropeanStation s = cellData.getValue().getStation();
            String details = "";
            if (s.isCity()) details += "🏙️ ";
            if (s.isMainStation()) details += "⭐";
            return new javafx.beans.property.SimpleStringProperty(details.trim());
        });
        detailsCol.setPrefWidth(70);
        detailsCol.setMinWidth(50);
        detailsCol.setMaxWidth(70);

        // Armazena as colunas para serem reutilizadas na paginação
        this.columnDefinitions = List.of(rankCol, nameCol, countryCol, distCol, detailsCol);
    }

    /**
     * Executa a busca por raio e atualiza a UI.
     */
    @FXML
    private void handleSearch() {
        stationData.clear();
        vboxCountrySummary.getChildren().clear();
        gridTypeSummary.getChildren().clear();
        paginationStations.setDisable(true);

        if (this.radiusSearchService == null) {
            lblStatus.setText("❌ Error: Radius Search service is not available.");
            return;
        }

        try {
            double lat = Double.parseDouble(txtLat.getText());
            double lon = Double.parseDouble(txtLon.getText());
            double radius = Double.parseDouble(txtRadius.getText());

            if (radius <= 0.0 || radius > 1000.0) {
                lblStatus.setText("❌ Error: Radius must be between 0.1 and 1000.0 km.");
                return;
            }

            lblStatus.setText("⏳ Executing radius search...");

            long startTime = System.nanoTime();

            Object[] results = radiusSearchService.radiusSearchWithSummary(lat, lon, radius);

            long endTime = System.nanoTime();
            double executionTimeMs = (endTime - startTime) / 1_000_000.0;

            BST<StationDistance, StationDistance> resultTree = (BST<StationDistance, StationDistance>) results[0];
            stationData.addAll(resultTree.inOrderTraversal());

            DensitySummary summary = (DensitySummary) results[1];
            populateSummary(summary);

            int totalStations = stationData.size();
            int pageCount = (int) Math.ceil((double) totalStations / ITEMS_PER_PAGE);

            paginationStations.setPageCount(Math.max(1, pageCount));
            paginationStations.setCurrentPageIndex(0);
            paginationStations.setDisable(totalStations == 0);

            if (totalStations == 0) {
                lblStatus.setText(String.format("✅ Search completed (%.2f ms). Total stations: 0. No stations found.", executionTimeMs));
            } else {
                lblStatus.setText(String.format("✅ Search completed (%.2f ms). Total stations: %d. Showing nearest stations.", executionTimeMs, totalStations));
            }
            mainController.showNotification(String.format("USEI10: Found %d stations in %.2f ms.", totalStations, executionTimeMs), "success");

        } catch (NumberFormatException e) {
            lblStatus.setText("❌ Error: Invalid number format for input fields.");
            mainController.showNotification("Invalid input format.", "error");
        } catch (Exception e) {
            lblStatus.setText("❌ An unexpected error occurred: " + e.getMessage());
            mainController.showNotification("An unexpected error occurred.", "error");
            e.printStackTrace();
        }
    }

    /**
     * Popula a UI com os dados do DensitySummary (Lógica de dashboard).
     */
    private void populateSummary(DensitySummary summary) {
        lblTotalStations.setText("Total Stations: " + summary.getTotalStations());

        if (summary.getTotalStations() == 0) {
            vboxCountrySummary.getChildren().add(new Label("No data in radius."));
            return;
        }

        // --- Popula Sumário por País com ProgressBar ---
        vboxCountrySummary.getChildren().clear();
        double total = (double) summary.getTotalStations();

        summary.getStationsByCountry().forEach((country, count) -> {
            double percentage = (count / total) * 100;

            // HBox: Label + ProgressBar
            HBox countryEntry = new HBox(10);
            HBox.setHgrow(countryEntry, Priority.ALWAYS); // Deixa o HBox crescer

            ProgressBar pb = new ProgressBar(percentage / 100.0);
            pb.setPrefWidth(150);
            pb.setMaxWidth(Double.MAX_VALUE); // Permite que a barra cresça
            pb.getStyleClass().add("country-progress-bar");
            HBox.setHgrow(pb, Priority.ALWAYS); // Faz a barra preencher o espaço disponível

            Label countryLabel = new Label(String.format("%s: %d (%.1f%%)", country, count, percentage));
            countryLabel.setPrefWidth(120);
            countryLabel.setMinWidth(120);

            countryEntry.getChildren().addAll(countryLabel, pb);
            vboxCountrySummary.getChildren().add(countryEntry);
        });

        // --- Popula Sumário por Tipo (Grid) ---
        gridTypeSummary.getChildren().clear();
        populateTypeGrid(summary.getStationsByCityType(), "City", gridTypeSummary, total, 0);
        populateTypeGrid(summary.getStationsByMainStation(), "Main Station", gridTypeSummary, total, 2);
    }

    /**
     * Helper para popular uma secção de resumo de tipo (City/MainStation) no GridPane.
     */
    private void populateTypeGrid(Map<Boolean, Integer> counts, String typeName, GridPane grid, double total, int rowStart) {
        int countTrue = counts.getOrDefault(true, 0);
        int countFalse = counts.getOrDefault(false, 0);

        // True
        double percentTrue = (countTrue / total) * 100;
        Label trueLabel = new Label("Is " + typeName + ":");
        Label trueValue = new Label(String.format("%d (%.1f%%)", countTrue, percentTrue));
        trueValue.getStyleClass().add("summary-value-label");

        grid.add(trueLabel, 0, rowStart);
        grid.add(trueValue, 1, rowStart);

        // False
        double percentFalse = (countFalse / total) * 100;
        Label falseLabel = new Label("Is NOT " + typeName + ":");
        Label falseValue = new Label(String.format("%d (%.1f%%)", countFalse, percentFalse));
        falseValue.getStyleClass().add("summary-value-label");

        grid.add(falseLabel, 0, rowStart + 1);
        grid.add(falseValue, 1, rowStart + 1);
    }

    /**
     * ✅ MÉTODO CORRIGIDO: Cria o conteúdo de cada página usando uma TableView.
     * Isto corrige o bug de visualização e paginação.
     */
    private VBox createPage(int pageIndex) {
        if (stationData.isEmpty() || columnDefinitions == null) {
            return new VBox(new Label("No stations found within the radius."));
        }

        int fromIndex = pageIndex * ITEMS_PER_PAGE;
        int toIndex = Math.min(fromIndex + ITEMS_PER_PAGE, stationData.size());

        // Usa a sublista para a página atual
        ObservableList<StationDistance> sublist = FXCollections.observableArrayList(stationData.subList(fromIndex, toIndex));

        TableView<StationDistance> tableView = new TableView<>();
        tableView.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        tableView.getStyleClass().add("results-table");

        // Adiciona as colunas previamente definidas
        tableView.getColumns().addAll(columnDefinitions);
        tableView.setItems(sublist);

        tableView.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);

        // CRÍTICO: Faz o TableView crescer para preencher o espaço do VBox, corrigindo o problema de layout/tamanho.
        VBox page = new VBox(tableView);
        VBox.setVgrow(tableView, Priority.ALWAYS);

        return page;
    }
}
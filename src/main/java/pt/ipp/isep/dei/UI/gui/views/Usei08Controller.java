package pt.ipp.isep.dei.UI.gui.views;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import pt.ipp.isep.dei.UI.gui.MainController;
import pt.ipp.isep.dei.domain.EuropeanStation;
import pt.ipp.isep.dei.domain.KDTree;
import pt.ipp.isep.dei.domain.SpatialSearch;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class Usei08Controller {

    // --- FXML existentes ---
    @FXML
    private TextField txtLatMin, txtLatMax, txtLonMin, txtLonMax, txtCountry;
    @FXML
    private ComboBox<String> cmbIsCity, cmbIsMain;
    @FXML
    private Button btnSearch;
    @FXML
    private Label lblStatus;
    @FXML
    private TextArea txtResult;

    // --- NOVOS FXML para Paginação ---
    // Assumir que estes foram adicionados ao FXML
    @FXML
    private Button btnPrev, btnNext;
    @FXML
    private Label lblPaginationInfo;

    // --- Variáveis de Serviço e Estado ---
    private MainController mainController;
    private KDTree spatialKDTree;
    private SpatialSearch spatialSearchEngine;

    // NOVO: Estado para a Paginação (Lazy Load)
    private List<EuropeanStation> allResults;
    private int currentPage = 0;
    private static final int ITEMS_PER_PAGE = 50; // Apenas 50 itens por página

    public void setServices(MainController mainController, KDTree spatialKDTree) {
        this.mainController = mainController;
        this.spatialKDTree = spatialKDTree;
        this.spatialSearchEngine = new SpatialSearch(spatialKDTree);
    }

    @FXML
    public void initialize() {
        ObservableList<String> options = FXCollections.observableArrayList("Any", "Yes", "No");
        cmbIsCity.setItems(options);
        cmbIsMain.setItems(options);
        cmbIsCity.setValue("Any");
        cmbIsMain.setValue("Any");

        // Inicializa botões de paginação como desativados
        if (btnPrev != null) btnPrev.setDisable(true);
        if (btnNext != null) btnNext.setDisable(true);
        if (lblPaginationInfo != null) lblPaginationInfo.setText("");
    }

    @FXML
    void handleSearchArea(ActionEvent event) {
        lblStatus.setText("");
        txtResult.clear();
        lblStatus.getStyleClass().removeAll("status-label-success", "status-label-error");

        // Limpa resultados anteriores e estado de paginação
        this.allResults = new ArrayList<>();
        this.currentPage = 0;
        if (btnPrev != null) btnPrev.setDisable(true);
        if (btnNext != null) btnNext.setDisable(true);
        if (lblPaginationInfo != null) lblPaginationInfo.setText("");

        if (spatialKDTree == null) {
            setErrorStatus("Error: KDTree was not injected correctly.");
            return;
        }

        try {
            double latMin = parseDoubleField(txtLatMin, -90.0);
            double latMax = parseDoubleField(txtLatMax, 90.0);
            double lonMin = parseDoubleField(txtLonMin, -180.0);
            double lonMax = parseDoubleField(txtLonMax, 180.0);

            if (latMin > latMax || lonMin > lonMax) {
                setErrorStatus("Error: Minimum coordinates cannot be greater than maximum.");
                return;
            }

            String country = txtCountry.getText().trim();
            if (country.isEmpty()) {
                country = null;
            } else {
                country = country.toUpperCase();
            }

            Boolean isCity = parseOptionalBoolean(cmbIsCity.getValue());
            Boolean isMain = parseOptionalBoolean(cmbIsMain.getValue());

            lblStatus.setText("Searching (O(√N) average case)..."); // Info local
            long startTime = System.nanoTime();

            // EXECUÇÃO DA BUSCA EFICIENTE (O(√N + K))
            List<EuropeanStation> results = spatialSearchEngine.searchByGeographicalArea(
                    latMin, latMax, lonMin, lonMax,
                    country,
                    isCity, isMain
            );
            long endTime = System.nanoTime();

            double timeMs = (endTime - startTime) / 1_000_000.0;

            // 1. SALVA TODOS OS RESULTADOS E INICIA A PAGINAÇÃO
            this.allResults = results;
            this.currentPage = 0;

            if (allResults.isEmpty()) {
                txtResult.setText("--- No stations found matching these filters ---");
                setSuccessStatus(String.format("Found 0 stations (%.2f ms)", timeMs));
            } else {
                // 2. RENDERIZA APENAS A PRIMEIRA PÁGINA (Lazy Load)
                updatePagination();
                setSuccessStatus(String.format("Found %d stations (%.2f ms). Displaying page 1.", allResults.size(), timeMs));
            }

        } catch (NumberFormatException e) {
            setErrorStatus("Error: Invalid coordinates. Please enter valid numbers.");
        } catch (Exception e) {
            setErrorStatus("Unexpected error during search: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // --- NOVOS MÉTODOS DE PAGINAÇÃO ---

    /**
     * Lógica para avançar página.
     */
    @FXML
    void handleNextPage(ActionEvent event) {
        int totalPages = (int) Math.ceil((double) allResults.size() / ITEMS_PER_PAGE);
        if (currentPage < totalPages - 1) {
            currentPage++;
            updatePagination();
        }
    }

    /**
     * Lógica para retroceder página.
     */
    @FXML
    void handlePrevPage(ActionEvent event) {
        if (currentPage > 0) {
            currentPage--;
            updatePagination();
        }
    }

    /**
     * Implementa o Lazy Load: fatia a lista completa e atualiza a exibição.
     */
    private void updatePagination() {
        int totalResults = allResults.size();
        int totalPages = (int) Math.ceil((double) totalResults / ITEMS_PER_PAGE);

        int startIndex = currentPage * ITEMS_PER_PAGE;
        int endIndex = Math.min(startIndex + ITEMS_PER_PAGE, totalResults);

        // 1. Lazy Load: Obtém a sublista para a página atual (O(1) para subList de ArrayList)
        List<EuropeanStation> pageResults = allResults.subList(startIndex, endIndex);

        // 2. Renderização
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("--- Página %d de %d (Resultados %d - %d de %d) ---\n\n",
                currentPage + 1, totalPages, startIndex + 1, endIndex, totalResults));

        // Renderiza apenas os 50 elementos da página
        pageResults.stream().forEach(station ->
                sb.append(formatStationDisplay(station)).append("\n")
        );

        txtResult.setText(sb.toString());

        // 3. Atualiza Botões e Info
        if (btnPrev != null) btnPrev.setDisable(currentPage == 0);
        if (btnNext != null) btnNext.setDisable(currentPage >= totalPages - 1);
        if (lblPaginationInfo != null) lblPaginationInfo.setText(String.format("Pág. %d/%d", currentPage + 1, totalPages));
    }

    // --- MÉTODOS HELPER (Inalterados) ---

    private String formatStationDisplay(EuropeanStation station) {
        return station.getStation() +
                " [" + station.getCountry() + "] " +
                "(" + String.format("%.6f", station.getLatitude()) + ", " +
                String.format("%.6f", station.getLongitude()) + ")" +
                (station.isCity() ? " 🏙️" : "") +
                (station.isMainStation() ? " ⭐" : "");
    }

    private double parseDoubleField(TextField field, double defaultValue) throws NumberFormatException {
        String text = field.getText().trim().replace(',', '.');
        if (text.isEmpty()) {
            if(field == txtLatMin) return -90.0;
            if(field == txtLatMax) return 90.0;
            if(field == txtLonMin) return -180.0;
            if(field == txtLonMax) return 180.0;
        }
        return Double.parseDouble(text);
    }

    private Boolean parseOptionalBoolean(String value) {
        if (value.equals("Yes")) {
            return true;
        }
        if (value.equals("No")) {
            return false;
        }
        return null; // "Any"
    }

    private void setErrorStatus(String message) {
        lblStatus.setText("❌ " + message);
        lblStatus.getStyleClass().add("status-label-error");

        if (mainController != null) {
            mainController.showNotification(message, "error");
        }
    }

    private void setSuccessStatus(String message) {
        lblStatus.setText("✅ " + message);
        lblStatus.getStyleClass().add("status-label-success");

        if (mainController != null) {
            mainController.showNotification(message, "success");
        }
    }
}
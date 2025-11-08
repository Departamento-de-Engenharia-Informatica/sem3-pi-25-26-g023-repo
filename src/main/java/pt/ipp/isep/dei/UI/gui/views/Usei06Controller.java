package pt.ipp.isep.dei.UI.gui.views;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.VBox;
import pt.ipp.isep.dei.UI.gui.MainController;
import pt.ipp.isep.dei.domain.EuropeanStation;
import pt.ipp.isep.dei.domain.StationIndexManager;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class Usei06Controller {

    @FXML
    private VBox step1Pane, resultsPane;
    @FXML
    private RadioButton radioSingleTZ, radioWindowTZ;
    @FXML
    private ToggleGroup tzToggleGroup;
    @FXML
    private TextField fieldSingleTZ, fieldMinTZ, fieldMaxTZ;
    @FXML
    private Label step1ErrorLabel;
    @FXML
    private Label resultsStatusLabel;
    @FXML
    private TextField filterCountry;
    @FXML
    private ComboBox<String> filterIsCity, filterIsMain, filterIsAirport;
    @FXML
    private TableView<EuropeanStation> resultsTable;
    @FXML
    private TableColumn<EuropeanStation, String> colStation, colCountry, colTimeZone;
    @FXML
    private TableColumn<EuropeanStation, Boolean> colCity, colMain, colAirport;

    private MainController mainController;
    private StationIndexManager stationIndexManager;

    private final ObservableList<EuropeanStation> masterData = FXCollections.observableArrayList();
    private FilteredList<EuropeanStation> filteredData;

    public void setServices(MainController main, StationIndexManager sim) {
        this.mainController = main;
        this.stationIndexManager = sim;
    }

    @FXML
    public void initialize() {
        fieldSingleTZ.disableProperty().bind(radioWindowTZ.selectedProperty());
        fieldMinTZ.disableProperty().bind(radioSingleTZ.selectedProperty());
        fieldMaxTZ.disableProperty().bind(radioSingleTZ.selectedProperty());
        step1ErrorLabel.setText("");

        ObservableList<String> filterOptions = FXCollections.observableArrayList("Any", "Yes", "No");
        filterIsCity.setItems(filterOptions);
        filterIsMain.setItems(filterOptions);
        filterIsAirport.setItems(filterOptions);
        filterIsCity.setValue("Any");
        filterIsMain.setValue("Any");
        filterIsAirport.setValue("Any");

        colStation.setCellValueFactory(new PropertyValueFactory<>("station"));
        colCountry.setCellValueFactory(new PropertyValueFactory<>("country"));
        colTimeZone.setCellValueFactory(new PropertyValueFactory<>("timeZoneGroup"));
        colCity.setCellValueFactory(new PropertyValueFactory<>("city"));
        colMain.setCellValueFactory(new PropertyValueFactory<>("mainStation"));
        colAirport.setCellValueFactory(new PropertyValueFactory<>("airport"));

        this.filteredData = new FilteredList<>(masterData, p -> true);
        resultsTable.setItems(filteredData);

        filterCountry.textProperty().addListener((obs, old, val) -> updateFilterPredicate());
        filterIsCity.valueProperty().addListener((obs, old, val) -> updateFilterPredicate());
        filterIsMain.valueProperty().addListener((obs, old, val) -> updateFilterPredicate());
        filterIsAirport.valueProperty().addListener((obs, old, val) -> updateFilterPredicate());

        showStep(1);
    }

    private void showStep(int step) {
        step1Pane.setVisible(step == 1);
        step1Pane.setManaged(step == 1);
        resultsPane.setVisible(step == 2);
        resultsPane.setManaged(step == 2);
    }

    @FXML
    void handleStep1Search() {
        step1ErrorLabel.setText("");
        String tzg1 = fieldSingleTZ.getText().trim().toUpperCase();
        String tzgMin = fieldMinTZ.getText().trim().toUpperCase();
        String tzgMax = fieldMaxTZ.getText().trim().toUpperCase();

        List<EuropeanStation> baseResults;
        try {
            if (radioSingleTZ.isSelected()) {
                if (tzg1.isEmpty()) {
                    String errorMsg = "Time Zone Group cannot be empty.";
                    step1ErrorLabel.setText(errorMsg);
                    mainController.showNotification(errorMsg, "error"); // ✅ Notificação Pop-up
                    return;
                }
                baseResults = stationIndexManager.getStationsByTimeZoneGroup(tzg1);
            } else {
                if (tzgMin.isEmpty() || tzgMax.isEmpty()) {
                    String errorMsg = "Min and Max Time Zones cannot be empty.";
                    step1ErrorLabel.setText(errorMsg);
                    mainController.showNotification(errorMsg, "error"); // ✅ Notificação Pop-up
                    return;
                }
                baseResults = stationIndexManager.getStationsInTimeZoneWindow(tzgMin, tzgMax);
            }

            if (baseResults.isEmpty()) {
                String errorMsg = "No stations found for this time zone query.";
                step1ErrorLabel.setText(errorMsg);
                // "Não encontrado" não é um erro, por isso não usamos pop-up
            } else {
                List<EuropeanStation> sortedResults = baseResults.stream()
                        .sorted(Comparator.comparing(EuropeanStation::getCountry)
                                .thenComparing(EuropeanStation::getStation))
                        .collect(Collectors.toList());

                masterData.setAll(sortedResults);
                updateResultsCount();
                showStep(2);

                // ✅ Notificação Pop-up de Sucesso
                mainController.showNotification(String.format("Search successful. Found %d stations.", baseResults.size()), "success");
            }
        } catch (Exception e) {
            String errorMsg = "Error: " + e.getMessage();
            step1ErrorLabel.setText(errorMsg);
            mainController.showNotification(errorMsg, "error"); // ✅ Notificação Pop-up
        }
    }

    private void updateFilterPredicate() {
        String country = filterCountry.getText().trim().toUpperCase();
        Boolean isCity = parseFilterComboBox(filterIsCity);
        Boolean isMain = parseFilterComboBox(filterIsMain);
        Boolean isAirport = parseFilterComboBox(filterIsAirport);

        filteredData.setPredicate(station -> {
            if (country != null && !country.isEmpty()) {
                if (!station.getCountry().equalsIgnoreCase(country)) {
                    return false;
                }
            }
            if (isCity != null) {
                if (station.isCity() != isCity) {
                    return false;
                }
            }
            if (isMain != null) {
                if (station.isMainStation() != isMain) {
                    return false;
                }
            }
            if (isAirport != null) {
                if (station.isAirport() != isAirport) {
                    return false;
                }
            }
            return true;
        });

        updateResultsCount();
    }

    private Boolean parseFilterComboBox(ComboBox<String> combo) {
        String value = combo.getValue();
        if (value == null || value.equals("Any")) {
            return null;
        }
        return value.equals("Yes");
    }

    @FXML
    void handleResultsBack() {
        masterData.clear();
        filterCountry.clear();
        filterIsCity.setValue("Any");
        filterIsMain.setValue("Any");
        filterIsAirport.setValue("Any");
        showStep(1);
    }

    private void updateResultsCount() {
        int totalFound = filteredData.size();
        if (totalFound == masterData.size()) {
            resultsStatusLabel.setText(String.format("Showing all %d results", totalFound));
        } else {
            resultsStatusLabel.setText(String.format("Showing %d of %d results", totalFound, masterData.size()));
        }
    }
}
package pt.ipp.isep.dei.UI.gui.views;

import pt.ipp.isep.dei.controller.TravelTimeController;
import pt.ipp.isep.dei.domain.EuropeanStation; // USAR EuropeanStation
import pt.ipp.isep.dei.domain.Locomotive;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextArea;
import java.util.List;
import java.util.Collection; // Usar List ou Collection, mas do tipo correto

public class TravelTimeGUIController {

    // 1. ALTERAÇÃO ESSENCIAL: O ComboBox deve usar EuropeanStation
    // Se o FXML estiver vinculado a 'Station', deve atualizar o FXML.
    @FXML
    private ComboBox<EuropeanStation> departureStationCombo;
    @FXML
    private ComboBox<EuropeanStation> arrivalStationCombo;

    @FXML
    private ComboBox<Locomotive> locomotiveCombo;
    @FXML
    private Button calculateButton;
    @FXML
    private TextArea resultArea;

    private TravelTimeController backendController; // O controlador de lógica

    public void setBackend(TravelTimeController backendController) {
        this.backendController = backendController;
        loadInitialData();
    }

    @FXML
    public void initialize() {
        // O listener agora trabalha com EuropeanStation
        departureStationCombo.getSelectionModel().selectedItemProperty().addListener((options, oldVal, newVal) -> {
            if (newVal != null) {
                // getIdEstacao() está agora em EuropeanStation
                updateArrivalStations(newVal.getIdEstacao());
            }
        });
    }

    private void loadInitialData() {
        if (backendController == null) return;

        try {
            // 2. CORREÇÃO DA INCOMPATIBILIDADE: A variável deve ser do tipo retornado
            List<EuropeanStation> allStations = backendController.getStationRepository().findAll();

            // A ComboBox deve ser do tipo EuropeanStation para aceitar esta lista
            departureStationCombo.setItems(FXCollections.observableArrayList(allStations));

            // 3. Carrega todas as locomotivas
            List<Locomotive> allLocomotives = backendController.getLocomotiveRepository().findAll();
            locomotiveCombo.setItems(FXCollections.observableArrayList(allLocomotives));

            resultArea.setText("Controlador de backend carregado. Selecione as opções.");

        } catch (Exception e) {
            resultArea.setText("Erro ao carregar dados dos repositórios: " + e.getMessage());
        }
    }

    private void updateArrivalStations(int departureId) {
        try {
            List<EuropeanStation> connectedStations = backendController.getDirectlyConnectedStations(departureId);

            // 4. A ComboBox de chegada deve aceitar a lista de EuropeanStation
            arrivalStationCombo.setItems(FXCollections.observableArrayList(connectedStations));
            arrivalStationCombo.setDisable(false);
        } catch (Exception e) {
            resultArea.setText("Erro ao buscar estações conectadas: " + e.getMessage());
        }
    }

    @FXML
    void handleCalculateTravelTime(ActionEvent event) {
        // 5. CORREÇÃO: As variáveis locais usam EuropeanStation
        EuropeanStation departure = departureStationCombo.getValue();
        EuropeanStation arrival = arrivalStationCombo.getValue();
        Locomotive loco = locomotiveCombo.getValue();

        // 6. Validar
        if (departure == null || arrival == null || loco == null) {
            resultArea.setText("ERRO: Preencha todos os campos (Partida, Chegada e Locomotiva).");
            return;
        }

        if (departure.getIdEstacao() == arrival.getIdEstacao()) {
            resultArea.setText("ERRO: A estação de chegada não pode ser igual à de partida.");
            return;
        }

        // 7. Chamar o controlador de lógica
        try {
            resultArea.setText("A calcular...");
            String result = backendController.calculateTravelTime(
                    departure.getIdEstacao(),
                    arrival.getIdEstacao(),
                    loco.getIdLocomotiva()
            );

            resultArea.setText(result);

        } catch (Exception e) {
            resultArea.setText("Erro inesperado ao calcular a rota: " + e.getMessage());
        }
    }
}
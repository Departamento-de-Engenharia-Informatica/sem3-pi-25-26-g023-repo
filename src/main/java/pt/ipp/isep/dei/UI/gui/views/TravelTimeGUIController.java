// package pt.ipp.isep.dei.UI.gui;
package pt.ipp.isep.dei.UI.gui.views;

import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextArea;
import pt.ipp.isep.dei.controller.TravelTimeController;
import pt.ipp.isep.dei.domain.Locomotive;
import pt.ipp.isep.dei.domain.Station;

import java.util.List;

public class TravelTimeGUIController {

    @FXML
    private ComboBox<Station> departureStationCombo;
    @FXML
    private ComboBox<Station> arrivalStationCombo;
    @FXML
    private ComboBox<Locomotive> locomotiveCombo;
    @FXML
    private Button calculateButton;
    @FXML
    private TextArea resultArea;

    private TravelTimeController backendController; // O controlador de lógica

    /**
     * Chamado pelo MainController para injetar a lógica de backend.
     */
    public void setBackend(TravelTimeController backendController) {
        this.backendController = backendController;

        // Agora que temos o controlador, populamos as ComboBox
        loadInitialData();
    }

    /**
     * O initialize é chamado ANTES do setBackend,
     * por isso temos de popular os dados *depois* do setBackend.
     */
    @FXML
    public void initialize() {
        // Configura um listener na ComboBox de partida
        // Isto replica a lógica da consola (só mostrar destinos válidos)
        departureStationCombo.getSelectionModel().selectedItemProperty().addListener((options, oldVal, newVal) -> {
            if (newVal != null) {
                // Quando uma estação de partida é selecionada,
                // atualiza a lista de estações de chegada.
                updateArrivalStations(newVal.getIdEstacao());
            }
        });
    }

    /**
     * Carrega os dados iniciais (estações e locomotivas)
     */
    private void loadInitialData() {
        if (backendController == null) return; // Segurança

        try {
            // 1. Carrega todas as estações (para a partida)
            List<Station> allStations = backendController.getStationRepository().findAll();
            departureStationCombo.setItems(FXCollections.observableArrayList(allStations));

            // 2. Carrega todas as locomotivas
            List<Locomotive> allLocomotives = backendController.getLocomotiveRepository().findAll();
            locomotiveCombo.setItems(FXCollections.observableArrayList(allLocomotives));

            resultArea.setText("Controlador de backend carregado. Selecione as opções.");

        } catch (Exception e) {
            resultArea.setText("Erro ao carregar dados dos repositórios: " + e.getMessage());
        }
    }

    /**
     * Atualiza a ComboBox de chegada com base na partida selecionada.
     */
    private void updateArrivalStations(int departureId) {
        try {
            // Exatamente a mesma lógica da TravelTimeUI da consola
            List<Station> connectedStations = backendController.getDirectlyConnectedStations(departureId);
            arrivalStationCombo.setItems(FXCollections.observableArrayList(connectedStations));
            arrivalStationCombo.setDisable(false); // Ativa a ComboBox
        } catch (Exception e) {
            resultArea.setText("Erro ao buscar estações conectadas: " + e.getMessage());
        }
    }

    /**
     * Chamado quando o botão "Calcular" é clicado.
     */
    @FXML
    void handleCalculateTravelTime(ActionEvent event) {
        // 1. Obter os dados da UI
        Station departure = departureStationCombo.getValue();
        Station arrival = arrivalStationCombo.getValue();
        Locomotive loco = locomotiveCombo.getValue();

        // 2. Validar (como na consola)
        if (departure == null || arrival == null || loco == null) {
            resultArea.setText("ERRO: Preencha todos os campos (Partida, Chegada e Locomotiva).");
            return;
        }

        if (departure.getIdEstacao() == arrival.getIdEstacao()) {
            resultArea.setText("ERRO: A estação de chegada não pode ser igual à de partida.");
            return;
        }

        // 3. Chamar o controlador de lógica (exatamente como na consola)
        try {
            resultArea.setText("A calcular...");
            String result = backendController.calculateTravelTime(
                    departure.getIdEstacao(),
                    arrival.getIdEstacao(),
                    loco.getIdLocomotiva()
            );

            // 4. Mostrar o resultado
            resultArea.setText(result);

        } catch (Exception e) {
            resultArea.setText("Erro inesperado ao calcular a rota: " + e.getMessage());
        }
    }
}
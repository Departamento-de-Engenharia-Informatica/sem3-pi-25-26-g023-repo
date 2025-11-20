package pt.ipp.isep.dei.UI.gui.views;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import pt.ipp.isep.dei.UI.gui.MainController;

public class BDDADMainController {

    private MainController mainController;

    /**
     * Injeta a dependência do MainController para permitir a navegação.
     */
    public void setServices(MainController mainController) {
        this.mainController = mainController;
    }

    /**
     * Chamado quando o botão "Operator" é clicado.
     */
    @FXML
    void handleShowOperatorCRUD(ActionEvent event) {
        if (mainController != null) {
            // Chama o novo método no MainController
            mainController.loadOperatorCrudView();
        }
    }
    // ✅ NOVO MÉTODO PARA LOCOMOTIVE
    @FXML
    void handleShowLocomotiveCRUD(ActionEvent event) {
        if (mainController != null) {
            mainController.loadLocomotiveCrudView(); // Chama o método de carregamento no MainController
        }
    }
    @FXML
    void handleShowWagonCRUD(ActionEvent event) {
        if (mainController != null) {
            mainController.loadWagonCrudView();
        }
    }
}
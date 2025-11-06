package pt.ipp.isep.dei.UI.gui;

import javafx.scene.control.Alert;
import javafx.stage.Stage;

/**
 * Classe utilitária para mostrar Alertas (pop-ups) na interface JavaFX.
 * Isto evita repetir código de Alertas em todos os controladores.
 */
public class GuiUtils {

    /**
     * Mostra um pop-up de Alerta.
     * @param type O tipo de Alerta (ERROR, INFORMATION, WARNING)
     * @param title O título da janela do pop-up
     * @param header O texto principal a bold
     * @param content O texto de detalhe
     */
    public static void showAlert(Alert.AlertType type, String title, String header, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);

        // Adiciona o stylesheet para que o pop-up tenha o mesmo estilo "dark mode"
        try {
            Stage stage = (Stage) alert.getDialogPane().getScene().getWindow();
            stage.getIcons().clear(); // Pode adicionar um ícone aqui se quiser
            alert.getDialogPane().getStylesheets().add(
                    GuiUtils.class.getResource("style.css").toExternalForm());
            alert.getDialogPane().getStyleClass().add("dialog-pane");
        } catch (Exception e) {
            // Ignora se o CSS não for encontrado, usa o default
        }

        alert.showAndWait();
    }
}
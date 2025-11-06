package pt.ipp.isep.dei.UI.gui;

import javafx.scene.control.Alert;
import javafx.scene.control.DialogPane;
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
    /**
     * Shows a styled ERROR alert.
     */
    public static void showErrorAlert(String title, String header, String content) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);

        // Apply dark mode CSS to the dialog
        styleDialog(alert);
        alert.showAndWait();
    }

    /**
     * Shows a styled INFORMATION alert.
     */
    public static void showInfoAlert(String title, String header, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);

        // Apply dark mode CSS to the dialog
        styleDialog(alert);
        alert.showAndWait();
    }

    /**
     * Applies the .dialog-pane styleclass from our style.css
     * to any Alert pop-up.
     */
    private static void styleDialog(Alert alert) {
        try {
            DialogPane dialogPane = alert.getDialogPane();
            dialogPane.getStylesheets().add(
                    GuiUtils.class.getResource("/style.css").toExternalForm()
            );
            dialogPane.getStyleClass().add("dialog-pane");
        } catch (Exception e) {
            // If it fails, show the default alert style
            System.err.println("Warning: Could not load 'style.css' for dialog.");
        }
    }
}
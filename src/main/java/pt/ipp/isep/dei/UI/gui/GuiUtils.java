package pt.ipp.isep.dei.UI.gui;

import javafx.scene.control.Alert;
import javafx.scene.control.DialogPane;
import javafx.stage.Stage;

/**
 * Utility class for displaying Alerts (pop-ups) in the JavaFX interface.
 * This prevents repetitive Alert code in all controllers.
 */
public class GuiUtils {

    /**
     * Displays an Alert pop-up.
     * @param type The type of Alert (ERROR, INFORMATION, WARNING)
     * @param title The title of the pop-up window
     * @param header The main bold text
     * @param content The detailed text
     */
    public static void showAlert(Alert.AlertType type, String title, String header, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);

        // Adds the stylesheet so the pop-up has the same "dark mode" style
        try {
            Stage stage = (Stage) alert.getDialogPane().getScene().getWindow();
            stage.getIcons().clear(); // Can add an icon here if desired
            alert.getDialogPane().getStylesheets().add(
                    GuiUtils.class.getResource("style.css").toExternalForm());
            alert.getDialogPane().getStyleClass().add("dialog-pane");
        } catch (Exception e) {
            // Ignores if CSS is not found, uses default
        }

        alert.showAndWait();
    }
    /**
     * Shows a styled ERROR alert.
     *
     * @param title The title of the alert window.
     * @param header The main header text.
     * @param content The detailed content text.
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
     *
     * @param title The title of the alert window.
     * @param header The main header text.
     * @param content The detailed content text.
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
     *
     * @param alert The JavaFX Alert object to style.
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
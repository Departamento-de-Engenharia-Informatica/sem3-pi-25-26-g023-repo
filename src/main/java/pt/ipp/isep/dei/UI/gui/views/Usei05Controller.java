package pt.ipp.isep.dei.UI.gui.views;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import pt.ipp.isep.dei.UI.gui.MainController;
import pt.ipp.isep.dei.domain.InventoryManager;
import pt.ipp.isep.dei.domain.WMS;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;

public class Usei05Controller {

    @FXML
    private Button btnProcess;
    @FXML
    private Label lblStatus;
    @FXML
    private TextArea txtResult;

    private MainController mainController;
    private WMS wms;
    private InventoryManager manager;

    public void setServices(MainController mainController, WMS wms, InventoryManager manager) {
        this.mainController = mainController;
        this.wms = wms;
        this.manager = manager;
    }

    @FXML
    void handleProcessReturns(ActionEvent event) {
        lblStatus.setText("");
        txtResult.clear();

        if (this.wms == null) {
            String errorMsg = "Error: WMS was not injected correctly.";
            lblStatus.setText(errorMsg);
            // ✅ Notificação Pop-up
            if (mainController != null) {
                mainController.showNotification(errorMsg, "error");
            }
            return;
        }

        lblStatus.setText("ℹ️  Processing returns (LIFO)...");
        txtResult.setText("ℹ️  --- [USEI05] Process Quarantine Returns (LIFO) ---");

        PrintStream originalOut = System.out;
        OutputStream taOutputStream = new TextAreaOutputStream(txtResult);
        System.setOut(new PrintStream(taOutputStream, true));

        try {
            wms.processReturns();

            String successMsg = "Return processing complete.";
            System.out.println("\n✅ SUCCESS: " + successMsg);
            System.out.println("\nℹ️  Check 'audit.log' for details.");

            lblStatus.setText("✅ Processing complete!");
            // ✅ Notificação Pop-up
            if (mainController != null) {
                mainController.showNotification(successMsg, "success");
            }

        } catch (Exception e) {
            String errorMsg = "Error processing returns: " + e.getMessage();
            System.out.println("\n❌ ERROR: " + e.getMessage());
            lblStatus.setText("❌ " + errorMsg);
            // ✅ Notificação Pop-up
            if (mainController != null) {
                mainController.showNotification(errorMsg, "error");
            }
            e.printStackTrace(System.out);

        } finally {
            System.setOut(originalOut);
        }
    }

    public static class TextAreaOutputStream extends OutputStream {

        private TextArea textArea;

        public TextAreaOutputStream(TextArea textArea) {
            this.textArea = textArea;
        }

        @Override
        public void write(byte[] b, int off, int len) throws IOException {
            String text = new String(b, off, len);
            Platform.runLater(() -> textArea.appendText(text));
        }

        @Override
        public void write(int b) throws IOException {
            write(new byte[]{(byte) b}, 0, 1);
        }
    }
}
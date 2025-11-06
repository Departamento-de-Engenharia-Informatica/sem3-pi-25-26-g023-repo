package pt.ipp.isep.dei.UI.gui;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;

public class MainApplication extends Application {

    @Override
    public void start(Stage primaryStage) throws IOException {

        // ====================================================================
        // MUDANÇA: Agora procuramos os ficheiros na RAIZ da pasta resources
        // ====================================================================

        String fxmlPath = "main-view.fxml"; // Sem caminho, procura na raiz
        String cssPath = "style.css";       // Sem caminho, procura na raiz

        // Obter o ClassLoader
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();

        // Carregar o FXML
        URL fxmlUrl = classLoader.getResource(fxmlPath);
        if (fxmlUrl == null) {
            System.err.println("ERRO CRÍTICO: Não foi possível encontrar o FXML em: " + fxmlPath);
            System.err.println("Verifique se o ficheiro está em 'src/main/resources/main-view.fxml'");
            throw new IOException("Location is not set: " + fxmlPath);
        }

        FXMLLoader loader = new FXMLLoader(fxmlUrl);
        Parent root = loader.load();

        // Carregar o CSS
        URL cssUrl = classLoader.getResource(cssPath);
        if (cssUrl != null) {
            root.getStylesheets().add(cssUrl.toExternalForm());
        } else {
            System.err.println("AVISO: Ficheiro CSS 'style.css' não encontrado em: " + cssPath);
        }

        primaryStage.setTitle("Gestão Integrada (PI - G023)");
        primaryStage.setScene(new Scene(root, 1200, 800));
        primaryStage.setMinWidth(1000);
        primaryStage.setMinHeight(700);
        primaryStage.show();
    }
}
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
        // Constrói o caminho para o FXML dentro do JAR (via resources)

        // MUDANÇA AQUI: Remova o caminho absoluto.
        // Isto procura "main-view.fxml" NA MESMA PASTA que MainApplication.class
        FXMLLoader loader = new FXMLLoader(getClass().getResource("main-view.fxml"));
        Parent root = loader.load();

        // Constrói o caminho para o CSS
        // MUDANÇA AQUI: Remova o caminho absoluto.
        URL cssUrl = getClass().getResource("style.css");
        if (cssUrl != null) {
            root.getStylesheets().add(cssUrl.toExternalForm());
        } else {
            System.err.println("Erro: Ficheiro CSS 'style.css' não encontrado.");
        }

        primaryStage.setTitle("Gestão Integrada (PI - G023)");
        primaryStage.setScene(new Scene(root, 1200, 800)); // Tamanho inicial da janela
        primaryStage.setMinWidth(1000); // Tamanho mínimo
        primaryStage.setMinHeight(700);
        primaryStage.show();
    }
}
package pt.ipp.isep.dei;

import pt.ipp.isep.dei.UI.gui.MainApplication;

/**
 * Classe de arranque principal (launcher) para a aplicação JavaFX.
 * O seu único propósito é chamar o método launch() da classe Application principal.
 * Isto é uma solução comum para problemas de "JavaFX runtime components are missing"
 * ao executar diretamente de um IDE.
 */
public class MainGuiLauncher {
    public static void main(String[] args) {
        // O ficheiro Main.java original pode continuar a ser usado para a consola.
        // Este ficheiro lança a MainApplication (a nossa app JavaFX).
        MainApplication.launch(MainApplication.class, args);
    }
}
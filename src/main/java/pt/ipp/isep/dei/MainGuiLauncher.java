package pt.ipp.isep.dei;

import pt.ipp.isep.dei.UI.gui.MainApplication;

/**
 * Main startup (launcher) class for the JavaFX application.
 * Its sole purpose is to call the launch() method of the main Application class.
 * This is a common workaround for "JavaFX runtime components are missing" issues
 * when running directly from an IDE.
 */
public class MainGuiLauncher {
    public static void main(String[] args) {
        // The original Main.java file can still be used for the console.
        // This file launches MainApplication (our JavaFX app).
        MainApplication.launch(MainApplication.class, args);
    }
}
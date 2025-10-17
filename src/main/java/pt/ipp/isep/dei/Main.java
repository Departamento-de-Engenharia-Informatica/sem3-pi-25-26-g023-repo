package pt.ipp.isep.dei;

import pt.ipp.isep.dei.UI.CargoHandlingUI;

public class Main {
    public static void main(String[] args) {
        // 1. Create an instance of your menu UI class.
        CargoHandlingUI cargoMenu = new CargoHandlingUI();

        // 2. Call the run() method to start the menu loop.
        cargoMenu.run();
    }
}
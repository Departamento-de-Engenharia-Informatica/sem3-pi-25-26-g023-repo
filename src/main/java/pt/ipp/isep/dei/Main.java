package pt.ipp.isep.dei;

import pt.ipp.isep.dei.UI.CargoHandlingUI;
import pt.ipp.isep.dei.domain.*;

public class Main {

    public static void main(String[] args) {
        System.out.println("=========================================");
        System.out.println("🚆 Railway Cargo Handling Terminal System");
        System.out.println("=========================================");

        try {
            // 1️⃣ Criar componentes de domínio principais
            Inventory inventory = new Inventory();
            Quarantine quarantine = new Quarantine();
            AuditLog auditLog = new AuditLog("audit.log");
            WMS wms = new WMS(quarantine, inventory, auditLog);

            // 2️⃣ Criar o InventoryManager responsável por carregar os ficheiros CSV
            InventoryManager manager = new InventoryManager();

            // 3️⃣ Carregar dados de exemplo
            System.out.println("Loading product items...");
            manager.loadItems("data/items.csv");

            System.out.println("Loading wagons and boxes...");
            var wagons = manager.loadWagons("data/wagons.csv");

            // 4️⃣ (Opcional) Mostrar um pequeno resumo
            System.out.printf("Loaded %d wagons into the system.%n%n", wagons.size());

            // 5️⃣ Iniciar a interface textual, passando-lhe as dependências
            CargoHandlingUI cargoMenu = new CargoHandlingUI(wms, manager, wagons);
            cargoMenu.run();

            System.out.println("\nSystem terminated normally.");
        } catch (Exception e) {
            System.err.println("❌ Fatal error during startup: " + e.getMessage());
            e.printStackTrace();
        }
    }
}

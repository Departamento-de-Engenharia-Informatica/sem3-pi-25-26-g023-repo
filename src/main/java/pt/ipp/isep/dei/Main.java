package pt.ipp.isep.dei;

import pt.ipp.isep.dei.UI.CargoHandlingUI;
import pt.ipp.isep.dei.domain.*;

import java.util.List;

public class Main {

    public static void main(String[] args) {
        System.out.println("=========================================");
        System.out.println("🚆 Railway Cargo Handling Terminal System");
        System.out.println("=========================================");

        try {
            // 1️⃣ Criar componentes principais
            InventoryManager manager = new InventoryManager();
            Inventory inventory = manager.getInventory();
            Quarantine quarantine = new Quarantine();
            AuditLog auditLog = new AuditLog("audit.log");

            // 2️⃣ Carregar ficheiros base
            System.out.println("Loading product items...");
            manager.loadItems("src/main/java/pt/ipp/isep/dei/FicheirosCSV/items.csv");

            System.out.println("Loading warehouse bays...");
            var bays = manager.loadBays("src/main/java/pt/ipp/isep/dei/FicheirosCSV/bays.csv");
            System.out.printf("Loaded %d bays across %d warehouses.%n", bays.size(), manager.getWarehouses().size());

            System.out.println("Loading wagons and boxes...");
            var wagons = manager.loadWagons("src/main/java/pt/ipp/isep/dei/FicheirosCSV/wagons.csv");
            System.out.printf("Loaded %d wagons.%n", wagons.size());

            // 3️⃣ Criar o WMS com a lista de warehouses carregada
            WMS wms = new WMS(quarantine, inventory, auditLog, manager.getWarehouses());

            // 4️⃣ Descarregar vagões (USEI01)
            System.out.println("Unloading wagons into warehouses and inventory...");
            wms.unloadWagons(wagons);

            // 5️⃣ Processar devoluções (USEI05)
            System.out.println("Loading returns...");
            List<Return> returns = manager.loadReturns("src/main/java/pt/ipp/isep/dei/FicheirosCSV/returns.csv");
            for (Return r : returns) {
                quarantine.addReturn(r);
            }
            wms.processReturns();

            // 6️⃣ Carregar encomendas e respetivas linhas
            System.out.println("Loading orders...");
            var orders = manager.loadOrders(
                    "src/main/java/pt/ipp/isep/dei/FicheirosCSV/orders.csv",
                    "src/main/java/pt/ipp/isep/dei/FicheirosCSV/order_lines.csv"
            );
            System.out.printf("Loaded %d orders.%n", orders.size());

            // 7️⃣ Lançar interface textual (opcional)
            CargoHandlingUI cargoMenu = new CargoHandlingUI(wms, manager, wagons);
            cargoMenu.run();

            System.out.println("\nSystem terminated normally.");
        } catch (Exception e) {
            System.err.println("❌ Fatal error during startup: " + e.getMessage());
            e.printStackTrace();
        }
    }
}

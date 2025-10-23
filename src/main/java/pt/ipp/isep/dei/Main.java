package pt.ipp.isep.dei;

import pt.ipp.isep.dei.UI.CargoHandlingUI;
import pt.ipp.isep.dei.UI.TravelTimeUI; // Importar a nova UI
import pt.ipp.isep.dei.controller.TravelTimeController; // Importar o novo Controller
import pt.ipp.isep.dei.domain.*;
import pt.ipp.isep.dei.repository.EstacaoRepository; // Importar o novo Repo
import pt.ipp.isep.dei.repository.LocomotivaRepository; // Importar o novo Repo
import pt.ipp.isep.dei.repository.SegmentoLinhaRepository; // Importar o novo Repo

import java.util.List;

public class Main {

    public static void main(String[] args) {
        System.out.println("=========================================");
        System.out.println("🚆 Railway Cargo Handling Terminal System");
        System.out.println("=========================================");

        try {
            // 1️⃣ Criar componentes principais (ESINF)
            InventoryManager manager = new InventoryManager();
            Inventory inventory = manager.getInventory();
            Quarantine quarantine = new Quarantine();
            AuditLog auditLog = new AuditLog("audit.log");

            // 2️⃣ Carregar ficheiros base (ESINF)
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

            // 6️⃣ Carregar encomendas e respetivas linhas (ESINF)
            System.out.println("Loading orders...");
            var orders = manager.loadOrders(
                    "src/main/java/pt/ipp/isep/dei/FicheirosCSV/orders.csv",
                    "src/main/java/pt/ipp/isep/dei/FicheirosCSV/order_lines.csv"
            );
            System.out.printf("Loaded %d orders.%n", orders.size());


            // 7️⃣ *** NOVO: Criar componentes LAPR3 (Mock Repositories) ***
            System.out.println("Initializing LAPR3/BDDAD Mock Repositories...");
            EstacaoRepository estacaoRepo = new EstacaoRepository();
            LocomotivaRepository locomotivaRepo = new LocomotivaRepository();
            SegmentoLinhaRepository segmentoRepo = new SegmentoLinhaRepository();
            TravelTimeController travelTimeController = new TravelTimeController(estacaoRepo, locomotivaRepo, segmentoRepo);
            System.out.println("LAPR3 components initialized.");


            // 8️⃣ Lançar interface textual (Modificada para incluir componentes LAPR3)
            // *** LINHA CORRIGIDA ABAIXO ***
            CargoHandlingUI cargoMenu = new CargoHandlingUI(wms, manager, wagons, travelTimeController, estacaoRepo, locomotivaRepo);
            cargoMenu.run();

            System.out.println("\nSystem terminated normally.");
        } catch (Exception e) {
            System.err.println("❌ Fatal error during startup: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
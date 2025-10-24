package pt.ipp.isep.dei;

import pt.ipp.isep.dei.UI.CargoHandlingUI;
import pt.ipp.isep.dei.UI.TravelTimeUI;
import pt.ipp.isep.dei.controller.TravelTimeController;
import pt.ipp.isep.dei.domain.*;
import pt.ipp.isep.dei.repository.EstacaoRepository;
import pt.ipp.isep.dei.repository.LocomotivaRepository;
import pt.ipp.isep.dei.repository.SegmentoLinhaRepository;

import java.util.List;

public class Main {

    public static void main(String[] args) {
        System.out.println("=========================================");
        System.out.println("🚆 Railway Cargo Handling Terminal System");
        System.out.println("=========================================");

        try {
            // 1️⃣ Componentes ESINF
            InventoryManager manager = new InventoryManager();
            Inventory inventory = manager.getInventory();
            Quarantine quarantine = new Quarantine();
            AuditLog auditLog = new AuditLog("audit.log");

            // 2️⃣ Carregar CSVs ESINF
            System.out.println("Loading product items...");
            manager.loadItems("src/main/java/pt/ipp/isep/dei/FicheirosCSV/items.csv"); //
            System.out.println("Loading warehouse bays...");
            var bays = manager.loadBays("src/main/java/pt/ipp/isep/dei/FicheirosCSV/bays.csv"); //
            System.out.printf("Loaded %d bays across %d warehouses.%n", bays.size(), manager.getWarehouses().size());
            System.out.println("Loading wagons and boxes...");
            var wagons = manager.loadWagons("src/main/java/pt/ipp/isep/dei/FicheirosCSV/wagons.csv"); //
            System.out.printf("Loaded %d wagons.%n", wagons.size());

            // 3️⃣ Criar WMS
            WMS wms = new WMS(quarantine, inventory, auditLog, manager.getWarehouses());

            // 4️⃣ USEI01
            System.out.println("Unloading wagons into warehouses and inventory...");
            wms.unloadWagons(wagons); //

            // 5️⃣ USEI05
            System.out.println("Loading returns...");
            List<Return> returns = manager.loadReturns("src/main/java/pt/ipp/isep/dei/FicheirosCSV/returns.csv"); //
            for (Return r : returns) {
                quarantine.addReturn(r); //
            }
            wms.processReturns(); //

            // 6️⃣ Carregar Encomendas ESINF
            System.out.println("Loading orders...");
            var orders = manager.loadOrders(
                    "src/main/java/pt/ipp/isep/dei/FicheirosCSV/orders.csv", //
                    "src/main/java/pt/ipp/isep/dei/FicheirosCSV/order_lines.csv" //
            );
            System.out.printf("Loaded %d orders.%n", orders.size());


            // 7️⃣ *** COMPONENTES LAPR3 MODIFICADOS ***
            System.out.println("Initializing LAPR3/BDDAD Mock Repositories...");
            EstacaoRepository estacaoRepo = new EstacaoRepository(); //
            LocomotivaRepository locomotivaRepo = new LocomotivaRepository(); //
            SegmentoLinhaRepository segmentoRepo = new SegmentoLinhaRepository(); //

            // Novo Serviço de Rede
            RailwayNetworkService networkService = new RailwayNetworkService(estacaoRepo, segmentoRepo); //

            // *** ALTERAÇÃO AQUI: Passar o segmentoRepo também ***
            TravelTimeController travelTimeController = new TravelTimeController(
                    estacaoRepo,
                    locomotivaRepo,
                    networkService,
                    segmentoRepo // Passando a dependência extra necessária para getDirectConnectionsInfo
            );
            System.out.println("LAPR3 components initialized.");


            // 8️⃣ Lançar interface textual
            CargoHandlingUI cargoMenu = new CargoHandlingUI(wms, manager, wagons,
                    travelTimeController, estacaoRepo, locomotivaRepo); //
            cargoMenu.run();

            System.out.println("\nSystem terminated normally.");
        } catch (Exception e) {
            System.err.println("❌ Fatal error during startup: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
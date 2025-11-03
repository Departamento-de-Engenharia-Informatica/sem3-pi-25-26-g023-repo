package pt.ipp.isep.dei;

import pt.ipp.isep.dei.UI.CargoHandlingUI;
import pt.ipp.isep.dei.controller.TravelTimeController;
import pt.ipp.isep.dei.domain.*;
import pt.ipp.isep.dei.repository.StationRepository;
import pt.ipp.isep.dei.repository.LocomotiveRepository;
import pt.ipp.isep.dei.repository.SegmentLineRepository;

import java.util.List;

/**
 * Ponto de entrada principal (Main) - Versão 2.2 "Concisa"
 * * Log de arranque limpo, 100% controlado pela Main.
 */
public class Main {

    // --- Códigos de Cores ANSI ---
    private static final String ANSI_RESET = "\u001B[0m";
    private static final String ANSI_RED = "\u001B[31m";
    private static final String ANSI_GREEN = "\u001B[32m";
    private static final String ANSI_YELLOW = "\u001B[33m";
    private static final String ANSI_BLUE = "\u001B[34m";
    private static final String ANSI_CYAN = "\u001B[36m";
    private static final String ANSI_BOLD = "\u001B[1m";

    /**
     * O método main que arranca a aplicação.
     */
    public static void main(String[] args) {


        try {
            // 1️⃣ Inicializar Componentes (Silencioso)
            InventoryManager manager = new InventoryManager();
            Inventory inventory = manager.getInventory();
            Quarantine quarantine = new Quarantine();
            AuditLog auditLog = new AuditLog("audit.log");

            // --- Bloco de Carregamento de Dados (Controlado) ---
            System.out.println(ANSI_BOLD + "Loading system data... Please wait." + ANSI_RESET);

            // 2️⃣ Carregar ESINF (Sprint 1)
            printLoadStep("Loading ESINF (Sprint 1) data...");
            manager.loadItems("src/main/java/pt/ipp/isep/dei/FicheirosCSV/items.csv");
            printLoadStep(String.format("  > Loaded %d items", manager.getItemsCount()), true);

            manager.loadBays("src/main/java/pt/ipp/isep/dei/FicheirosCSV/bays.csv");
            printLoadStep(String.format("  > Loaded %d bays across %d warehouses", manager.getBaysCount(), manager.getWarehouseCount()), true);

            List<Wagon> wagons = manager.loadWagons("src/main/java/pt/ipp/isep/dei/FicheirosCSV/wagons.csv");
            printLoadStep(String.format("  > Loaded %d wagons", manager.getWagonsCount()), true);

            // 3️⃣ Criar WMS e Carregar Vagões
            WMS wms = new WMS(quarantine, inventory, auditLog, manager.getWarehouses());

            printLoadStep("Unloading wagons into inventory...");
            // --- ALTERAÇÃO: Captura o resultado silencioso ---
            WMS.UnloadResult unloadResult = wms.unloadWagons(wagons);
            // Imprime o sumário conciso
            printLoadStep(String.format("  > Unloaded %d wagons (%d boxes). (Full: %d, Partial: %d, Failed: %d)",
                    unloadResult.totalProcessed, unloadResult.totalBoxes,
                    unloadResult.fullyUnloaded, unloadResult.partiallyUnloaded, unloadResult.notUnloaded), true);

            // 4️⃣ Carregar Devoluções (Returns)
            List<Return> returns = manager.loadReturns("src/main/java/pt/ipp/isep/dei/FicheirosCSV/returns.csv");
            for (Return r : returns) {
                quarantine.addReturn(r);
            }
            printLoadStep(String.format("  > Loaded %d returns into quarantine", manager.getReturnsCount()), true);

            // 5️⃣ Carregar Pedidos (Orders)
            List<Order> orders = manager.loadOrders(
                    "src/main/java/pt/ipp/isep/dei/FicheirosCSV/orders.csv",
                    "src/main/java/pt/ipp/isep/dei/FicheirosCSV/order_lines.csv"
            );
            printLoadStep(String.format("  > Loaded %d orders with lines", manager.getOrdersCount()), true);

            // 6️⃣ Carregar LAPR3 (Sprint 1)
            printLoadStep("Loading LAPR3 (Sprint 1) components...");
            StationRepository estacaoRepo = new StationRepository();
            LocomotiveRepository locomotivaRepo = new LocomotiveRepository();
            SegmentLineRepository segmentoRepo = new SegmentLineRepository();
            RailwayNetworkService networkService = new RailwayNetworkService(estacaoRepo, segmentoRepo);
            TravelTimeController travelTimeController = new TravelTimeController(
                    estacaoRepo, locomotivaRepo, networkService, segmentoRepo
            );
            printLoadStep("  > LAPR3 components initialized.", true);

            // 7️⃣ Carregar ESINF (Sprint 2)
            printLoadStep("Loading ESINF (Sprint 2) components...");
            StationIndexManager stationIndexManager = new StationIndexManager();

            // Chama o método silencioso
            List<EuropeanStation> europeanStations = manager.loadEuropeanStations("src/main/java/pt/ipp/isep/dei/FicheirosCSV/train_stations_europe.csv");

            // Imprime o sumário "bonito" usando os getters
            String summary = String.format("  > Loaded %d valid stations", manager.getValidStationCount());
            if (manager.getInvalidStationCount() > 0) {
                // Mostra o sumário de erros, mas não os erros em si
                summary += ANSI_YELLOW + String.format(" (%d invalid rows rejected)", manager.getInvalidStationCount()) + ANSI_GREEN;
            }
            printLoadStep(summary, true);

            printLoadStep("Building station indexes (USEI06)...");
            stationIndexManager.buildIndexes(europeanStations); // Chama o método silencioso
            printLoadStep("  > All station indexes built.", true); // A Main reporta o sucesso


            // 9️⃣ Lançar a UI
            System.out.println(ANSI_BOLD + "\nSystem loaded successfully. Launching UI..." + ANSI_RESET);
            Thread.sleep(1000); // Pausa dramática

            CargoHandlingUI cargoMenu = new CargoHandlingUI(
                    wms, manager, wagons,
                    travelTimeController, estacaoRepo, locomotivaRepo,
                    stationIndexManager
            );
            cargoMenu.run();

            System.out.println("\nSystem terminated normally.");

        } catch (Exception e) {
            // Erro fatal de arranque
            System.out.println(ANSI_RED + ANSI_BOLD + "❌ FATAL ERROR DURING STARTUP" + ANSI_RESET);
            System.out.println(ANSI_RED + e.getMessage() + ANSI_RESET);
            e.printStackTrace();
        }
    }


    /**
     * Helper "bonito" para imprimir o estado do carregamento.
     */
    private static void printLoadStep(String message, boolean success) {
        String color = success ? ANSI_GREEN : ANSI_RED;
        String symbol = success ? "✅" : "❌";
        System.out.println(color + " " + symbol + " " + message + ANSI_RESET);
    }

    /**
     * Sobrecarga para mensagens de "a carregar..." (sem sucesso/falha)
     */
    private static void printLoadStep(String message) {
        System.out.println(ANSI_CYAN + " ⚙️  " + message + ANSI_RESET);
    }
}
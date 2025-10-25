package pt.ipp.isep.dei.DatabaseConnection; // Ou o teu package correto

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class DatabaseConnection {

    // --- Detalhes da Conexão (Verifique se estão corretos) ---
    private static final String DB_URL = "jdbc:oracle:thin:@vsgate-s1.dei.isep.ipp.pt:10945:xe";
    private static final String DB_USER = "system"; // Confirme se este é o user correto
    private static final String DB_PASSWORD = "oracle"; // <-- MUDA ISTO PARA A PASSWORD CORRETA
    // --- Fim dos Detalhes ---

    // Carrega o driver
    static {
        try {
            Class.forName("oracle.jdbc.driver.OracleDriver");
            // System.out.println("Driver Oracle JDBC carregado com sucesso."); // <-- COMENTADO
        } catch (ClassNotFoundException e) {
            System.err.println("❌ ERRO FATAL: Driver Oracle JDBC não encontrado no classpath!");
            System.err.println("   Verifique se a dependência Maven 'ojdbc' está correta no pom.xml.");
            System.exit(1); // Sai se o driver não carregar
        }
    }

    // ... (restante da classe DatabaseConnection igual à versão anterior) ...

    /**
     * Obtém uma nova conexão à base de dados.
     * QUEM CHAMA ESTE MÉTODO É RESPONSÁVEL POR FECHAR A CONEXÃO!
     */
    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
    }

    /**
     * Conecta à base de dados, lê e imprime dados das tabelas
     * RAILWAY_LINE, LINE_SEGMENT e ROLLING_STOCK (locomotivas).
     */
    public static void printSelectedDatabaseData() {
        System.out.println("\n--- Lendo Dados da Base de Dados ---");

        try (Connection conn = getConnection()) {
            // System.out.println("✅ Conexão estabelecida com sucesso!"); // <-- COMENTADO (Opcional)

            // 1. Ler e Imprimir RAILWAY_LINE
            System.out.println("\n" + "=".repeat(60));
            System.out.println("   Tabela: RAILWAY_LINE");
            System.out.println("=".repeat(60));
            String lineSql = "SELECT line_id, name, owner_id, start_facility_id, end_facility_id, gauge FROM RAILWAY_LINE ORDER BY line_id";
            int lineCount = 0;
            try (PreparedStatement stmt = conn.prepareStatement(lineSql);
                 ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) { /* ... (impressão dos dados) ... */
                    int lineId = rs.getInt("line_id");
                    String name = rs.getString("name");
                    String ownerId = rs.getString("owner_id");
                    int startFacilityId = rs.getInt("start_facility_id");
                    int endFacilityId = rs.getInt("end_facility_id");
                    int gauge = rs.getInt("gauge");
                    System.out.printf("   -> LinhaID: %d | Nome: %s | Dono: %s | Facilidades: %d <-> %d | Bitola: %d\n",
                            lineId, name, ownerId, startFacilityId, endFacilityId, gauge);
                    lineCount++;
                }
                if (lineCount == 0) System.out.println("   -> Tabela RAILWAY_LINE está vazia ou não foi encontrada.");
                // else System.out.println("   (Total: " + lineCount + " linhas)"); // <-- COMENTADO (Opcional)
            } catch (SQLException e) { System.err.println("   ❌ Erro ao ler RAILWAY_LINE: " + e.getMessage()); }

            // 2. Ler e Imprimir LINE_SEGMENT
            System.out.println("\n" + "=".repeat(80));
            System.out.println("   Tabela: LINE_SEGMENT");
            System.out.println("=".repeat(80));
            String segmentSql = "SELECT segment_id, line_id, segment_order, electrified, max_weight_kg_m, length_m, number_tracks " +
                    "FROM LINE_SEGMENT ORDER BY line_id, segment_order";
            int segmentCount = 0;
            try (PreparedStatement stmt = conn.prepareStatement(segmentSql);
                 ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) { /* ... (impressão dos dados) ... */
                    int segId = rs.getInt("segment_id");
                    int lineId = rs.getInt("line_id");
                    int segOrder = rs.getInt("segment_order");
                    String electrified = rs.getString("electrified");
                    double maxWeight = rs.getDouble("max_weight_kg_m");
                    double lengthM = rs.getDouble("length_m");
                    int numTracks = rs.getInt("number_tracks");
                    System.out.printf("   -> SegID: %d | LinhaID: %d | Ordem: %d | Eletr: %s | PesoMax: %.0f kg/m | Compr: %.1f m | Vias: %d\n",
                            segId, lineId, segOrder, electrified, maxWeight, lengthM, numTracks);
                    segmentCount++;
                }
                if (segmentCount == 0) System.out.println("   -> Tabela LINE_SEGMENT está vazia ou não foi encontrada.");
                // else System.out.println("   (Total: " + segmentCount + " segmentos)"); // <-- COMENTADO (Opcional)
            } catch (SQLException e) { System.err.println("   ❌ Erro ao ler LINE_SEGMENT: " + e.getMessage()); }

            // 3. Ler e Imprimir ROLLING_STOCK (Locomotivas)
            System.out.println("\n" + "=".repeat(70));
            System.out.println("   Tabela: ROLLING_STOCK (Locomotivas)");
            System.out.println("=".repeat(70));
            String locoSql = "SELECT stock_id, operator_id, name, make, model, service_year, type, max_speed " +
                    "FROM ROLLING_STOCK WHERE type IN ('Electric', 'Diesel') ORDER BY stock_id";
            int locoCount = 0;
            try (PreparedStatement stmt = conn.prepareStatement(locoSql);
                 ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) { /* ... (impressão dos dados) ... */
                    String stockId = rs.getString("stock_id");
                    String operatorId = rs.getString("operator_id");
                    String name = rs.getString("name");
                    String make = rs.getString("make");
                    String model = rs.getString("model");
                    int serviceYear = rs.getInt("service_year");
                    String type = rs.getString("type");
                    double maxSpeed = rs.getDouble("max_speed");
                    System.out.printf("   -> ID: %s | Op: %s | Nome: %s | Fab: %s | Modelo: %s | Ano: %d | Tipo: %s | VelMax: %.1f\n",
                            stockId, operatorId, name, make, model, serviceYear, type, maxSpeed);
                    locoCount++;
                }
                if (locoCount == 0) System.out.println("   -> Nenhuma locomotiva encontrada.");
                // else System.out.println("   (Total: " + locoCount + " locomotivas)"); // <-- COMENTADO (Opcional)
            } catch (SQLException e) { System.err.println("   ❌ Erro ao ler ROLLING_STOCK: " + e.getMessage()); }

        } catch (SQLException e) { System.err.println("❌ FALHA GERAL NA CONEXÃO ou OPERAÇÃO DB: " + e.getMessage());
        } catch (Exception e) { System.err.println("❌ Erro inesperado durante a leitura: " + e.getMessage()); e.printStackTrace();}

        // System.out.println("\n----------------------------------------"); // <-- COMENTADO (Opcional)
        // System.out.println("Leitura de dados concluída."); // <-- COMENTADO
    }

    /**
     * Método main para executar a leitura dos dados selecionados.
     */
    public static void main(String[] args) {
        printSelectedDatabaseData();
    }
}
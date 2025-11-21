package pt.ipp.isep.dei.DatabaseConnection;

import java.sql.*;

public class DatabaseConnection {

    // --- Connection Details (Ensure these are correct) ---
    private static final String DB_URL = "jdbc:oracle:thin:@vsgate-s1.dei.isep.ipp.pt:10221:xe";
    private static final String DB_USER = "system";
    private static final String DB_PASSWORD = "oracle"; // <-- CHANGE TO CORRECT PASSWORD
    // --- End of Details ---

    static {
        try {
            Class.forName("oracle.jdbc.driver.OracleDriver");
        } catch (ClassNotFoundException e) {
            System.err.println("❌ FATAL ERROR: Oracle JDBC Driver not found in classpath!");
            System.err.println("   Verify that the Maven 'ojdbc' dependency is correctly included in pom.xml.");
            System.exit(1);
        }
    }

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
    }

    /**
     * Extracts and prints data from literally all major tables defined in the DDL.
     */
    public static void printSelectedDatabaseData() {
        System.out.println("\n\n############################################################");
        System.out.println("### FULL DATABASE EXTRACTION (USBD04 DML Verification) ###");
        System.out.println("############################################################");

        try (Connection conn = getConnection()) {

            // =========================================================================
            // 1. OPERATOR (Tabela Mestra)
            // =========================================================================
            printSectionTitle("OPERATOR");
            String sql1 = "SELECT operator_id, name FROM OPERATOR ORDER BY operator_id";
            try (PreparedStatement stmt = conn.prepareStatement(sql1);
                 ResultSet rs = stmt.executeQuery()) {
                int count = 0;
                while (rs.next()) {
                    System.out.printf("   -> ID: %s | Name: %s\n", rs.getString(1), rs.getString(2));
                    count++;
                }
                if (count == 0) System.out.println("   -> OPERATOR table is empty.");
            }

            // =========================================================================
            // 2. STATION (Pontos Geográficos)
            // =========================================================================
            printSectionTitle("STATION (Pontos Geográficos)");
            String sql2 = "SELECT station_id, name, latitude, longitude FROM STATION ORDER BY station_id";
            try (PreparedStatement stmt = conn.prepareStatement(sql2);
                 ResultSet rs = stmt.executeQuery()) {
                int count = 0;
                while (rs.next()) {
                    System.out.printf("   -> ID: %s | Name: %s | Coords: %.6f, %.6f\n",
                            rs.getString(1), rs.getString(2), rs.getDouble(3), rs.getDouble(4));
                    count++;
                }
                if (count == 0) System.out.println("   -> STATION table is empty.");
            }

            // =========================================================================
            // 2.1 FACILITY (Pontos de Carga/Descarga) <--- NOVO
            // =========================================================================
            printSectionTitle("FACILITY (Pontos de Carga/Descarga)");
            String sql2_1 = "SELECT F.facility_id, F.name AS facility_name, S.name AS station_name " +
                    "FROM FACILITY F JOIN STATION S ON F.station_id = S.station_id " +
                    "ORDER BY F.facility_id";
            try (PreparedStatement stmt = conn.prepareStatement(sql2_1);
                 ResultSet rs = stmt.executeQuery()) {
                int count = 0;
                while (rs.next()) {
                    System.out.printf("   -> ID: %d | Name: %s | Station: %s\n", rs.getInt(1), rs.getString(2), rs.getString(3));
                    count++;
                }
                if (count == 0) System.out.println("   -> FACILITY table is empty.");
            }


            // =========================================================================
            // 3. RAILWAY_LINE (Com proprietário)
            // =========================================================================
            printSectionTitle("RAILWAY_LINE");
            String sql3 = "SELECT RL.line_id, RL.name AS line_name, O.name AS owner_name " +
                    "FROM RAILWAY_LINE RL JOIN OPERATOR O ON RL.owner_operator_id = O.operator_id " +
                    "ORDER BY RL.line_id";
            try (PreparedStatement stmt = conn.prepareStatement(sql3);
                 ResultSet rs = stmt.executeQuery()) {
                int count = 0;
                while (rs.next()) {
                    System.out.printf("   -> LineID: %s | Name: %s | Owner: %s\n",
                            rs.getString(1), rs.getString(2), rs.getString(3));
                    count++;
                }
                if (count == 0) System.out.println("   -> RAILWAY_LINE table is empty.");
            }

            // =========================================================================
            // 4. LINE_SEGMENT (Detalhe da Linha)
            // =========================================================================
            printSectionTitle("LINE_SEGMENT (Detalhes Técnicos)");
            String sql4 = "SELECT segment_id, line_id, segment_order, is_electrified, max_weight_kg_m, length_m, number_tracks, siding_position, siding_length " +
                    "FROM LINE_SEGMENT ORDER BY line_id, segment_order";
            try (PreparedStatement stmt = conn.prepareStatement(sql4);
                 ResultSet rs = stmt.executeQuery()) {
                int count = 0;
                while (rs.next()) {
                    // Melhoria na leitura de campos nullable (siding_position e siding_length)
                    String sidingInfo;
                    rs.getInt(8);
                    if (rs.wasNull()) {
                        sidingInfo = "N/A";
                    } else {
                        sidingInfo = String.format("%d (%.0f m)", rs.getInt(8), rs.getDouble(9));
                    }

                    System.out.printf("   -> SegID: %s | Line: %s | Order: %d | Electr: %s | MaxWeight: %.0f kg/m | Length: %.1f m | Tracks: %d | Siding: %s\n",
                            rs.getString(1), rs.getString(2), rs.getInt(3), rs.getString(4), rs.getDouble(5), rs.getDouble(6), rs.getInt(7), sidingInfo);
                    count++;
                }
                if (count == 0) System.out.println("   -> LINE_SEGMENT table is empty.");
            }

            // =========================================================================
            // 5. LOCOMOTIVE (Com potência e modelo)
            // =========================================================================
            printSectionTitle("LOCOMOTIVE (Física e Modelo)");
            String sql5 = "SELECT L.stock_id, L.locomotive_type, L.power_kw, R.model, R.gauge_mm, O.name AS operator_name " +
                    "FROM LOCOMOTIVE L " +
                    "JOIN ROLLING_STOCK R ON L.stock_id = R.stock_id " +
                    "JOIN OPERATOR O ON R.operator_id = O.operator_id " +
                    "ORDER BY L.stock_id";
            try (PreparedStatement stmt = conn.prepareStatement(sql5);
                 ResultSet rs = stmt.executeQuery()) {
                int count = 0;
                while (rs.next()) {
                    System.out.printf("   -> ID: %s | Type: %s | Power: %.0f kW | Model: %s | Gauge: %.0f mm | Operator: %s\n",
                            rs.getString(1), rs.getString(2), rs.getDouble(3), rs.getString(4), rs.getDouble(5), rs.getString(6));
                    count++;
                }
                if (count == 0) System.out.println("   -> LOCOMOTIVE table is empty.");
            }

            // =========================================================================
            // 6. WAGON_MODEL (Definições) <--- CORRIGIDO
            // =========================================================================
            printSectionTitle("WAGON_MODEL (Definições)");
            // A consulta foi alterada para refletir as colunas inseridas pelo seu script DML:
            // (model_id, model_name, maker, wagon_type, gauge_mm)
            String sql6 = "SELECT model_id, model_name, maker, wagon_type, gauge_mm FROM WAGON_MODEL ORDER BY model_id";
            try (PreparedStatement stmt = conn.prepareStatement(sql6);
                 ResultSet rs = stmt.executeQuery()) {
                int count = 0;
                while (rs.next()) {
                    System.out.printf("   -> ID: %d | Name: %s | Maker: %s | Type: %s | Gauge: %.0f mm\n",
                            rs.getInt(1), rs.getString(2), rs.getString(3), rs.getString(4), rs.getDouble(5));
                    count++;
                }
                if (count == 0) System.out.println("   -> WAGON_MODEL table is empty.");
            }

            // =========================================================================
            // 7. WAGON (Stock de Vagões)
            // =========================================================================
            printSectionTitle("WAGON (Estoque Operacional)");
            String sql7 = "SELECT W.stock_id, WM.model_name, W.service_year, O.name AS operator_name " +
                    "FROM WAGON W JOIN ROLLING_STOCK R ON W.stock_id = R.stock_id " +
                    "JOIN WAGON_MODEL WM ON W.model_id = WM.model_id " +
                    "JOIN OPERATOR O ON R.operator_id = O.operator_id ORDER BY W.stock_id";
            try (PreparedStatement stmt = conn.prepareStatement(sql7);
                 ResultSet rs = stmt.executeQuery()) {
                int count = 0;
                while (rs.next()) {
                    System.out.printf("   -> ID: %s | Model: %s | Year: %d | Operator: %s\n",
                            rs.getString(1), rs.getString(2), rs.getInt(3), rs.getString(4));
                    count++;
                }
                if (count == 0) System.out.println("   -> WAGON table is empty.");
            }

            // =========================================================================
            // 8. TRAIN (Viagens Agendadas)
            // =========================================================================
            printSectionTitle("TRAIN (Viagens Agendadas)");
            String sql8 = "SELECT T.train_id, O.name AS operator_name, T.train_date, T.train_time, T.locomotive_id, F1.name AS start_facility " +
                    "FROM TRAIN T JOIN OPERATOR O ON T.operator_id = O.operator_id " +
                    "JOIN FACILITY F1 ON T.start_facility_id = F1.facility_id ORDER BY T.train_id";
            try (PreparedStatement stmt = conn.prepareStatement(sql8);
                 ResultSet rs = stmt.executeQuery()) {
                int count = 0;
                while (rs.next()) {
                    System.out.printf("   -> Train: %s | Op: %s | Date/Time: %s %s | Loco: %s | From: %s\n",
                            rs.getString(1), rs.getString(2), rs.getDate(3), rs.getString(4), rs.getString(5), rs.getString(6));
                    count++;
                }
                if (count == 0) System.out.println("   -> TRAIN table is empty.");
            }

            // =========================================================================
            // 9. TRAIN_WAGON_USAGE (Uso de Vagões em Comboios)
            // =========================================================================
            printSectionTitle("TRAIN_WAGON_USAGE (Relações N:M)");
            String sql9 = "SELECT TWU.usage_id, TWU.train_id, TWU.wagon_id, TWU.usage_date " +
                    "FROM TRAIN_WAGON_USAGE TWU ORDER BY TWU.usage_date DESC";
            try (PreparedStatement stmt = conn.prepareStatement(sql9);
                 ResultSet rs = stmt.executeQuery()) {
                int count = 0;
                while (rs.next()) {
                    System.out.printf("   -> UsageID: %s | Train: %s | Wagon: %s | Date: %s\n",
                            rs.getString(1), rs.getString(2), rs.getString(3), rs.getDate(4));
                    count++;
                }
                if (count == 0) System.out.println("   -> TRAIN_WAGON_USAGE table is empty.");
            }

        } catch (SQLException e) {
            System.err.println("❌ FALHA GERAL NA CONEXÃO OU OPERAÇÃO DE DB: Verifique as credenciais e se as tabelas foram criadas. " + e.getMessage());
        } catch (Exception e) {
            System.err.println("❌ Erro inesperado durante a leitura da base de dados: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // Método helper para formatação
    private static void printSectionTitle(String title) {
        System.out.println("\n" + "#".repeat(20) + " " + title + " " + "#".repeat(20));
    }

    public static void main(String[] args) {
        printSelectedDatabaseData();
    }
}
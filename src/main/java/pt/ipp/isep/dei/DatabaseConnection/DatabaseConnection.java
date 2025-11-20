package pt.ipp.isep.dei.DatabaseConnection;

import java.sql.*;

/**
 * Utility class for managing database connections and reading selected tables.
 * <p>
 * This class provides methods to obtain JDBC connections to the Oracle database
 * and to read data from specific tables, such as {@code RAILWAY_LINE}, {@code LINE_SEGMENT},
 * and {@code ROLLING_STOCK} (locomotives). It also includes a {@code main} method
 * for testing or demonstration purposes.
 * <p>
 * IMPORTANT: Users of {@link #getConnection()} are responsible for closing the connection.
 */

public class DatabaseConnection {

    // --- Connection Details (Ensure these are correct) ---
    private static final String DB_URL = "jdbc:oracle:thin:@vsgate-s1.dei.isep.ipp.pt:10945:xe";
    private static final String DB_USER = "system"; // Verify if correct
    private static final String DB_PASSWORD = "lapr"; // <-- CHANGE TO CORRECT PASSWORD
    // --- End of Details ---

    // Load Oracle JDBC driver
    static {
        try {
            Class.forName("oracle.jdbc.driver.OracleDriver");
        } catch (ClassNotFoundException e) {
            System.err.println("❌ FATAL ERROR: Oracle JDBC Driver not found in classpath!");
            System.err.println("   Verify that the Maven 'ojdbc' dependency is correctly included in pom.xml.");
            System.exit(1); // Exit if driver fails to load
        }
    }

    /**
     * Obtains a new connection to the database.
     * <p>
     * The caller is responsible for closing the connection to prevent resource leaks.
     *
     * @return a new {@link Connection} object
     * @throws SQLException if a database access error occurs
     */
    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
    }

    /**
     * Connects to the database and prints selected data from the tables:
     * {@code RAILWAY_LINE}, {@code LINE_SEGMENT}, and {@code ROLLING_STOCK}.
     * <p>
     * This method is mainly intended for testing, debugging, or inspection purposes.
     */
    public static void printSelectedDatabaseData() {
        System.out.println("\n--- Reading Database Data ---");

        try (Connection conn = getConnection()) {

            // 1. Print RAILWAY_LINE
            System.out.println("\n" + "=".repeat(60));
            System.out.println("   Table: RAILWAY_LINE");
            System.out.println("=".repeat(60));
            String lineSql = "SELECT line_id, name, owner_id, start_facility_id, end_facility_id, gauge " +
                    "FROM RAILWAY_LINE ORDER BY line_id";
            int lineCount = 0;
            try (PreparedStatement stmt = conn.prepareStatement(lineSql);
                 ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    int lineId = rs.getInt("line_id");
                    String name = rs.getString("name");
                    String ownerId = rs.getString("owner_id");
                    int startFacilityId = rs.getInt("start_facility_id");
                    int endFacilityId = rs.getInt("end_facility_id");
                    int gauge = rs.getInt("gauge");
                    System.out.printf("   -> LineID: %d | Name: %s | Owner: %s | Facilities: %d <-> %d | Gauge: %d\n",
                            lineId, name, ownerId, startFacilityId, endFacilityId, gauge);
                    lineCount++;
                }
                if (lineCount == 0) System.out.println("   -> RAILWAY_LINE table is empty or not found.");
            } catch (SQLException e) {
                System.err.println("   ❌ Error reading RAILWAY_LINE: " + e.getMessage());
            }

            // 2. Print LINE_SEGMENT
            System.out.println("\n" + "=".repeat(80));
            System.out.println("   Table: LINE_SEGMENT");
            System.out.println("=".repeat(80));
            String segmentSql = "SELECT segment_id, line_id, segment_order, electrified, max_weight_kg_m, length_m, number_tracks " +
                    "FROM LINE_SEGMENT ORDER BY line_id, segment_order";
            int segmentCount = 0;
            try (PreparedStatement stmt = conn.prepareStatement(segmentSql);
                 ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    int segId = rs.getInt("segment_id");
                    int lineId = rs.getInt("line_id");
                    int segOrder = rs.getInt("segment_order");
                    String electrified = rs.getString("electrified");
                    double maxWeight = rs.getDouble("max_weight_kg_m");
                    double lengthM = rs.getDouble("length_m");
                    int numTracks = rs.getInt("number_tracks");
                    System.out.printf("   -> SegID: %d | LineID: %d | Order: %d | Electr: %s | MaxWeight: %.0f kg/m | Length: %.1f m | Tracks: %d\n",
                            segId, lineId, segOrder, electrified, maxWeight, lengthM, numTracks);
                    segmentCount++;
                }
                if (segmentCount == 0) System.out.println("   -> LINE_SEGMENT table is empty or not found.");
            } catch (SQLException e) {
                System.err.println("   ❌ Error reading LINE_SEGMENT: " + e.getMessage());
            }

            // 3. Print ROLLING_STOCK (Locomotives)
            System.out.println("\n" + "=".repeat(70));
            System.out.println("   Table: ROLLING_STOCK (Locomotives)");
            System.out.println("=".repeat(70));
            String locoSql = "SELECT stock_id, operator_id, name, make, model, service_year, type, max_speed " +
                    "FROM ROLLING_STOCK WHERE type IN ('Electric', 'Diesel') ORDER BY stock_id";
            int locoCount = 0;
            try (PreparedStatement stmt = conn.prepareStatement(locoSql);
                 ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    String stockId = rs.getString("stock_id");
                    String operatorId = rs.getString("operator_id");
                    String name = rs.getString("name");
                    String make = rs.getString("make");
                    String model = rs.getString("model");
                    int serviceYear = rs.getInt("service_year");
                    String type = rs.getString("type");
                    double maxSpeed = rs.getDouble("max_speed");
                    System.out.printf("   -> ID: %s | Op: %s | Name: %s | Make: %s | Model: %s | Year: %d | Type: %s | MaxSpeed: %.1f\n",
                            stockId, operatorId, name, make, model, serviceYear, type, maxSpeed);
                    locoCount++;
                }
                if (locoCount == 0) System.out.println("   -> No locomotives found.");
            } catch (SQLException e) {
                System.err.println("   ❌ Error reading ROLLING_STOCK: " + e.getMessage());
            }

        } catch (SQLException e) {
            System.err.println("❌ GENERAL FAILURE IN DB CONNECTION OR OPERATION: " + e.getMessage());
        } catch (Exception e) {
            System.err.println("❌ Unexpected error during database read: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Main method to execute the database read for demonstration purposes.
     *
     * @param args command-line arguments (ignored)
     */
    public static void main(String[] args) {
        printSelectedDatabaseData();
    }
}

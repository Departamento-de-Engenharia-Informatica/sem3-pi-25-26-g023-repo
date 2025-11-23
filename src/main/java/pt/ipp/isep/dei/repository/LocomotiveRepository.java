package pt.ipp.isep.dei.repository;

import pt.ipp.isep.dei.DatabaseConnection.DatabaseConnection;
import pt.ipp.isep.dei.domain.Locomotive;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.sql.*;

/**
 * Repository class responsible for data access operations (CRUD) related to the {@link Locomotive} entity.
 * It queries the LOCOMOTIVE and ROLLING_STOCK tables in the database.
 */
public class LocomotiveRepository {

    /**
     * Searches for a Locomotive by its ID (int).
     *
     * @param id The integer ID of the locomotive (maps to 'stock_id' VARCHAR in DB).
     * @return An {@link Optional} containing the Locomotive if found, or empty otherwise.
     */
    public Optional<Locomotive> findById(int id) {
        // ID is int in the method, but string in the DB ('stock_id').
        String idStr = String.valueOf(id);

        String sql = "SELECT R.stock_id, L.locomotive_type, L.power_kw, R.model " +
                "FROM LOCOMOTIVE L JOIN ROLLING_STOCK R ON L.stock_id = R.stock_id " +
                "WHERE R.stock_id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, idStr); // Use setString for the VARCHAR2 stock_id

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    // Mapping: rs.getInt("stock_id") works if the value is numeric
                    return Optional.of(new Locomotive(
                            rs.getInt("stock_id"),
                            rs.getString("model"),
                            rs.getString("locomotive_type"), // <--- CORRECTION: Use the correct column name
                            rs.getDouble("power_kw")
                    ));
                }
            }
        } catch (SQLException e) {
            System.err.println("❌ Error retrieving Locomotive (Rolling Stock) by ID " + id + ": " + e.getMessage());
        }
        return Optional.empty();
    }

    /**
     * Returns all Locomotives from the database.
     *
     * @return A list of {@link Locomotive} objects.
     */
    public List<Locomotive> findAll() {
        List<Locomotive> locomotives = new ArrayList<>();
        String sql = "SELECT R.stock_id, L.locomotive_type, L.power_kw, R.model " +
                "FROM LOCOMOTIVE L JOIN ROLLING_STOCK R ON L.stock_id = R.stock_id " +
                "ORDER BY R.stock_id";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                try {
                    locomotives.add(new Locomotive(
                            rs.getInt("stock_id"),
                            rs.getString("model"),
                            rs.getString("locomotive_type"),
                            rs.getDouble("power_kw")
                    ));
                } catch (SQLException e) {
                    System.err.println("❌ Data error in Locomotive row: " + e.getMessage());
                }
            }
        } catch (SQLException e) {
            System.err.println("❌ Fatal error reading Locomotive data: " + e.getMessage());
        }
        return locomotives;
    }
}
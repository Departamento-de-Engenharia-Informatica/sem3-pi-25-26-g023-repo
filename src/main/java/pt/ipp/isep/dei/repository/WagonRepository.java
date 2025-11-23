package pt.ipp.isep.dei.repository;

import pt.ipp.isep.dei.DatabaseConnection.DatabaseConnection;
import pt.ipp.isep.dei.domain.Wagon;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.sql.*;

/**
 * Repository class responsible for data access operations (CRUD) related to the {@link Wagon} entity.
 * It queries the WAGON and ROLLING_STOCK tables in the database.
 */
public class WagonRepository {

    /**
     * Searches for a Wagon by its ID (String).
     *
     * @param id The String ID of the wagon (maps to 'stock_id' VARCHAR in DB).
     * @return An {@link Optional} containing the Wagon if found, or empty otherwise.
     */
    public Optional<Wagon> findById(String id) {
        String sql = "SELECT R.stock_id, W.model_id, W.service_year " +
                "FROM WAGON W JOIN ROLLING_STOCK R ON W.stock_id = R.stock_id " +
                "WHERE R.stock_id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, id);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    // Wagon constructor corrected for String ID
                    return Optional.of(new Wagon(
                            rs.getString("stock_id"),
                            rs.getInt("model_id"),
                            rs.getInt("service_year")
                    ));
                }
            }
        } catch (SQLException e) {
            System.err.println("❌ Error retrieving Wagon by ID " + id + ": " + e.getMessage());
        }
        return Optional.empty();
    }

    /**
     * Returns all Wagons from the database.
     *
     * @return A list of all {@link Wagon} objects.
     */
    public List<Wagon> findAll() {
        List<Wagon> wagons = new ArrayList<>();
        String sql = "SELECT R.stock_id, W.model_id, W.service_year FROM WAGON W JOIN ROLLING_STOCK R ON W.stock_id = R.stock_id ORDER BY R.stock_id";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                try {
                    wagons.add(new Wagon(
                            rs.getString("stock_id"),
                            rs.getInt("model_id"),
                            rs.getInt("service_year")
                    ));
                } catch (SQLException e) {
                    System.err.println("❌ Data error in Wagon row: " + e.getMessage());
                }
            }
        } catch (SQLException e) {
            System.err.println("❌ Fatal error reading Wagon data: " + e.getMessage());
        }
        return wagons;
    }
}
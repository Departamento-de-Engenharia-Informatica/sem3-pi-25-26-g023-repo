package pt.ipp.isep.dei.repository;

import pt.ipp.isep.dei.DatabaseConnection.DatabaseConnection;
import pt.ipp.isep.dei.domain.Wagon;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.sql.*;

public class WagonRepository {

    /** Busca um Wagon pelo seu ID (String). */
    public Optional<Wagon> findById(String id) {
        String sql = "SELECT R.stock_id, W.model_id, W.service_year " +
                "FROM WAGON W JOIN ROLLING_STOCK R ON W.stock_id = R.stock_id " +
                "WHERE R.stock_id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, id);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    // Construtor Wagon corrigido para String ID
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

    /** Retorna todos os Vagões. */
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
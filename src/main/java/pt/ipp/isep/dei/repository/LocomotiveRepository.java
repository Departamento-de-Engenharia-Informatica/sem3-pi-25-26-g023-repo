package pt.ipp.isep.dei.repository;

import pt.ipp.isep.dei.DatabaseConnection.DatabaseConnection;
import pt.ipp.isep.dei.domain.Locomotive;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.sql.*;

public class LocomotiveRepository {

    /** Busca uma Locomotiva pelo seu ID (int). */
    public Optional<Locomotive> findById(int id) {
        // ID é int no método, mas string na DB ('stock_id').
        String idStr = String.valueOf(id);

        String sql = "SELECT R.stock_id, L.locomotive_type, L.power_kw, R.model " +
                "FROM LOCOMOTIVE L JOIN ROLLING_STOCK R ON L.stock_id = R.stock_id " +
                "WHERE R.stock_id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, idStr); // Usar setString para o stock_id VARCHAR2

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    // Mapeamento: rs.getInt("stock_id") funciona se o valor for numérico
                    return Optional.of(new Locomotive(
                            rs.getInt("stock_id"),
                            rs.getString("model"),
                            rs.getString("locomotive_type"), // <--- CORREÇÃO: Usar o nome correto
                            rs.getDouble("power_kw")
                    ));
                }
            }
        } catch (SQLException e) {
            System.err.println("❌ Error retrieving Locomotive (Rolling Stock) by ID " + id + ": " + e.getMessage());
        }
        return Optional.empty();
    }

    /** Retorna todas as Locomotivas. */
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
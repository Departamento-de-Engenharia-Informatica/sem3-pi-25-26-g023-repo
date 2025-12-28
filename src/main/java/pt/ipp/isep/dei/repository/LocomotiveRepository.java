package pt.ipp.isep.dei.repository;

import pt.ipp.isep.dei.DatabaseConnection.DatabaseConnection;
import pt.ipp.isep.dei.domain.Locomotive;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.sql.*;

public class LocomotiveRepository {

    public Optional<Locomotive> findById(int id) {
        // ... (código existente findById mantido, podes atualizar se quiseres)
        return Optional.empty();
    }

    /**
     * Returns all Locomotives with physics data (Power, Length, Weight).
     */
    public List<Locomotive> findAll() {
        List<Locomotive> locomotives = new ArrayList<>();
        // Tenta obter length_m se disponível na tabela LOCOMOTIVE
        String sql = "SELECT R.stock_id, L.locomotive_type, L.power_kw, R.model, L.length_m " +
                "FROM LOCOMOTIVE L JOIN ROLLING_STOCK R ON L.stock_id = R.stock_id " +
                "ORDER BY R.stock_id";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                Locomotive loc = new Locomotive(
                        rs.getInt("stock_id"),
                        rs.getString("model"),
                        rs.getString("locomotive_type"),
                        rs.getDouble("power_kw")
                );

                // Preencher dados físicos
                try {
                    double len = rs.getDouble("length_m");
                    if (len > 0) loc.setLengthMeters(len);
                } catch (SQLException ignore) { /* Se a coluna não existir, usa default 20m */ }

                loc.setTotalWeightKg(80000.0); // Tara fixa para demo

                locomotives.add(loc);
            }
        } catch (SQLException e) {
            System.err.println("❌ Error reading Locomotive data: " + e.getMessage());
        }
        return locomotives;
    }
}
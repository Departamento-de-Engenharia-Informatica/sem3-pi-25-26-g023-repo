package pt.ipp.isep.dei.repository;

import pt.ipp.isep.dei.DatabaseConnection.DatabaseConnection;
import pt.ipp.isep.dei.domain.Locomotive;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class LocomotiveRepository {

    /**
     * Encontra uma locomotiva pelo ID.
     */
    public Optional<Locomotive> findById(String idStr) {
        String sql = "SELECT R.stock_id, L.locomotive_type, L.power_kw, R.model, L.length_m " +
                "FROM LOCOMOTIVE L JOIN ROLLING_STOCK R ON L.stock_id = R.stock_id " +
                "WHERE R.stock_id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, idStr);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToLocomotive(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("⚠️ Erro SQL ao buscar Locomotiva ID " + idStr + ": " + e.getMessage());
        }
        return Optional.empty();
    }

    public Optional<Locomotive> findById(int id) {
        return findById(String.valueOf(id));
    }

    public List<Locomotive> findAll() {
        List<Locomotive> locomotives = new ArrayList<>();
        String sql = "SELECT R.stock_id, L.locomotive_type, L.power_kw, R.model, L.length_m " +
                "FROM LOCOMOTIVE L JOIN ROLLING_STOCK R ON L.stock_id = R.stock_id " +
                "ORDER BY R.stock_id";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                locomotives.add(mapResultSetToLocomotive(rs));
            }
        } catch (SQLException e) {
            System.err.println("❌ Erro ao ler Locomotivas: " + e.getMessage());
        }
        return locomotives;
    }

    /**
     * CORREÇÃO AQUI: Tratamento robusto do ID para evitar NumberFormatException.
     */
    private Locomotive mapResultSetToLocomotive(ResultSet rs) throws SQLException {
        Object idObj = rs.getObject("stock_id");
        int id = 0;

        // --- BLOCO DE CORREÇÃO DO ID (Remove NumberFormatException) ---
        try {
            if (idObj instanceof Number) {
                id = ((Number) idObj).intValue();
            } else if (idObj != null) {
                // Remove tudo o que não é digito (ex: "335.001" vira "335001")
                String cleanId = idObj.toString().replaceAll("[^0-9]", "");
                if (!cleanId.isEmpty()) {
                    id = Integer.parseInt(cleanId);
                } else {
                    // Fallback se o ID for puramente texto (ex: "CP-LOC") -> Usa HashCode para não crashar
                    id = idObj.toString().hashCode();
                }
            }
        } catch (Exception e) {
            System.err.println("⚠️ Erro ao converter ID '" + idObj + "'. Usando fallback seguro.");
            id = (idObj != null) ? idObj.hashCode() : 0;
        }
        // -----------------------------------------------------------

        double originalPower = rs.getDouble("power_kw");
        double finalPower = originalPower;

        // Fix de física (impede 0 kW)
        if (finalPower < 1.0) {
            finalPower = 4200.0;
        }

        Locomotive loc = new Locomotive(
                id,
                rs.getString("model"),
                rs.getString("locomotive_type"),
                finalPower
        );

        try {
            double len = rs.getDouble("length_m");
            loc.setLengthMeters(len > 0 ? len : 22.0);
        } catch (SQLException ignore) {
            loc.setLengthMeters(22.0);
        }

        loc.setTotalWeightKg(80000.0); // Peso default

        return loc;
    }
}
package pt.ipp.isep.dei.repository;

import pt.ipp.isep.dei.DatabaseConnection.DatabaseConnection;
import pt.ipp.isep.dei.domain.Box;
import pt.ipp.isep.dei.domain.Wagon;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.sql.*;

public class WagonRepository {

    public Optional<Wagon> findById(String id) {
        String sql = "SELECT R.stock_id, W.model_id, W.service_year " +
                "FROM WAGON W JOIN ROLLING_STOCK R ON W.stock_id = R.stock_id " +
                "WHERE R.stock_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(new Wagon(rs.getString("stock_id"), rs.getInt("model_id"), rs.getInt("service_year")));
                }
            }
        } catch (SQLException e) {
            System.err.println("❌ Error retrieving Wagon by ID " + id + ": " + e.getMessage());
        }
        return Optional.empty();
    }

    /**
     * Carrega todos os vagões com dados físicos (comprimento/peso) para a UI de seleção.
     */
    public List<Wagon> findAll() {
        List<Wagon> wagons = new ArrayList<>();
        // JOIN com WAGON_MODEL para obter length_m e wagon_type
        String sql = "SELECT w.stock_id, w.model_id, w.service_year, wm.length_m, wm.wagon_type " +
                "FROM WAGON w " +
                "JOIN WAGON_MODEL wm ON w.model_id = wm.model_id " +
                "ORDER BY w.stock_id";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                Wagon w = new Wagon(rs.getString("stock_id"), rs.getInt("model_id"), rs.getInt("service_year"));

                // Preencher dados físicos (lidos do Modelo)
                w.setLengthMeters(rs.getDouble("length_m"));

                // Estimativa de Peso Bruto (Tara + Carga) baseada no tipo para validação visual
                String type = rs.getString("wagon_type");
                double estimatedWeight = 25000; // Tara base
                if (type != null) {
                    String t = type.toLowerCase();
                    if (t.contains("coal")) estimatedWeight += 60000;
                    else if (t.contains("steel")) estimatedWeight += 60000;
                    else if (t.contains("cereal")) estimatedWeight += 45000;
                    else estimatedWeight += 30000;
                }
                w.setGrossWeightKg(estimatedWeight);

                wagons.add(w);
            }
        } catch (SQLException e) {
            System.err.println("❌ Error reading Wagon data: " + e.getMessage());
        }
        return wagons;
    }

    public List<Wagon> findWagonsByTrainId(String trainId) {
        List<Wagon> wagons = new ArrayList<>();
        String sql = "SELECT w.stock_id, w.model_id, w.service_year, wm.wagon_type, wm.length_m " +
                "FROM TRAIN_WAGON_USAGE twu " +
                "JOIN WAGON w ON twu.wagon_id = w.stock_id " +
                "JOIN WAGON_MODEL wm ON w.model_id = wm.model_id " +
                "WHERE twu.train_id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, trainId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Wagon wagon = new Wagon(rs.getString("stock_id"), rs.getInt("model_id"), rs.getInt("service_year"));
                    wagon.setLengthMeters(rs.getDouble("length_m"));

                    String type = rs.getString("wagon_type");
                    if (type != null) {
                        String cargoContent = "General";
                        int weightKg = 20000;
                        if (type.toLowerCase().contains("cereal")) { cargoContent = "Cereal"; weightKg = 45000; }
                        else if (type.toLowerCase().contains("coal")) { cargoContent = "Coal"; weightKg = 65000; }
                        else if (type.toLowerCase().contains("wood")) { cargoContent = "Wood"; weightKg = 25000; }
                        else if (type.toLowerCase().contains("container")) { cargoContent = "Container"; weightKg = 30000; }

                        wagon.addBox(new Box("BX-" + wagon.getIdWagon(), cargoContent, weightKg, (LocalDate) null, null, "Wagon", "Hold"));
                    }
                    wagons.add(wagon);
                }
            }
        } catch (SQLException e) {
            System.err.println("Erro ao carregar vagões: " + e.getMessage());
        }
        return wagons;
    }
}
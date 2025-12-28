package pt.ipp.isep.dei.repository;

import pt.ipp.isep.dei.DatabaseConnection.DatabaseConnection;
import pt.ipp.isep.dei.domain.Box;
import pt.ipp.isep.dei.domain.Wagon;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class WagonRepository {

    // ... [findById e findAll mantêm-se iguais - copia da versão anterior] ...
    public Optional<Wagon> findById(String id) { /* ... código anterior ... */ return Optional.empty(); }
    public List<Wagon> findAll() { /* ... código anterior ... */ return new ArrayList<>(); }

    /**
     * Busca vagões e gera Carga com PESO REALISTA (qty = kg) baseado no tipo.
     */
    public List<Wagon> findWagonsByTrainId(String trainId) {
        List<Wagon> wagons = new ArrayList<>();

        String sql = "SELECT w.stock_id, w.model_id, w.service_year, wm.wagon_type " +
                "FROM TRAIN_WAGON_USAGE twu " +
                "JOIN WAGON w ON twu.wagon_id = w.stock_id " +
                "JOIN WAGON_MODEL wm ON w.model_id = wm.model_id " +
                "WHERE twu.train_id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, trainId);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    String wagonId = rs.getString("stock_id");
                    int modelId = rs.getInt("model_id");
                    int serviceYear = rs.getInt("service_year");
                    String type = rs.getString("wagon_type");

                    Wagon wagon = new Wagon(wagonId, modelId, serviceYear);

                    // --- CÁLCULO DE CARGA DINÂMICA (Peso em KG) ---
                    if (type != null) {
                        String cargoContent = "General";
                        int weightKg = 20000; // Peso base

                        String t = type.toLowerCase();
                        if (t.contains("cereal") || t.contains("grain")) {
                            cargoContent = "Cereal"; weightKg = 45000; // 45 toneladas
                        } else if (t.contains("coal")) {
                            cargoContent = "Coal"; weightKg = 65000;   // 65 toneladas (Pesado!)
                        } else if (t.contains("wood")) {
                            cargoContent = "Wood"; weightKg = 25000;   // 25 toneladas (Leve)
                        } else if (t.contains("steel") || t.contains("metal")) {
                            cargoContent = "Steel"; weightKg = 60000;
                        } else if (t.contains("container")) {
                            cargoContent = "Container"; weightKg = 30000; // Média
                        }

                        // Criar caixa onde qtyAvailable = PESO EM KG
                        wagon.addBox(new Box("BX-" + wagonId, cargoContent, weightKg, (LocalDate) null, null, "Wagon", "Hold"));
                    }
                    wagons.add(wagon);
                }
            }
        } catch (SQLException e) {
            System.err.println("Erro SQL WagonRepository: " + e.getMessage());
        }
        return wagons;
    }
}
package pt.ipp.isep.dei.repository;

import pt.ipp.isep.dei.DatabaseConnection.DatabaseConnection;
import pt.ipp.isep.dei.domain.Train;

import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class TrainRepository {

    /**
     * Procura todos os comboios na base de dados e adiciona um conjunto de mocks para
     * forçar múltiplos conflitos de via única no agendamento.
     */
    public List<Train> findAll() {
        List<Train> trains = new ArrayList<>();
        String sql = "SELECT train_id, operator_id, train_date, train_time, start_facility_id, end_facility_id, locomotive_id, route_id " +
                "FROM TRAIN ORDER BY train_id";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                try {
                    String trainId = rs.getString("train_id");
                    String operatorId = rs.getString("operator_id");
                    String locoId = rs.getString("locomotive_id");
                    String routeId = rs.getString("route_id");

                    int startFacilityId = rs.getInt("start_facility_id");
                    int endFacilityId = rs.getInt("end_facility_id");

                    Date date = rs.getDate("train_date");
                    // O campo train_time é lido como String (ou Time/Timestamp e depois convertido)
                    String timeStr = rs.getString("train_time");

                    LocalTime time = LocalTime.parse(timeStr.substring(0, 8));
                    LocalDateTime departureTime = date.toLocalDate().atTime(time);

                    trains.add(new Train(trainId, operatorId, departureTime, startFacilityId, endFacilityId, locoId, routeId));
                } catch (Exception e) {
                    System.err.println("❌ Erro de tipagem ao ler Train: " + e.getMessage());
                }
            }
        } catch (SQLException e) {
            System.err.println("❌ Erro fatal ao ler tabela TRAIN: " + e.getMessage());
        }

        // --- INJEÇÃO DE DADOS MOCK PARA CONFLITOS EXTREMOS (20 COMBOIOS) ---
        // Este bloco simula uma forte concentração de tráfego de 20 comboios no dia 6 de Outubro.

        List<String> mockIds = new ArrayList<>();
        for (int k = 60; k < 80; k++) {
            mockIds.add("54" + k);
        }
        // Assegura a remoção de IDs duplicados se existirem (embora não devessem).
        trains.removeIf(t -> mockIds.contains(t.getTrainId()));

        LocalDateTime baseDate = LocalDateTime.of(2025, 10, 6, 0, 0, 0);

        // CONFIGURAÇÕES DE ROTA E MATERIAL
        int facilityLeixoes = 50; // L
        int facilityValenca = 11; // V
        String locoFast = "5621"; // Electric, 5600kW (MAIOR VMAX)
        String locoSlow = "5034"; // Diesel, 3178kW (MENOR VMAX)
        String routeLV = "R001"; // Leixões -> Valença (Prioritário no L->V vs V->L)
        String routeVL = "R002"; // Valença -> Leixões (Atrasado no L->V vs V->L)

        // =================================================================
        // GRUPO 1: Conflitos "Head-On" Iniciais e Cascata (08:00 - 09:35)
        // =================================================================

        // 1. T5460 (L -> V, 08:00, SLOW) - Prioritário.
        trains.add(new Train("5460", "MEDWAY", baseDate.with(LocalTime.of(8, 0, 0)),
                facilityLeixoes, facilityValenca, locoSlow, routeLV));

        // 2. T5461 (V -> L, 08:20, FAST) - Colide com T5460 (e terá de ser atrasado).
        trains.add(new Train("5461", "CAPTRAIN", baseDate.with(LocalTime.of(8, 20, 0)),
                facilityValenca, facilityLeixoes, locoFast, routeVL));

        // 3. T5462 (L -> V, 08:15, SLOW) - Prioritário (em relação a T5463, T5461 recalculado, etc).
        trains.add(new Train("5462", "MEDWAY", baseDate.with(LocalTime.of(8, 15, 0)),
                facilityLeixoes, facilityValenca, locoSlow, routeLV));

        // 4. T5463 (V -> L, 08:40, FAST) - Colide com T5462 e T5460 (recalculado).
        trains.add(new Train("5463", "CAPTRAIN", baseDate.with(LocalTime.of(8, 40, 0)),
                facilityValenca, facilityLeixoes, locoFast, routeVL));

        // 5. T5464 (L -> V, 08:45, FAST) - Prioritário.
        trains.add(new Train("5464", "MEDWAY", baseDate.with(LocalTime.of(8, 45, 0)),
                facilityLeixoes, facilityValenca, locoFast, routeLV));

        // 6. T5465 (V -> L, 08:50, SLOW) - Colide com T5464.
        trains.add(new Train("5465", "CAPTRAIN", baseDate.with(LocalTime.of(8, 50, 0)),
                facilityValenca, facilityLeixoes, locoSlow, routeVL));

        // 7. T5466 (L -> V, 09:00, SLOW) - Prioritário.
        trains.add(new Train("5466", "MEDWAY", baseDate.with(LocalTime.of(9, 0, 0)),
                facilityLeixoes, facilityValenca, locoSlow, routeLV));

        // 8. T5467 (V -> L, 09:10, FAST) - Colide com T5466.
        trains.add(new Train("5467", "CAPTRAIN", baseDate.with(LocalTime.of(9, 10, 0)),
                facilityValenca, facilityLeixoes, locoFast, routeVL));

        // 9. T5468 (L -> V, 09:30, FAST) - Prioritário.
        trains.add(new Train("5468", "MEDWAY", baseDate.with(LocalTime.of(9, 30, 0)),
                facilityLeixoes, facilityValenca, locoFast, routeLV));

        // 10. T5469 (V -> L, 09:35, SLOW) - Colide com T5468.
        trains.add(new Train("5469", "CAPTRAIN", baseDate.with(LocalTime.of(9, 35, 0)),
                facilityValenca, facilityLeixoes, locoSlow, routeVL));

        // =================================================================
        // GRUPO 2: Conflitos Intensos e Concentrados (10:00 - 11:30)
        // =================================================================

        // 11. T5470 (L -> V, 10:00, SLOW) - Prioritário.
        trains.add(new Train("5470", "MEDWAY", baseDate.with(LocalTime.of(10, 0, 0)),
                facilityLeixoes, facilityValenca, locoSlow, routeLV));

        // 12. T5471 (V -> L, 10:10, SLOW) - Colide com T5470.
        trains.add(new Train("5471", "CAPTRAIN", baseDate.with(LocalTime.of(10, 10, 0)),
                facilityValenca, facilityLeixoes, locoSlow, routeVL));

        // 13. T5472 (L -> V, 10:05, FAST) - Prioritário (em relação a T5473).
        trains.add(new Train("5472", "MEDWAY", baseDate.with(LocalTime.of(10, 5, 0)),
                facilityLeixoes, facilityValenca, locoFast, routeLV));

        // 14. T5473 (V -> L, 10:30, FAST) - Colide com T5472 e potencialmente T5470 (recalculado).
        trains.add(new Train("5473", "CAPTRAIN", baseDate.with(LocalTime.of(10, 30, 0)),
                facilityValenca, facilityLeixoes, locoFast, routeVL));

        // 15. T5474 (L -> V, 10:45, SLOW) - Prioritário.
        trains.add(new Train("5474", "MEDWAY", baseDate.with(LocalTime.of(10, 45, 0)),
                facilityLeixoes, facilityValenca, locoSlow, routeLV));

        // 16. T5475 (V -> L, 10:50, SLOW) - Colide com T5474.
        trains.add(new Train("5475", "CAPTRAIN", baseDate.with(LocalTime.of(10, 50, 0)),
                facilityValenca, facilityLeixoes, locoSlow, routeVL));

        // 17. T5476 (L -> V, 11:00, FAST) - Prioritário.
        trains.add(new Train("5476", "MEDWAY", baseDate.with(LocalTime.of(11, 0, 0)),
                facilityLeixoes, facilityValenca, locoFast, routeLV));

        // 18. T5477 (V -> L, 11:15, SLOW) - Colide com T5476.
        trains.add(new Train("5477", "CAPTRAIN", baseDate.with(LocalTime.of(11, 15, 0)),
                facilityValenca, facilityLeixoes, locoSlow, routeVL));

        // 19. T5478 (L -> V, 11:20, SLOW) - Prioritário.
        trains.add(new Train("5478", "MEDWAY", baseDate.with(LocalTime.of(11, 20, 0)),
                facilityLeixoes, facilityValenca, locoSlow, routeLV));

        // 20. T5479 (V -> L, 11:30, FAST) - Colide com T5478.
        trains.add(new Train("5479", "CAPTRAIN", baseDate.with(LocalTime.of(11, 30, 0)),
                facilityValenca, facilityLeixoes, locoFast, routeVL));

        // --- FIM MOCK DE CONFLITOS EXTREMOS ---

        return trains;
    }

    /**
     * Procura um comboio pelo ID (inclui os mocks).
     */
    public Optional<Train> findById(String id) {
        return findAll().stream().filter(t -> t.getTrainId().equals(id)).findFirst();
    }

    /**
     * Retorna todos os IDs de operadores distintos dos comboios carregados.
     */
    public List<String> findAllOperators() {
        return findAll().stream()
                .map(Train::getOperatorId)
                .distinct()
                .collect(Collectors.toList());
    }

    /**
     * Retorna todos os IDs de rota distintos dos comboios carregados.
     */
    public List<String> findAllRouteIds() {
        return findAll().stream()
                .map(Train::getRouteId)
                .filter(id -> id != null && !id.isEmpty())
                .distinct()
                .collect(Collectors.toList());
    }
}
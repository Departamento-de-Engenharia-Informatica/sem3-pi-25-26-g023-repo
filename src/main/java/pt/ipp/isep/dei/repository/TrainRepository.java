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

public class TrainRepository {

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

        // --- ADIÇÃO DE MOCKS PARA FORÇAR CONFLITOS (HEAD-ON) ---
        // Estes mocks substituem quaisquer entradas da DB com o mesmo ID, garantindo o cenário de teste.

        // 1. MOCK T5439 (Leixões -> Valença) - Já existente, mantém-se para o cenário
        trains.add(new Train(
                "5439",
                "CAPTRAIN",
                LocalDateTime.of(2025, 10, 6, 9, 30, 0), // Partida: 09:30:00
                50, // Leixões (Start)
                11, // Valença (End)
                "5034", // Loco 5034 (3178 kW) -> V_calc ≈ 91 km/h
                "R001" // Rota para Valença
        ));

        // 2. MOCK T5437 (Valença -> Leixões) - ALTERADO para colidir com T5439 no Seg 18
        // Partida ANTECIPADA para 09:40:00 e locomotiva MAIS LENTA (5034) para esticar o tempo na via.
        trains.add(new Train(
                "5437",
                "CP",
                LocalDateTime.of(2025, 10, 6, 9, 40, 0), // Partida ANTECIPADA: 09:40:00
                11, // Valença (Start)
                50, // Leixões (End)
                "5034", // Loco 5034 (3178 kW) -> V_calc ≈ 91 km/h
                "R002" // Rota para Leixões
        ));

        // --- NOVO CENÁRIO DE CONFLITO 3 & 4 (Para testar Seg 26) ---

        // 3. MOCK T5440 (Leixões -> Valença) - Novo Comboio, Velo Média
        trains.add(new Train(
                "5440",
                "CAPTRAIN",
                LocalDateTime.of(2025, 10, 6, 10, 30, 0), // Partida: 10:30:00
                50, // Leixões (Start)
                11, // Valença (End)
                "5621", // Loco 5621 (5600 kW) -> V_calc ≈ 150 km/h
                "R001" // Rota para Valença
        ));

        // 4. MOCK T5441 (Valença -> Leixões) - Novo Comboio, Velo Lenta (Colide com T5440 no Seg 26)
        trains.add(new Train(
                "5441",
                "CP",
                LocalDateTime.of(2025, 10, 6, 10, 0, 0), // Partida: 10:00:00 (Tempo para colidir no final da rota)
                11, // Valença (Start)
                50, // Leixões (End)
                "5034", // Loco 5034 (3178 kW) -> V_calc ≈ 91 km/h
                "R002" // Rota para Leixões
        ));

        // --- FIM MOCK ---

        return trains;
    }

    public Optional<Train> findById(String id) {
        return findAll().stream().filter(t -> t.getTrainId().equals(id)).findFirst();
    }
}
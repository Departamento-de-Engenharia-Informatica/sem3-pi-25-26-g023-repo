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
     * Procura todos os comboios na base de dados.
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

                    // FIX para Range [0, 8) out of bounds:
                    // Se a string for curta (ex: "10:30", length 5), usa-se a string toda.
                    // Se for longa (ex: "10:30:00.123"), corta-se para "HH:mm:ss" (length 8).
                    String timePart = timeStr.length() >= 8 ? timeStr.substring(0, 8) : timeStr;
                    LocalTime time = LocalTime.parse(timePart);

                    LocalDateTime departureTime = date.toLocalDate().atTime(time);

                    trains.add(new Train(trainId, operatorId, departureTime, startFacilityId, endFacilityId, locoId, routeId));
                } catch (Exception e) {
                    System.err.println("❌ Erro de tipagem ao ler Train: " + e.getMessage());
                }
            }
        } catch (SQLException e) {
            System.err.println("❌ Erro fatal ao ler tabela TRAIN: " + e.getMessage());
        }

        return trains;
    }

    /**
     * Procura um comboio pelo ID.
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
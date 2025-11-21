// File: pt.ipp.isep.dei.repository.TrainRepository.java
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
        // SQL adaptado para incluir route_id
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
                    String routeId = rs.getString("route_id"); // NOVO: Capturar route_id

                    int startFacilityId = rs.getInt("start_facility_id");
                    int endFacilityId = rs.getInt("end_facility_id");

                    Date date = rs.getDate("train_date");
                    String timeStr = rs.getString("train_time");

                    LocalTime time = LocalTime.parse(timeStr.substring(0, 8));
                    LocalDateTime departureTime = date.toLocalDate().atTime(time);

                    // Construtor atualizado para aceitar routeId
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

    public Optional<Train> findById(String id) {
        // Manter por conveniência, mas é ineficiente.
        return findAll().stream().filter(t -> t.getTrainId().equals(id)).findFirst();
    }
}
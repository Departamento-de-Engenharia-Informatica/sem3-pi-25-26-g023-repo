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
        // SQL adaptado às colunas reais: train_id, operator_id, train_date, start_facility_id, end_facility_id, locomotive_id
        String sql = "SELECT train_id, operator_id, train_date, train_time, start_facility_id, end_facility_id, locomotive_id " +
                "FROM TRAIN ORDER BY train_id";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                try {
                    String trainId = rs.getString("train_id");
                    String operatorId = rs.getString("operator_id");
                    String locoId = rs.getString("locomotive_id");

                    // As Facility IDs são lidas como NUMBER, mas usadas como INT no Java
                    int startFacilityId = rs.getInt("start_facility_id");
                    int endFacilityId = rs.getInt("end_facility_id");

                    // Combina DATE e TIME para LocalDateTime
                    Date date = rs.getDate("train_date");
                    String timeStr = rs.getString("train_time"); // Lida como string (HH:MM:SS)

                    LocalTime time = LocalTime.parse(timeStr.substring(0, 8));
                    LocalDateTime departureTime = date.toLocalDate().atTime(time);

                    trains.add(new Train(trainId, operatorId, departureTime, startFacilityId, endFacilityId, locoId));
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
        return findAll().stream().filter(t -> t.getTrainId().equals(id)).findFirst();
    }
}
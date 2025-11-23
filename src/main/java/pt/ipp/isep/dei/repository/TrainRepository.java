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

/**
 * Repository class responsible for data access operations (CRUD) related to the {@link Train} entity.
 * It queries the TRAIN table in the database.
 */
public class TrainRepository {

    /**
     * Searches for all trains in the database.
     *
     * <p>It handles the combination of 'train_date' and 'train_time' columns
     * into a single {@link LocalDateTime} departure time.</p>
     *
     * @return A list of all {@link Train} objects loaded from the database.
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
                    // The train_time field is read as String (or Time/Timestamp and then converted)
                    String timeStr = rs.getString("train_time");

                    // FIX for Range [0, 8) out of bounds:
                    // If the string is short (e.g., "10:30", length 5), the whole string is used.
                    // If it is long (e.g., "10:30:00.123"), it is truncated to "HH:mm:ss" (length 8).
                    String timePart = timeStr.length() >= 8 ? timeStr.substring(0, 8) : timeStr;
                    LocalTime time = LocalTime.parse(timePart);

                    LocalDateTime departureTime = date.toLocalDate().atTime(time);

                    trains.add(new Train(trainId, operatorId, departureTime, startFacilityId, endFacilityId, locoId, routeId));
                } catch (Exception e) {
                    System.err.println("❌ Typing error when reading Train: " + e.getMessage());
                }
            }
        } catch (SQLException e) {
            System.err.println("❌ Fatal error reading TRAIN table: " + e.getMessage());
        }

        return trains;
    }

    /**
     * Searches for a train by its ID.
     *
     * @param id The unique identifier of the train.
     * @return An {@link Optional} containing the {@link Train} if found, or empty otherwise.
     */
    public Optional<Train> findById(String id) {
        return findAll().stream().filter(t -> t.getTrainId().equals(id)).findFirst();
    }

    /**
     * Returns all distinct operator IDs from the loaded trains.
     *
     * @return A list of unique operator ID strings.
     */
    public List<String> findAllOperators() {
        return findAll().stream()
                .map(Train::getOperatorId)
                .distinct()
                .collect(Collectors.toList());
    }

    /**
     * Returns all distinct route IDs from the loaded trains.
     *
     * @return A list of unique route ID strings.
     */
    public List<String> findAllRouteIds() {
        return findAll().stream()
                .map(Train::getRouteId)
                .filter(id -> id != null && !id.isEmpty())
                .distinct()
                .collect(Collectors.toList());
    }
}
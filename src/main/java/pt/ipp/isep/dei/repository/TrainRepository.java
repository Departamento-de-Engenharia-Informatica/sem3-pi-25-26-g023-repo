package pt.ipp.isep.dei.repository;

import pt.ipp.isep.dei.DatabaseConnection.DatabaseConnection;
import pt.ipp.isep.dei.domain.Train;
import pt.ipp.isep.dei.domain.Wagon;

import java.sql.*;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class TrainRepository {

    // Instância do repositório de vagões para fazer a ligação
    private final WagonRepository wagonRepo = new WagonRepository();

    /**
     * Recupera todos os comboios e preenche a sua lista de vagões.
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
                    String timeStr = rs.getString("train_time");

                    String timePart = timeStr.length() >= 8 ? timeStr.substring(0, 8) : timeStr;
                    LocalTime time = LocalTime.parse(timePart);
                    LocalDateTime departureTime = date.toLocalDate().atTime(time);

                    Train train = new Train(trainId, operatorId, departureTime, startFacilityId, endFacilityId, locoId, routeId);

                    // --- CARREGAR VAGÕES REAIS DA BD ---
                    List<Wagon> realWagons = wagonRepo.findWagonsByTrainId(trainId);
                    train.setWagons(realWagons);
                    // -----------------------------------

                    trains.add(train);

                } catch (Exception e) {
                    System.err.println("Erro de tipagem ao ler Train: " + e.getMessage());
                }
            }
        } catch (SQLException e) {
            System.err.println("Erro fatal ao ler tabela TRAIN: " + e.getMessage());
        }
        return trains;
    }

    public Optional<Train> findById(String id) {
        return findAll().stream().filter(t -> t.getTrainId().equals(id)).findFirst();
    }

    public List<String> findAllOperators() {
        return findAll().stream().map(Train::getOperatorId).distinct().collect(Collectors.toList());
    }

    public List<String> findAllRouteIds() {
        return findAll().stream().map(Train::getRouteId).filter(id -> id != null && !id.isEmpty()).distinct().collect(Collectors.toList());
    }

    /**
     * --- MÉTODO EM FALTA ---
     * Grava um novo comboio e a sua composição (vagões) na base de dados.
     * Utiliza uma transação para garantir integridade.
     */
    public boolean saveTrainWithConsist(Train train, List<String> wagonIds) {
        Connection conn = null;
        PreparedStatement stmtTrain = null;
        PreparedStatement stmtUsage = null;

        String sqlTrain = "INSERT INTO TRAIN (train_id, operator_id, train_date, train_time, " +
                "start_facility_id, end_facility_id, locomotive_id, route_id) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

        String sqlUsage = "INSERT INTO TRAIN_WAGON_USAGE (usage_id, train_id, wagon_id, usage_date) " +
                "VALUES (?, ?, ?, ?)";

        try {
            conn = DatabaseConnection.getConnection();
            conn.setAutoCommit(false); // 1. Iniciar Transação

            // 2. Inserir dados na tabela TRAIN
            stmtTrain = conn.prepareStatement(sqlTrain);
            stmtTrain.setString(1, train.getTrainId());
            stmtTrain.setString(2, train.getOperatorId());
            stmtTrain.setDate(3, Date.valueOf(train.getDepartureTime().toLocalDate()));
            stmtTrain.setString(4, train.getDepartureTime().toLocalTime().toString());
            stmtTrain.setInt(5, train.getStartFacilityId());
            stmtTrain.setInt(6, train.getEndFacilityId());
            stmtTrain.setString(7, train.getLocomotiveId());
            stmtTrain.setString(8, train.getRouteId());

            stmtTrain.executeUpdate();

            // 3. Inserir dados na tabela TRAIN_WAGON_USAGE (se houver vagões)
            if (wagonIds != null && !wagonIds.isEmpty()) {
                stmtUsage = conn.prepareStatement(sqlUsage);
                int idx = 1;
                for (String wId : wagonIds) {
                    // Gerar ID único para o uso: USG_TrainID_Index
                    String usageId = "USG_" + train.getTrainId() + "_" + idx++;

                    stmtUsage.setString(1, usageId);
                    stmtUsage.setString(2, train.getTrainId());
                    stmtUsage.setString(3, wId);
                    stmtUsage.setDate(4, Date.valueOf(train.getDepartureTime().toLocalDate()));

                    stmtUsage.addBatch();
                }
                stmtUsage.executeBatch();
            }

            conn.commit(); // 4. Gravar efetivamente
            return true;

        } catch (SQLException e) {
            if (conn != null) {
                try { conn.rollback(); } catch (SQLException ex) { ex.printStackTrace(); }
            }
            System.err.println("❌ Erro ao gravar comboio e composição: " + e.getMessage());
            return false;
        } finally {
            try {
                if (stmtTrain != null) stmtTrain.close();
                if (stmtUsage != null) stmtUsage.close();
                if (conn != null) {
                    conn.setAutoCommit(true);
                    conn.close();
                }
            } catch (SQLException ex) { ex.printStackTrace(); }
        }
    }
}
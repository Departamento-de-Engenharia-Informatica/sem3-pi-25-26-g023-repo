package pt.ipp.isep.dei.repository;

import pt.ipp.isep.dei.DatabaseConnection.DatabaseConnection;
import pt.ipp.isep.dei.domain.Train;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class TrainRepository {

    /**
     * Encontra um comboio pelo seu ID.
     * Devolve Optional<Train> para ser compatível com as UIs existentes.
     */
    public Optional<Train> findById(String trainId) {
        String query = "SELECT * FROM TRAIN WHERE train_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setString(1, trainId);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    String id = rs.getString("train_id");
                    String operator = rs.getString("operator_id");
                    Date date = rs.getDate("train_date");
                    String timeStr = rs.getString("train_time");
                    int startNode = rs.getInt("start_facility_id");
                    int endNode = rs.getInt("end_facility_id");
                    String locoId = rs.getString("locomotive_id");
                    String routeId = rs.getString("route_id");

                    LocalDateTime departure = null;
                    if (date != null && timeStr != null) {
                        // Tenta fazer o parse da hora. Se a BD tiver formato diferente, ajusta aqui.
                        try {
                            departure = LocalDateTime.of(date.toLocalDate(), java.time.LocalTime.parse(timeStr));
                        } catch (Exception e) {
                            // Fallback se a hora vier num formato estranho
                            departure = date.toLocalDate().atStartOfDay();
                        }
                    }

                    Train train = new Train(id, operator, departure, startNode, endNode, locoId, routeId);
                    return Optional.of(train);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return Optional.empty();
    }

    public List<Train> findAll() {
        List<Train> trains = new ArrayList<>();
        // Ordena por data decrescente para ver os mais recentes primeiro
        String query = "SELECT * FROM TRAIN ORDER BY train_date DESC, train_time DESC";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                String id = rs.getString("train_id");
                String operator = rs.getString("operator_id");
                Date date = rs.getDate("train_date");
                String timeStr = rs.getString("train_time");
                int startNode = rs.getInt("start_facility_id");
                int endNode = rs.getInt("end_facility_id");
                String locoId = rs.getString("locomotive_id");
                String routeId = rs.getString("route_id");

                LocalDateTime departure = null;
                if (date != null && timeStr != null) {
                    try {
                        departure = LocalDateTime.of(date.toLocalDate(), java.time.LocalTime.parse(timeStr));
                    } catch (Exception e) {
                        departure = date.toLocalDate().atStartOfDay();
                    }
                }

                Train train = new Train(id, operator, departure, startNode, endNode, locoId, routeId);
                trains.add(train);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return trains;
    }

    /**
     * CORREÇÃO CRÍTICA: Busca os operadores REAIS da base de dados.
     * Evita erros de chave estrangeira (ORA-02291).
     */
    public List<String> findAllOperators() {
        List<String> ops = new ArrayList<>();
        // Query à tabela OPERATOR (assumindo que a coluna chave é operator_id)
        String sql = "SELECT operator_id FROM OPERATOR";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                ops.add(rs.getString("operator_id"));
            }

        } catch (SQLException e) {
            System.err.println("Erro ao carregar operadores: " + e.getMessage());
            // Se a tabela não existir, tenta carregar pelo menos os que estão em uso na tabela TRAIN
            // Isto é um "hack" para tentar não devolver lista vazia
            return getOperatorsFromExistingTrains();
        }

        if (ops.isEmpty()) {
            System.err.println("AVISO: A tabela OPERATOR está vazia! Não será possível criar comboios válidos.");
        }

        return ops;
    }

    // Método auxiliar de recurso
    private List<String> getOperatorsFromExistingTrains() {
        List<String> ops = new ArrayList<>();
        String sql = "SELECT DISTINCT operator_id FROM TRAIN";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while(rs.next()) ops.add(rs.getString("operator_id"));
        } catch (SQLException e) { e.printStackTrace(); }
        return ops;
    }

    public List<String> findAllRouteIds() {
        List<String> routes = new ArrayList<>();
        // Apenas rotas que existem na BD
        String sql = "SELECT DISTINCT route_id FROM TRAIN WHERE route_id IS NOT NULL";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while(rs.next()) {
                routes.add(rs.getString("route_id"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return routes;
    }

    public List<String> getTrainConsist(String trainId) {
        List<String> wagonIds = new ArrayList<>();
        String sql = "SELECT wagon_id FROM TRAIN_WAGON_USAGE WHERE train_id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, trainId);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    wagonIds.add(rs.getString("wagon_id"));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return wagonIds;
    }

    public boolean saveTrainWithConsist(Train train, List<String> wagonIds) {
        Connection conn = null;
        PreparedStatement stmtCheck = null;
        PreparedStatement stmtTrain = null;
        PreparedStatement stmtDeleteUsage = null;
        PreparedStatement stmtInsertUsage = null;

        boolean isUpdate = false;

        try {
            conn = DatabaseConnection.getConnection();
            conn.setAutoCommit(false); // Transação

            // 1. Verificar UPDATE vs INSERT
            stmtCheck = conn.prepareStatement("SELECT COUNT(*) FROM TRAIN WHERE train_id = ?");
            stmtCheck.setString(1, train.getTrainId());
            ResultSet rs = stmtCheck.executeQuery();
            if (rs.next() && rs.getInt(1) > 0) {
                isUpdate = true;
            }
            rs.close();

            // 2. Tabela TRAIN
            if (isUpdate) {
                String sqlUpdate = "UPDATE TRAIN SET operator_id=?, train_date=?, train_time=?, start_facility_id=?, end_facility_id=?, locomotive_id=?, route_id=? WHERE train_id=?";
                stmtTrain = conn.prepareStatement(sqlUpdate);
                stmtTrain.setString(1, train.getOperatorId());
                stmtTrain.setDate(2, java.sql.Date.valueOf(train.getDepartureTime().toLocalDate()));
                stmtTrain.setString(3, train.getDepartureTime().toLocalTime().toString());
                stmtTrain.setInt(4, train.getStartFacilityId());
                stmtTrain.setInt(5, train.getEndFacilityId());
                stmtTrain.setString(6, train.getLocomotiveId());
                stmtTrain.setString(7, train.getRouteId());
                stmtTrain.setString(8, train.getTrainId());
                stmtTrain.executeUpdate();

                // Removemos composição antiga para adicionar a nova
                stmtDeleteUsage = conn.prepareStatement("DELETE FROM TRAIN_WAGON_USAGE WHERE train_id = ?");
                stmtDeleteUsage.setString(1, train.getTrainId());
                stmtDeleteUsage.executeUpdate();

            } else {
                String sqlInsert = "INSERT INTO TRAIN (train_id, operator_id, train_date, train_time, start_facility_id, end_facility_id, locomotive_id, route_id) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
                stmtTrain = conn.prepareStatement(sqlInsert);
                stmtTrain.setString(1, train.getTrainId());
                stmtTrain.setString(2, train.getOperatorId());
                stmtTrain.setDate(3, java.sql.Date.valueOf(train.getDepartureTime().toLocalDate()));
                stmtTrain.setString(4, train.getDepartureTime().toLocalTime().toString());
                stmtTrain.setInt(5, train.getStartFacilityId());
                stmtTrain.setInt(6, train.getEndFacilityId());
                stmtTrain.setString(7, train.getLocomotiveId());
                stmtTrain.setString(8, train.getRouteId());
                stmtTrain.executeUpdate();
            }

            // 3. Tabela TRAIN_WAGON_USAGE
            String sqlUsage = "INSERT INTO TRAIN_WAGON_USAGE (usage_id, train_id, wagon_id, usage_date) VALUES (?, ?, ?, ?)";
            stmtInsertUsage = conn.prepareStatement(sqlUsage);

            for (String wagonId : wagonIds) {
                // CORREÇÃO: ID curto (12 chars) para evitar ORA-12899
                String usageId = UUID.randomUUID().toString().replace("-", "").substring(0, 12).toUpperCase();

                stmtInsertUsage.setString(1, usageId);
                stmtInsertUsage.setString(2, train.getTrainId());
                stmtInsertUsage.setString(3, wagonId);
                stmtInsertUsage.setDate(4, java.sql.Date.valueOf(train.getDepartureTime().toLocalDate()));

                stmtInsertUsage.addBatch();
            }

            if (!wagonIds.isEmpty()) {
                stmtInsertUsage.executeBatch();
            }

            conn.commit();
            return true;

        } catch (SQLException e) {
            System.err.println("Erro SQL: " + e.getMessage());
            e.printStackTrace(); // Importante para debug
            if (conn != null) {
                try { conn.rollback(); } catch (SQLException ex) { ex.printStackTrace(); }
            }
            return false;
        } finally {
            try {
                if (conn != null) {
                    conn.setAutoCommit(true);
                    conn.close();
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }
}
package pt.ipp.isep.dei.repository;

import oracle.jdbc.OracleTypes;
import pt.ipp.isep.dei.DatabaseConnection.DatabaseConnection;
import pt.ipp.isep.dei.domain.Train;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.*;

public class TrainRepository {

    /**
     * Encontra um comboio pelo seu ID via PL/SQL Function.
     */
    public Optional<Train> findById(String trainId) {
        String call = "{ ? = call fn_get_train_by_id(?) }";
        try (Connection conn = DatabaseConnection.getConnection();
             CallableStatement cstmt = conn.prepareCall(call)) {

            cstmt.registerOutParameter(1, OracleTypes.CURSOR);
            cstmt.setString(2, trainId);
            cstmt.execute();

            try (ResultSet rs = (ResultSet) cstmt.getObject(1)) {
                if (rs != null && rs.next()) {
                    return Optional.of(mapResultSetToTrain(rs));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return Optional.empty();
    }

    /**
     * Lista todos os comboios registados.
     */
    public List<Train> findAll() {
        List<Train> trains = new ArrayList<>();
        String call = "{ ? = call fn_get_all_trains() }";

        try (Connection conn = DatabaseConnection.getConnection();
             CallableStatement cstmt = conn.prepareCall(call)) {

            cstmt.registerOutParameter(1, OracleTypes.CURSOR);
            cstmt.execute();

            try (ResultSet rs = (ResultSet) cstmt.getObject(1)) {
                while (rs != null && rs.next()) {
                    trains.add(mapResultSetToTrain(rs));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return trains;
    }

    /**
     * USLP09: Busca os IDs das rotas diretamente da tabela correta (TRAIN_ROUTE).
     */
    public List<String> findAllRouteIds() {
        List<String> routes = new ArrayList<>();
        String call = "{ ? = call fn_get_all_route_ids() }";
        try (Connection conn = DatabaseConnection.getConnection();
             CallableStatement cstmt = conn.prepareCall(call)) {
            cstmt.registerOutParameter(1, oracle.jdbc.OracleTypes.CURSOR);
            cstmt.execute();
            try (ResultSet rs = (ResultSet) cstmt.getObject(1)) {
                while (rs != null && rs.next()) {
                    routes.add(rs.getString("route_id"));
                }
            }
        } catch (SQLException e) {
            System.err.println("Erro PL/SQL: " + e.getMessage());
        }
        return routes;
    }

    /**
     * USLP09: Procura os detalhes da rota (Origem e Destino).
     * Usa a tabela ROUTE_SEGMENT para identificar a primeira e última paragem.
     */
    public Optional<Map<String, Object>> findRouteDetailsById(String routeId) {
        String call = "{ ? = call fn_get_route_details(?) }";
        try (Connection conn = DatabaseConnection.getConnection();
             CallableStatement cstmt = conn.prepareCall(call)) {
            cstmt.registerOutParameter(1, oracle.jdbc.OracleTypes.CURSOR);
            cstmt.setString(2, routeId);
            cstmt.execute();
            try (ResultSet rs = (ResultSet) cstmt.getObject(1)) {
                if (rs != null && rs.next()) {
                    Map<String, Object> details = new HashMap<>();
                    details.put("start", rs.getInt("START_ID"));
                    details.put("end", rs.getInt("END_ID"));
                    return Optional.of(details);
                }
            }
        } catch (SQLException e) {
            System.err.println("Erro PL/SQL: " + e.getMessage());
        }
        return Optional.empty();
    }

    /**
     * Helper privado para mapear ResultSet para objeto Train.
     */
    private Train mapResultSetToTrain(ResultSet rs) throws SQLException {
        String id = rs.getString("train_id");
        String operator = rs.getString("operator_id");
        java.sql.Date date = rs.getDate("train_date");
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
        return new Train(id, operator, departure, startNode, endNode, locoId, routeId);
    }

    /**
     * Busca os operadores reais registados na BD.
     */
    public List<String> findAllOperators() {
        List<String> ops = new ArrayList<>();
        String call = "{ ? = call fn_get_all_operators() }";
        try (Connection conn = DatabaseConnection.getConnection();
             CallableStatement cstmt = conn.prepareCall(call)) {
            cstmt.registerOutParameter(1, OracleTypes.CURSOR);
            cstmt.execute();
            try (ResultSet rs = (ResultSet) cstmt.getObject(1)) {
                while (rs != null && rs.next()) ops.add(rs.getString("operator_id"));
            }
        } catch (SQLException e) {
            return getOperatorsFromExistingTrains();
        }
        return ops;
    }

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

    /**
     * Obtém a lista de IDs de vagões associados a um comboio.
     */
    public List<String> getTrainConsist(String trainId) {
        List<String> wagonIds = new ArrayList<>();
        String call = "{ ? = call fn_get_train_consist_ids(?) }";
        try (Connection conn = DatabaseConnection.getConnection();
             CallableStatement cstmt = conn.prepareCall(call)) {
            cstmt.registerOutParameter(1, OracleTypes.CURSOR);
            cstmt.setString(2, trainId);
            cstmt.execute();
            try (ResultSet rs = (ResultSet) cstmt.getObject(1)) {
                while (rs != null && rs.next()) {
                    wagonIds.add(rs.getString("wagon_id"));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return wagonIds;
    }

    /**
     * MÉTODO PRINCIPAL DE GRAVAÇÃO (USLP09):
     * Salva o comboio e os seus vagões usando Procedures e transações.
     */
    public boolean saveTrainWithConsist(Train train, List<String> wagonIds) {
        Connection conn = null;
        CallableStatement cstmtHeader = null;
        CallableStatement cstmtUsage = null;

        try {
            conn = DatabaseConnection.getConnection();
            conn.setAutoCommit(false);

            // 1. Gravar Cabeçalho (TRAIN)
            String callHeader = "{ call pr_save_train_header(?, ?, ?, ?, ?, ?, ?, ?) }";
            cstmtHeader = conn.prepareCall(callHeader);

            cstmtHeader.setString(1, train.getTrainId());
            cstmtHeader.setString(2, train.getOperatorId());
            cstmtHeader.setDate(3, java.sql.Date.valueOf(train.getDepartureTime().toLocalDate()));
            cstmtHeader.setString(4, train.getDepartureTime().toLocalTime().toString());
            cstmtHeader.setInt(5, train.getStartFacilityId());
            cstmtHeader.setInt(6, train.getEndFacilityId());
            cstmtHeader.setString(7, train.getLocomotiveId());
            cstmtHeader.setString(8, train.getRouteId());

            try {
                cstmtHeader.execute();
            } catch (SQLException e) {
                throw new SQLException("Erro no Cabeçalho: " + e.getMessage() + " (Verifique se o Operator ID ou Route ID existem na BD)", e);
            }

            // 2. Gravar Composição (TRAIN_WAGON_USAGE)
            if (wagonIds != null && !wagonIds.isEmpty()) {
                String callUsage = "{ call pr_add_wagon_usage(?, ?, ?, ?) }";
                cstmtUsage = conn.prepareCall(callUsage);

                for (String wagonId : wagonIds) {
                    String usageId = UUID.randomUUID().toString().replace("-", "").substring(0, 12).toUpperCase();
                    cstmtUsage.setString(1, usageId);
                    cstmtUsage.setString(2, train.getTrainId());
                    cstmtUsage.setString(3, wagonId);
                    cstmtUsage.setDate(4, java.sql.Date.valueOf(train.getDepartureTime().toLocalDate()));
                    cstmtUsage.addBatch();
                }

                try {
                    cstmtUsage.executeBatch();
                } catch (SQLException e) {
                    throw new SQLException("Erro ao associar vagões: " + e.getMessage(), e);
                }
            }

            conn.commit();
            return true;

        } catch (SQLException e) {
            System.err.println("❌ ERRO BD: " + e.getMessage());
            if (conn != null) {
                try { conn.rollback(); } catch (SQLException ex) { ex.printStackTrace(); }
            }
            // Lançar RuntimeException para o Controller apanhar e mostrar na UI
            throw new RuntimeException(e.getMessage());
        } finally {
            closeResources(conn, cstmtHeader, cstmtUsage);
        }
    }

    private void closeResources(Connection conn, Statement... stmts) {
        try {
            for (Statement s : stmts) if (s != null) s.close();
            if (conn != null) {
                conn.setAutoCommit(true);
                conn.close();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
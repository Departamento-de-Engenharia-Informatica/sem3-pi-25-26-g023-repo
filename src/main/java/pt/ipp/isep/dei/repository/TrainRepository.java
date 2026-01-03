package pt.ipp.isep.dei.repository;

import oracle.jdbc.OracleTypes;
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

    // Helper privado para evitar duplicação de código de mapeamento
    private Train mapResultSetToTrain(ResultSet rs) throws SQLException {
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
        return new Train(id, operator, departure, startNode, endNode, locoId, routeId);
    }

    /**
     * Busca os operadores REAIS via PL/SQL.
     */
    public List<String> findAllOperators() {
        List<String> ops = new ArrayList<>();
        String call = "{ ? = call fn_get_all_operators() }";

        try (Connection conn = DatabaseConnection.getConnection();
             CallableStatement cstmt = conn.prepareCall(call)) {

            cstmt.registerOutParameter(1, OracleTypes.CURSOR);
            cstmt.execute();

            try (ResultSet rs = (ResultSet) cstmt.getObject(1)) {
                while (rs != null && rs.next()) {
                    ops.add(rs.getString("operator_id"));
                }
            }

        } catch (SQLException e) {
            System.err.println("Erro PL/SQL carregar operadores: " + e.getMessage());
            // Fallback: tenta pegar dos comboios existentes via query direta se a função falhar
            return getOperatorsFromExistingTrains();
        }

        if (ops.isEmpty()) {
            // Tenta o fallback caso a tabela OPERATOR esteja vazia
            ops = getOperatorsFromExistingTrains();
            if (ops.isEmpty())
                System.err.println("AVISO: Tabela OPERATOR vazia e sem comboios registados.");
        }
        return ops;
    }

    // Mantido como recurso
    private List<String> getOperatorsFromExistingTrains() {
        List<String> ops = new ArrayList<>();
        // Query simples de recurso, pode ficar em SQL direto ou criar outra func PL/SQL
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
        String call = "{ ? = call fn_get_train_routes() }";
        try (Connection conn = DatabaseConnection.getConnection();
             CallableStatement cstmt = conn.prepareCall(call)) {

            cstmt.registerOutParameter(1, OracleTypes.CURSOR);
            cstmt.execute();
            try (ResultSet rs = (ResultSet) cstmt.getObject(1)) {
                while(rs != null && rs.next()) routes.add(rs.getString("route_id"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return routes;
    }

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
     * O Método Principal: Salva o comboio usando Procedures PL/SQL.
     * Mantém a lógica de transação e batch insert.
     */
    public boolean saveTrainWithConsist(Train train, List<String> wagonIds) {
        Connection conn = null;
        CallableStatement cstmtHeader = null;
        CallableStatement cstmtUsage = null;

        try {
            conn = DatabaseConnection.getConnection();
            conn.setAutoCommit(false); // Transação iniciada

            // 1. Procedure para Cabeçalho (Trata UPDATE vs INSERT e limpeza de usage)
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

            cstmtHeader.execute();

            // 2. Procedure para Usage (Batch Insert)
            String callUsage = "{ call pr_add_wagon_usage(?, ?, ?, ?) }";
            cstmtUsage = conn.prepareCall(callUsage);

            for (String wagonId : wagonIds) {
                // ID curto (12 chars) mantido conforme original
                String usageId = UUID.randomUUID().toString().replace("-", "").substring(0, 12).toUpperCase();

                cstmtUsage.setString(1, usageId);
                cstmtUsage.setString(2, train.getTrainId());
                cstmtUsage.setString(3, wagonId);
                cstmtUsage.setDate(4, java.sql.Date.valueOf(train.getDepartureTime().toLocalDate()));

                cstmtUsage.addBatch();
            }

            if (!wagonIds.isEmpty()) {
                cstmtUsage.executeBatch();
            }

            conn.commit();
            return true;

        } catch (SQLException e) {
            System.err.println("Erro PL/SQL Save: " + e.getMessage());
            e.printStackTrace();
            if (conn != null) {
                try { conn.rollback(); } catch (SQLException ex) { ex.printStackTrace(); }
            }
            return false;
        } finally {
            try {
                if (cstmtHeader != null) cstmtHeader.close();
                if (cstmtUsage != null) cstmtUsage.close();
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
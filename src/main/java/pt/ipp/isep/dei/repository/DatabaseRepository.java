package pt.ipp.isep.dei.repository;

import pt.ipp.isep.dei.DatabaseConnection.DatabaseConnection;
import pt.ipp.isep.dei.domain.Locomotive;
import pt.ipp.isep.dei.domain.Operator;
import pt.ipp.isep.dei.domain.Wagon;
import pt.ipp.isep.dei.domain.Station;
import pt.ipp.isep.dei.domain.FreightRequest;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Repository class responsible for interacting with the database, handling
 * CRUD operations for core entities (Operator, Locomotive, Wagon) and providing
 * generic data access methods.
 */
public class DatabaseRepository {

    /** List of master tables available for generic viewing/management. */
    public static final List<String> MASTER_TABLES = List.of(
            "OPERATOR", "STATION", "RAILWAY_LINE", "LOCOMOTIVE", "WAGON_MODEL"
    );

    private static final String DEFAULT_OPERATOR_ID = "MEDWAY";

    // =========================================================================
    // SPECIFIC METHODS FOR ROUTE PLANNING (USLP08)
    // =========================================================================

    /**
     * Persiste uma nova rota e os seus segmentos na base de dados.
     * Além disso, associa/atualiza os freights processados.
     */
    // No ficheiro pt.ipp.isep.dei.repository.DatabaseRepository.java


    public void saveRoute(String routeId, String name, List<Station> stops, List<FreightRequest> assignedFreights) throws SQLException {
        String sqlRoute = "{ call pr_insert_train_route(?, ?, ?) }";
        String sqlSegment = "{ call pr_insert_route_segment(?, ?, ?, ?) }";

        try (Connection conn = DatabaseConnection.getConnection()) {
            conn.setAutoCommit(false);
            try {
                String shortId = routeId.length() > 10 ? routeId.substring(0, 10) : routeId;
                try (CallableStatement cstmt = conn.prepareCall(sqlRoute)) {
                    cstmt.setString(1, shortId);
                    cstmt.setString(2, name);
                    cstmt.setString(3, "Manifest: " + assignedFreights.size() + " freights.");
                    cstmt.execute();
                }
                try (CallableStatement cstmtSeg = conn.prepareCall(sqlSegment)) {
                    for (int i = 0; i < stops.size(); i++) {
                        cstmtSeg.setString(1, shortId);
                        cstmtSeg.setInt(2, i + 1);
                        cstmtSeg.setInt(3, stops.get(i).idEstacao());
                        cstmtSeg.setString(4, "Y");
                        cstmtSeg.execute();
                    }
                }
                conn.commit();
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            }
        }
    }
    public void addElectricLocomotive(String modelName, double powerKW, double maxSpeed, double consumption, double voltage) throws SQLException {
        String sql = "{ call pr_register_electric_locomotive(?, ?, ?, ?, ?, ?) }";
        try (Connection conn = DatabaseConnection.getConnection();
             CallableStatement cstmt = conn.prepareCall(sql)) {
            cstmt.setString(1, modelName);
            cstmt.setString(2, DEFAULT_OPERATOR_ID);
            cstmt.setDouble(3, powerKW);
            cstmt.setDouble(4, maxSpeed);
            cstmt.setDouble(5, consumption);
            cstmt.setDouble(6, voltage);
            cstmt.execute();
        }
    }

    /**
     * Busca todos os freights que ainda não foram processados.
     */
    public List<Map<String, Object>> findPendingFreights() throws SQLException {
        return findGenericTableData("FREIGHT");
    }

    // =========================================================================
    // SPECIFIC METHODS FOR OPERATOR (CRUD)
    // =========================================================================

    /**
     * READ: Returns all operators from the database.
     */
    public List<Operator> findAllOperators() throws SQLException {
        List<Operator> operators = new ArrayList<>();
        String sql = "SELECT operator_id, name FROM OPERATOR ORDER BY operator_id";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                operators.add(new Operator(rs.getString("operator_id"), rs.getString("name")));
            }
        }
        return operators;
    }

    /**
     * CREATE: Adds a new operator.
     */
    public void addOperator(String id, String name) throws SQLException {
        String sql = "INSERT INTO OPERATOR (operator_id, name) VALUES (?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, id);
            stmt.setString(2, name);
            int rows = stmt.executeUpdate();
            if (rows == 0) throw new SQLException("Failed to create operator.", "02000");
        }
    }

    /**
     * UPDATE: Updates an existing operator's name.
     */
    public void updateOperator(String id, String newName) throws SQLException {
        String sql = "UPDATE OPERATOR SET name = ? WHERE operator_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, newName);
            stmt.setString(2, id);
            int rows = stmt.executeUpdate();
            if (rows == 0) throw new SQLException("Update failed. Operator ID " + id + " not found.", "02000");
        }
    }

    /**
     * DELETE: Removes an operator.
     */
    public void deleteOperator(String id) throws SQLException {
        String sql = "DELETE FROM OPERATOR WHERE operator_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, id);
            int rows = stmt.executeUpdate();
            if (rows == 0) throw new SQLException("Delete failed. Operator ID " + id + " not found.", "02000");
        }
    }

    // =========================================================================
    // SPECIFIC METHODS FOR LOCOMOTIVE (CRUD)
    // =========================================================================

    /**
     * CREATE: Adds a new Locomotive.
     * <p>Requires a transaction since it involves ROLLING_STOCK (Parent) and LOCOMOTIVE (Child) tables.</p>
     */
    public void addLocomotive(int id, String model, String type, double powerKW) throws SQLException {
        Connection conn = null;
        String idStr = String.valueOf(id);
        try {
            conn = DatabaseConnection.getConnection();
            conn.setAutoCommit(false);
            int rowsAffected = 0;

            // 1. INSERT into ROLLING_STOCK
            String sqlRS = "INSERT INTO ROLLING_STOCK (stock_id, model, operator_id) VALUES (?, ?, ?)";
            try (PreparedStatement stmtRS = conn.prepareStatement(sqlRS)) {
                stmtRS.setString(1, idStr);
                stmtRS.setString(2, model);
                stmtRS.setString(3, DEFAULT_OPERATOR_ID);
                rowsAffected += stmtRS.executeUpdate();
            }

            // 2. INSERT into LOCOMOTIVE
            String sqlL = "INSERT INTO LOCOMOTIVE (stock_id, locomotive_type, power_kw) VALUES (?, ?, ?)";
            try (PreparedStatement stmtL = conn.prepareStatement(sqlL)) {
                stmtL.setString(1, idStr);
                stmtL.setString(2, type);
                stmtL.setDouble(3, powerKW);
                rowsAffected += stmtL.executeUpdate();
            }

            if (rowsAffected < 2) throw new SQLException("Failed to create Locomotive. Check if Operator ID is valid.", "02000");

            conn.commit();
            System.out.printf("DB: Inserting Locomotive ID:%s, Model:%s, Type:%s, Power:%.1f kW - SUCCESS\n", idStr, model, type, powerKW);

        } catch (SQLException e) {
            if (conn != null) {
                conn.rollback();
            }
            System.err.println("SQL ERROR during addLocomotive: " + e.getMessage());
            throw e;
        } finally {
            if (conn != null) {
                conn.setAutoCommit(true);
                conn.close();
            }
        }
    }

    /**
     * UPDATE: Updates an existing Locomotive.
     * <p>Requires a transaction since it involves ROLLING_STOCK (Parent) and LOCOMOTIVE (Child) tables.</p>
     */
    public void updateLocomotive(int id, String newModel, String newType, double newPowerKW) throws SQLException {
        Connection conn = null;
        String idStr = String.valueOf(id);
        try {
            conn = DatabaseConnection.getConnection();
            conn.setAutoCommit(false);
            int rowsAffected = 0;

            // 1. UPDATE on ROLLING_STOCK
            String sqlRS = "UPDATE ROLLING_STOCK SET model = ? WHERE stock_id = ?";
            try (PreparedStatement stmtRS = conn.prepareStatement(sqlRS)) {
                stmtRS.setString(1, newModel);
                stmtRS.setString(2, idStr);
                rowsAffected += stmtRS.executeUpdate();
            }

            // 2. UPDATE on LOCOMOTIVE
            String sqlL = "UPDATE LOCOMOTIVE SET locomotive_type = ?, power_kw = ? WHERE stock_id = ?";
            try (PreparedStatement stmtL = conn.prepareStatement(sqlL)) {
                stmtL.setString(1, newType);
                stmtL.setDouble(2, newPowerKW);
                stmtL.setString(3, idStr);
                rowsAffected += stmtL.executeUpdate();
            }

            // Check if both tables were updated
            if (rowsAffected < 2) {
                throw new SQLException("Update failed. Locomotive ID " + idStr + " not found or partial update failed.", "02000");
            }

            conn.commit();
            System.out.printf("DB: Updating Locomotive ID:%s, New Model:%s, New Type:%s, New Power:%.1f kW - SUCCESS\n", idStr, newModel, newType, newPowerKW);

        } catch (SQLException e) {
            if (conn != null) {
                conn.rollback();
            }
            System.err.println("SQL ERROR during updateLocomotive: " + e.getMessage());
            throw e;
        } finally {
            if (conn != null) {
                conn.setAutoCommit(true);
                conn.close();
            }
        }
    }

    /**
     * DELETE: Removes a Locomotive.
     * <p>Requires a transaction and cleaning up related records (TRAIN, TRAIN_WAGON_USAGE) first.</p>
     */
    public void deleteLocomotive(int id) throws SQLException {
        Connection conn = null;
        String idStr = String.valueOf(id);
        try {
            conn = DatabaseConnection.getConnection();
            conn.setAutoCommit(false);
            int rowsAffected = 0;

            // 🚨 0. CLEANUP: TRAIN and TRAIN_WAGON_USAGE 🚨
            String sqlTWUCleanup = "DELETE FROM TRAIN_WAGON_USAGE WHERE train_id IN (SELECT train_id FROM TRAIN WHERE locomotive_id = ?)";
            try (PreparedStatement stmtTWU = conn.prepareStatement(sqlTWUCleanup)) {
                stmtTWU.setString(1, idStr);
                stmtTWU.executeUpdate();
            }

            String sqlTrainCleanup = "DELETE FROM TRAIN WHERE locomotive_id = ?";
            try (PreparedStatement stmtTC = conn.prepareStatement(sqlTrainCleanup)) {
                stmtTC.setString(1, idStr);
                stmtTC.executeUpdate();
            }

            // 1. DELETE from LOCOMOTIVE (Child Table)
            String sqlL = "DELETE FROM LOCOMOTIVE WHERE stock_id = ?";
            try (PreparedStatement stmtL = conn.prepareStatement(sqlL)) {
                stmtL.setString(1, idStr);
                rowsAffected += stmtL.executeUpdate();
            }

            // 2. DELETE from ROLLING_STOCK (Parent Table)
            String sqlRS = "DELETE FROM ROLLING_STOCK WHERE stock_id = ?";
            try (PreparedStatement stmtRS = conn.prepareStatement(sqlRS)) {
                stmtRS.setString(1, idStr);
                rowsAffected += stmtRS.executeUpdate();
            }

            if (rowsAffected == 0) {
                throw new SQLException("Delete failed. Locomotive ID " + idStr + " not found.", "02000");
            }

            conn.commit();
            System.out.println("DB: Deleting Locomotive ID: " + idStr + " - SUCCESS");

        } catch (SQLException e) {
            if (conn != null) {
                conn.rollback();
            }
            System.err.println("SQL ERROR during deleteLocomotive: " + e.getMessage());
            throw e;
        } finally {
            if (conn != null) {
                conn.setAutoCommit(true);
                conn.close();
            }
        }
    }

    /**
     * READ SPECIFIC: Returns all Locomotives from the database.
     */
    public List<Locomotive> findAllLocomotives() throws SQLException {
        List<Locomotive> locomotives = new ArrayList<>();
        String sql = "SELECT R.stock_id, L.locomotive_type, L.power_kw, R.model " +
                "FROM LOCOMOTIVE L JOIN ROLLING_STOCK R ON L.stock_id = R.stock_id " +
                "ORDER BY R.stock_id";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                try {
                    locomotives.add(new Locomotive(
                            rs.getInt("stock_id"),
                            rs.getString("model"),
                            rs.getString("locomotive_type"),
                            rs.getDouble("power_kw")
                    ));
                } catch (SQLException e) {
                    System.err.println("DATA ERROR in a LOCOMOTIVE row: " + e.getMessage());
                }
            }
        }
        if (locomotives.isEmpty()) {
            locomotives.add(new Locomotive(999, "Fallback Model", "diesel", 100.0));
        }
        return locomotives;
    }

    // =========================================================================
    // SPECIFIC METHODS FOR WAGON (CRUD)
    // =========================================================================

    /**
     * READ: Returns all Wagons.
     */
    public List<Wagon> findAllWagons() throws SQLException {
        List<Wagon> wagons = new ArrayList<>();
        String sql = "SELECT R.stock_id, W.model_id, W.service_year " +
                "FROM WAGON W JOIN ROLLING_STOCK R ON W.stock_id = R.stock_id " +
                "ORDER BY R.stock_id";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                try {
                    wagons.add(new Wagon(
                            rs.getString("stock_id"),
                            rs.getInt("model_id"),
                            rs.getInt("service_year")
                    ));
                } catch (SQLException e) {
                    System.err.println("DATA ERROR in a WAGON row: " + e.getMessage());
                }
            }
        }
        if (wagons.isEmpty()) {
            wagons.add(new Wagon("100", 1, 2020));
        }
        return wagons;
    }

    /**
     * CREATE: Adds a new Wagon.
     */
    public void addWagon(String idStr, int modelId, int serviceYear) throws SQLException {
        Connection conn = null;
        try {
            conn = DatabaseConnection.getConnection();
            conn.setAutoCommit(false);
            int rowsAffected = 0;

            String sqlRS = "INSERT INTO ROLLING_STOCK (stock_id, model, operator_id) VALUES (?, ?, ?)";
            try (PreparedStatement stmtRS = conn.prepareStatement(sqlRS)) {
                stmtRS.setString(1, idStr);
                stmtRS.setString(2, "WAGON_MODEL_" + modelId);
                stmtRS.setString(3, DEFAULT_OPERATOR_ID);
                rowsAffected += stmtRS.executeUpdate();
            }

            String sqlW = "INSERT INTO WAGON (stock_id, model_id, service_year) VALUES (?, ?, ?)";
            try (PreparedStatement stmtW = conn.prepareStatement(sqlW)) {
                stmtW.setString(1, idStr);
                stmtW.setInt(2, modelId);
                stmtW.setInt(3, serviceYear);
                rowsAffected += stmtW.executeUpdate();
            }

            if (rowsAffected < 2) throw new SQLException("Failed to create Wagon.", "02000");

            conn.commit();
        } catch (SQLException e) {
            if (conn != null) conn.rollback();
            throw e;
        } finally {
            if (conn != null) {
                conn.setAutoCommit(true);
                conn.close();
            }
        }
    }

    /**
     * UPDATE: Updates an existing Wagon.
     */
    public void updateWagon(String idStr, int newModelId, int newServiceYear) throws SQLException {
        Connection conn = null;
        try {
            conn = DatabaseConnection.getConnection();
            conn.setAutoCommit(false);
            int rowsAffected = 0;

            String sqlRS = "UPDATE ROLLING_STOCK SET model = ? WHERE stock_id = ?";
            try (PreparedStatement stmtRS = conn.prepareStatement(sqlRS)) {
                stmtRS.setString(1, "WAGON_MODEL_" + newModelId);
                stmtRS.setString(2, idStr);
                rowsAffected += stmtRS.executeUpdate();
            }

            String sqlW = "UPDATE WAGON SET model_id = ?, service_year = ? WHERE stock_id = ?";
            try (PreparedStatement stmtW = conn.prepareStatement(sqlW)) {
                stmtW.setInt(1, newModelId);
                stmtW.setInt(2, newServiceYear);
                stmtW.setString(3, idStr);
                rowsAffected += stmtW.executeUpdate();
            }

            if (rowsAffected < 2) throw new SQLException("Update failed.", "02000");

            conn.commit();
        } catch (SQLException e) {
            if (conn != null) conn.rollback();
            throw e;
        } finally {
            if (conn != null) {
                conn.setAutoCommit(true);
                conn.close();
            }
        }
    }

    /**
     * DELETE: Removes a Wagon.
     */
    public void deleteWagon(String idStr) throws SQLException {
        Connection conn = null;
        try {
            conn = DatabaseConnection.getConnection();
            conn.setAutoCommit(false);

            String sqlTWU = "DELETE FROM TRAIN_WAGON_USAGE WHERE wagon_id = ?";
            try (PreparedStatement stmtTWU = conn.prepareStatement(sqlTWU)) {
                stmtTWU.setString(1, idStr);
                stmtTWU.executeUpdate();
            }

            String sqlW = "DELETE FROM WAGON WHERE stock_id = ?";
            try (PreparedStatement stmtW = conn.prepareStatement(sqlW)) {
                stmtW.setString(1, idStr);
                stmtW.executeUpdate();
            }

            String sqlRS = "DELETE FROM ROLLING_STOCK WHERE stock_id = ?";
            try (PreparedStatement stmtRS = conn.prepareStatement(sqlRS)) {
                stmtRS.setString(1, idStr);
                stmtRS.executeUpdate();
            }

            conn.commit();
        } catch (SQLException e) {
            if (conn != null) conn.rollback();
            throw e;
        } finally {
            if (conn != null) {
                conn.setAutoCommit(true);
                conn.close();
            }
        }
    }

    // =========================================================================
    // GENERIC METHODS (DYNAMIC VIEWER)
    // =========================================================================

    public List<Map<String, Object>> findGenericTableData(String tableName) throws SQLException {
        List<Map<String, Object>> data = new ArrayList<>();
        String sql = "SELECT * FROM " + tableName;
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            ResultSetMetaData metaData = rs.getMetaData();
            int columnCount = metaData.getColumnCount();
            while (rs.next()) {
                Map<String, Object> row = new HashMap<>();
                for (int i = 1; i <= columnCount; i++) {
                    row.put(metaData.getColumnName(i), rs.getObject(i));
                }
                data.add(row);
            }
        }
        return data;
    }

    public int executeDML(String sql, List<Object> params) throws SQLException {
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            for (int i = 0; i < params.size(); i++) {
                stmt.setObject(i + 1, params.get(i));
            }
            return stmt.executeUpdate();
        }
    }
}
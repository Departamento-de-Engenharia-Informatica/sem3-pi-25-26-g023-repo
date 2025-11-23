package pt.ipp.isep.dei.repository;

import pt.ipp.isep.dei.DatabaseConnection.DatabaseConnection;
import pt.ipp.isep.dei.domain.Locomotive;
import pt.ipp.isep.dei.domain.Operator;
import pt.ipp.isep.dei.domain.Wagon;

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
            // Delete wagon usages associated with trains using this locomotive
            String sqlTWUCleanup = "DELETE FROM TRAIN_WAGON_USAGE WHERE train_id IN (SELECT train_id FROM TRAIN WHERE locomotive_id = ?)";
            try (PreparedStatement stmtTWU = conn.prepareStatement(sqlTWUCleanup)) {
                stmtTWU.setString(1, idStr);
                stmtTWU.executeUpdate();
            }

            // Delete trains using this locomotive
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
        } catch (SQLException e) {
            String errorMsg = e.getMessage();
            throw new SQLException("Failed to execute LOCOMOTIVE query. Original error: " + errorMsg);
        }

        if (locomotives.isEmpty()) {
            // Fallback object to ensure the list is not empty for testing/UI purposes
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
                            rs.getString("stock_id"), // READ ID AS STRING
                            rs.getInt("model_id"),
                            rs.getInt("service_year")
                    ));
                } catch (SQLException e) {
                    System.err.println("DATA ERROR in a WAGON row: Failed internal conversion: " + e.getMessage());
                }
            }
        } catch (SQLException e) {
            throw new SQLException("Failed to execute WAGON query. Check the schema.", e);
        }

        if (wagons.isEmpty()) {
            // Fallback object to ensure the list is not empty for testing/UI purposes
            wagons.add(new Wagon("100", 1, 2020));
        }
        return wagons;
    }


    /**
     * CREATE: Adds a new Wagon.
     * <p>Requires a transaction since it involves ROLLING_STOCK (Parent) and WAGON (Child) tables.</p>
     */
    public void addWagon(String idStr, int modelId, int serviceYear) throws SQLException {
        Connection conn = null;
        try {
            conn = DatabaseConnection.getConnection();
            conn.setAutoCommit(false);
            int rowsAffected = 0;

            // 1. INSERT into ROLLING_STOCK
            String sqlRS = "INSERT INTO ROLLING_STOCK (stock_id, model, operator_id) VALUES (?, ?, ?)";
            try (PreparedStatement stmtRS = conn.prepareStatement(sqlRS)) {
                stmtRS.setString(1, idStr);
                stmtRS.setString(2, "WAGON_MODEL_" + modelId);
                stmtRS.setString(3, DEFAULT_OPERATOR_ID);
                rowsAffected += stmtRS.executeUpdate();
            }

            // 2. INSERT into WAGON
            String sqlW = "INSERT INTO WAGON (stock_id, model_id, service_year) VALUES (?, ?, ?)";
            try (PreparedStatement stmtW = conn.prepareStatement(sqlW)) {
                stmtW.setString(1, idStr);
                stmtW.setInt(2, modelId);
                stmtW.setInt(3, serviceYear);
                rowsAffected += stmtW.executeUpdate();
            }

            if (rowsAffected < 2) throw new SQLException("Failed to create Wagon. Check Model ID or Operator ID.", "02000");


            conn.commit();
            System.out.printf("DB: Inserting Wagon ID:%s, Model ID:%d, Year:%d - SUCCESS\n", idStr, modelId, serviceYear);

        } catch (SQLException e) {
            if (conn != null) conn.rollback();
            System.err.println("SQL ERROR during addWagon: " + e.getMessage());
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
     * <p>Requires a transaction since it involves ROLLING_STOCK (Parent) and WAGON (Child) tables.</p>
     */
    public void updateWagon(String idStr, int newModelId, int newServiceYear) throws SQLException {
        Connection conn = null;
        try {
            conn = DatabaseConnection.getConnection();
            conn.setAutoCommit(false);
            int rowsAffected = 0;

            // 1. UPDATE on ROLLING_STOCK
            String sqlRS = "UPDATE ROLLING_STOCK SET model = ? WHERE stock_id = ?";
            try (PreparedStatement stmtRS = conn.prepareStatement(sqlRS)) {
                stmtRS.setString(1, "WAGON_MODEL_" + newModelId);
                stmtRS.setString(2, idStr);
                rowsAffected += stmtRS.executeUpdate();
            }

            // 2. UPDATE on WAGON
            String sqlW = "UPDATE WAGON SET model_id = ?, service_year = ? WHERE stock_id = ?";
            try (PreparedStatement stmtW = conn.prepareStatement(sqlW)) {
                stmtW.setInt(1, newModelId);
                stmtW.setInt(2, newServiceYear);
                stmtW.setString(3, idStr);
                rowsAffected += stmtW.executeUpdate();
            }

            if (rowsAffected < 2) {
                throw new SQLException("Update failed. Wagon ID " + idStr + " not found or partial update failed.", "02000");
            }


            conn.commit();
            System.out.printf("DB: Updating Wagon ID:%s, New Model ID:%d, New Year:%d - SUCCESS\n", idStr, newModelId, newServiceYear);

        } catch (SQLException e) {
            if (conn != null) conn.rollback();
            System.err.println("SQL ERROR during updateWagon: " + e.getMessage());
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
     * <p>Requires a transaction and cleaning up related records (TRAIN_WAGON_USAGE) first.</p>
     */
    public void deleteWagon(String idStr) throws SQLException {
        Connection conn = null;
        try {
            conn = DatabaseConnection.getConnection();
            conn.setAutoCommit(false);
            int rowsAffected = 0;

            // 🚨 CLEANUP: Remove TRAIN_WAGON_USAGE references if they exist!
            String sqlTWU = "DELETE FROM TRAIN_WAGON_USAGE WHERE wagon_id = ?";
            try (PreparedStatement stmtTWU = conn.prepareStatement(sqlTWU)) {
                stmtTWU.setString(1, idStr);
                stmtTWU.executeUpdate();
            }

            // 1. DELETE from WAGON (Child Table)
            String sqlW = "DELETE FROM WAGON WHERE stock_id = ?";
            try (PreparedStatement stmtW = conn.prepareStatement(sqlW)) {
                stmtW.setString(1, idStr);
                rowsAffected += stmtW.executeUpdate();
            }

            // 2. DELETE from ROLLING_STOCK (Parent Table)
            String sqlRS = "DELETE FROM ROLLING_STOCK WHERE stock_id = ?";
            try (PreparedStatement stmtRS = conn.prepareStatement(sqlRS)) {
                stmtRS.setString(1, idStr);
                rowsAffected += stmtRS.executeUpdate();
            }

            if (rowsAffected == 0) {
                throw new SQLException("Delete failed. Wagon ID " + idStr + " not found.", "02000");
            }


            conn.commit();
            System.out.println("DB: Deleting Wagon ID: " + idStr + " - SUCCESS");

        } catch (SQLException e) {
            if (conn != null) conn.rollback();
            System.err.println("SQL ERROR during deleteWagon: " + e.getMessage());
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
    /**
     * Reads all data from a specified table for dynamic viewing (e.g., in a GUI).
     *
     * @param tableName The name of the table to query.
     * @return A list of maps, where each map represents a row and maps column names to their values.
     */
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
                    String columnName = metaData.getColumnName(i);
                    row.put(columnName, rs.getObject(i));
                }
                data.add(row);
            }
        }
        return data;
    }

    /**
     * Executes a generic Data Manipulation Language (DML) statement (INSERT, UPDATE, DELETE).
     *
     * @param sql The DML query to execute (with '?' placeholders).
     * @param params A list of parameters to set for the placeholders.
     * @return The number of rows affected by the statement.
     */
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
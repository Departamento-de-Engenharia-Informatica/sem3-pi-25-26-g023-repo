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

public class DatabaseRepository {

    public static final List<String> MASTER_TABLES = List.of(
            "OPERATOR", "STATION", "RAILWAY_LINE", "LOCOMOTIVE", "WAGON_MODEL"
    );

    private static final String DEFAULT_OPERATOR_ID = "MEDWAY";

    // =========================================================================
    // MÉTODOS ESPECÍFICOS PARA OPERATOR (CRUD)
    // =========================================================================

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
    // MÉTODOS ESPECÍFICOS PARA LOCOMOTIVE (CRUD)
    // =========================================================================

    /**
     * CREATE: Adiciona uma nova Locomotive.
     */
    public void addLocomotive(int id, String model, String type, double powerKW) throws SQLException {
        Connection conn = null;
        String idStr = String.valueOf(id);
        try {
            conn = DatabaseConnection.getConnection();
            conn.setAutoCommit(false);
            int rowsAffected = 0;

            // 1. INSERT em ROLLING_STOCK
            String sqlRS = "INSERT INTO ROLLING_STOCK (stock_id, model, operator_id) VALUES (?, ?, ?)";
            try (PreparedStatement stmtRS = conn.prepareStatement(sqlRS)) {
                stmtRS.setString(1, idStr);
                stmtRS.setString(2, model);
                stmtRS.setString(3, DEFAULT_OPERATOR_ID);
                rowsAffected += stmtRS.executeUpdate();
            }

            // 2. INSERT em LOCOMOTIVE
            String sqlL = "INSERT INTO LOCOMOTIVE (stock_id, locomotive_type, power_kw) VALUES (?, ?, ?)";
            try (PreparedStatement stmtL = conn.prepareStatement(sqlL)) {
                stmtL.setString(1, idStr);
                stmtL.setString(2, type);
                stmtL.setDouble(3, powerKW);
                rowsAffected += stmtL.executeUpdate();
            }

            if (rowsAffected < 2) throw new SQLException("Failed to create Locomotive. Check if Operator ID is valid.", "02000");

            conn.commit();
            System.out.printf("DB: Inserindo Locomotive ID:%s, Model:%s, Type:%s, Power:%.1f kW - SUCESSO\n", idStr, model, type, powerKW);

        } catch (SQLException e) {
            if (conn != null) {
                conn.rollback();
            }
            System.err.println("SQL ERROR durante addLocomotive: " + e.getMessage());
            throw e;
        } finally {
            if (conn != null) {
                conn.setAutoCommit(true);
                conn.close();
            }
        }
    }

    /**
     * UPDATE: Atualiza uma Locomotive existente.
     */
    public void updateLocomotive(int id, String newModel, String newType, double newPowerKW) throws SQLException {
        Connection conn = null;
        String idStr = String.valueOf(id);
        try {
            conn = DatabaseConnection.getConnection();
            conn.setAutoCommit(false);
            int rowsAffected = 0;

            // 1. UPDATE em ROLLING_STOCK
            String sqlRS = "UPDATE ROLLING_STOCK SET model = ? WHERE stock_id = ?";
            try (PreparedStatement stmtRS = conn.prepareStatement(sqlRS)) {
                stmtRS.setString(1, newModel);
                stmtRS.setString(2, idStr);
                rowsAffected += stmtRS.executeUpdate();
            }

            // 2. UPDATE em LOCOMOTIVE
            String sqlL = "UPDATE LOCOMOTIVE SET locomotive_type = ?, power_kw = ? WHERE stock_id = ?";
            try (PreparedStatement stmtL = conn.prepareStatement(sqlL)) {
                stmtL.setString(1, newType);
                stmtL.setDouble(2, newPowerKW);
                stmtL.setString(3, idStr);
                rowsAffected += stmtL.executeUpdate();
            }

            // Verifica se as duas tabelas foram atualizadas
            if (rowsAffected < 2) {
                throw new SQLException("Update failed. Locomotive ID " + idStr + " not found or partial update failed.", "02000");
            }

            conn.commit();
            System.out.printf("DB: Atualizando Locomotive ID:%s, Novo Model:%s, Novo Type:%s, Nova Power:%.1f kW - SUCESSO\n", idStr, newModel, newType, newPowerKW);

        } catch (SQLException e) {
            if (conn != null) {
                conn.rollback();
            }
            System.err.println("SQL ERROR durante updateLocomotive: " + e.getMessage());
            throw e;
        } finally {
            if (conn != null) {
                conn.setAutoCommit(true);
                conn.close();
            }
        }
    }

    /**
     * DELETE: Remove uma Locomotive.
     */
    public void deleteLocomotive(int id) throws SQLException {
        Connection conn = null;
        String idStr = String.valueOf(id);
        try {
            conn = DatabaseConnection.getConnection();
            conn.setAutoCommit(false);
            int rowsAffected = 0;

            // 🚨 0. LIMPEZA: TRAIN e TRAIN_WAGON_USAGE 🚨
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

            // 1. DELETE em LOCOMOTIVE (Tabela Filha)
            String sqlL = "DELETE FROM LOCOMOTIVE WHERE stock_id = ?";
            try (PreparedStatement stmtL = conn.prepareStatement(sqlL)) {
                rowsAffected += stmtL.executeUpdate();
            }

            // 2. DELETE em ROLLING_STOCK (Tabela Pai)
            String sqlRS = "DELETE FROM ROLLING_STOCK WHERE stock_id = ?";
            try (PreparedStatement stmtRS = conn.prepareStatement(sqlRS)) {
                rowsAffected += stmtRS.executeUpdate();
            }

            if (rowsAffected == 0) {
                throw new SQLException("Delete failed. Locomotive ID " + idStr + " not found.", "02000");
            }

            conn.commit();
            System.out.println("DB: Eliminando Locomotive ID: " + idStr + " - SUCESSO");

        } catch (SQLException e) {
            if (conn != null) {
                conn.rollback();
            }
            System.err.println("SQL ERROR durante deleteLocomotive: " + e.getMessage());
            throw e;
        } finally {
            if (conn != null) {
                conn.setAutoCommit(true);
                conn.close();
            }
        }
    }

    /**
     * READ ESPECÍFICO: Retorna todas as Locomotives da base de dados.
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
                    System.err.println("ERRO DE DADOS NUMA LINHA DA LOCOMOTIVE: " + e.getMessage());
                }
            }
        } catch (SQLException e) {
            String errorMsg = e.getMessage();
            throw new SQLException("Falha ao executar consulta de LOCOMOTIVE. Erro original: " + errorMsg);
        }

        if (locomotives.isEmpty()) {
            locomotives.add(new Locomotive(999, "Fallback Model", "diesel", 100.0));
        }

        return locomotives;
    }

    // =========================================================================
    // MÉTODOS ESPECÍFICOS PARA WAGON (CRUD)
    // =========================================================================

    /**
     * READ: Retorna todos os Vagões.
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
                            rs.getString("stock_id"), // LER ID COMO STRING
                            rs.getInt("model_id"),
                            rs.getInt("service_year")
                    ));
                } catch (SQLException e) {
                    System.err.println("ERRO DE DADOS NUMA LINHA DA WAGON: Falha na conversão para representação interna: " + e.getMessage());
                }
            }
        } catch (SQLException e) {
            throw new SQLException("Falha ao executar consulta de WAGON. Verifique o esquema.", e);
        }

        if (wagons.isEmpty()) {
            wagons.add(new Wagon("100", 1, 2020));
        }
        return wagons;
    }


    /**
     * CREATE: Adiciona um novo Wagon.
     */
    public void addWagon(String idStr, int modelId, int serviceYear) throws SQLException {
        Connection conn = null;
        try {
            conn = DatabaseConnection.getConnection();
            conn.setAutoCommit(false);
            int rowsAffected = 0;

            // 1. INSERT em ROLLING_STOCK
            String sqlRS = "INSERT INTO ROLLING_STOCK (stock_id, model, operator_id) VALUES (?, ?, ?)";
            try (PreparedStatement stmtRS = conn.prepareStatement(sqlRS)) {
                stmtRS.setString(1, idStr);
                stmtRS.setString(2, "WAGON_MODEL_" + modelId);
                stmtRS.setString(3, DEFAULT_OPERATOR_ID);
                rowsAffected += stmtRS.executeUpdate();
            }

            // 2. INSERT em WAGON
            String sqlW = "INSERT INTO WAGON (stock_id, model_id, service_year) VALUES (?, ?, ?)";
            try (PreparedStatement stmtW = conn.prepareStatement(sqlW)) {
                stmtW.setString(1, idStr);
                stmtW.setInt(2, modelId);
                stmtW.setInt(3, serviceYear);
                rowsAffected += stmtW.executeUpdate();
            }

            if (rowsAffected < 2) throw new SQLException("Failed to create Wagon. Check Model ID or Operator ID.", "02000");


            conn.commit();
            System.out.printf("DB: Inserindo Wagon ID:%s, Model ID:%d, Year:%d - SUCESSO\n", idStr, modelId, serviceYear);

        } catch (SQLException e) {
            if (conn != null) conn.rollback();
            System.err.println("SQL ERROR durante addWagon: " + e.getMessage());
            throw e;
        } finally {
            if (conn != null) {
                conn.setAutoCommit(true);
                conn.close();
            }
        }
    }

    /**
     * UPDATE: Atualiza um Wagon existente.
     */
    public void updateWagon(String idStr, int newModelId, int newServiceYear) throws SQLException {
        Connection conn = null;
        try {
            conn = DatabaseConnection.getConnection();
            conn.setAutoCommit(false);
            int rowsAffected = 0;

            // 1. UPDATE em ROLLING_STOCK
            String sqlRS = "UPDATE ROLLING_STOCK SET model = ? WHERE stock_id = ?";
            try (PreparedStatement stmtRS = conn.prepareStatement(sqlRS)) {
                stmtRS.setString(1, "WAGON_MODEL_" + newModelId);
                stmtRS.setString(2, idStr);
                rowsAffected += stmtRS.executeUpdate();
            }

            // 2. UPDATE em WAGON
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
            System.out.printf("DB: Atualizando Wagon ID:%s, Novo Model ID:%d, Novo Year:%d - SUCESSO\n", idStr, newModelId, newServiceYear);

        } catch (SQLException e) {
            if (conn != null) conn.rollback();
            System.err.println("SQL ERROR durante updateWagon: " + e.getMessage());
            throw e;
        } finally {
            if (conn != null) {
                conn.setAutoCommit(true);
                conn.close();
            }
        }
    }

    /**
     * DELETE: Remove um Wagon.
     */
    public void deleteWagon(String idStr) throws SQLException {
        Connection conn = null;
        try {
            conn = DatabaseConnection.getConnection();
            conn.setAutoCommit(false);
            int rowsAffected = 0;

            // 🚨 LIMPEZA: Remover referências de TRAIN_WAGON_USAGE se existirem!
            String sqlTWU = "DELETE FROM TRAIN_WAGON_USAGE WHERE wagon_id = ?";
            try (PreparedStatement stmtTWU = conn.prepareStatement(sqlTWU)) {
                stmtTWU.setString(1, idStr);
                stmtTWU.executeUpdate();
            }

            // 1. DELETE em WAGON (Tabela Filha)
            String sqlW = "DELETE FROM WAGON WHERE stock_id = ?";
            try (PreparedStatement stmtW = conn.prepareStatement(sqlW)) {
                rowsAffected += stmtW.executeUpdate();
            }

            // 2. DELETE em ROLLING_STOCK (Tabela Pai)
            String sqlRS = "DELETE FROM ROLLING_STOCK WHERE stock_id = ?";
            try (PreparedStatement stmtRS = conn.prepareStatement(sqlRS)) {
                rowsAffected += stmtRS.executeUpdate();
            }

            if (rowsAffected == 0) {
                throw new SQLException("Delete failed. Wagon ID " + idStr + " not found.", "02000");
            }


            conn.commit();
            System.out.println("DB: Eliminando Wagon ID: " + idStr + " - SUCESSO");

        } catch (SQLException e) {
            if (conn != null) conn.rollback();
            System.err.println("SQL ERROR durante deleteWagon: " + e.getMessage());
            throw e;
        } finally {
            if (conn != null) {
                conn.setAutoCommit(true);
                conn.close();
            }
        }
    }


    // =========================================================================
    // MÉTODOS GENÉRICOS (VIEWER DINÂMICO)
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
                    String columnName = metaData.getColumnName(i);
                    row.put(columnName, rs.getObject(i));
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
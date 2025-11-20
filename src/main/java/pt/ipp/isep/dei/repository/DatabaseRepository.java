package pt.ipp.isep.dei.repository;

import pt.ipp.isep.dei.DatabaseConnection.DatabaseConnection;
import pt.ipp.isep.dei.domain.Operator; // Necessário para o método findAllOperators

import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Repositório genérico para operações de leitura (READ) e execução de DML (CRUD).
 * Inclui métodos específicos para a tabela OPERATOR (requeridos pelo CRUD inicial).
 */
public class DatabaseRepository {

    /**
     * Lista de tabelas mestras que o utilizador pode querer inspecionar.
     */
    public static final List<String> MASTER_TABLES = List.of(
            "OPERATOR", "STATION", "RAILWAY_LINE", "LOCOMOTIVE", "WAGON_MODEL"
    );

    // =========================================================================
    // MÉTODOS ESPECÍFICOS PARA OPERATOR (CRUD)
    // =========================================================================

    /**
     * READ ESPECÍFICO: Busca todos os operadores. (Necessário para a TableView do CRUD).
     */
    public List<Operator> findAllOperators() throws SQLException {
        List<Operator> operators = new ArrayList<>();
        String sql = "SELECT operator_id, name FROM OPERATOR ORDER BY operator_id";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                // Assumimos que o modelo Operator existe e é compatível
                operators.add(new Operator(rs.getString("operator_id"), rs.getString("name")));
            }
        }
        return operators;
    }

    /**
     * CREATE: Adiciona um novo operador.
     */
    public void addOperator(String id, String name) throws SQLException {
        String sql = "INSERT INTO OPERATOR (operator_id, name) VALUES (?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, id);
            stmt.setString(2, name);
            stmt.executeUpdate();
        }
    }

    /**
     * UPDATE: Atualiza o nome de um operador existente.
     */
    public void updateOperator(String id, String newName) throws SQLException {
        String sql = "UPDATE OPERATOR SET name = ? WHERE operator_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, newName);
            stmt.setString(2, id);
            stmt.executeUpdate();
        }
    }

    /**
     * DELETE: Remove um operador.
     */
    public void deleteOperator(String id) throws SQLException {
        String sql = "DELETE FROM OPERATOR WHERE operator_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, id);
            stmt.executeUpdate();
        }
    }

    // =========================================================================
    // MÉTODOS GENÉRICOS (VIEWER DINÂMICO)
    // =========================================================================

    /**
     * READ GENÉRICO: Busca todos os dados de uma tabela em formato genérico (Map).
     *
     * @param tableName Nome da tabela (ex: "OPERATOR").
     * @return Lista de Mapas, onde cada Mapa é uma linha (ColName -> Value).
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
                    // Captura o valor como Object; JavaFX lida com a conversão posterior.
                    row.put(columnName, rs.getObject(i));
                }
                data.add(row);
            }
        }
        return data;
    }

    /**
     * UPDATE/DELETE: Executa uma instrução DML simples (Genérico para futuras US).
     */
    public int executeDML(String sql, List<Object> params) throws SQLException {
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            for (int i = 0; i < params.size(); i++) {
                // Simplificação: assume que todos os parâmetros são Strings ou numéricos.
                stmt.setObject(i + 1, params.get(i));
            }
            return stmt.executeUpdate();
        }
    }
}
package pt.ipp.isep.dei.repository;

import pt.ipp.isep.dei.DatabaseConnection.DatabaseConnection; // Importa conexão
import pt.ipp.isep.dei.domain.Locomotiva;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class LocomotivaRepository {

    // Construtor vazio
    public LocomotivaRepository() {
        // System.out.println("LocomotivaRepository: Initialized (will connect to DB on demand)."); // <-- COMENTADO
    }

    // ... (restante da classe LocomotivaRepository igual à versão anterior) ...

    public List<Locomotiva> findAll() { /* ... código JDBC ... */
        List<Locomotiva> locomotivas = new ArrayList<>();
        String sql = "SELECT stock_id, model, type FROM ROLLING_STOCK WHERE type IN ('Electric', 'Diesel') ORDER BY stock_id"; //
        try (Connection conn = DatabaseConnection.getConnection(); PreparedStatement stmt = conn.prepareStatement(sql); ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) { /* ... Mapeamento ... */
                String stockIdStr = rs.getString("stock_id"); String model = rs.getString("model"); String typeDb = rs.getString("type");
                String tipoJava = mapTypeToDomain(typeDb);
                try { int stockIdInt = Integer.parseInt(stockIdStr); locomotivas.add(new Locomotiva(stockIdInt, model, tipoJava)); //
                } catch (NumberFormatException e) { System.err.println("⚠️ Aviso: Não foi possível converter stock_id '" + stockIdStr + "' para int. Locomotiva ignorada.");}
            }
        } catch (SQLException e) { System.err.println("❌ Erro ao buscar todas as Locomotivas (Rolling Stock) da BD: " + e.getMessage()); }
        return locomotivas;
    }
    public Optional<Locomotiva> findById(int id) { /* ... código JDBC ... */
        String idStr = String.valueOf(id); String sql = "SELECT stock_id, model, type FROM ROLLING_STOCK WHERE type IN ('Electric', 'Diesel') AND stock_id = ?"; //
        Locomotiva locomotiva = null;
        try (Connection conn = DatabaseConnection.getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, idStr);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) { /* ... Mapeamento ... */
                    String stockIdStr = rs.getString("stock_id"); String model = rs.getString("model"); String typeDb = rs.getString("type");
                    String tipoJava = mapTypeToDomain(typeDb);
                    try { int stockIdInt = Integer.parseInt(stockIdStr); locomotiva = new Locomotiva(stockIdInt, model, tipoJava); //
                    } catch (NumberFormatException e) { System.err.println("⚠️ Aviso: Não foi possível converter stock_id '" + stockIdStr + "' para int ao buscar por ID. Locomotiva ignorada."); }
                }
            }
        } catch (SQLException e) { System.err.println("❌ Erro ao buscar Locomotiva (Rolling Stock) por ID " + id + ": " + e.getMessage()); }
        return Optional.ofNullable(locomotiva);
    }
    private String mapTypeToDomain(String dbType) { /* ... código mapeamento ... */
        if ("Electric".equalsIgnoreCase(dbType)) return "eletrica";
        else if ("Diesel".equalsIgnoreCase(dbType)) return "diesel";
        return "desconhecido";
    }
}
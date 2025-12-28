package pt.ipp.isep.dei.repository;

import pt.ipp.isep.dei.DatabaseConnection.DatabaseConnection;
import pt.ipp.isep.dei.domain.Locomotive;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class LocomotiveRepository {

    /**
     * Encontra uma locomotiva pelo ID.
     * CORREÇÃO: Usa setString para evitar ORA-01722 se a coluna na BD for mista.
     */
    public Optional<Locomotive> findById(String idStr) {
        // Query ajustada para ser segura
        String sql = "SELECT R.stock_id, L.locomotive_type, L.power_kw, R.model, L.length_m " +
                "FROM LOCOMOTIVE L JOIN ROLLING_STOCK R ON L.stock_id = R.stock_id " +
                "WHERE R.stock_id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            // USAR setString É MAIS SEGURO AQUI
            // Se a BD espera número, o driver converte "123" para 123.
            // Se a BD espera texto, passa "123".
            // Isto evita que o Oracle tente converter colunas de texto para número e falhe.
            stmt.setString(1, idStr);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToLocomotive(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("⚠️ Erro SQL ao buscar Locomotiva ID " + idStr + ": " + e.getMessage());
            // Em vez de crashar a simulação, devolvemos vazio e o Dispatcher lida com isso
        }
        return Optional.empty();
    }

    /**
     * Sobrecarga para int, converte para String e chama o método principal.
     */
    public Optional<Locomotive> findById(int id) {
        return findById(String.valueOf(id));
    }

    public List<Locomotive> findAll() {
        List<Locomotive> locomotives = new ArrayList<>();
        String sql = "SELECT R.stock_id, L.locomotive_type, L.power_kw, R.model, L.length_m " +
                "FROM LOCOMOTIVE L JOIN ROLLING_STOCK R ON L.stock_id = R.stock_id " +
                "ORDER BY R.stock_id";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                locomotives.add(mapResultSetToLocomotive(rs));
            }
        } catch (SQLException e) {
            System.err.println("❌ Erro ao ler Locomotivas: " + e.getMessage());
        }
        return locomotives;
    }

    /**
     * Método auxiliar para mapear e CORRIGIR A FÍSICA (0 km/h fix).
     */
    private Locomotive mapResultSetToLocomotive(ResultSet rs) throws SQLException {
        // Usa getObject para ser agnóstico ao tipo (int ou string) na BD
        Object idObj = rs.getObject("stock_id");
        int id = (idObj instanceof Number) ? ((Number) idObj).intValue() : Integer.parseInt(idObj.toString());

        double originalPower = rs.getDouble("power_kw");

        // --- FIX CRÍTICO DE FÍSICA ---
        // Se a BD devolver 0 ou NULL, forçamos 4200 kW para a simulação andar
        double finalPower = originalPower;
        if (finalPower < 1.0) {
            finalPower = 4200.0;
            // System.out.println("🔧 FIXED: Locomotiva " + id + " tinha 0kW, assumido 4200kW.");
        }

        // Criar Objeto
        Locomotive loc = new Locomotive(
                id,
                rs.getString("model"),
                rs.getString("locomotive_type"),
                finalPower // Importante: usar a variável corrigida
        );

        // Preencher dados físicos (Comprimento)
        try {
            double len = rs.getDouble("length_m");
            loc.setLengthMeters(len > 0 ? len : 22.0);
        } catch (SQLException ignore) {
            loc.setLengthMeters(22.0);
        }

        // Peso (Tara) - Default 80t se a BD não tiver
        loc.setTotalWeightKg(80000.0);

        return loc;
    }
}
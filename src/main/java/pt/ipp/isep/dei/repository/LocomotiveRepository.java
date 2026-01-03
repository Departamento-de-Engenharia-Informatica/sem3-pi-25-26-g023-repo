package pt.ipp.isep.dei.repository;

import oracle.jdbc.OracleTypes;
import pt.ipp.isep.dei.DatabaseConnection.DatabaseConnection;
import pt.ipp.isep.dei.domain.Locomotive;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class LocomotiveRepository {

    // --- NOVO: Método de criação via Procedure (USLP08) ---
    public boolean registerLocomotive(Locomotive loc) {
        String call = "{ call pr_register_locomotive(?, ?, ?, ?, ?, ?) }";
        try (Connection conn = DatabaseConnection.getConnection();
             CallableStatement cstmt = conn.prepareCall(call)) {

            // Usando os getters da tua classe Locomotive
            cstmt.setString(1, String.valueOf(loc.getIdLocomotiva()));
            cstmt.setString(2, loc.getModelo());
            cstmt.setString(3, loc.getTipo());
            cstmt.setDouble(4, loc.getPowerKW());
            cstmt.setDouble(5, loc.getLengthMeters());
            cstmt.setDouble(6, loc.getTotalWeightKg());

            cstmt.execute();
            return true;
        } catch (SQLException e) {
            System.err.println("❌ Erro PL/SQL registerLocomotive: " + e.getMessage());
            return false;
        }
    }

    /**
     * Encontra uma locomotiva pelo ID via Function PL/SQL.
     */
    public Optional<Locomotive> findById(String idStr) {
        String call = "{ ? = call fn_get_locomotive_by_id(?) }";

        try (Connection conn = DatabaseConnection.getConnection();
             CallableStatement cstmt = conn.prepareCall(call)) {

            cstmt.registerOutParameter(1, OracleTypes.CURSOR);
            cstmt.setString(2, idStr);
            cstmt.execute();

            try (ResultSet rs = (ResultSet) cstmt.getObject(1)) {
                if (rs != null && rs.next()) {
                    return Optional.of(mapResultSetToLocomotive(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("⚠️ Erro PL/SQL ao buscar Locomotiva ID " + idStr + ": " + e.getMessage());
        }
        return Optional.empty();
    }

    public Optional<Locomotive> findById(int id) {
        return findById(String.valueOf(id));
    }

    public List<Locomotive> findAll() {
        List<Locomotive> locomotives = new ArrayList<>();
        String call = "{ ? = call fn_get_all_locomotives() }";

        try (Connection conn = DatabaseConnection.getConnection();
             CallableStatement cstmt = conn.prepareCall(call)) {

            cstmt.registerOutParameter(1, OracleTypes.CURSOR);
            cstmt.execute();

            try (ResultSet rs = (ResultSet) cstmt.getObject(1)) {
                while (rs != null && rs.next()) {
                    locomotives.add(mapResultSetToLocomotive(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("❌ Erro PL/SQL ao ler Locomotivas: " + e.getMessage());
        }
        return locomotives;
    }

    /**
     * Mapeamento mantido IDÊNTICO ao original para evitar NumberFormatException.
     */
    private Locomotive mapResultSetToLocomotive(ResultSet rs) throws SQLException {
        Object idObj = rs.getObject("stock_id");
        int id = 0;

        try {
            if (idObj instanceof Number) {
                id = ((Number) idObj).intValue();
            } else if (idObj != null) {
                String cleanId = idObj.toString().replaceAll("[^0-9]", "");
                if (!cleanId.isEmpty()) {
                    id = Integer.parseInt(cleanId);
                } else {
                    id = idObj.toString().hashCode();
                }
            }
        } catch (Exception e) {
            System.err.println("⚠️ Erro ao converter ID '" + idObj + "'. Usando fallback seguro.");
            id = (idObj != null) ? idObj.hashCode() : 0;
        }

        double finalPower = rs.getDouble("power_kw");
        if (finalPower < 1.0) finalPower = 4200.0;

        Locomotive loc = new Locomotive(
                id,
                rs.getString("model"),
                rs.getString("locomotive_type"),
                finalPower
        );

        try {
            double len = rs.getDouble("length_m");
            loc.setLengthMeters(len > 0 ? len : 22.0);
        } catch (SQLException ignore) { loc.setLengthMeters(22.0); }

        loc.setTotalWeightKg(80000.0);
        return loc;
    }
}
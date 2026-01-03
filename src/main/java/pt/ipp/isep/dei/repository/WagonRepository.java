package pt.ipp.isep.dei.repository;

import oracle.jdbc.OracleTypes;
import pt.ipp.isep.dei.DatabaseConnection.DatabaseConnection;
import pt.ipp.isep.dei.domain.Box;
import pt.ipp.isep.dei.domain.Wagon;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.sql.*;

public class WagonRepository {

    public Optional<Wagon> findById(String id) {
        String call = "{ ? = call fn_get_wagon_by_id(?) }";
        try (Connection conn = DatabaseConnection.getConnection();
             CallableStatement cstmt = conn.prepareCall(call)) {

            cstmt.registerOutParameter(1, OracleTypes.CURSOR);
            cstmt.setString(2, id);
            cstmt.execute();

            try (ResultSet rs = (ResultSet) cstmt.getObject(1)) {
                if (rs != null && rs.next()) {
                    return Optional.of(new Wagon(rs.getString("stock_id"), rs.getInt("model_id"), rs.getInt("service_year")));
                }
            }
        } catch (SQLException e) {
            System.err.println("❌ Error PL/SQL Wagon by ID " + id + ": " + e.getMessage());
        }
        return Optional.empty();
    }

    public List<Wagon> findAll() {
        List<Wagon> wagons = new ArrayList<>();
        String call = "{ ? = call fn_get_all_wagons() }";

        try (Connection conn = DatabaseConnection.getConnection();
             CallableStatement cstmt = conn.prepareCall(call)) {

            cstmt.registerOutParameter(1, OracleTypes.CURSOR);
            cstmt.execute();

            try (ResultSet rs = (ResultSet) cstmt.getObject(1)) {
                while (rs != null && rs.next()) {
                    Wagon w = new Wagon(rs.getString("stock_id"), rs.getInt("model_id"), rs.getInt("service_year"));

                    try { w.setLengthMeters(rs.getDouble("length_m")); } catch(Exception e){}

                    // Lógica original de estimativa de peso
                    String type = rs.getString("wagon_type");
                    double estimatedWeight = 25000;
                    if (type != null) {
                        String t = type.toLowerCase();
                        if (t.contains("coal")) estimatedWeight += 60000;
                        else if (t.contains("steel")) estimatedWeight += 60000;
                        else if (t.contains("cereal")) estimatedWeight += 45000;
                        else estimatedWeight += 30000;
                    }
                    w.setGrossWeightKg(estimatedWeight);
                    wagons.add(w);
                }
            }
        } catch (SQLException e) {
            System.err.println("❌ Error PL/SQL reading Wagon data: " + e.getMessage());
        }
        return wagons;
    }

    public List<Wagon> findWagonsByTrainId(String trainId) {
        List<Wagon> wagons = new ArrayList<>();
        String call = "{ ? = call fn_get_wagons_by_train(?) }";

        try (Connection conn = DatabaseConnection.getConnection();
             CallableStatement cstmt = conn.prepareCall(call)) {

            cstmt.registerOutParameter(1, OracleTypes.CURSOR);
            cstmt.setString(2, trainId);
            cstmt.execute();

            try (ResultSet rs = (ResultSet) cstmt.getObject(1)) {
                while (rs != null && rs.next()) {
                    Wagon wagon = new Wagon(rs.getString("stock_id"), rs.getInt("model_id"), rs.getInt("service_year"));
                    try { wagon.setLengthMeters(rs.getDouble("length_m")); } catch(Exception e){}

                    // Lógica original de Boxes
                    String type = rs.getString("wagon_type");
                    if (type != null) {
                        String cargoContent = "General";
                        int weightKg = 20000;
                        if (type.toLowerCase().contains("cereal")) { cargoContent = "Cereal"; weightKg = 45000; }
                        else if (type.toLowerCase().contains("coal")) { cargoContent = "Coal"; weightKg = 65000; }
                        else if (type.toLowerCase().contains("wood")) { cargoContent = "Wood"; weightKg = 25000; }
                        else if (type.toLowerCase().contains("container")) { cargoContent = "Container"; weightKg = 30000; }

                        wagon.addBox(new Box("BX-" + wagon.getIdWagon(), cargoContent, weightKg, (LocalDate) null, null, "Wagon", "Hold"));
                    }
                    wagons.add(wagon);
                }
            }
        } catch (SQLException e) {
            System.err.println("Erro PL/SQL carregar vagões: " + e.getMessage());
        }
        return wagons;
    }
}
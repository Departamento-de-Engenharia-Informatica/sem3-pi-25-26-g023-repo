package pt.ipp.isep.dei.repository;

import pt.ipp.isep.dei.DatabaseConnection.DatabaseConnection;
import pt.ipp.isep.dei.domain.Locomotive;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Repository class responsible for retrieving and managing {@link Locomotive} entities
 * from the database. This repository interacts directly with the ROLLING_STOCK table,
 * fetching only entries representing locomotives (i.e., those with type 'Electric' or 'Diesel').
 * <p>
 * The repository uses JDBC for database communication and maps SQL query results
 * into domain-level {@code Locomotive} objects.
 */
public class LocomotiveRepository {

    /**
     * Default constructor.
     * <p>
     * Initializes the repository without immediately connecting to the database.
     * The connection is established only when one of the query methods is invoked.
     */
    public LocomotiveRepository() {
        // System.out.println("LocomotivaRepository: Initialized (will connect to DB on demand).");
    }

    /**
     * Retrieves all locomotives stored in the database.
     * <p>
     * This method executes a SQL query to fetch all rows from the ROLLING_STOCK table
     * where the type is either 'Electric' or 'Diesel'. Each record is mapped into
     * a {@link Locomotive} object and returned in a list.
     *
     * @return a list containing all locomotives found in the database; the list is empty if none are found.
     */
    public List<Locomotive> findAll() {
        List<Locomotive> locomotivas = new ArrayList<>();
        String sql = "SELECT stock_id, model, type FROM ROLLING_STOCK WHERE type IN ('Electric', 'Diesel') ORDER BY stock_id";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                String stockIdStr = rs.getString("stock_id");
                String model = rs.getString("model");
                String typeDb = rs.getString("type");

                String tipoJava = mapTypeToDomain(typeDb);

                try {
                    int stockIdInt = Integer.parseInt(stockIdStr);
                    locomotivas.add(new Locomotive(stockIdInt, model, tipoJava));
                } catch (NumberFormatException e) {
                    System.err.println("⚠️ Warning: Unable to convert stock_id '" + stockIdStr + "' to int. Locomotive ignored.");
                }
            }

        } catch (SQLException e) {
            System.err.println("❌ Error retrieving all Locomotives (Rolling Stock) from the database: " + e.getMessage());
        }

        return locomotivas;
    }

    /**
     * Retrieves a specific locomotive from the database by its unique identifier.
     * <p>
     * Executes a parameterized SQL query to fetch a single record from the ROLLING_STOCK table
     * that matches the given {@code stock_id} and whose type is either 'Electric' or 'Diesel'.
     *
     * @param id the unique identifier of the locomotive (stock_id in the database)
     * @return an {@link Optional} containing the found {@link Locomotive}, or an empty Optional if no match is found.
     */
    public Optional<Locomotive> findById(int id) {
        String idStr = String.valueOf(id);
        String sql = "SELECT stock_id, model, type FROM ROLLING_STOCK WHERE type IN ('Electric', 'Diesel') AND stock_id = ?";
        Locomotive locomotiva = null;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, idStr);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    String stockIdStr = rs.getString("stock_id");
                    String model = rs.getString("model");
                    String typeDb = rs.getString("type");
                    String tipoJava = mapTypeToDomain(typeDb);

                    try {
                        int stockIdInt = Integer.parseInt(stockIdStr);
                        locomotiva = new Locomotive(stockIdInt, model, tipoJava);
                    } catch (NumberFormatException e) {
                        System.err.println("⚠️ Warning: Unable to convert stock_id '" + stockIdStr + "' to int when fetching by ID. Locomotive ignored.");
                    }
                }
            }

        } catch (SQLException e) {
            System.err.println("❌ Error retrieving Locomotive (Rolling Stock) by ID " + id + ": " + e.getMessage());
        }

        return Optional.ofNullable(locomotiva);
    }

    /**
     * Maps the database-level locomotive type to the corresponding domain-level representation.
     * <p>
     * Converts values such as "Electric" and "Diesel" from the database
     * into their respective Portuguese domain equivalents ("eletrica", "diesel").
     * Any unknown type defaults to "desconhecido".
     *
     * @param dbType the type string retrieved from the database (e.g., "Electric", "Diesel")
     * @return a domain-level type string (e.g., "eletrica", "diesel", or "desconhecido").
     */
    private String mapTypeToDomain(String dbType) {
        if ("Electric".equalsIgnoreCase(dbType)) return "eletrica";
        else if ("Diesel".equalsIgnoreCase(dbType)) return "diesel";
        return "desconhecido";
    }
}

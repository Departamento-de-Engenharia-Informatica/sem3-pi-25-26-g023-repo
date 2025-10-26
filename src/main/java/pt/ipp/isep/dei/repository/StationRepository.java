package pt.ipp.isep.dei.repository;

import pt.ipp.isep.dei.DatabaseConnection.DatabaseConnection;
import pt.ipp.isep.dei.domain.Station;

import java.sql.*;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Repository class responsible for loading and providing access to {@link Station} entities.
 * <p>
 * This repository retrieves stations that are referenced in the {@code RAILWAY_LINE} table,
 * based on the start and end facilities. It caches results in memory to avoid redundant
 * database queries.
 * </p>
 *
 * <p>
 * The repository assumes that each station corresponds to a facility entry in the
 * {@code FACILITY} table. Only facilities that are directly referenced by railway lines
 * will be loaded.
 * </p>
 *
 * <p><b>Note:</b> This class relies on {@link DatabaseConnection} for database access.</p>
 *
 */
public class StationRepository {

    /** Cached list of stations loaded from the database. */
    private List<Station> loadedEstacoes = null;

    /**
     * Default constructor.
     * <p>
     * Initializes the repository. The actual data loading happens lazily
     * when a query method (e.g., {@link #findAll()}) is first called.
     * </p>
     */
    public StationRepository() {
        // Lazy loading design: data is only fetched when needed.
    }

    /**
     * Loads all stations referenced in the {@code RAILWAY_LINE} table, if not already loaded.
     * <p>
     * This method retrieves the unique set of facility IDs that appear as
     * {@code start_facility_id} or {@code end_facility_id} in {@code RAILWAY_LINE}.
     * Then, it loads their corresponding names from the {@code FACILITY} table
     * and creates {@link Station} objects.
     * </p>
     *
     * <p>
     * The loaded stations are stored in memory to improve performance for subsequent calls.
     * </p>
     */
    private void loadStationsIfNeeded() {
        if (loadedEstacoes != null) return;

        loadedEstacoes = new ArrayList<>();
        Set<Integer> uniqueFacilityIds = new HashSet<>();
        String sqlGetIds = "SELECT start_facility_id, end_facility_id FROM RAILWAY_LINE";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmtIds = conn.prepareStatement(sqlGetIds);
             ResultSet rsIds = stmtIds.executeQuery()) {

            while (rsIds.next()) {
                uniqueFacilityIds.add(rsIds.getInt("start_facility_id"));
                uniqueFacilityIds.add(rsIds.getInt("end_facility_id"));
            }

        } catch (SQLException e) {
            System.err.println("❌ Error while fetching facility IDs from RAILWAY_LINE: " + e.getMessage());
            loadedEstacoes = new ArrayList<>();
            return;
        }

        if (uniqueFacilityIds.isEmpty()) {
            loadedEstacoes = new ArrayList<>();
            return;
        }

        String sqlGetDetailsBase = "SELECT facility_id, name FROM FACILITY WHERE facility_id IN (";
        StringBuilder sqlGetDetails = new StringBuilder(sqlGetDetailsBase);
        sqlGetDetails.append(uniqueFacilityIds.stream()
                .map(String::valueOf)
                .collect(Collectors.joining(",")));
        sqlGetDetails.append(") ORDER BY facility_id");

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmtDetails = conn.prepareStatement(sqlGetDetails.toString());
             ResultSet rsDetails = stmtDetails.executeQuery()) {

            while (rsDetails.next()) {
                loadedEstacoes.add(new Station(
                        rsDetails.getInt("facility_id"),
                        rsDetails.getString("name")));
            }

        } catch (SQLException e) {
            System.err.println("❌ Error while fetching facility details: " + e.getMessage());
            loadedEstacoes = new ArrayList<>();
        }
    }

    /**
     * Retrieves all stations loaded from the database.
     *
     * @return a list of all {@link Station} objects referenced in {@code RAILWAY_LINE}
     */
    public List<Station> findAll() {
        loadStationsIfNeeded();
        return new ArrayList<>(loadedEstacoes);
    }

    /**
     * Finds a station by its unique identifier.
     *
     * @param id the ID of the station
     * @return an {@link Optional} containing the {@link Station} if found, otherwise empty
     */
    public Optional<Station> findById(int id) {
        loadStationsIfNeeded();
        return loadedEstacoes.stream()
                .filter(e -> e.getIdEstacao() == id)
                .findFirst();
    }

    /**
     * Finds a station by its name (case-insensitive).
     *
     * @param nome the name of the station
     * @return an {@link Optional} containing the {@link Station} if found, otherwise empty
     */
    public Optional<Station> findByNome(String nome) {
        loadStationsIfNeeded();
        return loadedEstacoes.stream()
                .filter(e -> e.getNome().equalsIgnoreCase(nome))
                .findFirst();
    }
}

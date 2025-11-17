package pt.ipp.isep.dei.repository;

import pt.ipp.isep.dei.DatabaseConnection.DatabaseConnection;
import pt.ipp.isep.dei.domain.EuropeanStation;
import pt.ipp.isep.dei.domain.Station; // Manter a importação de Station se necessário noutras partes

import java.sql.*;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Repository class responsible for loading and providing access to {@link EuropeanStation} entities.
 */
public class StationRepository {

    /** Cached list of stations loaded from the database. */
    private List<EuropeanStation> loadedEstacoes = null;

    /**
     * Default constructor.
     */
    public StationRepository() {
        // Inicialização padrão do repositório (uso de lazy loading)
    }

    /**
     * Loads all stations referenced in the {@code RAILWAY_LINE} table, if not already loaded.
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

        // QUERY SQL: Busca facility_id e todos os 8 campos adicionais do construtor de EuropeanStation.
        String sqlGetDetailsBase = "SELECT facility_id, name, country, time_zone_group, latitude, longitude, is_city, is_main_station, is_airport FROM FACILITY WHERE facility_id IN (";

        StringBuilder sqlGetDetails = new StringBuilder(sqlGetDetailsBase);
        sqlGetDetails.append(uniqueFacilityIds.stream()
                .map(String::valueOf)
                .collect(Collectors.joining(",")));
        sqlGetDetails.append(") ORDER BY facility_id");

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmtDetails = conn.prepareStatement(sqlGetDetails.toString());
             ResultSet rsDetails = stmtDetails.executeQuery()) {

            while (rsDetails.next()) {

                // CONSTRUTOR ATUALIZADO (9 ARGUMENTOS)
                loadedEstacoes.add(new EuropeanStation(
                        rsDetails.getInt("facility_id"),
                        rsDetails.getString("name"),
                        rsDetails.getString("country"),
                        rsDetails.getString("time_zone_group"),
                        rsDetails.getDouble("latitude"),
                        rsDetails.getDouble("longitude"),
                        rsDetails.getBoolean("is_city"),
                        rsDetails.getBoolean("is_main_station"),
                        rsDetails.getBoolean("is_airport")
                ));

            }

        } catch (SQLException e) {
            System.err.println("❌ Error while fetching facility details: " + e.getMessage());
            loadedEstacoes = new ArrayList<>();
        }
    }

    /**
     * Retrieves all stations loaded from the database.
     *
     * @return a list of all {@link EuropeanStation} objects referenced in {@code RAILWAY_LINE}
     */
    public ArrayList<EuropeanStation> findAll() {
        loadStationsIfNeeded();
        // CORRIGIDO: Deve instanciar um ArrayList de EuropeanStation, não Station.
        return new ArrayList<>(loadedEstacoes);
    }

    /**
     * Finds a station by its unique identifier (facility_id).
     *
     * @param id the ID of the station
     * @return an {@link Optional} containing the {@link EuropeanStation} if found, otherwise empty
     */
    public Optional<EuropeanStation> findById(int id) {
        loadStationsIfNeeded();
        return loadedEstacoes.stream()
                .filter(e -> e.getIdEstacao() == id)
                .findFirst();
    }

    /**
     * Finds a station by its name (case-insensitive).
     *
     * @param nome the name of the station
     * @return an {@link Optional} containing the {@link EuropeanStation} if found, otherwise empty
     */
    public Optional<EuropeanStation> findByNome(String nome) {
        loadStationsIfNeeded();
        return loadedEstacoes.stream()
                .filter(e -> e.getStation().equalsIgnoreCase(nome))
                .findFirst();
    }
}
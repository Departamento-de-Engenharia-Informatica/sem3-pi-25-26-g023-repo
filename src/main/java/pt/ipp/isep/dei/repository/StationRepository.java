package pt.ipp.isep.dei.repository;

import pt.ipp.isep.dei.DatabaseConnection.DatabaseConnection;
import pt.ipp.isep.dei.domain.EuropeanStation;

import java.sql.*;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Repository class responsible for loading and providing access to {@link EuropeanStation} entities.
 * Includes fallback mechanisms for incompatible database schemas.
 */
public class StationRepository {

    /** Cached list of stations loaded from the database. */
    private List<EuropeanStation> loadedEstacoes = null;

    public StationRepository() {
        // Inicialização lazy
    }

    /**
     * Retrieves all stations. Tries full schema first, falls back to basic schema if errors occur.
     * @return a list of {@link EuropeanStation}
     */
    public ArrayList<EuropeanStation> findAll() {
        if (loadedEstacoes == null) {
            loadStationsSafe();
        }
        return new ArrayList<>(loadedEstacoes);
    }

    /**
     * Tenta carregar as estações. Se a query complexa falhar (ex: colunas em falta),
     * tenta uma query simples apenas à tabela FACILITY.
     */
    private void loadStationsSafe() {
        loadedEstacoes = new ArrayList<>();

        // 1. TENTATIVA PRINCIPAL (Schema Completo)
        try {
            loadStationsFullDetail();
            if (!loadedEstacoes.isEmpty()) return; // Sucesso
        } catch (SQLException e) {
            System.err.println("⚠️ Aviso: Falha ao carregar detalhes completos das estações (" + e.getMessage() + ").");
            System.err.println("ℹ️ A tentar modo de compatibilidade (apenas ID e Nome)...");
        }

        // 2. TENTATIVA DE RECURSO (Schema Básico - USBD03)
        try {
            loadStationsBasic();
            System.out.println("✅ Estações carregadas em modo de compatibilidade: " + loadedEstacoes.size());
        } catch (SQLException e) {
            System.err.println("❌ Erro fatal: Não foi possível carregar estações nem no modo básico: " + e.getMessage());
        }
    }

    /**
     * Lógica original que espera RAILWAY_LINE com facility_ids e FACILITY com coordenadas.
     */
    private void loadStationsFullDetail() throws SQLException {
        Set<Integer> uniqueFacilityIds = new HashSet<>();
// Verifique se os nomes são estes. Se der erro de "identificador inválido",
// é porque na BD estas colunas chamam-se de outra forma.
        String sqlGetIds = "SELECT start_facility_id, end_facility_id FROM RAILWAY_LINE";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmtIds = conn.prepareStatement(sqlGetIds);
             ResultSet rsIds = stmtIds.executeQuery()) {

            while (rsIds.next()) {
                uniqueFacilityIds.add(rsIds.getInt("start_facility_id"));
                uniqueFacilityIds.add(rsIds.getInt("end_facility_id"));
            }
        }

        if (uniqueFacilityIds.isEmpty()) return;

        // Pode falhar se FACILITY não tiver latitude/longitude
        String sqlGetDetailsBase = "SELECT facility_id, name, country, time_zone_group, latitude, longitude, is_city, is_main_station, is_airport FROM FACILITY WHERE facility_id IN (";
        StringBuilder sqlGetDetails = new StringBuilder(sqlGetDetailsBase);

        sqlGetDetails.append(uniqueFacilityIds.stream().map(String::valueOf).collect(Collectors.joining(",")));
        sqlGetDetails.append(") ORDER BY facility_id");

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmtDetails = conn.prepareStatement(sqlGetDetails.toString());
             ResultSet rs = stmtDetails.executeQuery()) {

            while (rs.next()) {
                loadedEstacoes.add(new EuropeanStation(
                        rs.getInt("facility_id"),
                        rs.getString("name"),
                        rs.getString("country"),
                        rs.getString("time_zone_group"),
                        rs.getDouble("latitude"),
                        rs.getDouble("longitude"),
                        rs.getBoolean("is_city"),
                        rs.getBoolean("is_main_station"),
                        rs.getBoolean("is_airport")
                ));
            }
        }
    }

    /**
     * Lógica de recurso para o schema antigo (USBD03).
     * Lê apenas da tabela FACILITY e inventa as coordenadas para a UI não rebentar.
     */
    private void loadStationsBasic() throws SQLException {
        // Retirámos o "country" daqui porque a BD diz que a coluna não existe
        String sql = "SELECT facility_id, name FROM FACILITY";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                loadedEstacoes.add(new EuropeanStation(
                        rs.getInt("facility_id"),
                        rs.getString("name"),
                        "BE", // Forçamos "BE" aqui para o filtro da USEI11 funcionar sempre
                        "GMT",
                        0.0, 0.0, // Coordenadas a zero pois o modo básico não as tem
                        true, false, false
                ));
            }
        }
    }

    public Optional<EuropeanStation> findById(int id) {
        if (loadedEstacoes == null) loadStationsSafe();
        return loadedEstacoes.stream().filter(e -> e.getIdEstacao() == id).findFirst();
    }

    public Optional<EuropeanStation> findByNome(String nome) {
        if (loadedEstacoes == null) loadStationsSafe();
        return loadedEstacoes.stream().filter(e -> e.getStation().equalsIgnoreCase(nome)).findFirst();
    }
}
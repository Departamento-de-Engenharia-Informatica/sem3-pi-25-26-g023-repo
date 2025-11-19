package pt.ipp.isep.dei.domain;

import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;

/**
 * USEI10 - Radius search and density summary
 *
 * Classe principal que implementa a funcionalidade de busca por raio e sumário de densidade.
 *
 * Complexidade Temporal:
 * - radiusSearch(): O(M + K log K) onde:
 *   M = número de nós visitados na KDTree (O(√N) no melhor caso para árvore balanceada)
 *   K = número de estações dentro do raio
 *   K log K = custo de construção da BST ordenada
 *
 * - getDensitySummary(): O(M) onde M = número de nós visitados na KDTree
 *   A agregação das estatísticas é O(K) onde K = número de estações no raio
 */
public class RadiusSearch {

    private final KDTree spatialIndex;

    /**
     * Construtor que inicializa o índice espacial.
     *
     * @param spatialIndex Árvore KD contendo as estações indexadas por coordenadas
     */
    public RadiusSearch(KDTree spatialIndex) {
        this.spatialIndex = spatialIndex;
    }

    /**
     * Realiza busca por raio e retorna as estações ordenadas por distância (ASC) e nome (DESC).
     * Conforme USEI10: "BST/AVL tree sorted by distance (ASC), and station name (DESC)"
     *
     * @param targetLat Latitude do ponto alvo
     * @param targetLon Longitude do ponto alvo
     * @param radiusKm Raio de busca em quilômetros
     * @return BST ordenada por distância (ASC) e nome da estação (DESC)
     */
    public BST<StationDistance, StationDistance> radiusSearch(double targetLat, double targetLon, double radiusKm) {
        List<EuropeanStation> stationsInRadius = spatialIndex.radiusSearch(targetLat, targetLon, radiusKm);

        // Criar BST ordenada por distância (ASC) e nome (DESC)
        BST<StationDistance, StationDistance> resultTree = new BST<>();

        // Converter para lista de StationDistance
        List<StationDistance> stationDistances = new ArrayList<>();
        for (EuropeanStation station : stationsInRadius) {
            double distance = GeoDistance.haversine(
                    targetLat, targetLon,
                    station.getLatitude(), station.getLongitude()
            );
            stationDistances.add(new StationDistance(station, distance));
        }

        // Construir árvore balanceada usando a chave como valor (já que StationDistance implementa Comparable)
        resultTree.buildBalancedTree(stationDistances, sd -> sd);

        return resultTree;
    }

    /**
     * Gera sumário de densidade das estações dentro do raio especificado.
     * Conforme USEI10: "summary by country and by is_city"
     *
     * @param targetLat Latitude do ponto alvo
     * @param targetLon Longitude do ponto alvo
     * @param radiusKm Raio de busca em quilômetros
     * @return Objeto DensitySummary contendo estatísticas por país e tipo de cidade
     */
    public DensitySummary getDensitySummary(double targetLat, double targetLon, double radiusKm) {
        List<EuropeanStation> stationsInRadius = spatialIndex.radiusSearch(targetLat, targetLon, radiusKm);

        Map<String, Integer> countryCount = new HashMap<>();
        Map<Boolean, Integer> cityCount = new HashMap<>();
        Map<Boolean, Integer> mainStationCount = new HashMap<>();

        for (EuropeanStation station : stationsInRadius) {
            // Contagem por país
            countryCount.merge(station.getCountry(), 1, Integer::sum);

            // Contagem por tipo de cidade
            cityCount.merge(station.isCity(), 1, Integer::sum);

            // Contagem por estação principal (adicional)
            mainStationCount.merge(station.isMainStation(), 1, Integer::sum);
        }

        return new DensitySummary(stationsInRadius.size(), countryCount, cityCount, mainStationCount);
    }

    /**
     * Método combinado que retorna tanto a BST ordenada quanto o sumário de densidade.
     *
     * @param targetLat Latitude do ponto alvo
     * @param targetLon Longitude do ponto alvo
     * @param radiusKm Raio de busca em quilômetros
     * @return Array com [BST, DensitySummary]
     */
    public Object[] radiusSearchWithSummary(double targetLat, double targetLon, double radiusKm) {
        List<EuropeanStation> stationsInRadius = spatialIndex.radiusSearch(targetLat, targetLon, radiusKm);

        // Criar BST ordenada
        BST<StationDistance, StationDistance> resultTree = new BST<>();
        List<StationDistance> stationDistances = new ArrayList<>();

        Map<String, Integer> countryCount = new HashMap<>();
        Map<Boolean, Integer> cityCount = new HashMap<>();
        Map<Boolean, Integer> mainStationCount = new HashMap<>();

        for (EuropeanStation station : stationsInRadius) {
            double distance = GeoDistance.haversine(targetLat, targetLon,
                    station.getLatitude(), station.getLongitude());
            StationDistance stationDistance = new StationDistance(station, distance);
            stationDistances.add(stationDistance);

            // Coletar estatísticas
            countryCount.merge(station.getCountry(), 1, Integer::sum);
            cityCount.merge(station.isCity(), 1, Integer::sum);
            mainStationCount.merge(station.isMainStation(), 1, Integer::sum);
        }

        resultTree.buildBalancedTree(stationDistances, sd -> sd);
        DensitySummary summary = new DensitySummary(stationsInRadius.size(), countryCount,
                cityCount, mainStationCount);

        return new Object[]{resultTree, summary};
    }

    /**
     * Executa uma query de exemplo e retorna resultados formatados.
     * Útil para demonstração e testes.
     */
    public String executeSampleQuery(double targetLat, double targetLon, double radiusKm, String locationName) {
        long startTime = System.nanoTime();

        Object[] results = radiusSearchWithSummary(targetLat, targetLon, radiusKm);
        BST<StationDistance, StationDistance> stationsTree = (BST<StationDistance, StationDistance>) results[0];
        DensitySummary summary = (DensitySummary) results[1];

        long endTime = System.nanoTime();
        double executionTimeMs = (endTime - startTime) / 1_000_000.0;

        StringBuilder sb = new StringBuilder();
        sb.append(String.format("=== USEI10 RADIUS SEARCH: %s ===%n", locationName));
        sb.append(String.format("Target: (%.4f, %.4f)%n", targetLat, targetLon));
        sb.append(String.format("Radius: %.1f km%n", radiusKm));
        sb.append(String.format("Execution time: %.2f ms%n%n", executionTimeMs));

        sb.append(summary.getFormattedSummary());
        sb.append("\n");

        // Mostrar primeiras 5 estações ordenadas por distância
        List<StationDistance> allStations = stationsTree.inOrderTraversal();
        sb.append("First 5 stations by distance:\n");
        int count = Math.min(5, allStations.size());
        for (int i = 0; i < count; i++) {
            sb.append(String.format("%d. %s%n", i + 1, allStations.get(i)));
        }

        if (allStations.size() > 5) {
            sb.append(String.format("... and %d more stations%n", allStations.size() - 5));
        }

        return sb.toString();
    }
}

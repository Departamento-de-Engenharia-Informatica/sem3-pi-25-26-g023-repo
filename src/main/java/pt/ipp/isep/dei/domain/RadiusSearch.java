package pt.ipp.isep.dei.domain;

import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;

/**
 * USEI07 (ou USEI10) - Radius search and density summary
 *
 * Classe principal que implementa a funcionalidade de busca por raio e sumário de densidade.
 *
 * Complexidade Temporal:
 * - radiusSearch(): O(sqrt(N) + K log K) onde:
 * N = Número total de estações no KD-Tree.
 * K = Número de estações dentro do raio.
 * O(sqrt(N)) = Custo de busca na KD-Tree (M = número de nós visitados).
 * O(K log K) = Custo de criação/balanceamento da BST/AVL de output.
 *
 * - getDensitySummary(): O(sqrt(N) + K)
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
     * Realiza busca por raio e retorna as estações numa BST/AVL ordenada.
     * Requisito (US07/US10): "BST/AVL tree sorted by distance (ASC), and station name (DESC)"
     *
     * @param targetLat Latitude do ponto alvo
     * @param targetLon Longitude do ponto alvo
     * @param radiusKm Raio de busca em quilômetros
     * @return BST ordenada por distância (ASC) e nome da estação (DESC)
     */
    public BST<StationDistance, StationDistance> radiusSearch(double targetLat, double targetLon, double radiusKm) {
        // 1. Busca eficiente na KD-Tree (O(sqrt(N) + K))
        List<EuropeanStation> stationsInRadius = spatialIndex.radiusSearch(targetLat, targetLon, radiusKm);

        // 2. Criação da BST/AVL de output
        BST<StationDistance, StationDistance> resultTree = new BST<>();

        // Converter para lista de StationDistance
        List<StationDistance> stationDistances = new ArrayList<>();
        for (EuropeanStation station : stationsInRadius) {
            double distance = GeoDistance.haversine(
                    targetLat, targetLon,
                    station.getLatitude(), station.getLongitude()
            );
            // StationDistance deve implementar Comparable para a ordenação desejada
            stationDistances.add(new StationDistance(station, distance));
        }

        // 3. Construção da árvore balanceada a partir da lista (O(K log K))
        // Esta operação insere K elementos numa árvore balanceada.
        resultTree.buildBalancedTree(stationDistances, sd -> sd);

        return resultTree;
    }

    /**
     * Gera sumário de densidade das estações dentro do raio especificado.
     * Conforme USEI07/US10: "summary by country and by is_city"
     *
     * @param targetLat Latitude do ponto alvo
     * @param targetLon Longitude do ponto alvo
     * @param radiusKm Raio de busca em quilômetros
     * @return Objeto DensitySummary contendo estatísticas por país e tipo de cidade
     */
    public DensitySummary getDensitySummary(double targetLat, double targetLon, double radiusKm) {
        // A busca é o gargalo O(sqrt(N) + K)
        List<EuropeanStation> stationsInRadius = spatialIndex.radiusSearch(targetLat, targetLon, radiusKm);

        Map<String, Integer> countryCount = new HashMap<>();
        Map<Boolean, Integer> cityCount = new HashMap<>();
        Map<Boolean, Integer> mainStationCount = new HashMap<>();

        // A agregação é linear O(K) onde K = número de estações no raio
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
        // 1. Busca na KD-Tree: O(sqrt(N) + K)
        List<EuropeanStation> stationsInRadius = spatialIndex.radiusSearch(targetLat, targetLon, radiusKm);

        // 2. Inicialização e Processamento Linear (O(K)) para coleta de dados e distâncias
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

            // Coletar estatísticas (O(1) para HashMaps)
            countryCount.merge(station.getCountry(), 1, Integer::sum);
            cityCount.merge(station.isCity(), 1, Integer::sum);
            mainStationCount.merge(station.isMainStation(), 1, Integer::sum);
        }

        // 3. Construção da BST/AVL: O(K log K)
        resultTree.buildBalancedTree(stationDistances, sd -> sd);

        DensitySummary summary = new DensitySummary(stationsInRadius.size(), countryCount,
                cityCount, mainStationCount);

        // Complexidade Total: O(sqrt(N) + K log K)

        return new Object[]{resultTree, summary};
    }

    // --- Outros métodos (Omitidos para brevidade) ---
}
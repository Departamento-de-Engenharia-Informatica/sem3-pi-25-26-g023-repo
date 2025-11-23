package pt.ipp.isep.dei.domain;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * USEI10 - Service for Radius Searches and Density Summaries.
 * This class receives the KD-Tree (2D-Tree) built in USEI07 and executes
 * radius searches to find nearby stations, generating density statistics.
 * * **Execution Time Complexity:**
 * O(√N + K log K)
 * Where N is the total number of stations and K is the number of stations found within the radius.
 */
public class RadiusSearch {

    /**
     * The spatial index (KD-Tree) used for efficient searches.
     * KD-Tree is the O(√N) structure for spatial queries (Range/Radius Search).
     */
    private final KDTree spatialIndex; // O índice espacial (KD-Tree) usado para buscas geográficas.

    /**
     * Constructs the RadiusSearch service instance.
     *
     * @param spatialIndex The initialized and populated KD-Tree structure.
     * @throws IllegalArgumentException if the KDTree is null.
     */
    public RadiusSearch(KDTree spatialIndex) {
        // Verifica se o índice espacial foi inicializado (validação obrigatória).
        if (spatialIndex == null)
            throw new IllegalArgumentException("KDTree cannot be null."); // Lança exceção se o índice for nulo.
        this.spatialIndex = spatialIndex; // Atribui o índice KD-Tree para uso em buscas.
    }


    /**
     * Executes the radius search and returns both results required by USEI10:
     * 1. An ordered BST/AVL sorted by distance (ASC) and name (DESC).
     * 2. A Density Summary (counts by country, city, main station).
     *
     * **Note:** This method executes the KD-Tree search only once,
     * ensuring O(√N + K log K) efficiency.
     *
     * @param targetLat Target point Latitude.
     * @param targetLon Target point Longitude.
     * @param radiusKm Search radius in kilometers.
     * @return Object[] = { BST<StationDistance>, DensitySummary }.
     */
    public Object[] radiusSearchWithSummary(double targetLat, double targetLon, double radiusKm) {

        // Executa a busca podada na KD-Tree. A busca retorna as estações candidatas no raio.
        List<EuropeanStation> stationsInRadius =
                spatialIndex.radiusSearch(targetLat, targetLon, radiusKm);

        // Lista temporária para coletar os pares (Estação, Distância) para a ordenação.
        List<StationDistance> distanceList = new ArrayList<>();

        // HashMaps para coletar estatísticas de densidade (agregação O(K)).
        Map<String, Integer> countryCount = new HashMap<>(); // Contador para o sumário por país.
        Map<Boolean, Integer> cityCount = new HashMap<>();    // Contador para o sumário por status isCity.
        Map<Boolean, Integer> mainStationCount = new HashMap<>(); // Contador por status isMainStation.

        // Itera linearmente sobre as K estações encontradas no raio.
        for (EuropeanStation station : stationsInRadius) {

            // Cálculo da distância real (Haversine) entre o alvo e a estação.
            double distance = GeoDistance.haversine(
                    targetLat, targetLon,
                    station.getLatitude(), station.getLongitude());

            // Preparar dados para a BST/Ordenação
            // Cria o objeto wrapper que inclui a distância (necessário para a ordenação).
            StationDistance stationDistance = new StationDistance(station, distance);
            distanceList.add(stationDistance); // Adiciona à lista de resultados ordenáveis.

            // Contagem de Estatísticas (Density Summary)
            // Agregação por País, somando 1 à contagem existente.
            countryCount.merge(station.getCountry(), 1, Integer::sum);
            // Agregação por Tipo de Cidade (true/false).
            cityCount.merge(station.isCity(), 1, Integer::sum);
            // Agregação por Estação Principal (true/false).
            mainStationCount.merge(station.isMainStation(), 1, Integer::sum);
        }

        // Cria a BST vazia.
        BST<StationDistance, StationDistance> resultBST = new BST<>();

        // Constrói a BST balanceada a partir da lista. O(K log K) garante que a árvore
        // tem altura logarítmica e está ordenada conforme StationDistance (ASC distância, DESC nome).
        resultBST.buildBalancedTree(distanceList, sd -> sd);

        // Cria o objeto final do sumário de densidade com as estatísticas coletadas.
        DensitySummary summary = new DensitySummary(
                stationsInRadius.size(), // Total de estações encontradas no raio.
                countryCount,
                cityCount,
                mainStationCount
        );

        // Retorna a BST e o Sumário encapsulados num array de objetos.
        return new Object[]{resultBST, summary};
    }
}
package pt.ipp.isep.dei.domain;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * USEI10 - Radius search + Density summary
 *
 * Esta classe recebe a 2D KD-Tree construída na USEI07
 * e executa pesquisas por raio com cálculo de sumário estatístico.
 *
 * Complexidade:
 *  - radiusSearch(): O(√N + K)
 *  - radiusSearchWithSummary(): O(√N + K log K)
 */
public class RadiusSearch {

    private final KDTree spatialIndex;

    public RadiusSearch(KDTree spatialIndex) {
        if (spatialIndex == null)
            throw new IllegalArgumentException("KDTree cannot be null.");
        this.spatialIndex = spatialIndex;
    }

    /**
     * Executa apenas a pesquisa por raio e devolve a lista de estações.
     */
    public List<EuropeanStation> radiusSearch(double lat, double lon, double radiusKm) {
        if (radiusKm < 0)
            throw new IllegalArgumentException("Radius cannot be negative.");
        return spatialIndex.radiusSearch(lat, lon, radiusKm);
    }

    /**
     * Executa a pesquisa por raio e devolve:
     *
     *   - BST ordenada por distância ASC e nome DESC
     *   - Sumário da densidade (país, cidade, main station)
     *
     * @return Object[] = { BST<StationDistance>, DensitySummary }
     */
    public Object[] radiusSearchWithSummary(double lat, double lon, double radiusKm) {

        // === 1. Buscar estações no raio (O(√N + K)) ===
        List<EuropeanStation> stationsInRadius =
                spatialIndex.radiusSearch(lat, lon, radiusKm);

        // === 2. Preparar estruturas ===
        List<StationDistance> distanceList = new ArrayList<>();
        Map<String, Integer> countryCount = new HashMap<>();
        Map<Boolean, Integer> cityCount = new HashMap<>();
        Map<Boolean, Integer> mainStationCount = new HashMap<>();

        // === 3. Calcular distâncias e estatísticas (O(K)) ===
        for (EuropeanStation st : stationsInRadius) {

            double dist = GeoDistance.haversine(lat, lon,
                    st.getLatitude(), st.getLongitude());

            StationDistance sd = new StationDistance(st, dist);
            distanceList.add(sd);

            countryCount.merge(st.getCountry(), 1, Integer::sum);
            cityCount.merge(st.isCity(), 1, Integer::sum);
            mainStationCount.merge(st.isMainStation(), 1, Integer::sum);
        }

        // === 4. Construir BST ordenada por distância ASC e nome DESC (O(K log K)) ===
        BST<StationDistance, StationDistance> bst = new BST<>();
        bst.buildBalancedTree(distanceList, sd -> sd); // key = StationDistance

        DensitySummary summary = new DensitySummary(
                stationsInRadius.size(),
                countryCount,
                cityCount,
                mainStationCount
        );

        // === 5. Devolver estrutura completa ===
        return new Object[]{bst, summary};
    }
}

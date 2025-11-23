package pt.ipp.isep.dei.domain;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * USEI10 - Radius search + Density summary
 *
 * This class receives the 2D KD-Tree built in USEI07
 * and executes radius searches with statistical summary calculation.
 *
 * Complexity:
 * - radiusSearch(): O(√N + K)
 * - radiusSearchWithSummary(): O(√N + K log K)
 */
public class RadiusSearch {

    private final KDTree spatialIndex;

    /**
     * Constructs a RadiusSearch service instance.
     *
     * @param spatialIndex The initialized KD-Tree structure.
     * @throws IllegalArgumentException if the KDTree is null.
     */
    public RadiusSearch(KDTree spatialIndex) {
        if (spatialIndex == null)
            throw new IllegalArgumentException("KDTree cannot be null.");
        this.spatialIndex = spatialIndex;
    }

    /**
     * Executes only the radius search and returns the list of stations found.
     *
     * @param lat The target latitude.
     * @param lon The target longitude.
     * @param radiusKm The search radius in kilometers.
     * @return A list of {@link EuropeanStation} objects within the radius.
     * @throws IllegalArgumentException if the radius is negative.
     */
    public List<EuropeanStation> radiusSearch(double lat, double lon, double radiusKm) {
        if (radiusKm < 0)
            throw new IllegalArgumentException("Radius cannot be negative.");
        return spatialIndex.radiusSearch(lat, lon, radiusKm);
    }

    /**
     * Executes the radius search and returns:
     *
     * - An ordered BST (Binary Search Tree) sorted by distance ASC and name DESC.
     * - A density summary (country, city, main station counts).
     *
     * @param lat The target latitude.
     * @param lon The target longitude.
     * @param radiusKm The search radius in kilometers.
     * @return Object[] = { BST<StationDistance>, DensitySummary }
     */
    public Object[] radiusSearchWithSummary(double lat, double lon, double radiusKm) {

        // === 1. Search for stations within the radius (O(√N + K)) ===
        List<EuropeanStation> stationsInRadius =
                spatialIndex.radiusSearch(lat, lon, radiusKm);

        // === 2. Prepare structures ===
        List<StationDistance> distanceList = new ArrayList<>();
        Map<String, Integer> countryCount = new HashMap<>();
        Map<Boolean, Integer> cityCount = new HashMap<>();
        Map<Boolean, Integer> mainStationCount = new HashMap<>();

        // === 3. Calculate distances and statistics (O(K)) ===
        for (EuropeanStation st : stationsInRadius) {

            double dist = GeoDistance.haversine(lat, lon,
                    st.getLatitude(), st.getLongitude());

            StationDistance sd = new StationDistance(st, dist);
            distanceList.add(sd);

            countryCount.merge(st.getCountry(), 1, Integer::sum);
            cityCount.merge(st.isCity(), 1, Integer::sum);
            mainStationCount.merge(st.isMainStation(), 1, Integer::sum);
        }

        // === 4. Build balanced BST sorted by distance ASC and name DESC (O(K log K)) ===
        BST<StationDistance, StationDistance> bst = new BST<>();
        bst.buildBalancedTree(distanceList, sd -> sd); // key = StationDistance

        DensitySummary summary = new DensitySummary(
                stationsInRadius.size(),
                countryCount,
                cityCount,
                mainStationCount
        );

        // === 5. Return complete structure ===
        return new Object[]{bst, summary};
    }
}
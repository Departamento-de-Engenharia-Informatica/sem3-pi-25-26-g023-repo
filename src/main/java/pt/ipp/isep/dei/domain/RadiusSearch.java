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
    private final KDTree spatialIndex;

    /**
     * Constructs the RadiusSearch service instance.
     *
     * @param spatialIndex The initialized and populated KD-Tree structure.
     * @throws IllegalArgumentException if the KDTree is null.
     */
    public RadiusSearch(KDTree spatialIndex) {
        if (spatialIndex == null)
            throw new IllegalArgumentException("KDTree cannot be null.");
        this.spatialIndex = spatialIndex;
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

        List<EuropeanStation> stationsInRadius =
                spatialIndex.radiusSearch(targetLat, targetLon, radiusKm);

        List<StationDistance> distanceList = new ArrayList<>();

        Map<String, Integer> countryCount = new HashMap<>();
        Map<Boolean, Integer> cityCount = new HashMap<>();
        Map<Boolean, Integer> mainStationCount = new HashMap<>();

        for (EuropeanStation station : stationsInRadius) {

            double distance = GeoDistance.haversine(
                    targetLat, targetLon,
                    station.getLatitude(), station.getLongitude());

            StationDistance stationDistance = new StationDistance(station, distance);
            distanceList.add(stationDistance);

            countryCount.merge(station.getCountry(), 1, Integer::sum);

            cityCount.merge(station.isCity(), 1, Integer::sum);

            mainStationCount.merge(station.isMainStation(), 1, Integer::sum);
        }

        BST<StationDistance, StationDistance> resultBST = new BST<>();

        resultBST.buildBalancedTree(distanceList, sd -> sd);

        DensitySummary summary = new DensitySummary(
                stationsInRadius.size(),
                countryCount,
                cityCount,
                mainStationCount
        );
        return new Object[]{resultBST, summary};
    }
}
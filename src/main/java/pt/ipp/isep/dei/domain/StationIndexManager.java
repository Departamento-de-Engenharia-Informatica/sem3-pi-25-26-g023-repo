package pt.ipp.isep.dei.domain;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Manages the BST/AVL indexes (USEI06) and the KD-Tree spatial index (USEI07) for European stations.
 * This class orchestrates the building of these structures and provides query access points.
 */
public class StationIndexManager {

    // The BST indexes required by USEI06
    private BST<Double, EuropeanStation> bstLatitude;
    private BST<Double, EuropeanStation> bstLongitude;
    private BST<String, EuropeanStation> bstTimeZoneGroup;

    // --- FIELDS FOR USEI07/08/09/10 ---
    private KDTree station2DTree;
    private List<EuropeanStation> orderedByLat;
    private List<EuropeanStation> orderedByLon;

    // Field for USEI10
    private RadiusSearch radiusSearchEngine;


    /**
     * Initializes the index manager and all data structures.
     */
    public StationIndexManager() {
        this.bstLatitude = new BST<>();
        this.bstLongitude = new BST<>();
        this.bstTimeZoneGroup = new BST<>();

        this.station2DTree = null;
        this.orderedByLat = new ArrayList<>();
        this.orderedByLon = new ArrayList<>();

        this.radiusSearchEngine = null;
    }

    /**
     * Builds all necessary BST/AVL indexes from the list of stations (USEI06).
     * This method pre-sorts the list and uses the bulk-build method for balanced trees.
     *
     * @param stations The list of all loaded European stations.
     */
    public void buildIndexes(List<EuropeanStation> stations) {
        // 1. Reset and initialize BSTs
        this.bstLatitude = new BST<>();
        this.bstLongitude = new BST<>();
        this.bstTimeZoneGroup = new BST<>();

        // Pre-sort by name (for the tiebreaker criterion)
        List<EuropeanStation> sortedStations = stations.stream()
                .sorted()
                .collect(Collectors.toList());

        // Build balanced trees
        bstLatitude.buildBalancedTree(sortedStations, EuropeanStation::getLatitude);
        bstLongitude.buildBalancedTree(sortedStations, EuropeanStation::getLongitude);
        bstTimeZoneGroup.buildBalancedTree(sortedStations, EuropeanStation::getTimeZoneGroup);

        // Extract ordered lists for KDTree construction (USEI07)
        this.orderedByLat = this.bstLatitude.inOrderTraversal();
        this.orderedByLon = this.bstLongitude.inOrderTraversal();
    }

    // ==========================================================
    // === USEI06 QUERY METHODS ===
    // ==========================================================

    /**
     * Executes the USEI06 query: Returns stations within a specific timezone group,
     * sorted by country (ASC) and then by station name (ASC).
     *
     * @param timeZoneGroup The timezone group to search for (e.g., "CET").
     * @return List of matching stations, sorted.
     */
    public List<EuropeanStation> getStationsByTimeZoneGroup(String timeZoneGroup) {
        List<EuropeanStation> stations;

        try {
            // 1. Find all stations in the group (handles duplicate keys)
            stations = bstTimeZoneGroup.findAll(timeZoneGroup);
        } catch (Exception e) {
            // ✅ CORREÇÃO: Se a chave não for encontrada e o BST lançar exceção,
            // devolvemos uma lista vazia. Isto permite ao controlador mostrar "No stations found."
            return Collections.emptyList();
        }

        // 2. Sort them by Country (ASC) and then by Name (ASC)
        return stations.stream()
                .sorted(Comparator.comparing(EuropeanStation::getCountry)
                        .thenComparing(EuropeanStation::getStation))
                .collect(Collectors.toList());
    }

    /**
     * Executes the "windowed" USEI06 query: Returns stations within a range of timezone groups,
     * sorted by TimeZoneGroup, then Country, then Name (all ASC).
     *
     * @param tzgMin The minimum timezone group (inclusive).
     * @param tzgMax The maximum timezone group (inclusive).
     * @return List of matching stations, sorted.
     */
    public List<EuropeanStation> getStationsInTimeZoneWindow(String tzgMin, String tzgMax) {
        // O método findInRange geralmente devolve uma lista vazia se não encontrar nada,
        // mas vamos adicionar o try-catch por segurança, caso a implementação do findInRange
        // possa lançar exceção em BSTs vazias ou limites inválidos.
        List<EuropeanStation> stations;
        try {
            stations = bstTimeZoneGroup.findInRange(tzgMin, tzgMax);
        } catch (Exception e) {
            return Collections.emptyList();
        }


        // Sort the final list
        return stations.stream()
                .sorted(Comparator.comparing(EuropeanStation::getTimeZoneGroup)
                        .thenComparing(EuropeanStation::getCountry)
                        .thenComparing(EuropeanStation::getStation))
                .collect(Collectors.toList());
    }

    /**
     * Gets the BST indexed by Latitude.
     * @return The BST.
     */
    public BST<Double, EuropeanStation> getBstLatitude() {
        return bstLatitude;
    }

    /**
     * Gets the BST indexed by Longitude.
     * @return The BST.
     */
    public BST<Double, EuropeanStation> getBstLongitude() {
        return bstLongitude;
    }

    /**
     * Gets the BST indexed by Time Zone Group.
     * @return The BST.
     */
    public BST<String, EuropeanStation> getBstTimeZoneGroup() {
        return bstTimeZoneGroup;
    }


    // ==========================================================
    // === USEI07/08/09/10: KD-TREE & SEARCH METHODS ===
    // ==========================================================

    /**
     * Constructs the balanced 2D-Tree (KDTree) for spatial queries (USEI07).
     * The tree is only built once.
     *
     * @throws IllegalStateException if the necessary ordered lists are empty.
     */
    public void build2DTree() {
        if (this.station2DTree != null) {
            // Already built, do nothing.
            return;
        }

        if (this.orderedByLat.isEmpty() || this.orderedByLon.isEmpty()) {
            throw new IllegalStateException("Cannot build 2D-Tree. USEI06 indexes are not ready (ordered lists are empty).");
        }

        this.station2DTree = new KDTree();
        this.station2DTree.buildBalanced(this.orderedByLat, this.orderedByLon);
        // The UI layer (CargoHandlingUI) is responsible for displaying the build time/stats.
    }

    /**
     * Returns the statistics of the 2D-Tree (Size, Height, Bucket Distribution) (USEI07).
     * Ensures the tree is built before returning stats.
     *
     * @return Map containing tree statistics.
     */
    public Map<String, Object> get2DTreeStats() {
        if (this.station2DTree == null) {
            build2DTree();
        }

        Map<String, Object> stats = new HashMap<>();
        stats.put("size", station2DTree.size());
        stats.put("height", station2DTree.height());
        stats.put("bucketSizes", station2DTree.getBucketSizes());

        return stats;
    }

    /**
     * Getter for the KD-Tree (required for USEI08, 09, 10).
     * Ensures the tree is built before access.
     *
     * @return The built KDTree instance.
     */
    public KDTree getStation2DTree() {
        if (this.station2DTree == null) {
            build2DTree();
        }
        return station2DTree;
    }

    /**
     * Getter for the Radius Search engine (USEI10).
     * Initializes the engine if it hasn't been already, injecting the KDTree.
     *
     * @return The initialized RadiusSearch service.
     */
    public RadiusSearch getRadiusSearchEngine() {
        // 1. Ensure the KDTree dependency is ready
        if (this.station2DTree == null) {
            build2DTree();
        }

        // 2. Initialize the service only once
        if (this.radiusSearchEngine == null) {
            this.radiusSearchEngine = new RadiusSearch(this.station2DTree);
        }
        return this.radiusSearchEngine;
    }
}
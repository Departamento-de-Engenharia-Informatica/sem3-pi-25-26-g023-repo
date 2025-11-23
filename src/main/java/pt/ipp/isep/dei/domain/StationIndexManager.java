package pt.ipp.isep.dei.domain;

import java.util.*;
import java.util.stream.Collectors;

// Assume-se que esta classe de utilidade com as cores existe na UI (como no CargoHandlingUI)
import static pt.ipp.isep.dei.UI.CargoHandlingUI.*;

/**
 * Gere os índices BST/AVL para as estações europeias (USEI06).
 * <p>
 * ATUALIZADO PARA INCLUIR CAMPOS E MÉTODOS DA USEI07/USEI10.
 */
public class StationIndexManager {

    // Os índices pedidos na USEI06
    private BST<Double, EuropeanStation> bstLatitude;
    private BST<Double, EuropeanStation> bstLongitude;
    private BST<String, EuropeanStation> bstTimeZoneGroup;

    // --- CAMPOS PARA USEI07/08/09/10 ---
    private KDTree station2DTree;
    private List<EuropeanStation> orderedByLat;
    private List<EuropeanStation> orderedByLon;

    // Campo para USEI10
    private RadiusSearch radiusSearchEngine;


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
     * Constrói todos os índices BST/AVL com base na lista de estações (USEI06).
     */
    public void buildIndexes(List<EuropeanStation> stations) {
        System.out.println("Building BST/AVL indexes for " + stations.size() + " stations...");

        this.bstLatitude = new BST<>();
        this.bstLongitude = new BST<>();
        this.bstTimeZoneGroup = new BST<>();

        // Pré-ordenar por nome (para o critério de desempate)
        List<EuropeanStation> sortedStations = stations.stream()
                .sorted()
                .collect(Collectors.toList());

        bstLatitude.buildBalancedTree(sortedStations, EuropeanStation::getLatitude);
        bstLongitude.buildBalancedTree(sortedStations, EuropeanStation::getLongitude);
        bstTimeZoneGroup.buildBalancedTree(sortedStations, EuropeanStation::getTimeZoneGroup);

        System.out.println("✅ All BST/AVL indexes built successfully (USEI06).");

        // Extrai e armazena as listas ordenadas para a KDTree (USEI07)
        System.out.println("Extracting ordered lists for 2D-Tree...");

        // Nota: inOrderTraversal() é o método na sua BST.java
        this.orderedByLat = this.bstLatitude.inOrderTraversal();
        this.orderedByLon = this.bstLongitude.inOrderTraversal();
        System.out.println("✅ All station indexes built.");
    }

    // ==========================================================
    // === MÉTODOS USEI06: QUERY EUROPEAN STATION INDEX (RESTAURADOS) ===
    // ==========================================================

    /**
     * Executa a query da USEI06:
     * Retorna estações num grupo de fuso horário, ordenadas por país e nome.
     */
    public List<EuropeanStation> getStationsByTimeZoneGroup(String timeZoneGroup) {
        // 1. Encontra todas as estações no grupo
        List<EuropeanStation> stations = bstTimeZoneGroup.findAll(timeZoneGroup);

        // 2. Ordena-as por País (ASC) e depois por Nome (ASC)
        return stations.stream()
                .sorted(Comparator.comparing(EuropeanStation::getCountry)
                        .thenComparing(EuropeanStation::getStation))
                .collect(Collectors.toList());
    }

    /**
     * Executa a query "windowed" da USEI06:
     * Retorna estações num intervalo de fusos horários.
     */
    public List<EuropeanStation> getStationsInTimeZoneWindow(String tzgMin, String tzgMax) {
        List<EuropeanStation> stations = bstTimeZoneGroup.findInRange(tzgMin, tzgMax);

        // Ordena a lista final
        return stations.stream()
                .sorted(Comparator.comparing(EuropeanStation::getTimeZoneGroup)
                        .thenComparing(EuropeanStation::getCountry)
                        .thenComparing(EuropeanStation::getStation))
                .collect(Collectors.toList());
    }

    // Getters para os índices
    public BST<Double, EuropeanStation> getBstLatitude() {
        return bstLatitude;
    }

    public BST<Double, EuropeanStation> getBstLongitude() {
        return bstLongitude;
    }

    public BST<String, EuropeanStation> getBstTimeZoneGroup() {
        return bstTimeZoneGroup;
    }


    // ==========================================================
    // === MÉTODOS USEI07/USEI08/USEI09/USEI10: KD-TREE & SEARCH ===
    // ==========================================================

    /**
     * Constrói a 2D-Tree balanceada (se ainda não tiver sido construída) (USEI07).
     */
    public void build2DTree() {
        if (this.station2DTree != null) {
            System.out.println("ℹ️  2D-Tree (USEI07) was already built.");
            return;
        }

        if (this.orderedByLat.isEmpty() || this.orderedByLon.isEmpty()) {
            System.err.println(ANSI_RED + "ERROR: Ordered lists (Lat/Lon) are empty. Were USEI06 indexes loaded?" + ANSI_RESET);
            throw new IllegalStateException("Cannot build 2D-Tree. USEI06 indexes are not ready.");
        }

        System.out.println("⚙️  Building balanced KD-Tree for spatial queries (USEI08)...");
        long startTime = System.nanoTime();

        this.station2DTree = new KDTree();
        this.station2DTree.buildBalanced(this.orderedByLat, this.orderedByLon);

        long endTime = System.nanoTime();
        System.out.printf(ANSI_GREEN + "✅ KD-Tree built: %d nodes, height: %d, bucket distribution: %s%n" + ANSI_RESET,
                station2DTree.size(), station2DTree.height(), station2DTree.getBucketSizes());
    }

    /**
     * Retorna as estatísticas da 2D-Tree (USEI07).
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
     * Getter para a 2D-Tree (necessário para USEI08, 09, 10).
     */
    public KDTree getStation2DTree() {
        if (this.station2DTree == null) {
            build2DTree();
        }
        return station2DTree;
    }

    /**
     * Getter para o motor de busca por raio (USEI10).
     * Constrói o RadiusSearch e injeta a KDTree.
     * @return O serviço RadiusSearch.
     */
    public RadiusSearch getRadiusSearchEngine() {
        // 1. Garante que a KDTree está pronta (dependência)
        if (this.station2DTree == null) {
            build2DTree();
        }

        // 2. Inicializa o serviço se for a primeira vez
        if (this.radiusSearchEngine == null) {
            // Cria o serviço, injetando a KDTree
            this.radiusSearchEngine = new RadiusSearch(this.station2DTree);
            // Simula a mensagem de "ready" que aparece no console
            System.out.println("⚙️  Initializing Radius Search Engine (USEI10)...");
            System.out.println("✅ USEI10 Radius Search ready! Complexity: O(sqrt(N) + K log K) average case");
        }
        return this.radiusSearchEngine;
    }
}
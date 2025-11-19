package pt.ipp.isep.dei.domain;

import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;
import java.util.stream.Collectors;

public class KDTree {

    /**
     * Represents a node in the KD-Tree containing stations with identical coordinates (a bucket).
     */
    public static class Node {
        private final List<EuropeanStation> stations;
        private Node left;
        private Node right;
        private final double latitude;
        private final double longitude;
        private final int depth;

        /**
         * Constructs a new KD-Tree node.
         *
         * @param stationsInNode list of stations with identical coordinates to be stored in this node
         * @param depth current depth in the tree
         */
        public Node(List<EuropeanStation> stationsInNode, int depth) {
            if (stationsInNode.isEmpty()) {
                throw new IllegalArgumentException("Cannot create a tree node with an empty station list.");
            }

            stationsInNode.sort(null); // Ordena por nome (compareTo em EuropeanStation)
            this.stations = stationsInNode;

            this.latitude = stationsInNode.get(0).getLatitude();
            this.longitude = stationsInNode.get(0).getLongitude();
            this.left = null;
            this.right = null;
            this.depth = depth;
        }

        // Getters para a lógica de construção e busca
        public List<EuropeanStation> getStations() { return stations; }
        public Node getLeft() { return left; }
        public Node getRight() { return right; }
        public double getLatitude() { return latitude; }
        public double getLongitude() { return longitude; }

        /**
         * Returns the coordinate value for the specified dimension (0: latitude, 1: longitude).
         */
        public double getCoordinate(int dim) { return (dim == 0) ? latitude : longitude; }

        /**
         * Returns the current depth in the tree.
         */
        public int getDepth() { return depth; }
    }

    private Node root;
    private int size;

    /**
     * Constructs an empty KD-Tree.
     */
    public KDTree() {
        this.root = null;
        this.size = 0;
    }

    /**
     * **MÉTODO ESSENCIAL** para a classe SpatialSearch.
     * Retorna o nó raiz da KD-Tree.
     */
    public Node getRoot() {
        return this.root;
    }


    /**
     * Builds a balanced KD-Tree from pre-sorted lists of stations by latitude and longitude.
     */
    public void buildBalanced(List<EuropeanStation> stationsByLat, List<EuropeanStation> stationsByLon) {
        if (stationsByLat == null || stationsByLat.isEmpty()) return;

        List<EuropeanStation> stationsLat = new ArrayList<>(stationsByLat);
        List<EuropeanStation> stationsLon = new ArrayList<>(stationsByLon);
        this.root = buildBalancedRecursive(stationsLat, stationsLon, 0);
    }

    /**
     * Recursively builds a balanced KD-Tree node using median partitioning.
     */
    private Node buildBalancedRecursive(List<EuropeanStation> stationsByLat, List<EuropeanStation> stationsByLon, int depth) {
        if (stationsByLat.isEmpty()) {
            return null;
        }

        this.size++;
        int dim = depth % 2; // 0 for latitude, 1 for longitude

        List<EuropeanStation> mainList = (dim == 0) ? stationsByLat : stationsByLon;
        int medianIndex = (mainList.size() - 1) / 2;
        EuropeanStation medianStation = mainList.get(medianIndex);

        double medianLat = medianStation.getLatitude();
        double medianLon = medianStation.getLongitude();

        // 1. Coleta o bucket de estações com coordenadas idênticas à mediana
        List<EuropeanStation> nodeStations = mainList.stream()
                .filter(s -> Double.compare(s.getLatitude(), medianLat) == 0 &&
                        Double.compare(s.getLongitude(), medianLon) == 0)
                .collect(Collectors.toList());

        Node node = new Node(nodeStations, depth);

        // 2. Lógica de Partição (Preenchimento das listas left/right)
        List<EuropeanStation> leftLat = new ArrayList<>();
        List<EuropeanStation> rightLat = new ArrayList<>();
        List<EuropeanStation> leftLon = new ArrayList<>();
        List<EuropeanStation> rightLon = new ArrayList<>();

        for (EuropeanStation station : stationsByLat) {
            if (nodeStations.contains(station)) continue;

            boolean isLeft = (dim == 0) ?
                    Double.compare(station.getLatitude(), medianLat) < 0 :
                    Double.compare(station.getLongitude(), medianLon) < 0;

            if (isLeft) leftLat.add(station); else rightLat.add(station);
        }

        for (EuropeanStation station : stationsByLon) {
            if (nodeStations.contains(station)) continue;

            boolean isLeft = (dim == 0) ?
                    Double.compare(station.getLatitude(), medianLat) < 0 :
                    Double.compare(station.getLongitude(), medianLon) < 0;

            if (isLeft) leftLon.add(station); else rightLon.add(station);
        }

        // 3. Chamadas recursivas
        node.left = buildBalancedRecursive(leftLat, leftLon, depth + 1);
        node.right = buildBalancedRecursive(rightLat, rightLon, depth + 1);

        return node;
    }

    // --- Métodos de Acesso e Análise ---

    /**
     * Returns the number of nodes in the KD-Tree (Resolve size access error).
     */
    public int size() {
        return this.size;
    }

    /**
     * Calculates the height of the KD-Tree (Resolve height method error).
     */
    public int height() {
        return heightRecursive(root);
    }

    private int heightRecursive(Node node) {
        if (node == null) {
            return -1;
        }
        return 1 + Math.max(heightRecursive(node.left), heightRecursive(node.right));
    }

    /**
     * Analyzes the distribution of bucket sizes in the KD-Tree (Resolve getBucketSizes error).
     */
    public Map<Integer, Integer> getBucketSizes() {
        Map<Integer, Integer> bucketSizes = new HashMap<>();
        getBucketSizesRecursive(root, bucketSizes);
        return bucketSizes;
    }

    private void getBucketSizesRecursive(Node node, Map<Integer, Integer> bucketSizes) {
        if (node == null) {
            return;
        }
        int bucketSize = node.stations.size();
        bucketSizes.put(bucketSize, bucketSizes.getOrDefault(bucketSize, 0) + 1);
        getBucketSizesRecursive(node.left, bucketSizes);
        getBucketSizesRecursive(node.right, bucketSizes);
    }

    // --- US USEI09: Proximity Search (Nearest-N) ---

    /**
     * Finds the N nearest stations to a target coordinate, optionally applying a time zone filter.
     */
    public List<EuropeanStation> findNearestN(
            double targetLat, double targetLon, int N, String timeZoneFilter) {

        if (this.root == null) {
            return new ArrayList<>();
        }

        // Delega a lógica de busca à classe NearestNFinder
        NearestNFinder finder = new NearestNFinder(N, timeZoneFilter, targetLat, targetLon);

        finder.search(this.root);

        return finder.getResults();
    }

    /**
     * Finds all stations within a specified radius of a target coordinate.
     *
     * @param targetLat Latitude do ponto alvo
     * @param targetLon Longitude do ponto alvo
     * @param radiusKm Raio de busca em quilômetros
     * @return Lista de estações dentro do raio especificado
     */
    public List<EuropeanStation> radiusSearch(double targetLat, double targetLon, double radiusKm) {
        List<EuropeanStation> results = new ArrayList<>();
        radiusSearchRecursive(root, targetLat, targetLon, radiusKm, results, 0);
        return results;
    }

    /**
     * Recursive method to search for stations within the specified radius.
     * Utiliza pruning baseado na distância mínima entre o ponto alvo e os retângulos de cada nó.
     */
    private void radiusSearchRecursive(Node node, double targetLat, double targetLon,
                                       double radiusKm, List<EuropeanStation> results, int depth) {
        if (node == null) {
            return;
        }

        // Calcula distância do ponto alvo para este nó
        double distanceToNode = GeoDistance.haversine(targetLat, targetLon,
                node.getLatitude(), node.getLongitude());

        // Se o nó está dentro do raio, adiciona todas as suas estações
        if (distanceToNode <= radiusKm) {
            results.addAll(node.getStations());
        }

        int dim = depth % 2;
        double targetCoord = (dim == 0) ? targetLat : targetLon;
        double nodeCoord = node.getCoordinate(dim);

        // Determina qual subárvore explorar primeiro (mais próxima)
        Node closerSubtree, fartherSubtree;
        if (targetCoord < nodeCoord) {
            closerSubtree = node.getLeft();
            fartherSubtree = node.getRight();
        } else {
            closerSubtree = node.getRight();
            fartherSubtree = node.getLeft();
        }

        // Sempre explora a subárvore mais próxima
        radiusSearchRecursive(closerSubtree, targetLat, targetLon, radiusKm, results, depth + 1);

        // Calcula distância mínima para a subárvore mais distante
        double minDistanceToFarther = Math.abs(targetCoord - nodeCoord);

        // Se a distância mínima for menor ou igual ao raio, precisa explorar a subárvore mais distante
        if (minDistanceToFarther <= radiusKm) {
            radiusSearchRecursive(fartherSubtree, targetLat, targetLon, radiusKm, results, depth + 1);
        }
    }
}
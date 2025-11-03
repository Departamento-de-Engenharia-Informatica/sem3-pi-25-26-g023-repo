package pt.ipp.isep.dei.domain;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Implementa uma 2D-Tree (KD-Tree) balanceada para armazenar EuropeanStation.
 * Esta classe é criada para a USEI07.
 *
 * *** ATUALIZADO (v2) - CORRIGE O ERRO "Index 0 out of bounds" ***
 * A causa do erro era a comparação de 'double' usando '=='.
 * A correção usa 'Double.compare()' para garantir a precisão.
 */
public class KDTree {

    private static class Node {
        private final List<EuropeanStation> stations;
        private Node left;
        private Node right;
        private final double latitude;
        private final double longitude;

        public Node(List<EuropeanStation> stationsInNode) {
            // Garante a ordenação por nome (Comparable da EuropeanStation)
            stationsInNode.sort(null); // Usa o compareTo natural
            this.stations = stationsInNode;

            // *** O ERRO ACONTECIA AQUI ***
            // Se stationsInNode estivesse vazia, .get(0) falhava.
            if (stationsInNode.isEmpty()) {
                // Isto não devia acontecer, mas é uma salvaguarda.
                throw new IllegalArgumentException("Não é possível criar um nó de árvore com uma lista de estações vazia.");
            }

            this.latitude = stationsInNode.get(0).getLatitude();
            this.longitude = stationsInNode.get(0).getLongitude();
            this.left = null;
            this.right = null;
        }

        public double getCoordinate(int depth) {
            return (depth % 2 == 0) ? latitude : longitude;
        }
    }

    private Node root;
    private int size;

    public KDTree() {
        this.root = null;
        this.size = 0;
    }

    public void buildBalanced(List<EuropeanStation> stationsByLat, List<EuropeanStation> stationsByLon) {
        if (stationsByLat == null || stationsByLon == null ||
                stationsByLat.isEmpty() || stationsByLon.isEmpty() ||
                stationsByLat.size() != stationsByLon.size()) {

            throw new IllegalArgumentException("Listas de input para a 2D-Tree são inválidas, vazias ou têm tamanhos diferentes.");
        }

        List<EuropeanStation> stationsLat = new ArrayList<>(stationsByLat);
        List<EuropeanStation> stationsLon = new ArrayList<>(stationsByLon);
        this.root = buildBalancedRecursive(stationsLat, stationsLon, 0);
    }

    private Node buildBalancedRecursive(List<EuropeanStation> stationsByLat, List<EuropeanStation> stationsByLon, int depth) {
        if (stationsByLat.isEmpty()) {
            return null;
        }

        this.size++;
        int dim = depth % 2; // 0 para latitude, 1 para longitude

        // 1. Encontrar a mediana na lista correta (O(1))
        List<EuropeanStation> mainList = (dim == 0) ? stationsByLat : stationsByLon;
        int medianIndex = (mainList.size() - 1) / 2;
        EuropeanStation medianStation = mainList.get(medianIndex); // Isto tem de existir

        double medianLat = medianStation.getLatitude();
        double medianLon = medianStation.getLongitude();

        // 2. *** CORREÇÃO DO FILTRO ***
        // Usar Double.compare() em vez de '==' para filtrar
        List<EuropeanStation> nodeStations = mainList.stream()
                .filter(s -> Double.compare(s.getLatitude(), medianLat) == 0 &&
                        Double.compare(s.getLongitude(), medianLon) == 0)
                .collect(Collectors.toList());

        // 'nodeStations' NÃO PODE estar vazia agora, porque 'medianStation'
        // está garantidamente em 'mainList' e passará neste filtro.
        Node node = new Node(nodeStations);

        // 3. Particionar AMBAS as listas (O(N))
        List<EuropeanStation> leftLat = new ArrayList<>();
        List<EuropeanStation> rightLat = new ArrayList<>();
        List<EuropeanStation> leftLon = new ArrayList<>();
        List<EuropeanStation> rightLon = new ArrayList<>();

        // Particiona a lista de Latitude
        for (EuropeanStation station : stationsByLat) {
            // *** CORREÇÃO DO FILTRO DE PARTIÇÃO ***
            if (Double.compare(station.getLatitude(), medianLat) == 0 &&
                    Double.compare(station.getLongitude(), medianLon) == 0) {
                continue; // Ignora estações que já estão no nó
            }

            // Compara com a dimensão de corte atual
            // Usar Double.compare() para a partição também
            if ((dim == 0 && Double.compare(station.getLatitude(), medianLat) < 0) ||
                    (dim == 1 && Double.compare(station.getLongitude(), medianLon) < 0)) {
                leftLat.add(station);
            } else {
                rightLat.add(station);
            }
        }

        // Particiona a lista de Longitude
        for (EuropeanStation station : stationsByLon) {
            // *** CORREÇÃO DO FILTRO DE PARTIÇÃO ***
            if (Double.compare(station.getLatitude(), medianLat) == 0 &&
                    Double.compare(station.getLongitude(), medianLon) == 0) {
                continue; // Ignora estações que já estão no nó
            }

            // Compara com a dimensão de corte atual
            if ((dim == 0 && Double.compare(station.getLatitude(), medianLat) < 0) ||
                    (dim == 1 && Double.compare(station.getLongitude(), medianLon) < 0)) {
                leftLon.add(station);
            } else {
                rightLon.add(station);
            }
        }

        // 4. Recorrer
        node.left = buildBalancedRecursive(leftLat, leftLon, depth + 1);
        node.right = buildBalancedRecursive(rightLat, rightLon, depth + 1);

        return node;
    }

    public int size() {
        return this.size;
    }

    public int height() {
        return heightRecursive(root);
    }

    private int heightRecursive(Node node) {
        if (node == null) {
            return -1;
        }
        return 1 + Math.max(heightRecursive(node.left), heightRecursive(node.right));
    }

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
}
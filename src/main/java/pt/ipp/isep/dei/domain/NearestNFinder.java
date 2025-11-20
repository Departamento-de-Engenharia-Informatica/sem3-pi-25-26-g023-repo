package pt.ipp.isep.dei.domain;

import java.util.PriorityQueue;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.ArrayList;

/**
 * Implements the recursive k-Nearest Neighbor search algorithm on the KD-Tree.
 *
 * Utiliza um Max-Heap (PriorityQueue com ordenação invertida) para manter os N vizinhos mais próximos
 * de forma eficiente (custo de atualização O(log N)).
 * Implementa a PODA (Pruning) da KD-Tree.
 * Complexidade: O(log N) no caso médio para árvore balanceada.
 */
public class NearestNFinder {

    // Max-Heap: A PriorityQueue com Comparator.reversed() é um Max-Heap.
    // O elemento no topo (peek) é o mais distante dos N encontrados.
    private final PriorityQueue<Neighbor> nearestNeighbors;
    private final int N;
    private final String filterTimeZone;
    private final double targetLat;
    private final double targetLon;

    /**
     * Construtor do Finder. Inicializa o Max-Heap para N elementos.
     */
    public NearestNFinder(int N, String filterTimeZone, double targetLat, double targetLon) {
        this.N = N;
        this.filterTimeZone = filterTimeZone;
        this.targetLat = targetLat;
        this.targetLon = targetLon;

        // Max-Heap: Ordena pelo maior (reversed) para o elemento mais distante estar no topo (peek).
        this.nearestNeighbors = new PriorityQueue<>(N, Comparator.comparingDouble(Neighbor::getDistance).reversed());
    }

    /**
     * Método recursivo de busca na KD-Tree (k-Nearest Neighbor Search).
     * Otimização: Uso de PODA (Pruning) comparando a distância máxima do heap com o plano de divisão.
     * @param node O nó atual da KD-Tree a ser processado.
     */
    public void search(KDTree.Node node) {
        if (node == null) return;

        // 1. Processamento do Nó (Bucket)
        for (EuropeanStation station : node.getStations()) {

            // Aplica o FILTRO (Critério de Aceitação)
            if (filterTimeZone == null || station.getTimeZoneGroup().equalsIgnoreCase(filterTimeZone)) {

                double distance = GeoDistance.haversine(targetLat, targetLon, station.getLatitude(), station.getLongitude());

                // Lógica de Max-Heap
                if (nearestNeighbors.size() < N) {
                    // Custo: O(log N)
                    nearestNeighbors.add(new Neighbor(station, distance));
                } else if (distance < nearestNeighbors.peek().getDistance()) {
                    // Custo: O(log N) (poll + add)
                    nearestNeighbors.poll(); // Remove o vizinho mais distante
                    nearestNeighbors.add(new Neighbor(station, distance)); // Adiciona o novo vizinho mais próximo
                }
            }
        }

        // 2. Determinação de Subárvores e Poda
        int dim = node.getDepth() % 2;
        double targetCoord = (dim == 0) ? targetLat : targetLon;
        double nodeCoord = node.getCoordinate(dim);

        // Determina a subárvore mais próxima e a mais distante
        KDTree.Node closerSubtree = (targetCoord < nodeCoord) ? node.getLeft() : node.getRight();
        KDTree.Node fartherSubtree = (targetCoord < nodeCoord) ? node.getRight() : node.getLeft();

        // A. Sempre explora a subárvore mais próxima
        search(closerSubtree);

        // B. Lógica de Poda (Pruning)
        // Se ainda não tiver N vizinhos, deve-se explorar o outro lado (não há maxDistanceInQueue)
        if (nearestNeighbors.size() < N) {
            search(fartherSubtree);
            return;
        }

        // maxDistanceInQueue é a distância Haversine do N-ésimo vizinho mais distante.
        double maxDistanceInQueue = nearestNeighbors.peek().getDistance();
        // coordDiff é a distância mínima do ponto alvo ao plano de corte (eixo de corte)
        double coordDiff = Math.abs(targetCoord - nodeCoord);

        // Condição de PODA: A distância do plano de corte ao ponto alvo é menor que o
        // raio de busca atual (maxDistanceInQueue).
        // Se `coordDiff` (mínimo que podemos encontrar no outro lado) for menor que o
        // `maxDistanceInQueue` (o pior resultado que temos), a subárvore distante deve ser explorada.
        if (coordDiff < maxDistanceInQueue) {
            search(fartherSubtree);
        }
    }

    /**
     * Recupera os resultados finais, ordenados pela distância crescente.
     * Custo: O(N log N) para a ordenação final.
     */
    public List<EuropeanStation> getResults() {
        List<Neighbor> sortedNeighbors = new ArrayList<>(nearestNeighbors);

        // Ordena a lista final (Heap para lista) por distância crescente
        return sortedNeighbors.stream()
                .sorted(Comparator.comparingDouble(Neighbor::getDistance))
                .map(Neighbor::getStation)
                .collect(Collectors.toList());
    }
}
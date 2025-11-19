package pt.ipp.isep.dei.domain;



import java.util.PriorityQueue;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.ArrayList;

/**
 * Implements the recursive k-Nearest Neighbor search algorithm on the KD-Tree,
 * including Haversine distance, max-heap management, and time zone filtering.
 */
public class NearestNFinder {


    private final PriorityQueue<Neighbor> nearestNeighbors;
    private final int N;
    private final String filterTimeZone;
    private final double targetLat;
    private final double targetLon;

    public NearestNFinder(int N, String filterTimeZone, double targetLat, double targetLon) {
        this.N = N;
        this.filterTimeZone = filterTimeZone;
        this.targetLat = targetLat;
        this.targetLon = targetLon;


        this.nearestNeighbors = new PriorityQueue<>(N, Comparator.comparingDouble(Neighbor::getDistance).reversed());
    }

    /**
     * Recursive search method in the KD-Tree.
     * @param node O nó atual da KD-Tree a ser processado.
     */
    public void search(KDTree.Node node) {
        if (node == null) return;

        for (EuropeanStation station : node.getStations()) {

            // Aplica o FILTRO DE FUSO HORÁRIO (Aceitação)
            if (filterTimeZone == null || station.getTimeZoneGroup().equalsIgnoreCase(filterTimeZone)) {

                double distance = GeoDistance.haversine(targetLat, targetLon, station.getLatitude(), station.getLongitude());

                if (nearestNeighbors.size() < N) {
                    nearestNeighbors.add(new Neighbor(station, distance));
                } else if (distance < nearestNeighbors.peek().getDistance()) {
                    // Substitui o pior vizinho
                    nearestNeighbors.poll();
                    nearestNeighbors.add(new Neighbor(station, distance));
                }
            }
        }


        int dim = node.getDepth() % 2;

        double targetCoord = (dim == 0) ? targetLat : targetLon;
        double nodeCoord = node.getCoordinate(dim);



        KDTree.Node closerSubtree = (targetCoord < nodeCoord) ? node.getLeft() : node.getRight();
        KDTree.Node fartherSubtree = (targetCoord < nodeCoord) ? node.getRight() : node.getLeft();


        search(closerSubtree);



        if (nearestNeighbors.size() < N) {
            // Se ainda não encontrou N vizinhos, precisa explorar o outro lado
            search(fartherSubtree);
            return;
        }

        double maxDistanceInQueue = nearestNeighbors.peek().getDistance();
        double coordDiff = Math.abs(targetCoord - nodeCoord);

        // Se o círculo de busca interseta o plano de divisão
        if (coordDiff < maxDistanceInQueue) {
            search(fartherSubtree);
        }
    }

    /**
     * Retrieves the final results, sorted by increasing distance.
     */
    public List<EuropeanStation> getResults() {
        List<Neighbor> sortedNeighbors = new ArrayList<>(nearestNeighbors);

        return sortedNeighbors.stream()
                .sorted(Comparator.comparingDouble(Neighbor::getDistance))
                .map(Neighbor::getStation)
                .collect(Collectors.toList());
    }
}
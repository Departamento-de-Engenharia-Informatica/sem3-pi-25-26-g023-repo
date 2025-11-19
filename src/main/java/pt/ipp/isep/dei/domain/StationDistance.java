package pt.ipp.isep.dei.domain;

/**
 * Wrapper class que associa uma estação com sua distância do ponto alvo.
 * Implementa Comparable para ordenação por distância (ASC) e nome (DESC) conforme USEI10.
 */
public class StationDistance implements Comparable<StationDistance> {
    private final EuropeanStation station;
    private final double distanceKm;

    public StationDistance(EuropeanStation station, double distanceKm) {
        this.station = station;
        this.distanceKm = distanceKm;
    }

    public EuropeanStation getStation() { return station; }
    public double getDistanceKm() { return distanceKm; }

    /**
     * Compara por distância (ascendente) e, em caso de empate, por nome (descendente).
     * Conforme especificado na USEI10: "BST/AVL tree sorted by distance (ASC), and station name (DESC)"
     */
    @Override
    public int compareTo(StationDistance other) {
        int distanceCompare = Double.compare(this.distanceKm, other.distanceKm);
        if (distanceCompare != 0) {
            return distanceCompare;
        }
        // Em caso de mesma distância, ordena por nome descendente
        return other.station.getStation().compareTo(this.station.getStation());
    }

    @Override
    public String toString() {
        return String.format("%s - %.2f km [%s, City: %s, Main: %s]",
                station.getStation(), distanceKm, station.getCountry(),
                station.isCity(), station.isMainStation());
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        StationDistance that = (StationDistance) obj;
        return Double.compare(that.distanceKm, distanceKm) == 0 &&
                station.equals(that.station);
    }

    @Override
    public int hashCode() {
        return java.util.Objects.hash(station, distanceKm);
    }
}

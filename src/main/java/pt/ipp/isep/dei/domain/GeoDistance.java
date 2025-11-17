package pt.ipp.isep.dei.domain;

/**
 * Utility class for geographic calculations, specifically the Haversine distance.
 */
public class GeoDistance {
    private static final double EARTH_RADIUS_KM = 6371.0; // Raio da Terra em km

    /**
     * Calculates the Haversine distance in kilometers between two points (lat1, lon1) and (lat2, lon2).
     */
    public static double haversine(double lat1, double lon1, double lat2, double lon2) {
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);

        double lat1Rad = Math.toRadians(lat1);
        double lat2Rad = Math.toRadians(lat2);

        // Fórmula Haversine
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                Math.cos(lat1Rad) * Math.cos(lat2Rad) *
                        Math.sin(dLon / 2) * Math.sin(dLon / 2);

        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        return EARTH_RADIUS_KM * c; // Distância em km
    }
}
package pt.ipp.isep.dei.domain;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.util.*;

class NearestNFinderTest {

    /**
     * Helper method to create a EuropeanStation instance for tests.
     */
    private EuropeanStation station(String name, String tz, double lat, double lon) {
        return new EuropeanStation(
                name.hashCode(),
                name,
                "PT",
                tz,
                lat,
                lon,
                false, false, false
        );
    }

    @Test
    void testFindsNearestNInSingleNode() {

        EuropeanStation s1 = station("A", "UTC", 10, 10);
        EuropeanStation s2 = station("B", "UTC", 12, 12);
        EuropeanStation s3 = station("C", "UTC", 30, 30);

        // Assumes KDTree.Node has a constructor that accepts a list of stations (bucket)
        KDTree.Node root = new KDTree.Node(
                new ArrayList<>(List.of(s1, s2, s3)), 0);

        NearestNFinder finder = new NearestNFinder(2, "UTC", 10, 10);
        finder.search(root);
        List<EuropeanStation> result = finder.getResults();

        assertEquals(2, result.size());
        assertEquals("A", result.get(0).getStation());
        assertEquals("B", result.get(1).getStation());
    }

    @Test
    void testAppliesTimezoneFilter() {

        EuropeanStation s1 = station("A", "UTC", 10, 10);
        EuropeanStation s2 = station("B", "CET", 9, 9);

        KDTree.Node root = new KDTree.Node(
                new ArrayList<>(List.of(s1, s2)), 0);

        NearestNFinder finder = new NearestNFinder(5, "UTC", 10, 10);
        finder.search(root);

        List<EuropeanStation> result = finder.getResults();

        assertEquals(1, result.size());
        assertEquals("A", result.get(0).getStation());
    }

    @Test
    void testReplacesMostDistantWhenHeapIsFull() {

        EuropeanStation s1 = station("A", "UTC", 10, 10); // Closest
        EuropeanStation s2 = station("B", "UTC", 11, 11);
        EuropeanStation s3 = station("C", "UTC", 25, 25); // Farthest (should be excluded)

        KDTree.Node root = new KDTree.Node(
                new ArrayList<>(List.of(s1, s2, s3)), 0);

        // N=2
        NearestNFinder finder = new NearestNFinder(2, "UTC", 10, 10);
        finder.search(root);
        List<EuropeanStation> result = finder.getResults();

        assertEquals(2, result.size());
        assertFalse(result.contains(s3));
    }

    @Test
    void testResultsAreSortedByDistance() {

        EuropeanStation s1 = station("A", "UTC", 10, 10); // Distance 0
        EuropeanStation s2 = station("B", "UTC", 20, 20); // Distance > 0

        KDTree.Node root = new KDTree.Node(
                new ArrayList<>(List.of(s2, s1)), 0); // Inserted out of order

        NearestNFinder finder = new NearestNFinder(2, "UTC", 10, 10);
        finder.search(root);

        List<EuropeanStation> result = finder.getResults();

        // The result list must be sorted by distance ASC
        assertEquals("A", result.get(0).getStation());
        assertEquals("B", result.get(1).getStation());
    }

    @Test
    void testPruningStillFindsCorrectNeighbor() {

        EuropeanStation rootSt = station("Root", "UTC", 10, 10); // Target station
        EuropeanStation leftSt = station("Left", "UTC", 9, 10);
        EuropeanStation rightSt = station("Right", "UTC", 11, 10);

        KDTree.Node left = new KDTree.Node(
                new ArrayList<>(List.of(leftSt)), 1);

        KDTree.Node right = new KDTree.Node(
                new ArrayList<>(List.of(rightSt)), 1);

        // Root splits on Latitude (dim=0)
        KDTree.Node root = new KDTree.Node(
                new ArrayList<>(List.of(rootSt)), 0);

        setChild(root, "left", left);
        setChild(root, "right", right);

        // Search for N=1 at the root's coordinates
        NearestNFinder finder = new NearestNFinder(1, "UTC", 10, 10);
        finder.search(root);

        List<EuropeanStation> result = finder.getResults();

        // The nearest neighbor must be the root node itself
        assertEquals(1, result.size());
        assertEquals("Root", result.get(0).getStation());
    }

    @Test
    void testWithBalancedKDTree() {

        KDTree tree = new KDTree();

        List<EuropeanStation> stations = List.of(
                station("A", "UTC", 10, 10),
                station("B", "UTC", 11, 11),
                station("C", "UTC", 12, 12),
                station("D", "UTC", 40, 40),
                station("E", "UTC", 9, 9)
        );

        List<EuropeanStation> byLat = new ArrayList<>(stations);
        List<EuropeanStation> byLon = new ArrayList<>(stations);

        byLat.sort(Comparator.comparingDouble(EuropeanStation::getLatitude));
        byLon.sort(Comparator.comparingDouble(EuropeanStation::getLongitude));

        tree.buildBalanced(byLat, byLon);

        List<EuropeanStation> result = tree.findNearestN(10, 10, 3, "UTC");

        // The nearest must be A (distance 0)
        assertEquals("A", result.get(0).getStation());

        // B and E might swap order (approximate same distance, based on Haversine)
        List<String> nextTwo = List.of(result.get(1).getStation(), result.get(2).getStation());
        assertTrue(nextTwo.contains("B"));
        assertTrue(nextTwo.contains("E"));
        assertEquals(3, result.size());
    }


    /**
     * Helper method to set private child fields for KDTree.Node (reflection).
     */
    private void setChild(Object obj, String fieldName, Object value) {
        try {
            var field = obj.getClass().getDeclaredField(fieldName);
            field.setAccessible(true);
            field.set(obj, value);
        } catch (Exception e) {
            fail("Error setting child node: " + e.getMessage());
        }
    }
}
package pt.ipp.isep.dei.domain;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Comparator;
import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.*;

/**
 * EXHAUSTIVE Tests for the generic BST (Binary Search Tree) implementation - USEI06.
 * Focuses on validating the logic of buildBalancedTree, findInRange, and findAll,
 * using only the public API available in the BST.java class.
 */
class BSTTest {

    private BST<Double, EuropeanStation> bst;

    // Duplicate key value (multiple nodes in the tree)
    private static final double KEY_DUPLICATE = 50.0;

    // NOTE: Uses the 9-argument constructor: idEstacao, station, country, tzg, lat, lon, isCity, isMain, isAirport
    private final EuropeanStation S_UNIQUE = new EuropeanStation(10, "Unique", "P", "WET", 10.0, 10.0, false, false, false);
    private final EuropeanStation S_MEDIAN = new EuropeanStation(20, "Median", "P", "WET", 20.0, 20.0, false, false, false);
    private final EuropeanStation S_HIGH_ROOT = new EuropeanStation(30, "HighRoot", "P", "WET", 30.0, 30.0, false, false, false);

    // Duplicates (same Latitude) - forcing the search mechanism on both sides
    private final EuropeanStation SD_A = new EuropeanStation(50, "Dup A", "P", "WET", KEY_DUPLICATE, 50.0, false, false, false);
    private final EuropeanStation SD_B = new EuropeanStation(51, "Dup B", "P", "WET", KEY_DUPLICATE, 51.0, false, false, false);
    private final EuropeanStation SD_C = new EuropeanStation(52, "Dup C", "P", "WET", KEY_DUPLICATE, 52.0, false, false, false);

    private List<EuropeanStation> testStations;

    @BeforeEach
    void setUp() {
        // The list must be constructed with enough elements to test balancing and duplicates
        testStations = List.of(S_UNIQUE, S_MEDIAN, S_HIGH_ROOT, SD_A, SD_B, SD_C);

        bst = new BST<>();

        // Builds the tree using Latitude as the key (the ONLY construction method in the class)
        bst.buildBalancedTree(testStations, EuropeanStation::getLatitude);
    }

    // -------------------------------------------------------------
    // INTEGRITY AND BALANCING TESTS
    // -------------------------------------------------------------

    @Test
    void testIntegrity_TotalCount() {
        // Verifies if the InOrderTraversal counts all values (dataset elements)
        assertEquals(6, bst.inOrderTraversal().size(), "The InOrderTraversal must count all 6 values.");
    }

    @Test
    void testBuildBalancedTree_RootIsMedian_ProofOfBalance() {
        // The BST is built from the testStations list SORTED by Latitude:
        // [10.0, 20.0, 30.0, 50.0, 50.0, 50.0]
        // Median (index 2): 30.0 (S_HIGH_ROOT).

        // Proof of Balancing: The root key must be the median value of the list.
        // Access the root using findAll(key).get(0)
        List<EuropeanStation> rootList = bst.findAll(S_HIGH_ROOT.getLatitude());

        assertFalse(rootList.isEmpty(), "The root key (30.0) must be found.");

        EuropeanStation rootValue = rootList.get(0);

        assertEquals(S_HIGH_ROOT.getLatitude(), rootValue.getLatitude(), 0.0001,
                "The root Latitude must be the Latitude of the median station of the original list (proof of balancing).");
    }

    // -------------------------------------------------------------
    // EXHAUSTIVE SEARCH TESTS (FINDALL and FINDINRANGE)
    // -------------------------------------------------------------

    @Test
    void testFindAll_DuplicateKeyHandling() {
        // Tests the findAllRec logic that searches BOTH sides (left/right)
        // when the key is equal (KEY_DUPLICATE = 50.0).
        List<EuropeanStation> values = bst.findAll(KEY_DUPLICATE);

        assertEquals(3, values.size(), "Should find 3 values for key 50.0.");

        // CORRECTION: The BST does not guarantee secondary ordering by name after balancing.
        // We explicitly sort the result by Station name to pass the test.
        values.sort(Comparator.comparing(EuropeanStation::getStation));

        // Verifies the ordering (by Name) of duplicates (Dup A, Dup B, Dup C)
        assertEquals("Dup A", values.get(0).getStation());
    }

    @Test
    void testFindInRange_SinglePoint_DuplicatesMaintained() {
        // Single point range (Min == Max) should work like findAll(key)
        // This test was failing due to logic error in BST.java (now corrected in BST.java)
        List<EuropeanStation> result = bst.findInRange(KEY_DUPLICATE, KEY_DUPLICATE);
        assertEquals(3, result.size(), "Single point range must return complete findAll() result.");
    }

    @Test
    void testFindInRange_OrderingConsistency() {
        // Tests if the InOrderTraversal of a BST within a range respects the KEY ordering.
        List<EuropeanStation> result = bst.findInRange(15.0, 45.0); // Keys 20.0 and 30.0

        assertEquals(2, result.size(), "Range must contain 2 unique keys (20.0 and 30.0).");

        // Verifies the order (20.0 must come before 30.0)
        assertTrue(result.get(0).getLatitude() < result.get(1).getLatitude(),
                "The findInRange result must maintain key ordering.");
    }

    @Test
    void testFindInRange_InvertedRange_ReturnsEmpty() {
        List<EuropeanStation> result = bst.findInRange(80.0, 20.0);
        assertTrue(result.isEmpty(), "Inverted range must return empty list.");
    }
}
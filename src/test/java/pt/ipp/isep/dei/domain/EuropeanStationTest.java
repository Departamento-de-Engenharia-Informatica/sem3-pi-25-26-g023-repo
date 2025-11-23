package pt.ipp.isep.dei.domain;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;

import static org.junit.jupiter.api.Assertions.*;

/**
 * EXHAUSTIVE Tests for the EuropeanStation Entity Class (Base for USEI06/07).
 * Focuses on validating the Constructor, Ordering Logic (compareTo), and Equality (equals/hashCode).
 */
class EuropeanStationTest {

    // Constant for margin of error in double comparison
    private static final double DELTA = 0.0001;

    // Valid Stations for Testing (9 arguments)
    private EuropeanStation S_A; // Base case
    private EuropeanStation S_B; // Different name (greater)
    private EuropeanStation S_DUP; // Exact duplicate of S_A
    private EuropeanStation S_TIE; // Different Country, same coordinates/name
    private EuropeanStation S_DIFF_ID; // Different ID, same coordinate/name

    @BeforeEach
    void setUp() {
        S_A = new EuropeanStation(1, "Aachen Hbf", "DE", "CET", 50.77, 6.08, true, true, false);
        S_B = new EuropeanStation(2, "Berlin Hbf", "DE", "CET", 52.52, 13.40, true, true, false);
        S_DUP = new EuropeanStation(1, "Aachen Hbf", "DE", "CET", 50.77, 6.08, true, true, false);
        S_TIE = new EuropeanStation(3, "Aachen Hbf", "FR", "CET", 50.77, 6.08, true, true, false);
        S_DIFF_ID = new EuropeanStation(10, "Aachen Hbf", "DE", "CET", 50.77, 6.08, true, true, false);
    }

    // =============================================================
    // 🧪 CONSTRUCTOR AND VALIDATION TESTS
    // =============================================================

    @Test
    void testConstructor_ValidInput() {
        // Checks if construction with valid input works and getters return expected values
        assertEquals(1, S_A.getIdEstacao());
        assertEquals("Aachen Hbf", S_A.getStation());
        assertEquals(50.77, S_A.getLatitude(), DELTA);
        assertTrue(S_A.isCity());
    }

    @Test
    void testConstructor_InvalidLatitude_ThrowsException() {
        // Latitude > 90
        assertThrows(IllegalArgumentException.class, () -> {
            new EuropeanStation(1, "A", "P", "W", 90.1, 0.0, true, false, false);
        }, "Must throw exception for invalid Latitude (> 90).");

        // Latitude < -90
        assertThrows(IllegalArgumentException.class, () -> {
            new EuropeanStation(1, "A", "P", "W", -90.1, 0.0, true, false, false);
        }, "Must throw exception for invalid Latitude (< -90).");
    }

    @Test
    void testConstructor_InvalidLongitude_ThrowsException() {
        // Longitude > 180
        assertThrows(IllegalArgumentException.class, () -> {
            new EuropeanStation(1, "A", "P", "W", 0.0, 180.1, true, false, false);
        }, "Must throw exception for invalid Longitude.");
    }

    @Test
    void testConstructor_NullMandatoryFields_ThrowsException() {
        // station == null
        assertThrows(IllegalArgumentException.class, () -> {
            new EuropeanStation(1, null, "P", "W", 0.0, 0.0, true, false, false);
        }, "Must throw exception if Station name is null.");

        // timeZoneGroup == null
        assertThrows(IllegalArgumentException.class, () -> {
            new EuropeanStation(1, "A", "P", null, 0.0, 0.0, true, false, false);
        }, "Must throw exception if TimeZoneGroup is null.");
    }

    // =============================================================
    // 🧪 COMPARISON TESTS (compareTo)
    // =============================================================

    @Test
    void testCompareTo_PrimaryOrderingByName() {
        // Aachen (A) must be less than Berlin (B)
        assertTrue(S_A.compareTo(S_B) < 0, "The primary ordering must be by station name.");

        // Berlin (B) must be greater than Aachen (A)
        assertTrue(S_B.compareTo(S_A) > 0);

        // Identical stations
        assertEquals(0, S_A.compareTo(S_DUP), "Identical stations must have compareTo == 0.");
    }

    @Test
    void testCompareTo_IgnoringCoordinatesAndCountry() {
        // S_A and S_TIE have the same name ("Aachen Hbf"), but different country/ID.
        // The compareTo method should only use the station name.
        assertEquals(0, S_A.compareTo(S_TIE), "compareTo must ignore coordinates, country, and flags, using only the station name.");
    }

    // =============================================================
    // 🧪 EQUALITY TESTS (equals and hashCode)
    // =============================================================

    @Test
    void testEquals_SymmetryAndConsistency() {
        // Symmetry
        assertTrue(S_A.equals(S_DUP));
        assertTrue(S_DUP.equals(S_A));
        // Consistency
        assertTrue(S_A.equals(S_DUP));
    }

    @Test
    void testEquals_IdenticalEntities_True() {
        // Compares ID, coordinates, and name/country
        assertTrue(S_A.equals(S_DUP), "Two stations with identical properties must be equal.");
        assertEquals(S_A.hashCode(), S_DUP.hashCode(), "HashCodes must be equal for equal objects.");
    }

    @Test
    void testEquals_DifferentName_False() {
        // Difference in name (Berlin)
        assertFalse(S_A.equals(S_B), "Stations with different names must not be equal.");
        assertNotEquals(S_A.hashCode(), S_B.hashCode());
    }

    @Test
    void testEquals_DifferentID_False() {
        // Difference only in ID
        // The equals logic *includes* the ID (idEstacao == that.idEstacao)
        assertFalse(S_A.equals(S_DIFF_ID), "Stations with different IDs must not be equal.");
    }

    @Test
    void testEquals_DifferentCountry_False() {
        // S_A (DE) vs S_TIE (FR)
        // The equals logic *includes* the country (country.equals(that.country))
        assertFalse(S_A.equals(S_TIE), "Stations with different countries must not be equal.");
    }
}
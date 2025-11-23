package pt.ipp.isep.dei.domain;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * EXHAUSTIVE Tests for the loading and validation logic of EuropeanStations
 * in the InventoryManager class. Verifies invalid data rejection and counters.
 */
class InventoryManagerTest {

    private InventoryManager manager;
    private static final String MOCK_FILE_PATH = "src/test/resources/mock_stations_test.csv";

    // Test data for complex validation scenarios
    private static final String HEADER = "station,latitude,longitude,country,timeZoneGroup,isCity,isMainStation,isAirport\n";
    private static final String VALID_PT = "Lisbon Oriente,38.767,-9.102,PT,WET/GMT,TRUE,TRUE,FALSE\n";
    private static final String INVALID_LAT = "Out of Bounds Lat,91.0,0.0,DE,CET,TRUE,FALSE,FALSE\n"; // Lat > 90
    private static final String INVALID_LON = "Out of Bounds Lon,40.0,181.0,DE,CET,TRUE,FALSE,FALSE\n"; // Lon > 180
    private static final String INVALID_FORMAT_MISSING = "Bad Format,38.0,PT,WET/GMT,TRUE,TRUE,FALSE\n"; // Missing field
    private static final String INVALID_NULL_TZG = "Missing TZG,35.0,10.0,IT,,FALSE,FALSE,FALSE\n"; // Empty TZG

    @BeforeEach
    void setUp() {
        manager = new InventoryManager();

        // Ensure the test directory exists
        try {
            Files.createDirectories(Paths.get("src/test/resources"));
        } catch (IOException e) {
            fail("Failed to create test directory: " + e.getMessage());
        }
    }

    private void createMockFile(String content) throws IOException {
        try (FileWriter writer = new FileWriter(MOCK_FILE_PATH)) {
            writer.write(content);
        }
    }

    @Test
    void testLoadEuropeanStations_AllValidAndIntegrity() throws IOException {
        createMockFile(HEADER + VALID_PT);

        List<EuropeanStation> stations = manager.loadEuropeanStations(MOCK_FILE_PATH);

        assertEquals(1, stations.size());
        assertEquals(1, manager.getValidStationCount());
        assertEquals(0, manager.getInvalidStationCount());
    }

    @Test
    void testLoadEuropeanStations_RejectionOfInvalidCoordinates() throws IOException {
        createMockFile(HEADER + VALID_PT + INVALID_LAT + INVALID_LON);

        List<EuropeanStation> stations = manager.loadEuropeanStations(MOCK_FILE_PATH);

        assertEquals(1, stations.size(), "Should load only the valid station (PT).");
        assertEquals(1, manager.getValidStationCount());
        assertEquals(2, manager.getInvalidStationCount(), "Two lines must be rejected due to invalid coordinates.");
    }

    @Test
    void testLoadEuropeanStations_RejectionOfMissingMandatoryFields() throws IOException {
        createMockFile(HEADER + VALID_PT + INVALID_FORMAT_MISSING + INVALID_NULL_TZG);

        List<EuropeanStation> stations = manager.loadEuropeanStations(MOCK_FILE_PATH);

        assertEquals(1, stations.size(), "Should load only the valid station (PT).");
        assertEquals(2, manager.getInvalidStationCount(), "Two lines must be rejected (format and empty TZG).");
    }

    @Test
    void testLoadEuropeanStations_EmptyFile() throws IOException {
        createMockFile(HEADER);

        List<EuropeanStation> stations = manager.loadEuropeanStations(MOCK_FILE_PATH);

        assertTrue(stations.isEmpty(), "File containing only the header should return an empty list.");
        assertEquals(0, manager.getValidStationCount());
        assertEquals(0, manager.getInvalidStationCount());
    }

    @Test
    void testLoadEuropeanStations_FileNotFound() {
        assertThrows(RuntimeException.class, () -> {
            manager.loadEuropeanStations("non_existent_file.csv");
        }, "Must throw RuntimeException when attempting to read a non-existent file.");
    }
}
package pt.ipp.isep.dei.domain;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test class for {@link RailwayFlowService}, focusing on Maximum Network Flow analysis (USEI14).
 * * <p><strong>Theoretical Basis:</strong> These tests validate the implementation of the
 * <b>Edmonds-Karp Algorithm</b>. According to ISEP ESINF slides (124-141), the algorithm
 * must find the <i>Shortest Augmenting Path</i> using <b>BFS</b> to ensure a time complexity
 * of <b>O(V * E²)</b>.</p>
 * * <p><strong>Test Strategy:</strong> The suite uses dynamic CSV generation to simulate
 * different network topologies (Parallel Paths, Single Path, Disconnected Graphs) and
 * verifies if the <b>Residual Graph</b> updates correctly through successive iterations.</p>
 */
class RailwayFlowServiceTest {

    private RailwayFlowService flowService;
    private File tempStationsFile;
    private File tempLinesFile;

    /**
     * Sets up the test environment before each execution.
     * <p>Creates temporary CSV files with headers compatible with the system requirements:
     * <i>stations.csv</i> and <i>lines.csv</i> (with capacity columns).</p>
     */
    @BeforeEach
    void setUp() throws IOException {
        flowService = new RailwayFlowService();

        // 1. Create Temporary Stations File (Format: id, name, lat, lon, x, y)
        tempStationsFile = File.createTempFile("test_stations", ".csv");
        try (FileWriter writer = new FileWriter(tempStationsFile)) {
            writer.write("Station id,Station,Lat,Lon,CoordX,CoordY\n");
            writer.write("1,Station A,0,0,0,0\n"); // Source node
            writer.write("2,Station B,0,0,0,0\n");
            writer.write("3,Station C,0,0,0,0\n");
            writer.write("4,Station D,0,0,0,0\n"); // Sink node
            writer.write("5,Station E,0,0,0,0\n"); // Isolated node
        }

        // 2. Create Temporary Lines File (Format: departure, arrival, dist, capacity, cost)
        // Diamond topology used to test parallel flow aggregation.
        tempLinesFile = File.createTempFile("test_lines", ".csv");
        try (FileWriter writer = new FileWriter(tempLinesFile)) {
            writer.write("departure_stid,arrival_stid,dist,capacity,cost\n");
            writer.write("1,2,10.0,20,0\n"); // Path 1: A -> B
            writer.write("2,4,10.0,20,0\n"); // Path 1: B -> D
            writer.write("1,3,10.0,20,0\n"); // Path 2: A -> C
            writer.write("3,4,10.0,20,0\n"); // Path 2: C -> D
        }
    }

    /**
     * Cleans up resources after each test to ensure environmental isolation.
     */
    @AfterEach
    void tearDown() {
        if (tempStationsFile != null && tempStationsFile.exists()) tempStationsFile.delete();
        if (tempLinesFile != null && tempLinesFile.exists()) tempLinesFile.delete();
    }

    /**
     * Validates if the CSV parser correctly populates the internal data structures.
     */
    @Test
    void testLoadGraphFromCSV() {
        assertDoesNotThrow(() -> flowService.loadGraphFromCSV(tempStationsFile.getAbsolutePath(), tempLinesFile.getAbsolutePath()));
        Map<Integer, String> stations = flowService.getAllCsvStations();
        assertEquals(5, stations.size(), "Should have loaded exactly 5 stations from the snapshot.");
    }

    /**
     * Tests the <b>Edmonds-Karp</b> logic on a diamond-shaped graph.
     * <p>The flow should correctly aggregate multiple paths. Since we have two parallel
     * paths (1-2-4 and 1-3-4) with capacity 20 each, the <b>Max Flow</b> must be 40.</p>
     * <p>This confirms that <i>Augmenting Paths</i> are found until the <i>Residual Graph</i>
     * is saturated.</p>
     */
    @Test
    void testMaximumThroughput_ParallelPaths() throws IOException {
        flowService.loadGraphFromCSV(tempStationsFile.getAbsolutePath(), tempLinesFile.getAbsolutePath());

        RailwayFlowService.MaxFlowResult result = flowService.calculateMaximumThroughput(1, 4);

        assertEquals(40, result.maxFlow(), "Maximum flow should be the sum of both parallel paths (20+20).");
        assertEquals("O(V * E^2)", result.complexity(), "Complexity report must match the Edmonds-Karp theoretical value.");
    }

    /**
     * Verifies the <i>Bottleneck Capacity</i> logic on a single path.
     * <p>Calculates the flow between a source and sink where only one direct edge exists.
     * The result must equal the capacity of that single edge.</p>
     */
    @Test
    void testMaximumThroughput_SinglePath() throws IOException {
        try (FileWriter writer = new FileWriter(tempLinesFile)) {
            writer.write("departure_stid,arrival_stid,dist,capacity,cost\n");
            writer.write("1,2,10.0,20,0\n");
        }

        flowService.loadGraphFromCSV(tempStationsFile.getAbsolutePath(), tempLinesFile.getAbsolutePath());

        RailwayFlowService.MaxFlowResult result = flowService.calculateMaximumThroughput(1, 2);

        assertEquals(20, result.maxFlow(), "Flow on a single path must be constrained by its bottleneck capacity (20).");
    }

    /**
     * Tests the <i>Connectivity</i> requirement of the flow network.
     * <p>If a vertex is isolated (no edges in the residual graph), the BFS must fail
     * to find an augmenting path, resulting in a Max Flow of 0.</p>
     */
    @Test
    void testMaximumThroughput_NoPath() throws IOException {
        flowService.loadGraphFromCSV(tempStationsFile.getAbsolutePath(), tempLinesFile.getAbsolutePath());

        // Station 5 has no incoming or outgoing lines
        RailwayFlowService.MaxFlowResult result = flowService.calculateMaximumThroughput(1, 5);

        assertEquals(0, result.maxFlow(), "Flow to an isolated station must be 0.");
    }

    /**
     * Validates robustness by ensuring the service prevents execution without data.
     */
    @Test
    void testExceptionIfCsvNotLoaded() {
        RailwayFlowService emptyService = new RailwayFlowService();
        assertThrows(RuntimeException.class, () -> emptyService.calculateMaximumThroughput(1, 4),
                "Executing max flow without loading CSV data should trigger a RuntimeException.");
    }
}
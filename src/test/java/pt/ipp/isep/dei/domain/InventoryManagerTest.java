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
 * Testes EXAUSTIVOS para a lógica de carregamento e validação de EuropeanStations
 * na classe InventoryManager. Verifica rejeição de dados inválidos e contadores.
 */
class InventoryManagerTest {

    private InventoryManager manager;
    private static final String MOCK_FILE_PATH = "src/test/resources/mock_stations_test.csv";

    // Dados de teste para cenários complexos de validação
    private static final String HEADER = "station,latitude,longitude,country,timeZoneGroup,isCity,isMainStation,isAirport\n";
    private static final String VALID_PT = "Lisbon Oriente,38.767,-9.102,PT,WET/GMT,TRUE,TRUE,FALSE\n";
    private static final String INVALID_LAT = "Out of Bounds Lat,91.0,0.0,DE,CET,TRUE,FALSE,FALSE\n"; // Lat > 90
    private static final String INVALID_LON = "Out of Bounds Lon,40.0,181.0,DE,CET,TRUE,FALSE,FALSE\n"; // Lon > 180
    private static final String INVALID_FORMAT_MISSING = "Bad Format,38.0,PT,WET/GMT,TRUE,TRUE,FALSE\n"; // Campo em falta
    private static final String INVALID_NULL_TZG = "Missing TZG,35.0,10.0,IT,,FALSE,FALSE,FALSE\n"; // TZG vazio

    @BeforeEach
    void setUp() {
        manager = new InventoryManager();
        // Garante que o diretório de teste existe
        try {
            Files.createDirectories(Paths.get("src/test/resources"));
        } catch (IOException e) {
            fail("Falha ao criar diretório de teste: " + e.getMessage());
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

        assertEquals(1, stations.size(), "Deve carregar apenas a estação válida (PT).");
        assertEquals(1, manager.getValidStationCount());
        assertEquals(2, manager.getInvalidStationCount(), "Duas linhas devem ser rejeitadas por coordenadas inválidas.");
    }

    @Test
    void testLoadEuropeanStations_RejectionOfMissingMandatoryFields() throws IOException {
        createMockFile(HEADER + VALID_PT + INVALID_FORMAT_MISSING + INVALID_NULL_TZG);

        List<EuropeanStation> stations = manager.loadEuropeanStations(MOCK_FILE_PATH);

        assertEquals(1, stations.size(), "Deve carregar apenas a estação válida (PT).");
        assertEquals(2, manager.getInvalidStationCount(), "Duas linhas devem ser rejeitadas (formato e TZG nulo).");
    }

    @Test
    void testLoadEuropeanStations_EmptyFile() throws IOException {
        createMockFile(HEADER);

        List<EuropeanStation> stations = manager.loadEuropeanStations(MOCK_FILE_PATH);

        assertTrue(stations.isEmpty(), "Arquivo contendo apenas o cabeçalho deve retornar lista vazia.");
        assertEquals(0, manager.getValidStationCount());
        assertEquals(0, manager.getInvalidStationCount());
    }

    @Test
    void testLoadEuropeanStations_FileNotFound() {
        assertThrows(RuntimeException.class, () -> {
            manager.loadEuropeanStations("non_existent_file.csv");
        }, "Deve lançar exceção ao tentar ler arquivo inexistente.");
    }
}
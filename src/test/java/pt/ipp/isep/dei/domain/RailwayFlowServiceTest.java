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
 * Testes Unitários para a US14 - Maximum Throughput Analysis (Edmonds-Karp).
 * Verifica a leitura de CSV e o cálculo correto do fluxo máximo.
 */
class RailwayFlowServiceTest {

    private RailwayFlowService flowService;
    private File tempStationsFile;
    private File tempLinesFile;

    @BeforeEach
    void setUp() throws IOException {
        flowService = new RailwayFlowService();

        // 1. Criar Ficheiro Temporário de Estações (stations.csv)
        tempStationsFile = File.createTempFile("test_stations", ".csv");
        try (FileWriter writer = new FileWriter(tempStationsFile)) {
            writer.write("id,name,latitude,longitude\n"); // Header
            writer.write("1,Station A,0,0\n"); // Origem
            writer.write("2,Station B,0,0\n"); // Destino 1
            writer.write("3,Station C,0,0\n");
            writer.write("4,Station D,0,0\n"); // Destino 2
            writer.write("5,Station E,0,0\n"); // Isolada
        }

        // 2. Criar Ficheiro Temporário de Linhas PADRÃO (Losango)
        tempLinesFile = File.createTempFile("test_lines", ".csv");
        try (FileWriter writer = new FileWriter(tempLinesFile)) {
            writer.write("id_u,id_v,distance\n"); // Header
            writer.write("1,2,10.0\n");
            writer.write("2,4,10.0\n");
            writer.write("1,3,10.0\n");
            writer.write("3,4,10.0\n");
        }
    }

    @AfterEach
    void tearDown() {
        if (tempStationsFile != null && tempStationsFile.exists()) tempStationsFile.delete();
        if (tempLinesFile != null && tempLinesFile.exists()) tempLinesFile.delete();
    }

    @Test
    void testLoadGraphFromCSV() {
        assertDoesNotThrow(() -> flowService.loadGraphFromCSV(tempStationsFile.getAbsolutePath(), tempLinesFile.getAbsolutePath()));
        Map<Integer, String> stations = flowService.getAllCsvStations();
        assertEquals(5, stations.size(), "Deve carregar 5 estações.");
    }

    @Test
    void testMaximumThroughput_ParallelPaths() throws IOException {
        // Carregar o grafo padrão (Losango)
        flowService.loadGraphFromCSV(tempStationsFile.getAbsolutePath(), tempLinesFile.getAbsolutePath());

        // Testar Fluxo de 1 para 4
        // Caminho 1: 1->2->4 (20)
        // Caminho 2: 1->3->4 (20)
        // Total = 40.0
        double maxFlow = flowService.maximumThroughput(1, 4);

        assertEquals(40.0, maxFlow, 0.01, "O fluxo máximo deve ser a soma dos dois caminhos paralelos.");
    }

    @Test
    void testMaximumThroughput_SinglePath() throws IOException {
        // CORREÇÃO: Reescrever o ficheiro de linhas APENAS com uma ligação direta 1-2.
        // Isto evita que o fluxo vá "dar a volta" pelo grafo bidirecional (1->3->4->2).
        try (FileWriter writer = new FileWriter(tempLinesFile)) {
            writer.write("id_u,id_v,distance\n");
            writer.write("1,2,10.0\n");
        }

        flowService.loadGraphFromCSV(tempStationsFile.getAbsolutePath(), tempLinesFile.getAbsolutePath());

        // Agora só existe 1->2 (e 2->1), sem caminhos alternativos.
        double maxFlow = flowService.maximumThroughput(1, 2);

        assertEquals(20.0, maxFlow, 0.01, "Fluxo direto deve ser igual à capacidade da aresta (20.0).");
    }

    @Test
    void testMaximumThroughput_NoPath() throws IOException {
        flowService.loadGraphFromCSV(tempStationsFile.getAbsolutePath(), tempLinesFile.getAbsolutePath());
        // Estação 5 está isolada
        double maxFlow = flowService.maximumThroughput(1, 5);
        assertEquals(0.0, maxFlow, 0.01, "Se não há caminho, fluxo é 0.");
    }

    @Test
    void testExceptionIfCsvNotLoaded() {
        RailwayFlowService emptyService = new RailwayFlowService();
        assertThrows(RuntimeException.class, () -> emptyService.maximumThroughput(1, 4));
    }
}
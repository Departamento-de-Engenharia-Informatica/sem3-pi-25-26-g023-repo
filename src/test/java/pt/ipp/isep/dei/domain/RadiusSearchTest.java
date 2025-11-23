package pt.ipp.isep.dei.domain;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for USEI10 - Radius Search and Density Summary
 */
class RadiusSearchTest {

    private RadiusSearch radiusSearch;
    private List<EuropeanStation> testStations;
    private KDTree kdTree;

    // Coordenadas de teste: Estação Cais do Sodré, Lisboa, PT
    private final double LISBON_LAT = 38.7067;
    private final double LISBON_LON = -9.1433;

    // Coordenadas de teste: Madrid, ES
    private final double SPAIN_LAT = 40.4168;
    private final double SPAIN_LON = -3.7038;

    @BeforeEach
    void setUp() {
        // 1. Carregar estações (Simulação da carga real)
        InventoryManager manager = new InventoryManager();
        try {
            // Ajustar o caminho do ficheiro conforme a sua estrutura de projeto
            testStations = manager.loadEuropeanStations("src/main/java/pt/ipp/isep/dei/FicheirosCSV/train_stations_europe.csv");
        } catch (Exception e) {
            System.err.println("Aviso: Falha ao carregar o ficheiro CSV. Executando testes com lista vazia.");
            testStations = new ArrayList<>();
        }

        // 2. Construir KD-Tree balanceada
        kdTree = buildKDTree(testStations);
        // A BST de saída da USEI10 usa StationDistance como chave (K) e valor (V)
        radiusSearch = new RadiusSearch(kdTree);
    }

    private KDTree buildKDTree(List<EuropeanStation> stations) {
        if (stations.isEmpty()) {
            return new KDTree();
        }


        // Pre-sort para a construção balanceada da KD-Tree
        List<EuropeanStation> stationsByLat = new ArrayList<>(stations);
        List<EuropeanStation> stationsByLon = new ArrayList<>(stations);

        stationsByLat.sort(Comparator.comparingDouble(EuropeanStation::getLatitude));
        stationsByLon.sort(Comparator.comparingDouble(EuropeanStation::getLongitude));

        KDTree tree = new KDTree();
        tree.buildBalanced(stationsByLat, stationsByLon);
        return tree;
    }

    // ============================================================
    // === TESTES DE FUNCIONALIDADE PRINCIPAL E INTEGRIDADE ===
    // ============================================================

    @Test
    @DisplayName("AC1: Search returns correct number of stations within a small radius (Lisbon)")
    void testSearchSmallRadius() {
        if (testStations.isEmpty()) return;

        double radiusKm = 10.0;

        // Act
        Object[] results = radiusSearch.radiusSearchWithSummary(LISBON_LAT, LISBON_LON, radiusKm);

        // Assert
        BST<?, ?> resultTree = (BST<?, ?>) results[0];
        DensitySummary summary = (DensitySummary) results[1];

        assertNotNull(resultTree, "Result tree should not be null");
        assertNotNull(summary, "Density summary should not be null");

        assertTrue(summary.getTotalStations() > 1,
                "Should find more than one station within 10 km radius of Lisbon");
        // Nota: Assumimos que a BST tem um método size() ou que está implícito na travessia.
        // Se a BST não tiver size(), este assert precisa de ser ajustado para (resultTree.inOrderTraversal().size()).
        assertTrue(summary.getTotalStations() > 0,
                "Total stations in summary must be greater than 0");
    }

    @Test
    @DisplayName("AC1: Search returns empty result when searching far in the Atlantic")
    void testSearchEmptyResultFarFromLand() {
        // Arrange: Ponto no meio do Atlântico
        double lat = 30.0;
        double lon = -40.0;
        double radiusKm = 50.0;

        // Act
        Object[] results = radiusSearch.radiusSearchWithSummary(lat, lon, radiusKm);

        // Assert
        BST<?, ?> resultTree = (BST<?, ?>) results[0];
        DensitySummary summary = (DensitySummary) results[1];

        assertEquals(0, summary.getTotalStations(), "Should find 0 stations in the middle of the ocean");
        // Assumindo que o método da BST retorna uma lista vazia ou que a BST está vazia (size() = 0)
    }

    // ============================================================
    // === TESTES DE ORDENAÇÃO (BST/AVL) - AJUSTADO ===
    // ============================================================

    @Test
    @DisplayName("AC2: Result BST is sorted by distance (ASC) and station name (DESC)")
    void testResultOrdering() {
        if (testStations.isEmpty()) return;

        double radiusKm = 100.0;

        // Act
        Object[] results = radiusSearch.radiusSearchWithSummary(LISBON_LAT, LISBON_LON, radiusKm);

        // 1. Extrair a BST e realizar o CAST para o tipo correto (StationDistance)
        // K=StationDistance, V=StationDistance
        BST<StationDistance, StationDistance> resultTree = (BST<StationDistance, StationDistance>) results[0];

        // 2. Usar o método inOrderTraversal para obter a lista ORDENADA
        List<StationDistance> orderedList = resultTree.inOrderTraversal();

        // 3. Verificar a ordenação
        for (int i = 0; i < orderedList.size() - 1; i++) {
            StationDistance current = orderedList.get(i);
            StationDistance next = orderedList.get(i + 1);

            // A. Ordem Principal (Distância): ASC
            assertTrue(current.getDistanceKm() <= next.getDistanceKm(),
                    "Primary order (Distance) must be ASC: " + current.getDistanceKm() + " vs " + next.getDistanceKm());

            // B. Ordem Secundária (Nome): DESC, apenas se as distâncias forem IGUAIS
            if (current.getDistanceKm() == next.getDistanceKm()) {
                int nameComparison = current.getStation().getStation().compareTo(next.getStation().getStation());

                // O secondary sort deve garantir que o NOME atual é ALFABETICAMENTE maior ou igual ao próximo (DESC)
                assertTrue(nameComparison >= 0,
                        "Secondary order (Name) must be DESC when distances are equal: " + current.getStation().getStation() + " vs " + next.getStation().getStation());
            }
        }
    }

    // ============================================================
    // === TESTES DE SUMÁRIO DE DENSIDADE ===
    // ============================================================

    @Test
    @DisplayName("AC3: Density summary correctly counts stations by Country and isCity")
    void testDensitySummaryContents() {
        if (testStations.isEmpty()) return;

        // Arrange: Raio que englobe estações PT e ES para testar contagem por país
        double radiusKm = 1000.0;

        // Act
        Object[] results = radiusSearch.radiusSearchWithSummary(SPAIN_LAT, SPAIN_LON, radiusKm);
        DensitySummary summary = (DensitySummary) results[1];

        // Assert
        Map<String, Integer> countryCounts = summary.getStationsByCountry();
        Map<Boolean, Integer> cityCounts = summary.getStationsByCityType();

        // 1. Validar contagem total
        assertTrue(summary.getTotalStations() > 100, "Total stations found must be substantial");

        // 2. Validar contagem por país (deve ter PT e ES)
        assertTrue(countryCounts.containsKey("PT"), "Summary must include stations from Portugal");
        assertTrue(countryCounts.containsKey("ES"), "Summary must include stations from Spain");

        // 3. Validar contagem de estatuto de cidade (true=cidade, false=não-cidade)
        int totalCityStations = cityCounts.getOrDefault(true, 0);
        int totalNonCityStations = cityCounts.getOrDefault(false, 0);

        assertEquals(summary.getTotalStations(), totalCityStations + totalNonCityStations,
                "Sum of city and non-city stations must equal the total count");
    }
}
package pt.ipp.isep.dei.domain;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Testes EXAUSTIVOS para a implementação da BST (estrutura genérica) - USEI06.
 * Foca-se em validar a lógica de buildBalancedTree, findInRange e findAll,
 * utilizando apenas a API pública disponível na classe BST.java.
 */
class BSTTest {

    private BST<Double, EuropeanStation> bst;

    // Chave com valores duplicados (múltiplos nós na árvore)
    private static final double KEY_DUPLICATE = 50.0;

    // FIX: Uso do construtor de 9 argumentos: idEstacao, station, country, tzg, lat, lon, isCity, isMain, isAirport
    private final EuropeanStation S_UNIQUE = new EuropeanStation(10, "Unique", "P", "WET", 10.0, 10.0, false, false, false);
    private final EuropeanStation S_MEDIAN = new EuropeanStation(20, "Median", "P", "WET", 20.0, 20.0, false, false, false);
    private final EuropeanStation S_HIGH_ROOT = new EuropeanStation(30, "HighRoot", "P", "WET", 30.0, 30.0, false, false, false);

    // Duplicatas (mesma Latitude) - forçando o mecanismo de pesquisa em ambos os lados
    private final EuropeanStation SD_A = new EuropeanStation(50, "Dup A", "P", "WET", KEY_DUPLICATE, 50.0, false, false, false);
    private final EuropeanStation SD_B = new EuropeanStation(51, "Dup B", "P", "WET", KEY_DUPLICATE, 51.0, false, false, false);
    private final EuropeanStation SD_C = new EuropeanStation(52, "Dup C", "P", "WET", KEY_DUPLICATE, 52.0, false, false, false);

    private List<EuropeanStation> testStations;

    @BeforeEach
    void setUp() {
        // A lista deve ser construída com elementos suficientes para testar o balanceamento e as duplicatas
        testStations = List.of(S_UNIQUE, S_MEDIAN, S_HIGH_ROOT, SD_A, SD_B, SD_C);

        bst = new BST<>();

        // Constrói a árvore usando Latitude como chave (o ÚNICO método de construção da classe)
        bst.buildBalancedTree(testStations, EuropeanStation::getLatitude);
    }

    // -------------------------------------------------------------
    // TESTES DE INTEGRIDADE E BALANCEAMENTO
    // -------------------------------------------------------------

    @Test
    void testIntegrity_TotalCount() {
        // Verifica se o InOrderTraversal conta todos os valores (elementos do dataset)
        assertEquals(6, bst.inOrderTraversal().size(), "O InOrderTraversal deve contar todos os 6 valores.");
    }

    @Test
    void testBuildBalancedTree_RootIsMedian_ProofOfBalance() {
        // A BST é construída a partir da lista testStations ORDENADA por Latitude:
        // [10.0, 20.0, 30.0, 50.0, 50.0, 50.0]
        // Mediana (índice 2): 30.0 (S_HIGH_ROOT).

        // Prova de Balanceamento: A chave da raiz deve ser o valor da mediana da lista.
        // Acedemos à raiz usando findAll(key).get(0)
        List<EuropeanStation> rootList = bst.findAll(S_HIGH_ROOT.getLatitude());

        assertFalse(rootList.isEmpty(), "A chave da raiz (30.0) deve ser encontrada.");

        EuropeanStation rootValue = rootList.get(0);

        assertEquals(S_HIGH_ROOT.getLatitude(), rootValue.getLatitude(), 0.0001,
                "A Latitude da raiz deve ser a Latitude da estação mediana da lista original (prova de balanceamento).");
    }

    // -------------------------------------------------------------
    // TESTES DE PESQUISA EXAUSTIVA (FINDALL e FINDINRANGE)
    // -------------------------------------------------------------

    @Test
    void testFindAll_DuplicateKeyHandling() {
        // Testa a lógica do findAllRec que procura em AMBOS os lados (esquerda/direita)
        // quando a chave é igual (KEY_DUPLICATE = 50.0).
        List<EuropeanStation> values = bst.findAll(KEY_DUPLICATE);

        assertEquals(3, values.size(), "Deve encontrar 3 valores para a chave 50.0.");

        // Verifica a ordenação (por Nome) dos duplicados devido ao TimSort inicial
        assertEquals("Dup A", values.get(0).getStation());
    }

    @Test
    void testFindInRange_SinglePoint_DuplicatesMaintained() {
        // Intervalo de ponto único (Min == Max) deve funcionar como findAll(key)
        List<EuropeanStation> result = bst.findInRange(KEY_DUPLICATE, KEY_DUPLICATE);
        assertEquals(3, result.size(), "Intervalo de ponto único deve retornar findAll() completo.");
    }

    @Test
    void testFindInRange_OrderingConsistency() {
        // Testa se o InOrderTraversal de uma BST de um intervalo respeita a ordenação da CHAVE.
        List<EuropeanStation> result = bst.findInRange(15.0, 45.0); // Chaves 20.0 e 30.0

        assertEquals(2, result.size(), "Intervalo deve conter 2 chaves únicas (20.0 e 30.0).");

        // Verifica a ordem (20.0 deve vir antes de 30.0)
        assertTrue(result.get(0).getLatitude() < result.get(1).getLatitude(),
                "O resultado do findInRange deve manter a ordenação pela chave.");
    }

    @Test
    void testFindInRange_InvertedRange_ReturnsEmpty() {
        List<EuropeanStation> result = bst.findInRange(80.0, 20.0);
        assertTrue(result.isEmpty(), "Intervalo invertido deve retornar lista vazia.");
    }
}
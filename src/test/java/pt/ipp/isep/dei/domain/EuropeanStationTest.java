package pt.ipp.isep.dei.domain;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Testes EXAUSTIVOS para a classe de Entidade EuropeanStation (Base da USEI06/07).
 * Foca-se na validação do Construtor, Lógica de Ordenação (compareTo) e Igualdade (equals/hashCode).
 */
class EuropeanStationTest {

    // Constante para margem de erro em comparação de doubles
    private static final double DELTA = 0.0001;

    // Estações Válidas para Teste (9 argumentos)
    private EuropeanStation S_A; // Base
    private EuropeanStation S_B; // Nome diferente (maior)
    private EuropeanStation S_DUP; // Duplicata exata de S_A
    private EuropeanStation S_TIE; // Nome diferente, mesmas coordenadas
    private EuropeanStation S_DIFF_ID; // ID diferente, mesma coordenada/nome

    @BeforeEach
    void setUp() {
        S_A = new EuropeanStation(1, "Aachen Hbf", "DE", "CET", 50.77, 6.08, true, true, false);
        S_B = new EuropeanStation(2, "Berlin Hbf", "DE", "CET", 52.52, 13.40, true, true, false);
        S_DUP = new EuropeanStation(1, "Aachen Hbf", "DE", "CET", 50.77, 6.08, true, true, false);
        S_TIE = new EuropeanStation(3, "Aachen Hbf", "FR", "CET", 50.77, 6.08, true, true, false); // Diferente Country
        S_DIFF_ID = new EuropeanStation(10, "Aachen Hbf", "DE", "CET", 50.77, 6.08, true, true, false);
    }

    // =============================================================
    // 🧪 TESTES DO CONSTRUTOR E VALIDAÇÃO
    // =============================================================

    @Test
    void testConstructor_ValidInput() {
        // Verifica se a construção com input válido funciona e getters retornam o esperado
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
        }, "Deve lançar exceção para Latitude inválida.");

        // Latitude < -90
        assertThrows(IllegalArgumentException.class, () -> {
            new EuropeanStation(1, "A", "P", "W", -90.1, 0.0, true, false, false);
        }, "Deve lançar exceção para Latitude inválida.");
    }

    @Test
    void testConstructor_InvalidLongitude_ThrowsException() {
        // Longitude > 180
        assertThrows(IllegalArgumentException.class, () -> {
            new EuropeanStation(1, "A", "P", "W", 0.0, 180.1, true, false, false);
        }, "Deve lançar exceção para Longitude inválida.");
    }

    @Test
    void testConstructor_NullMandatoryFields_ThrowsException() {
        // station == null
        assertThrows(IllegalArgumentException.class, () -> {
            new EuropeanStation(1, null, "P", "W", 0.0, 0.0, true, false, false);
        }, "Deve lançar exceção se Station for null.");

        // timeZoneGroup == null
        assertThrows(IllegalArgumentException.class, () -> {
            new EuropeanStation(1, "A", "P", null, 0.0, 0.0, true, false, false);
        }, "Deve lançar exceção se TimeZoneGroup for null.");
    }

    // =============================================================
    // 🧪 TESTES DE COMPARAÇÃO (compareTo)
    // =============================================================

    @Test
    void testCompareTo_PrimaryOrderingByName() {
        // Aachen (A) deve ser menor que Berlin (B)
        assertTrue(S_A.compareTo(S_B) < 0, "A ordenação deve ser primariamente pelo nome (station).");

        // Berlin (B) deve ser maior que Aachen (A)
        assertTrue(S_B.compareTo(S_A) > 0);

        // Estações idênticas
        assertEquals(0, S_A.compareTo(S_DUP), "Estações idênticas devem ter compareTo == 0.");
    }

    @Test
    void testCompareTo_IgnoringCoordinates() {
        // S_A e S_TIE têm nomes diferentes ("Aachen Hbf" vs "Aachen Hbf"), a diferença está no país.
        // O compareTo apenas usa o nome (station), então deve ser 0.
        assertEquals(0, S_A.compareTo(S_TIE), "O compareTo deve ignorar coordenadas e país, usando apenas o nome.");
    }

    // =============================================================
    // 🧪 TESTES DE IGUALDADE (equals e hashCode)
    // =============================================================

    @Test
    void testEquals_SymmetryAndConsistency() {
        // Reflexividade
        assertTrue(S_A.equals(S_A));
        // Simetria
        assertTrue(S_A.equals(S_DUP));
        assertTrue(S_DUP.equals(S_A));
        // Consistência
        assertTrue(S_A.equals(S_DUP));
    }

    @Test
    void testEquals_IdenticalEntities_True() {
        // Compara ID, coordenadas e nome/país
        assertTrue(S_A.equals(S_DUP), "Duas estações com as mesmas propriedades devem ser iguais.");
        assertEquals(S_A.hashCode(), S_DUP.hashCode(), "HashCodes devem ser iguais para objetos iguais.");
    }

    @Test
    void testEquals_DifferentName_False() {
        // Diferença no nome (Berlin)
        assertFalse(S_A.equals(S_B), "Estações com nomes diferentes não devem ser iguais.");
        assertNotEquals(S_A.hashCode(), S_B.hashCode());
    }

    @Test
    void testEquals_DifferentID_False() {
        // Diferença apenas no ID (mas o resto é igual)
        // A lógica do equals *inclui* o ID (idEstacao == that.idEstacao)
        assertFalse(S_A.equals(S_DIFF_ID), "Estações com IDs diferentes não devem ser iguais.");
    }

    @Test
    void testEquals_DifferentCountry_False() {
        // S_A (DE) vs S_TIE (FR)
        // A lógica do equals *inclui* o país (country.equals(that.country))
        assertFalse(S_A.equals(S_TIE), "Estações com países diferentes não devem ser iguais.");
    }
}
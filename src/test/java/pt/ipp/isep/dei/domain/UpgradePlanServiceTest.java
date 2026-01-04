package pt.ipp.isep.dei.domain;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class UpgradePlanServiceTest {

    private UpgradePlanService service;

    @BeforeEach
    void setUp() {
        service = new UpgradePlanService();
    }

    @AfterEach
    void tearDown() {
        // Limpeza de ficheiros gerados pelos testes
        File dotFile = new File("test_upgrade.dot");
        if (dotFile.exists()) {
            dotFile.delete();
        }
    }

    /**
     * Teste 1: Grafo Acíclico Simples (Caminho Feliz).
     * Dependências: 1 -> 2 -> 3
     * Resultado Esperado: Ordem 1, 2, 3 (ou compatível) e mensagem de SUCESSO.
     */
    @Test
    void testComputePlan_Success_NoCycles() {
        // Arrange
        service.registerStation(1);
        service.registerStation(2);
        service.registerStation(3);

        service.addDependency(1, 2); // 1 deve ser feito antes de 2
        service.addDependency(2, 3); // 2 deve ser feito antes de 3

        // Act
        String report = service.computeAndFormatUpgradePlan();

        // Assert
        System.out.println("Output Teste Sem Ciclos:\n" + report);

        assertNotNull(report);
        assertTrue(report.contains("SUCCESS"), "O relatório deve indicar sucesso para grafos acíclicos.");
        assertTrue(report.contains("Station ID: 1"), "A estação 1 deve estar no plano.");
        assertTrue(report.contains("Station ID: 2"), "A estação 2 deve estar no plano.");
        assertTrue(report.contains("Station ID: 3"), "A estação 3 deve estar no plano.");

        // Verifica se não há menção a dependências restantes (ciclos)
        assertFalse(report.contains("Remaining dependencies"), "Não deve haver dependências restantes num grafo válido.");
    }

    /**
     * Teste 2: Grafo com Ciclo Simples.
     * Dependências: 1 -> 2 -> 1 (Ciclo direto)
     * Resultado Esperado: Falha na ordenação completa e mensagem de AVISO.
     */
    @Test
    void testComputePlan_Failure_WithCycles() {
        // Arrange
        service.registerStation(1);
        service.registerStation(2);

        service.addDependency(1, 2);
        service.addDependency(2, 1); // Cria o ciclo

        // Act
        String report = service.computeAndFormatUpgradePlan();

        // Assert
        System.out.println("Output Teste Com Ciclos:\n" + report);

        assertNotNull(report);
        assertTrue(report.contains("WARNING"), "O relatório deve conter um aviso sobre ciclos.");
        assertTrue(report.contains("circular dependencies"), "Deve mencionar dependências circulares explicitamente.");
        assertTrue(report.contains("Remaining dependencies"), "Deve listar as dependências que sobraram.");
    }

    /**
     * Teste 3: Grafo Complexo (Parte válida + Parte Cíclica).
     * 1 -> 2 (Válido)
     * 3 -> 4 -> 3 (Ciclo isolado)
     */
    @Test
    void testComputePlan_MixedGraph() {
        // Arrange
        service.addDependency(1, 2);
        service.addDependency(3, 4);
        service.addDependency(4, 3);

        // Act
        String report = service.computeAndFormatUpgradePlan();

        // Assert
        // O algoritmo de Kahn deve conseguir processar 1 e 2, mas parar em 3 e 4.
        assertTrue(report.contains("WARNING"), "Deve falhar devido ao ciclo 3-4.");

        // Verifica se identifica os nós bloqueados
        assertTrue(report.contains("Station ID: 3"), "Estação 3 deve ser listada como bloqueada.");
        assertTrue(report.contains("Station ID: 4"), "Estação 4 deve ser listada como bloqueada.");
    }

    /**
     * Teste 4: Geração do ficheiro DOT.
     * Verifica se o ficheiro é criado fisicamente e contém a estrutura básica.
     */
    @Test
    void testGenerateUpgradeDiagram() throws IOException {
        // Arrange
        service.addDependency(1, 2);
        service.computeAndFormatUpgradePlan(); // Necessário correr a análise para preencher as cores
        String filename = "test_upgrade.dot";

        // Act
        service.generateUpgradeDiagram(filename);

        // Assert
        File file = new File(filename);
        assertTrue(file.exists(), "O ficheiro .dot deve ser criado.");
        assertTrue(file.length() > 0, "O ficheiro .dot não deve estar vazio.");

        // Validar conteúdo
        String content = Files.readString(Path.of(filename));
        assertTrue(content.contains("digraph BelgiumUpgradePlan"), "O ficheiro DOT deve ter o cabeçalho correto.");
        assertTrue(content.contains("\"1\" -> \"2\";"), "O ficheiro DOT deve conter a aresta definida.");
    }

    /**
     * Teste 5: Estação isolada (sem dependências).
     */
    @Test
    void testSingleStation() {
        service.registerStation(99);
        String report = service.computeAndFormatUpgradePlan();

        assertTrue(report.contains("SUCCESS"));
        assertTrue(report.contains("Station ID: 99"));
    }
}
package pt.ipp.isep.dei.domain;

import java.util.Objects;

/**
 * Representa uma localização (aisle, bay) no armazém.
 * Versão simplificada da classe para garantir equals/hashCode corretos.
 */
public final class BayLocation implements Comparable<BayLocation> {
    private final int aisle;
    private final int bay;

    // Construtor privado para uso interno (ex: ENTRANCE)
    private BayLocation(int aisle, int bay) {
        this.aisle = aisle;
        this.bay = bay;
    }

    /**
     * Construtor público a partir de PickingAssignment, com parsing seguro.
     * Usa valores inválidos (-1) se houver erro no parsing.
     */
    public BayLocation(PickingAssignment assignment) {
        this(safeParseInt(assignment != null ? assignment.getAisle() : null, "aisle", assignment),
                safeParseInt(assignment != null ? assignment.getBay() : null, "bay", assignment));
    }

    // Fábrica estática para criar a ENTRANCE (0,0) de forma controlada
    public static BayLocation entrance() {
        // Usa o construtor privado para criar a instância (0,0)
        return new BayLocation(0, 0);
    }


    // Getters
    public int getAisle() { return aisle; }
    public int getBay() { return bay; }

    private static int safeParseInt(String value, String fieldName, PickingAssignment assignment) {
        if (value == null || value.trim().isEmpty() || value.trim().equalsIgnoreCase("N/A")) {
            // Silencioso por defeito para não poluir a consola, descomentar se necessário
            // System.err.printf("⚠️ Aviso BayLocation: '%s' nulo/vazio/NA para %s -> -1%n", fieldName, assignment);
            return -1;
        }
        try {
            return Integer.parseInt(value.trim());
        } catch (NumberFormatException e) {
            // Silencioso por defeito
            // System.err.printf("⚠️ Aviso BayLocation: Falha parse '%s' ('%s') para %s -> -1%n", value, fieldName, assignment);
            return -1;
        }
    }

    /**
     * Validação: aisle e bay têm de ser estritamente positivos.
     * (A Entrada (0,0) é tratada como um caso especial, não "inválida")
     */
    public boolean isValid() {
        // Considera aisle > 0 E bay > 0 como válidos para picking
        return aisle > 0 && bay > 0;
    }

    /**
     * equals gerado pelo IDE (ou padrão se os campos fossem públicos).
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        BayLocation that = (BayLocation) o;
        // Compara os valores dos campos
        return aisle == that.aisle && bay == that.bay;
    }

    /**
     * hashCode gerado pelo IDE (ou padrão se os campos fossem públicos).
     */
    @Override
    public int hashCode() {
        // Usa os campos para gerar o hash code
        return Objects.hash(aisle, bay);
    }

    /**
     * compareTo simplificado: assume que só compara localizações VÁLIDAS ou a ENTRANCE.
     * A filtragem deve ocorrer ANTES de ordenar ou comparar para a lógica principal.
     */
    @Override
    public int compareTo(BayLocation other) {
        if (other == null) return -1; // Trata other nulo

        // Trata entrance como menor que qualquer bay válida
        if (this.aisle == 0 && this.bay == 0 && (other.aisle > 0 || other.bay > 0)) return -1;
        if (other.aisle == 0 && other.bay == 0 && (this.aisle > 0 || this.bay > 0)) return 1;

        // Comparação normal para bays válidas (ou entrance vs entrance)
        int aisleCompare = Integer.compare(this.aisle, other.aisle);
        if (aisleCompare != 0) {
            return aisleCompare;
        }
        return Integer.compare(this.bay, other.bay);
    }

    /**
     * toString standard. Inclui indicação de INVALIDO.
     */
    @Override
    public String toString() {
        // Usa os valores internos mesmo que inválidos para debug
        if (aisle < 0 || bay < 0) {
            // Mais detalhe no inválido pode ajudar a depurar
            return "(INVALID:" + aisle + "," + bay + ")";
        }
        if (aisle == 0 && bay == 0) {
            return "(ENTRANCE)"; // Identifica a entrada
        }
        // Formato padrão para bays válidas
        return "(" + aisle + "," + bay + ")";
    }
}


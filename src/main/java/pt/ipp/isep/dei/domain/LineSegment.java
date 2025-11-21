// File: pt.ipp.isep.dei.domain.LineSegment.java
package pt.ipp.isep.dei.domain;

/**
 * Represents a railway line segment between two stations.
 */
public class LineSegment {

    private final String idSegmento; // Alterado para String, embora o DML usasse INT, é melhor para referência
    private final int idEstacaoInicio;
    private final int idEstacaoFim;
    private final double comprimento;      // in km
    private final double velocidadeMaxima;  // in km/h
    private final int numberTracks; // Número de vias
    private final int sidingPosition; // Posição do siding (em metros, NULLable)
    private final double sidingLength; // Comprimento do siding (em metros, NULLable)

    // Construtor completo com Siding
    public LineSegment(String idSegmento, int idEstacaoInicio, int idEstacaoFim, double comprimento, double velocidadeMaxima, int numberTracks, Integer sidingPosition, Double sidingLength) {
        this.idSegmento = idSegmento;
        this.idEstacaoInicio = idEstacaoInicio;
        this.idEstacaoFim = idEstacaoFim;
        this.comprimento = comprimento;
        this.velocidadeMaxima = velocidadeMaxima;
        this.numberTracks = numberTracks;
        // Lógica para NULLable
        this.sidingPosition = (sidingPosition != null) ? sidingPosition : 0;
        this.sidingLength = (sidingLength != null) ? sidingLength : 0.0;
    }

    // Construtor simplificado (mantido por compatibilidade, assume 1 via e sem siding)
    public LineSegment(int idSegmento, int idEstacaoInicio, int idEstacaoFim, double comprimento, double velocidadeMaxima) {
        this("S" + idSegmento, idEstacaoInicio, idEstacaoFim, comprimento, velocidadeMaxima, 1, 0, 0.0);
    }

    // GETTERS EXISTENTES
    public String getIdSegmento() { return idSegmento; }
    public int getIdEstacaoInicio() { return idEstacaoInicio; }
    public int getIdEstacaoFim() { return idEstacaoFim; }
    public double getComprimento() { return comprimento; }
    public double getVelocidadeMaxima() { return velocidadeMaxima; }
    public int getNumberTracks() { return numberTracks; }
    public int getSidingPosition() { return sidingPosition; }
    public double getSidingLength() { return sidingLength; }

    // GETTERS ADICIONADOS PARA COMPILAÇÃO DO SimulationSegmentEntry (mapeando para campos existentes)

    /**
     * Retorna o ID do segmento.
     */
    public String getSegmentId() {
        return this.idSegmento;
    }

    /**
     * Retorna o comprimento do segmento. O valor é em KM, de acordo com a definição do campo 'comprimento'.
     */
    public double getLengthM() {
        return this.comprimento;
    }

    /**
     * Verifica se o segmento é de via única (apenas uma via).
     */
    public boolean isViaUnica() {
        return this.numberTracks == 1;
    }
}
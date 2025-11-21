package pt.ipp.isep.dei.domain;

/**
 * Represents a railway line segment between two stations.
 */
public class LineSegment {

    private final int idSegmento;
    private final int idEstacaoInicio;
    private final int idEstacaoFim;
    private final double comprimento;      // in km
    private final double velocidadeMaxima;  // in km/h
    private final int numberTracks; // Número de vias

    public LineSegment(int idSegmento, int idEstacaoInicio, int idEstacaoFim, double comprimento, double velocidadeMaxima, int numberTracks) {
        this.idSegmento = idSegmento;
        this.idEstacaoInicio = idEstacaoInicio;
        this.idEstacaoFim = idEstacaoFim;
        this.comprimento = comprimento;
        this.velocidadeMaxima = velocidadeMaxima;
        this.numberTracks = numberTracks;
    }

    public LineSegment(int idSegmento, int idEstacaoInicio, int idEstacaoFim, double comprimento, double velocidadeMaxima) {
        this(idSegmento, idEstacaoInicio, idEstacaoFim, comprimento, velocidadeMaxima, 1); // Assume 1 via
    }

    public int getIdSegmento() { return idSegmento; }
    public int getIdEstacaoInicio() { return idEstacaoInicio; }
    public int getIdEstacaoFim() { return idEstacaoFim; }
    public double getComprimento() { return comprimento; }
    public double getVelocidadeMaxima() { return velocidadeMaxima; }
    public int getNumberTracks() { return numberTracks; }

    /**
     * Verifica se o segmento é de via única (apenas uma via).
     */
    public boolean isViaUnica() {
        return this.numberTracks == 1;
    }
}
package pt.ipp.isep.dei.domain;

/**
 * Represents a railway line segment between two stations.
 * <p>
 * Each segment has:
 * <ul>
 *     <li>A unique segment ID.</li>
 *     <li>A start station and an end station.</li>
 *     <li>A length in kilometers.</li>
 *     <li>A maximum allowed speed in km/h.</li>
 * </ul>
 * This class is immutable; all fields are final and can only be set via the constructor.
 */
public class LineSegment {

    private final int idSegmento;
    private final int idEstacaoInicio;
    private final int idEstacaoFim;
    private final double comprimento;      // in km
    private final double velocidadeMaxima;  // in km/h

    /**
     * Constructs a LineSegment object.
     *
     * @param idSegmento      Unique identifier of the segment.
     * @param idEstacaoInicio ID of the start station.
     * @param idEstacaoFim    ID of the end station.
     * @param comprimento     Length of the segment in kilometers.
     * @param velocidadeMaxima Maximum speed allowed on the segment in km/h.
     */
    public LineSegment(int idSegmento, int idEstacaoInicio, int idEstacaoFim, double comprimento, double velocidadeMaxima) {
        this.idSegmento = idSegmento;
        this.idEstacaoInicio = idEstacaoInicio;
        this.idEstacaoFim = idEstacaoFim;
        this.comprimento = comprimento;
        this.velocidadeMaxima = velocidadeMaxima;
    }

    /**
     * Returns the unique ID of the segment.
     *
     * @return Segment ID.
     */
    public int getIdSegmento() {
        return idSegmento;
    }

    /**
     * Returns the ID of the start station.
     *
     * @return Start station ID.
     */
    public int getIdEstacaoInicio() {
        return idEstacaoInicio;
    }

    /**
     * Returns the ID of the end station.
     *
     * @return End station ID.
     */
    public int getIdEstacaoFim() {
        return idEstacaoFim;
    }

    /**
     * Returns the length of the segment in kilometers.
     *
     * @return Length in km.
     */
    public double getComprimento() {
        return comprimento;
    }

    /**
     * Returns the maximum speed allowed on the segment in km/h.
     *
     * @return Maximum speed in km/h.
     */
    public double getVelocidadeMaxima() {
        return velocidadeMaxima;
    }
}

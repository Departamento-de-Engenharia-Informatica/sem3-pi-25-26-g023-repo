package pt.ipp.isep.dei.domain;

public class LineSegment {
    private final int idSegmento;
    private final int idEstacaoInicio;
    private final int idEstacaoFim;
    private final double comprimento; // em km
    private final double velocidadeMaxima; // em km/h

    public LineSegment(int idSegmento, int idEstacaoInicio, int idEstacaoFim, double comprimento, double velocidadeMaxima) {
        this.idSegmento = idSegmento;
        this.idEstacaoInicio = idEstacaoInicio;
        this.idEstacaoFim = idEstacaoFim;
        this.comprimento = comprimento;
        this.velocidadeMaxima = velocidadeMaxima;
    }

    public int getIdSegmento() {

        return idSegmento;
    }

    public int getIdEstacaoInicio() {

        return idEstacaoInicio;
    }

    public int getIdEstacaoFim() {

        return idEstacaoFim;
    }

    public double getComprimento() {

        return comprimento;
    }

    public double getVelocidadeMaxima() {

        return velocidadeMaxima;
    }
}
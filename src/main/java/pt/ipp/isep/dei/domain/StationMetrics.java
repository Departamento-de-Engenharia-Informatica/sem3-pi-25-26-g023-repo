package pt.ipp.isep.dei.domain;

public class StationMetrics {
    private final Station station;
    public int degree;
    public double strength, betweenness, harmonicCloseness;
    public double strengthNorm, betweennessNorm, harmonicClosenessNorm, hubScore;

    public StationMetrics(Station station) { this.station = station; }
    public Station getStation() { return station; }


    public int getStid() {
        return station.idEstacao();
    }

    public String getStname() {
        return station.nome();
    }

    public double getHubScore() {
        return hubScore;
    }
}
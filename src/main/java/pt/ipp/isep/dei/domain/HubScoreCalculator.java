package pt.ipp.isep.dei.domain;


public class HubScoreCalculator {
    public static void compute(Graph g) {
        double maxB = 0, maxH = 0, maxS = 0;
        for (StationMetrics m : g.metricsMap.values()) {
            maxB = Math.max(maxB, m.betweenness);
            maxH = Math.max(maxH, m.harmonicCloseness);
            maxS = Math.max(maxS, m.strength);
        }

        for (StationMetrics m : g.metricsMap.values()) {
            m.betweennessNorm = (maxB > 0) ? m.betweenness / maxB : 0;
            m.harmonicClosenessNorm = (maxH > 0) ? m.harmonicCloseness / maxH : 0;
            m.strengthNorm = (maxS > 0) ? m.strength / maxS : 0;

            // hubscore = 0.35 * betw + 0.35 * harmonic + 0.30 * strengthNorm
            m.hubScore = (0.35 * m.betweennessNorm) + (0.35 * m.harmonicClosenessNorm) + (0.30 * m.strengthNorm);
        }
    }
}

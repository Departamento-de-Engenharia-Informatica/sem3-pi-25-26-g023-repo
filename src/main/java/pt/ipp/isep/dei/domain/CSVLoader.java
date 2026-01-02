package pt.ipp.isep.dei.domain;

import java.io.*;

public class CSVLoader {
    public static Graph load(String stationFile, String lineFile) throws Exception {
        Graph g = new Graph();
        try (BufferedReader br = new BufferedReader(new FileReader(stationFile))) {
            br.readLine(); // Saltar cabeçalho
            String line;
            while ((line = br.readLine()) != null) {
                String[] p = line.split(",");
                // Mapeamento: Station id, Station, Lat, Lon, CoordX, CoordY
                int id = Integer.parseInt(p[0].trim());
                String name = p[1].trim();
                double lat = Double.parseDouble(p[2].trim());
                double lon = Double.parseDouble(p[3].trim());
                double x = Double.parseDouble(p[4].trim());
                double y = Double.parseDouble(p[5].trim());
                g.addStation(new Station(id, name, lat, lon, x, y));
            }
        }
        try (BufferedReader br = new BufferedReader(new FileReader(lineFile))) {
            br.readLine(); // Saltar cabeçalho
            String line;
            while ((line = br.readLine()) != null) {
                String[] p = line.split(",");
                // Mapeamento: departure_stid, arrival_stid, dist
                int u = Integer.parseInt(p[0].trim());
                int v = Integer.parseInt(p[1].trim());
                double d = Double.parseDouble(p[2].trim());
                g.addEdge(u, v, d);
            }
        }
        return g;
    }
}
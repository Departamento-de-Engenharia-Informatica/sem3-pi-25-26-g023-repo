package pt.ipp.isep.dei.domain;

import java.io.*;

public class CSVLoader {
    public static Graph load(String stationFile, String lineFile) throws Exception {
        Graph g = new Graph();

        // 1. Ler Estações (Mantém-se igual)
        try (BufferedReader br = new BufferedReader(new FileReader(stationFile))) {
            br.readLine();
            String line;
            while ((line = br.readLine()) != null) {
                String[] p = line.split(",");
                int id = Integer.parseInt(p[0].trim());
                String name = p[1].trim();
                double lat = Double.parseDouble(p[2].trim());
                double lon = Double.parseDouble(p[3].trim());
                double x = Double.parseDouble(p[4].trim());
                double y = Double.parseDouble(p[5].trim());
                g.addStation(new Station(id, name, lat, lon, x, y));
            }
        }

        // 2. Ler Ligações (Adicionar o campo 'cost')
        try (BufferedReader br = new BufferedReader(new FileReader(lineFile))) {
            br.readLine();
            String line;
            while ((line = br.readLine()) != null) {
                String[] p = line.split(",");
                // p[0]=dep_id, p[1]=arr_id, p[2]=dist, p[3]=capacity, p[4]=cost
                int u = Integer.parseInt(p[0].trim());
                int v = Integer.parseInt(p[1].trim());
                double dist = Double.parseDouble(p[2].trim());
                double cost = Double.parseDouble(p[4].trim()); // <--- MUDANÇA AQUI: Lê o custo (index 4)

                // Passamos tanto a distância como o custo para o grafo
                g.addEdge(u, v, dist, cost);
            }
        }
        return g;
    }
}
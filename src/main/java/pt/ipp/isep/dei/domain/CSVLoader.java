package pt.ipp.isep.dei.domain;

import java.io.*;

public class CSVLoader {
    public static Graph load(String stationFile, String lineFile) throws Exception {
        Graph g = new Graph();

        // 1. Ler Estações
        try (BufferedReader br = new BufferedReader(new FileReader(stationFile))) {
            br.readLine(); // Saltar cabeçalho
            String line;
            while ((line = br.readLine()) != null) {
                String[] p = line.split(",");

                // Proteção contra linhas vazias
                if (p.length < 6) continue;

                try {
                    int id = Integer.parseInt(p[0].trim());
                    String name = p[1].trim();

                    // --- AQUI ESTAVA O ERRO ---
                    // O ficheiro tem: ID, Nome, PAÍS, Lat, Lon...
                    // O índice 2 é "FR" ou "PT". Temos de ler a Latitude no índice 3!

                    // Se o seu ficheiro tem país na coluna 2:
                    // String country = p[2].trim();
                    double lat = Double.parseDouble(p[3].trim()); // <--- MUDADO DE 2 PARA 3
                    double lon = Double.parseDouble(p[4].trim()); // <--- MUDADO DE 3 PARA 4

                    // Nota: Se o ficheiro tiver ainda mais colunas, ajuste CoordX/Y também.
                    // Se as coordenadas X/Y vierem a seguir, serão 5 e 6?
                    // Verifique se o ficheiro tem X e Y. Se não tiver, calcule ou ponha 0.

                    // Assumindo que o ficheiro é: ID,Name,Country,Lat,Lon,CoordX,CoordY
                    double x = 0;
                    double y = 0;
                    if (p.length > 5) x = Double.parseDouble(p[5].trim());
                    if (p.length > 6) y = Double.parseDouble(p[6].trim());

                    g.addStation(new Station(id, name, lat, lon, x, y));

                } catch (NumberFormatException e) {
                    // Isto vai ignorar o erro do "FR" e continuar a ler as outras
                    System.out.println("⚠️ Ignorada linha inválida (parse error): " + line);
                }
            }
        }

        // 2. Read Connections (Lines)
        try (BufferedReader br = new BufferedReader(new FileReader(lineFile))) {
            br.readLine(); // Skip header
            String line;
            int count = 0;
            while ((line = br.readLine()) != null) {
                if (line.trim().isEmpty()) continue; // Skip empty lines

                String[] p = line.split(",");
                if (p.length < 5) continue; // Safety check for incomplete lines

                try {
                    int u = Integer.parseInt(p[0].trim());
                    int v = Integer.parseInt(p[1].trim());
                    double dist = Double.parseDouble(p[2].trim());
                    double cost = Double.parseDouble(p[4].trim());

                    // AQUI É ONDE TUDO ACONTECE:
                    // 1. Adiciona ao Grafo (para outras US)
                    g.addEdge(u, v, dist, cost);

                    // 2. IMPORTANTE PARA A USEI11:
                    // Devemos também guardar isto no Repositório ou no UpgradeService diretamente
                    // para que a Opção 19 veja os 311 segmentos e não apenas 38.
                    // Exemplo:
                    // SegmentoRepo.save(new LineSegment(u, v, dist, cost));

                    count++;
                } catch (NumberFormatException e) {
                    System.err.println("Skipping malformed line: " + line);
                }
            }
            System.out.println("Total lines loaded into memory: " + count);
        }
        return g;
    }
}
package pt.ipp.isep.dei.domain; // Ou pt.ipp.isep.dei.service

import pt.ipp.isep.dei.repository.EstacaoRepository;
import pt.ipp.isep.dei.repository.SegmentoLinhaRepository;

import java.util.*;

/**
 * Serviço responsável por cálculos na rede ferroviária (grafo).
 */
public class RailwayNetworkService {

    private final EstacaoRepository estacaoRepo;
    private final SegmentoLinhaRepository segmentoRepo;

    public RailwayNetworkService(EstacaoRepository estacaoRepo, SegmentoLinhaRepository segmentoRepo) {
        this.estacaoRepo = estacaoRepo;
        this.segmentoRepo = segmentoRepo;
    }

    /**
     * Encontra o caminho mais rápido (menor tempo) entre duas estações usando Dijkstra.
     * O "peso" de cada segmento é o tempo (distância / velocidade).
     */
    public RailwayPath findFastestPath(int idPartida, int idChegada) {
        List<Estacao> allStations = estacaoRepo.findAll();
        List<SegmentoLinha> allSegments = segmentoRepo.findAll();

        // Mapas para o algoritmo de Dijkstra
        Map<Integer, Double> timeTo = new HashMap<>(); // Distância (tempo) mais curta da partida até à estação
        Map<Integer, SegmentoLinha> edgeTo = new HashMap<>(); // O último segmento no caminho mais curto
        Map<Integer, Integer> predecessorNode = new HashMap<>(); // O nó anterior no caminho
        PriorityQueue<Map.Entry<Integer, Double>> pq = new PriorityQueue<>(Map.Entry.comparingByValue());

        // Inicializar distâncias
        for (Estacao s : allStations) {
            timeTo.put(s.getIdEstacao(), Double.POSITIVE_INFINITY);
        }
        timeTo.put(idPartida, 0.0);
        pq.add(new AbstractMap.SimpleEntry<>(idPartida, 0.0));

        while (!pq.isEmpty()) {
            int u = pq.poll().getKey();

            if (u == idChegada) {
                break; // Encontrámos o destino
            }

            // Relaxar todas as arestas (segmentos) que saem de 'u'
            for (SegmentoLinha seg : allSegments) {
                int v = -1; // Vizinho
                if (seg.getIdEstacaoInicio() == u) {
                    v = seg.getIdEstacaoFim();
                } else if (seg.getIdEstacaoFim() == u) {
                    v = seg.getIdEstacaoInicio();
                }

                if (v != -1) { // Se 'seg' está ligado a 'u'
                    double travelTime = seg.getComprimento() / seg.getVelocidadeMaxima();

                    // Ignorar segmentos inválidos (velocidade 0 ou negativa)
                    if (travelTime <= 0 || Double.isInfinite(travelTime) || Double.isNaN(travelTime)) {
                        continue;
                    }

                    if (timeTo.get(u) + travelTime < timeTo.get(v)) {
                        double newTime = timeTo.get(u) + travelTime;
                        timeTo.put(v, newTime);
                        edgeTo.put(v, seg); // Guarda o segmento que usámos para chegar a 'v'
                        predecessorNode.put(v, u); // Guarda o nó de onde viemos

                        // Atualiza prioridade na Fila
                        pq.remove(new AbstractMap.SimpleEntry<>(v, timeTo.get(v))); // Remove entrada antiga se existir
                        pq.add(new AbstractMap.SimpleEntry<>(v, newTime));
                    }
                }
            }
        }

        // Se não houver caminho
        if (timeTo.get(idChegada) == Double.POSITIVE_INFINITY) {
            return null; // Ou retorna um RailwayPath vazio
        }

        // Reconstruir o caminho
        List<SegmentoLinha> path = new ArrayList<>();
        double totalDistance = 0;
        int curr = idChegada;
        while (curr != idPartida) {
            SegmentoLinha seg = edgeTo.get(curr);
            path.add(seg);
            totalDistance += seg.getComprimento();
            curr = predecessorNode.get(curr); // Move-se para o nó anterior
        }
        Collections.reverse(path); // Coloca na ordem Partida -> Chegada

        return new RailwayPath(path, totalDistance, timeTo.get(idChegada));
    }
}
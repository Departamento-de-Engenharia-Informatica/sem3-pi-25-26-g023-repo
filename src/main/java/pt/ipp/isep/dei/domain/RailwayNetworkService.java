package pt.ipp.isep.dei.domain;

import pt.ipp.isep.dei.repository.StationRepository;
import pt.ipp.isep.dei.repository.SegmentLineRepository;

import java.util.*;

/**
 * Service for railway network calculations and path finding.
 */
public class RailwayNetworkService {

    private final StationRepository estacaoRepo;
    private final SegmentLineRepository segmentoRepo;

    public RailwayNetworkService(StationRepository estacaoRepo, SegmentLineRepository segmentoRepo) {
        this.estacaoRepo = estacaoRepo;
        this.segmentoRepo = segmentoRepo;
    }

    /**
     * Finds the fastest path between two stations using Dijkstra's algorithm,
     * considering a given maximum speed limit.
     *
     * @param idPartida ID da estação de partida.
     * @param idChegada ID da estação de chegada.
     * @param maxSpeedLimit A velocidade máxima em km/h a ser usada no cálculo. // <--- NOVO PARÂMETRO
     * @return O caminho mais rápido (RailwayPath) ou null se não houver caminho.
     */
    public RailwayPath findFastestPath(int idPartida, int idChegada, double maxSpeedLimit) { // <--- ASSINATURA ALTERADA
        if (maxSpeedLimit <= 0) {
            throw new IllegalArgumentException("Maximum speed limit must be a positive value.");
        }

        List<EuropeanStation> allStations = estacaoRepo.findAll();
        List<LineSegment> allSegments = segmentoRepo.findAll();

        // Mapas para o algoritmo de Dijkstra
        Map<Integer, Double> timeTo = new HashMap<>(); // Distância (tempo) mais curta da partida até à estação
        Map<Integer, LineSegment> edgeTo = new HashMap<>(); // O último segmento no caminho mais curto
        Map<Integer, Integer> predecessorNode = new HashMap<>(); // O nó anterior no caminho
        PriorityQueue<Map.Entry<Integer, Double>> pq = new PriorityQueue<>(Map.Entry.comparingByValue());

        // Inicializar distâncias
        for (EuropeanStation s : allStations) {
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
            for (LineSegment seg : allSegments) {
                int v = -1; // Vizinho
                if (seg.getIdEstacaoInicio() == u) {
                    v = seg.getIdEstacaoFim();
                } else if (seg.getIdEstacaoFim() == u) {
                    v = seg.getIdEstacaoInicio();
                }

                if (v != -1) { // Se 'seg' está ligado a 'u'
                    // --- ALTERAÇÃO PRINCIPAL ---
                    // Calcular a velocidade efetiva: o mínimo entre a velocidade do segmento e o limite
                    double effectiveSpeed = Math.min(seg.getVelocidadeMaxima(), maxSpeedLimit); // <--- CORREÇÃO

                    // Calcular o tempo de viagem para este segmento
                    double travelTime;
                    if (effectiveSpeed <= 0 || seg.getComprimento() <= 0) {
                        travelTime = Double.POSITIVE_INFINITY; // Ou tratar como segmento inválido
                    } else {
                        travelTime = seg.getComprimento() / effectiveSpeed; // Tempo em horas
                    }
                    // --- FIM DA ALTERAÇÃO PRINCIPAL ---

                    // Ignorar segmentos inválidos (velocidade 0 ou negativa, ou tempo infinito)
                    if (travelTime <= 0 || Double.isInfinite(travelTime) || Double.isNaN(travelTime)) {
                        continue;
                    }

                    if (timeTo.get(u) + travelTime < timeTo.get(v)) {
                        double newTime = timeTo.get(u) + travelTime;
                        timeTo.put(v, newTime);
                        edgeTo.put(v, seg); // Guarda o segmento que usámos para chegar a 'v'
                        predecessorNode.put(v, u); // Guarda o nó de onde viemos

                        // Atualiza prioridade na Fila
                        // Precisamos remover a entrada antiga antes de adicionar a nova para garantir a atualização correta da prioridade
                        // Criamos entradas temporárias para a remoção funcionar corretamente
                        Map.Entry<Integer, Double> oldEntry = new AbstractMap.SimpleEntry<>(v, timeTo.get(v));
                        pq.remove(oldEntry); // Tenta remover a entrada antiga (pode não existir se foi a primeira vez)
                        pq.add(new AbstractMap.SimpleEntry<>(v, newTime)); // Adiciona a nova entrada com a prioridade atualizada
                    }
                }
            }
        }

        // Se não houver caminho
        if (timeTo.get(idChegada) == Double.POSITIVE_INFINITY) {
            return null; // Ou retorna um RailwayPath vazio
        }

        // Reconstruir o caminho
        List<LineSegment> path = new ArrayList<>();
        double totalDistance = 0;
        int curr = idChegada;
        while (curr != idPartida) {
            // Tratamento de erro caso predecessor não seja encontrado (ciclo ou erro no algoritmo)
            if (!predecessorNode.containsKey(curr)) {
                System.err.println("Erro ao reconstruir o caminho: Nó predecessor não encontrado para " + curr);
                return null; // Não foi possível reconstruir o caminho
            }
            LineSegment seg = edgeTo.get(curr);
            if (seg == null) {
                System.err.println("Erro ao reconstruir o caminho: Segmento não encontrado para chegar a " + curr);
                return null; // Segmento associado está em falta
            }
            path.add(seg);
            totalDistance += seg.getComprimento();
            curr = predecessorNode.get(curr); // Move-se para o nó anterior
        }
        Collections.reverse(path); // Coloca na ordem Partida -> Chegada

        return new RailwayPath(path, totalDistance, timeTo.get(idChegada));
    }
}
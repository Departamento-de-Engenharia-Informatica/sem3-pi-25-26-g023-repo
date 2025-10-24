package pt.ipp.isep.dei.controller;

import pt.ipp.isep.dei.domain.Estacao;
import pt.ipp.isep.dei.domain.Locomotiva; // Mantido para consistência
import pt.ipp.isep.dei.domain.RailwayNetworkService;
import pt.ipp.isep.dei.domain.RailwayPath;
import pt.ipp.isep.dei.domain.SegmentoLinha;
import pt.ipp.isep.dei.repository.EstacaoRepository;
import pt.ipp.isep.dei.repository.LocomotivaRepository;
import pt.ipp.isep.dei.repository.SegmentoLinhaRepository;

import java.util.*; // Importado para List, Set, Map, etc.
import java.util.stream.Collectors; // Para streams (já estava)

public class TravelTimeController {

    private final EstacaoRepository estacaoRepo;
    private final LocomotivaRepository locomotivaRepo;
    private final RailwayNetworkService networkService;
    private final SegmentoLinhaRepository segmentoRepo; // Dependência adicionada

    // Construtor modificado para incluir SegmentoLinhaRepository
    public TravelTimeController(EstacaoRepository estacaoRepo,
                                LocomotivaRepository locomotivaRepo,
                                RailwayNetworkService networkService,
                                SegmentoLinhaRepository segmentoRepo) { // Adicionado segmentoRepo
        this.estacaoRepo = estacaoRepo;
        this.locomotivaRepo = locomotivaRepo;
        this.networkService = networkService;
        this.segmentoRepo = segmentoRepo; // Atribuição adicionada
    }


    /**
     * Calcula o tempo de viagem (caminho mais rápido) para a USLP03.
     * Retorna uma String com o resultado formatado ou uma mensagem de erro.
     */
    public String calculateTravelTime(int idEstacaoPartida, int idEstacaoChegada, int idLocomotiva) {
        // 1. Validar Entradas (IDs existem?)
        Optional<Estacao> optPartida = estacaoRepo.findById(idEstacaoPartida); //
        Optional<Estacao> optChegada = estacaoRepo.findById(idEstacaoChegada); //
        Optional<Locomotiva> optLocomotiva = locomotivaRepo.findById(idLocomotiva); //

        if (optPartida.isEmpty()) {
            return String.format("❌ ERRO: Estação de partida com ID %d não encontrada.", idEstacaoPartida);
        }
        if (optChegada.isEmpty()) {
            return String.format("❌ ERRO: Estação de chegada com ID %d não encontrada.", idEstacaoChegada);
        }
        if (optLocomotiva.isEmpty()) {
            return String.format("❌ ERRO: Locomotiva com ID %d não encontrada.", idLocomotiva);
        }
        if (idEstacaoPartida == idEstacaoChegada) {
            return "❌ ERRO: Estação de partida e chegada são a mesma.";
        }

        // 2. Chamar o serviço para encontrar o caminho mais rápido
        RailwayPath path = networkService.findFastestPath(idEstacaoPartida, idEstacaoChegada); //

        if (path == null || path.isEmpty()) { //
            return String.format("❌ ERRO: Não foi encontrado um caminho ferroviário entre %s e %s.",
                    optPartida.get().getNome(), optChegada.get().getNome()); //
        }

        // 3. Formatar o resultado
        return formatPathResult(path, optPartida.get(), optChegada.get(), optLocomotiva.get());
    }

    /**
     * Retorna informações formatadas sobre as estações diretamente conectadas à estação de partida.
     * @deprecated Use getDirectlyConnectedStations para obter a lista e formate na UI. Mantido para compatibilidade se necessário.
     */
    public String getDirectConnectionsInfo(int idEstacaoPartida) {
        // 1. Validar Estação de Partida
        Optional<Estacao> optPartida = estacaoRepo.findById(idEstacaoPartida); //
        if (optPartida.isEmpty()) {
            return String.format("❌ ERRO: Estação de partida com ID %d não encontrada.", idEstacaoPartida);
        }
        Estacao partida = optPartida.get();

        // 2. Obter estações conectadas usando o novo método
        List<Estacao> reachableStations = getDirectlyConnectedStations(idEstacaoPartida);

        // 3. Formatar a Saída
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("📍 Ponto de Partida: %s (ID: %d)\n", partida.getNome(), partida.getIdEstacao())); //
        sb.append("\n" + "-".repeat(40) + "\n");
        sb.append("🎯 Destinos Diretamente Alcançáveis:\n");

        if (reachableStations.isEmpty()) {
            sb.append("   Nenhuma estação diretamente conectada encontrada.\n");
        } else {
            for (Estacao destino : reachableStations) {
                // Calcular e mostrar a distância/tempo do segmento direto
                Optional<SegmentoLinha> directSegment = segmentoRepo.findDirectSegment(idEstacaoPartida, destino.getIdEstacao()); //
                if (directSegment.isPresent()) {
                    SegmentoLinha seg = directSegment.get();
                    double tempoHoras = (seg.getVelocidadeMaxima() > 0) ? (seg.getComprimento() / seg.getVelocidadeMaxima()) : Double.POSITIVE_INFINITY; //
                    long tempoMinutos = Math.round(tempoHoras * 60);
                    sb.append(String.format("   -> %s (ID: %d) | Dist: %.2f km | Tempo Est: ~%d min\n",
                            destino.getNome(), destino.getIdEstacao(), seg.getComprimento(), tempoMinutos)); //
                } else {
                    sb.append(String.format("   -> %s (ID: %d)\n", destino.getNome(), destino.getIdEstacao())); //
                }
            }
        }
        sb.append("-".repeat(40) + "\n");

        return sb.toString();
    }


    /**
     * Retorna uma lista de Estações diretamente conectadas à estação de partida.
     * @param idEstacaoPartida O ID da estação de partida.
     * @return Uma lista de objetos Estacao que são diretamente alcançáveis, ou uma lista vazia se nenhuma for encontrada ou a partida for inválida.
     */
    public List<Estacao> getDirectlyConnectedStations(int idEstacaoPartida) {
        // 1. Validar Estação de Partida
        if (estacaoRepo.findById(idEstacaoPartida).isEmpty()) { //
            System.err.printf("❌ ERRO: Estação de partida com ID %d não encontrada ao buscar conexões.\n", idEstacaoPartida);
            return Collections.emptyList(); // Retorna lista vazia se a partida não existe
        }

        // 2. Encontrar Segmentos Conectados
        List<SegmentoLinha> todosSegmentos = segmentoRepo.findAll(); //
        Set<Integer> reachableStationIds = new HashSet<>();

        for (SegmentoLinha seg : todosSegmentos) {
            int vizinhoId = -1;
            if (seg.getIdEstacaoInicio() == idEstacaoPartida) { //
                vizinhoId = seg.getIdEstacaoFim(); //
            } else if (seg.getIdEstacaoFim() == idEstacaoPartida) { //
                vizinhoId = seg.getIdEstacaoInicio(); //
            }

            if (vizinhoId != -1) {
                reachableStationIds.add(vizinhoId);
            }
        }

        // 3. Buscar Objetos Estacao
        List<Estacao> reachableStations = new ArrayList<>();
        for (int id : reachableStationIds) {
            estacaoRepo.findById(id).ifPresent(reachableStations::add); //
        }
        reachableStations.sort(Comparator.comparingInt(Estacao::getIdEstacao)); // Ordenar por ID //

        return reachableStations;
    }


    /**
     * Método auxiliar para formatar o relatório de saída do caminho mais rápido.
     */
    private String formatPathResult(RailwayPath path, Estacao partida, Estacao chegada, Locomotiva locomotiva) {
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("Resultados para a viagem de %s para %s:%n", partida.getNome(), chegada.getNome())); //
        sb.append(String.format("   (Locomotiva selecionada: ID %d - %s)%n",
                locomotiva.getIdLocomotiva(), locomotiva.getModelo())); //
        sb.append("-".repeat(50) + "\n");
        sb.append("   Caminho mais rápido encontrado (por segmentos):\n");

        int i = 1;
        int estacaoAnteriorId = partida.getIdEstacao(); //

        for (SegmentoLinha seg : path.getSegments()) { //
            // Descobre a ordem correta das estações para este segmento
            int estacaoInicioSeg = seg.getIdEstacaoInicio(); //
            int estacaoFimSeg = seg.getIdEstacaoFim(); //

            String nomeInicio, nomeFim;

            // Garante que a ordem de impressão segue o caminho
            if (estacaoInicioSeg == estacaoAnteriorId) {
                nomeInicio = estacaoRepo.findById(estacaoInicioSeg).map(Estacao::getNome).orElse("ID "+estacaoInicioSeg); //
                nomeFim = estacaoRepo.findById(estacaoFimSeg).map(Estacao::getNome).orElse("ID "+estacaoFimSeg); //
                estacaoAnteriorId = estacaoFimSeg; // Atualiza para o próximo loop
            } else {
                nomeInicio = estacaoRepo.findById(estacaoFimSeg).map(Estacao::getNome).orElse("ID "+estacaoFimSeg); //
                nomeFim = estacaoRepo.findById(estacaoInicioSeg).map(Estacao::getNome).orElse("ID "+estacaoInicioSeg); //
                estacaoAnteriorId = estacaoInicioSeg; // Atualiza para o próximo loop
            }

            double tempoSeg = Double.POSITIVE_INFINITY;
            if(seg.getVelocidadeMaxima() > 0) { // Evita divisão por zero //
                tempoSeg = (seg.getComprimento() / seg.getVelocidadeMaxima()) * 60; // em minutos //
            }


            sb.append(String.format("   Secção %d: %s -> %s\n", i++, nomeInicio, nomeFim));
            sb.append(String.format("      Dist: %.2f km | Vel: %.1f km/h | Tempo: %.1f min\n",
                    seg.getComprimento(), seg.getVelocidadeMaxima(), tempoSeg)); //
        }

        sb.append("-".repeat(50) + "\n");

        long totalMinutos = path.getTotalTimeMinutes(); //
        long horas = totalMinutos / 60;
        long minutos = totalMinutos % 60;
        String tempoFormatado = (horas > 0) ? String.format("%d horas e %d minutos", horas, minutos) : String.format("%d minutos", minutos);

        sb.append(String.format("   Distância Total: %.2f km\n", path.getTotalDistance())); //
        sb.append(String.format("   Tempo Total Estimado: %s (%.2f Horas)\n", tempoFormatado, path.getTotalTimeHours())); //

        return sb.toString();
    }
}
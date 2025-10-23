package pt.ipp.isep.dei.controller;

import pt.ipp.isep.dei.domain.Estacao;
import pt.ipp.isep.dei.domain.Locomotiva;
import pt.ipp.isep.dei.domain.RailwayNetworkService; // NOVO
import pt.ipp.isep.dei.domain.RailwayPath; // NOVO
import pt.ipp.isep.dei.domain.SegmentoLinha;
import pt.ipp.isep.dei.repository.EstacaoRepository;
import pt.ipp.isep.dei.repository.LocomotivaRepository;
import pt.ipp.isep.dei.repository.SegmentoLinhaRepository;

import java.util.Optional;

public class TravelTimeController {

    private final EstacaoRepository estacaoRepo;
    private final LocomotivaRepository locomotivaRepo;
    // Removido SegmentoRepo, agora está no serviço
    private final RailwayNetworkService networkService; // NOVO

    // Construtor modificado
    public TravelTimeController(EstacaoRepository estacaoRepo,
                                LocomotivaRepository locomotivaRepo,
                                RailwayNetworkService networkService) { // Modificado
        this.estacaoRepo = estacaoRepo;
        this.locomotivaRepo = locomotivaRepo;
        this.networkService = networkService; // NOVO
    }

    /**
     * Calcula o tempo de viagem para a USLP03 (agora com múltiplos segmentos).
     * Retorna uma String com o resultado formatado ou uma mensagem de erro.
     */
    public String calculateTravelTime(int idEstacaoPartida, int idEstacaoChegada, int idLocomotiva) {

        // 1. Validar Entradas (IDs existem?)
        Optional<Estacao> optPartida = estacaoRepo.findById(idEstacaoPartida);
        Optional<Estacao> optChegada = estacaoRepo.findById(idEstacaoChegada);
        Optional<Locomotiva> optLocomotiva = locomotivaRepo.findById(idLocomotiva);

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
        RailwayPath path = networkService.findFastestPath(idEstacaoPartida, idEstacaoChegada);

        if (path == null || path.isEmpty()) {
            return String.format("❌ ERRO: Não foi encontrado um caminho ferroviário entre %s e %s.",
                    optPartida.get().getNome(), optChegada.get().getNome());
        }

        // 3. Formatar o resultado
        return formatPathResult(path, optPartida.get(), optChegada.get(), optLocomotiva.get());
    }

    /**
     * Método auxiliar para formatar o relatório de saída.
     */
    private String formatPathResult(RailwayPath path, Estacao partida, Estacao chegada, Locomotiva locomotiva) {
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("Resultados para a viagem de %s para %s:%n", partida.getNome(), chegada.getNome()));
        sb.append(String.format("   (Locomotiva selecionada: ID %d - %s)%n",
                locomotiva.getIdLocomotiva(), locomotiva.getModelo()));
        sb.append("-".repeat(50) + "\n");
        sb.append("   Caminho mais rápido encontrado (por segmentos):\n");

        int i = 1;
        int estacaoAnteriorId = partida.getIdEstacao();

        for (SegmentoLinha seg : path.getSegments()) {
            // Descobre a ordem correta das estações para este segmento
            int estacaoInicioSeg = seg.getIdEstacaoInicio();
            int estacaoFimSeg = seg.getIdEstacaoFim();

            String nomeInicio, nomeFim;

            // Garante que a ordem de impressão segue o caminho
            if (estacaoInicioSeg == estacaoAnteriorId) {
                nomeInicio = estacaoRepo.findById(estacaoInicioSeg).map(Estacao::getNome).orElse("ID "+estacaoInicioSeg);
                nomeFim = estacaoRepo.findById(estacaoFimSeg).map(Estacao::getNome).orElse("ID "+estacaoFimSeg);
                estacaoAnteriorId = estacaoFimSeg; // Atualiza para o próximo loop
            } else {
                nomeInicio = estacaoRepo.findById(estacaoFimSeg).map(Estacao::getNome).orElse("ID "+estacaoFimSeg);
                nomeFim = estacaoRepo.findById(estacaoInicioSeg).map(Estacao::getNome).orElse("ID "+estacaoInicioSeg);
                estacaoAnteriorId = estacaoInicioSeg; // Atualiza para o próximo loop
            }

            double tempoSeg = (seg.getComprimento() / seg.getVelocidadeMaxima()) * 60; // em minutos

            sb.append(String.format("   Secção %d: %s -> %s\n", i++, nomeInicio, nomeFim));
            sb.append(String.format("      Dist: %.2f km | Vel: %.1f km/h | Tempo: %.1f min\n",
                    seg.getComprimento(), seg.getVelocidadeMaxima(), tempoSeg));
        }

        sb.append("-".repeat(50) + "\n");

        long totalMinutos = path.getTotalTimeMinutes();
        long horas = totalMinutos / 60;
        long minutos = totalMinutos % 60;
        String tempoFormatado = (horas > 0) ? String.format("%d horas e %d minutos", horas, minutos) : String.format("%d minutos", minutos);

        sb.append(String.format("   Distância Total: %.2f km\n", path.getTotalDistance()));
        sb.append(String.format("   Tempo Total Estimado: %s (%.2f Horas)\n", tempoFormatado, path.getTotalTimeHours()));

        return sb.toString();
    }
}
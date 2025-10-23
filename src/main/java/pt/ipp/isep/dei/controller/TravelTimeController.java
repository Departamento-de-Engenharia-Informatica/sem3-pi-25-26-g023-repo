package pt.ipp.isep.dei.controller;

import pt.ipp.isep.dei.domain.Estacao;
import pt.ipp.isep.dei.domain.Locomotiva;
import pt.ipp.isep.dei.domain.SegmentoLinha;
import pt.ipp.isep.dei.repository.EstacaoRepository;
import pt.ipp.isep.dei.repository.LocomotivaRepository;
import pt.ipp.isep.dei.repository.SegmentoLinhaRepository;

import java.util.Optional;

public class TravelTimeController {

    private final EstacaoRepository estacaoRepo;
    private final LocomotivaRepository locomotivaRepo;
    private final SegmentoLinhaRepository segmentoRepo;

    public TravelTimeController(EstacaoRepository estacaoRepo, LocomotivaRepository locomotivaRepo, SegmentoLinhaRepository segmentoRepo) {
        this.estacaoRepo = estacaoRepo;
        this.locomotivaRepo = locomotivaRepo;
        this.segmentoRepo = segmentoRepo;
    }

    /**
     * Calcula o tempo de viagem para a USLP03.
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

        // 2. Encontrar o Segmento Direto
        Optional<SegmentoLinha> optSegmento = segmentoRepo.findDirectSegment(idEstacaoPartida, idEstacaoChegada);

        if (optSegmento.isEmpty()) {
            return String.format("❌ ERRO: Não existe ligação ferroviária *direta* entre %s e %s.",
                    optPartida.get().getNome(), optChegada.get().getNome());
        }

        // 3. Calcular Tempo (AC: Aceleração instantânea)
        SegmentoLinha segmento = optSegmento.get();
        double distancia = segmento.getComprimento();
        double velocidade = segmento.getVelocidadeMaxima();

        if (velocidade <= 0) {
            return String.format("❌ ERRO: O segmento entre %s e %s tem uma velocidade máxima inválida (%.1f km/h).",
                    optPartida.get().getNome(), optChegada.get().getNome(), velocidade);
        }

        double tempoEmHoras = distancia / velocidade;
        long tempoTotalMinutos = Math.round(tempoEmHoras * 60); // Arredonda para o minuto mais próximo

        // Formata para H:M
        long horas = tempoTotalMinutos / 60;
        long minutos = tempoTotalMinutos % 60;
        String tempoFormatado;
        if (horas > 0) {
            tempoFormatado = String.format("%d horas e %d minutos", horas, minutos);
        } else {
            tempoFormatado = String.format("%d minutos", minutos);
        }

        // 4. Montar Relatório (AC: Listar secções)
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("Resultados para a viagem com Locomotiva %d (%s):%n",
                optLocomotiva.get().getIdLocomotiva(), optLocomotiva.get().getModelo()));
        sb.append("-".repeat(50) + "\n");
        sb.append("Secção 1 (Ligação Direta):\n");
        sb.append(String.format("   De: %s (ID: %d)\n", optPartida.get().getNome(), idEstacaoPartida));
        sb.append(String.format("   Para: %s (ID: %d)\n", optChegada.get().getNome(), idEstacaoChegada));
        sb.append(String.format("   Distância: %.2f km\n", distancia));
        sb.append(String.format("   Velocidade Máxima Permitida: %.1f km/h\n", velocidade));
        sb.append("-".repeat(50) + "\n");
        sb.append(String.format("Tempo de Viagem Estimado (Acel. Instantânea): %s%n", tempoFormatado));

        return sb.toString();
    }
}
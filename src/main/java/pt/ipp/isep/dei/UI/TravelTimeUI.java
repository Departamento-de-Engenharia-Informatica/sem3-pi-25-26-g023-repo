package pt.ipp.isep.dei.UI;

import pt.ipp.isep.dei.controller.TravelTimeController;
import pt.ipp.isep.dei.domain.Estacao;
import pt.ipp.isep.dei.domain.Locomotiva;
import pt.ipp.isep.dei.repository.EstacaoRepository;
import pt.ipp.isep.dei.repository.LocomotivaRepository;

import java.util.*;
import java.util.stream.Collectors;

public class TravelTimeUI implements Runnable {

    private final TravelTimeController controller;
    private final EstacaoRepository estacaoRepo;
    private final LocomotivaRepository locomotivaRepo;

    public TravelTimeUI(TravelTimeController controller, EstacaoRepository estacaoRepo, LocomotivaRepository locomotivaRepo) {
        this.controller = controller;
        this.estacaoRepo = estacaoRepo;
        this.locomotivaRepo = locomotivaRepo;
    }

    @Override
    public void run() {
        Scanner scanner = new Scanner(System.in);
        System.out.println("\n" + "=".repeat(50));
        System.out.println("     USLP03 - Calcular Tempo de Viagem (Caminho Rápido)"); // Título ajustado
        System.out.println("=".repeat(50));

        try {
            // 1. Listar e pedir Estação de Partida
            System.out.println("\n--- Estações Disponíveis ---");
            List<Estacao> estacoes = estacaoRepo.findAll();
            if (estacoes.isEmpty()) {
                System.out.println("❌ ERRO: Não há estações carregadas.");
                return;
            }
            estacoes.forEach(System.out::println);
            System.out.print("➡️  Insira o ID da Estação de PARTIDA: ");
            int idPartida = scanner.nextInt();
            scanner.nextLine(); // Consumir newline

            // 1.1 Validar se estação de partida existe
            if (estacaoRepo.findById(idPartida).isEmpty()) {
                System.out.printf("❌ ERRO: Estação com ID %d não encontrada.\n", idPartida);
                return;
            }

            // 2. Listar Destinos Possíveis (Conexões Diretas)
            System.out.println("\n--- Destinos Diretamente Conectados ---");
            List<Estacao> destinosPossiveis = controller.getDirectlyConnectedStations(idPartida); // Chama o novo método do controller

            if (destinosPossiveis.isEmpty()) {
                System.out.println("   Nenhuma estação diretamente conectada encontrada a partir desta origem.");
                return;
            }

            final Set<Integer> idsDestinosValidos = new HashSet<>(); // Para validação rápida
            destinosPossiveis.forEach(est -> {
                System.out.println(est); // Mostra "ID: X - Nome"
                idsDestinosValidos.add(est.getIdEstacao());
            });

            // 3. Pedir Estação de Chegada (validando contra a lista)
            int idChegada = -1;
            boolean destinoValido = false;
            while (!destinoValido) {
                System.out.print("\n➡️  Insira o ID da Estação de CHEGADA (da lista acima): ");
                try {
                    idChegada = scanner.nextInt();
                    scanner.nextLine(); // Consumir newline

                    if (idChegada == idPartida) {
                        System.out.println("❌ ERRO: A estação de chegada não pode ser igual à de partida.");
                    } else if (idsDestinosValidos.contains(idChegada)) {
                        destinoValido = true;
                    } else {
                        System.out.println("❌ ERRO: ID inválido. Escolha um ID da lista de destinos conectados.");
                    }
                } catch (InputMismatchException e) {
                    System.out.println("❌ ERRO: Input inválido. Por favor, insira apenas números.");
                    scanner.nextLine(); // Limpar buffer
                }
            }


            // 4. Listar e pedir Locomotiva (como antes)
            System.out.println("\n--- Locomotivas Disponíveis ---");
            List<Locomotiva> locomotivas = locomotivaRepo.findAll();
            if (locomotivas.isEmpty()) {
                System.out.println("❌ ERRO: Não há locomotivas carregadas.");
                return;
            }
            locomotivas.forEach(System.out::println);
            System.out.print("➡️  Insira o ID da Locomotiva selecionada: ");
            int idLocomotiva = scanner.nextInt();
            scanner.nextLine(); // Consumir newline

            // 5. Chamar Controller (método original) e Imprimir Resultado
            System.out.println("\n⚙️  A calcular o caminho mais rápido...");
            String resultado = controller.calculateTravelTime(idPartida, idChegada, idLocomotiva); // Chama o método original

            System.out.println("\n" + "=".repeat(50));
            System.out.println("              Resultado do Cálculo");
            System.out.println("=".repeat(50));
            System.out.println(resultado); // Imprime o relatório formatado ou a mensagem de erro
            System.out.println("=".repeat(50));


        } catch (InputMismatchException e) {
            System.out.println("\n❌ ERRO: Input inválido. Por favor, insira apenas números.");
            scanner.nextLine(); // Limpar o buffer do scanner
        } catch (Exception e) {
            System.out.println("\n❌ ERRO inesperado ao executar USLP03: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
package pt.ipp.isep.dei.UI;

import pt.ipp.isep.dei.controller.TravelTimeController;
import pt.ipp.isep.dei.domain.Estacao;
import pt.ipp.isep.dei.domain.Locomotiva;
import pt.ipp.isep.dei.repository.EstacaoRepository;
import pt.ipp.isep.dei.repository.LocomotivaRepository;

import java.util.InputMismatchException;
import java.util.List;
import java.util.Scanner;

public class TravelTimeUI implements Runnable {

    private final TravelTimeController controller;
    private final EstacaoRepository estacaoRepo; // Para listar
    private final LocomotivaRepository locomotivaRepo; // Para listar

    public TravelTimeUI(TravelTimeController controller, EstacaoRepository estacaoRepo, LocomotivaRepository locomotivaRepo) {
        this.controller = controller;
        this.estacaoRepo = estacaoRepo;
        this.locomotivaRepo = locomotivaRepo;
    }

    @Override
    public void run() {
        Scanner scanner = new Scanner(System.in);
        System.out.println("\n" + "=".repeat(50));
        System.out.println("     USLP03 - Calcular Tempo de Viagem Direta");
        System.out.println("=".repeat(50));

        try {
            // 1. Listar e pedir Estação de Partida
            System.out.println("\n--- Estações Disponíveis ---");
            List<Estacao> estacoes = estacaoRepo.findAll();
            if (estacoes.isEmpty()) {
                System.out.println("❌ ERRO: Não há estações carregadas (verificar Repositório Mock).");
                return;
            }
            estacoes.forEach(System.out::println);
            System.out.print("➡️  Insira o ID da Estação de PARTIDA: ");
            int idPartida = scanner.nextInt();

            // 2. Listar e pedir Estação de Chegada
            System.out.print("➡️  Insira o ID da Estação de CHEGADA: ");
            int idChegada = scanner.nextInt();

            // 3. Listar e pedir Locomotiva
            System.out.println("\n--- Locomotivas Disponíveis ---");
            List<Locomotiva> locomotivas = locomotivaRepo.findAll();
            if (locomotivas.isEmpty()) {
                System.out.println("❌ ERRO: Não há locomotivas carregadas (verificar Repositório Mock).");
                return;
            }
            locomotivas.forEach(System.out::println);
            System.out.print("➡️  Insira o ID da Locomotiva selecionada: ");
            int idLocomotiva = scanner.nextInt();

            // 4. Chamar Controller e Imprimir Resultado
            System.out.println("\n⚙️  A calcular...");
            String resultado = controller.calculateTravelTime(idPartida, idChegada, idLocomotiva);

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
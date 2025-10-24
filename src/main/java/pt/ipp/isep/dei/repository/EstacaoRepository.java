package pt.ipp.isep.dei.repository;

import pt.ipp.isep.dei.domain.Estacao;
import java.util.*;
import java.util.stream.Collectors;

public class EstacaoRepository {
    private final List<Estacao> mockEstacoes = new ArrayList<>();

    public EstacaoRepository() {
        // Dados extraídos de image_19a499.png
        Map<Integer, String> estacoesMap = new HashMap<>();
        // Linha 1
        estacoesMap.put(7, "Porto São Bento");
        estacoesMap.put(5, "Porto Campanhã");
        // Linha 2
        estacoesMap.put(5, "Porto Campanhã");
        estacoesMap.put(20, "Nine");
        // Linha 3
        estacoesMap.put(20, "Nine");
        estacoesMap.put(8, "Barcelos");
        // Linha 4
        estacoesMap.put(8, "Barcelos");
        estacoesMap.put(17, "Viana do Castelo");
        // Linha 5
        estacoesMap.put(17, "Viana do Castelo");
        estacoesMap.put(21, "Caminha");
        // Linha 6
        estacoesMap.put(21, "Caminha");
        estacoesMap.put(16, "São Pedro da Torre");
        // Linha 7
        estacoesMap.put(16, "São Pedro da Torre");
        estacoesMap.put(11, "Valença");

        // Adiciona as estações únicas à lista, ordenadas por ID para consistência
        mockEstacoes.addAll(estacoesMap.entrySet().stream()
                .map(entry -> new Estacao(entry.getKey(), entry.getValue()))
                .sorted(Comparator.comparingInt(Estacao::getIdEstacao))
                .collect(Collectors.toList()));

        System.out.println("EstacaoRepository: Loaded " + mockEstacoes.size() + " unique stations.");
    }

    public List<Estacao> findAll() {
        return new ArrayList<>(mockEstacoes); // Retorna cópia
    }

    public Optional<Estacao> findById(int id) {
        return mockEstacoes.stream().filter(e -> e.getIdEstacao() == id).findFirst();
    }

    public Optional<Estacao> findByNome(String nome) {
        return mockEstacoes.stream().filter(e -> e.getNome().equalsIgnoreCase(nome)).findFirst();
    }
}
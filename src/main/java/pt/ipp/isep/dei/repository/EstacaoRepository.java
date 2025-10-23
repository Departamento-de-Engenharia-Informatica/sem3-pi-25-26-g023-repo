package pt.ipp.isep.dei.repository;

import pt.ipp.isep.dei.domain.Estacao;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Repositório MOCK para simular o acesso à tabela Estacao do BDDAD.
 */
public class EstacaoRepository {
    private final List<Estacao> mockEstacoes = new ArrayList<>();

    public EstacaoRepository() {
        // Dados de exemplo
        mockEstacoes.add(new Estacao(1, "Porto-Campanha"));
        mockEstacoes.add(new Estacao(2, "Lisboa-Oriente"));
        mockEstacoes.add(new Estacao(3, "Coimbra-B"));
        mockEstacoes.add(new Estacao(4, "Faro"));
        mockEstacoes.add(new Estacao(5, "Braga"));
    }

    public List<Estacao> findAll() {
        return new ArrayList<>(mockEstacoes);
    }

    public Optional<Estacao> findById(int id) {
        return mockEstacoes.stream().filter(e -> e.getIdEstacao() == id).findFirst();
    }

    public Optional<Estacao> findByNome(String nome) {
        return mockEstacoes.stream().filter(e -> e.getNome().equalsIgnoreCase(nome)).findFirst();
    }
}
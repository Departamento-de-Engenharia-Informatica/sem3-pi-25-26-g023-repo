package pt.ipp.isep.dei.repository;

import pt.ipp.isep.dei.domain.Locomotiva;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Repositório MOCK para simular o acesso à tabela Locomotiva do BDDAD.
 */
public class LocomotivaRepository {
    private final List<Locomotiva> mockLocomotivas = new ArrayList<>();

    public LocomotivaRepository() {
        // Dados de exemplo
        mockLocomotivas.add(new Locomotiva(101, "Siemens Vectron", "eletrica"));
        mockLocomotivas.add(new Locomotiva(102, "Stadler Euro 4000", "diesel"));
        mockLocomotivas.add(new Locomotiva(103, "Alstom Prima H4", "hibrida"));
    }

    public List<Locomotiva> findAll() {
        return new ArrayList<>(mockLocomotivas);
    }

    public Optional<Locomotiva> findById(int id) {
        return mockLocomotivas.stream().filter(l -> l.getIdLocomotiva() == id).findFirst();
    }
}
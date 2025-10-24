package pt.ipp.isep.dei.repository;

import pt.ipp.isep.dei.domain.Locomotiva;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Repositório MOCK para simular o acesso à tabela Locomotiva do BDDAD.
 * Dados atualizados com base em image_19a4b6.png.
 */
public class LocomotivaRepository {
    private final List<Locomotiva> mockLocomotivas = new ArrayList<>();

    public LocomotivaRepository() {
        // Dados de exemplo de image_19a4b6.png
        // Coluna "Traction (Nº)" mapeada para tipo: "300 Electric" -> "eletrica", "300 Diesel" -> "diesel"
        mockLocomotivas.add(new Locomotiva(5621, "EuroSprinter", "eletrica")); // 5621	Inês	Siemens	Eurosprinter	1995	...	300 Electric
        mockLocomotivas.add(new Locomotiva(5629, "EuroSprinter", "eletrica")); // 5629	Paz		Siemens	Eurosprinter	1995	...	300 Electric
        mockLocomotivas.add(new Locomotiva(5603, "EuroSprinter", "eletrica")); // 5603	Helena	Siemens	Eurosprinter	1996	...	300 Electric
        mockLocomotivas.add(new Locomotiva(1901, "CP 1900", "diesel"));      // 1901	Elsa	Sorefame	Alsthom-CP 1900	1981	...	300 Diesel
        // Adicione mais locomotivas se necessário, seguindo o padrão da imagem.
        System.out.println("LocomotivaRepository: Loaded " + mockLocomotivas.size() + " locomotives.");

    }

    public List<Locomotiva> findAll() {
        return new ArrayList<>(mockLocomotivas);
    }

    public Optional<Locomotiva> findById(int id) {
        return mockLocomotivas.stream().filter(l -> l.getIdLocomotiva() == id).findFirst();
    }
}
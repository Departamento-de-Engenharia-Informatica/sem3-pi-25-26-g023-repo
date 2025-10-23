package pt.ipp.isep.dei.repository;

import pt.ipp.isep.dei.domain.SegmentoLinha;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class SegmentoLinhaRepository {
    private final List<SegmentoLinha> mockSegmentos = new ArrayList<>();

    public SegmentoLinhaRepository() {
        // Ligação Porto -> Lisboa (ID 1->2)
        mockSegmentos.add(new SegmentoLinha(1001, 1, 2, 312.5, 220.0));
        // Ligação Porto -> Coimbra (ID 1->3)
        mockSegmentos.add(new SegmentoLinha(1002, 1, 3, 116.0, 200.0));
        // Ligação Lisboa -> Coimbra (ID 2->3)
        mockSegmentos.add(new SegmentoLinha(1003, 2, 3, 196.5, 180.0));
        // Ligação Lisboa -> Faro (ID 2->4)
        mockSegmentos.add(new SegmentoLinha(1004, 2, 4, 292.0, 160.0));
        // Ligação Porto -> Braga (ID 1->5)
        mockSegmentos.add(new SegmentoLinha(1005, 1, 5, 53.0, 140.0));
        // Segmento com velocidade 0 para teste de erro
        mockSegmentos.add(new SegmentoLinha(1006, 3, 4, 100.0, 0.0)); // Coimbra -> Faro (com erro)
    }

    public List<SegmentoLinha> findAll() {
        return new ArrayList<>(mockSegmentos); // Retorna cópia
    }

    public Optional<SegmentoLinha> findDirectSegment(int idEstacaoA, int idEstacaoB) {
        for (SegmentoLinha s : mockSegmentos) {
            if ((s.getIdEstacaoInicio() == idEstacaoA && s.getIdEstacaoFim() == idEstacaoB) ||
                    (s.getIdEstacaoInicio() == idEstacaoB && s.getIdEstacaoFim() == idEstacaoA)) {
                return Optional.of(s);
            }
        }
        return Optional.empty();
    }
}
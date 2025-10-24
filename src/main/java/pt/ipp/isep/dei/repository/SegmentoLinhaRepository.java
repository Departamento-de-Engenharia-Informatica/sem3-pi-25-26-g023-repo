package pt.ipp.isep.dei.repository;

import pt.ipp.isep.dei.domain.SegmentoLinha;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class SegmentoLinhaRepository {
    private final List<SegmentoLinha> mockSegmentos = new ArrayList<>();

    public SegmentoLinhaRepository() {
        // Dados de conexões de image_19a499.png
        // Comprimentos calculados a partir da soma em image_19a49d.png (convertido para km)
        // Velocidades máximas atribuídas de forma plausível (ASSUMPTION: Missing data)

        // Linha 1: Ramal São Bento - Campanhã (ID 7 -> 5)
        // image_19a49d.png: Line 1, Order 1 -> Length 2618m
        mockSegmentos.add(new SegmentoLinha(1, 7, 5, 2.618, 120.0)); // ID Segmento, Inicio, Fim, Comprimento(km), VelMax(km/h)

        // Linha 2: Ramal Campanhã - Nine (ID 5 -> 20)
        // image_19a49d.png: Line 2, Order 1 (29003m) + Order 2 (10000m) = 39003m
        mockSegmentos.add(new SegmentoLinha(2, 5, 20, 39.003, 160.0));

        // Linha 3: Ramal Nine - Barcelos (ID 20 -> 8)
        // image_19a49d.png: Line 3, Order 1 (5286m) + Order 2 (6000m) = 11286m
        mockSegmentos.add(new SegmentoLinha(3, 20, 8, 11.286, 140.0));

        // Linha 4: Ramal Barcelos - Viana (ID 8 -> 17)
        // image_19a49d.png: Line 4, Order 1 (10387m) + Order 2 (12000m) + Order 3 (8000m) = 30387m
        mockSegmentos.add(new SegmentoLinha(4, 8, 17, 30.387, 160.0));

        // Linha 5: Ramal Viana - Caminha (ID 17 -> 21)
        // image_19a49d.png: Line 5, Order 1 (6000m) + Order 2 (3000m) + Order 3 (15000m) = 24000m
        mockSegmentos.add(new SegmentoLinha(5, 17, 21, 24.0, 140.0));

        // Linha 6: Ramal Caminha - Torre (ID 21 -> 16)
        // image_19a49d.png: Line 6, Order 1 -> Length 20829m
        mockSegmentos.add(new SegmentoLinha(6, 21, 16, 20.829, 140.0));

        // Linha 7: Ramal Torre - Valença (ID 16 -> 11)
        // image_19a49d.png: Line 7, Order 1 -> Length 4264m
        mockSegmentos.add(new SegmentoLinha(7, 16, 11, 4.264, 120.0));

        // Adicionar ligação inversa para permitir caminhos nos dois sentidos no Dijkstra
        // (O código atual já trata disso verificando inicio==u ou fim==u, mas adicionar explicitamente
        // pode ser mais claro se a lógica mudar ou para outros algoritmos)
        // Se a sua lógica `findFastestPath` já considera bidirecional, pode omitir estes.
        // Vou adicionar por segurança, mas com IDs diferentes.
        mockSegmentos.add(new SegmentoLinha(8, 5, 7, 2.618, 120.0));
        mockSegmentos.add(new SegmentoLinha(9, 20, 5, 39.003, 160.0));
        mockSegmentos.add(new SegmentoLinha(10, 8, 20, 11.286, 140.0));
        mockSegmentos.add(new SegmentoLinha(11, 17, 8, 30.387, 160.0));
        mockSegmentos.add(new SegmentoLinha(12, 21, 17, 24.0, 140.0));
        mockSegmentos.add(new SegmentoLinha(13, 16, 21, 20.829, 140.0));
        mockSegmentos.add(new SegmentoLinha(14, 11, 16, 4.264, 120.0));

        System.out.println("SegmentoLinhaRepository: Loaded " + mockSegmentos.size() + " segments (includes reverse paths).");
        // Nota: O segmento com velocidade 0 foi removido, pois era do mock antigo.
    }

    public List<SegmentoLinha> findAll() {
        return new ArrayList<>(mockSegmentos); // Retorna cópia
    }

    // Método findDirectSegment pode já não ser tão útil se o caminho sempre usa Dijkstra,
    // mas mantemo-lo por enquanto.
    public Optional<SegmentoLinha> findDirectSegment(int idEstacaoA, int idEstacaoB) {
        // Esta procura agora pode encontrar o segmento original OU o seu inverso adicionado.
        for (SegmentoLinha s : mockSegmentos) {
            if ((s.getIdEstacaoInicio() == idEstacaoA && s.getIdEstacaoFim() == idEstacaoB) ||
                    (s.getIdEstacaoInicio() == idEstacaoB && s.getIdEstacaoFim() == idEstacaoA)) {
                return Optional.of(s);
            }
        }
        return Optional.empty();
    }
}
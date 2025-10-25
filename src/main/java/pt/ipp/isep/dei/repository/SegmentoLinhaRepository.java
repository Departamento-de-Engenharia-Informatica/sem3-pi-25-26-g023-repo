package pt.ipp.isep.dei.repository;

import pt.ipp.isep.dei.DatabaseConnection.DatabaseConnection; // Importa conexão
import pt.ipp.isep.dei.domain.SegmentoLinha;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class SegmentoLinhaRepository {

    // Velocidade padrão em km/h (já que não existe na BD por segmento/linha)
    private static final double DEFAULT_MAX_SPEED = 150.0;
    // Constante para gerar IDs únicos para segmentos inversos
    private static final int INVERSE_ID_OFFSET = 1000;

    // Construtor vazio
    public SegmentoLinhaRepository() {
        System.out.println("SegmentoLinhaRepository: Initialized (will connect to DB on demand).");
    }

    /**
     * Busca todas as linhas da tabela RAILWAY_LINE, calcula o comprimento total
     * a partir de LINE_SEGMENT, e cria objetos SegmentoLinha para AMBOS os sentidos,
     * imitando a estrutura do mock anterior.
     *
     * @return Lista de objetos SegmentoLinha (incluindo segmentos inversos).
     */
    public List<SegmentoLinha> findAll() {
        List<SegmentoLinha> segmentos = new ArrayList<>();
        Map<Integer, Double> lineLengthsKm = calculateAllLineLengthsKm(); // Calcula comprimentos primeiro

        String sql = "SELECT line_id, start_facility_id, end_facility_id FROM RAILWAY_LINE ORDER BY line_id"; //

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                int lineId = rs.getInt("line_id");
                int startFacilityId = rs.getInt("start_facility_id");
                int endFacilityId = rs.getInt("end_facility_id");

                double comprimentoKm = lineLengthsKm.getOrDefault(lineId, 0.0);
                if (comprimentoKm <= 0.0) {
                    System.err.println("⚠️ Aviso: Comprimento inválido (<=0) para line_id " + lineId + ". Segmento pode não funcionar corretamente.");
                    // Poderia optar por não adicionar o segmento se o comprimento for inválido
                    // continue;
                }

                // Cria o segmento no sentido original (A -> B)
                // Usando line_id como idSegmento
                segmentos.add(new SegmentoLinha(lineId, startFacilityId, endFacilityId, comprimentoKm, DEFAULT_MAX_SPEED)); //

                // Cria o segmento no sentido inverso (B -> A)
                // Usando line_id + OFFSET como idSegmento para garantir unicidade
                segmentos.add(new SegmentoLinha(lineId + INVERSE_ID_OFFSET, endFacilityId, startFacilityId, comprimentoKm, DEFAULT_MAX_SPEED)); //

            }
        } catch (SQLException e) {
            System.err.println("❌ Erro ao buscar Linhas (Railway Lines) da BD para criar segmentos: " + e.getMessage());
        }
        System.out.println("SegmentoLinhaRepository: Generated " + segmentos.size() + " segments (includes reverse paths) from DB.");
        return segmentos;
    }

    /**
     * Busca um "Segmento" direto entre duas Facilidades (Estações).
     * Procura uma RAILWAY_LINE que conecte essas duas facilidades diretamente
     * e retorna o objeto SegmentoLinha correspondente (no sentido A->B encontrado na BD).
     *
     * @param idEstacaoA ID da primeira facility.
     * @param idEstacaoB ID da segunda facility.
     * @return Optional contendo o SegmentoLinha (representando a linha no sentido encontrado) se existir.
     */
    public Optional<SegmentoLinha> findDirectSegment(int idEstacaoA, int idEstacaoB) {
        // Recalcula o comprimento especificamente para esta linha para garantir que está atualizado
        Map<Integer, Double> lineLengthsKm = calculateAllLineLengthsKm();
        SegmentoLinha segmento = null;

        String sql = "SELECT line_id, start_facility_id, end_facility_id FROM RAILWAY_LINE " + //
                "WHERE (start_facility_id = ? AND end_facility_id = ?) OR (start_facility_id = ? AND end_facility_id = ?)";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, idEstacaoA);
            stmt.setInt(2, idEstacaoB);
            stmt.setInt(3, idEstacaoB); // Para a condição OR (sentido inverso)
            stmt.setInt(4, idEstacaoA);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    int lineId = rs.getInt("line_id");
                    int startFacilityId = rs.getInt("start_facility_id");
                    int endFacilityId = rs.getInt("end_facility_id");
                    double comprimentoKm = lineLengthsKm.getOrDefault(lineId, 0.0);
                    if (comprimentoKm <= 0.0) {
                        System.err.println("⚠️ Aviso: Comprimento inválido (<=0) para line_id " + lineId + " ao buscar segmento direto.");
                    }
                    // Retorna o segmento na direção encontrada na BD (start -> end)
                    // Usa line_id como idSegmento
                    segmento = new SegmentoLinha(lineId, startFacilityId, endFacilityId, comprimentoKm, DEFAULT_MAX_SPEED); //
                }
            }
        } catch (SQLException e) {
            System.err.println("❌ Erro ao buscar Segmento direto entre " + idEstacaoA + " e " + idEstacaoB + ": " + e.getMessage());
        }
        return Optional.ofNullable(segmento);
    }

    /**
     * Método auxiliar para calcular o comprimento total (em KM) para cada linha
     * a partir da tabela LINE_SEGMENT.
     * @return Map onde a chave é line_id e o valor é o comprimento total em KM.
     */
    private Map<Integer, Double> calculateAllLineLengthsKm() {
        Map<Integer, Double> lengths = new HashMap<>();
        String sql = "SELECT line_id, SUM(length_m) as total_length_m FROM LINE_SEGMENT GROUP BY line_id"; //

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                int lineId = rs.getInt("line_id");
                double totalLengthMeters = rs.getDouble("total_length_m");
                lengths.put(lineId, totalLengthMeters / 1000.0); // Converte para KM
            }
        } catch (SQLException e) {
            System.err.println("❌ Erro ao calcular comprimentos totais das linhas (LINE_SEGMENT): " + e.getMessage());
            // Retorna um mapa vazio em caso de erro para evitar NullPointerException
            return new HashMap<>();
        }
        return lengths;
    }
}
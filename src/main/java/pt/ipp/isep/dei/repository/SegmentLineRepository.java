// File: pt.ipp.isep.dei.repository.SegmentLineRepository.java
package pt.ipp.isep.dei.repository;

import pt.ipp.isep.dei.DatabaseConnection.DatabaseConnection;
import pt.ipp.isep.dei.domain.LineSegment;

import java.sql.*;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Repository class responsible for loading and managing {@link LineSegment} entities.
 * CRITICAL: Agora carrega os dados diretamente da tabela LINE_SEGMENT no Oracle DB.
 */
public class SegmentLineRepository {

    // Constante para prefixar IDs do segmento inverso
    private static final String INVERSE_ID_PREFIX = "INV_";
    private static final double GENERIC_MAX_SPEED_KMH = 150.0; // Velocidade máxima genérica (se não for fornecida)

    /** Cache interna para facilitar a pesquisa por ID (String). */
    private final Map<String, LineSegment> segmentCache = new HashMap<>();

    // 🚨 MOCK TEMPORÁRIO PARA LIGAÇÃO FACILITY <-> SEGMENTO 🚨
    // ISTO É CRÍTICO para construir o grafo, pois LINE_SEGMENT na DB não tem os IDs de Facility de início/fim.
    // Segmento ID (INT) -> {Start Facility ID (INT), End Facility ID (INT)}
    private final Map<Integer, Integer[]> segmentFacilityMapping = new HashMap<>();


    public SegmentLineRepository() {
        System.out.println("SegmentLineRepository: Initialized (Loading data from DB).");
        loadSegmentsFromDatabase();
    }

    /**
     * Popula o mapeamento de Facilities manualmente, com base no DML e no contexto do projeto.
     */
    private void populateFacilityMapping() {
        // --- Linha Principal/Ramais ---
        segmentFacilityMapping.put(1, new Integer[]{7, 5});     // L001 Porto São Bento (7) -> Porto Campanhã (5)
        segmentFacilityMapping.put(3, new Integer[]{5, 13});     // L002 Porto Campanhã (5) -> Contumil (13)
        segmentFacilityMapping.put(30, new Integer[]{13, 43});    // L010 Contumil (13) -> São Gemil (43)
        segmentFacilityMapping.put(31, new Integer[]{43, 45});    // L011 São Gemil (43) -> São Mamede (45)
        segmentFacilityMapping.put(33, new Integer[]{45, 48});    // L012 São Mamede (45) -> Leça do Balio (48)
        segmentFacilityMapping.put(35, new Integer[]{48, 50});    // L013 Leça do Balio (48) -> Leixões (50)

        // --- Linha Minho (Contumil (13) -> Valença (11)) ---
        segmentFacilityMapping.put(10, new Integer[]{13, 20});    // L003 Contumil (13) -> Nine (20)
        segmentFacilityMapping.put(15, new Integer[]{20, 8});     // L004 Nine (20) -> Barcelos (8)
        segmentFacilityMapping.put(14, new Integer[]{8, 12});     // L005 Barcelos (8) -> Darque (12)
        segmentFacilityMapping.put(20, new Integer[]{12, 17});    // L006 Darque (12) -> Viana (17)
        segmentFacilityMapping.put(18, new Integer[]{17, 21});    // L007 Viana (17) -> Caminha (21)
        segmentFacilityMapping.put(25, new Integer[]{21, 16});    // L008 Caminha (21) -> São Pedro da Torre (16)
        segmentFacilityMapping.put(26, new Integer[]{16, 11});    // L009 São Pedro da Torre (16) -> Valença (11)

        // --- Troços internos ou alternativos (para garantir que o Dijkstra tem caminhos) ---
        segmentFacilityMapping.put(11, new Integer[]{20, 18}); // Nine -> Famalicão (ST018)
        segmentFacilityMapping.put(16, new Integer[]{18, 8});  // Famalicão -> Barcelos (ST008)
        segmentFacilityMapping.put(12, new Integer[]{12, 17}); // Outro troço Darque -> Viana
        segmentFacilityMapping.put(13, new Integer[]{17, 12}); // Viana -> Darque
        segmentFacilityMapping.put(21, new Integer[]{21, 16}); // Outro troço Caminha -> Torre
        segmentFacilityMapping.put(22, new Integer[]{16, 11}); // Outro troço Torre -> Valença
    }


    private void loadSegmentsFromDatabase() {

        populateFacilityMapping();

        // SQL para obter todos os dados da tabela LINE_SEGMENT.
        String sql = "SELECT segment_id, length_m, number_tracks, siding_position, siding_length " +
                "FROM LINE_SEGMENT ORDER BY segment_id";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            int segmentsCount = 0;

            while (rs.next()) {
                // 1. DADOS OBTIDOS DA DB
                int segmentIdInt = rs.getInt("segment_id");
                String segmentId = String.valueOf(segmentIdInt);
                double lengthM = rs.getDouble("length_m");
                int numberTracks = rs.getInt("number_tracks");

                Integer sidingPosition = rs.getInt("siding_position");
                if (rs.wasNull()) sidingPosition = null;
                Double sidingLength = rs.getDouble("siding_length");
                if (rs.wasNull()) sidingLength = null;

                // 2. DADOS OBTIDOS DO MOCK TEMPORÁRIO (Facility IDs)
                Integer[] facilities = segmentFacilityMapping.get(segmentIdInt);

                if (facilities == null || facilities.length < 2) {
                    continue;
                }

                int startFacilityId = facilities[0];
                int endFacilityId = facilities[1];
                double lengthKm = lengthM / 1000.0; // Converte metros para KM (formato do construtor)

                // 3. CRIAÇÃO DOS SEGMENTOS (IDA e VOLTA)

                // Segmento 1: A -> B (ID original)
                LineSegment segAB = new LineSegment(
                        segmentId,
                        startFacilityId,
                        endFacilityId,
                        lengthKm,
                        GENERIC_MAX_SPEED_KMH,
                        numberTracks,
                        sidingPosition,
                        sidingLength);
                segmentCache.put(segmentId, segAB);
                segmentsCount++;

                // Segmento 2: B -> A (Inverso - Criado manualmente para grafo bidirecional)
                String inverseId = INVERSE_ID_PREFIX + segmentId;
                LineSegment segBA = new LineSegment(
                        inverseId,
                        endFacilityId,
                        startFacilityId,
                        lengthKm,
                        GENERIC_MAX_SPEED_KMH,
                        numberTracks,
                        sidingPosition,
                        sidingLength);
                segmentCache.put(inverseId, segBA);
                segmentsCount++;
            }

            System.out.println("SegmentLineRepository: Successfully loaded " + segmentsCount + " segments (including inverses) from DB/Mapping.");


        } catch (SQLException e) {
            System.err.println("SegmentLineRepository: ❌ FATAL ERROR loading segments from DB. Check DDL or connection: " + e.getMessage());
        }
    }


    public List<LineSegment> findByIds(List<String> segmentIds) {
        return segmentIds.stream()
                .filter(segmentCache::containsKey)
                .map(segmentCache::get)
                .collect(Collectors.toList());
    }

    public Optional<LineSegment> findById(String id) {
        return Optional.ofNullable(segmentCache.get(id));
    }


    /**
     * Finds a direct line segment connecting two facilities na cache.
     */
    public Optional<LineSegment> findDirectSegment(int stationAId, int stationBId) {
        // Procura na cache por QUALQUER segmento que ligue A -> B (o primeiro encontrado é o caminho direto)
        return segmentCache.values().stream()
                .filter(s -> (s.getIdEstacaoInicio() == stationAId && s.getIdEstacaoFim() == stationBId))
                .findFirst();
    }

    public List<LineSegment> findAll() { return new ArrayList<>(segmentCache.values()); }

    private Map<Integer, Double> calculateAllLineLengthsKm() {
        return new HashMap<>();
    }
}
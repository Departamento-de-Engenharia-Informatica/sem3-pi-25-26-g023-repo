package pt.ipp.isep.dei.repository;

import pt.ipp.isep.dei.DatabaseConnection.DatabaseConnection;
import pt.ipp.isep.dei.domain.LineSegment;

import java.sql.*;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Repository class responsible for loading and managing {@link LineSegment} entities.
 * Uses a hardcoded mapping to link segments to facilities since the DB table lacks FKs.
 */
public class SegmentLineRepository {

    public static final String INVERSE_ID_PREFIX = "INV_";
    private static final double GENERIC_MAX_SPEED_KMH = 150.0;

    private final Map<String, LineSegment> segmentCache = new HashMap<>();

    // Mapping: Segment ID -> {Start Facility ID, End Facility ID}
    private final Map<Integer, Integer[]> segmentFacilityMapping = new HashMap<>();

    public SegmentLineRepository() {
        System.out.println("SegmentLineRepository: Initialized.");
        loadSegmentsFromDatabase();
    }

    public void cleanDatabaseData() {
        this.segmentCache.clear();
    }

    /**
     * Define a topologia da rede manualmente baseada na descrição das Linhas (Lxxx).
     */
    private void populateFacilityMapping() {
        // L001: S. Bento (7) <-> Campanhã (5)
        segmentFacilityMapping.put(1, new Integer[]{7, 5});

        // L002: Campanhã (5) <-> Contumil (13)
        segmentFacilityMapping.put(3, new Integer[]{5, 13});

        // L003: Contumil (13) <-> Nine (20)
        // Mapeamos ambos os segmentos para cobrir o troço
        segmentFacilityMapping.put(10, new Integer[]{13, 20});
        segmentFacilityMapping.put(11, new Integer[]{13, 20});

        // L004: Nine (20) <-> Barcelos (8)
        segmentFacilityMapping.put(15, new Integer[]{20, 8});
        segmentFacilityMapping.put(16, new Integer[]{20, 8});

        // L005: Barcelos (8) <-> Darque (12)
        segmentFacilityMapping.put(14, new Integer[]{8, 12});
        segmentFacilityMapping.put(12, new Integer[]{8, 12});
        segmentFacilityMapping.put(13, new Integer[]{8, 12});

        // L006: Darque (12) <-> Viana (17)
        // Nota: O ID do segmento é 20, cuidado para não confundir com ID da estação Nine
        segmentFacilityMapping.put(20, new Integer[]{12, 17});

        // L007: Viana (17) <-> Caminha (21)
        segmentFacilityMapping.put(18, new Integer[]{17, 21});
        segmentFacilityMapping.put(21, new Integer[]{17, 21});
        segmentFacilityMapping.put(22, new Integer[]{17, 21});

        // L008: Caminha (21) <-> S. Pedro (16)
        segmentFacilityMapping.put(25, new Integer[]{21, 16});

        // L009: S. Pedro (16) <-> Valença (11)
        segmentFacilityMapping.put(26, new Integer[]{16, 11});

        // L010: Contumil (13) <-> S. Gemil (43)
        segmentFacilityMapping.put(30, new Integer[]{13, 43}); // Inverso na definição de rota, mas bidirecional aqui

        // L011: S. Gemil (43) <-> S. Mamede (45)
        segmentFacilityMapping.put(31, new Integer[]{43, 45});
        segmentFacilityMapping.put(32, new Integer[]{43, 45});

        // L012: S. Mamede (45) <-> Leça (48)
        segmentFacilityMapping.put(33, new Integer[]{45, 48});
        segmentFacilityMapping.put(34, new Integer[]{45, 48});

        // L013: Leça (48) <-> Leixões (50)
        segmentFacilityMapping.put(35, new Integer[]{48, 50});
        segmentFacilityMapping.put(36, new Integer[]{48, 50});

        // --- NOVAS LINHAS (USBD32) ---

        // L030: Ramal Braga (Nine -> Braga)
        // Segmento '50' (ID string)
        segmentFacilityMapping.put(50, new Integer[]{20, 30});

        // L031: Nine (20) -> Manzagão (31)
        segmentFacilityMapping.put(51, new Integer[]{20, 31});
        segmentFacilityMapping.put(52, new Integer[]{20, 31});
        segmentFacilityMapping.put(53, new Integer[]{20, 31});
        segmentFacilityMapping.put(54, new Integer[]{20, 31});
        segmentFacilityMapping.put(55, new Integer[]{20, 31});

        // L032: Manzagão (31) -> Cerqueiral (32)
        segmentFacilityMapping.put(58, new Integer[]{31, 32});

        // L035: Cerqueiral (32) -> Gemieira (33)
        segmentFacilityMapping.put(59, new Integer[]{32, 33});

        // L036: Gemieira (33) -> Paredes de Coura (35)
        segmentFacilityMapping.put(60, new Integer[]{33, 35});

        // L037: Paredes de Coura (35) -> Valença (11)
        segmentFacilityMapping.put(61, new Integer[]{35, 11});
    }

    public void save(LineSegment segment) {
        if (segment != null) {
            String idKey = String.valueOf(segment.getIdSegmento());
            this.segmentCache.put(idKey, segment);
        }
    }

    public List<LineSegment> findAll() {
        return new ArrayList<>(segmentCache.values());
    }

    private void loadSegmentsFromDatabase() {
        populateFacilityMapping();

        String sql = "SELECT segment_id, length_m, number_tracks, siding_position, siding_length " +
                "FROM LINE_SEGMENT ORDER BY segment_id";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            int segmentsCount = 0;

            while (rs.next()) {
                int segmentIdInt = rs.getInt("segment_id");
                String segmentId = String.valueOf(segmentIdInt);
                double lengthM = rs.getDouble("length_m");
                int numberTracks = rs.getInt("number_tracks");

                Integer sidingPosition = rs.getInt("siding_position");
                if (rs.wasNull()) sidingPosition = null;
                Double sidingLength = rs.getDouble("siding_length");
                if (rs.wasNull()) sidingLength = null;

                // Tenta encontrar mapeamento para este segmento
                Integer[] facilities = segmentFacilityMapping.get(segmentIdInt);

                if (facilities == null || facilities.length < 2) {
                    // Se não estiver no mapa, não conseguimos ligar a estações, ignoramos para o grafo
                    continue;
                }

                int startFacilityId = facilities[0];
                int endFacilityId = facilities[1];
                double lengthKm = lengthM / 1000.0;

                // Segmento Ida (A -> B)
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

                // Segmento Volta (B -> A) - Inverso
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

            System.out.println("SegmentLineRepository: Loaded " + segmentsCount + " segments (inc. inverses) via Mapping.");

        } catch (SQLException e) {
            System.err.println("SegmentLineRepository: ❌ Error loading segments: " + e.getMessage());
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

    public Optional<LineSegment> findDirectSegment(int stationAId, int stationBId) {
        return segmentCache.values().stream()
                .filter(s -> (s.getIdEstacaoInicio() == stationAId && s.getIdEstacaoFim() == stationBId))
                .findFirst();
    }
}
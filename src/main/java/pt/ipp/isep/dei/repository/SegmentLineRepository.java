package pt.ipp.isep.dei.repository;

import pt.ipp.isep.dei.DatabaseConnection.DatabaseConnection;
import pt.ipp.isep.dei.domain.LineSegment;

import java.sql.*;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Repository class responsible for loading and managing {@link LineSegment} entities.
 */
public class SegmentLineRepository {

    // Constante para prefixar IDs do segmento inverso (CRÍTICO PARA CONFLITOS)
    public static final String INVERSE_ID_PREFIX = "INV_";
    private static final double GENERIC_MAX_SPEED_KMH = 150.0;

    private final Map<String, LineSegment> segmentCache = new HashMap<>();

    // 🚨 MOCK TEMPORÁRIO PARA LIGAÇÃO FACILITY <-> SEGMENTO 🚨
    private final Map<Integer, Integer[]> segmentFacilityMapping = new HashMap<>();


    public SegmentLineRepository() {
        System.out.println("SegmentLineRepository: Initialized (Loading data from DB).");
        loadSegmentsFromDatabase();
    }

    private void populateFacilityMapping() {
        // Mapeamento mantido para a consistência do grafo
        segmentFacilityMapping.put(1, new Integer[]{7, 5});
        segmentFacilityMapping.put(3, new Integer[]{5, 13});
        segmentFacilityMapping.put(30, new Integer[]{13, 43});
        segmentFacilityMapping.put(31, new Integer[]{43, 45});
        segmentFacilityMapping.put(33, new Integer[]{45, 48});
        segmentFacilityMapping.put(35, new Integer[]{48, 50});
        segmentFacilityMapping.put(10, new Integer[]{13, 20});
        segmentFacilityMapping.put(15, new Integer[]{20, 8});
        segmentFacilityMapping.put(14, new Integer[]{8, 12});
        segmentFacilityMapping.put(20, new Integer[]{12, 17});
        segmentFacilityMapping.put(18, new Integer[]{17, 21});
        segmentFacilityMapping.put(25, new Integer[]{21, 16});
        segmentFacilityMapping.put(26, new Integer[]{16, 11});
        segmentFacilityMapping.put(11, new Integer[]{20, 18});
        segmentFacilityMapping.put(16, new Integer[]{18, 8});
        segmentFacilityMapping.put(12, new Integer[]{12, 17});
        segmentFacilityMapping.put(13, new Integer[]{17, 12});
        segmentFacilityMapping.put(21, new Integer[]{21, 16});
        segmentFacilityMapping.put(22, new Integer[]{16, 11});
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

                Integer[] facilities = segmentFacilityMapping.get(segmentIdInt);

                if (facilities == null || facilities.length < 2) {
                    continue;
                }

                int startFacilityId = facilities[0];
                int endFacilityId = facilities[1];
                double lengthKm = lengthM / 1000.0;

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

                // Segmento 2: B -> A (Inverso)
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

    public Optional<LineSegment> findDirectSegment(int stationAId, int stationBId) {
        return segmentCache.values().stream()
                .filter(s -> (s.getIdEstacaoInicio() == stationAId && s.getIdEstacaoFim() == stationBId))
                .findFirst();
    }

    public List<LineSegment> findAll() { return new ArrayList<>(segmentCache.values()); }

    private Map<Integer, Double> calculateAllLineLengthsKm() {
        return new HashMap<>();
    }
}
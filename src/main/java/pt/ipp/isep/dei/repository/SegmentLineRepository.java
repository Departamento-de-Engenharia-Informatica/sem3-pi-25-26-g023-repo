package pt.ipp.isep.dei.repository;

import pt.ipp.isep.dei.DatabaseConnection.DatabaseConnection;
import pt.ipp.isep.dei.domain.LineSegment;

import java.sql.*;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Repository class responsible for loading and managing {@link LineSegment} entities.
 *
 * <p>It loads segments from the database and creates inverse segments to support
 * bidirectional route simulation. It uses an in-memory cache for fast lookup.</p>
 */
public class SegmentLineRepository {

    // Constant to prefix inverse segment IDs (CRITICAL FOR CONFLICTS)
    public static final String INVERSE_ID_PREFIX = "INV_";
    private static final double GENERIC_MAX_SPEED_KMH = 150.0;
    private final List<LineSegment> inMemorySegments = new ArrayList<>();

    private final Map<String, LineSegment> segmentCache = new HashMap<>();

    // 🚨 TEMPORARY MOCK FOR FACILITY <-> SEGMENT LINKAGE 🚨
    private final Map<Integer, Integer[]> segmentFacilityMapping = new HashMap<>();

    public void cleanDatabaseData() {
        this.segmentCache.clear();
        this.inMemorySegments.clear();
        // Se estiver a usar a lista segmentFacilityMapping, limpe-a também, mas o crucial é o cache.
        System.out.println("🧹 Repository cleaned: Old database data removed.");
    }
    /**
     * Constructs the Segment Line Repository and initializes data loading.
     */
    public SegmentLineRepository() {
        System.out.println("SegmentLineRepository: Initialized (Loading data from DB).");
        loadSegmentsFromDatabase();
    }

    /**
     * Populates the hardcoded mapping between segment IDs (integer key) and the start/end facility IDs.
     */
    private void populateFacilityMapping() {
        // Mapping maintained for graph consistency
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
    /**
     * Guarda um segmento no Cache principal.
     * Isto permite que o Main injete dados do CSV e o Controller os encontre.
     */
    public void save(LineSegment segment) {
        if (segment != null) {
            // CORREÇÃO CRÍTICA: Guardar no segmentCache, não na lista isolada!
            // Assumindo que getIdSegmento() devolve int ou String, convertemos para String para ser a chave.
            String idKey = String.valueOf(segment.getIdSegmento());

            // Adiciona ou atualiza no mapa que o findAll usa
            this.segmentCache.put(idKey, segment);

            // Se quiser manter a lista por compatibilidade, pode, mas o importante é o Cache:
            this.inMemorySegments.add(segment);
        }
    }

    /**
     * Retorna todos os segmentos (BD + CSV injetado).
     */
    public List<LineSegment> findAll() {
        // Agora que o save() escreve no segmentCache, este método vai devolver TUDO (311 + 38)
        return new ArrayList<>(segmentCache.values());
    }
    /**
     * Loads segment data from the LINE_SEGMENT database table and creates inverse segments.
     */
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

                // Segment 1: A -> B (Original ID)
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

                // Segment 2: B -> A (Inverse)
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

    /**
     * Finds a list of segments by their IDs.
     *
     * @param segmentIds The list of segment ID strings (including inverse IDs).
     * @return A list of matching {@link LineSegment} objects.
     */
    public List<LineSegment> findByIds(List<String> segmentIds) {
        return segmentIds.stream()
                .filter(segmentCache::containsKey)
                .map(segmentCache::get)
                .collect(Collectors.toList());
    }

    /**
     * Finds a single segment by its ID.
     *
     * @param id The segment ID string.
     * @return An {@link Optional} containing the segment if found, or empty otherwise.
     */
    public Optional<LineSegment> findById(String id) {
        return Optional.ofNullable(segmentCache.get(id));
    }

    /**
     * Finds the direct segment going from Station A to Station B.
     *
     * @param stationAId The ID of the starting station.
     * @param stationBId The ID of the ending station.
     * @return An {@link Optional} containing the segment, if a direct mapping is found.
     */
    public Optional<LineSegment> findDirectSegment(int stationAId, int stationBId) {
        return segmentCache.values().stream()
                .filter(s -> (s.getIdEstacaoInicio() == stationAId && s.getIdEstacaoFim() == stationBId))
                .findFirst();
    }

    /**
     * Helper method to calculate line lengths (Currently unused/placeholder).
     */
    private Map<Integer, Double> calculateAllLineLengthsKm() {
        return new HashMap<>();
    }
}
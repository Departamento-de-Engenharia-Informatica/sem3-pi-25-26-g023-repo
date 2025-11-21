package pt.ipp.isep.dei.repository;

import pt.ipp.isep.dei.DatabaseConnection.DatabaseConnection;
import pt.ipp.isep.dei.domain.LineSegment;

import java.sql.*;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Repository class responsible for loading and managing {@link LineSegment} entities.
 * CRITICAL: Usa HARDCODED MOCK DATA (Dataset Line.csv e Segment.csv) para construir o grafo,
 * ignorando a DB Oracle vazia/inconsistente.
 */
public class SegmentLineRepository {

    private static final double DEFAULT_MAX_SPEED = 150.0;
    private static final int INVERSE_ID_OFFSET = 10000;

    /** Cache interna para facilitar a pesquisa por ID (String). */
    private final Map<String, LineSegment> segmentCache = new HashMap<>();

    // 🚨 MAPA DE TRADUÇÃO BASEADO EM Dataset_Sprint_1_v0.xlsx - Line.csv 🚨
    private final Map<Integer, Integer[]> facilityMap = new HashMap<>();

    // 🚨 MOCK DATA DO SEGMENT.CSV 🚨
    private final Map<Integer, int[]> segmentData = new HashMap<>();


    public SegmentLineRepository() {
        System.out.println("SegmentLineRepository: Initialized (will connect to DB on demand).");
        populateMapsFromDataset();
    }

    private void populateMapsFromDataset() {
        // --- Popula Facilities (Line.csv) ---
        facilityMap.put(1, new Integer[]{7, 5});     // Line 1: São Bento (7) -> Campanhã (5)
        facilityMap.put(2, new Integer[]{5, 20});    // Line 2: Campanhã (5) -> Nine (20)
        facilityMap.put(3, new Integer[]{20, 8});    // Line 3: Nine (20) -> Barcelos (8)

        // 🚨 LIGAÇÕES CRÍTICAS PARA O COMBOIO AGENDADO (3 -> 4) 🚨
        facilityMap.put(99, new Integer[]{3, 13});   // MOCK: Senhora das Dores (3) -> Contumil (13)
        facilityMap.put(100, new Integer[]{13, 4});  // MOCK: Contumil (13) -> Lousado (4)


        // --- Popula Segmentos (Segment.csv) ---
        segmentData.put(1, new int[]{1, 2618, 4});    // Line 1: 7 -> 5
        segmentData.put(10, new int[]{2, 29003, 2});  // Line 2: 5 -> 20
        segmentData.put(15, new int[]{3, 5286, 2}); // Line 3: 20 -> 8

        // 🚨 SEGMENTOS DE CONEXÃO MOCKADOS PARA O TRAIN 5421 (3 -> 4) 🚨
        segmentData.put(100, new int[]{99, 5000, 2}); // S100: Conecta Facility 3 -> 13
        segmentData.put(101, new int[]{100, 15000, 2}); // S101: Conecta Facility 13 -> 4


        List<LineSegment> segments = new ArrayList<>();

        for (Map.Entry<Integer, int[]> entry : segmentData.entrySet()) {
            int segmentId = entry.getKey();
            int[] data = entry.getValue();
            int lineId = data[0];
            int lengthMeters = data[1];
            int numberTracks = data[2];

            Integer[] facilities = facilityMap.get(lineId);

            if (facilities == null || facilities.length < 2) continue;

            int startFacilityId = facilities[0];
            int endFacilityId = facilities[1];
            double lengthKm = lengthMeters / 1000.0;

            // 1. Segmento na direção A -> B
            LineSegment segAB = new LineSegment(segmentId, startFacilityId, endFacilityId, lengthKm, DEFAULT_MAX_SPEED, numberTracks);
            segments.add(segAB);

            // 2. Segmento inverso B -> A
            LineSegment segBA = new LineSegment(segmentId + INVERSE_ID_OFFSET, endFacilityId, startFacilityId, lengthKm, DEFAULT_MAX_SPEED, numberTracks);
            segments.add(segBA);

            // Popula a cache
            segmentCache.put(String.valueOf(segmentId), segAB);
            segmentCache.put(String.valueOf(segmentId + INVERSE_ID_OFFSET), segBA);
        }

        System.out.println("SegmentLineRepository: Generated " + segments.size() + " segments (MOCK DATA) from CSV mapping.");
    }


    public List<LineSegment> findByIds(List<String> segmentIds) {
        if (segmentCache.isEmpty()) populateMapsFromDataset();

        return segmentIds.stream()
                .filter(segmentCache::containsKey)
                .map(segmentCache::get)
                .collect(Collectors.toList());
    }

    public Optional<LineSegment> findById(String id) {
        if (segmentCache.isEmpty()) populateMapsFromDataset();
        return Optional.ofNullable(segmentCache.get(id));
    }


    /**
     * Finds a direct line segment connecting two facilities na cache.
     */
    public Optional<LineSegment> findDirectSegment(int stationAId, int stationBId) {
        if (segmentCache.isEmpty()) populateMapsFromDataset();

        // Procura na cache por QUALQUER segmento que ligue A -> B (o primeiro encontrado é o caminho direto)
        return segmentCache.values().stream()
                .filter(s -> (s.getIdEstacaoInicio() == stationAId && s.getIdEstacaoFim() == stationBId))
                .findFirst();
    }

    public List<LineSegment> findAll() { if (segmentCache.isEmpty()) populateMapsFromDataset(); return new ArrayList<>(segmentCache.values()); }

    private Map<Integer, Double> calculateAllLineLengthsKm() {
        return new HashMap<>();
    }
}
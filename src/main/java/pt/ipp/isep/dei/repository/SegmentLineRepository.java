package pt.ipp.isep.dei.repository;

import pt.ipp.isep.dei.DatabaseConnection.DatabaseConnection;
import pt.ipp.isep.dei.domain.LineSegment;

import java.sql.*;
import java.util.*;

/**
 * Repository class responsible for loading and managing {@link LineSegment} entities.
 * <p>
 * This repository connects to the database to fetch railway line information
 * from the {@code RAILWAY_LINE} and {@code LINE_SEGMENT} tables.
 * It generates {@link LineSegment} objects for both directions (A→B and B→A),
 * assigning unique identifiers to reverse segments.
 * </p>
 *
 * <p>
 * Since the database does not contain speed information for each segment or line,
 * a default maximum speed is assigned to all line segments.
 * </p>
 *
 * <p><b>Note:</b> This class relies on {@link DatabaseConnection} to obtain database access.</p>
 *
 */
public class SegmentLineRepository {

    /** Default maximum speed for all segments (in km/h). */
    private static final double DEFAULT_MAX_SPEED = 150.0;

    /** Offset used to generate unique IDs for reverse segments. */
    private static final int INVERSE_ID_OFFSET = 1000;

    /**
     * Default constructor.
     * <p>
     * Initializes the repository. Database access occurs on demand
     * when data retrieval methods are invoked.
     * </p>
     */
    public SegmentLineRepository() {
        System.out.println("SegmentLineRepository: Initialized (will connect to DB on demand).");
    }

    /**
     * Retrieves all railway lines from the {@code RAILWAY_LINE} table,
     * calculates their total length based on {@code LINE_SEGMENT} data,
     * and creates {@link LineSegment} objects for both directions (A→B and B→A).
     * <p>
     * If a line has an invalid or zero length, a warning is displayed but the segment
     * is still added to maintain structural consistency.
     * </p>
     *
     * @return a list of {@link LineSegment} objects (including reverse segments)
     */
    public List<LineSegment> findAll() {
        List<LineSegment> segments = new ArrayList<>();
        Map<Integer, Double> lineLengthsKm = calculateAllLineLengthsKm(); // Compute total lengths first

        String sql = "SELECT line_id, start_facility_id, end_facility_id FROM RAILWAY_LINE ORDER BY line_id";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                int lineId = rs.getInt("line_id");
                int startFacilityId = rs.getInt("start_facility_id");
                int endFacilityId = rs.getInt("end_facility_id");

                double lengthKm = lineLengthsKm.getOrDefault(lineId, 0.0);
                if (lengthKm <= 0.0) {
                    System.err.println("⚠️ Warning: Invalid length (<=0) for line_id " + lineId +
                            ". Segment may not behave correctly.");
                }

                // Create the segment in the original direction (A → B)
                segments.add(new LineSegment(lineId, startFacilityId, endFacilityId, lengthKm, DEFAULT_MAX_SPEED));

                // Create the reverse segment (B → A) with a unique ID (using offset)
                segments.add(new LineSegment(lineId + INVERSE_ID_OFFSET, endFacilityId, startFacilityId, lengthKm, DEFAULT_MAX_SPEED));
            }

        } catch (SQLException e) {
            System.err.println("❌ Error while fetching railway lines from DB: " + e.getMessage());
        }

        System.out.println("SegmentLineRepository: Generated " + segments.size() + " segments (including reverse paths) from DB.");
        return segments;
    }

    /**
     * Finds a direct line segment connecting two facilities (stations).
     * <p>
     * This method queries the {@code RAILWAY_LINE} table for a direct connection
     * between the given start and end facilities (in either direction).
     * </p>
     *
     * @param stationAId the ID of the first facility
     * @param stationBId the ID of the second facility
     * @return an {@link Optional} containing the {@link LineSegment} if found, otherwise empty
     */
    public Optional<LineSegment> findDirectSegment(int stationAId, int stationBId) {
        Map<Integer, Double> lineLengthsKm = calculateAllLineLengthsKm();
        LineSegment segment = null;

        String sql = "SELECT line_id, start_facility_id, end_facility_id FROM RAILWAY_LINE " +
                "WHERE (start_facility_id = ? AND end_facility_id = ?) OR (start_facility_id = ? AND end_facility_id = ?)";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, stationAId);
            stmt.setInt(2, stationBId);
            stmt.setInt(3, stationBId);
            stmt.setInt(4, stationAId);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    int lineId = rs.getInt("line_id");
                    int startFacilityId = rs.getInt("start_facility_id");
                    int endFacilityId = rs.getInt("end_facility_id");
                    double lengthKm = lineLengthsKm.getOrDefault(lineId, 0.0);

                    if (lengthKm <= 0.0) {
                        System.err.println("⚠️ Warning: Invalid length (<=0) for line_id " + lineId +
                                " while fetching direct segment.");
                    }

                    // Return the segment in the same direction found in DB
                    segment = new LineSegment(lineId, startFacilityId, endFacilityId, lengthKm, DEFAULT_MAX_SPEED);
                }
            }

        } catch (SQLException e) {
            System.err.println("❌ Error while fetching direct segment between " + stationAId + " and " + stationBId +
                    ": " + e.getMessage());
        }

        return Optional.ofNullable(segment);
    }

    /**
     * Helper method that calculates the total length (in kilometers) of each railway line
     * based on the {@code LINE_SEGMENT} table.
     *
     * @return a map where the key is {@code line_id} and the value is the total length (in km)
     */
    private Map<Integer, Double> calculateAllLineLengthsKm() {
        Map<Integer, Double> lengths = new HashMap<>();
        String sql = "SELECT line_id, SUM(length_m) AS total_length_m FROM LINE_SEGMENT GROUP BY line_id";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                int lineId = rs.getInt("line_id");
                double totalLengthMeters = rs.getDouble("total_length_m");
                lengths.put(lineId, totalLengthMeters / 1000.0); // Convert meters → kilometers
            }

        } catch (SQLException e) {
            System.err.println("❌ Error while calculating total line lengths (LINE_SEGMENT): " + e.getMessage());
            return new HashMap<>(); // Return empty map to prevent NullPointerException
        }

        return lengths;
    }
}

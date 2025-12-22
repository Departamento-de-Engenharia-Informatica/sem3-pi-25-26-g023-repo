-- =============================================
-- USBD44 - Function: add_segment_to_line
-- =============================================

CREATE OR REPLACE FUNCTION add_segment_to_line(
    p_segment_id        IN LINE_SEGMENT.segment_id%TYPE,
    p_line_id           IN LINE_SEGMENT.line_id%TYPE,
    p_segment_order     IN LINE_SEGMENT.segment_order%TYPE,
    p_is_electrified    IN LINE_SEGMENT.is_electrified%TYPE,
    p_max_weight_kg_m   IN LINE_SEGMENT.max_weight_kg_m%TYPE,
    p_length_m          IN LINE_SEGMENT.length_m%TYPE,
    p_number_tracks     IN LINE_SEGMENT.number_tracks%TYPE,
    p_siding_position   IN LINE_SEGMENT.siding_position%TYPE DEFAULT NULL,
    p_siding_length     IN LINE_SEGMENT.siding_length%TYPE DEFAULT NULL
)
RETURN VARCHAR2
IS
    v_line_exists NUMBER;
    v_segment_exists NUMBER;
BEGIN
SELECT COUNT(*) INTO v_line_exists
FROM RAILWAY_LINE WHERE line_id = p_line_id;

IF v_line_exists = 0 THEN
        RETURN 'ERROR: Line does not exist';
END IF;

SELECT COUNT(*) INTO v_segment_exists
FROM LINE_SEGMENT WHERE segment_id = p_segment_id;

IF v_segment_exists > 0 THEN
        RETURN 'ERROR: Segment already exists';
END IF;

    IF (p_siding_position IS NOT NULL AND p_siding_length IS NULL) OR
       (p_siding_position IS NULL AND p_siding_length IS NOT NULL) THEN
        RETURN 'ERROR: Siding requires both position and length';
END IF;

    IF p_length_m <= 0 THEN
        RETURN 'ERROR: Length must be positive';
END IF;

    IF p_max_weight_kg_m <= 0 THEN
        RETURN 'ERROR: Max weight must be positive';
END IF;

    IF p_number_tracks NOT IN (1, 2) THEN
        RETURN 'ERROR: Number of tracks must be 1 or 2';
END IF;

    IF p_is_electrified NOT IN ('Yes', 'No') THEN
        RETURN 'ERROR: Electrification must be Yes or No';
END IF;

    IF p_siding_position IS NOT NULL AND p_siding_position < 0 THEN
        RETURN 'ERROR: Siding position cannot be negative';
END IF;

    IF p_siding_length IS NOT NULL AND p_siding_length < 0 THEN
        RETURN 'ERROR: Siding length cannot be negative';
END IF;

INSERT INTO LINE_SEGMENT VALUES (
                                    p_segment_id,
                                    p_line_id,
                                    p_segment_order,
                                    p_is_electrified,
                                    p_max_weight_kg_m,
                                    p_length_m,
                                    p_number_tracks,
                                    p_siding_position,
                                    p_siding_length
                                );

COMMIT;
RETURN 'SUCCESS: Segment added to line';
END;
/
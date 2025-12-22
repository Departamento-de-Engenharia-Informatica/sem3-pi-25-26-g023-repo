-- =============================================
-- USBD38 - Function: add_new_gauge
-- =============================================

CREATE OR REPLACE FUNCTION add_new_gauge(
    p_gauge_mm      IN NUMBER,
    p_gauge_name    IN VARCHAR2,
    p_description   IN VARCHAR2 DEFAULT NULL
) RETURN VARCHAR2
IS
    v_gauge_exists  NUMBER;
    v_name_exists   NUMBER;
BEGIN
    IF p_gauge_mm IS NULL THEN
        RETURN 'ERROR: Gauge value (mm) is required.';
END IF;

    IF p_gauge_name IS NULL OR TRIM(p_gauge_name) IS NULL THEN
        RETURN 'ERROR: Gauge name is required.';
END IF;

    IF p_gauge_mm < 500 OR p_gauge_mm > 3000 THEN
        RETURN 'ERROR: Gauge value must be between 500 and 3000 mm.';
END IF;

    IF LENGTH(TRIM(p_gauge_name)) < 2 THEN
        RETURN 'ERROR: Gauge name must have at least 2 characters.';
END IF;

SELECT COUNT(*)
INTO v_gauge_exists
FROM GAUGE
WHERE gauge_mm = p_gauge_mm;

IF v_gauge_exists > 0 THEN
        RETURN 'ERROR: Gauge with value ' || p_gauge_mm || ' mm already exists.';
END IF;

SELECT COUNT(*)
INTO v_name_exists
FROM GAUGE
WHERE UPPER(gauge_name) = UPPER(TRIM(p_gauge_name));

IF v_name_exists > 0 THEN
        RETURN 'ERROR: Gauge name "' || TRIM(p_gauge_name) || '" already exists.';
END IF;

INSERT INTO GAUGE (gauge_mm, gauge_name, description)
VALUES (
           p_gauge_mm,
           TRIM(p_gauge_name),
           TRIM(p_description)
       );

COMMIT;

RETURN 'SUCCESS: Gauge ' || p_gauge_mm || ' mm (' || TRIM(p_gauge_name) || ') added successfully.';
END add_new_gauge;
/
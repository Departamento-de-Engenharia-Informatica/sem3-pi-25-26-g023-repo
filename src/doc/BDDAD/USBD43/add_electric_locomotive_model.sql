CREATE OR REPLACE FUNCTION add_electric_locomotive_model (
    p_stock_id     VARCHAR2,
    p_operator_id  VARCHAR2,
    p_model        VARCHAR2,
    p_gauge_mm     NUMBER,
    p_power_kw     NUMBER,
    p_length_m     NUMBER,
    p_supports_multiple_gauges CHAR DEFAULT 'N'
) RETURN NUMBER
IS
    v_count NUMBER;
    v_stock_exists NUMBER;
BEGIN
    IF p_stock_id IS NULL OR p_operator_id IS NULL OR p_model IS NULL OR
       p_gauge_mm IS NULL OR p_power_kw IS NULL OR p_length_m IS NULL THEN
        RAISE_APPLICATION_ERROR(-20001, 'All mandatory parameters must be provided');
END IF;

    IF p_power_kw <= 0 THEN
        RAISE_APPLICATION_ERROR(-20002, 'Power must be greater than 0');
END IF;

    IF p_length_m <= 0 THEN
        RAISE_APPLICATION_ERROR(-20003, 'Length must be greater than 0');
END IF;

    IF p_supports_multiple_gauges NOT IN ('Y', 'N') THEN
        RAISE_APPLICATION_ERROR(-20009, 'supports_multiple_gauges must be Y or N');
END IF;

SELECT COUNT(*)
INTO v_count
FROM OPERATOR
WHERE operator_id = p_operator_id;

IF v_count = 0 THEN
        RAISE_APPLICATION_ERROR(-20004, 'Operator does not exist');
END IF;

SELECT COUNT(*)
INTO v_count
FROM GAUGE
WHERE gauge_mm = p_gauge_mm;

IF v_count = 0 THEN
        RAISE_APPLICATION_ERROR(-20005, 'Gauge does not exist');
END IF;

SELECT COUNT(*)
INTO v_stock_exists
FROM ROLLING_STOCK
WHERE stock_id = p_stock_id;

IF v_stock_exists > 0 THEN
        RAISE_APPLICATION_ERROR(-20006, 'Stock ID already exists');
END IF;

INSERT INTO ROLLING_STOCK (
    stock_id,
    operator_id,
    model,
    gauge_mm
) VALUES (
             p_stock_id,
             p_operator_id,
             p_model,
             p_gauge_mm
         );

INSERT INTO LOCOMOTIVE (
    stock_id,
    locomotive_type,
    power_kw,
    supports_multiple_gauges,
    length_m
) VALUES (
             p_stock_id,
             'Electric',
             p_power_kw,
             p_supports_multiple_gauges,
             p_length_m
         );

DBMS_OUTPUT.PUT_LINE('Electric locomotive model added successfully: ' || p_stock_id);
RETURN 1;

EXCEPTION
    WHEN DUP_VAL_ON_INDEX THEN
        RAISE_APPLICATION_ERROR(-20007, 'Duplicate stock ID (constraint violation)');
WHEN OTHERS THEN
        RAISE_APPLICATION_ERROR(-20008, 'Error adding electric locomotive model: ' || SQLERRM);
END;
/
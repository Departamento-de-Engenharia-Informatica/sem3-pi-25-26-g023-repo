CREATE OR REPLACE FUNCTION add_electric_locomotive_model (
    p_stock_id     VARCHAR2,
    p_operator_id  VARCHAR2,
    p_model        VARCHAR2,
    p_gauge_mm     NUMBER,
    p_power_kw     NUMBER,
    p_length_m     NUMBER
) RETURN NUMBER
IS
    v_count NUMBER;
BEGIN
    -- Verificar se o operador existe
SELECT COUNT(*)
INTO v_count
FROM OPERATOR
WHERE operator_id = p_operator_id;

IF v_count = 0 THEN
        RAISE_APPLICATION_ERROR(-20010, 'Operator does not exist');
END IF;

    -- Verificar se o gauge existe
SELECT COUNT(*)
INTO v_count
FROM GAUGE
WHERE gauge_mm = p_gauge_mm;

IF v_count = 0 THEN
        RAISE_APPLICATION_ERROR(-20011, 'Gauge does not exist');
END IF;

    -- Inserir em ROLLING_STOCK
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

-- Inserir em LOCOMOTIVE (elétrica)
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
             'N',
             p_length_m
         );

RETURN 1;
END;
/

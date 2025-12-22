-- =============================================
-- USBD33 - Train Length Constraint
-- =============================================

CREATE OR REPLACE FUNCTION get_train_length(p_train_id IN VARCHAR2)
RETURN NUMBER
IS
    v_total_length NUMBER := 0;
BEGIN
SELECT NVL(loc.length_m, 0)
INTO v_total_length
FROM TRAIN t
         LEFT JOIN LOCOMOTIVE loc ON t.locomotive_id = loc.stock_id
WHERE t.train_id = p_train_id;

SELECT v_total_length + NVL(SUM(wm.length_m), 0)
INTO v_total_length
FROM TRAIN_WAGON_USAGE twu
         JOIN WAGON w ON twu.wagon_id = w.stock_id
         JOIN WAGON_MODEL wm ON w.model_id = wm.model_id
WHERE twu.train_id = p_train_id;

RETURN v_total_length;
END;
/

CREATE OR REPLACE TRIGGER check_train_length
BEFORE INSERT OR UPDATE ON TRAIN_WAGON_USAGE
                            FOR EACH ROW
DECLARE
v_wagon_length NUMBER;
    v_current_length NUMBER;
    v_max_length NUMBER;
    v_new_length NUMBER;
BEGIN
BEGIN
SELECT wm.length_m INTO v_wagon_length
FROM WAGON w
         JOIN WAGON_MODEL wm ON w.model_id = wm.model_id
WHERE w.stock_id = :NEW.wagon_id;
EXCEPTION
        WHEN NO_DATA_FOUND THEN
            v_wagon_length := 0;
END;

    v_current_length := get_train_length(:NEW.train_id);

    IF UPDATING THEN
BEGIN
SELECT wm.length_m INTO v_wagon_length
FROM WAGON w
         JOIN WAGON_MODEL wm ON w.model_id = wm.model_id
WHERE w.stock_id = :OLD.wagon_id;

v_current_length := v_current_length - v_wagon_length;
EXCEPTION
            WHEN NO_DATA_FOUND THEN
                NULL;
END;

SELECT wm.length_m INTO v_wagon_length
FROM WAGON w
         JOIN WAGON_MODEL wm ON w.model_id = wm.model_id
WHERE w.stock_id = :NEW.wagon_id;
END IF;

    v_new_length := v_current_length + v_wagon_length;

SELECT max_length_m INTO v_max_length
FROM TRAIN
WHERE train_id = :NEW.train_id;

IF v_new_length > v_max_length THEN
        RAISE_APPLICATION_ERROR(-20001,
            'Train ' || :NEW.train_id ||
            ' would exceed maximum length. ' ||
            'Current: ' || v_current_length || 'm + ' ||
            'New wagon: ' || v_wagon_length || 'm = ' ||
            v_new_length || 'm > Max: ' || v_max_length || 'm');
END IF;
END;
/
-- =============================================
-- USBD33 - Train Length Constraint
-- =============================================

-- USBD33 - Train Length Constraint
-- Function to calculate current train length

CREATE OR REPLACE FUNCTION get_train_length(p_train_id IN VARCHAR2)
RETURN NUMBER
IS
    v_total_length NUMBER := 0;
    PRAGMA AUTONOMOUS_TRANSACTION;
BEGIN
    -- Comprimento da locomotiva deste comboio
BEGIN
SELECT NVL(loc.length_m, 0)
INTO v_total_length
FROM TRAIN t
         LEFT JOIN LOCOMOTIVE loc ON t.locomotive_id = loc.stock_id
WHERE t.train_id = p_train_id;
EXCEPTION
        WHEN NO_DATA_FOUND THEN
            v_total_length := 0;
END;

    -- Comprimento dos vagões associados a ESTE comboio
BEGIN
SELECT v_total_length + NVL(SUM(wm.length_m), 0)
INTO v_total_length
FROM TRAIN_WAGON_USAGE twu
         JOIN WAGON w ON twu.wagon_id = w.stock_id
         JOIN WAGON_MODEL wm ON w.model_id = wm.model_id
WHERE twu.train_id = p_train_id;
EXCEPTION
        WHEN NO_DATA_FOUND THEN
            NULL;
END;

COMMIT;
RETURN v_total_length;
END;
/

-- Trigger usando autonomous transaction para evitar mutating table
CREATE OR REPLACE TRIGGER check_train_length
BEFORE INSERT OR UPDATE ON TRAIN_WAGON_USAGE
                            FOR EACH ROW
DECLARE
PRAGMA AUTONOMOUS_TRANSACTION;
    v_max_length NUMBER;
    v_locomotive_length NUMBER;
    v_current_length NUMBER;
    v_new_wagon_length NUMBER;
    v_old_wagon_length NUMBER;
BEGIN
    -- Obter comprimento máximo e da locomotiva
BEGIN
SELECT t.max_length_m, NVL(loc.length_m, 0)
INTO v_max_length, v_locomotive_length
FROM TRAIN t
         LEFT JOIN LOCOMOTIVE loc ON t.locomotive_id = loc.stock_id
WHERE t.train_id = :NEW.train_id;
EXCEPTION
        WHEN NO_DATA_FOUND THEN
            COMMIT;
            RETURN;
END;

    -- Calcular comprimento atual usando a função (que é autonomous)
    v_current_length := get_train_length(:NEW.train_id);

    -- Obter comprimento do novo vagão
BEGIN
SELECT wm.length_m
INTO v_new_wagon_length
FROM WAGON w
         JOIN WAGON_MODEL wm ON w.model_id = wm.model_id
WHERE w.stock_id = :NEW.wagon_id;
EXCEPTION
        WHEN NO_DATA_FOUND THEN
            v_new_wagon_length := 0;
END;

    -- Se for UPDATE, obter comprimento do vagão antigo
    IF UPDATING THEN
BEGIN
SELECT wm.length_m
INTO v_old_wagon_length
FROM WAGON w
         JOIN WAGON_MODEL wm ON w.model_id = wm.model_id
WHERE w.stock_id = :OLD.wagon_id;
EXCEPTION
            WHEN NO_DATA_FOUND THEN
                v_old_wagon_length := 0;
END;

        -- Ajustar: remover o comprimento do vagão antigo
        v_current_length := v_current_length - v_old_wagon_length;
END IF;

    -- Adicionar comprimento do novo vagão
    v_current_length := v_current_length + v_new_wagon_length;

    -- Verificar limite
    IF v_current_length > v_max_length THEN
        COMMIT;
        RAISE_APPLICATION_ERROR(-20001,
            'Train ' || :NEW.train_id ||
            ' would exceed maximum length. ' ||
            'Length: ' || v_current_length || 'm > Max: ' || v_max_length || 'm');
END IF;

COMMIT;
END;
/

-- Trigger para DELETE (sempre permitido)
CREATE OR REPLACE TRIGGER check_train_length_delete
BEFORE DELETE ON TRAIN_WAGON_USAGE
FOR EACH ROW
DECLARE
BEGIN
NULL; -- DELETE sempre é permitido (reduz comprimento)
END;
/
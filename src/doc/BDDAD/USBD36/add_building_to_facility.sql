CREATE OR REPLACE FUNCTION add_building_to_facility (
    p_building_id   NUMBER,
    p_name          VARCHAR2,
    p_building_type VARCHAR2,
    p_facility_id   NUMBER
) RETURN NUMBER
IS
    v_count NUMBER;
BEGIN
    -- verificar se a facility existe
SELECT COUNT(*)
INTO v_count
FROM FACILITY
WHERE facility_id = p_facility_id;

IF v_count = 0 THEN
        RAISE_APPLICATION_ERROR(-20001, 'Facility does not exist');
END IF;

    -- inserir building
INSERT INTO BUILDING (building_id, name, building_type, facility_id)
VALUES (p_building_id, p_name, p_building_type, p_facility_id);

RETURN 1;
END;
/

CREATE OR REPLACE FUNCTION check_work_order_status_transition()
RETURNS TRIGGER AS $$
BEGIN
    -- Estados finales: no se puede cambiar nada más
    IF OLD.status IN ('finished', 'canceled') AND NEW.status <> OLD.status THEN
        RAISE EXCEPTION 'No se puede cambiar el estado de una orden en estado final (%): id=%', OLD.status, OLD.id;
    END IF;

    -- Si el status no cambió, no hay nada que validar
    IF NEW.status = OLD.status THEN
        RETURN NEW;
    END IF;

    -- Transiciones válidas (RN-06)
    IF (OLD.status = 'pending' AND NEW.status IN ('in_progress', 'canceled'))
       OR (OLD.status = 'in_progress' AND NEW.status IN ('finished', 'canceled')) THEN

        -- closed_at se completa automáticamente al llegar a un estado final (RN-13)
        IF NEW.status IN ('finished', 'canceled') THEN
            NEW.closed_at := CURRENT_TIMESTAMP;
        END IF;

        RETURN NEW;
    END IF;

    RAISE EXCEPTION 'Transición de estado no permitida: % -> % (id=%)', OLD.status, NEW.status, OLD.id;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trg_work_order_status_transition
BEFORE UPDATE ON "work_orders"
FOR EACH ROW
EXECUTE FUNCTION check_work_order_status_transition();
-- ============================================================
-- 99-seed.sql
-- Datos iniciales / demostración - Épsilon Workshop
-- ============================================================

-- ============================================================
-- CLIENTES
-- ============================================================

INSERT INTO "clients"
    ("document_type", "document", "name", "paternal_surname",
     "maternal_surname", "phone", "email", "notes")
VALUES
    ('DNI', '71425836', 'Juan', 'Pérez', 'Gómez',
     '987654321', 'juan.perez@example.com', 'Cliente frecuente'),

    ('DNI', '72819453', 'María', 'Sánchez', 'Vega',
     '986123456', 'maria.sanchez@example.com', NULL),

    ('DNI', '45678912', 'Pedro', 'Ramírez', 'Castillo',
     '985456789', 'pedro.ramirez@example.com',
     'Prefiere contacto por teléfono'),

    ('CE', '001245789', 'Andrés', 'Morales', 'Rojas',
     '984321654', 'andres.morales@example.com', NULL),

    ('RUC', '20601234567', 'Transportes Lima SAC', NULL, NULL,
     '983456789', 'contacto@transporteslima.example.com',
     'Cliente empresarial');


-- ============================================================
-- VEHÍCULOS
-- ============================================================

INSERT INTO "vehicles"
    ("client_id", "plate", "brand", "model", "year", "notes")
VALUES
    (
        (SELECT id FROM "clients" WHERE document = '71425836'),
        'ABC-123', 'Toyota', 'Corolla', 2020,
        'Sedán de uso particular'
    ),
    (
        (SELECT id FROM "clients" WHERE document = '71425836'),
        'BDE-456', 'Kia', 'Sportage', 2022,
        NULL
    ),
    (
        (SELECT id FROM "clients" WHERE document = '72819453'),
        'CFR-789', 'Hyundai', 'Tucson', 2021,
        'Revisión periódica'
    ),
    (
        (SELECT id FROM "clients" WHERE document = '45678912'),
        'DGH-321', 'Chevrolet', 'Onix', 2019,
        NULL
    ),
    (
        (SELECT id FROM "clients" WHERE document = '001245789'),
        'EJK-654', 'Nissan', 'Sentra', 2018,
        NULL
    ),
    (
        (SELECT id FROM "clients" WHERE document = '20601234567'),
        'FLM-987', 'Toyota', 'Hilux', 2023,
        'Vehículo de empresa'
    ),
    (
        (SELECT id FROM "clients" WHERE document = '20601234567'),
        'GNP-246', 'Ford', 'Ranger', 2022,
        'Vehículo de empresa'
    ),
    (
        (SELECT id FROM "clients" WHERE document = '72819453'),
        'HQR-135', 'Mazda', 'CX-5', NULL,
        'Año no registrado'
    );


-- ============================================================
-- SERVICIOS
-- ============================================================

INSERT INTO "services"
    ("name", "description", "reference_price")
VALUES
    (
        'Cambio de aceite',
        'Cambio de aceite de motor y revisión de niveles',
        120.00
    ),
    (
        'Alineación y balanceo',
        'Alineación de dirección y balanceo de ruedas',
        150.00
    ),
    (
        'Diagnóstico computarizado',
        'Diagnóstico mediante escáner automotriz',
        80.00
    ),
    (
        'Mantenimiento preventivo',
        'Revisión general y mantenimiento preventivo',
        250.00
    ),
    (
        'Cambio de pastillas de freno',
        'Reemplazo de pastillas de freno delanteras',
        180.00
    ),
    (
        'Revisión eléctrica',
        'Diagnóstico del sistema eléctrico del vehículo',
        100.00
    );


-- ============================================================
-- PARTES
-- ============================================================

INSERT INTO "parts"
    ("name", "stock", "min_stock_threshold", "unit_price")
VALUES
    ('Aceite sintético 5W-30 1L', 25, 10, 35.00),
    ('Filtro de aceite Toyota', 12, 5, 28.00),
    ('Filtro de aire universal', 8, 5, 45.00),
    ('Pastillas de freno delanteras', 6, 3, 160.00),
    ('Bujía de encendido', 20, 8, 18.00),
    ('Fusible automotriz 15A', 30, 10, 3.50);


-- ============================================================
-- USUARIOS
-- ============================================================
-- IMPORTANTE:
-- Reemplazar los password_hash por hashes generados mediante
-- PasswordHasher antes de utilizar estos usuarios para login.
--
-- Ejemplo de usuarios:
--   admin
--   mecanico01
--   mecanico02

INSERT INTO "users"
    ("username", "password_hash", "role", "name",
     "paternal_surname", "maternal_surname")
VALUES
    (
        'admin',
        '$2a$10$BwGDpJbFlzVd1dgIDuv9ROfLJQWF738Ml2BA0GxnSFqEalYTiQs1W',
        'admin',
        'Carlos',
        'Ramírez',
        'Torres'
    ),
    (
        'mecanico01',
        '$2a$10$Pp.KB/EQeH6FKXe8bCACwuFDMkkpG6GJcTufOj4fy6JvGFq43eB3.',
        'mechanic',
        'Luis',
        'García',
        'Mendoza'
    ),
    (
        'mecanico02',
        '$2a$10$kwmKQZEHjCcy9WlrwnD4h.T5Wzs3eHPUi4Y6AdlJ7RSLXJgkDJjxS',
        'mechanic',
        'Miguel',
        'Quispe',
        'Flores'
    );


-- ============================================================
-- MECÁNICOS
-- ============================================================

INSERT INTO "mechanics"
    ("user_id", "specialty")
VALUES
    (
        (SELECT id FROM "users" WHERE username = 'mecanico01'),
        'Mecánica general'
    ),
    (
        (SELECT id FROM "users" WHERE username = 'mecanico02'),
        'Diagnóstico y electricidad automotriz'
    );


-- ============================================================
-- ÓRDENES DE TRABAJO
-- ============================================================

-- Orden 1: TERMINADA
INSERT INTO "work_orders"
    ("vehicle_id", "mechanic_id", "status", "description",
     "created_at", "updated_at", "closed_at")
VALUES
    (
        (SELECT id FROM "vehicles" WHERE plate = 'ABC-123'),

        (
            SELECT m.id
            FROM "mechanics" m
            JOIN "users" u ON u.id = m.user_id
            WHERE u.username = 'mecanico01'
        ),

        'finished',

        'Mantenimiento preventivo y cambio de aceite',

        CURRENT_TIMESTAMP - INTERVAL '12 days',
        CURRENT_TIMESTAMP - INTERVAL '10 days',
        CURRENT_TIMESTAMP - INTERVAL '10 days'
    );


-- Orden 2: EN PROGRESO
INSERT INTO "work_orders"
    ("vehicle_id", "mechanic_id", "status", "description",
     "created_at", "updated_at")
VALUES
    (
        (SELECT id FROM "vehicles" WHERE plate = 'CFR-789'),

        (
            SELECT m.id
            FROM "mechanics" m
            JOIN "users" u ON u.id = m.user_id
            WHERE u.username = 'mecanico02'
        ),

        'in_progress',

        'Diagnóstico de falla en sistema eléctrico',

        CURRENT_TIMESTAMP - INTERVAL '2 days',
        CURRENT_TIMESTAMP - INTERVAL '1 day'
    );


-- Orden 3: PENDIENTE SIN MECÁNICO
-- Demuestra RN-03
INSERT INTO "work_orders"
    ("vehicle_id", "mechanic_id", "status", "description")
VALUES
    (
        (SELECT id FROM "vehicles" WHERE plate = 'DGH-321'),
        NULL,
        'pending',
        'Revisión general por ruido en suspensión'
    );


-- Orden 4: PENDIENTE ASIGNADA
INSERT INTO "work_orders"
    ("vehicle_id", "mechanic_id", "status", "description")
VALUES
    (
        (SELECT id FROM "vehicles" WHERE plate = 'EJK-654'),

        (
            SELECT m.id
            FROM "mechanics" m
            JOIN "users" u ON u.id = m.user_id
            WHERE u.username = 'mecanico01'
        ),

        'pending',

        'Cambio de pastillas de freno'
    );


-- Orden 5: CANCELADA
INSERT INTO "work_orders"
    ("vehicle_id", "mechanic_id", "status", "description",
     "created_at", "updated_at", "closed_at")
VALUES
    (
        (SELECT id FROM "vehicles" WHERE plate = 'FLM-987'),

        (
            SELECT m.id
            FROM "mechanics" m
            JOIN "users" u ON u.id = m.user_id
            WHERE u.username = 'mecanico02'
        ),

        'canceled',

        'Diagnóstico solicitado por el cliente y posteriormente cancelado',

        CURRENT_TIMESTAMP - INTERVAL '8 days',
        CURRENT_TIMESTAMP - INTERVAL '7 days',
        CURRENT_TIMESTAMP - INTERVAL '7 days'
    );


-- Orden 6: TERMINADA
-- Mismo vehículo que la orden 1.
-- Demuestra RN-02: múltiples órdenes por vehículo.
INSERT INTO "work_orders"
    ("vehicle_id", "mechanic_id", "status", "description",
     "created_at", "updated_at", "closed_at")
VALUES
    (
        (SELECT id FROM "vehicles" WHERE plate = 'ABC-123'),

        (
            SELECT m.id
            FROM "mechanics" m
            JOIN "users" u ON u.id = m.user_id
            WHERE u.username = 'mecanico02'
        ),

        'finished',

        'Alineación y balanceo',

        CURRENT_TIMESTAMP - INTERVAL '20 days',
        CURRENT_TIMESTAMP - INTERVAL '18 days',
        CURRENT_TIMESTAMP - INTERVAL '18 days'
    );


-- Orden 7: PENDIENTE SIN MECÁNICO
INSERT INTO "work_orders"
    ("vehicle_id", "mechanic_id", "status", "description")
VALUES
    (
        (SELECT id FROM "vehicles" WHERE plate = 'GNP-246'),
        NULL,
        'pending',
        'Inspección preventiva del vehículo'
    );


-- Orden 8: EN PROGRESO
INSERT INTO "work_orders"
    ("vehicle_id", "mechanic_id", "status", "description")
VALUES
    (
        (SELECT id FROM "vehicles" WHERE plate = 'HQR-135'),

        (
            SELECT m.id
            FROM "mechanics" m
            JOIN "users" u ON u.id = m.user_id
            WHERE u.username = 'mecanico01'
        ),

        'in_progress',

        'Revisión de motor y diagnóstico computarizado'
    );


-- ============================================================
-- SERVICIOS DE ÓRDENES
-- ============================================================

INSERT INTO "work_order_services"
    ("work_order_id", "service_id", "price_at_time")
VALUES
    (
        (
            SELECT wo.id
            FROM "work_orders" wo
            JOIN "vehicles" v ON v.id = wo.vehicle_id
            WHERE v.plate = 'ABC-123'
              AND wo.description =
                  'Mantenimiento preventivo y cambio de aceite'
        ),
        (SELECT id FROM "services"
         WHERE name = 'Mantenimiento preventivo'),
        250.00
    ),

    (
        (
            SELECT wo.id
            FROM "work_orders" wo
            JOIN "vehicles" v ON v.id = wo.vehicle_id
            WHERE v.plate = 'ABC-123'
              AND wo.description =
                  'Mantenimiento preventivo y cambio de aceite'
        ),
        (SELECT id FROM "services"
         WHERE name = 'Cambio de aceite'),
        120.00
    ),

    (
        (
            SELECT wo.id
            FROM "work_orders" wo
            JOIN "vehicles" v ON v.id = wo.vehicle_id
            WHERE v.plate = 'ABC-123'
              AND wo.description = 'Alineación y balanceo'
        ),
        (SELECT id FROM "services"
         WHERE name = 'Alineación y balanceo'),
        150.00
    ),

    (
        (
            SELECT wo.id
            FROM "work_orders" wo
            JOIN "vehicles" v ON v.id = wo.vehicle_id
            WHERE v.plate = 'CFR-789'
        ),
        (SELECT id FROM "services"
         WHERE name = 'Diagnóstico computarizado'),
        80.00
    ),

    (
        (
            SELECT wo.id
            FROM "work_orders" wo
            JOIN "vehicles" v ON v.id = wo.vehicle_id
            WHERE v.plate = 'HQR-135'
        ),
        (SELECT id FROM "services"
         WHERE name = 'Diagnóstico computarizado'),
        80.00
    );


-- ============================================================
-- PARTES DE ÓRDENES
-- ============================================================

INSERT INTO "work_order_parts"
    ("work_order_id", "part_id", "quantity", "price_at_time")
VALUES
    (
        (
            SELECT wo.id
            FROM "work_orders" wo
            JOIN "vehicles" v ON v.id = wo.vehicle_id
            WHERE v.plate = 'ABC-123'
              AND wo.description =
                  'Mantenimiento preventivo y cambio de aceite'
        ),
        (SELECT id FROM "parts"
         WHERE name = 'Aceite sintético 5W-30 1L'),
        4,
        35.00
    ),

    (
        (
            SELECT wo.id
            FROM "work_orders" wo
            JOIN "vehicles" v ON v.id = wo.vehicle_id
            WHERE v.plate = 'ABC-123'
              AND wo.description =
                  'Mantenimiento preventivo y cambio de aceite'
        ),
        (SELECT id FROM "parts"
         WHERE name = 'Filtro de aceite Toyota'),
        1,
        28.00
    ),

    (
        (
            SELECT wo.id
            FROM "work_orders" wo
            JOIN "vehicles" v ON v.id = wo.vehicle_id
            WHERE v.plate = 'EJK-654'
        ),
        (SELECT id FROM "parts"
         WHERE name = 'Pastillas de freno delanteras'),
        1,
        160.00
    ),

    (
        (
            SELECT wo.id
            FROM "work_orders" wo
            JOIN "vehicles" v ON v.id = wo.vehicle_id
            WHERE v.plate = 'HQR-135'
        ),
        (SELECT id FROM "parts"
         WHERE name = 'Bujía de encendido'),
        4,
        18.00
    );


-- ============================================================
-- HISTORIAL
-- ============================================================

-- Orden 1:
-- pending -> in_progress -> finished
INSERT INTO "work_order_history"
    ("work_order_id", "user_id", "change_type",
     "old_value", "new_value", "changed_at")
VALUES
    (
        (
            SELECT wo.id
            FROM "work_orders" wo
            JOIN "vehicles" v ON v.id = wo.vehicle_id
            WHERE v.plate = 'ABC-123'
              AND wo.description =
                  'Mantenimiento preventivo y cambio de aceite'
        ),
        (SELECT id FROM "users"
         WHERE username = 'admin'),
        'status_change',
        'pending',
        'in_progress',
        CURRENT_TIMESTAMP - INTERVAL '11 days'
    ),

    (
        (
            SELECT wo.id
            FROM "work_orders" wo
            JOIN "vehicles" v ON v.id = wo.vehicle_id
            WHERE v.plate = 'ABC-123'
              AND wo.description =
                  'Mantenimiento preventivo y cambio de aceite'
        ),
        (SELECT id FROM "users"
         WHERE username = 'admin'),
        'status_change',
        'in_progress',
        'finished',
        CURRENT_TIMESTAMP - INTERVAL '10 days'
    ),

    (
        (
            SELECT wo.id
            FROM "work_orders" wo
            JOIN "vehicles" v ON v.id = wo.vehicle_id
            WHERE v.plate = 'ABC-123'
              AND wo.description =
                  'Mantenimiento preventivo y cambio de aceite'
        ),
        (SELECT id FROM "users"
         WHERE username = 'admin'),
        'mechanic_reassignment',
        '2',
        '1',
        CURRENT_TIMESTAMP - INTERVAL '11 days'
    );


-- Orden 2:
-- pending -> in_progress
INSERT INTO "work_order_history"
    ("work_order_id", "user_id", "change_type",
     "old_value", "new_value", "changed_at")
VALUES
    (
        (
            SELECT wo.id
            FROM "work_orders" wo
            JOIN "vehicles" v ON v.id = wo.vehicle_id
            WHERE v.plate = 'CFR-789'
        ),
        (SELECT id FROM "users"
         WHERE username = 'mecanico02'),
        'status_change',
        'pending',
        'in_progress',
        CURRENT_TIMESTAMP - INTERVAL '1 day'
    );


-- Orden 5:
-- pending -> canceled
INSERT INTO "work_order_history"
    ("work_order_id", "user_id", "change_type",
     "old_value", "new_value", "changed_at")
VALUES
    (
        (
            SELECT wo.id
            FROM "work_orders" wo
            JOIN "vehicles" v ON v.id = wo.vehicle_id
            WHERE v.plate = 'FLM-987'
        ),
        (SELECT id FROM "users"
         WHERE username = 'admin'),
        'status_change',
        'pending',
        'canceled',
        CURRENT_TIMESTAMP - INTERVAL '7 days'
    );

    -- ============================================================
-- ÓRDENES DE TRABAJO ADICIONALES
-- Órdenes 9 - 20
-- ============================================================


-- ============================================================
-- Orden 9: PENDIENTE ASIGNADA
-- Mismo vehículo que la orden 3
-- ============================================================

INSERT INTO "work_orders"
    ("vehicle_id", "mechanic_id", "status", "description")
VALUES
(
    (SELECT id FROM "vehicles" WHERE plate = 'DGH-321'),
    (
        SELECT m.id
        FROM "mechanics" m
        JOIN "users" u ON u.id = m.user_id
        WHERE u.username = 'mecanico02'
    ),
    'pending',
    'Cambio de aceite y revisión de filtros'
);


-- ============================================================
-- Orden 10: EN PROGRESO
-- Mismo vehículo que la orden 4
-- ============================================================

INSERT INTO "work_orders"
    ("vehicle_id", "mechanic_id", "status", "description",
     "created_at", "updated_at")
VALUES
(
    (SELECT id FROM "vehicles" WHERE plate = 'EJK-654'),
    (
        SELECT m.id
        FROM "mechanics" m
        JOIN "users" u ON u.id = m.user_id
        WHERE u.username = 'mecanico02'
    ),
    'in_progress',
    'Revisión del sistema de frenos',
    CURRENT_TIMESTAMP - INTERVAL '3 days',
    CURRENT_TIMESTAMP - INTERVAL '1 day'
);


-- ============================================================
-- Orden 11: TERMINADA
-- Nuevo trabajo para GNP-246
-- ============================================================

INSERT INTO "work_orders"
    ("vehicle_id", "mechanic_id", "status", "description",
     "created_at", "updated_at", "closed_at")
VALUES
(
    (SELECT id FROM "vehicles" WHERE plate = 'GNP-246'),
    (
        SELECT m.id
        FROM "mechanics" m
        JOIN "users" u ON u.id = m.user_id
        WHERE u.username = 'mecanico01'
    ),
    'finished',
    'Mantenimiento preventivo completo',
    CURRENT_TIMESTAMP - INTERVAL '15 days',
    CURRENT_TIMESTAMP - INTERVAL '12 days',
    CURRENT_TIMESTAMP - INTERVAL '12 days'
);


-- ============================================================
-- Orden 12: CANCELADA
-- ============================================================

INSERT INTO "work_orders"
    ("vehicle_id", "mechanic_id", "status", "description",
     "created_at", "updated_at", "closed_at")
VALUES
(
    (SELECT id FROM "vehicles" WHERE plate = 'HQR-135'),
    (
        SELECT m.id
        FROM "mechanics" m
        JOIN "users" u ON u.id = m.user_id
        WHERE u.username = 'mecanico02'
    ),
    'canceled',
    'Cambio de batería solicitado por el cliente',
    CURRENT_TIMESTAMP - INTERVAL '14 days',
    CURRENT_TIMESTAMP - INTERVAL '13 days',
    CURRENT_TIMESTAMP - INTERVAL '13 days'
);


-- ============================================================
-- Orden 13: PENDIENTE SIN MECÁNICO
-- ============================================================

INSERT INTO "work_orders"
    ("vehicle_id", "mechanic_id", "status", "description")
VALUES
(
    (SELECT id FROM "vehicles" WHERE plate = 'ABC-123'),
    NULL,
    'pending',
    'Inspección de suspensión y dirección'
);


-- ============================================================
-- Orden 14: EN PROGRESO
-- ============================================================

INSERT INTO "work_orders"
    ("vehicle_id", "mechanic_id", "status", "description",
     "created_at", "updated_at")
VALUES
(
    (SELECT id FROM "vehicles" WHERE plate = 'FLM-987'),
    (
        SELECT m.id
        FROM "mechanics" m
        JOIN "users" u ON u.id = m.user_id
        WHERE u.username = 'mecanico01'
    ),
    'in_progress',
    'Diagnóstico de motor y sistema de inyección',
    CURRENT_TIMESTAMP - INTERVAL '5 days',
    CURRENT_TIMESTAMP - INTERVAL '2 days'
);


-- ============================================================
-- Orden 15: TERMINADA
-- Segundo trabajo terminado para CFR-789
-- ============================================================

INSERT INTO "work_orders"
    ("vehicle_id", "mechanic_id", "status", "description",
     "created_at", "updated_at", "closed_at")
VALUES
(
    (SELECT id FROM "vehicles" WHERE plate = 'CFR-789'),
    (
        SELECT m.id
        FROM "mechanics" m
        JOIN "users" u ON u.id = m.user_id
        WHERE u.username = 'mecanico02'
    ),
    'finished',
    'Cambio de pastillas de freno',
    CURRENT_TIMESTAMP - INTERVAL '25 days',
    CURRENT_TIMESTAMP - INTERVAL '23 days',
    CURRENT_TIMESTAMP - INTERVAL '23 days'
);


-- ============================================================
-- Orden 16: PENDIENTE ASIGNADA
-- ============================================================

INSERT INTO "work_orders"
    ("vehicle_id", "mechanic_id", "status", "description")
VALUES
(
    (SELECT id FROM "vehicles" WHERE plate = 'FLM-987'),
    (
        SELECT m.id
        FROM "mechanics" m
        JOIN "users" u ON u.id = m.user_id
        WHERE u.username = 'mecanico01'
    ),
    'pending',
    'Alineación y balanceo preventivo'
);


-- ============================================================
-- Orden 17: EN PROGRESO
-- ============================================================

INSERT INTO "work_orders"
    ("vehicle_id", "mechanic_id", "status", "description",
     "created_at", "updated_at")
VALUES
(
    (SELECT id FROM "vehicles" WHERE plate = 'GNP-246'),
    (
        SELECT m.id
        FROM "mechanics" m
        JOIN "users" u ON u.id = m.user_id
        WHERE u.username = 'mecanico02'
    ),
    'in_progress',
    'Revisión eléctrica y sistema de iluminación',
    CURRENT_TIMESTAMP - INTERVAL '4 days',
    CURRENT_TIMESTAMP - INTERVAL '1 day'
);


-- ============================================================
-- Orden 18: TERMINADA
-- Segundo trabajo terminado para HQR-135
-- ============================================================

INSERT INTO "work_orders"
    ("vehicle_id", "mechanic_id", "status", "description",
     "created_at", "updated_at", "closed_at")
VALUES
(
    (SELECT id FROM "vehicles" WHERE plate = 'HQR-135'),
    (
        SELECT m.id
        FROM "mechanics" m
        JOIN "users" u ON u.id = m.user_id
        WHERE u.username = 'mecanico01'
    ),
    'finished',
    'Mantenimiento preventivo y cambio de bujías',
    CURRENT_TIMESTAMP - INTERVAL '30 days',
    CURRENT_TIMESTAMP - INTERVAL '28 days',
    CURRENT_TIMESTAMP - INTERVAL '28 days'
);


-- ============================================================
-- Orden 19: PENDIENTE SIN MECÁNICO
-- ============================================================

INSERT INTO "work_orders"
    ("vehicle_id", "mechanic_id", "status", "description")
VALUES
(
    (SELECT id FROM "vehicles" WHERE plate = 'EJK-654'),
    NULL,
    'pending',
    'Diagnóstico de vibración al frenar'
);


-- ============================================================
-- Orden 20: TERMINADA
-- Segundo trabajo terminado para FLM-987
-- ============================================================

INSERT INTO "work_orders"
    ("vehicle_id", "mechanic_id", "status", "description",
     "created_at", "updated_at", "closed_at")
VALUES
(
    (SELECT id FROM "vehicles" WHERE plate = 'FLM-987'),
    (
        SELECT m.id
        FROM "mechanics" m
        JOIN "users" u ON u.id = m.user_id
        WHERE u.username = 'mecanico02'
    ),
    'finished',
    'Cambio de aceite y filtros',
    CURRENT_TIMESTAMP - INTERVAL '40 days',
    CURRENT_TIMESTAMP - INTERVAL '38 days',
    CURRENT_TIMESTAMP - INTERVAL '38 days'
);


-- ============================================================
-- SERVICIOS DE LAS NUEVAS ÓRDENES
-- ============================================================


-- Orden 9
INSERT INTO "work_order_services"
    ("work_order_id", "service_id", "price_at_time")
VALUES
(
    (
        SELECT wo.id
        FROM "work_orders" wo
        JOIN "vehicles" v ON v.id = wo.vehicle_id
        WHERE v.plate = 'DGH-321'
          AND wo.description = 'Cambio de aceite y revisión de filtros'
    ),
    (SELECT id FROM "services"
     WHERE name = 'Cambio de aceite'),
    120.00
);


-- Orden 10
INSERT INTO "work_order_services"
    ("work_order_id", "service_id", "price_at_time")
VALUES
(
    (
        SELECT wo.id
        FROM "work_orders" wo
        JOIN "vehicles" v ON v.id = wo.vehicle_id
        WHERE v.plate = 'EJK-654'
          AND wo.description = 'Revisión del sistema de frenos'
    ),
    (SELECT id FROM "services"
     WHERE name = 'Cambio de pastillas de freno'),
    180.00
);


-- Orden 11
INSERT INTO "work_order_services"
    ("work_order_id", "service_id", "price_at_time")
VALUES
(
    (
        SELECT wo.id
        FROM "work_orders" wo
        JOIN "vehicles" v ON v.id = wo.vehicle_id
        WHERE v.plate = 'GNP-246'
          AND wo.description = 'Mantenimiento preventivo completo'
    ),
    (SELECT id FROM "services"
     WHERE name = 'Mantenimiento preventivo'),
    250.00
);


-- Orden 14
INSERT INTO "work_order_services"
    ("work_order_id", "service_id", "price_at_time")
VALUES
(
    (
        SELECT wo.id
        FROM "work_orders" wo
        JOIN "vehicles" v ON v.id = wo.vehicle_id
        WHERE v.plate = 'FLM-987'
          AND wo.description = 'Diagnóstico de motor y sistema de inyección'
    ),
    (SELECT id FROM "services"
     WHERE name = 'Diagnóstico computarizado'),
    80.00
);


-- Orden 15
INSERT INTO "work_order_services"
    ("work_order_id", "service_id", "price_at_time")
VALUES
(
    (
        SELECT wo.id
        FROM "work_orders" wo
        JOIN "vehicles" v ON v.id = wo.vehicle_id
        WHERE v.plate = 'CFR-789'
          AND wo.description = 'Cambio de pastillas de freno'
    ),
    (SELECT id FROM "services"
     WHERE name = 'Cambio de pastillas de freno'),
    180.00
);


-- Orden 17
INSERT INTO "work_order_services"
    ("work_order_id", "service_id", "price_at_time")
VALUES
(
    (
        SELECT wo.id
        FROM "work_orders" wo
        JOIN "vehicles" v ON v.id = wo.vehicle_id
        WHERE v.plate = 'GNP-246'
          AND wo.description = 'Revisión eléctrica y sistema de iluminación'
    ),
    (SELECT id FROM "services"
     WHERE name = 'Revisión eléctrica'),
    100.00
);


-- Orden 18
INSERT INTO "work_order_services"
    ("work_order_id", "service_id", "price_at_time")
VALUES
(
    (
        SELECT wo.id
        FROM "work_orders" wo
        JOIN "vehicles" v ON v.id = wo.vehicle_id
        WHERE v.plate = 'HQR-135'
          AND wo.description = 'Mantenimiento preventivo y cambio de bujías'
    ),
    (SELECT id FROM "services"
     WHERE name = 'Mantenimiento preventivo'),
    250.00
);


-- Orden 20
INSERT INTO "work_order_services"
    ("work_order_id", "service_id", "price_at_time")
VALUES
(
    (
        SELECT wo.id
        FROM "work_orders" wo
        JOIN "vehicles" v ON v.id = wo.vehicle_id
        WHERE v.plate = 'FLM-987'
          AND wo.description = 'Cambio de aceite y filtros'
    ),
    (SELECT id FROM "services"
     WHERE name = 'Cambio de aceite'),
    120.00
);


-- ============================================================
-- PARTES DE LAS NUEVAS ÓRDENES
-- ============================================================


-- Orden 9
INSERT INTO "work_order_parts"
    ("work_order_id", "part_id", "quantity", "price_at_time")
VALUES
(
    (
        SELECT wo.id
        FROM "work_orders" wo
        JOIN "vehicles" v ON v.id = wo.vehicle_id
        WHERE v.plate = 'DGH-321'
          AND wo.description = 'Cambio de aceite y revisión de filtros'
    ),
    (SELECT id FROM "parts"
     WHERE name = 'Aceite sintético 5W-30 1L'),
    4,
    35.00
),
(
    (
        SELECT wo.id
        FROM "work_orders" wo
        JOIN "vehicles" v ON v.id = wo.vehicle_id
        WHERE v.plate = 'DGH-321'
          AND wo.description = 'Cambio de aceite y revisión de filtros'
    ),
    (SELECT id FROM "parts"
     WHERE name = 'Filtro de aceite Toyota'),
    1,
    28.00
);


-- Orden 10
INSERT INTO "work_order_parts"
    ("work_order_id", "part_id", "quantity", "price_at_time")
VALUES
(
    (
        SELECT wo.id
        FROM "work_orders" wo
        JOIN "vehicles" v ON v.id = wo.vehicle_id
        WHERE v.plate = 'EJK-654'
          AND wo.description = 'Revisión del sistema de frenos'
    ),
    (SELECT id FROM "parts"
     WHERE name = 'Pastillas de freno delanteras'),
    1,
    160.00
);


-- Orden 11
INSERT INTO "work_order_parts"
    ("work_order_id", "part_id", "quantity", "price_at_time")
VALUES
(
    (
        SELECT wo.id
        FROM "work_orders" wo
        JOIN "vehicles" v ON v.id = wo.vehicle_id
        WHERE v.plate = 'GNP-246'
          AND wo.description = 'Mantenimiento preventivo completo'
    ),
    (SELECT id FROM "parts"
     WHERE name = 'Filtro de aire universal'),
    1,
    45.00
);


-- Orden 18
INSERT INTO "work_order_parts"
    ("work_order_id", "part_id", "quantity", "price_at_time")
VALUES
(
    (
        SELECT wo.id
        FROM "work_orders" wo
        JOIN "vehicles" v ON v.id = wo.vehicle_id
        WHERE v.plate = 'HQR-135'
          AND wo.description = 'Mantenimiento preventivo y cambio de bujías'
    ),
    (SELECT id FROM "parts"
     WHERE name = 'Bujía de encendido'),
    4,
    18.00
);


-- Orden 20
INSERT INTO "work_order_parts"
    ("work_order_id", "part_id", "quantity", "price_at_time")
VALUES
(
    (
        SELECT wo.id
        FROM "work_orders" wo
        JOIN "vehicles" v ON v.id = wo.vehicle_id
        WHERE v.plate = 'FLM-987'
          AND wo.description = 'Cambio de aceite y filtros'
    ),
    (SELECT id FROM "parts"
     WHERE name = 'Aceite sintético 5W-30 1L'),
    4,
    35.00
),
(
    (
        SELECT wo.id
        FROM "work_orders" wo
        JOIN "vehicles" v ON v.id = wo.vehicle_id
        WHERE v.plate = 'FLM-987'
          AND wo.description = 'Cambio de aceite y filtros'
    ),
    (SELECT id FROM "parts"
     WHERE name = 'Filtro de aceite Toyota'),
    1,
    28.00
);


-- ============================================================
-- HISTORIAL DE LAS NUEVAS ÓRDENES
-- ============================================================


-- ============================================================
-- Orden 11
-- pending -> in_progress -> finished
-- ============================================================

INSERT INTO "work_order_history"
    ("work_order_id", "user_id", "change_type",
     "old_value", "new_value", "changed_at")
VALUES
(
    (
        SELECT wo.id
        FROM "work_orders" wo
        JOIN "vehicles" v ON v.id = wo.vehicle_id
        WHERE v.plate = 'GNP-246'
          AND wo.description = 'Mantenimiento preventivo completo'
    ),
    (SELECT id FROM "users" WHERE username = 'admin'),
    'status_change',
    'pending',
    'in_progress',
    CURRENT_TIMESTAMP - INTERVAL '14 days'
),
(
    (
        SELECT wo.id
        FROM "work_orders" wo
        JOIN "vehicles" v ON v.id = wo.vehicle_id
        WHERE v.plate = 'GNP-246'
          AND wo.description = 'Mantenimiento preventivo completo'
    ),
    (SELECT id FROM "users" WHERE username = 'mecanico01'),
    'status_change',
    'in_progress',
    'finished',
    CURRENT_TIMESTAMP - INTERVAL '12 days'
);


-- ============================================================
-- Orden 12
-- pending -> canceled
-- ============================================================

INSERT INTO "work_order_history"
    ("work_order_id", "user_id", "change_type",
     "old_value", "new_value", "changed_at")
VALUES
(
    (
        SELECT wo.id
        FROM "work_orders" wo
        JOIN "vehicles" v ON v.id = wo.vehicle_id
        WHERE v.plate = 'HQR-135'
          AND wo.description = 'Cambio de batería solicitado por el cliente'
    ),
    (SELECT id FROM "users" WHERE username = 'admin'),
    'status_change',
    'pending',
    'canceled',
    CURRENT_TIMESTAMP - INTERVAL '13 days'
);


-- ============================================================
-- Orden 15
-- pending -> in_progress -> finished
-- ============================================================

INSERT INTO "work_order_history"
    ("work_order_id", "user_id", "change_type",
     "old_value", "new_value", "changed_at")
VALUES
(
    (
        SELECT wo.id
        FROM "work_orders" wo
        JOIN "vehicles" v ON v.id = wo.vehicle_id
        WHERE v.plate = 'CFR-789'
          AND wo.description = 'Cambio de pastillas de freno'
    ),
    (SELECT id FROM "users" WHERE username = 'admin'),
    'status_change',
    'pending',
    'in_progress',
    CURRENT_TIMESTAMP - INTERVAL '24 days'
),
(
    (
        SELECT wo.id
        FROM "work_orders" wo
        JOIN "vehicles" v ON v.id = wo.vehicle_id
        WHERE v.plate = 'CFR-789'
          AND wo.description = 'Cambio de pastillas de freno'
    ),
    (SELECT id FROM "users" WHERE username = 'mecanico02'),
    'status_change',
    'in_progress',
    'finished',
    CURRENT_TIMESTAMP - INTERVAL '23 days'
);


-- ============================================================
-- Orden 18
-- pending -> in_progress -> finished
-- ============================================================

INSERT INTO "work_order_history"
    ("work_order_id", "user_id", "change_type",
     "old_value", "new_value", "changed_at")
VALUES
(
    (
        SELECT wo.id
        FROM "work_orders" wo
        JOIN "vehicles" v ON v.id = wo.vehicle_id
        WHERE v.plate = 'HQR-135'
          AND wo.description = 'Mantenimiento preventivo y cambio de bujías'
    ),
    (SELECT id FROM "users" WHERE username = 'admin'),
    'status_change',
    'pending',
    'in_progress',
    CURRENT_TIMESTAMP - INTERVAL '29 days'
),
(
    (
        SELECT wo.id
        FROM "work_orders" wo
        JOIN "vehicles" v ON v.id = wo.vehicle_id
        WHERE v.plate = 'HQR-135'
          AND wo.description = 'Mantenimiento preventivo y cambio de bujías'
    ),
    (SELECT id FROM "users" WHERE username = 'mecanico01'),
    'status_change',
    'in_progress',
    'finished',
    CURRENT_TIMESTAMP - INTERVAL '28 days'
);


-- ============================================================
-- Orden 20
-- pending -> in_progress -> finished
-- ============================================================

INSERT INTO "work_order_history"
    ("work_order_id", "user_id", "change_type",
     "old_value", "new_value", "changed_at")
VALUES
(
    (
        SELECT wo.id
        FROM "work_orders" wo
        JOIN "vehicles" v ON v.id = wo.vehicle_id
        WHERE v.plate = 'FLM-987'
          AND wo.description = 'Cambio de aceite y filtros'
    ),
    (SELECT id FROM "users" WHERE username = 'admin'),
    'status_change',
    'pending',
    'in_progress',
    CURRENT_TIMESTAMP - INTERVAL '39 days'
),
(
    (
        SELECT wo.id
        FROM "work_orders" wo
        JOIN "vehicles" v ON v.id = wo.vehicle_id
        WHERE v.plate = 'FLM-987'
          AND wo.description = 'Cambio de aceite y filtros'
    ),
    (SELECT id FROM "users" WHERE username = 'mecanico02'),
    'status_change',
    'in_progress',
    'finished',
    CURRENT_TIMESTAMP - INTERVAL '38 days'
);
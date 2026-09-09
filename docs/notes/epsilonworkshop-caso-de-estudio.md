# Épsilon Workshop — Caso de Estudio y Especificación de Requisitos (v3)

**Curso:** 2026-II
**Nomenclatura interna:** Épsilon
**Repositorio:** `epsilonworkshop`
**Stack:** Java (Maven) · Swing (GUI, pendiente confirmar vs JavaFX) · PostgreSQL 16 (Docker) · Arquitectura MVC + DAO

**Cronograma real del curso:**

| Etapa | Semanas |
|---|---|
| Análisis | 3 – 5 |
| Diseño | 5 – 7 |
| Desarrollo — Backend | 6 – 10 |
| Desarrollo — Frontend | 8 – 13 |
| Testing | 13 – 15 |
| Despliegue | 15 – 18 |

---

## 1. Caso de estudio

Un taller automotriz de tamaño pequeño-mediano gestiona actualmente el ingreso de vehículos, la asignación de mecánicos y el seguimiento de reparaciones de forma manual (cuadernos, hojas de cálculo sueltas o mensajes informales). Esto genera problemas recurrentes:

- No hay trazabilidad clara del estado de una reparación.
- No hay registro histórico ordenado de qué vehículo perteneció a qué cliente, ni qué mecánico atendió qué orden, ni quién hizo qué cambio y cuándo.
- No existe un catálogo estandarizado de servicios, lo que dificulta reportes y consistencia.
- El acceso a la información no está diferenciado por rol.

**Épsilon Workshop** es un sistema de escritorio que digitaliza este flujo, permitiendo registrar clientes, vehículos, mecánicos y órdenes de trabajo, con control de acceso por rol (administrador / mecánico), trazabilidad completa del ciclo de vida de una reparación, y reglas de negocio explícitas que garantizan consistencia de los datos.

El dominio real es automotriz, pero el modelado de clases se mantiene deliberadamente genérico en su nomenclatura (inglés, sin acoplarse a vocabulario específico del rubro), para que el diseño sea reutilizable como patrón de gestión "clientes → activos → órdenes de trabajo".

---

## 2. Alcance del proyecto (priorización, no calendario rígido)

Dado que Backend y Frontend se desarrollan en paralelo (semanas 6-10 y 8-13 respectivamente, traslapados) y no en fases lineales, el alcance se define por **prioridad (MoSCoW)** en vez de niveles atados a semanas exactas. Esto permite recular sin renegociar fechas si el tiempo se acorta.

### Must have (núcleo — se construye durante Backend/Frontend, semanas 6-13)
- Autenticación con roles Administrador / Mecánico.
- CRUD de clientes, vehículos, mecánicos (con baja lógica).
- Órdenes de trabajo con ciclo de vida de estado completo (ver Reglas de Negocio).
- Historial de auditoría de cambios de estado y reasignación de mecánico.
- Permisos diferenciados por rol.

### Should have (si el Must have está estable antes de Testing)
- Catálogo de servicios + asociación a órdenes de trabajo (tabla de detalle).
- Reportes por estado, mecánico, cliente y rango de fechas.

### Could have (solo si sobra tiempo en Testing/Despliegue)
- Catálogo/inventario de repuestos con control de stock.
- Asociación de repuestos a órdenes, con descuento de stock y alertas de stock bajo.

---

## 3. Fuera de alcance (limitaciones explícitas)

- Facturación / cobros / módulo contable.
- Acceso web o remoto (fuera de red local); es una aplicación de escritorio.
- Notificaciones automáticas (email, SMS, WhatsApp).
- Portal de autoservicio para clientes (no son usuarios del sistema).
- Integración con proveedores externos.
- IDs distribuidos (UUID) — se usa `SERIAL/INTEGER`.
- Multi-taller / multi-sucursal.
- Historial de cambios de propietario de un vehículo como entidad propia (se guarda el dueño actual; el historial de *órdenes de trabajo* del vehículo se conserva intacto, pero no se audita explícitamente "quién fue dueño cuándo").

---

## 4. Actores del sistema

| Actor | Descripción | Autenticación |
|---|---|---|
| **Administrador** | Gestión completa: clientes, vehículos, mecánicos, catálogos (Should/Could have), visibilidad total de órdenes y reportes. Puede reasignar mecánicos y cambiar cualquier estado de orden. | Sí |
| **Mecánico** | Visualiza y actualiza el estado de las órdenes que tiene asignadas. No gestiona clientes, vehículos ni mecánicos. | Sí |
| **Cliente** | No es usuario del sistema; es una entidad de datos gestionada por el Administrador. | No aplica |

**Diseño de tablas de usuario (confirmado):** patrón de tabla de extensión por rol.
- `users`: id, username, password_hash, role, name, paternal_surname, maternal_surname, **active**, created_at, updated_at.
- `mechanics`: id, user_id (FK → users, único), specialty, active — solo existe una fila por usuario con role = mechanic.

---

## 5. Reglas de negocio (RN)

Esta es la sección más importante del diseño: define el comportamiento real del sistema, más allá del CRUD básico.

**RN-01 — Baja lógica (soft delete).**
Clientes, vehículos y mecánicos nunca se eliminan físicamente. Se marcan con un campo `active = false`. Esto preserva la integridad del historial de órdenes de trabajo, que siempre debe poder reconstruirse.

**RN-02 — Concurrencia de órdenes por vehículo.**
Un mismo vehículo **puede tener múltiples órdenes de trabajo abiertas simultáneamente** (ej. revisión de motor y cambio de llantas registradas por separado). No existe restricción de unicidad vehículo-orden-abierta.

**RN-03 — Asignación de mecánico.**
Una orden de trabajo puede crearse **sin mecánico asignado** (`mechanic_id` nullable, estado inicial "pendiente de asignar" dentro del estado `pendiente`).

**RN-04 — Reasignación de mecánico.**
El Administrador puede reasignar el mecánico de una orden **en cualquier momento**, mientras la orden no esté en un estado final (`terminado` o `cancelado`). Cada reasignación se registra en el historial de auditoría (RN-08).

**RN-05 — Estados de una orden de trabajo.**
Cuatro estados: `pendiente`, `en_progreso`, `terminado`, `cancelado`.

**RN-06 — Transiciones válidas de estado.**
Camino estricto:
- `pendiente → en_progreso`
- `en_progreso → terminado`
- `pendiente → cancelado`
- `en_progreso → cancelado`

No están permitidas otras transiciones (ej. no se puede cancelar algo ya `terminado`, ni saltar directamente de `pendiente` a `terminado`).

**RN-07 — Estados finales bloqueados.**
`terminado` y `cancelado` son estados finales: una vez alcanzados, la orden **no puede reabrirse ni cambiar de estado**. Si se cometió un error, se crea una nueva orden de trabajo. *(Razón: mantiene el historial y los reportes por estado semánticamente estables — RF de reportes asume que "terminado" es una respuesta definitiva.)*

**RN-08 — Auditoría de cambios.**
Toda orden de trabajo mantiene una tabla de historial (`work_order_history`) que registra: quién hizo el cambio (`user_id`), tipo de cambio (`status_change` o `mechanic_reassignment`), valor anterior, valor nuevo, y fecha/hora. Se registra tanto cada cambio de estado como cada reasignación de mecánico.

**RN-09 — Permisos sobre el estado de la orden.**
El Administrador puede cambiar el estado de **cualquier** orden. El Mecánico solo puede cambiar el estado de las órdenes **que tiene asignadas actualmente**.

**RN-10 — Transferencia de propiedad de vehículo.**
Un vehículo puede transferirse a otro cliente (`vehicles.client_id` es editable). El historial de órdenes de trabajo del vehículo permanece intacto y no se reasigna — las órdenes pasadas siguen asociadas al mismo `vehicle_id`, independientemente de quién sea el dueño actual.

**RN-11 — Mecánicos inactivos.**
Un mecánico marcado como `active = false` no puede ser asignado a **nuevas** órdenes ni reasignado a órdenes existentes, pero permanece visible en el historial de órdenes que ya tenía asignadas.

**RN-12 — Unicidad de datos reales.**
La placa del vehículo es única (`UNIQUE`). El documento de identidad del cliente es único **por tipo de documento**: la restricción es `UNIQUE(document_type, document)`. Esto evita que dos clientes compartan el mismo número bajo el mismo tipo de documento (ej. dos DNI "12345678"), pero permite que el mismo número coexista bajo tipos distintos sin conflicto (ej. un DNI "12345678" y un RUC "12345678" no colisionan). `document_type` está restringido a los valores `DNI`, `CE`, `RUC` (`CHECK` en base de datos, enum `DocumentType` en Java).

**RN-13 — Trazabilidad temporal general.**
Todas las tablas principales (`clients`, `vehicles`, `mechanics`, `work_orders`) registran `created_at` y `updated_at`. Adicionalmente, `work_orders` registra `closed_at` (nullable, se completa automáticamente al pasar a `terminado` o `cancelado`), para poder reportar tanto por fecha de ingreso como por fecha de cierre.

**RN-14 — Bloqueo de cuenta de usuario.**
`users.active = false` bloquea el inicio de sesión de esa cuenta (Administrador o Mecánico), independientemente del estado de `mechanics.active`. Son dos flags independientes:
- `users.active` controla el **acceso al sistema** (¿puede loguearse?).
- `mechanics.active` controla la **elegibilidad para ser asignado a órdenes de trabajo** (RN-11).

Un mecánico puede tener `users.active = true` (puede loguearse) y `mechanics.active = false` (no asignable a nuevas órdenes), o viceversa, según la decisión del Administrador. No hay cascada automática entre ambos campos.

---

## 6. Requisitos funcionales (RF)

### Must have
- **RF-01:** Inicio de sesión con usuario y contraseña, identificando rol (Administrador / Mecánico), respetando el bloqueo de cuenta (RN-14).
- **RF-02:** CRUD de clientes (con baja lógica, RN-01), incluyendo tipo y número de documento (RN-12).
- **RF-03:** CRUD de vehículos, asociados a un cliente, con soporte para transferencia de dueño (RN-10).
- **RF-04:** CRUD de mecánicos, con baja lógica (RN-01) y estado activo/inactivo (RN-11).
- **RF-05:** Registro de orden de trabajo asociada a un vehículo, con mecánico opcional al momento de creación (RN-03).
- **RF-06:** Cambio de estado de una orden de trabajo, respetando las transiciones válidas (RN-06) y bloqueo de estados finales (RN-07).
- **RF-07:** Reasignación de mecánico en órdenes no finalizadas (RN-04), solo por el Administrador.
- **RF-08:** Registro automático en `work_order_history` de cada cambio de estado y reasignación (RN-08).
- **RF-09:** El Administrador tiene acceso completo a todas las funcionalidades.
- **RF-10:** El Mecánico solo visualiza y actualiza el estado de las órdenes que tiene asignadas (RN-09).
- **RF-17:** El Administrador puede activar/desactivar cuentas de usuario (RN-14), independientemente del estado de `mechanics.active`.

### Should have
- **RF-11:** Gestión de catálogo de servicios (nombre, descripción, precio referencial).
- **RF-12:** Asociación de uno o más servicios del catálogo a una orden de trabajo.
- **RF-13:** Reportes de órdenes filtrados por estado, mecánico, cliente y rango de fechas, permitiendo filtrar tanto por fecha de creación (`created_at`) como por fecha de cierre (`closed_at`).

### Could have
- **RF-14:** Gestión de catálogo de repuestos con control de stock.
- **RF-15:** Asociación de repuestos a una orden de trabajo, descontando stock automáticamente.
- **RF-16:** Alerta cuando el stock de un repuesto esté por debajo de un umbral mínimo configurable.

---

## 7. Requisitos no funcionales (RNF)

- **RNF-01:** Aplicación de escritorio (Java Swing), monousuario o multiusuario en red local — no diseñada para acceso vía internet.
- **RNF-02:** Persistencia en PostgreSQL 16, en contenedor Docker para desarrollo y pruebas.
- **RNF-03:** Arquitectura MVC extendida con capa DAO, separando presentación, lógica de negocio y acceso a datos. La capa DAO comparte una clase base común (`BaseDAO`) para evitar duplicación de lógica de conexión entre DAOs concretos.
- **RNF-04:** Contraseñas cifradas (hash), nunca en texto plano.
- **RNF-05:** Validación de toda entrada de usuario en la capa `util/Validations`.
- **RNF-06:** Nomenclatura de código consistente en inglés, sin mezcla de idiomas.
- **RNF-07:** Pruebas unitarias mínimas de la capa DAO y de la capa Controller (lógica de negocio) con JUnit 5, incluyendo casos de transición de estado inválida (RN-06/RN-07), permisos por rol (RN-09) y bloqueo de mecánico inactivo (RN-11).
- **RNF-08:** El proyecto debe levantarse en un entorno limpio siguiendo el README (clonar, Docker, Maven), sin configuración manual adicional.
- **RNF-09:** Identificadores `SERIAL/INTEGER`, no UUID.
- **RNF-10:** Las reglas de transición de estado (RN-06/RN-07) deben validarse tanto en la capa de negocio (Controller) como reforzarse con un trigger a nivel de base de datos (`02-triggers.sql`), que además completa `closed_at` automáticamente al llegar a un estado final.
- **RNF-11:** Las tablas principales mantienen `created_at`/`updated_at`; `work_orders` además mantiene `closed_at` (RN-13).

---

## 8. Entidades identificadas (resumen, sin schema definitivo)

| Entidad | Prioridad | Notas |
|---|---|---|
| `users` | Must | Login: username, password_hash, role, name, paternal_surname, maternal_surname, `active` (RN-14, bloqueo de login, independiente de `mechanics.active`), created_at, updated_at |
| `clients` | Must | + `document_type` (enum `DocumentType`: DNI/CE/RUC, con `CHECK` en BD), `document`, `notes` opcional, `active` (RN-01); UNIQUE compuesto `(document_type, document)` (RN-12); `paternal_surname` nullable, `maternal_surname` nullable; created_at, updated_at |
| `vehicles` | Must | FK → clients (editable, RN-10), + `active`, `plate` UNIQUE (RN-12), `notes` opcional, created_at, updated_at |
| `mechanics` | Must | FK → users (único), + `active` (RN-11), created_at, updated_at |
| `work_orders` | Must | FK → vehicles, FK → mechanics (nullable), `status` (enum Postgres `order_status`, 4 valores, RN-05/06/07), created_at, updated_at, closed_at nullable (RN-13) |
| `work_order_history` | Must | FK → work_orders, FK → users (quién hizo el cambio), `change_type` (`status_change` / `mechanic_reassignment`, enum Java `WorkOrderChangeType`), valor anterior/nuevo, timestamp (RN-08) |
| `services` | Should | Catálogo |
| `work_order_services` | Should | Tabla de detalle (work_order ↔ service) |
| `parts` | Could | Catálogo + stock |
| `work_order_parts` | Could | Tabla de detalle (work_order ↔ part), descuenta stock |

---

## 9. Próximos pasos

1. Completar la lógica de negocio en la capa Controller (RN-01 a RN-14), actualmente cascarones sin validación.
2. Escribir tests JUnit de Controller para los casos críticos: transiciones de estado inválidas, permisos por rol, mecánico/usuario inactivo.
3. Confirmar decisión de GUI: Swing vs JavaFX.
4. Avanzar a la capa View: `LoginFrame` como punto de entrada real, luego `MainFrame` con menú condicionado por rol, luego los paneles restantes.

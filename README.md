# Épsilon Workshop

Sistema de escritorio (Java + Swing) para gestionar un taller automotriz: clientes, vehículos, mecánicos y órdenes de trabajo, con control de acceso por rol (administrador / mecánico) y auditoría de cambios.

Algoritmos y Estructuras de Datos 2026-II. Arquitectura MVC + DAO sobre PostgreSQL 16.

## Requisitos

- JDK 25
- Maven 3.9 o superior
- Docker con Docker Compose (Docker Desktop en Windows y macOS)

## Inicio rápido

### 1. Clonar el repositorio

```bash
git clone <URL-DEL-REPOSITORIO>
cd epsilonworkshop
```

### 2. Levantar la base de datos

```bash
docker compose -f db/docker-compose.yml up -d
```

La primera vez, Postgres ejecuta automáticamente los scripts de `db/init/` (schema, triggers y datos de ejemplo). No hay que hacer nada más.

### 3. Ejecutar la aplicación

Desde la terminal:

```bash
mvn compile exec:java
```

O desde NetBeans: abrir el proyecto y usar **Run Project**.

### 4. Iniciar sesión

Usuarios de desarrollo cargados por el seed:

| Usuario      | Contraseña    | Rol           |
|--------------|---------------|---------------|
| `admin`      | `admin123`    | Administrador |
| `mecanico01` | `mecanico123` | Mecánico      |
| `mecanico02` | `mecanico123` | Mecánico      |

Estas credenciales son solo para desarrollo local y deben cambiarse antes de cualquier uso real.

## Base de datos

| Parámetro  | Valor                    |
|------------|--------------------------|
| Host       | `localhost:5432`         |
| Base       | `epsilonworkshop`        |
| Usuario    | `epsilonworkshop_user`   |
| Contraseña | `epsilonworkshop_pass`   |

La aplicación lee estos valores de `src/main/resources/application.properties`; deben coincidir con los de `db/docker-compose.yml`.

Los scripts de `db/init/` se ejecutan en orden alfabético: `01-schema.sql`, `02-triggers.sql`, `99-seed.sql`.

### Reiniciar la base de datos desde cero

Postgres solo ejecuta los scripts de `db/init/` cuando el volumen está vacío, es decir, la primera vez. Si modificas un script y vuelves a levantar el contenedor, los cambios **no se aplican**. Para recrear la base con los scripts actuales:

```bash
docker compose -f db/docker-compose.yml down -v
docker compose -f db/docker-compose.yml up -d
```

El flag `-v` elimina el volumen y, con él, todos los datos.

## Tests

```bash
mvn test
```

**La base de datos debe estar levantada**: los tests de DAO y algunos de integración se conectan a ella. Cada test corre dentro de una transacción que se revierte al terminar, así que no modifican los datos de la base.

## Estructura del proyecto

```
db/                  docker-compose.yml y scripts SQL de inicialización
docs/notes/          caso de estudio, reglas de negocio y diagrama de la base
src/main/java/com/epsilonworkshop/
    config/          conexión a BD y sesión de usuario
    controller/      lógica de negocio (reglas RN-01 a RN-14)
    dao/             acceso a datos
    model/           entidades y enums
    exception/       excepciones de negocio
    util/            hashing, validaciones, arreglos, ordenamiento y listas enlazadas
    view/            interfaz Swing
    view/avances/    paneles de demostración académica (Avance 1 y 2)
```

## Documentación

El caso de estudio, los requisitos y las reglas de negocio están en [`docs/notes/epsilonworkshop-caso-de-estudio.md`](docs/notes/epsilonworkshop-caso-de-estudio.md).

## Problemas comunes

**El puerto 5432 ya está en uso.** Probablemente tienes otro PostgreSQL instalado localmente. Deténlo, o cambia el mapeo de puertos en `db/docker-compose.yml` (por ejemplo `"5433:5432"`) y la URL en `application.properties`.

**La aplicación no se conecta a la base.** Verifica que el contenedor esté corriendo con `docker compose -f db/docker-compose.yml ps`.
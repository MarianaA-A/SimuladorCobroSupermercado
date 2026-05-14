# Simulador de Cobro de Supermercado

Aplicación en Java 17 que simula el proceso de cobro en un supermercado, permitiendo administrar productos, clientes y cajeras, ejecutar una simulación concurrente y guardar el resultado en MySQL.

## Requisitos

- Java 17
- Maven 3.8 o superior
- MySQL 8

## Estructura

- `backend/`: lógica de negocio, persistencia, simulación por consola y acceso a base de datos.
- `frontend/`: interfaz gráfica Swing para administrar datos y ejecutar la simulación.
- `backend/db/inicializar_bd.sql`: script de esquema y datos base.
- `backend/db/config.properties`: configuración de conexión a la base de datos.

## Arquitectura

El proyecto está organizado por capas para mantener cada responsabilidad separada:

- `model`: contiene las entidades de negocio como productos, clientes, cajeras y resultados de cobro.
- `db`: centraliza el acceso a datos con DAO y la conexión JDBC.
- `service`: coordina la simulación concurrente, el procesamiento de clientes y el guardado de reportes.
- `ui`: implementa la interfaz Swing para administración y ejecución de la simulación.

El flujo general es: los datos se cargan o administran desde la UI, pasan al servicio de simulación y finalmente se persisten en MySQL mediante los DAO.

## Configuración de base de datos

1. Crear la base de datos en MySQL.
2. Ejecutar `backend/db/inicializar_bd.sql`.
3. Ajustar `backend/db/config.properties` con host, puerto, usuario y contraseña.

## Ejecución

### Backend por consola

Desde la carpeta `backend`:

```bash
mvn -q -DskipTests clean compile
mvn -q exec:java
```

### Interfaz gráfica

Desde la carpeta `frontend`:

```bash
mvn -q -DskipTests clean compile
mvn -q exec:java
```

## Validación técnica

- `backend` compila correctamente con Maven.
- `frontend` compila correctamente con Maven.
- Existe una utilidad de prueba manual para validar desactivación y eliminación física en `backend/src/com/supermercado/test/TestDeletes.java`.

## Observaciones

- El proyecto usa el patrón DAO para aislar el acceso a datos.
- La simulación maneja atención concurrente mediante hilos y colas de clientes.
- El campo `activo` se usa para desactivar registros sin perder historial.
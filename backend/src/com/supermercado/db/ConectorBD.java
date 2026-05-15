package com.supermercado.db;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Properties;

/**
 * Gestiona la carga de la configuracion y la creacion de conexiones JDBC a MySQL.
 */
public class ConectorBD {
    private static ConectorBD instancia;
    private Connection conexion;
    private Properties propiedades;
    private boolean usarH2EnMemoria = false;

    private ConectorBD() {
        propiedades = new Properties();
        cargarConfiguracion();
    }

    public static synchronized ConectorBD obtenerInstancia() {
        if (instancia == null) {
            instancia = new ConectorBD();
        }
        return instancia;
    }

    private void cargarConfiguracion() {
        try (InputStream entrada = obtenerEntradaConfiguracion()) {
            if (entrada == null) {
                System.err.println("No se encontro el archivo config.properties, se usará H2 en memoria como fallback");
                usarH2EnMemoria = true;
                return;
            }
            propiedades.load(entrada);
        } catch (IOException e) {
            System.err.println("Error cargando configuracion: " + e.getMessage());
        }
    }

    private InputStream obtenerEntradaConfiguracion() {
        ClassLoader classLoader = getClass().getClassLoader();
        InputStream entrada = classLoader.getResourceAsStream("db/config.properties");
        if (entrada != null) {
            return entrada;
        }
        return classLoader.getResourceAsStream("config.properties");
    }

    public Connection obtenerConexion() throws SQLException {
        if (conexion == null || conexion.isClosed()) {
            conexion = crearConexion();
        }
        return conexion;
    }

    private Connection crearConexion() throws SQLException {
        if (usarH2EnMemoria) {
            try {
                Class.forName("org.h2.Driver");
                String url = "jdbc:h2:mem:supermercado;DB_CLOSE_DELAY=-1";
                Connection conn = DriverManager.getConnection(url, "sa", "");
                inicializarEsquemaH2(conn);
                System.out.println("Usando H2 en memoria para pruebas (sin config.properties)");
                return conn;
            } catch (ClassNotFoundException e) {
                System.err.println("Driver H2 no encontrado: " + e.getMessage());
                throw new SQLException("Driver H2 no disponible", e);
            }
        }

        String host = propiedades.getProperty("db.host", "localhost");
        String puerto = propiedades.getProperty("db.port", "3306");
        String baseDatos = propiedades.getProperty("db.name", "supermercado_cobro");
        String usuario = propiedades.getProperty("db.user", "root");
        String contrasena = propiedades.getProperty("db.password", "");
        String useSSL = propiedades.getProperty("db.useSSL", "false");
        String allowPublicKey = propiedades.getProperty("db.allowPublicKeyRetrieval", "true");
        String serverTimezone = propiedades.getProperty("db.serverTimezone", "America/Bogota");

        String url = String.format(
                "jdbc:mysql://%s:%s/%s?useSSL=%s&allowPublicKeyRetrieval=%s&serverTimezone=%s",
                host, puerto, baseDatos, useSSL, allowPublicKey, serverTimezone
        );

        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            try {
                Connection conn = DriverManager.getConnection(url, usuario, contrasena);
                asegurarEsquemaActivo(conn);
                System.out.println("Conexion exitosa a base de datos: " + baseDatos);
                return conn;
            } catch (SQLException mysqlEx) {
                System.err.println("Error conectando a MySQL: " + mysqlEx.getMessage());
                // Intentar fallback a H2 en memoria en caso de error de conexion
                try {
                    return crearConexionH2Fallback();
                } catch (SQLException | ClassNotFoundException fallbackEx) {
                    // Re-lanzar el error original para que la capa superior lo maneje
                    throw mysqlEx;
                }
            }
        } catch (ClassNotFoundException e) {
            System.err.println("Driver JDBC no encontrado: " + e.getMessage());
            // Si el driver MySQL no está, intentar H2 como alternativa
            try {
                return crearConexionH2Fallback();
            } catch (SQLException | ClassNotFoundException ex) {
                throw new SQLException("Drivers de BD no disponibles", ex);
            }
        }
    }

    private Connection crearConexionH2Fallback() throws SQLException, ClassNotFoundException {
        Class.forName("org.h2.Driver");
        String url = "jdbc:h2:mem:supermercado;DB_CLOSE_DELAY=-1";
        Connection conn = DriverManager.getConnection(url, "sa", "");
        try {
            inicializarEsquemaH2(conn);
        } catch (SQLException e) {
            // si la inicialización falla, cerramos la conexión y re-lanzamos
            try { conn.close(); } catch (SQLException ignore) {}
            throw e;
        }
        System.out.println("Fallback: usando H2 en memoria para pruebas");
        return conn;
    }

    private void inicializarEsquemaH2(Connection conn) throws SQLException {
        InputStream entrada = getClass().getClassLoader().getResourceAsStream("db/inicializar_bd.sql");
        try {
            if (entrada == null) {
                // Buscar en varias rutas relativas posibles
                java.nio.file.Path[] candidates = new java.nio.file.Path[]{
                        java.nio.file.Paths.get("backend", "db", "inicializar_bd.sql"),
                        java.nio.file.Paths.get("..", "backend", "db", "inicializar_bd.sql"),
                        java.nio.file.Paths.get(System.getProperty("user.dir"), "backend", "db", "inicializar_bd.sql")
                };
                boolean found = false;
                for (java.nio.file.Path alt : candidates) {
                    try {
                        if (alt != null && java.nio.file.Files.exists(alt)) {
                            System.out.println("Inicializar H2: cargando script desde " + alt.toAbsolutePath());
                            entrada = java.nio.file.Files.newInputStream(alt);
                            found = true;
                            break;
                        }
                    } catch (Exception ex) {
                        // continue to next candidate
                    }
                }
                if (!found) {
                    System.out.println("Inicializar H2: resource db/inicializar_bd.sql no encontrada en classpath ni en rutas relativas");
                    return; // nada que inicializar
                }
            } else {
                System.out.println("Inicializar H2: cargando script desde classpath db/inicializar_bd.sql");
            }

            String sql = new String(entrada.readAllBytes());
            // Simple split by semicolon; ignores advanced cases but sufficient for our script
            String[] statements = sql.split(";\\s*\\r?\\n");
            System.out.println("Inicializar H2: ejecutando " + statements.length + " sentencias del script");
            for (String stmt : statements) {
                String s = stmt.trim();
                if (s.isEmpty()) continue;
                // Ignorar instrucciones incompatibles con H2 (como USE)
                String upper = s.toUpperCase();
                if (upper.startsWith("USE ") || upper.startsWith("DELIMITER ") || upper.startsWith("SET ")) {
                    System.out.println("Inicializar H2: ignorando sentencia incompatible: " + s.split("\\n",1)[0]);
                    continue;
                }
                System.out.println("Inicializar H2: ejecutando sentencia: " + (s.length() > 120 ? s.substring(0, 120) + "..." : s));
                try (PreparedStatement ps = conn.prepareStatement(s)) {
                    try {
                        ps.execute();
                    } catch (SQLException stmtEx) {
                        // Registrar y continuar con la siguiente sentencia
                        System.err.println("Inicializar H2: ignorada sentencia SQL por error: " + stmtEx.getMessage());
                    }
                }
            }
            // Verificar si las tablas principales existen; si no, crear un esquema mínimo compatible
            if (!existeTabla(conn, "clientes") || !existeTabla(conn, "productos") || !existeTabla(conn, "cajeras")) {
                crearTablasH2Basicas(conn);
            }
        } catch (IOException e) {
            throw new SQLException("Error leyendo script de inicializacion H2", e);
        } finally {
            if (entrada != null) {
                try { entrada.close(); } catch (IOException ignored) {}
            }
        }
    }

    private boolean existeTabla(Connection conn, String tableName) throws SQLException {
        try (ResultSet rs = conn.getMetaData().getTables(null, null, tableName.toUpperCase(), null)) {
            return rs.next();
        }
    }

    private void crearTablasH2Basicas(Connection conn) throws SQLException {
        String[] stmts = new String[]{
                "CREATE TABLE IF NOT EXISTS clientes (id INT AUTO_INCREMENT PRIMARY KEY, nombre VARCHAR(255), activo TINYINT)",
                "CREATE TABLE IF NOT EXISTS productos (id INT AUTO_INCREMENT PRIMARY KEY, nombre VARCHAR(255), precio DOUBLE, tiempo_procesamiento_segundos INT, activo TINYINT)",
                "CREATE TABLE IF NOT EXISTS cajeras (id INT AUTO_INCREMENT PRIMARY KEY, nombre VARCHAR(255), estado VARCHAR(50), activo TINYINT)",
                "CREATE TABLE IF NOT EXISTS transacciones (id INT AUTO_INCREMENT PRIMARY KEY, id_cliente INT, id_cajera INT, total_cliente DOUBLE, tiempo_total_segundos INT, fecha_inicio TIMESTAMP, fecha_fin TIMESTAMP)",
                "CREATE TABLE IF NOT EXISTS detalle_transacciones (id INT AUTO_INCREMENT PRIMARY KEY, id_transaccion INT, id_producto INT, nombre_producto VARCHAR(255), costo_producto DOUBLE, tiempo_procesamiento_segundos INT)"
        };
        for (String s : stmts) {
            try (PreparedStatement ps = conn.prepareStatement(s)) {
                ps.execute();
            }
        }
        // Insertar algunos datos de ejemplo para evitar errores de tablas vacías
        try (PreparedStatement ps = conn.prepareStatement("INSERT INTO clientes (nombre, activo) VALUES ('Cliente Ejemplo', 1)")) {
            ps.executeUpdate();
        } catch (SQLException ignored) {}
        try (PreparedStatement ps = conn.prepareStatement("INSERT INTO productos (nombre, precio, tiempo_procesamiento_segundos, activo) VALUES ('Producto Ejemplo', 1000, 1, 1)")) {
            ps.executeUpdate();
        } catch (SQLException ignored) {}
    }

    private void asegurarEsquemaActivo(Connection conn) throws SQLException {
        asegurarColumnaActivo(conn, "productos");
        asegurarColumnaActivo(conn, "clientes");
        asegurarColumnaActivo(conn, "cajeras");
    }

    private void asegurarColumnaActivo(Connection conn, String tabla) throws SQLException {
        boolean tieneActivo = existeColumna(conn, tabla, "activo");
        if (!tieneActivo) {
            String addSql = "ALTER TABLE " + tabla + " ADD COLUMN activo TINYINT(1) NOT NULL DEFAULT 1";
            try (PreparedStatement stmt = conn.prepareStatement(addSql)) {
                stmt.executeUpdate();
            }

            // Migracion una sola vez: solo cuando la columna activo acaba de ser creada.
            boolean tieneEstado = existeColumna(conn, tabla, "estado");
            if (tieneEstado) {
                String syncSql = "UPDATE " + tabla + " SET activo = CASE " +
                        "WHEN estado IS NULL THEN 1 " +
                        "WHEN LOWER(estado) LIKE '%inactivo%' OR LOWER(estado) LIKE '%no disponible%' OR LOWER(estado) LIKE '%ocupada%' THEN 0 " +
                        "ELSE 1 END";
                try (PreparedStatement stmt = conn.prepareStatement(syncSql)) {
                    stmt.executeUpdate();
                }
            }
        }
    }

    private boolean existeColumna(Connection conn, String tabla, String columna) throws SQLException {
        String sql = "SELECT COUNT(*) FROM information_schema.columns " +
                "WHERE table_schema = DATABASE() AND table_name = ? AND column_name = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, tabla);
            stmt.setString(2, columna);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next() && rs.getInt(1) > 0;
            }
        }
    }

    public void cerrar() {
        try {
            if (conexion != null && !conexion.isClosed()) {
                conexion.close();
                System.out.println("Conexion cerrada");
            }
        } catch (SQLException e) {
            System.err.println("Error cerrando conexion: " + e.getMessage());
        }
    }

    public boolean verificarConexion() {
        try {
            Connection conn = obtenerConexion();
            return conn != null && !conn.isClosed();
        } catch (SQLException e) {
            System.err.println("Error verificando conexion: " + e.getMessage());
            return false;
        }
    }
}

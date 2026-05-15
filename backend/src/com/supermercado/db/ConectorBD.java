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
        try (InputStream entrada = getClass().getClassLoader().getResourceAsStream("db/inicializar_bd.sql")) {
            if (entrada == null) return;
            String sql = new String(entrada.readAllBytes());
            // Simple split by semicolon; ignores advanced cases but sufficient for our script
            String[] statements = sql.split(";\\s*\\r?\\n");
            for (String stmt : statements) {
                String s = stmt.trim();
                if (s.isEmpty()) continue;
                try (PreparedStatement ps = conn.prepareStatement(s)) {
                    ps.execute();
                }
            }
        } catch (IOException e) {
            throw new SQLException("Error leyendo script de inicializacion H2", e);
        }
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

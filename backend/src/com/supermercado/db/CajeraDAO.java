package com.supermercado.db;

import com.supermercado.model.Cajera;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object (DAO) para operaciones CRUD sobre cajeras.
 * Gestiona la persistencia y estado de cajeras en la base de datos.
 */
public class CajeraDAO {
    private static final String ESTADO_ACTIVO = "Activo / Disponible";

    private final ConectorBD conector;

    public CajeraDAO() {
        this.conector = ConectorBD.obtenerInstancia();
    }

    // Recupera todas las cajeras de la base de datos ordenadas por ID
    public List<Cajera> obtenerTodas() throws SQLException {
        List<Cajera> cajeras = new ArrayList<>();
        String sql = "SELECT id, nombre, activo FROM cajeras ORDER BY id";

        try (Connection conn = conector.obtenerConexion();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                cajeras.add(new Cajera(
                        rs.getInt("id"),
                        rs.getString("nombre"),
                    rs.getInt("activo") == 1 ? ESTADO_ACTIVO : "Inactivo / No disponible"
                ));
            }
        }

        return cajeras;
    }

    public Cajera obtenerPorId(int id) throws SQLException {
        String sql = "SELECT id, nombre, activo FROM cajeras WHERE id = ?";

        try (Connection conn = conector.obtenerConexion();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return new Cajera(
                            rs.getInt("id"),
                            rs.getString("nombre"),
                            rs.getInt("activo") == 1 ? ESTADO_ACTIVO : "Inactivo / No disponible"
                    );
                }
            }
        }

        return null;
    }

    public int guardar(String nombre, String estado) throws SQLException {
        String sql = "INSERT INTO cajeras (nombre, activo) VALUES (?, ?)";

        try (Connection conn = conector.obtenerConexion();
             PreparedStatement stmt = conn.prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, nombre);
            stmt.setInt(2, normalizarActivo(estado));
            stmt.executeUpdate();

            try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    return generatedKeys.getInt(1);
                }
            }
        }

        return -1;
    }

    public void actualizar(Cajera cajera) throws SQLException {
        String sql = "UPDATE cajeras SET nombre = ?, activo = ? WHERE id = ?";

        try (Connection conn = conector.obtenerConexion();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, cajera.getNombre());
            stmt.setInt(2, normalizarActivo(cajera.getEstado()));
            stmt.setInt(3, cajera.getId());
            stmt.executeUpdate();
        }
    }

    public void eliminar(int id) throws SQLException {
        String sql = "UPDATE cajeras SET activo = 0 WHERE id = ?";

        try (Connection conn = conector.obtenerConexion();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            stmt.executeUpdate();
        }
    }

    public void activar(int id) throws SQLException {
        String sql = "UPDATE cajeras SET activo = 1 WHERE id = ?";

        try (Connection conn = conector.obtenerConexion();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            stmt.executeUpdate();
        }
    }

    private int normalizarActivo(String estado) {
        if (estado == null) {
            return 1;
        }
        String valor = estado.trim().toLowerCase();
        if (valor.contains("inactivo") || valor.contains("no disponible") || valor.contains("ocupada")) {
            return 0;
        }
        return 1;
    }

    public void eliminarFisico(int id) throws SQLException {
        String checkSql = "SELECT COUNT(*) FROM transacciones WHERE id_cajera = ?";
        try (Connection conn = conector.obtenerConexion();
             PreparedStatement checkStmt = conn.prepareStatement(checkSql)) {
            checkStmt.setInt(1, id);
            try (ResultSet rs = checkStmt.executeQuery()) {
                if (rs.next() && rs.getInt(1) > 0) {
                    throw new SQLException("No se puede eliminar la cajera porque existen transacciones que la referencian.");
                }
            }
        }

        String sql = "DELETE FROM cajeras WHERE id = ?";
        try (Connection conn = conector.obtenerConexion();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            stmt.executeUpdate();
        }
    }

    public List<Cajera> obtenerORegistrar(int cantidad) throws SQLException {
        List<Cajera> cajeras = obtenerTodas();

        for (int i = cajeras.size() + 1; i <= cantidad; i++) {
            int id = guardar("Cajera " + i, ESTADO_ACTIVO);
            if (id != -1) {
                cajeras.add(new Cajera(id, "Cajera " + i, ESTADO_ACTIVO));
            }
        }

        if (cajeras.size() < cantidad) {
            throw new SQLException("No fue posible registrar la cantidad solicitada de cajeras");
        }

        if (cajeras.size() > cantidad) {
            return new ArrayList<>(cajeras.subList(0, cantidad));
        }

        return cajeras;
    }
}
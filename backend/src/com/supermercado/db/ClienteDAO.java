package com.supermercado.db;

import com.supermercado.model.Cliente;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object (DAO) para operaciones CRUD sobre clientes.
 * Gestiona la persistencia de clientes en la base de datos utilizando soft-delete (columna activo).
 */
public class ClienteDAO {
    private final ConectorBD conector;

    public ClienteDAO() {
        this.conector = ConectorBD.obtenerInstancia();
    }

    // Inserta un nuevo cliente y retorna el ID generado
    public int guardar(String nombre) throws SQLException {
        String sql = "INSERT INTO clientes (nombre) VALUES (?)";
        try (Connection conn = conector.obtenerConexion();
             PreparedStatement stmt = conn.prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, nombre);
            stmt.executeUpdate();

            try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    return generatedKeys.getInt(1);
                }
            }
        }
        return -1;
    }

    public Cliente obtenerPorId(int id) throws SQLException {
        String sql = "SELECT id, nombre FROM clientes WHERE id = ?";
        try (Connection conn = conector.obtenerConexion();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return new Cliente(rs.getInt("id"), rs.getString("nombre"));
                }
            }
        }
        return null;
    }

    // Recupera todos los clientes activos ordenados por ID
    public List<Cliente> obtenerTodos() throws SQLException {
        List<Cliente> clientes = new ArrayList<>();
        String sql = "SELECT id, nombre FROM clientes ORDER BY id";
        try (Connection conn = conector.obtenerConexion();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                clientes.add(new Cliente(rs.getInt("id"), rs.getString("nombre")));
            }
        }
        return clientes;
    }

    public void actualizar(Cliente cliente) throws SQLException {
        String sql = "UPDATE clientes SET nombre = ? WHERE id = ?";
        try (Connection conn = conector.obtenerConexion();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, cliente.getNombre());
            stmt.setInt(2, cliente.getId());
            stmt.executeUpdate();
        }
    }

    public void eliminar(int id) throws SQLException {
        String sql = "UPDATE clientes SET activo = 0 WHERE id = ?";
        try (Connection conn = conector.obtenerConexion();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            stmt.executeUpdate();
        }
    }

    public void activar(int id) throws SQLException {
        String sql = "UPDATE clientes SET activo = 1 WHERE id = ?";
        try (Connection conn = conector.obtenerConexion();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            stmt.executeUpdate();
        }
    }

    public void eliminarFisico(int id) throws SQLException {
        String checkSql = "SELECT COUNT(*) FROM transacciones WHERE id_cliente = ?";
        try (Connection conn = conector.obtenerConexion();
             PreparedStatement checkStmt = conn.prepareStatement(checkSql)) {
            checkStmt.setInt(1, id);
            try (ResultSet rs = checkStmt.executeQuery()) {
                if (rs.next() && rs.getInt(1) > 0) {
                    throw new SQLException("No se puede eliminar el cliente porque existen transacciones que lo referencian.");
                }
            }
        }

        String sql = "DELETE FROM clientes WHERE id = ?";
        try (Connection conn = conector.obtenerConexion();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            stmt.executeUpdate();
        }
    }
}
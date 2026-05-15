package com.supermercado.db;

import com.supermercado.model.Producto;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object (DAO) para operaciones CRUD sobre productos.
 * Gestiona la persistencia de productos en la base de datos utilizando soft-delete (columna activo).
 */
public class ProductoDAO {
    private ConectorBD conector;

    public ProductoDAO() {
        this.conector = ConectorBD.obtenerInstancia();
    }

    // Inserta un nuevo producto en la base de datos
    public void guardar(Producto producto) throws SQLException {
        String sql = "INSERT INTO productos (nombre, precio, tiempo_procesamiento_segundos) VALUES (?, ?, ?)";
        try (Connection conn = conector.obtenerConexion();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, producto.getNombre());
            stmt.setDouble(2, producto.getPrecio());
            stmt.setInt(3, producto.getTiempoProcesamientoSegundos());
            stmt.executeUpdate();
        }
    }

    // Actualiza los datos de un producto existente
    public void actualizar(Producto producto) throws SQLException {
        String sql = "UPDATE productos SET nombre = ?, precio = ?, tiempo_procesamiento_segundos = ? WHERE id = ?";
        try (Connection conn = conector.obtenerConexion();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, producto.getNombre());
            stmt.setDouble(2, producto.getPrecio());
            stmt.setInt(3, producto.getTiempoProcesamientoSegundos());
            stmt.setInt(4, producto.getId());
            stmt.executeUpdate();
        }
    }

    public void eliminar(int id) throws SQLException {
        String sql = "UPDATE productos SET activo = 0 WHERE id = ?";
        try (Connection conn = conector.obtenerConexion();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            stmt.executeUpdate();
        }
    }

    public void activar(int id) throws SQLException {
        String sql = "UPDATE productos SET activo = 1 WHERE id = ?";
        try (Connection conn = conector.obtenerConexion();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            stmt.executeUpdate();
        }
    }

    public void eliminarFisico(int id) throws SQLException {
        String checkSql = "SELECT COUNT(*) FROM detalle_transacciones WHERE id_producto = ?";
        try (Connection conn = conector.obtenerConexion();
             PreparedStatement checkStmt = conn.prepareStatement(checkSql)) {
            checkStmt.setInt(1, id);
            try (ResultSet rs = checkStmt.executeQuery()) {
                if (rs.next() && rs.getInt(1) > 0) {
                    throw new SQLException("No se puede eliminar el producto porque existen transacciones que lo referencian.");
                }
            }
        }

        String sql = "DELETE FROM productos WHERE id = ?";
        try (Connection conn = conector.obtenerConexion();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            stmt.executeUpdate();
        }
    }

    // Recupera todos los productos activos de la base de datos
    public List<Producto> obtenerTodos() throws SQLException {
        List<Producto> productos = new ArrayList<>();
        String sql = "SELECT id, nombre, precio, tiempo_procesamiento_segundos FROM productos";
        try (Connection conn = conector.obtenerConexion();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                Producto p = new Producto(
                        rs.getInt("id"),
                        rs.getString("nombre"),
                        rs.getDouble("precio"),
                        rs.getInt("tiempo_procesamiento_segundos")
                );
                productos.add(p);
            }
        }
        return productos;
    }

    public Producto obtenerPorId(int id) throws SQLException {
        String sql = "SELECT id, nombre, precio, tiempo_procesamiento_segundos FROM productos WHERE id = ?";
        try (Connection conn = conector.obtenerConexion();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return new Producto(
                            rs.getInt("id"),
                            rs.getString("nombre"),
                            rs.getDouble("precio"),
                            rs.getInt("tiempo_procesamiento_segundos")
                    );
                }
            }
        }
        return null;
    }
}

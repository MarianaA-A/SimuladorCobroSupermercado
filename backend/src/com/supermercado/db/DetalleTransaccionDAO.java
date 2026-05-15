package com.supermercado.db;

import com.supermercado.model.DetalleProductoCobrado;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Encapsula las operaciones de persistencia para el detalle de cada transaccion.
 */
public class DetalleTransaccionDAO {
    private final ConectorBD conector;

    public DetalleTransaccionDAO() {
        this.conector = ConectorBD.obtenerInstancia();
    }

    public int guardar(DetalleProductoCobrado detalle, int idTransaccion) throws SQLException {
        String sql = "INSERT INTO detalle_transacciones (id_transaccion, id_producto, nombre_producto, costo_producto, tiempo_procesamiento_segundos) " +
                     "VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = conector.obtenerConexion();
             PreparedStatement stmt = conn.prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS)) {
            stmt.setInt(1, idTransaccion);
            stmt.setInt(2, detalle.getIdProducto());
            stmt.setString(3, detalle.getNombreProducto());
            stmt.setDouble(4, detalle.getCostoProducto());
            stmt.setInt(5, detalle.getTiempoProcesamientoSegundos());
            stmt.executeUpdate();

            try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    return generatedKeys.getInt(1);
                }
            }
        }
        return -1;
    }

    public DetalleProductoCobrado obtenerPorId(int id) throws SQLException {
        String sql = "SELECT id, id_transaccion, id_producto, nombre_producto, costo_producto, tiempo_procesamiento_segundos FROM detalle_transacciones WHERE id = ?";
        try (Connection conn = conector.obtenerConexion();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return new DetalleProductoCobrado(
                            rs.getInt("id"),
                            rs.getInt("id_transaccion"),
                            rs.getInt("id_producto"),
                            rs.getString("nombre_producto"),
                            rs.getDouble("costo_producto"),
                            rs.getInt("tiempo_procesamiento_segundos")
                    );
                }
            }
        }
        return null;
    }

    public List<DetalleProductoCobrado> obtenerTodos() throws SQLException {
        List<DetalleProductoCobrado> detalles = new ArrayList<>();
        String sql = "SELECT id, id_transaccion, id_producto, nombre_producto, costo_producto, tiempo_procesamiento_segundos FROM detalle_transacciones ORDER BY id";
        try (Connection conn = conector.obtenerConexion();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                detalles.add(new DetalleProductoCobrado(
                        rs.getInt("id"),
                        rs.getInt("id_transaccion"),
                        rs.getInt("id_producto"),
                        rs.getString("nombre_producto"),
                        rs.getDouble("costo_producto"),
                        rs.getInt("tiempo_procesamiento_segundos")
                ));
            }
        }
        return detalles;
    }

    public void actualizar(DetalleProductoCobrado detalle) throws SQLException {
        String sql = "UPDATE detalle_transacciones SET id_transaccion = ?, id_producto = ?, nombre_producto = ?, costo_producto = ?, tiempo_procesamiento_segundos = ? WHERE id = ?";
        try (Connection conn = conector.obtenerConexion();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, detalle.getIdTransaccion());
            stmt.setInt(2, detalle.getIdProducto());
            stmt.setString(3, detalle.getNombreProducto());
            stmt.setDouble(4, detalle.getCostoProducto());
            stmt.setInt(5, detalle.getTiempoProcesamientoSegundos());
            stmt.setInt(6, detalle.getId());
            stmt.executeUpdate();
        }
    }

    public void eliminar(int id) throws SQLException {
        String sql = "DELETE FROM detalle_transacciones WHERE id = ?";
        try (Connection conn = conector.obtenerConexion();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            stmt.executeUpdate();
        }
    }
}
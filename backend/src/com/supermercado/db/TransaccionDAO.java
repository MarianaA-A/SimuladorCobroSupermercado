package com.supermercado.db;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Encapsula las operaciones de persistencia de la tabla transacciones.
 */
public class TransaccionDAO {
    private ConectorBD conector;

    public TransaccionDAO() {
        this.conector = ConectorBD.obtenerInstancia();
    }

    public int guardar(int idCliente, int idCajera, double totalCliente, long tiempoTotalSegundos) throws SQLException {
        String sql = "INSERT INTO transacciones (id_cliente, id_cajera, total_cliente, tiempo_total_segundos, fecha_fin) " +
                     "VALUES (?, ?, ?, ?, NOW())";
        try (Connection conn = conector.obtenerConexion();
             PreparedStatement stmt = conn.prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS)) {
            stmt.setInt(1, idCliente);
            stmt.setInt(2, idCajera);
            stmt.setDouble(3, totalCliente);
            stmt.setLong(4, tiempoTotalSegundos);
            stmt.executeUpdate();

            try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    return generatedKeys.getInt(1);
                }
            }
        }
        return -1;
    }

    public ResultSet obtenerPorId(int idTransaccion) throws SQLException {
        String sql = "SELECT id, id_cliente, id_cajera, total_cliente, tiempo_total_segundos, fecha_inicio, fecha_fin " +
                     "FROM transacciones WHERE id = ?";
        Connection conn = conector.obtenerConexion();
        PreparedStatement stmt = conn.prepareStatement(sql);
        stmt.setInt(1, idTransaccion);
        return stmt.executeQuery();
    }

    public ResultSet obtenerTodas() throws SQLException {
        String sql = "SELECT id, id_cliente, id_cajera, total_cliente, tiempo_total_segundos, fecha_inicio, fecha_fin FROM transacciones";
        Connection conn = conector.obtenerConexion();
        PreparedStatement stmt = conn.prepareStatement(sql);
        return stmt.executeQuery();
    }

    public void actualizarFechaFin(int idTransaccion) throws SQLException {
        String sql = "UPDATE transacciones SET fecha_fin = NOW() WHERE id = ?";
        try (Connection conn = conector.obtenerConexion();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, idTransaccion);
            stmt.executeUpdate();
        }
    }

    public void eliminar(int idTransaccion) throws SQLException {
        String sql = "DELETE FROM transacciones WHERE id = ?";
        try (Connection conn = conector.obtenerConexion();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, idTransaccion);
            stmt.executeUpdate();
        }
    }

    public void guardarDetalle(int idTransaccion, int idProducto, String nombreProducto,
                               double costoProducto, int tiempoSegundos) throws SQLException {
        String sql = "INSERT INTO detalle_transacciones (id_transaccion, id_producto, nombre_producto, " +
                     "costo_producto, tiempo_procesamiento_segundos) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = conector.obtenerConexion();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, idTransaccion);
            stmt.setInt(2, idProducto);
            stmt.setString(3, nombreProducto);
            stmt.setDouble(4, costoProducto);
            stmt.setInt(5, tiempoSegundos);
            stmt.executeUpdate();
        }
    }
}

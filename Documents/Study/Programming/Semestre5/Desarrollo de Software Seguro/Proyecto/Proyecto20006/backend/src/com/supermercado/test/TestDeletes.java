package com.supermercado.test;

import com.supermercado.db.CajeraDAO;
import com.supermercado.db.ClienteDAO;
import com.supermercado.db.ConectorBD;
import com.supermercado.db.ProductoDAO;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Clase de prueba para validar el comportamiento de eliminación (soft-delete y hard-delete).
 * Verifica que el sistema gestione correctamente la desactivación y eliminación de productos, clientes y cajeras.
 */
public class TestDeletes {
    public static void main(String[] args) throws Exception {
        asegurarEsquema();

        ProductoDAO productoDAO = new ProductoDAO();
        ClienteDAO clienteDAO = new ClienteDAO();
        CajeraDAO cajeraDAO = new CajeraDAO();

        System.out.println("--- Estado inicial (primeros registros) ---");
        mostrarProductos();
        mostrarClientes();
        mostrarCajeras();

        Integer idProducto = primerIdProducto();
        if (idProducto != null) {
            System.out.println("\nDesactivando producto id=" + idProducto);
            productoDAO.eliminar(idProducto);
            mostrarProducto(idProducto);
            System.out.println("Intentando eliminar fisicamente producto id=" + idProducto);
            try {
                productoDAO.eliminarFisico(idProducto);
                System.out.println("Eliminación física OK para producto " + idProducto);
            } catch (SQLException e) {
                System.out.println("Fallo eliminación física producto: " + e.getMessage());
            }
        }

        Integer idCliente = primerIdCliente();
        if (idCliente != null) {
            System.out.println("\nDesactivando cliente id=" + idCliente);
            clienteDAO.eliminar(idCliente);
            mostrarCliente(idCliente);
            System.out.println("Intentando eliminar fisicamente cliente id=" + idCliente);
            try {
                clienteDAO.eliminarFisico(idCliente);
                System.out.println("Eliminación física OK para cliente " + idCliente);
            } catch (SQLException e) {
                System.out.println("Fallo eliminación física cliente: " + e.getMessage());
            }
        }

        Integer idCajera = primerIdCajera();
        if (idCajera != null) {
            System.out.println("\nDesactivando cajera id=" + idCajera);
            cajeraDAO.eliminar(idCajera);
            mostrarCajera(idCajera);
            System.out.println("Intentando eliminar fisicamente cajera id=" + idCajera);
            try {
                cajeraDAO.eliminarFisico(idCajera);
                System.out.println("Eliminación física OK para cajera " + idCajera);
            } catch (SQLException e) {
                System.out.println("Fallo eliminación física cajera: " + e.getMessage());
            }
        }

        System.out.println("\n--- Estado final (primeros registros) ---");
        mostrarProductos();
        mostrarClientes();
        mostrarCajeras();
    }

    private static void asegurarEsquema() throws SQLException {
        try (Connection conn = ConectorBD.obtenerInstancia().obtenerConexion()) {
            try (PreparedStatement check = conn.prepareStatement(
                    "SELECT COUNT(*) FROM information_schema.columns WHERE table_schema = DATABASE() AND table_name = 'productos' AND column_name = 'activo'")) {
                try (ResultSet rs = check.executeQuery()) {
                    if (rs.next() && rs.getInt(1) == 0) {
                        try (PreparedStatement add = conn.prepareStatement(
                                "ALTER TABLE productos ADD COLUMN activo TINYINT(1) NOT NULL DEFAULT 1")) {
                            add.executeUpdate();
                            System.out.println("Columna 'activo' agregada a productos");
                        }
                    }
                }
            }
            try (PreparedStatement check = conn.prepareStatement(
                    "SELECT COUNT(*) FROM information_schema.columns WHERE table_schema = DATABASE() AND table_name = 'clientes' AND column_name = 'activo'")) {
                try (ResultSet rs = check.executeQuery()) {
                    if (rs.next() && rs.getInt(1) == 0) {
                        try (PreparedStatement add = conn.prepareStatement(
                                "ALTER TABLE clientes ADD COLUMN activo TINYINT(1) NOT NULL DEFAULT 1")) {
                            add.executeUpdate();
                            System.out.println("Columna 'activo' agregada a clientes");
                        }
                    }
                }
            }

            try (PreparedStatement check = conn.prepareStatement(
                    "SELECT COUNT(*) FROM information_schema.columns WHERE table_schema = DATABASE() AND table_name = 'cajeras' AND column_name = 'activo'")) {
                try (ResultSet rs = check.executeQuery()) {
                    if (rs.next() && rs.getInt(1) == 0) {
                        try (PreparedStatement add = conn.prepareStatement(
                                "ALTER TABLE cajeras ADD COLUMN activo TINYINT(1) NOT NULL DEFAULT 1")) {
                            add.executeUpdate();
                            System.out.println("Columna 'activo' agregada a cajeras");
                        }
                    }
                }
            }
        } catch (SQLException e) {
            System.out.println("Error al asegurar esquema: " + e.getMessage());
            throw e;
        }
    }

    private static void mostrarProductos() throws SQLException {
        try (Connection conn = ConectorBD.obtenerInstancia().obtenerConexion();
             PreparedStatement stmt = conn.prepareStatement("SELECT id, nombre, COALESCE(activo, 1) AS activo FROM productos ORDER BY id LIMIT 10");
             ResultSet rs = stmt.executeQuery()) {
            System.out.println("Productos:");
            while (rs.next()) {
                System.out.println("  #" + rs.getInt("id") + " - " + rs.getString("nombre") + " - " + (rs.getInt("activo") == 1 ? "Activo" : "Inactivo"));
            }
        }
    }

    private static void mostrarClientes() throws SQLException {
        try (Connection conn = ConectorBD.obtenerInstancia().obtenerConexion();
             PreparedStatement stmt = conn.prepareStatement("SELECT id, nombre, COALESCE(activo, 1) AS activo FROM clientes ORDER BY id LIMIT 10");
             ResultSet rs = stmt.executeQuery()) {
            System.out.println("Clientes:");
            while (rs.next()) {
                System.out.println("  #" + rs.getInt("id") + " - " + rs.getString("nombre") + " - " + (rs.getInt("activo") == 1 ? "Activo" : "Inactivo"));
            }
        }
    }

    private static void mostrarCajeras() throws SQLException {
        try (Connection conn = ConectorBD.obtenerInstancia().obtenerConexion();
             PreparedStatement stmt = conn.prepareStatement("SELECT id, nombre, COALESCE(activo, 1) AS activo FROM cajeras ORDER BY id LIMIT 10");
             ResultSet rs = stmt.executeQuery()) {
            System.out.println("Cajeras:");
            while (rs.next()) {
                System.out.println("  #" + rs.getInt("id") + " - " + rs.getString("nombre") + " - " + (rs.getInt("activo") == 1 ? "Activo" : "Inactivo"));
            }
        }
    }

    private static void mostrarProducto(int id) throws SQLException {
        try (Connection conn = ConectorBD.obtenerInstancia().obtenerConexion();
             PreparedStatement stmt = conn.prepareStatement("SELECT id, nombre, COALESCE(activo, 1) AS activo FROM productos WHERE id = ?")) {
            stmt.setInt(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    System.out.println("Producto #" + rs.getInt("id") + " - " + rs.getString("nombre") + " - " + (rs.getInt("activo") == 1 ? "Activo" : "Inactivo"));
                } else {
                    System.out.println("Producto #" + id + " no encontrado (posible eliminación física)");
                }
            }
        }
    }

    private static void mostrarCliente(int id) throws SQLException {
        try (Connection conn = ConectorBD.obtenerInstancia().obtenerConexion();
             PreparedStatement stmt = conn.prepareStatement("SELECT id, nombre, COALESCE(activo, 1) AS activo FROM clientes WHERE id = ?")) {
            stmt.setInt(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    System.out.println("Cliente #" + rs.getInt("id") + " - " + rs.getString("nombre") + " - " + (rs.getInt("activo") == 1 ? "Activo" : "Inactivo"));
                } else {
                    System.out.println("Cliente #" + id + " no encontrado (posible eliminación física)");
                }
            }
        }
    }

    private static void mostrarCajera(int id) throws SQLException {
        try (Connection conn = ConectorBD.obtenerInstancia().obtenerConexion();
             PreparedStatement stmt = conn.prepareStatement("SELECT id, nombre, COALESCE(activo, 1) AS activo FROM cajeras WHERE id = ?")) {
            stmt.setInt(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    System.out.println("Cajera #" + rs.getInt("id") + " - " + rs.getString("nombre") + " - " + (rs.getInt("activo") == 1 ? "Activo" : "Inactivo"));
                } else {
                    System.out.println("Cajera #" + id + " no encontrada (posible eliminación física)");
                }
            }
        }
    }

    private static Integer primerIdProducto() throws SQLException {
        try (Connection conn = ConectorBD.obtenerInstancia().obtenerConexion();
             PreparedStatement stmt = conn.prepareStatement("SELECT id FROM productos ORDER BY id LIMIT 1");
             ResultSet rs = stmt.executeQuery()) {
            if (rs.next()) return rs.getInt(1);
        }
        return null;
    }

    private static Integer primerIdCliente() throws SQLException {
        try (Connection conn = ConectorBD.obtenerInstancia().obtenerConexion();
             PreparedStatement stmt = conn.prepareStatement("SELECT id FROM clientes ORDER BY id LIMIT 1");
             ResultSet rs = stmt.executeQuery()) {
            if (rs.next()) return rs.getInt(1);
        }
        return null;
    }

    private static Integer primerIdCajera() throws SQLException {
        try (Connection conn = ConectorBD.obtenerInstancia().obtenerConexion();
             PreparedStatement stmt = conn.prepareStatement("SELECT id FROM cajeras ORDER BY id LIMIT 1");
             ResultSet rs = stmt.executeQuery()) {
            if (rs.next()) return rs.getInt(1);
        }
        return null;
    }
}

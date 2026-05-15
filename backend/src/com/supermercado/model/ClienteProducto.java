package com.supermercado.model;

/**
 * Representa la relación entre un cliente y un producto comprado, incluyendo la cantidad.
 */
public class ClienteProducto {
    private final Producto producto;
    private final int cantidad;

    public ClienteProducto(Producto producto) {
        this(producto, 1);
    }

    public ClienteProducto(Producto producto, int cantidad) {
        if (producto == null) {
            throw new IllegalArgumentException("El producto no puede ser nulo");
        }
        if (cantidad <= 0) {
            throw new IllegalArgumentException("La cantidad debe ser mayor que cero");
        }
        this.producto = producto;
        this.cantidad = cantidad;
    }

    public Producto getProducto() {
        return producto;
    }

    public int getCantidad() {
        return cantidad;
    }

    public double getCostoTotal() {
        return producto.getPrecio() * cantidad;
    }

    public int getTiempoTotalSegundos() {
        return producto.getTiempoProcesamientoSegundos() * cantidad;
    }
}

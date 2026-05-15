package com.supermercado.model;

/**
 * Representa un detalle individual de un producto cobrado durante una transacción.
 * Incluye información del producto, su costo y tiempo de procesamiento.
 */
public class DetalleProductoCobrado {
    private final int id;
    private final int idTransaccion;
    private final int idProducto;
    private final String nombreProducto;
    private final double costoProducto;
    private final int tiempoProcesamientoSegundos;

    public DetalleProductoCobrado(int idProducto, String nombreProducto, double costoProducto, int tiempoProcesamientoSegundos) {
        this(0, 0, idProducto, nombreProducto, costoProducto, tiempoProcesamientoSegundos);
    }

    public DetalleProductoCobrado(int id, int idTransaccion, int idProducto, String nombreProducto, double costoProducto, int tiempoProcesamientoSegundos) {
        this.id = id;
        this.idTransaccion = idTransaccion;
        this.idProducto = idProducto;
        this.nombreProducto = nombreProducto;
        this.costoProducto = costoProducto;
        this.tiempoProcesamientoSegundos = tiempoProcesamientoSegundos;
    }

    public int getId() {
        return id;
    }

    public int getIdTransaccion() {
        return idTransaccion;
    }

    public int getIdProducto() {
        return idProducto;
    }

    public String getNombreProducto() {
        return nombreProducto;
    }

    public double getCostoProducto() {
        return costoProducto;
    }

    public int getTiempoProcesamientoSegundos() {
        return tiempoProcesamientoSegundos;
    }
}

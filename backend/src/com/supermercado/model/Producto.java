package com.supermercado.model;

/**
 * Representa un producto del supermercado con su precio y tiempo de procesamiento.
 */
public class Producto {
    private final int id;
    private final String nombre;
    private final double precio;
    private final int tiempoProcesamientoSegundos;

    public Producto(int id, String nombre, double precio, int tiempoProcesamientoSegundos) {
        this.id = id;
        this.nombre = nombre;
        this.precio = precio;
        this.tiempoProcesamientoSegundos = tiempoProcesamientoSegundos;
    }

    public int getId() {
        return id;
    }

    public String getNombre() {
        return nombre;
    }

    public double getPrecio() {
        return precio;
    }

    public int getTiempoProcesamientoSegundos() {
        return tiempoProcesamientoSegundos;
    }
}

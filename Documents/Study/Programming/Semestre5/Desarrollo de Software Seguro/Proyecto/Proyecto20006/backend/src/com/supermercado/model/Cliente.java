package com.supermercado.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Modela un cliente y los productos que lleva en su compra.
 */
public class Cliente {
    private final int id;
    private final String nombre;
    private final List<ClienteProducto> clienteProductos;

    public Cliente(int id) {
        this(id, null);
    }

    public Cliente(int id, String nombre) {
        this.id = id;
        this.nombre = nombre;
        this.clienteProductos = new ArrayList<>();
    }

    public int getId() {
        return id;
    }

    public String getNombre() {
        return nombre;
    }

    public void agregarProducto(Producto producto) {
        this.clienteProductos.add(new ClienteProducto(producto));
    }

    public void agregarProducto(Producto producto, int cantidad) {
        this.clienteProductos.add(new ClienteProducto(producto, cantidad));
    }

    public List<Producto> getProductos() {
        return clienteProductos.stream()
                .map(ClienteProducto::getProducto)
                .collect(Collectors.toUnmodifiableList());
    }

    public List<ClienteProducto> getClienteProductos() {
        return Collections.unmodifiableList(clienteProductos);
    }

    public double calcularTotalCompra() {
        return clienteProductos.stream().mapToDouble(ClienteProducto::getCostoTotal).sum();
    }

    public int calcularTiempoTotalSegundos() {
        return clienteProductos.stream().mapToInt(ClienteProducto::getTiempoTotalSegundos).sum();
    }
}

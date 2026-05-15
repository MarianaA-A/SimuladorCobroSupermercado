package com.supermercado.model;

import java.util.Collections;
import java.util.List;

/**
 * Encapsula el resultado del cobro de un cliente en una cajera en particular.
 * Contiene los detalles de productos cobrados, el total y el tiempo de procesamiento.
 */
public class ResultadoCobro {
    private final int idCliente;
    private final int idCajera;
    private final List<DetalleProductoCobrado> productosCobrados;
    private final double totalCliente;
    private final long tiempoTotalClienteSegundos;

    public ResultadoCobro(
            int idCliente,
            int idCajera,
            List<DetalleProductoCobrado> productosCobrados,
            double totalCliente,
            long tiempoTotalClienteSegundos
    ) {
        this.idCliente = idCliente;
        this.idCajera = idCajera;
        this.productosCobrados = List.copyOf(productosCobrados);
        this.totalCliente = totalCliente;
        this.tiempoTotalClienteSegundos = tiempoTotalClienteSegundos;
    }

    public int getIdCliente() {
        return idCliente;
    }

    public int getIdCajera() {
        return idCajera;
    }

    public List<DetalleProductoCobrado> getProductosCobrados() {
        return Collections.unmodifiableList(productosCobrados);
    }

    public double getTotalCliente() {
        return totalCliente;
    }

    public long getTiempoTotalClienteSegundos() {
        return tiempoTotalClienteSegundos;
    }
}

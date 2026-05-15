package com.supermercado.model;

import java.time.Duration;
import java.time.Instant;
import java.util.List;

/**
 * Resumen de la actividad de una cajera durante la simulación.
 * Incluye los clientes atendidos, total recaudado y tiempos de procesamiento.
 */
public class ResumenCajera {
    private final int idCajera;
    private final List<Integer> clientesAtendidos;
    private final int cantidadClientesAtendidos;
    private final double totalRecaudado;
    private final long tiempoProcesamientoSegundos;
    private final Instant inicioProcesamiento;
    private final Instant finProcesamiento;

    public ResumenCajera(
            int idCajera,
            List<Integer> clientesAtendidos,
            double totalRecaudado,
            Instant inicioProcesamiento,
            Instant finProcesamiento
    ) {
        this.idCajera = idCajera;
        this.clientesAtendidos = List.copyOf(clientesAtendidos);
        this.cantidadClientesAtendidos = this.clientesAtendidos.size();
        this.totalRecaudado = totalRecaudado;
        this.inicioProcesamiento = inicioProcesamiento;
        this.finProcesamiento = finProcesamiento;
        this.tiempoProcesamientoSegundos = Duration.between(inicioProcesamiento, finProcesamiento).toSeconds();
    }

    public static ResumenCajera vacio(int idCajera) {
        Instant ahora = Instant.now();
        return new ResumenCajera(idCajera, List.of(), 0.0, ahora, ahora);
    }

    public int getIdCajera() {
        return idCajera;
    }

    public List<Integer> getClientesAtendidos() {
        return clientesAtendidos;
    }

    public int getCantidadClientesAtendidos() {
        return cantidadClientesAtendidos;
    }

    public double getTotalRecaudado() {
        return totalRecaudado;
    }

    public long getTiempoProcesamientoSegundos() {
        return tiempoProcesamientoSegundos;
    }

    public Instant getInicioProcesamiento() {
        return inicioProcesamiento;
    }

    public Instant getFinProcesamiento() {
        return finProcesamiento;
    }
}

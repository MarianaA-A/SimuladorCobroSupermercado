package com.supermercado.model;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

/**
 * Ejecuta el cobro de clientes como tarea concurrente y devuelve un resumen final.
 */
public class Cajera implements Callable<ResumenCajera> {
    private final int id;
    private final String nombre;
    private final String estado;
    private final BlockingQueue<Cliente> colaClientes;
    private final List<ResultadoCobro> resultadosCompartidos;

    public Cajera(int id, String nombre, String estado) {
        this(id, nombre, estado, null, null);
    }

    public Cajera(int id, String nombre, String estado, BlockingQueue<Cliente> colaClientes, List<ResultadoCobro> resultadosCompartidos) {
        this.id = id;
        this.nombre = nombre;
        this.estado = estado;
        this.colaClientes = colaClientes;
        this.resultadosCompartidos = resultadosCompartidos;
    }

    public int getId() {
        return id;
    }

    public String getNombre() {
        return nombre;
    }

    public String getEstado() {
        return estado;
    }

    @Override
    public ResumenCajera call() {
        // Registrar tiempos de inicio y fin de la cajera
        Instant inicioCajera = null;
        Instant finCajera = null;
        List<Integer> clientesAtendidos = new ArrayList<>();
        double totalRecaudado = 0.0;

        if (colaClientes == null || resultadosCompartidos == null) {
            throw new IllegalStateException("La cajera no fue inicializada para ejecutar cobros");
        }

        try {
            // Procesar clientes desde la cola compartida con timeout
            while (true) {
                Cliente cliente = colaClientes.poll(250, TimeUnit.MILLISECONDS);
                if (cliente == null) {
                    break;  // No hay más clientes
                }

                if (inicioCajera == null) {
                    inicioCajera = Instant.now();
                }

                ResultadoCobro resultado = procesarCliente(cliente);
                resultadosCompartidos.add(resultado);

                clientesAtendidos.add(cliente.getId());
                totalRecaudado += resultado.getTotalCliente();
                finCajera = Instant.now();
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            if (inicioCajera != null && finCajera == null) {
                finCajera = Instant.now();
            }
        }

        // Si no se atendió ningún cliente, retornar resumen vacío
        if (inicioCajera == null) {
            return ResumenCajera.vacio(id);
        }

        if (finCajera == null) {
            finCajera = Instant.now();
        }

        return new ResumenCajera(id, clientesAtendidos, totalRecaudado, inicioCajera, finCajera);
    }

    /**
     * Procesa el cobro de un cliente, incluyendo simular el tiempo de escaneo de cada producto.
     */
    private ResultadoCobro procesarCliente(Cliente cliente) throws InterruptedException {
        Instant inicioCliente = Instant.now();
        List<DetalleProductoCobrado> detalles = new ArrayList<>();

        for (ClienteProducto cp : cliente.getClienteProductos()) {
            for (int i = 0; i < cp.getCantidad(); i++) {
                Thread.sleep(cp.getProducto().getTiempoProcesamientoSegundos() * 1000L);
                detalles.add(new DetalleProductoCobrado(
                        cp.getProducto().getId(),
                        cp.getProducto().getNombre(),
                        cp.getProducto().getPrecio(),
                        cp.getProducto().getTiempoProcesamientoSegundos()
                ));
            }
        }

        Instant finCliente = Instant.now();
        long tiempoCliente = Duration.between(inicioCliente, finCliente).toSeconds();

        return new ResultadoCobro(
                cliente.getId(),
                id,
                detalles,
                cliente.calcularTotalCompra(),
                tiempoCliente
        );
    }
}

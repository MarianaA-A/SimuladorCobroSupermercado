package com.supermercado.service;

import com.supermercado.db.CajeraDAO;
import com.supermercado.db.TransaccionDAO;
import com.supermercado.model.Cajera;
import com.supermercado.model.Cliente;
import com.supermercado.model.DetalleProductoCobrado;
import com.supermercado.model.ResultadoCobro;
import com.supermercado.model.ResumenCajera;

import java.sql.SQLException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

/**
 * Coordina la simulación concurrente del cobro y consolida los resultados.
 */
public class SimuladorCobro {
    private final int numeroCajeras;
    private final BlockingQueue<Cliente> colaGlobalClientes;
    private final List<Cajera> cajerasRegistradas;

    private final List<ResultadoCobro> resultadosPorCliente;
    private final List<ResumenCajera> resumenesPorCajera;

    private Instant inicioGlobal;
    private Instant finGlobal;

    public SimuladorCobro(int numeroCajeras, List<Cliente> clientes) {
        if (numeroCajeras <= 0) {
            throw new IllegalArgumentException("El numero de cajeras debe ser mayor que cero");
        }

        this.numeroCajeras = numeroCajeras;
        this.colaGlobalClientes = new LinkedBlockingQueue<>(clientes);
        this.resultadosPorCliente = new CopyOnWriteArrayList<>();
        this.resumenesPorCajera = new ArrayList<>();

        try {
            this.cajerasRegistradas = new CajeraDAO().obtenerORegistrar(numeroCajeras);
        } catch (SQLException e) {
            throw new IllegalStateException("No se pudieron cargar o registrar las cajeras", e);
        }
    }

    public void ejecutar() {
        // Registrar tiempo de inicio global
        inicioGlobal = Instant.now();

        // Crear pool de threads para ejecutar cajeras en paralelo
        ExecutorService executor = Executors.newFixedThreadPool(numeroCajeras);
        List<Future<ResumenCajera>> futuros = new ArrayList<>();

        // Asignar cada cajera a un thread con acceso a la cola compartida
        for (Cajera cajeraRegistrada : cajerasRegistradas) {
            Cajera cajera = new Cajera(
                    cajeraRegistrada.getId(),
                    cajeraRegistrada.getNombre(),
                    cajeraRegistrada.getEstado(),
                    colaGlobalClientes,
                    resultadosPorCliente
            );
            futuros.add(executor.submit(cajera));
        }

        // Aguardar a que todas las cajeras terminen y recopilar resultados
        for (Future<ResumenCajera> futuro : futuros) {
            try {
                resumenesPorCajera.add(futuro.get());
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new IllegalStateException("La ejecucion fue interrumpida", e);
            } catch (ExecutionException e) {
                throw new IllegalStateException("Error durante el cobro concurrente", e);
            }
        }

        // Cerrar executor y aguardar terminación
        executor.shutdown();
        try {
            executor.awaitTermination(1, TimeUnit.MINUTES);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("No se pudo cerrar correctamente el executor", e);
        }

        finGlobal = Instant.now();
    }

    public void imprimirReportes() {
        imprimirDetallePorCliente();
        imprimirDetallePorCajera();
        imprimirTotalesGlobales();
        guardarEnBD();
    }

    public List<ResultadoCobro> getResultadosPorCliente() {
        return List.copyOf(resultadosPorCliente);
    }

    public List<ResumenCajera> getResumenesPorCajera() {
        return List.copyOf(resumenesPorCajera);
    }

    private void guardarEnBD() {
        System.out.println("\nGuardando resultados en base de datos...");
        TransaccionDAO transaccionDAO = new TransaccionDAO();
        
        try {
            for (ResultadoCobro resultado : resultadosPorCliente) {
                int idTransaccion = transaccionDAO.guardar(
                        resultado.getIdCliente(),
                        resultado.getIdCajera(),
                        resultado.getTotalCliente(),
                        resultado.getTiempoTotalClienteSegundos()
                );

                if (idTransaccion != -1) {
                    for (DetalleProductoCobrado detalle : resultado.getProductosCobrados()) {
                        transaccionDAO.guardarDetalle(
                                idTransaccion,
                                detalle.getIdProducto(),
                                detalle.getNombreProducto(),
                                detalle.getCostoProducto(),
                                detalle.getTiempoProcesamientoSegundos()
                        );
                    }
                }
            }
            System.out.println("Datos guardados exitosamente en base de datos");
        } catch (SQLException e) {
            System.err.println("Error guardando en base de datos: " + e.getMessage());
        }
    }

    private void imprimirDetallePorCliente() {
        for (ResultadoCobro resultado : resultadosPorCliente) {
            System.out.println("Cliente #" + resultado.getIdCliente() + " | Cajera #" + resultado.getIdCajera());
            for (DetalleProductoCobrado detalle : resultado.getProductosCobrados()) {
                System.out.printf(Locale.US, "- %s | $%.2f | %ds%n",
                        detalle.getNombreProducto(),
                        detalle.getCostoProducto(),
                        detalle.getTiempoProcesamientoSegundos());
            }
        }
    }

    private void imprimirDetallePorCajera() {
        for (ResumenCajera resumen : resumenesPorCajera) {
            String nombreCajera = cajerasRegistradas.stream()
                    .filter(cajera -> cajera.getId() == resumen.getIdCajera())
                    .map(Cajera::getNombre)
                    .findFirst()
                    .orElse("Cajera #" + resumen.getIdCajera());

            for (Integer idCliente : resumen.getClientesAtendidos()) {
                System.out.println(nombreCajera + " | Cliente #" + idCliente);
            }
        }
    }

    private void imprimirTotalesGlobales() {
        double totalGeneral = resultadosPorCliente.stream()
                .mapToDouble(ResultadoCobro::getTotalCliente)
                .sum();

        long tiempoTotalClientes = resultadosPorCliente.stream()
                .mapToLong(ResultadoCobro::getTiempoTotalClienteSegundos)
                .sum();

        long tiempoGlobalCobro = inicioGlobal != null && finGlobal != null
                ? TimeUnit.MILLISECONDS.toSeconds(finGlobal.toEpochMilli() - inicioGlobal.toEpochMilli())
                : 0L;

        System.out.println("=== TOTALES GLOBALES DE COBRO ===");
        System.out.printf(Locale.US, "Total recaudado general: $%.2f%n", totalGeneral);
        System.out.println("Tiempo total de cobro (suma por cliente): " + tiempoTotalClientes + "s");
        System.out.println("Tiempo total de simulacion concurrente: " + tiempoGlobalCobro + "s");
    }
}

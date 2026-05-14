package com.supermercado;

import com.supermercado.model.Cliente;
import com.supermercado.model.Producto;
import com.supermercado.service.SimuladorCobro;

import java.util.ArrayList;
import java.util.List;

/**
 * Punto de entrada de la simulacion por consola para validar la logica del backend.
 */
public class Principal {
    public static void main(String[] args) {
        List<Producto> catalogo = construirCatalogo();
        List<Cliente> clientes = construirClientes(catalogo);

        SimuladorCobro simulador = new SimuladorCobro(3, clientes);
        simulador.ejecutar();
        simulador.imprimirReportes();
    }

    private static List<Producto> construirCatalogo() {
        List<Producto> catalogo = new ArrayList<>();
        catalogo.add(new Producto(1, "Arroz", 4500, 1));
        catalogo.add(new Producto(2, "Leche", 3800, 1));
        catalogo.add(new Producto(3, "Huevos", 12000, 2));
        catalogo.add(new Producto(4, "Pan Integral", 5200, 1));
        catalogo.add(new Producto(5, "Cafe", 15000, 2));
        catalogo.add(new Producto(6, "Queso", 9800, 2));
        catalogo.add(new Producto(7, "Manzana", 2500, 1));
        catalogo.add(new Producto(8, "Cereal", 11000, 2));
        return catalogo;
    }

    private static List<Cliente> construirClientes(List<Producto> catalogo) {
        List<Cliente> clientes = new ArrayList<>();

        Cliente cliente1 = new Cliente(1);
        cliente1.agregarProducto(catalogo.get(0));
        cliente1.agregarProducto(catalogo.get(1));
        cliente1.agregarProducto(catalogo.get(3));
        clientes.add(cliente1);

        Cliente cliente2 = new Cliente(2);
        cliente2.agregarProducto(catalogo.get(2));
        cliente2.agregarProducto(catalogo.get(5));
        clientes.add(cliente2);

        Cliente cliente3 = new Cliente(3);
        cliente3.agregarProducto(catalogo.get(4));
        cliente3.agregarProducto(catalogo.get(6));
        cliente3.agregarProducto(catalogo.get(7));
        clientes.add(cliente3);

        Cliente cliente4 = new Cliente(4);
        cliente4.agregarProducto(catalogo.get(1));
        cliente4.agregarProducto(catalogo.get(3));
        cliente4.agregarProducto(catalogo.get(6));
        clientes.add(cliente4);

        return clientes;
    }
}

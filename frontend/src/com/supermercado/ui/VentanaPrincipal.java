package com.supermercado.ui;

import com.supermercado.db.ConectorBD;
import com.supermercado.db.CajeraDAO;
import com.supermercado.db.ClienteDAO;
import com.supermercado.db.ProductoDAO;
import com.supermercado.model.Cajera;
import com.supermercado.model.Cliente;
import com.supermercado.model.Producto;
import com.supermercado.model.ResultadoCobro;
import com.supermercado.model.ResumenCajera;
import com.supermercado.service.SimuladorCobro;

import javax.swing.BorderFactory;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingUtilities;
import javax.swing.event.ListSelectionEvent;
import javax.swing.table.DefaultTableModel;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.ResultSet;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * Ventana principal de la aplicacion Swing para administrar datos y ejecutar la simulacion.
 */
public class VentanaPrincipal extends JFrame {
    // ============ CONSTANTES Y ESTILOS ============
    private static final String ESTADO_ACTIVO = "Activo";
    private static final String ESTADO_INACTIVO = "Inactivo";

    private static final Color FONDO = new Color(254, 241, 246);
    private static final Color TARJETA = new Color(255, 250, 252);
    private static final Color ACENTO = new Color(235, 214, 244);
    private static final Color BOTON = new Color(243, 221, 245);
    private static final Color BOTON_BORDE = new Color(224, 191, 231);
    private static final Color ENCABEZADO_TABLA = new Color(243, 224, 233);
    private static final Color TEXTO_TABLA = new Color(96, 72, 92);

    // ============ DAOs Y ACCESO A DATOS ============
    private final ProductoDAO productoDAO = new ProductoDAO();
    private final ClienteDAO clienteDAO = new ClienteDAO();
    private final CajeraDAO cajeraDAO = new CajeraDAO();

    // ============ COMPONENTES DE UI - CONSOLA ============
    private final JTextArea consola = new JTextArea(14, 40);

    // ============ MODELOS DE TABLAS ============
    private final DefaultTableModel modeloProductos = new DefaultTableModel(new Object[]{"ID", "Nombre", "Precio", "Tiempo", "Activo"}, 0) {
        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }
    };
    private final DefaultTableModel modeloClientes = new DefaultTableModel(new Object[]{"ID", "Nombre", "Activo"}, 0) {
        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }
    };
    private final DefaultTableModel modeloCajeras = new DefaultTableModel(new Object[]{"ID", "Nombre", "Activo"}, 0) {
        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }
    };

    // ============ TABLAS ============
    private final JTable tablaProductos = new JTable(modeloProductos);
    private final JTable tablaClientes = new JTable(modeloClientes);
    private final JTable tablaCajeras = new JTable(modeloCajeras);

    // ============ CONTENEDOR DE PESTAÑAS ============
    private JTabbedPane pestanas;

    // ============ CAMPOS DE FORMULARIO - PRODUCTOS ============
    private final JTextField productoNombre = new JTextField();
    private final JTextField productoPrecio = new JTextField();
    private final JTextField productoTiempo = new JTextField();

    // ============ CAMPOS DE FORMULARIO - CLIENTES ============
    private final JTextField clienteNombre = new JTextField();

    // ============ CAMPOS DE FORMULARIO - CAJERAS ============
    private final JTextField cajeraNombre = new JTextField();
    private final JComboBox<String> cajeraEstado = new JComboBox<>(new String[]{ESTADO_INACTIVO, ESTADO_ACTIVO});

    // ============ CONTROLES DE SIMULACION ============
    private final JComboBox<ItemCombo> comboClientesSimulacion = new JComboBox<>();
    private final JSpinner spinnerCajeras = new JSpinner(new SpinnerNumberModel(1, 1, 20, 1));
    private final JTextField tiempoAutomatico = new JTextField();

    // ============ ESTADO DE SIMULACION ============
    private SimuladorCobro simulacionActual;
    private Cliente clienteTemporal;

    // ============ INICIALIZACION ============

    public VentanaPrincipal() {
        configurarVentana();
        estilizarTabla(tablaProductos);
        estilizarTabla(tablaClientes);
        estilizarTabla(tablaCajeras);
        consola.setBackground(new Color(252, 249, 245));
        consola.setForeground(new Color(64, 64, 64));
        construirUI();
        cargarDatos();
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new VentanaPrincipal().setVisible(true));
    }

    private void configurarVentana() {
        setTitle("Simulador de Cobro - Supermercado");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setMinimumSize(new Dimension(1200, 760));
        setLocationRelativeTo(null);
        getContentPane().setBackground(FONDO);
    }

    // ============ CONSTRUCCION DE UI ============

    private void construirUI() {
        pestanas = new JTabbedPane();
        pestanas.addTab("Productos", construirPanelProductos());
        pestanas.addTab("Clientes", construirPanelClientes());
        pestanas.addTab("Cajeras", construirPanelCajeras());
        pestanas.addTab("Reportes", construirPanelReportes());
        pestanas.addTab("Simulación", construirPanelSimulacion());

        consola.setEditable(false);
        consola.setLineWrap(true);
        consola.setWrapStyleWord(true);
        JScrollPane scrollConsola = new JScrollPane(consola);
        scrollConsola.setBorder(BorderFactory.createTitledBorder("Impresión / salida"));

        JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, pestanas, scrollConsola);
        splitPane.setResizeWeight(0.78);
        splitPane.setBorder(null);

        add(splitPane, BorderLayout.CENTER);
    }

    // --- Paneles de Pestañas ---

    private JPanel construirPanelProductos() {
        JPanel panel = panelBase();
        panel.setLayout(new BorderLayout(12, 12));
        panel.add(formularioProductos(), BorderLayout.NORTH);
        panel.add(new JScrollPane(tablaProductos), BorderLayout.CENTER);
        panel.add(botoneraProductos(), BorderLayout.SOUTH);
        tablaProductos.getSelectionModel().addListSelectionListener(this::seleccionarProducto);
        return panel;
    }

    private JPanel construirPanelClientes() {
        JPanel panel = panelBase();
        panel.setLayout(new BorderLayout(12, 12));
        panel.add(formularioClientes(), BorderLayout.NORTH);
        panel.add(new JScrollPane(tablaClientes), BorderLayout.CENTER);
        panel.add(botoneraClientes(), BorderLayout.SOUTH);
        tablaClientes.getSelectionModel().addListSelectionListener(this::seleccionarCliente);
        return panel;
    }

    private JPanel construirPanelCajeras() {
        JPanel panel = panelBase();
        panel.setLayout(new BorderLayout(12, 12));
        panel.add(formularioCajeras(), BorderLayout.NORTH);
        panel.add(new JScrollPane(tablaCajeras), BorderLayout.CENTER);
        panel.add(botoneraCajeras(), BorderLayout.SOUTH);
        tablaCajeras.getSelectionModel().addListSelectionListener(this::seleccionarCajera);
        return panel;
    }

    private JPanel construirPanelReportes() {
        JPanel panel = panelBase();
        panel.setLayout(new BorderLayout(12, 12));

        JPanel cabecera = tarjeta();
        cabecera.setLayout(new BorderLayout());
        JLabel titulo = new JLabel("Reportes guardados en MySQL");
        titulo.setForeground(new Color(92, 92, 92));
        cabecera.add(titulo, BorderLayout.CENTER);

        JPanel botones = panelBotones();
        botones.add(boton("Imprimir productos", e -> imprimirProductos()));
        botones.add(boton("Imprimir clientes", e -> imprimirClientes()));
        botones.add(boton("Imprimir cajeras", e -> imprimirCajeras()));
        botones.add(boton("Detalle de cobros", e -> imprimirDetalleCobros()));
        botones.add(boton("Resultados de cobro", e -> imprimirResultadosCobro()));
        botones.add(boton("Resumen de cajeras", e -> imprimirResumenCajeras()));
        cabecera.add(botones, BorderLayout.SOUTH);

        panel.add(cabecera, BorderLayout.NORTH);

        JTextArea ayuda = new JTextArea();
        ayuda.setEditable(false);
        ayuda.setLineWrap(true);
        ayuda.setWrapStyleWord(true);
        ayuda.setBackground(FONDO);
        ayuda.setForeground(new Color(88, 88, 88));
        ayuda.setText("Usa estos botones para imprimir directamente la información almacenada en la base de datos. La consola inferior mostrará el resultado.");
        panel.add(ayuda, BorderLayout.CENTER);

        return panel;
    }

    private JPanel construirPanelSimulacion() {
        JPanel panel = panelBase();
        panel.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        configurarCampoAutomatico(tiempoAutomatico);

        gbc.gridx = 0; gbc.gridy = 0;
        panel.add(etiqueta("Cliente"), gbc);
        gbc.gridx = 1; gbc.gridy = 0; gbc.weightx = 1;
        panel.add(comboClientesSimulacion, gbc);

        gbc.gridx = 0; gbc.gridy = 1; gbc.weightx = 0;
        panel.add(etiqueta("Cajeras"), gbc);
        gbc.gridx = 1; gbc.gridy = 1; gbc.weightx = 1;
        panel.add(spinnerCajeras, gbc);

        gbc.gridx = 0; gbc.gridy = 2; gbc.weightx = 0;
        panel.add(etiqueta("Tiempo automático"), gbc);
        gbc.gridx = 1; gbc.gridy = 2; gbc.weightx = 1;
        panel.add(tiempoAutomatico, gbc);

        JPanel botones = new JPanel();
        botones.setBackground(FONDO);
        JButton simular = boton("Ejecutar simulación", e -> ejecutarSimulacion());
        JButton imprimirDetalle = boton("Imprimir detalle", e -> imprimirDetalle());
        JButton imprimirResumen = boton("Imprimir resumen", e -> imprimirResumen());
        JButton irAProductos = boton("Ir a productos", e -> mostrarPestanaProductos());
        botones.add(simular);
        botones.add(imprimirDetalle);
        botones.add(imprimirResumen);
        botones.add(irAProductos);

        gbc.gridx = 0; gbc.gridy = 3; gbc.gridwidth = 2;
        panel.add(botones, gbc);

        JTextArea ayuda = new JTextArea();
        ayuda.setEditable(false);
        ayuda.setOpaque(false);
        ayuda.setLineWrap(true);
        ayuda.setWrapStyleWord(true);
        ayuda.setForeground(new Color(88, 88, 88));
        ayuda.setText("Pasos: 1) Selecciona uno o más productos en la pestaña Productos. 2) Elige un cliente. 3) Ejecuta la simulación.");
        gbc.gridx = 0; gbc.gridy = 4; gbc.gridwidth = 2;
        panel.add(ayuda, gbc);

        JLabel aviso = etiqueta("El tiempo se calcula automáticamente a partir de los productos seleccionados.");
        gbc.gridx = 0; gbc.gridy = 5; gbc.gridwidth = 2;
        panel.add(aviso, gbc);

        return panel;
    }

    // ============ FORMULARIOS ============

    private JPanel formularioProductos() {
        JPanel panel = tarjeta();
        panel.setLayout(new GridBagLayout());
        GridBagConstraints gbc = baseConstraints();
        configurarCampoAutomatico(productoTiempo);
        agregarCampo(panel, gbc, 0, 0, "Nombre", productoNombre);
        agregarCampo(panel, gbc, 0, 1, "Precio", productoPrecio);
        agregarCampo(panel, gbc, 0, 2, "Tiempo automático", productoTiempo);
        return panel;
    }

    private JPanel formularioClientes() {
        JPanel panel = tarjeta();
        panel.setLayout(new GridBagLayout());
        GridBagConstraints gbc = baseConstraints();
        agregarCampo(panel, gbc, 0, 0, "Nombre", clienteNombre);
        return panel;
    }

    private JPanel formularioCajeras() {
        JPanel panel = tarjeta();
        panel.setLayout(new GridBagLayout());
        GridBagConstraints gbc = baseConstraints();
        agregarCampo(panel, gbc, 0, 0, "Nombre", cajeraNombre);
        agregarCampo(panel, gbc, 0, 1, "Activo", cajeraEstado);
        return panel;
    }

    // ============ BARRAS DE BOTONES ============

    private JPanel botoneraProductos() {
        JPanel panel = panelBotones();
        panel.add(boton("Agregar", e -> guardarProducto()));
        panel.add(boton("Actualizar", e -> actualizarProducto()));
        panel.add(boton("Activar", e -> activarProducto()));
        panel.add(boton("Desactivar", e -> eliminarProducto()));
        panel.add(boton("Eliminar", e -> eliminarProductoFisico()));
        panel.add(boton("Limpiar", e -> limpiarProducto()));
        panel.add(boton("Imprimir productos", e -> imprimirProductos()));
        return panel;
    }

    private JPanel botoneraClientes() {
        JPanel panel = panelBotones();
        panel.add(boton("Agregar", e -> guardarCliente()));
        panel.add(boton("Actualizar", e -> actualizarCliente()));
        panel.add(boton("Activar", e -> activarCliente()));
        panel.add(boton("Desactivar", e -> eliminarCliente()));
        panel.add(boton("Eliminar", e -> eliminarClienteFisico()));
        panel.add(boton("Limpiar", e -> limpiarCliente()));
        panel.add(boton("Imprimir clientes", e -> imprimirClientes()));
        return panel;
    }

    private JPanel botoneraCajeras() {
        JPanel panel = panelBotones();
        panel.add(boton("Agregar", e -> guardarCajera()));
        panel.add(boton("Actualizar", e -> actualizarCajera()));
        panel.add(boton("Activar", e -> activarCajera()));
        panel.add(boton("Desactivar", e -> eliminarCajera()));
        panel.add(boton("Eliminar", e -> eliminarCajeraFisico()));
        panel.add(boton("Limpiar", e -> limpiarCajera()));
        panel.add(boton("Imprimir cajeras", e -> imprimirCajeras()));
        return panel;
    }

    // ============ CARGA DE DATOS ============

    private void cargarDatos() {
        cargarProductos();
        cargarClientes();
        cargarCajeras();
        cargarClientesCombo();
    }

    private void cargarProductos() {
        modeloProductos.setRowCount(0);
        try {
            String sql = "SELECT id, nombre, precio, tiempo_procesamiento_segundos, COALESCE(activo, 1) AS activo FROM productos ORDER BY id";
            try (Connection conn = ConectorBD.obtenerInstancia().obtenerConexion();
                 PreparedStatement stmt = conn.prepareStatement(sql);
                 ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    String activo = rs.getInt("activo") == 1 ? "Activo" : "Inactivo";
                    modeloProductos.addRow(new Object[]{rs.getInt("id"), rs.getString("nombre"), rs.getDouble("precio"), rs.getInt("tiempo_procesamiento_segundos"), activo});
                }
            }
        } catch (SQLException e) {
            mostrarError("No se pudieron cargar los productos", e);
        }
    }

    private void cargarClientes() {
        modeloClientes.setRowCount(0);
        try {
            String sql = "SELECT id, nombre, COALESCE(activo, 1) AS activo FROM clientes ORDER BY id";
            try (Connection conn = ConectorBD.obtenerInstancia().obtenerConexion();
                 PreparedStatement stmt = conn.prepareStatement(sql);
                 ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    String activo = rs.getInt("activo") == 1 ? "Activo" : "Inactivo";
                    modeloClientes.addRow(new Object[]{rs.getInt("id"), rs.getString("nombre"), activo});
                }
            }
        } catch (SQLException e) {
            mostrarError("No se pudieron cargar los clientes", e);
        }
    }

    private void cargarCajeras() {
        modeloCajeras.setRowCount(0);
        try {
            for (Cajera cajera : cajeraDAO.obtenerTodas()) {
                modeloCajeras.addRow(new Object[]{cajera.getId(), cajera.getNombre(), normalizarEstadoCajera(cajera.getEstado())});
            }
        } catch (SQLException e) {
            mostrarError("No se pudieron cargar las cajeras", e);
        }
    }

    private void cargarClientesCombo() {
        comboClientesSimulacion.removeAllItems();
        try {
            String sql = "SELECT id, nombre FROM clientes WHERE COALESCE(activo, 1) = 1 ORDER BY id";
            try (Connection conn = ConectorBD.obtenerInstancia().obtenerConexion();
                 PreparedStatement stmt = conn.prepareStatement(sql);
                 ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    comboClientesSimulacion.addItem(new ItemCombo(rs.getInt("id"), rs.getString("nombre")));
                }
            }
        } catch (SQLException e) {
            mostrarError("No se pudieron cargar los clientes para simulación", e);
        }
    }

    // ============ CRUD PRODUCTOS ============

    private void guardarProducto() {
        if (!validarCampos(productoNombre, productoPrecio)) {
            return;
        }
        try {
            Producto producto = new Producto(0, productoNombre.getText().trim(), Double.parseDouble(productoPrecio.getText().trim()), calcularTiempoAutomatico(Double.parseDouble(productoPrecio.getText().trim())));
            productoDAO.guardar(producto);
            limpiarProducto();
            recargarTodo();
        } catch (NumberFormatException e) {
            mostrarAviso("El precio debe ser numérico.");
        } catch (SQLException e) {
            mostrarError("No se pudo guardar el producto", e);
        }
    }

    private void actualizarProducto() {
        Integer id = idSeleccionado(tablaProductos);
        if (id == null || !validarCampos(productoNombre, productoPrecio)) {
            return;
        }
        try {
            double precio = Double.parseDouble(productoPrecio.getText().trim());
            productoDAO.actualizar(new Producto(id, productoNombre.getText().trim(), precio, calcularTiempoAutomatico(precio)));
            limpiarProducto();
            recargarTodo();
        } catch (NumberFormatException e) {
            mostrarAviso("El precio debe ser numérico.");
        } catch (SQLException e) {
            mostrarError("No se pudo actualizar el producto", e);
        }
    }

    private void eliminarProducto() {
        Integer id = idSeleccionado(tablaProductos);
        if (id == null) return;
        try {
            productoDAO.eliminar(id);
            limpiarProducto();
            recargarTodo();
        } catch (SQLException e) {
            mostrarError("No se pudo desactivar el producto", e);
        }
    }

    private void eliminarProductoFisico() {
        Integer id = idSeleccionado(tablaProductos);
        if (id == null) return;
        int opcion = JOptionPane.showConfirmDialog(this, "¿Confirma eliminar definitivamente el producto? Esta acción no se puede deshacer.", "Confirmar eliminación", JOptionPane.YES_NO_OPTION);
        if (opcion != JOptionPane.YES_OPTION) return;
        try {
            productoDAO.eliminarFisico(id);
            limpiarProducto();
            recargarTodo();
        } catch (SQLException e) {
            mostrarError("No se pudo eliminar el producto. Si tiene transacciones asociadas, usa Desactivar.", e);
        }
    }

    private void activarProducto() {
        Integer id = idSeleccionado(tablaProductos);
        if (id == null) return;
        try {
            productoDAO.activar(id);
            limpiarProducto();
            recargarTodo();
        } catch (SQLException e) {
            mostrarError("No se pudo activar el producto", e);
        }
    }

    private void limpiarProducto() {
        productoNombre.setText("");
        productoPrecio.setText("");
        productoTiempo.setText("");
        tablaProductos.clearSelection();
    }

    // ============ CRUD CLIENTES ============

    private void guardarCliente() {
        if (!validarCampos(clienteNombre)) return;
        try {
            clienteDAO.guardar(clienteNombre.getText().trim());
            limpiarCliente();
            recargarTodo();
        } catch (SQLException e) {
            mostrarError("No se pudo guardar el cliente", e);
        }
    }

    private void actualizarCliente() {
        Integer id = idSeleccionado(tablaClientes);
        if (id == null || !validarCampos(clienteNombre)) return;
        try {
            clienteDAO.actualizar(new Cliente(id, clienteNombre.getText().trim()));
            limpiarCliente();
            recargarTodo();
        } catch (SQLException e) {
            mostrarError("No se pudo actualizar el cliente", e);
        }
    }

    private void eliminarCliente() {
        Integer id = idSeleccionado(tablaClientes);
        if (id == null) return;
        try {
            clienteDAO.eliminar(id);
            limpiarCliente();
            recargarTodo();
        } catch (SQLException e) {
            mostrarError("No se pudo desactivar el cliente", e);
        }
    }

    private void activarCliente() {
        Integer id = idSeleccionado(tablaClientes);
        if (id == null) return;
        try {
            clienteDAO.activar(id);
            limpiarCliente();
            recargarTodo();
        } catch (SQLException e) {
            mostrarError("No se pudo activar el cliente", e);
        }
    }

    private void eliminarClienteFisico() {
        Integer id = idSeleccionado(tablaClientes);
        if (id == null) return;
        int opcion = JOptionPane.showConfirmDialog(this, "¿Confirma eliminar definitivamente el cliente? Esta acción no se puede deshacer.", "Confirmar eliminación", JOptionPane.YES_NO_OPTION);
        if (opcion != JOptionPane.YES_OPTION) return;
        try {
            clienteDAO.eliminarFisico(id);
            limpiarCliente();
            recargarTodo();
        } catch (SQLException e) {
            mostrarError("No se pudo eliminar el cliente", e);
        }
    }

    private void limpiarCliente() {
        clienteNombre.setText("");
        tablaClientes.clearSelection();
    }

    // ============ CRUD CAJERAS ============

    private void guardarCajera() {
        if (!validarCampos(cajeraNombre)) return;
        try {
            cajeraDAO.guardar(cajeraNombre.getText().trim(), normalizarEstadoCajera(cajeraEstado.getSelectedItem().toString()));
            limpiarCajera();
            recargarTodo();
        } catch (SQLException e) {
            mostrarError("No se pudo guardar la cajera", e);
        }
    }

    private void actualizarCajera() {
        Integer id = idSeleccionado(tablaCajeras);
        if (id == null || !validarCampos(cajeraNombre)) return;
        try {
            cajeraDAO.actualizar(new Cajera(id, cajeraNombre.getText().trim(), normalizarEstadoCajera(cajeraEstado.getSelectedItem().toString())));
            limpiarCajera();
            recargarTodo();
        } catch (SQLException e) {
            mostrarError("No se pudo actualizar la cajera", e);
        }
    }

    private void eliminarCajera() {
        Integer id = idSeleccionado(tablaCajeras);
        if (id == null) return;
        try {
            cajeraDAO.eliminar(id);
            limpiarCajera();
            recargarTodo();
        } catch (SQLException e) {
            mostrarError("No se pudo desactivar la cajera", e);
        }
    }

    private void activarCajera() {
        Integer id = idSeleccionado(tablaCajeras);
        if (id == null) return;
        try {
            cajeraDAO.activar(id);
            limpiarCajera();
            recargarTodo();
        } catch (SQLException e) {
            mostrarError("No se pudo activar la cajera", e);
        }
    }

    private void eliminarCajeraFisico() {
        Integer id = idSeleccionado(tablaCajeras);
        if (id == null) return;
        int opcion = JOptionPane.showConfirmDialog(this, "¿Confirma eliminar definitivamente la cajera? Esta acción no se puede deshacer.", "Confirmar eliminación", JOptionPane.YES_NO_OPTION);
        if (opcion != JOptionPane.YES_OPTION) return;
        try {
            cajeraDAO.eliminarFisico(id);
            limpiarCajera();
            recargarTodo();
        } catch (SQLException e) {
            mostrarError("No se pudo eliminar la cajera", e);
        }
    }

    private void limpiarCajera() {
        cajeraNombre.setText("");
        cajeraEstado.setSelectedIndex(0);
        tablaCajeras.clearSelection();
    }

    // ============ SIMULACION ============

    private void ejecutarSimulacion() {
        ItemCombo seleccionado = (ItemCombo) comboClientesSimulacion.getSelectedItem();
        if (seleccionado == null || tablaProductos.getSelectedRows().length == 0) {
            mostrarAviso("Debes seleccionar un cliente y al menos un producto en la pestaña Productos.");
            if (pestanas != null) {
                pestanas.setSelectedIndex(0);
            }
            return;
        }

        // Crear cliente temporal y agregar productos seleccionados
        clienteTemporal = new Cliente(seleccionado.id, seleccionado.nombre);
        for (int fila : tablaProductos.getSelectedRows()) {
            clienteTemporal.agregarProducto(new Producto(
                    ((Number) modeloProductos.getValueAt(fila, 0)).intValue(),
                    modeloProductos.getValueAt(fila, 1).toString(),
                    Double.parseDouble(modeloProductos.getValueAt(fila, 2).toString()),
                    Integer.parseInt(modeloProductos.getValueAt(fila, 3).toString())
            ));
        }

        // Ejecutar simulación concurrente
        int cantidadCajeras = (Integer) spinnerCajeras.getValue();
        simulacionActual = new SimuladorCobro(cantidadCajeras, List.of(clienteTemporal));
        simulacionActual.ejecutar();
        tiempoAutomatico.setText(String.valueOf(clienteTemporal.calcularTiempoTotalSegundos()));
        consola.setText("Simulación ejecutada. Usa los botones de impresión para ver detalle o resumen.\n");
    }

    private void imprimirDetalle() {
        if (simulacionActual == null) {
            mostrarAviso("Primero ejecuta la simulación.");
            return;
        }
        StringBuilder sb = new StringBuilder();
        for (ResultadoCobro resultado : simulacionActual.getResultadosPorCliente()) {
            sb.append("Cliente #").append(resultado.getIdCliente()).append(" | Cajera #").append(resultado.getIdCajera()).append('\n');
            for (var detalle : resultado.getProductosCobrados()) {
                sb.append("- ").append(detalle.getNombreProducto()).append(" | $").append(detalle.getCostoProducto())
                        .append(" | ").append(detalle.getTiempoProcesamientoSegundos()).append("s\n");
            }
        }
        consola.setText(sb.toString());
    }

    private void imprimirResumen() {
        if (simulacionActual == null) {
            mostrarAviso("Primero ejecuta la simulación.");
            return;
        }
        StringBuilder sb = new StringBuilder();
        for (ResumenCajera resumen : simulacionActual.getResumenesPorCajera()) {
            sb.append("Cajera #").append(resumen.getIdCajera())
                    .append(" | Clientes: ").append(resumen.getCantidadClientesAtendidos())
                    .append(" | Total: $").append(resumen.getTotalRecaudado())
                    .append(" | Tiempo: ").append(resumen.getTiempoProcesamientoSegundos()).append("s\n");
        }
        consola.setText(sb.toString());
    }

    // ============ REPORTES / IMPRESION ============

    private void imprimirProductos() {
        StringBuilder sb = new StringBuilder();
        try {
            for (Producto p : productoDAO.obtenerTodos()) {
                sb.append("Producto #").append(p.getId()).append(" | ")
                        .append(p.getNombre()).append(" | $").append(p.getPrecio())
                        .append(" | Tiempo: ").append(p.getTiempoProcesamientoSegundos()).append("s\n");
            }
            consola.setText(sb.toString());
        } catch (SQLException e) {
            mostrarError("No se pudieron obtener los productos", e);
        }
    }

    private void imprimirClientes() {
        StringBuilder sb = new StringBuilder();
        try {
            String sql = "SELECT id, nombre, COALESCE(activo, 1) AS activo FROM clientes ORDER BY id";
            try (Connection conn = ConectorBD.obtenerInstancia().obtenerConexion();
                 PreparedStatement stmt = conn.prepareStatement(sql);
                 ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    String activo = rs.getInt("activo") == 1 ? "Activo" : "Inactivo";
                    sb.append("Cliente #").append(rs.getInt("id")).append(" | ").append(rs.getString("nombre")).append(" | ").append(activo).append("\n");
                }
            }
            consola.setText(sb.toString());
        } catch (SQLException e) {
            mostrarError("No se pudieron obtener los clientes", e);
        }
    }

    private void imprimirCajeras() {
        StringBuilder sb = new StringBuilder();
        try {
            for (Cajera c : cajeraDAO.obtenerTodas()) {
                sb.append("Cajera #").append(c.getId()).append(" | ")
                    .append(c.getNombre()).append(" | Activo: ").append(normalizarEstadoCajera(c.getEstado())).append("\n");
            }
            consola.setText(sb.toString());
        } catch (SQLException e) {
            mostrarError("No se pudieron obtener las cajeras", e);
        }
    }

    private void imprimirDetalleCobros() {
        StringBuilder sb = new StringBuilder("=== DETALLE DE PRODUCTO COBRADO ===\n");
        String sql = "SELECT id_transaccion, id_producto, nombre_producto, costo_producto, tiempo_procesamiento_segundos " +
                "FROM detalle_transacciones ORDER BY id_transaccion, id";
        try (Connection conn = ConectorBD.obtenerInstancia().obtenerConexion();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                sb.append("Transacción #").append(rs.getInt("id_transaccion"))
                        .append(" | Producto #").append(rs.getInt("id_producto"))
                        .append(" | ").append(rs.getString("nombre_producto"))
                        .append(" | $").append(rs.getDouble("costo_producto"))
                        .append(" | Tiempo: ").append(rs.getInt("tiempo_procesamiento_segundos")).append("s\n");
            }
            consola.setText(sb.toString());
        } catch (SQLException e) {
            mostrarError("No se pudo imprimir el detalle de productos cobrados", e);
        }
    }

    private void imprimirResultadosCobro() {
        StringBuilder sb = new StringBuilder("=== RESULTADOS DE COBRO ===\n");
        String sql = "SELECT t.id, t.id_cliente, c.nombre AS nombre_cliente, t.id_cajera, cj.nombre AS nombre_cajera, " +
                "t.total_cliente, t.tiempo_total_segundos, t.fecha_inicio, t.fecha_fin " +
                "FROM transacciones t " +
                "INNER JOIN clientes c ON c.id = t.id_cliente " +
                "INNER JOIN cajeras cj ON cj.id = t.id_cajera " +
                "ORDER BY t.id";
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        try (Connection conn = ConectorBD.obtenerInstancia().obtenerConexion();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                sb.append("Transacción #").append(rs.getInt("id"))
                        .append(" | Cliente #").append(rs.getInt("id_cliente")).append(" (").append(rs.getString("nombre_cliente")).append(")")
                        .append(" | Cajera #").append(rs.getInt("id_cajera")).append(" (").append(rs.getString("nombre_cajera")).append(")")
                        .append(" | Total: $").append(rs.getDouble("total_cliente"))
                        .append(" | Tiempo: ").append(rs.getInt("tiempo_total_segundos")).append("s");

                if (rs.getTimestamp("fecha_fin") != null) {
                    sb.append(" | Fin: ").append(rs.getTimestamp("fecha_fin").toLocalDateTime().format(formatter));
                }
                sb.append('\n');
            }
            consola.setText(sb.toString());
        } catch (SQLException e) {
            mostrarError("No se pudieron imprimir los resultados de cobro", e);
        }
    }

    private void imprimirResumenCajeras() {
        StringBuilder sb = new StringBuilder("=== RESUMEN DE CAJERAS ===\n");
        String sql = "SELECT cj.id AS id_cajera, cj.nombre, " +
                "COUNT(t.id) AS clientes_atendidos, " +
                "COALESCE(GROUP_CONCAT(t.id_cliente ORDER BY t.id_cliente SEPARATOR ', '), '') AS ids_clientes, " +
                "COALESCE(SUM(t.total_cliente), 0) AS total_recaudado, " +
                "COALESCE(TIMESTAMPDIFF(SECOND, MIN(t.fecha_inicio), MAX(t.fecha_fin)), 0) AS tiempo_total " +
                "FROM cajeras cj " +
                "LEFT JOIN transacciones t ON t.id_cajera = cj.id " +
                "GROUP BY cj.id, cj.nombre " +
                "ORDER BY cj.id";
        try (Connection conn = ConectorBD.obtenerInstancia().obtenerConexion();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                sb.append("Cajera #").append(rs.getInt("id_cajera"))
                        .append(" | ").append(rs.getString("nombre"))
                        .append(" | Clientes: ").append(rs.getInt("clientes_atendidos"))
                        .append(" | IDs: [").append(rs.getString("ids_clientes")).append("]")
                        .append(" | Total: $").append(rs.getDouble("total_recaudado"))
                        .append(" | Tiempo: ").append(rs.getInt("tiempo_total")).append("s\n");
            }
            consola.setText(sb.toString());
        } catch (SQLException e) {
            mostrarError("No se pudo imprimir el resumen de cajeras", e);
        }
    }

    // ============ SELECTORES DE TABLA ============

    private void seleccionarProducto(ListSelectionEvent e) {
        if (!e.getValueIsAdjusting()) {
            int fila = tablaProductos.getSelectedRow();
            if (fila >= 0) {
                productoNombre.setText(modeloProductos.getValueAt(fila, 1).toString());
                productoPrecio.setText(modeloProductos.getValueAt(fila, 2).toString());
                productoTiempo.setText(modeloProductos.getValueAt(fila, 3).toString());
            }
        }
    }

    private void seleccionarCliente(ListSelectionEvent e) {
        if (!e.getValueIsAdjusting()) {
            int fila = tablaClientes.getSelectedRow();
            if (fila >= 0) {
                clienteNombre.setText(modeloClientes.getValueAt(fila, 1).toString());
            }
        }
    }

    private void seleccionarCajera(ListSelectionEvent e) {
        if (!e.getValueIsAdjusting()) {
            int fila = tablaCajeras.getSelectedRow();
            if (fila >= 0) {
                cajeraNombre.setText(modeloCajeras.getValueAt(fila, 1).toString());
                cajeraEstado.setSelectedItem(modeloCajeras.getValueAt(fila, 2).toString());
            }
        }
    }

    // ============ METODOS AUXILIARES DE VALIDACION ============

    private boolean validarCampos(JTextField... campos) {
        for (JTextField campo : campos) {
            if (campo.getText().trim().isEmpty()) {
                mostrarAviso("Todos los campos son obligatorios.");
                return false;
            }
        }
        return true;
    }

    private Integer idSeleccionado(JTable tabla) {
        int fila = tabla.getSelectedRow();
        if (fila < 0) {
            mostrarAviso("Selecciona un registro de la tabla.");
            return null;
        }
        int filaModelo = tabla.convertRowIndexToModel(fila);
        return ((Number) tabla.getModel().getValueAt(filaModelo, 0)).intValue();
    }

    private int calcularTiempoAutomatico(double precio) {
        if (precio < 5000) return 1;
        if (precio < 10000) return 2;
        return 3;
    }

    private String normalizarEstadoCajera(String estado) {
        if (estado == null) return ESTADO_ACTIVO;
        String valor = estado.trim().toLowerCase();
        if (valor.contains("inactivo") || valor.contains("no disponible") || valor.contains("ocupada")) {
            return ESTADO_INACTIVO;
        }
        return ESTADO_ACTIVO;
    }

    private void recargarTodo() {
        cargarDatos();
    }

    private void mostrarPestanaProductos() {
        if (pestanas != null) {
            pestanas.setSelectedIndex(0);
        }
        tablaProductos.requestFocusInWindow();
    }

    // ============ METODOS AUXILIARES DE UI Y ESTILOS ============

    private void mostrarAviso(String mensaje) {
        JOptionPane.showMessageDialog(this, mensaje, "Validación", JOptionPane.INFORMATION_MESSAGE);
    }

    private void mostrarError(String mensaje, Exception e) {
        JOptionPane.showMessageDialog(this, mensaje + "\n" + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
    }

    private void estilizarTabla(JTable tabla) {
        tabla.setFillsViewportHeight(true);
        tabla.setRowHeight(26);
        tabla.setGridColor(new Color(233, 221, 231));
        tabla.setBackground(Color.WHITE);
        tabla.setForeground(TEXTO_TABLA);
        tabla.getTableHeader().setBackground(ENCABEZADO_TABLA);
        tabla.getTableHeader().setForeground(TEXTO_TABLA);
        tabla.getTableHeader().setReorderingAllowed(false);
    }

    private void configurarCampoAutomatico(JTextField campo) {
        campo.setEditable(false);
        campo.setBackground(new Color(241, 236, 230));
    }

    private JPanel panelBase() {
        JPanel panel = new JPanel();
        panel.setBackground(FONDO);
        panel.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));
        return panel;
    }

    private JPanel tarjeta() {
        JPanel panel = panelBase();
        panel.setBackground(TARJETA);
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BOTON_BORDE),
                BorderFactory.createEmptyBorder(12, 12, 12, 12)
        ));
        return panel;
    }

    private JPanel panelBotones() {
        JPanel panel = new JPanel();
        panel.setBackground(FONDO);
        return panel;
    }

    private JButton boton(String texto, java.awt.event.ActionListener accion) {
        JButton boton = new JButton(texto);
        boton.setBackground(BOTON);
        boton.setBorder(BorderFactory.createLineBorder(BOTON_BORDE));
        boton.setFocusPainted(false);
        boton.addActionListener(accion);
        return boton;
    }

    private JLabel etiqueta(String texto) {
        JLabel label = new JLabel(texto);
        label.setForeground(new Color(80, 80, 80));
        return label;
    }

    private GridBagConstraints baseConstraints() {
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 6, 6, 6);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        return gbc;
    }

    private void agregarCampo(JPanel panel, GridBagConstraints gbc, int x, int y, String etiqueta, java.awt.Component componente) {
        gbc.gridx = x;
        gbc.gridy = y;
        gbc.weightx = 0;
        panel.add(this.etiqueta(etiqueta), gbc);
        gbc.gridx = x + 1;
        gbc.weightx = 1;
        panel.add(componente, gbc);
    }

    // ============ CLASE INTERNA - ITEM COMBO ============

    private static class ItemCombo {
        private final int id;
        private final String nombre;

        private ItemCombo(int id, String nombre) {
            this.id = id;
            this.nombre = nombre;
        }

        @Override
        public String toString() {
            return nombre + " (#" + id + ")";
        }
    }
}

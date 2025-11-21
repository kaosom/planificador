import javax.swing.*;
import java.awt.*;

public class PlanificadorGUI extends JFrame {
    private ServidorManager servidorManager;
    private RPCCliente clienteActual;
    private ProcesosImpl planificador;
    
    private JButton btnIniciarServidor;
    private JButton btnDetenerServidor;
    private JLabel lblEstadoServidor;
    private JTextArea logArea;
    
    private JTextField txtClienteId;
    private JButton btnConectarCliente;
    private JTextField txtNombreProceso;
    private JTextField txtTiempoCreacion;
    private JTextField txtTiempoCPU;
    private JButton btnSolicitarProceso;
    
    private GanttChartPanel ganttDinamico;
    private GanttChartPanelStatic ganttEstatico;
    private JTabbedPane tabbedPane;
    private TablaPanel panelColaEspera;
    private TablaPanel panelColaRechazo;
    private TablaPanel panelTablaProcesos;
    private TablaPanel panelTiempoEspera;
    private TablaPanel panelTiempoFinalizacion;
    private TablaPanel panelPenalizacion;
    private TablaPanel panelResumen;
    
    private javax.swing.Timer timerActualizacion;
    private int tiempoVisualActual;
    
    public PlanificadorGUI() {
        super("Planificador de Procesos");
        planificador = new ProcesosImpl();
        ProcesosImpl.setInstanciaCompartida(planificador);
        servidorManager = new ServidorManager(planificador);
        tiempoVisualActual = 0;
        
        inicializarComponentes();
        configurarLayout();
        configurarEventos();
        iniciarActualizacionAutomatica();
        
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1400, 900);
        setLocationRelativeTo(null);
    }
    
    private void inicializarComponentes() {
        btnIniciarServidor = new JButton("Arrancar Servidor");
        btnDetenerServidor = new JButton("Parar Servidor");
        btnDetenerServidor.setEnabled(false);
        lblEstadoServidor = new JLabel("Servidor parado");
        lblEstadoServidor.setForeground(Color.RED);
        logArea = new JTextArea(5, 30);
        logArea.setEditable(false);
        logArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
        
        txtClienteId = new JTextField(10);
        btnConectarCliente = new JButton("Conectar");
        txtNombreProceso = new JTextField(10);
        txtTiempoCreacion = new JTextField(5);
        txtTiempoCPU = new JTextField(5);
        btnSolicitarProceso = new JButton("Agregar Proceso");
        
        ganttDinamico = new GanttChartPanel(planificador);
        ganttEstatico = new GanttChartPanelStatic(planificador);
        
        panelColaEspera = new TablaPanel();
        panelColaRechazo = new TablaPanel();
        panelTablaProcesos = new TablaPanel();
        panelTiempoEspera = new TablaPanel();
        panelTiempoFinalizacion = new TablaPanel();
        panelPenalizacion = new TablaPanel();
        panelResumen = new TablaPanel();
        
        tabbedPane = new JTabbedPane();
        tabbedPane.addTab("En Espera", panelColaEspera);
        tabbedPane.addTab("Rechazados", panelColaRechazo);
        tabbedPane.addTab("Procesos", panelTablaProcesos);
        tabbedPane.addTab("Tiempo Espera", panelTiempoEspera);
        tabbedPane.addTab("Tiempo Final", panelTiempoFinalizacion);
        tabbedPane.addTab("Penalización", panelPenalizacion);
        tabbedPane.addTab("Resumen", panelResumen);
    }
    
    private void configurarLayout() {
        setLayout(new BorderLayout());
        
        JPanel panelSuperior = new JPanel(new BorderLayout());
        JPanel panelControlServidor = new JPanel(new FlowLayout(FlowLayout.LEFT));
        panelControlServidor.add(btnIniciarServidor);
        panelControlServidor.add(btnDetenerServidor);
        panelControlServidor.add(lblEstadoServidor);
        panelSuperior.add(panelControlServidor, BorderLayout.WEST);
        
        JScrollPane scrollLog = new JScrollPane(logArea);
        scrollLog.setPreferredSize(new Dimension(400, 100));
        panelSuperior.add(scrollLog, BorderLayout.EAST);
        
        add(panelSuperior, BorderLayout.NORTH);
        
        JPanel panelIzquierdo = crearPanelCliente();
        add(panelIzquierdo, BorderLayout.WEST);
        
        JPanel panelCentral = new JPanel(new BorderLayout());
        JLabel lblDinamico = new JLabel("Gantt en Tiempo Real", JLabel.CENTER);
        lblDinamico.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 14));
        panelCentral.add(lblDinamico, BorderLayout.NORTH);
        panelCentral.add(new JScrollPane(ganttDinamico, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, 
                                         JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED), BorderLayout.CENTER);
        add(panelCentral, BorderLayout.CENTER);
        
        add(tabbedPane, BorderLayout.EAST);
        
        JPanel panelInferior = new JPanel(new BorderLayout());
        JLabel lblEstatico = new JLabel("Gantt Final", JLabel.CENTER);
        lblEstatico.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 14));
        panelInferior.add(lblEstatico, BorderLayout.NORTH);
        panelInferior.add(new JScrollPane(ganttEstatico, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, 
                                         JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED), BorderLayout.CENTER);
        panelInferior.setPreferredSize(new Dimension(0, 250));
        add(panelInferior, BorderLayout.SOUTH);
    }
    
    private JPanel crearPanelCliente() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(BorderFactory.createTitledBorder("Cliente"));
        panel.setPreferredSize(new Dimension(250, 0));
        
        JPanel panelId = new JPanel(new FlowLayout(FlowLayout.LEFT));
        panelId.add(new JLabel("ID:"));
        panelId.add(txtClienteId);
        panelId.add(btnConectarCliente);
        panel.add(panelId);
        
        panel.add(Box.createVerticalStrut(10));
        
        JPanel panelSolicitud = new JPanel();
        panelSolicitud.setLayout(new BoxLayout(panelSolicitud, BoxLayout.Y_AXIS));
        panelSolicitud.setBorder(BorderFactory.createTitledBorder("Nuevo Proceso"));
        
        JPanel p1 = new JPanel(new FlowLayout(FlowLayout.LEFT));
        p1.add(new JLabel("Nombre:"));
        p1.add(txtNombreProceso);
        panelSolicitud.add(p1);
        
        JPanel p2 = new JPanel(new FlowLayout(FlowLayout.LEFT));
        p2.add(new JLabel("Tiempo Creación:"));
        p2.add(txtTiempoCreacion);
        panelSolicitud.add(p2);
        
        JPanel p3 = new JPanel(new FlowLayout(FlowLayout.LEFT));
        p3.add(new JLabel("Tiempo CPU:"));
        p3.add(txtTiempoCPU);
        panelSolicitud.add(p3);
        
        panelSolicitud.add(btnSolicitarProceso);
        panel.add(panelSolicitud);
        
        return panel;
    }
    
    private void configurarEventos() {
        btnIniciarServidor.addActionListener(e -> {
            try {
                servidorManager.iniciarServidor();
                btnIniciarServidor.setEnabled(false);
                btnDetenerServidor.setEnabled(true);
                lblEstadoServidor.setText("Servidor activo");
                lblEstadoServidor.setForeground(Color.GREEN);
                tiempoVisualActual = 0;
                agregarLog("Servidor arrancado en puerto 8080");
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "No se pudo arrancar el servidor: " + ex.getMessage(), 
                                             "Error", JOptionPane.ERROR_MESSAGE);
                agregarLog("Error: " + ex.getMessage());
            }
        });
        
        btnDetenerServidor.addActionListener(e -> {
            try {
                servidorManager.detenerServidor();
                btnIniciarServidor.setEnabled(true);
                btnDetenerServidor.setEnabled(false);
                lblEstadoServidor.setText("Servidor parado");
                lblEstadoServidor.setForeground(Color.RED);
                tiempoVisualActual = 0;
                agregarLog("Servidor parado");
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "No se pudo parar el servidor: " + ex.getMessage(), 
                                             "Error", JOptionPane.ERROR_MESSAGE);
            }
        });
        
        btnConectarCliente.addActionListener(e -> {
            String clienteId = txtClienteId.getText().trim();
            if (clienteId.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Pon un ID de cliente", 
                                             "Aviso", JOptionPane.WARNING_MESSAGE);
                return;
            }
            try {
                clienteActual = new RPCCliente(clienteId);
                agregarLog("Cliente " + clienteId + " conectado");
                JOptionPane.showMessageDialog(this, "Cliente " + clienteId + " conectado", 
                                             "Listo", JOptionPane.INFORMATION_MESSAGE);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "No se pudo conectar: " + ex.getMessage(), 
                                             "Error", JOptionPane.ERROR_MESSAGE);
                agregarLog("Error: " + ex.getMessage());
            }
        });
        
        btnSolicitarProceso.addActionListener(e -> {
            if (clienteActual == null) {
                JOptionPane.showMessageDialog(this, "Conecta un cliente primero", 
                                             "Aviso", JOptionPane.WARNING_MESSAGE);
                return;
            }
            try {
                String nombre = txtNombreProceso.getText().trim();
                int tiempoCreacion = Integer.parseInt(txtTiempoCreacion.getText().trim());
                int tiempoCPU = Integer.parseInt(txtTiempoCPU.getText().trim());
                
                clienteActual.solicitarProceso(nombre, tiempoCreacion, tiempoCPU);
                agregarLog("Proceso " + nombre + " agregado");
                
                txtNombreProceso.setText("");
                txtTiempoCreacion.setText("");
                txtTiempoCPU.setText("");
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "Pon números válidos", 
                                             "Error", JOptionPane.ERROR_MESSAGE);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage(), 
                                             "Error", JOptionPane.ERROR_MESSAGE);
            }
        });
    }
    
    private void iniciarActualizacionAutomatica() {
        timerActualizacion = new javax.swing.Timer(1000, e -> {
            if (servidorManager.isServidorActivo()) {
                tiempoVisualActual++;
                if (tiempoVisualActual > 30) tiempoVisualActual = 0;
            }
            actualizarVista();
        });
        timerActualizacion.start();
    }
    
    private void actualizarVista() {
        SwingUtilities.invokeLater(() -> {
            ganttDinamico.setTiempoActual(tiempoVisualActual);
            ganttDinamico.repaint();
            ganttEstatico.repaint();
            
            actualizarTabla(panelColaEspera, planificador.obtenerColaEspera());
            actualizarTabla(panelColaRechazo, planificador.obtenerColaRechazo());
            actualizarTabla(panelTablaProcesos, planificador.obtenerTablaProcesos());
            actualizarTabla(panelTiempoEspera, planificador.obtenerTablaTiempoEspera());
            actualizarTabla(panelTiempoFinalizacion, planificador.obtenerTablaTiempoFinalizacion());
            actualizarTabla(panelPenalizacion, planificador.obtenerTablaPenalizacion());
            actualizarTabla(panelResumen, planificador.obtenerTablaResumen());
        });
    }
    
    private void actualizarTabla(TablaPanel panel, java.util.List<String> datos) {
        if (datos == null || datos.isEmpty()) {
            panel.actualizarDatos(new String[0][0], new String[0]);
            return;
        }
        
        String[] headers = extraerHeaders(datos);
        String[][] rows = extraerFilas(datos);
        panel.actualizarDatos(rows, headers);
    }
    
    private String[] extraerHeaders(java.util.List<String> datos) {
        if (datos.size() < 2) return new String[0];
        String headerLine = datos.get(1);
        String[] partes = headerLine.split("\\|");
        java.util.List<String> headers = new java.util.ArrayList<>();
        for (String parte : partes) {
            String limpio = parte.trim();
            if (!limpio.isEmpty() && !limpio.matches("[+-]+")) {
                headers.add(limpio);
            }
        }
        return headers.toArray(new String[0]);
    }
    
    private String[][] extraerFilas(java.util.List<String> datos) {
        java.util.List<String[]> filas = new java.util.ArrayList<>();
        for (int i = 2; i < datos.size() - 1; i++) {
            String linea = datos.get(i);
            if (linea.contains("|") && !linea.matches(".*[+-]{3,}.*")) {
                String[] partes = linea.split("\\|");
                java.util.List<String> valores = new java.util.ArrayList<>();
                for (String parte : partes) {
                    String limpio = parte.trim();
                    if (!limpio.isEmpty() && !limpio.matches("[+-]+")) {
                        valores.add(limpio);
                    }
                }
                if (valores.size() > 1) {
                    filas.add(valores.toArray(new String[0]));
                }
            }
        }
        return filas.toArray(new String[0][]);
    }
    
    private void agregarLog(String mensaje) {
        SwingUtilities.invokeLater(() -> {
            logArea.append("[" + new java.util.Date() + "] " + mensaje + "\n");
            logArea.setCaretPosition(logArea.getDocument().getLength());
        });
    }
    
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception e) {
                e.printStackTrace();
            }
            new PlanificadorGUI().setVisible(true);
        });
    }
}


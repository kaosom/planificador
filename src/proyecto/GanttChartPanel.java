import javax.swing.*;
import java.awt.*;
import java.util.*;

public class GanttChartPanel extends JPanel {
    private ProcesosImpl planificador;
    private int tiempoActualVisual;
    private static final int CELL_WIDTH = 20;
    private static final int ROW_HEIGHT = 30;
    private static final int HEADER_HEIGHT = 40;
    private static final int PROCESS_NAME_WIDTH = 100;
    private static final int TIEMPO_TOTAL = 30;
    
    public GanttChartPanel(ProcesosImpl planificador) {
        this.planificador = planificador;
        this.tiempoActualVisual = 0;
        setPreferredSize(new Dimension((TIEMPO_TOTAL + 1) * CELL_WIDTH + PROCESS_NAME_WIDTH, 400));
        setBackground(Color.WHITE);
    }
    
    public void setTiempoActual(int tiempo) {
        this.tiempoActualVisual = tiempo;
    }
    
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        
        if (planificador == null) return;
        
        java.util.List<ProcesoInfo> procesos = obtenerProcesos();
        
        dibujarHeader(g2d);
        dibujarLeyenda(g2d);
        
        int y = HEADER_HEIGHT + 40;
        for (int i = 0; i < procesos.size(); i++) {
            ProcesoInfo proceso = procesos.get(i);
            dibujarFilaProceso(g2d, proceso, y, tiempoActualVisual);
            y += ROW_HEIGHT;
        }
        
        dibujarLineaTiempoActual(g2d, tiempoActualVisual);
    }
    
    private void dibujarHeader(Graphics2D g2d) {
        g2d.setColor(Color.BLACK);
        g2d.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 12));
        
        g2d.drawString("Proceso", 5, 20);
        
        int x = PROCESS_NAME_WIDTH;
        for (int i = 0; i <= TIEMPO_TOTAL; i++) {
            g2d.drawString(String.valueOf(i), x + CELL_WIDTH / 2 - 5, 20);
            g2d.drawLine(x, HEADER_HEIGHT, x, getHeight());
            x += CELL_WIDTH;
        }
        
        g2d.drawLine(PROCESS_NAME_WIDTH, HEADER_HEIGHT, x, HEADER_HEIGHT);
    }
    
    private void dibujarLeyenda(Graphics2D g2d) {
        int y = HEADER_HEIGHT + 20;
        int x = PROCESS_NAME_WIDTH + 5;
        
        g2d.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 10));
        
        g2d.setColor(Color.YELLOW);
        g2d.fillRect(x, y - 10, 15, 10);
        g2d.setColor(Color.BLACK);
        g2d.drawRect(x, y - 10, 15, 10);
        g2d.drawString("Solicitud", x + 20, y);
        
        x += 100;
        g2d.setColor(Color.GREEN);
        g2d.fillRect(x, y - 10, 15, 10);
        g2d.setColor(Color.BLACK);
        g2d.drawRect(x, y - 10, 15, 10);
        g2d.drawString("Ejecutando", x + 20, y);
        
        x += 100;
        g2d.setColor(Color.ORANGE);
        g2d.fillRect(x, y - 10, 15, 10);
        g2d.setColor(Color.BLACK);
        g2d.drawRect(x, y - 10, 15, 10);
        g2d.drawString("Esperando", x + 20, y);
        
        x += 100;
        g2d.setColor(Color.RED);
        g2d.setStroke(new BasicStroke(2));
        g2d.drawLine(x, y - 5, x + 15, y - 5);
        g2d.setStroke(new BasicStroke(1));
        g2d.drawString("Ahora", x + 20, y);
    }
    
    private void dibujarFilaProceso(Graphics2D g2d, ProcesoInfo proceso, int y, int tiempoActual) {
        g2d.setColor(Color.BLACK);
        g2d.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 11));
        g2d.drawString(proceso.nombre, 5, y + ROW_HEIGHT / 2 + 5);
        
        int x = PROCESS_NAME_WIDTH;
        for (int t = 0; t <= TIEMPO_TOTAL; t++) {
            if (t == proceso.tiempoCreacion) {
                g2d.setColor(Color.YELLOW);
                g2d.fillRect(x + 2, y + 2, CELL_WIDTH - 4, ROW_HEIGHT - 4);
                g2d.setColor(Color.BLACK);
                g2d.drawString("X", x + CELL_WIDTH / 2 - 3, y + ROW_HEIGHT / 2 + 5);
            } else if (proceso.tiempoAsignacion != -1 && 
                      t >= proceso.tiempoAsignacion && 
                      t < proceso.tiempoAsignacion + proceso.tiempoEjecucion) {
                if (proceso.estado == ProcesosImpl.EstadoProceso.EJECUCION) {
                    g2d.setColor(Color.GREEN);
                } else {
                    g2d.setColor(new Color(200, 200, 200));
                }
                g2d.fillRect(x + 2, y + 2, CELL_WIDTH - 4, ROW_HEIGHT - 4);
            } else if (proceso.estado == ProcesosImpl.EstadoProceso.PREPARADO && 
                      t > proceso.tiempoCreacion && 
                      (proceso.tiempoAsignacion == -1 || t < proceso.tiempoAsignacion)) {
                g2d.setColor(Color.ORANGE);
                g2d.fillRect(x + 2, y + 2, CELL_WIDTH - 4, ROW_HEIGHT - 4);
            }
            
            g2d.setColor(Color.LIGHT_GRAY);
            g2d.drawRect(x, y, CELL_WIDTH, ROW_HEIGHT);
            
            x += CELL_WIDTH;
        }
    }
    
    private void dibujarLineaTiempoActual(Graphics2D g2d, int tiempoActual) {
        if (tiempoActual < 0 || tiempoActual > TIEMPO_TOTAL) return;
        
        int x = PROCESS_NAME_WIDTH + tiempoActual * CELL_WIDTH;
        g2d.setColor(Color.RED);
        g2d.setStroke(new BasicStroke(3));
        g2d.drawLine(x, HEADER_HEIGHT, x, getHeight());
        g2d.setColor(new Color(255, 0, 0, 100));
        g2d.fillRect(x - 2, HEADER_HEIGHT, 4, getHeight() - HEADER_HEIGHT);
        g2d.setStroke(new BasicStroke(1));
    }
    
    private java.util.List<ProcesoInfo> obtenerProcesos() {
        java.util.List<ProcesoInfo> procesos = new ArrayList<>();
        
        if (planificador == null) return procesos;
        
        java.util.Map<String, ProcesosImpl.ProcesoInfo> mapaProcesos = planificador.obtenerProcesosParaGUI();
        for (ProcesosImpl.ProcesoInfo p : mapaProcesos.values()) {
            procesos.add(new ProcesoInfo(p.nombre, p.clienteId, p.tiempoCreacion, 
                                        p.tiempoEjecucion, p.estado, p.tiempoAsignacion));
        }
        
        procesos.sort((a, b) -> a.nombre.compareTo(b.nombre));
        return procesos;
    }
    
    private static class ProcesoInfo {
        String nombre;
        String clienteId;
        int tiempoCreacion;
        int tiempoEjecucion;
        ProcesosImpl.EstadoProceso estado;
        int tiempoAsignacion;
        
        ProcesoInfo(String nombre, String clienteId, int tiempoCreacion, int tiempoEjecucion,
                   ProcesosImpl.EstadoProceso estado, int tiempoAsignacion) {
            this.nombre = nombre;
            this.clienteId = clienteId;
            this.tiempoCreacion = tiempoCreacion;
            this.tiempoEjecucion = tiempoEjecucion;
            this.estado = estado;
            this.tiempoAsignacion = tiempoAsignacion;
        }
    }
}


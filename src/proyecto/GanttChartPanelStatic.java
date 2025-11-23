import javax.swing.*;
import java.awt.*;
import java.util.*;

public class GanttChartPanelStatic extends JPanel {
    private ProcesosImpl planificador;
    private static final int CELL_WIDTH = 20;
    private static final int ROW_HEIGHT = 25;
    private static final int HEADER_HEIGHT = 30;
    private static final int PROCESS_NAME_WIDTH = 100;
    private static final int TIEMPO_TOTAL = 30;
    
    public GanttChartPanelStatic(ProcesosImpl planificador) {
        this.planificador = planificador;
        setPreferredSize(new Dimension((TIEMPO_TOTAL + 1) * CELL_WIDTH + PROCESS_NAME_WIDTH, 300));
        setBackground(Color.WHITE);
    }
    
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        
        if (planificador == null) return;
        
        java.util.List<ProcesoInfo> procesos = obtenerProcesos();
        
        dibujarHeader(g2d);
        
        int y = HEADER_HEIGHT;
        for (int i = 0; i < procesos.size(); i++) {
            ProcesoInfo proceso = procesos.get(i);
            dibujarFilaProceso(g2d, proceso, y);
            y += ROW_HEIGHT;
        }
    }
    
    private void dibujarHeader(Graphics2D g2d) {
        g2d.setColor(Color.BLACK);
        g2d.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 11));
        
        g2d.drawString("Proceso", 5, 18);
        
        int x = PROCESS_NAME_WIDTH;
        for (int i = 0; i <= TIEMPO_TOTAL; i++) {
            if (i % 5 == 0) {
                g2d.drawString(String.valueOf(i), x + CELL_WIDTH / 2 - 5, 18);
            }
            g2d.drawLine(x, HEADER_HEIGHT, x, getHeight());
            x += CELL_WIDTH;
        }
        
        g2d.drawLine(PROCESS_NAME_WIDTH, HEADER_HEIGHT, x, HEADER_HEIGHT);
    }
    
    private void dibujarFilaProceso(Graphics2D g2d, ProcesoInfo proceso, int y) {
        g2d.setColor(Color.BLACK);
        g2d.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 10));
        g2d.drawString(proceso.nombre, 5, y + ROW_HEIGHT / 2 + 4);
        
        int x = PROCESS_NAME_WIDTH;
        for (int t = 0; t <= TIEMPO_TOTAL; t++) {
            if (t == proceso.tiempoCreacion) {
                g2d.setColor(Color.YELLOW);
                g2d.fillRect(x + 1, y + 1, CELL_WIDTH - 2, ROW_HEIGHT - 2);
                g2d.setColor(Color.BLACK);
                g2d.drawString("X", x + CELL_WIDTH / 2 - 3, y + ROW_HEIGHT / 2 + 4);
            } else if (proceso.tiempoInicioReal != -1 && 
                      t >= proceso.tiempoInicioReal && 
                      t < proceso.tiempoFinReal) {
                if (proceso.estado == ProcesosImpl.EstadoProceso.FINALIZADO) {
                    g2d.setColor(Color.GREEN);
                } else if (proceso.estado == ProcesosImpl.EstadoProceso.EJECUCION) {
                    g2d.setColor(new Color(100, 200, 100));
                } else {
                    g2d.setColor(new Color(200, 200, 200));
                }
                g2d.fillRect(x + 1, y + 1, CELL_WIDTH - 2, ROW_HEIGHT - 2);
            } else if (proceso.estado == ProcesosImpl.EstadoProceso.PREPARADO && 
                      t > proceso.tiempoCreacion && 
                      (proceso.tiempoAsignacion == -1 || t < proceso.tiempoAsignacion)) {
                g2d.setColor(Color.ORANGE);
                g2d.fillRect(x + 1, y + 1, CELL_WIDTH - 2, ROW_HEIGHT - 2);
            }
            
            g2d.setColor(Color.LIGHT_GRAY);
            g2d.drawRect(x, y, CELL_WIDTH, ROW_HEIGHT);
            
            x += CELL_WIDTH;
        }
    }
    
    private java.util.List<ProcesoInfo> obtenerProcesos() {
        java.util.List<ProcesoInfo> procesos = new ArrayList<>();
        
        if (planificador == null) return procesos;
        
        java.util.List<ProcesosImpl.ProcesoInfo> procesosOrdenados = planificador.obtenerProcesosOrdenadosPorEntrada();
        
        java.util.Map<String, ProcesosImpl.TiempoEjecucionInfo> tiemposEjecucion = planificador.obtenerTiemposEjecucionSecuenciales();
        
        for (ProcesosImpl.ProcesoInfo p : procesosOrdenados) {
            String clave = p.clienteId + "_" + p.nombre;
            ProcesosImpl.TiempoEjecucionInfo tiempoEjec = tiemposEjecucion.get(clave);
            
            ProcesoInfo info = new ProcesoInfo(p.nombre, p.clienteId, p.tiempoCreacion, 
                                              p.tiempoEjecucion, p.estado, p.tiempoAsignacion);
            if (tiempoEjec != null) {
                info.tiempoInicioReal = tiempoEjec.tiempoInicioReal;
                info.tiempoFinReal = tiempoEjec.tiempoFinReal;
            }
            procesos.add(info);
        }
        
        return procesos;
    }
    
    private static class ProcesoInfo {
        String nombre;
        String clienteId;
        int tiempoCreacion;
        int tiempoEjecucion;
        ProcesosImpl.EstadoProceso estado;
        int tiempoAsignacion;
        int tiempoInicioReal = -1;
        int tiempoFinReal = -1;
        
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


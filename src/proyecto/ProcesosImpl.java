import java.util.*;
import java.io.*;

public class ProcesosImpl {
    private static final int TIEMPO_TOTAL = 30;
    private static final int TIEMPO_LIMITE_CLIENTE = 5;
    private static final int MAX_INTENTOS = 3;
    private static final int ESPERA_REINTENTO = 3;
    private static final int TAMANO_COLA_ESPERA = 10;
    
    private Map<String, Proceso> tablaProcesos;
    private Queue<Proceso> colaEspera;
    private List<Proceso> colaRechazo;
    private List<ProcesoCompletado> procesosCompletados;
    private Map<String, Integer> ultimaActividadCliente;
    private Proceso procesoEnEjecucion;
    private int tiempoActual;
    
    public enum EstadoProceso {
        CREACION, PREPARADO, EJECUCION, FINALIZADO, ELIMINADO
    }
    
    private class Proceso {
        String nombre;
        String clienteId;
        int tiempoCreacion;
        int tiempoEjecucion;
        EstadoProceso estado;
        int tiempoAsignacion;
        int tiempoFinalizacion;
        int intentosRechazo;
        int tiempoUltimoIntento;
        
        Proceso(String nombre, String clienteId, int tiempoCreacion, int tiempoEjecucion) {
            this.nombre = nombre;
            this.clienteId = clienteId;
            this.tiempoCreacion = tiempoCreacion;
            this.tiempoEjecucion = tiempoEjecucion;
            this.estado = EstadoProceso.CREACION;
            this.tiempoAsignacion = -1;
            this.tiempoFinalizacion = -1;
            this.intentosRechazo = 0;
            this.tiempoUltimoIntento = tiempoCreacion;
        }
    }
    
    private class ProcesoCompletado {
        String nombre;
        String clienteId;
        int tiempoPeticion;
        int tiempoAsignacion;
        int tiempoFinalizacion;
        int tiempoEjecucion;
        
        ProcesoCompletado(Proceso proceso) {
            this.nombre = proceso.nombre;
            this.clienteId = proceso.clienteId;
            this.tiempoPeticion = proceso.tiempoCreacion;
            this.tiempoAsignacion = proceso.tiempoAsignacion;
            this.tiempoFinalizacion = proceso.tiempoFinalizacion;
            this.tiempoEjecucion = proceso.tiempoEjecucion;
        }
    }
    
    private static ProcesosImpl instanciaCompartida;
    
    public ProcesosImpl() {
        if (instanciaCompartida != null) {
            this.tablaProcesos = instanciaCompartida.tablaProcesos;
            this.colaEspera = instanciaCompartida.colaEspera;
            this.colaRechazo = instanciaCompartida.colaRechazo;
            this.procesosCompletados = instanciaCompartida.procesosCompletados;
            this.ultimaActividadCliente = instanciaCompartida.ultimaActividadCliente;
            this.procesoEnEjecucion = instanciaCompartida.procesoEnEjecucion;
            this.tiempoActual = instanciaCompartida.tiempoActual;
        } else {
            tablaProcesos = new HashMap<>();
            colaEspera = new LinkedList<>();
            colaRechazo = new ArrayList<>();
            procesosCompletados = new ArrayList<>();
            ultimaActividadCliente = new HashMap<>();
            procesoEnEjecucion = null;
            tiempoActual = 0;
        }
    }
    
    public static void setInstanciaCompartida(ProcesosImpl instancia) {
        instanciaCompartida = instancia;
    }
    
    public static ProcesosImpl getInstanciaCompartida() {
        return instanciaCompartida;
    }
    
    public synchronized int getTiempoActual() {
        return tiempoActual;
    }
    
    public static class ProcesoInfo {
        public String nombre;
        public String clienteId;
        public int tiempoCreacion;
        public int tiempoEjecucion;
        public EstadoProceso estado;
        public int tiempoAsignacion;
        public int tiempoFinalizacion;
        
        public ProcesoInfo(String nombre, String clienteId, int tiempoCreacion, int tiempoEjecucion,
                          EstadoProceso estado, int tiempoAsignacion, int tiempoFinalizacion) {
            this.nombre = nombre;
            this.clienteId = clienteId;
            this.tiempoCreacion = tiempoCreacion;
            this.tiempoEjecucion = tiempoEjecucion;
            this.estado = estado;
            this.tiempoAsignacion = tiempoAsignacion;
            this.tiempoFinalizacion = tiempoFinalizacion;
        }
    }
    
    public synchronized Map<String, ProcesoInfo> obtenerProcesosParaGUI() {
        Map<String, ProcesoInfo> procesos = new HashMap<>();
        for (Map.Entry<String, Proceso> entry : tablaProcesos.entrySet()) {
            Proceso proceso = entry.getValue();
            if (proceso.estado != EstadoProceso.ELIMINADO) {
                procesos.put(entry.getKey(), new ProcesoInfo(
                    proceso.nombre,
                    proceso.clienteId,
                    proceso.tiempoCreacion,
                    proceso.tiempoEjecucion,
                    proceso.estado,
                    proceso.tiempoAsignacion,
                    proceso.tiempoFinalizacion
                ));
            }
        }
        return procesos;
    }
    
    public synchronized String solicitarProceso(String clienteId, String nombre, int tiempoCreacion, int tiempoEjecucion) {
        validarConsistencia();
        
        actualizarActividadCliente(clienteId, tiempoCreacion);
        verificarClientesInactivos(tiempoCreacion);
        
        String claveProceso = clienteId + "_" + nombre;
        
        if (tablaProcesos.containsKey(claveProceso)) {
            Proceso procesoExistente = tablaProcesos.get(claveProceso);
            if (procesoExistente.estado == EstadoProceso.ELIMINADO) {
                tablaProcesos.remove(claveProceso);
            } else {
                return "Proceso " + nombre + " ya existe";
            }
        }
        
        Proceso nuevoProceso = new Proceso(nombre, clienteId, tiempoCreacion, tiempoEjecucion);
        nuevoProceso.estado = EstadoProceso.CREACION;
        
        if (tiempoCreacion > tiempoActual) {
            tiempoActual = tiempoCreacion;
        }
        
        if (procesoEnEjecucion == null && colaEspera.isEmpty()) {
            nuevoProceso.estado = EstadoProceso.EJECUCION;
            nuevoProceso.tiempoAsignacion = tiempoActual;
            procesoEnEjecucion = nuevoProceso;
            tiempoActual += nuevoProceso.tiempoEjecucion;
            tablaProcesos.put(claveProceso, nuevoProceso);
            return "Proceso " + nombre + " aceptado y en ejecucion (Intento 1 exitoso)";
        } else if (colaEspera.size() < TAMANO_COLA_ESPERA) {
            nuevoProceso.estado = EstadoProceso.PREPARADO;
            colaEspera.add(nuevoProceso);
            tablaProcesos.put(claveProceso, nuevoProceso);
            return "Proceso " + nombre + " agregado a cola de espera (Intento 1 exitoso)";
        } else {
            nuevoProceso.intentosRechazo = 1;
            nuevoProceso.tiempoUltimoIntento = tiempoActual;
            colaRechazo.add(nuevoProceso);
            tablaProcesos.put(claveProceso, nuevoProceso);
            return "Proceso " + nombre + " rechazado - cola llena (Intento 1 fallido)";
        }
    }
    
    public synchronized String reportarCompletado(String clienteId, String nombre, int tiempoFinalizacion) {
        String claveProceso = clienteId + "_" + nombre;
        Proceso proceso = tablaProcesos.get(claveProceso);
        
        if (proceso != null && proceso == procesoEnEjecucion) {
            proceso.estado = EstadoProceso.FINALIZADO;
            proceso.tiempoFinalizacion = tiempoFinalizacion;
            procesosCompletados.add(new ProcesoCompletado(proceso));
            
            colaEspera.remove(proceso);
            colaRechazo.remove(proceso);
            procesoEnEjecucion = null;
            
            procesarSiguienteProceso();
            return "Proceso " + nombre + " completado exitosamente";
        }
        return "Error: Proceso " + nombre + " no encontrado o no esta en ejecucion";
    }
    
    private synchronized void procesarSiguienteProceso() {
        limpiarColaEspera();
        
        if (!colaEspera.isEmpty()) {
            Proceso siguiente = colaEspera.poll();
            if (siguiente.estado != EstadoProceso.ELIMINADO) {
                siguiente.estado = EstadoProceso.EJECUCION;
                siguiente.tiempoAsignacion = tiempoActual;
                procesoEnEjecucion = siguiente;
                tiempoActual += siguiente.tiempoEjecucion;
            }
        }
        
        procesarReintentos();
    }
    
    private synchronized void limpiarColaEspera() {
        colaEspera.removeIf(p -> p.estado == EstadoProceso.ELIMINADO || 
                                  p.estado == EstadoProceso.FINALIZADO ||
                                  p.estado == EstadoProceso.EJECUCION ||
                                  p == procesoEnEjecucion);
    }
    
    private synchronized void validarConsistencia() {
        if (procesoEnEjecucion != null) {
            colaEspera.remove(procesoEnEjecucion);
            colaRechazo.remove(procesoEnEjecucion);
        }
        
        limpiarColaEspera();
        
        for (Proceso proceso : tablaProcesos.values()) {
            if (proceso.estado == EstadoProceso.ELIMINADO) {
                colaEspera.remove(proceso);
                if (proceso == procesoEnEjecucion) {
                    procesoEnEjecucion = null;
                }
            }
        }
    }
    
    private synchronized void procesarReintentos() {
        List<Proceso> procesosParaReintentar = new ArrayList<>();
        
        for (Proceso proceso : colaRechazo) {
            if (proceso.intentosRechazo < MAX_INTENTOS) {
                if (tiempoActual - proceso.tiempoUltimoIntento >= ESPERA_REINTENTO) {
                    procesosParaReintentar.add(proceso);
                }
            } else {
                proceso.estado = EstadoProceso.ELIMINADO;
            }
        }
        
        colaRechazo.removeAll(procesosParaReintentar);
        
        for (Proceso proceso : procesosParaReintentar) {
            proceso.intentosRechazo++;
            proceso.tiempoUltimoIntento = tiempoActual;
            
            if (procesoEnEjecucion == null && colaEspera.isEmpty()) {
                proceso.estado = EstadoProceso.EJECUCION;
                proceso.tiempoAsignacion = tiempoActual;
                procesoEnEjecucion = proceso;
                tiempoActual += proceso.tiempoEjecucion;
            } else if (colaEspera.size() < TAMANO_COLA_ESPERA) {
                proceso.estado = EstadoProceso.PREPARADO;
                colaEspera.add(proceso);
            } else {
                colaRechazo.add(proceso);
            }
        }
    }
    
    private synchronized void actualizarActividadCliente(String clienteId, int tiempo) {
        ultimaActividadCliente.put(clienteId, tiempo);
    }
    
    private synchronized void verificarClientesInactivos(int tiempoActual) {
        List<String> clientesAEliminar = new ArrayList<>();
        
        for (Map.Entry<String, Integer> entry : ultimaActividadCliente.entrySet()) {
            if (tiempoActual - entry.getValue() > TIEMPO_LIMITE_CLIENTE) {
                clientesAEliminar.add(entry.getKey());
            }
        }
        
        for (String clienteId : clientesAEliminar) {
            eliminarCliente(clienteId);
        }
    }
    
    private synchronized void eliminarCliente(String clienteId) {
        ultimaActividadCliente.remove(clienteId);
        
        List<String> procesosAEliminar = new ArrayList<>();
        for (Map.Entry<String, Proceso> entry : tablaProcesos.entrySet()) {
            Proceso proceso = entry.getValue();
            if (proceso.clienteId.equals(clienteId)) {
                if (proceso.estado != EstadoProceso.EJECUCION && 
                    proceso.estado != EstadoProceso.FINALIZADO &&
                    proceso != procesoEnEjecucion &&
                    !colaEspera.contains(proceso)) {
                    procesosAEliminar.add(entry.getKey());
                }
            }
        }
        
        for (String clave : procesosAEliminar) {
            Proceso proceso = tablaProcesos.get(clave);
            proceso.estado = EstadoProceso.ELIMINADO;
            colaRechazo.remove(proceso);
        }
    }
    
    public synchronized String obtenerSiguienteProceso() {
        validarConsistencia();
        procesarReintentos();
        
        if (procesoEnEjecucion != null) {
            return "Proceso en ejecucion: " + procesoEnEjecucion.nombre + 
                   " (C=" + procesoEnEjecucion.tiempoCreacion + 
                   ", t=" + procesoEnEjecucion.tiempoEjecucion + 
                   ", asignado en T=" + procesoEnEjecucion.tiempoAsignacion + ")";
        } else if (!colaEspera.isEmpty()) {
            Proceso siguiente = colaEspera.peek();
            return "Siguiente proceso en cola: " + siguiente.nombre + 
                   " (C=" + siguiente.tiempoCreacion + 
                   ", t=" + siguiente.tiempoEjecucion + 
                   ", estado=" + siguiente.estado + ")";
        } else {
            return "No hay procesos en la cola de espera";
        }
    }
    
    public synchronized List<String> obtenerTablaGantt() {
        List<String> tabla = new ArrayList<>();
        tabla.add("+========== TABLA GANTT - PLANIFICADOR FIFO ==========+");
        
        List<Proceso> procesosOrdenados = new ArrayList<>(tablaProcesos.values());
        procesosOrdenados.sort((a, b) -> a.nombre.compareTo(b.nombre));
        
        StringBuilder header = new StringBuilder("| Proceso |");
        for (int i = 0; i <= TIEMPO_TOTAL; i++) {
            header.append(String.format("%3d", i));
        }
        header.append("|");
        tabla.add(header.toString());
        tabla.add("+---------+" + "-".repeat((TIEMPO_TOTAL + 1) * 3) + "+");
        
        for (Proceso proceso : procesosOrdenados) {
            if (proceso.estado == EstadoProceso.ELIMINADO) continue;
            
            StringBuilder fila = new StringBuilder("|    ");
            fila.append(String.format("%-4s", proceso.nombre)).append(" |");
            
            for (int t = 0; t <= TIEMPO_TOTAL; t++) {
                if (t == proceso.tiempoCreacion) {
                    fila.append(" X ");
                } else if (proceso.tiempoAsignacion != -1 && 
                          t >= proceso.tiempoAsignacion && 
                          t < proceso.tiempoAsignacion + proceso.tiempoEjecucion) {
                    fila.append("███");
                } else {
                    fila.append("   ");
                }
            }
            fila.append("|");
            tabla.add(fila.toString());
        }
        
        tabla.add("+---------+" + "-".repeat((TIEMPO_TOTAL + 1) * 3) + "+");
        tabla.add("Leyenda: X = Solicitud, ███ = Ejecucion");
        
        return tabla;
    }
    
    public synchronized List<String> obtenerColaEspera() {
        List<String> infoCola = new ArrayList<>();
        infoCola.add("+---------------------- COLA DE ESPERA FIFO ---------------------+");
        infoCola.add("| #  | Proceso | Cliente  |   C   |   t   |      Estado         |");
        infoCola.add("+----+---------+----------+-------+-------+---------------------+");
        
        int posicion = 1;
        for (Proceso proceso : colaEspera) {
            if (proceso.estado == EstadoProceso.ELIMINADO || 
                proceso.estado == EstadoProceso.EJECUCION ||
                proceso.estado == EstadoProceso.FINALIZADO ||
                proceso == procesoEnEjecucion) {
                continue;
            }
            
            String estado = proceso.estado == EstadoProceso.PREPARADO ? "En espera        " : 
                           proceso.estado.toString();
            infoCola.add(String.format("| %-2d |    %-4s |   %-6s |  %-3d  |  %-3d  | %-18s |", 
                posicion, proceso.nombre, proceso.clienteId, proceso.tiempoCreacion, 
                proceso.tiempoEjecucion, estado));
            posicion++;
        }
        
        if (posicion == 1) {
            infoCola.add("|                      COLA VACIA                           |");
        }
        
        infoCola.add("+-------------------------------------------------------------+");
        return infoCola;
    }
    
    public synchronized List<String> obtenerColaRechazo() {
        List<String> infoCola = new ArrayList<>();
        infoCola.add("+---------------------- COLA DE RECHAZO ---------------------+");
        infoCola.add("| #  | Proceso | Cliente  |   C   |   t   | Intentos | Estado |");
        infoCola.add("+----+---------+----------+-------+-------+----------+--------+");
        
        int posicion = 1;
        for (Proceso proceso : colaRechazo) {
            String estado = proceso.estado == EstadoProceso.ELIMINADO ? "Eliminado" : "Rechazado";
            infoCola.add(String.format("| %-2d |    %-4s |   %-6s |  %-3d  |  %-3d  |    %-3d    | %-6s |", 
                posicion, proceso.nombre, proceso.clienteId, proceso.tiempoCreacion, 
                proceso.tiempoEjecucion, proceso.intentosRechazo, estado));
            posicion++;
        }
        
        if (colaRechazo.isEmpty()) {
            infoCola.add("|                  COLA DE RECHAZO VACIA                  |");
        }
        
        infoCola.add("+-------------------------------------------------------------+");
        return infoCola;
    }
    
    public synchronized List<String> obtenerTablaProcesos() {
        List<String> tabla = new ArrayList<>();
        tabla.add("+------------------- TABLA DE PROCESOS -------------------+");
        tabla.add("| Proceso | Cliente |   C   |   t   | Estado    | T.Asignado |");
        tabla.add("+---------+---------+-------+-------+-----------+------------+");
        
        List<Proceso> procesosOrdenados = new ArrayList<>(tablaProcesos.values());
        procesosOrdenados.sort((a, b) -> a.nombre.compareTo(b.nombre));
        
        for (Proceso proceso : procesosOrdenados) {
            if (proceso.estado == EstadoProceso.ELIMINADO) continue;
            
            String tiempoAsignadoStr = proceso.tiempoAsignacion == -1 ? "   No    " : 
                                      String.format("   %-3d   ", proceso.tiempoAsignacion);
            String estadoStr = proceso.estado.toString();
            
            tabla.add(String.format("|    %-4s |   %-5s |  %-3d  |  %-3d  | %-9s | %s |", 
                proceso.nombre, proceso.clienteId, proceso.tiempoCreacion, 
                proceso.tiempoEjecucion, estadoStr, tiempoAsignadoStr));
        }
        
        tabla.add("+---------+---------+-------+-------+-----------+------------+");
        return tabla;
    }
    
    public synchronized List<String> obtenerTablaTiempoEspera() {
        List<String> tabla = new ArrayList<>();
        tabla.add("+------- TABLA DE TIEMPO DE ESPERA (E = T.Asignacion - C) -------+");
        tabla.add("| Proceso | Cliente |   C   |   t   | T.Asignado | T.Espera (E) |");
        tabla.add("+---------+---------+-------+-------+------------+--------------+");
        
        double sumaEspera = 0;
        int contador = 0;
        
        List<Proceso> procesosOrdenados = new ArrayList<>(tablaProcesos.values());
        procesosOrdenados.sort((a, b) -> a.nombre.compareTo(b.nombre));
        
        for (Proceso proceso : procesosOrdenados) {
            if (proceso.estado == EstadoProceso.ELIMINADO) continue;
            
            int tiempoEspera = 0;
            if (proceso.tiempoAsignacion != -1) {
                tiempoEspera = proceso.tiempoAsignacion - proceso.tiempoCreacion;
                sumaEspera += tiempoEspera;
                contador++;
            }
            
            String tiempoAsignadoStr = proceso.tiempoAsignacion == -1 ? "   No    " : 
                                      String.format("   %-3d   ", proceso.tiempoAsignacion);
            
            tabla.add(String.format("|    %-4s |   %-5s |  %-3d  |  %-3d  | %s |     %-3d       |", 
                proceso.nombre, proceso.clienteId, proceso.tiempoCreacion, 
                proceso.tiempoEjecucion, tiempoAsignadoStr, tiempoEspera));
        }
        
        tabla.add("+---------+---------+-------+-------+------------+--------------+");
        if (contador > 0) {
            tabla.add(String.format("| Promedio de Tiempo de Espera: %-18.2f |", sumaEspera / contador));
            tabla.add("+-------------------------------------------------+");
        }
        
        return tabla;
    }
    
    public synchronized List<String> obtenerTablaTiempoFinalizacion() {
        List<String> tabla = new ArrayList<>();
        tabla.add("+---------- TABLA DE TIEMPO DE FINALIZACION (F) ----------+");
        tabla.add("| Proceso | Cliente |   C   |   t   | T.Espera (E) | T.Final (F) |");
        tabla.add("+---------+---------+-------+-------+--------------+-------------+");
        
        double sumaFinalizacion = 0;
        int contador = 0;
        
        procesosCompletados.sort((a, b) -> a.nombre.compareTo(b.nombre));
        
        for (ProcesoCompletado proceso : procesosCompletados) {
            int tiempoEspera = proceso.tiempoAsignacion - proceso.tiempoPeticion;
            int tiempoFinalizacion = proceso.tiempoFinalizacion;
            sumaFinalizacion += tiempoFinalizacion;
            contador++;
            
            tabla.add(String.format("|    %-4s |   %-5s |  %-3d  |  %-3d  |     %-3d      |    %-3d      |", 
                proceso.nombre, proceso.clienteId, proceso.tiempoPeticion, 
                proceso.tiempoEjecucion, tiempoEspera, tiempoFinalizacion));
        }
        
        if (procesosCompletados.isEmpty()) {
            tabla.add("|          No hay procesos completados aun           |");
        }
        
        tabla.add("+---------+---------+-------+-------+--------------+-------------+");
        if (contador > 0) {
            tabla.add(String.format("| Promedio de Tiempo Finalizacion: %-15.2f |", sumaFinalizacion / contador));
            tabla.add("+---------------------------------------------------+");
        }
        
        return tabla;
    }
    
    public synchronized List<String> obtenerTablaPenalizacion() {
        List<String> tabla = new ArrayList<>();
        tabla.add("+------ TABLA DE PENALIZACION (P = F / t) ------+");
        tabla.add("| Proceso | Cliente |   t   | T.Final (F) | Penaliz (P) |");
        tabla.add("+---------+---------+-------+-------------+-------------+");
        
        double sumaPenalizacion = 0;
        int contador = 0;
        
        procesosCompletados.sort((a, b) -> a.nombre.compareTo(b.nombre));
        
        for (ProcesoCompletado proceso : procesosCompletados) {
            if (proceso.tiempoEjecucion > 0) {
                double penalizacion = (double) proceso.tiempoFinalizacion / proceso.tiempoEjecucion;
                sumaPenalizacion += penalizacion;
                contador++;
                tabla.add(String.format("|    %-4s |   %-5s |  %-3d  |    %-3d      |    %-5.2f    |", 
                    proceso.nombre, proceso.clienteId, proceso.tiempoEjecucion, 
                    proceso.tiempoFinalizacion, penalizacion));
            }
        }
        
        if (procesosCompletados.isEmpty()) {
            tabla.add("|    No hay datos para calcular penalizacion    |");
        }
        
        tabla.add("+---------+---------+-------+-------------+-------------+");
        if (contador > 0) {
            tabla.add(String.format("| Penalizacion Promedio: %-19.2f |", sumaPenalizacion / contador));
            tabla.add("+-------------------------------------------+");
        }
        
        return tabla;
    }
    
    public synchronized List<String> obtenerTablaResumen() {
        List<String> tabla = new ArrayList<>();
        tabla.add("+--------------- TABLA RESUMEN DE PROCESOS --------------+");
        tabla.add("| Proceso | Cliente | T.Peticion | T.Asignacion | T.Finalizacion |");
        tabla.add("+---------+---------+------------+--------------+----------------+");
        
        procesosCompletados.sort((a, b) -> a.nombre.compareTo(b.nombre));
        
        for (ProcesoCompletado proceso : procesosCompletados) {
            tabla.add(String.format("|    %-4s |   %-5s |    %-5d   |     %-5d    |      %-5d     |", 
                proceso.nombre, proceso.clienteId, proceso.tiempoPeticion, 
                proceso.tiempoAsignacion, proceso.tiempoFinalizacion));
        }
        
        if (procesosCompletados.isEmpty()) {
            tabla.add("|        No hay procesos completados aun         |");
        }
        
        tabla.add("+---------+---------+------------+--------------+----------------+");
        return tabla;
    }
    
    public synchronized String generarArchivoCliente(String clienteId) {
        try {
            String nombreArchivo = "resultados_cliente_" + clienteId + ".txt";
            FileWriter writer = new FileWriter(nombreArchivo);
            PrintWriter pw = new PrintWriter(writer);
            
            pw.println("+===============================================================+");
            pw.println("|        RESULTADOS DEL CLIENTE: " + String.format("%-30s", clienteId) + "|");
            pw.println("+===============================================================+");
            pw.println();
            
            pw.println("TABLA GANTT:");
            for (String linea : obtenerTablaGantt()) {
                pw.println(linea);
            }
            pw.println();
            
            pw.println("COLA DE ESPERA:");
            for (String linea : obtenerColaEspera()) {
                pw.println(linea);
            }
            pw.println();
            
            pw.println("COLA DE RECHAZO:");
            for (String linea : obtenerColaRechazo()) {
                pw.println(linea);
            }
            pw.println();
            
            pw.println("TABLA DE PROCESOS:");
            for (String linea : obtenerTablaProcesos()) {
                pw.println(linea);
            }
            pw.println();
            
            pw.println("TABLA DE TIEMPO DE ESPERA:");
            for (String linea : obtenerTablaTiempoEspera()) {
                pw.println(linea);
            }
            pw.println();
            
            pw.println("TABLA DE TIEMPO DE FINALIZACION:");
            for (String linea : obtenerTablaTiempoFinalizacion()) {
                pw.println(linea);
            }
            pw.println();
            
            pw.println("TABLA DE PENALIZACION:");
            for (String linea : obtenerTablaPenalizacion()) {
                pw.println(linea);
            }
            pw.println();
            
            pw.println("TABLA RESUMEN:");
            for (String linea : obtenerTablaResumen()) {
                pw.println(linea);
            }
            
            pw.close();
            return nombreArchivo;
        } catch (IOException e) {
            return "Error al generar archivo: " + e.getMessage();
        }
    }
}

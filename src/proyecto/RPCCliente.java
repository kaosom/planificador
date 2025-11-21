import java.util.*;
import org.apache.xmlrpc.client.XmlRpcClient;
import org.apache.xmlrpc.client.XmlRpcClientConfigImpl;

public class RPCCliente {
    private XmlRpcClient cliente;
    private String clienteId;
    
    public RPCCliente(String clienteId) {
        this.clienteId = clienteId;
        try {
            XmlRpcClientConfigImpl config = new XmlRpcClientConfigImpl();
            config.setServerURL(new java.net.URL("http://localhost:8080"));
            cliente = new XmlRpcClient();
            cliente.setConfig(config);
            System.out.println("+=========================================+");
            System.out.println("| Cliente " + clienteId + " conectado al servidor     |");
            System.out.println("+=========================================+");
        } catch (Exception e) {
            System.err.println("Error al conectar con el servidor: " + e.getMessage());
            System.err.println("Asegurese de que el servidor este ejecutandose");
            e.printStackTrace();
        }
    }
    
    private Object ejecutarMetodo(String metodo, Object[] params) {
        try {
            return cliente.execute("Procesos." + metodo, params);
        } catch (Exception e) {
            System.err.println("Error al ejecutar metodo " + metodo + ": " + e.getMessage());
            return null;
        }
    }
    
    public void solicitarProceso(String nombreProceso, int tiempoCreacion, int tiempoCPU) {
        Object[] params = {clienteId, nombreProceso, tiempoCreacion, tiempoCPU};
        String respuesta = (String) ejecutarMetodo("solicitarProceso", params);
        if (respuesta != null) {
            System.out.println("\n+--------------------------------------+");
            System.out.println("| " + respuesta);
            System.out.println("+--------------------------------------+");
        }
    }
    
    public void reportarCompletado(String nombreProceso, int tiempoFinalizacion) {
        Object[] params = {clienteId, nombreProceso, tiempoFinalizacion};
        String resultado = (String) ejecutarMetodo("reportarCompletado", params);
        System.out.println("\n+--------------------------------------+");
        if (resultado != null) {
            System.out.println("| " + resultado);
        } else {
            System.out.println("| Proceso " + nombreProceso + " completado en T=" + tiempoFinalizacion);
        }
        System.out.println("+--------------------------------------+");
    }
    
    public void obtenerSiguienteProceso() {
        String siguienteProceso = (String) ejecutarMetodo("obtenerSiguienteProceso", new Object[0]);
        if (siguienteProceso != null) {
            System.out.println("\n+--------------------------------------+");
            System.out.println("| " + siguienteProceso);
            System.out.println("+--------------------------------------+");
        }
    }
    
    @SuppressWarnings("unchecked")
    private List<String> convertirALista(Object resultado) {
        List<String> lista = new ArrayList<>();
        if (resultado != null) {
            if (resultado instanceof Object[]) {
                Object[] array = (Object[]) resultado;
                for (Object item : array) {
                    if (item != null) {
                        lista.add(item.toString());
                    }
                }
            } else if (resultado instanceof List) {
                try {
                    lista = (List<String>) resultado;
                } catch (ClassCastException e) {
                    List<?> listaGenerica = (List<?>) resultado;
                    for (Object item : listaGenerica) {
                        if (item != null) {
                            lista.add(item.toString());
                        }
                    }
                }
            }
        }
        return lista;
    }
    
    public void mostrarTablaGantt() {
        List<String> tabla = convertirALista(ejecutarMetodo("obtenerTablaGantt", new Object[0]));
        if (tabla != null && !tabla.isEmpty()) {
            System.out.println();
            for (String linea : tabla) {
                System.out.println(linea);
            }
            System.out.println();
        }
    }
    
    public void mostrarTodasLasTablas() {
        try {
            System.out.println("\n+======== ESTADO DEL SISTEMA - " + clienteId + " ========+");
            
            mostrarTablaGantt();
            mostrarColaEspera();
            mostrarColaRechazo();
            mostrarTablaProcesos();
            mostrarTablaTiempoEspera();
            mostrarTablaTiempoFinalizacion();
            mostrarTablaPenalizacion();
            mostrarTablaResumen();
            
        } catch (Exception e) {
            System.err.println("Error mostrando tablas: " + e.getMessage());
        }
    }
    
    public void mostrarColaEspera() {
        List<String> cola = convertirALista(ejecutarMetodo("obtenerColaEspera", new Object[0]));
        if (cola != null && !cola.isEmpty()) {
            System.out.println();
            for (String linea : cola) {
                System.out.println(linea);
            }
            System.out.println();
        }
    }
    
    public void mostrarColaRechazo() {
        List<String> cola = convertirALista(ejecutarMetodo("obtenerColaRechazo", new Object[0]));
        if (cola != null && !cola.isEmpty()) {
            System.out.println();
            for (String linea : cola) {
                System.out.println(linea);
            }
            System.out.println();
        }
    }
    
    public void mostrarTablaProcesos() {
        List<String> tabla = convertirALista(ejecutarMetodo("obtenerTablaProcesos", new Object[0]));
        if (tabla != null && !tabla.isEmpty()) {
            System.out.println();
            for (String linea : tabla) {
                System.out.println(linea);
            }
            System.out.println();
        }
    }
    
    public void mostrarTablaTiempoEspera() {
        List<String> tabla = convertirALista(ejecutarMetodo("obtenerTablaTiempoEspera", new Object[0]));
        if (tabla != null && !tabla.isEmpty()) {
            System.out.println();
            for (String linea : tabla) {
                System.out.println(linea);
            }
            System.out.println();
        }
    }
    
    public void mostrarTablaTiempoFinalizacion() {
        List<String> tabla = convertirALista(ejecutarMetodo("obtenerTablaTiempoFinalizacion", new Object[0]));
        if (tabla != null && !tabla.isEmpty()) {
            System.out.println();
            for (String linea : tabla) {
                System.out.println(linea);
            }
            System.out.println();
        }
    }
    
    public void mostrarTablaPenalizacion() {
        List<String> tabla = convertirALista(ejecutarMetodo("obtenerTablaPenalizacion", new Object[0]));
        if (tabla != null && !tabla.isEmpty()) {
            System.out.println();
            for (String linea : tabla) {
                System.out.println(linea);
            }
            System.out.println();
        }
    }
    
    public void mostrarTablaResumen() {
        List<String> tabla = convertirALista(ejecutarMetodo("obtenerTablaResumen", new Object[0]));
        if (tabla != null && !tabla.isEmpty()) {
            System.out.println();
            for (String linea : tabla) {
                System.out.println(linea);
            }
            System.out.println();
        }
    }
    
    public void generarArchivoCliente() {
        String archivo = (String) ejecutarMetodo("generarArchivoCliente", new Object[]{clienteId});
        if (archivo != null) {
            System.out.println("\n+--------------------------------------+");
            System.out.println("| Archivo generado: " + archivo);
            System.out.println("+--------------------------------------+");
        }
    }
    
    public static void main(String[] args) {
        try (Scanner scanner = new Scanner(System.in)) {
            System.out.print("Ingrese ID del cliente: ");
            String clienteId = scanner.nextLine();
            
            RPCCliente cliente = new RPCCliente(clienteId);
            
            if (cliente.cliente == null) {
                System.out.println("No se pudo conectar al servidor. Saliendo...");
                return;
            }
            
            while (true) {
            System.out.println("\n+---------- MENU CLIENTE " + clienteId + " ----------+");
            System.out.println("| 1. Solicitar nuevo proceso            |");
            System.out.println("| 2. Reportar proceso completado        |");
            System.out.println("| 3. Obtener siguiente proceso          |");
            System.out.println("| 4. Mostrar tabla Gantt                 |");
            System.out.println("| 5. Mostrar todas las tablas           |");
            System.out.println("| 6. Mostrar cola de espera             |");
            System.out.println("| 7. Mostrar cola de rechazo            |");
            System.out.println("| 8. Mostrar tabla de procesos          |");
            System.out.println("| 9. Mostrar tabla tiempo de espera     |");
            System.out.println("| 10. Mostrar tabla tiempo finalizacion |");
            System.out.println("| 11. Mostrar tabla de penalizacion     |");
            System.out.println("| 12. Mostrar tabla resumen             |");
            System.out.println("| 13. Generar archivo de resultados     |");
            System.out.println("| 0. Salir                               |");
            System.out.println("+---------------------------------------+");
            System.out.print("Seleccione opcion: ");
            
            try {
                int opcion = scanner.nextInt();
                scanner.nextLine();
                
                switch (opcion) {
                    case 1:
                        System.out.print("Nombre del proceso: ");
                        String nombre = scanner.nextLine();
                        System.out.print("Tiempo de creacion (C): ");
                        int c = scanner.nextInt();
                        System.out.print("Tiempo de CPU (t): ");
                        int t = scanner.nextInt();
                        scanner.nextLine();
                        cliente.solicitarProceso(nombre, c, t);
                        break;
                        
                    case 2:
                        System.out.print("Nombre del proceso completado: ");
                        String nombreProc = scanner.nextLine();
                        System.out.print("Tiempo de finalizacion (F): ");
                        int tiempoFinal = scanner.nextInt();
                        scanner.nextLine();
                        cliente.reportarCompletado(nombreProc, tiempoFinal);
                        break;
                        
                    case 3:
                        cliente.obtenerSiguienteProceso();
                        break;
                        
                    case 4:
                        cliente.mostrarTablaGantt();
                        break;
                        
                    case 5:
                        cliente.mostrarTodasLasTablas();
                        break;
                        
                    case 6:
                        cliente.mostrarColaEspera();
                        break;
                        
                    case 7:
                        cliente.mostrarColaRechazo();
                        break;
                        
                    case 8:
                        cliente.mostrarTablaProcesos();
                        break;
                        
                    case 9:
                        cliente.mostrarTablaTiempoEspera();
                        break;
                        
                    case 10:
                        cliente.mostrarTablaTiempoFinalizacion();
                        break;
                        
                    case 11:
                        cliente.mostrarTablaPenalizacion();
                        break;
                        
                    case 12:
                        cliente.mostrarTablaResumen();
                        break;
                        
                    case 13:
                        cliente.generarArchivoCliente();
                        break;
                        
                    case 0:
                        System.out.println("\n+---------------------------------------+");
                        System.out.println("|          Saliendo del cliente        |");
                        System.out.println("+---------------------------------------+");
                        return;
                        
                    default:
                        System.out.println("\nOpcion no valida. Intente nuevamente.");
                }
            } catch (Exception e) {
                System.err.println("Error en la entrada: " + e.getMessage());
                scanner.nextLine();
            }
            }
        }
    }
}

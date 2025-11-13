import org.apache.xmlrpc.webserver.WebServer;
import org.apache.xmlrpc.server.PropertyHandlerMapping;
import org.apache.xmlrpc.server.XmlRpcServer;
import java.util.Scanner;

public class RCPServidor {
    private static ProcesosImpl planificador;
    private static WebServer server;
    
    public static void main(String[] args) {
        try {
            System.out.println("+=========================================+");
            System.out.println("|      INICIANDO SERVIDOR XML-RPC        |");
            System.out.println("+=========================================+");
            
            planificador = new ProcesosImpl();
            
            server = new WebServer(8080);
            XmlRpcServer xmlRpcServer = server.getXmlRpcServer();
            PropertyHandlerMapping phm = new PropertyHandlerMapping();
            phm.addHandler("Procesos", ProcesosImpl.class);
            xmlRpcServer.setHandlerMapping(phm);
            server.start();
            
            System.out.println("| Servidor XML-RPC iniciado en puerto 8080|");
            System.out.println("| Servidor registrado como: Procesos      |");
            System.out.println("| Esperando solicitudes de clientes...     |");
            System.out.println("+=========================================+");
            
            iniciarMenuServidor();
            
        } catch (Exception e) {
            System.err.println("Error en el servidor: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private static void iniciarMenuServidor() {
        Scanner scanner = new Scanner(System.in);
        
        while (true) {
            System.out.println("\n+---------- MENU DEL SERVIDOR ----------+");
            System.out.println("| 1. Mostrar Cola de Espera            |");
            System.out.println("| 2. Mostrar Tabla de Procesos         |");
            System.out.println("| 3. Mostrar Tabla Gantt                |");
            System.out.println("| 4. Mostrar Tabla Tiempo de Espera    |");
            System.out.println("| 5. Mostrar Tabla Tiempo Finalizacion |");
            System.out.println("| 6. Mostrar Tabla de Penalizacion     |");
            System.out.println("| 7. Mostrar Tabla Resumen             |");
            System.out.println("| 8. Mostrar Cola de Rechazo           |");
            System.out.println("| 9. Mostrar Todas las Tablas          |");
            System.out.println("| 10. Obtener Siguiente Proceso        |");
            System.out.println("| 11. Generar Archivo Cliente           |");
            System.out.println("| 0. Salir del Servidor                 |");
            System.out.println("+--------------------------------------+");
            System.out.print("Seleccione opcion: ");
            
            try {
                int opcion = scanner.nextInt();
                scanner.nextLine();
                
                switch (opcion) {
                    case 1:
                        mostrarTablaEnServidor(planificador.obtenerColaEspera());
                        break;
                        
                    case 2:
                        mostrarTablaEnServidor(planificador.obtenerTablaProcesos());
                        break;
                        
                    case 3:
                        mostrarTablaEnServidor(planificador.obtenerTablaGantt());
                        break;
                        
                    case 4:
                        mostrarTablaEnServidor(planificador.obtenerTablaTiempoEspera());
                        break;
                        
                    case 5:
                        mostrarTablaEnServidor(planificador.obtenerTablaTiempoFinalizacion());
                        break;
                        
                    case 6:
                        mostrarTablaEnServidor(planificador.obtenerTablaPenalizacion());
                        break;
                        
                    case 7:
                        mostrarTablaEnServidor(planificador.obtenerTablaResumen());
                        break;
                        
                    case 8:
                        mostrarTablaEnServidor(planificador.obtenerColaRechazo());
                        break;
                        
                    case 9:
                        mostrarTodasLasTablas();
                        break;
                        
                    case 10:
                        String siguienteProceso = planificador.obtenerSiguienteProceso();
                        System.out.println("\n+--------------------------------------+");
                        System.out.println("| " + siguienteProceso);
                        System.out.println("+--------------------------------------+");
                        break;
                        
                    case 11:
                        System.out.print("Ingrese ID del cliente: ");
                        String clienteId = scanner.nextLine();
                        String archivo = planificador.generarArchivoCliente(clienteId);
                        System.out.println("\n+--------------------------------------+");
                        System.out.println("| Archivo generado: " + archivo);
                        System.out.println("+--------------------------------------+");
                        break;
                        
                    case 0:
                        System.out.println("\n+--------------------------------------+");
                        System.out.println("|        Apagando servidor...         |");
                        System.out.println("+--------------------------------------+");
                        if (server != null) {
                            server.shutdown();
                        }
                        scanner.close();
                        System.exit(0);
                        break;
                        
                    default:
                        System.out.println("\nOpcion no valida. Intente nuevamente.");
                }
            } catch (Exception e) {
                System.err.println("Error en el menu del servidor: " + e.getMessage());
                scanner.nextLine();
            }
        }
    }
    
    private static void mostrarTablaEnServidor(java.util.List<String> tabla) {
        System.out.println();
        for (String linea : tabla) {
            System.out.println(linea);
        }
        System.out.println();
    }
    
    private static void mostrarTodasLasTablas() {
        try {
            System.out.println("\n+========== ESTADO COMPLETO DEL SISTEMA ==========+");
            
            mostrarTablaEnServidor(planificador.obtenerTablaGantt());
            mostrarTablaEnServidor(planificador.obtenerColaEspera());
            mostrarTablaEnServidor(planificador.obtenerColaRechazo());
            mostrarTablaEnServidor(planificador.obtenerTablaProcesos());
            mostrarTablaEnServidor(planificador.obtenerTablaTiempoEspera());
            mostrarTablaEnServidor(planificador.obtenerTablaTiempoFinalizacion());
            mostrarTablaEnServidor(planificador.obtenerTablaPenalizacion());
            mostrarTablaEnServidor(planificador.obtenerTablaResumen());
            
        } catch (Exception e) {
            System.err.println("Error mostrando tablas: " + e.getMessage());
        }
    }
}

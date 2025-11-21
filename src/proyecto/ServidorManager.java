import org.apache.xmlrpc.webserver.WebServer;
import org.apache.xmlrpc.server.PropertyHandlerMapping;
import org.apache.xmlrpc.server.XmlRpcServer;

public class ServidorManager {
    private ProcesosImpl planificador;
    private WebServer server;
    private Thread servidorThread;
    private boolean servidorActivo;
    
    public ServidorManager(ProcesosImpl planificador) {
        this.planificador = planificador;
        this.servidorActivo = false;
    }
    
    public synchronized void iniciarServidor() throws Exception {
        if (servidorActivo) {
            throw new IllegalStateException("El servidor ya está activo");
        }
        
        servidorThread = new Thread(() -> {
            try {
                ProcesosImpl.setInstanciaCompartida(planificador);
                
                server = new WebServer(8080);
                XmlRpcServer xmlRpcServer = server.getXmlRpcServer();
                PropertyHandlerMapping phm = new PropertyHandlerMapping();
                phm.addHandler("Procesos", ProcesosImpl.class);
                xmlRpcServer.setHandlerMapping(phm);
                server.start();
                
                servidorActivo = true;
                System.out.println("Servidor XML-RPC iniciado en puerto 8080");
            } catch (Exception e) {
                System.err.println("Error al iniciar servidor: " + e.getMessage());
                servidorActivo = false;
                throw new RuntimeException(e);
            }
        });
        
        servidorThread.setDaemon(true);
        servidorThread.start();
        
        Thread.sleep(500);
        
        if (!servidorActivo) {
            throw new Exception("No se pudo iniciar el servidor");
        }
    }
    
    public synchronized void detenerServidor() throws Exception {
        if (!servidorActivo || server == null) {
            throw new IllegalStateException("El servidor no está activo");
        }
        
        try {
            server.shutdown();
            servidorActivo = false;
            System.out.println("Servidor XML-RPC detenido");
        } catch (Exception e) {
            servidorActivo = false;
            throw new Exception("Error al detener servidor: " + e.getMessage());
        }
    }
    
    public boolean isServidorActivo() {
        return servidorActivo;
    }
    
    public ProcesosImpl getPlanificador() {
        return planificador;
    }
}


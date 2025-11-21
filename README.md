# Planificador de Procesos - Sistema Unificado

Sistema de planificación de procesos con interfaz gráfica que integra servidor y cliente en una sola aplicación.

## Requisitos

- Java 8 o superior
- Las librerías XML-RPC ya están incluidas en la carpeta `lib/`

## Compilación

Para compilar todos los archivos del proyecto:

```bash
javac -cp "lib/*" -d build/classes src/proyecto/*.java
```

Esto compilará todos los archivos Java y los guardará en `build/classes/`.

## Ejecución

### Interfaz Gráfica Unificada (Recomendado)

La forma más fácil de usar el sistema es con la interfaz gráfica que incluye servidor y cliente:

```bash
./run-gui.sh
```

O directamente:

```bash
java -cp "lib/*:build/classes" PlanificadorGUI
```

### Modo Consola (Alternativo)

Si prefieres usar el modo consola con servidor y cliente separados:

**Terminal 1 - Servidor:**
```bash
./run-servidor.sh
```

**Terminal 2 - Cliente:**
```bash
./run-cliente.sh
```

## Uso de la Interfaz Gráfica

1. **Arrancar el Servidor:**
   - Haz clic en el botón "Arrancar Servidor"
   - El estado cambiará a "Servidor activo" (verde)
   - La línea roja del tiempo empezará a moverse

2. **Conectar un Cliente:**
   - Ingresa un ID de cliente en el campo "ID:"
   - Haz clic en "Conectar"
   - Verás un mensaje de confirmación

3. **Agregar Procesos:**
   - Ingresa el nombre del proceso
   - Ingresa el tiempo de creación (C)
   - Ingresa el tiempo de CPU (t)
   - Haz clic en "Agregar Proceso"
   - El proceso aparecerá en la tabla de Gantt

4. **Ver Información:**
   - La tabla de Gantt en tiempo real se actualiza automáticamente
   - Usa las pestañas a la derecha para ver:
     - En Espera: Procesos en cola de espera
     - Rechazados: Procesos rechazados
     - Procesos: Tabla completa de procesos
     - Tiempo Espera: Tiempos de espera calculados
     - Tiempo Final: Tiempos de finalización
     - Penalización: Penalizaciones calculadas
     - Resumen: Resumen general

5. **Parar el Servidor:**
   - Haz clic en "Parar Servidor" cuando termines
   - El tiempo se reiniciará a 0

## Estructura del Proyecto

```
planificador/
├── src/proyecto/          # Código fuente
│   ├── PlanificadorGUI.java      # Interfaz gráfica principal
│   ├── ServidorManager.java      # Gestión del servidor
│   ├── GanttChartPanel.java       # Tabla de Gantt dinámica
│   ├── GanttChartPanelStatic.java # Tabla de Gantt estática
│   ├── TablaPanel.java            # Panel para tablas
│   ├── ProcesosImpl.java          # Lógica del planificador
│   ├── RCPServidor.java           # Servidor RPC (consola)
│   └── RPCCliente.java            # Cliente RPC (consola)
├── lib/                   # Librerías XML-RPC
├── build/classes/         # Archivos compilados
├── run-gui.sh            # Script para ejecutar GUI
├── run-servidor.sh       # Script para ejecutar servidor
└── run-cliente.sh        # Script para ejecutar cliente
```

## Características

- **Interfaz Gráfica Unificada:** Servidor y cliente en una sola ventana
- **Tabla de Gantt en Tiempo Real:** Visualización gráfica que se actualiza cada segundo
- **Tabla de Gantt Final:** Muestra los resultados finales de todos los procesos
- **Actualización Automática:** Todas las tablas se actualizan automáticamente
- **Múltiples Clientes:** Puedes conectar diferentes clientes con sus IDs
- **Visualización Clara:** Colores para identificar estados (verde=ejecutando, amarillo=solicitud, naranja=esperando)

## Solución de Problemas

### Error: "Address already in use"
Si el puerto 8080 está ocupado:
```bash
lsof -i :8080
kill <PID>
```

### Error: "ClassNotFoundException"
Asegúrate de haber compilado primero:
```bash
javac -cp "lib/*" -d build/classes src/proyecto/*.java
```

### La línea del tiempo no se mueve
Verifica que el servidor esté activo. La línea roja solo se mueve cuando el servidor está corriendo.

## Notas

- El tiempo máximo es 30 unidades
- La cola de espera tiene un tamaño máximo de 10 procesos
- Los procesos rechazados pueden reintentar hasta 3 veces
- Si un cliente no envía procesos por más de 5 unidades de tiempo, se elimina automáticamente


# Cómo ejecutar el proyecto Planificador RPC

## Requisitos previos
- Java 11 o superior
- Las librerías XML-RPC descargadas en `lib/`

## Pasos para ejecutar

### Opción 1: Usando los scripts (Recomendado)

**Terminal 1 - Servidor:**
```bash
./run-servidor.sh
```

**Terminal 2 - Cliente:**
```bash
./run-cliente.sh
```

### Opción 2: Comandos directos

**Terminal 1 - Servidor:**
```bash
java -cp "lib/*:build/classes" RCPServidor
```

**Terminal 2 - Cliente:**
```bash
java -cp "lib/*:build/classes" RPCCliente
```

## Notas importantes

1. **El servidor debe ejecutarse primero** antes de iniciar el cliente
2. El servidor corre en el puerto **8080**
3. El cliente se conectará automáticamente a `localhost:8080`
4. Para detener el servidor, usa la opción **0** en el menú del servidor

## Recompilar el proyecto

Si haces cambios en el código, recompila con:

```bash
javac -cp "lib/*:." -d build/classes src/proyecto/*.java
```


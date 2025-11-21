#!/bin/bash
cd "$(dirname "$0")"
echo "Iniciando servidor RPC..."
java -cp "lib/*:build/classes" RCPServidor


#!/bin/bash
cd "$(dirname "$0")"
echo "Iniciando cliente RPC..."
java -cp "lib/*:build/classes" RPCCliente


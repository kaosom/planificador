#!/bin/bash
cd "$(dirname "$0")"
echo "Iniciando Planificador GUI..."
java -cp "lib/*:build/classes" PlanificadorGUI


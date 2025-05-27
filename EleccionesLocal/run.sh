#!/bin/bash

DB_USER="elecciones_user"
DB_PASS="voto2025"
DB_NAME="eleccionesdb"
JARS="ice-3.7.6.jar:postgresql-42.3.1.jar:eleccioneslocal.jar"

echo "▶️ Ejecutando servidor Elecciones Local..."

java -cp "lib/*" EleccionesLocal.Server

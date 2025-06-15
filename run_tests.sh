#!/bin/bash

# Validación de parámetros
if [ $# -ne 2 ]; then
  echo "Uso: $0 <documento_origen.csv> <cantidad_devices>"
  exit 1
fi

ORIGEN="$1"
CANTIDAD="$2"

# Verifica que el archivo de origen existe
if [ ! -f "$ORIGEN" ]; then
  echo "Error: El archivo '$ORIGEN' no existe."
  exit 1
fi

# Obtener número de líneas del CSV (excluyendo cabecera si se desea)
LINEAS=$(($(wc -l < "$ORIGEN")))

# Construir nombre del archivo de resultados
RESULTADO="results-${LINEAS}-${CANTIDAD}.csv"

# Ejecutar el JAR con los argumentos requeridos
java -jar TestsConsultas.jar "$ORIGEN" "$RESULTADO" "$CANTIDAD"

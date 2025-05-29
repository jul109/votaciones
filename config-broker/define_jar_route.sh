#!/bin/bash

JAR_PATH="$1"
TEMPLATE="application.xml"
OUTPUT="icegird_gen.xml"

if [ -f "$OUTPUT" ]; then
    read -p "$OUTPUT ya existe. ¿Deseás sobrescribirlo? (s/N): " confirm
    if [[ "$confirm" != "s" && "$confirm" != "S" ]]; then
        echo "Operación cancelada."
        exit 1
    fi
fi

sed "s|{{JAR_PATH}}|$JAR_PATH|g" "$TEMPLATE" > "$OUTPUT"
echo "Archivo generado: $OUTPUT"
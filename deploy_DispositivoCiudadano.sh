#!/bin/bash

if [ "$#" -ne 2 ]; then
    echo "Uso: $0 <direccion_scp> <nombre_nodo>"
    exit 1
fi

DESTINO=$1           
NOMBRE_NODO=$2        
DIR_COMPACT="compact"

echo "Eliminando carpeta compact..."
rm -rf "$DIR_COMPACT"

echo "Creando carpeta compact..."
mkdir -p "$DIR_COMPACT/db/node"

echo "Copiando DispositivoCiudadano.jar..."
cp DispositivoCiudadano/build/libs/DispositivoCiudadano.jar "$DIR_COMPACT/"

echo "Copiando todos los .jar desde la carpeta jars al root de compact..."
cp jars/*.jar "$DIR_COMPACT/"


echo "Creando script de ejecución..."
SCRIPT_OBJETIVO="run_app.sh"

cat << 'EOF' > "$SCRIPT_OBJETIVO"
#!/bin/bash

# Salir si ocurre un error
set -e

# Ejecutar la clase MesaVotacion
echo "Ejecutando DispositivoCiudadano..."
java -cp DispositivoCiudadano.jar:ice-3.7.6.jar:icegrid-3.7.10.jar:postgresql-42.3.1.jar consultaVotacion.CitizenUI
EOF

# Dar permisos de ejecución al nuevo script
chmod +x "$SCRIPT_OBJETIVO"

echo "Enviando por scp al destino..."
scp -r "$DIR_COMPACT" "$DESTINO:~/LosPelados/deploy"

echo "Eliminando carpeta compact..."
rm -rf "$DIR_COMPACT"

echo "Transferencia completada a $DESTINO con nodo $NOMBRE_NODO"

#!/bin/bash

DESTINO=$1
DEPLOY_NUMBER=$2              
DIR_COMPACT="compact"

echo "Eliminando carpeta compact..."
rm -rf "$DIR_COMPACT"

echo "Creando carpeta compact..."
mkdir "$DIR_COMPACT"

cd DispositivoCiudadano
echo "Compilando DispositivoCiudadano..."
gradle clean build installDist
cd ..

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
scp -r "$DIR_COMPACT" "$DESTINO:~/LosPelados/$DEPLOY_NUMBER"

echo "Eliminando carpeta compact..."
rm -rf "$DIR_COMPACT"

echo "Transferencia completada a $DESTINO

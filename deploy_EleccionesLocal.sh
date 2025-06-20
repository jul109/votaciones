#!/bin/bash


DESTINO=$1           
NOMBRE_NODO=$2       
DEPLOY_NUMBER=$3
DIR_COMPACT="compact"

echo "Eliminando carpeta compact..."
rm -rf "$DIR_COMPACT"

echo "Creando carpeta compact..."
mkdir -p "$DIR_COMPACT/db/node"

cd EleccionesLocal
echo "Compilando EleccionesLocal..."
gradle clean build installDist
cd ..

echo "Copiando EleccionesLocal.jar..."
cp EleccionesLocal/build/libs/EleccionesLocal.jar "$DIR_COMPACT/"

echo "Copiando archivo de configuración..."
cp config-broker/node_voto.config "$DIR_COMPACT/"

echo "Copiando todos los .jar desde la carpeta jars al root de compact..."
cp jars/*.jar "$DIR_COMPACT/"

echo "Modificando el archivo node_voto.config cambiando el nombre del nodo..."
sed -i "s/^IceGrid.Node.Name=.*/IceGrid.Node.Name=$NOMBRE_NODO/" "$DIR_COMPACT/node_voto.config"

echo "Creando script de ejecución..."
SCRIPT_OBJETIVO="run_app.sh"

cat << 'EOF' > "$SCRIPT_OBJETIVO"
#!/bin/bash

# Salir si ocurre un error
set -e

# Ejecutar la clase MesaVotacion
echo "Ejecutando EleccionesLocal..."
java -cp EleccionesLocal.jar:ice-3.7.6.jar:icegrid-3.7.10.jar:postgresql-42.3.1.jar EleccionesLocal.Server
EOF

chmod +x "$SCRIPT_OBJETIVO"

echo "Enviando por scp al destino..."
scp -r "$DIR_COMPACT" "$DESTINO:~/LosPelados/$DEPLOY_NUMBER"

echo "Eliminando carpeta compact..."
rm -rf "$DIR_COMPACT"

echo "Transferencia completada a $DESTINO con nodo $NOMBRE_NODO"

#!/bin/bash

# Salir si ocurre un error
set -e

# Ir a la carpeta MesaVotacion y compilar
cd MesaVotacion
echo "Compilando MesaVotacion..."
gradle clean build installDist

# Volver al directorio base y entrar a EleccionesLocal
cd ../EleccionesLocal
echo "Compilando EleccionesLocal..."
gradle clean build installDist

cd ../MesaVotacionLocal
echo "Compilando MesaVotacionLocal..."
gradle clean build installDist

cd ../DispositivoCiudadano
echo "Compilando DispositivoCiudadano..."
gradle clean build installDist

# Volver al directorio base
cd ..

# Copiar EleccionesLocal.jar al servidor remoto
echo "Enviando EleccionesLocal.jar..."
scp EleccionesLocal/build/libs/EleccionesLocal.jar swarch@x206m03:~/LosPelados/deploy

# Copiar app.jar al servidor remoto
echo "Enviando app.jar..."
scp MesaVotacion/app/build/libs/app.jar swarch@x206m03:~/LosPelados/deploy

echo "Enviando MesaVotacionLocal.jar..."
scp MesaVotacionLocal/build/libs/MesaVotacionLocal.jar swarch@x206m02:~/LosPelados/deploy

echo "Enviando DispositvoCiudadano.jar..."
scp DispositivoCiudadano/build/libs/DispositivoCiudadano.jar swarch@x206m04:~/LosPelados/deploy

# Copiar config-broker al servidor remoto
echo "Enviando config-broker/..."
scp -r config-broker/ swarch@x206m03:~/LosPelados/deploy

echo "✅ Todo listo."

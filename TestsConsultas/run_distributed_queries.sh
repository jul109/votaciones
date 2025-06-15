#!/bin/bash

# Build the project
./gradlew build

# Run the distributed query manager
java -jar build/libs/TestsConsultas.jar ../documentos.csv ../resultados_consultas.csv 
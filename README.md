# Votaciones 

<div id="user-content-toc">
  <ul align="center">
    <summary><h1 style="display: inline-block"><a href="https://github.com/Electromayonaise">Martín Gómez</a>, <a href="https://github.com/jul109">Julio Prado</a>, <a href="https://github.com/MateoRAR">Mateo Rubio</a></h1></summary>
  </ul>
</div>

<div align="center">
  <img src="https://github.com/Electromayonaise/Electromayonaise/blob/main/Assets/github-contribution-grid-snake%20blacktest(1).svg" alt="snake" />
</div>

## Entregables tarea avance:
### 1. Codigo fuente: encuentra los 4 componentes implementados en este repositorio
### 2. Diagrama de deployment: se encuentra en [la carpeta con el mismo nombre dentro de docs/](https://github.com/jul109/votaciones/tree/main/docs/DeploymentDiagram)
### 3. Diseño del experimento
#### [Presentación](https://github.com/jul109/votaciones/blob/main/docs/Presentacion%20Diseño%20Experimento.pdf)
#### Video de la presentación
[![Diseño del experimento](https://img.youtube.com/vi/_amL15bWQNs/0.jpg)](https://youtu.be/_amL15bWQNs)
### 4. Video explicativo
[![Funcionamiento](https://img.youtube.com/vi/JXbvOTVATgM/0.jpg)](https://youtu.be/JXbvOTVATgM)

## Instrucciones

Para a configuracion de los archivos usamos el usuario swarch en la carpeta ~/LosPelados/deploy teniendo esta ruta base en cuenta, y que ya todo se ha compilado y los jars enviado,

● (206m04) DispositivoCiudadano, permitir al usuario consultar su lugar de votación

●​ (206m03)MesaVotacion, validar identidad, mostrar candidatos y registrar el voto.

●​ (104m14) MesaVotacionLocal, proveer datos de los ciudadanos registrados por mesa, este es el que usa la interfaz consultaVotacion, query que en este caso el encargado de manejar la base de datos si no lo encuentra retorna null, pero el controlador (QueryStationServer) si recibe null devuelve el string "No está registrado""

●​ (104m12) EleccionesLocal, registrar votos y gestionar candidatos.

●​ (104m10) CentralizadorNacional, recibir votos consolidados y generar resultados nacionales.

Broker - Icegrid (104m11)

Tambien es importante aclarar que cuando se correr algun device se demora un rato, al enviar los primero votos tambien y al consultar las primeras cedula, pedimos paciencia ya que creemos que es debido al icegrid, los primero acks tambien toman su tiempo, funciona todo porfavor tener paciencia al comienzo.

En la ruta base
1. En el 104m10 ~/LosPelados/deploy:

```bash
 ./run_app.sh
```
2. En el 104m11 - ~/LosPelados/deploy: 

```bash
 icegridregistry --Ice.Config=grid.config
```
3. En el 104m12 - ~/LosPelados/deploy: 

```bash
 icegridnode --Ice.Config=node_voto.config
```
4. En el 104m14 - ~/LosPelados/deploy: 
```bash
icegridnode --Ice.Config=node_consulta.config
```
5. En el 104m11 - ~/LosPelados/deploy: 
```bash
icegridadmin --Ice.Config=grid.config
```
6. (OPCIONAL) Después de que icegridadmin abre una terminal: 
  ```bash
   application add "/home/swarch/LosPelados/deploy/application.xml"
   usuario: pelado
   clave: 123456
```
(el paso anterior es opcional ya que ya esta montada)
7. En el 206m04 - ~/LosPelados/deploy:   
```bash
./run_app.sh
```
Esto corre el DispositivoCiudadano que es el encargado de las consultas, pero en realidad con el jar y las dependencias se deberia de poder correr en cualquier maquina.

8. El dispositivo 206m03 (192.168.131.23 o 10.147.19.23) tiene los archivos para el despliegue de cada una de las 10 mesas (TENER EN CUENTA: el id de los cadidatos debe ir de 1 a 3).

8.1 El despliegue de la mesa con id 1 se realiza ejecutando. El servicio VoteStation_Mesa queda expuesto en el puerto 10017.
```bash
/home/swarch/LosPelados/mesa1/deploy/run_app.sh
```
8.2 El despliegue de la mesa con id 2 se realiza ejecutando. El servicio VoteStation_Mesa queda expuesto en el puerto 10019.
```bash
/home/swarch/LosPelados/mesa2/deploy/run_app.sh
```
8.3 El despliegue de la mesa con id 3 se realiza ejecutando. El servicio VoteStation_Mesa queda expuesto en el puerto 10021.
```bash
/home/swarch/LosPelados/mesa3/deploy/run_app.sh
```
8.4 El despliegue de la mesa con id 4 se realiza ejecutando. El servicio VoteStation_Mesa queda expuesto en el puerto 10023.
```bash
/home/swarch/LosPelados/mesa4/deploy/run_app.sh
```
8.5 El despliegue de la mesa con id 5 se realiza ejecutando. El servicio VoteStation_Mesa queda expuesto en el puerto 10025.
```bash
/home/swarch/LosPelados/mesa5/deploy/run_app.sh
```

8.6 El despliegue de la mesa con id 6 se realiza ejecutando. El servicio VoteStation_Mesa queda expuesto en el puerto 10027.
```bash
/home/swarch/LosPelados/mesa6/deploy/run_app.sh
```
8.7 El despliegue de la mesa con id 7 se realiza ejecutando. El servicio VoteStation_Mesa queda expuesto en el puerto 10029.
```bash
/home/swarch/LosPelados/mesa7/deploy/run_app.sh
```
8.8 El despliegue de la mesa con id 8 se realiza ejecutando. El servicio VoteStation_Mesa queda expuesto en el puerto 10031.
```bash
/home/swarch/LosPelados/mesa8/deploy/run_app.sh
```
8.9 El despliegue de la mesa con id 9 se realiza ejecutando. El servicio VoteStation_Mesa queda expuesto en el puerto 10033.
```bash
/home/swarch/LosPelados/mesa9/deploy/run_app.sh
```

8.10 El despliegue de la mesa con id 10 se realiza ejecutando. El servicio VoteStation_Mesa queda expuesto en el puerto 10035.
```bash
/home/swarch/LosPelados/mesa10/deploy/run_app.sh
```
Nota: Los tests deben ser ejecutados una vez aparezca el menu de seleccion en cada mesa. Nuevamente aclaramos que los primeros votos pueden llegar a tardar un par de minutos. Si los votos se realizan por terminal la confirmacion de que el voto llega al centralizador nacional es en tiempo real, sin embargo, si los tests se realizan usando la interfaz VoteStation_Mesa recibira la confirmacion instantaneamente sin haber esperado por la confirmacion del centralizador. En este caso, es necesario esperar a la llegada de todos los acks en la terminal en la que se este ejecutando el centralizador. 

9. Para obtener el acumulado de las elecciones en la terminal en la que se este ejcutando el centralizador (104m10) debe oprimir 1. Esto guardará los resultados en swarch@104m10:~/LosPelados/deploy/resultados$ aquí quedará el csv resume.csv con el agregado de votos y en swarch@104m10:~/LosPelados/deploy/resultados/mesas$  están los resultados obtenidos de cada mesa.
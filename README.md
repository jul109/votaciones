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

```bash
1. En en un pc en la carpeta del repositorio y acceso a la vpn de la universidad: ./deploy.sh
2. En el 206m03: ./deploy_rest.sh
3. En el 104m11: icegridregistry --Ice.Config=grid.config
4. En el 104m12: icegridnode --Ice.Config=node.config
5. En el 104m11: icegridadmin --Ice.Config=grid.config
6. Después de que icegridadmin abre una terminal: 
   application add "/home/swarch/LosPelados/deploy/icegrid_gen.xml"
7. En el 104m10: ./run.sh
8. En el 206m03: ./run_app.sh 
```


# Cronicas Gatunas

Proyecto del curso **Languages and Compilers** — Universidad EIA.
Instructor: Sebastián Zapata Ramírez.

## Integrantes del grupo

- Federico Betancur - fedriki060
- Miguel Angel Muñoz - mgts7

## Cómo compilar y correr el proyecto

Requiere **JDK 17 o superior** y **Maven**.

```bash
mvn clean javafx:run
```

### Requisito de configuración: plataforma de JavaFX

El `pom.xml` fija la dependencia de JavaFX a un `classifier` de sistema operativo
específico (`win`, `mac` o `linux`), en la propiedad `<javafx.platform>`. **Si se
compila en un sistema operativo distinto al que se dejó configurado, hay que
cambiar ese valor antes de compilar** (por ejemplo, de `win` a `linux`), o el
build va a descargar los binarios nativos equivocados de JavaFX y la aplicación
fallará al abrir la ventana.

## Lógica

- **DFS iterativo, no recursivo ni en thread aparte**: la Mision 1 permite grids
  de hasta 10^6 celdas, y una version recursiva desbordaria el stack de Java. Se
  implemento con una pila explicita (dos arreglos paralelos: nodo actual y
  siguiente direccion a probar), simulando a mano los "frames" de la recursion.
- **Tres layouts distintos para las 3 misiones con grafos**, para que cada una se
  vea visualmente diferenciada ademas de tematicamente distinta:
    - Mission 2: `CircularLayout` (nodos distribuidos en un circulo).
    - Mission 3: `ScatterLayout` (secuencia de Halton con rotacion aleatoria por
      generacion, mas una heuristica de "mejor de 25 semillas" para minimizar
      cruces de aristas — ver `GraphCanvas.computeBestLayout`).
    - Mission 4: `GridLayout` (cuadricula con variacion aleatoria determinista,
      tipo manzana urbana).
- **Estructuras planas (arreglos) en vez de objetos por celda/nodo** en las
  estructuras auxiliares de los algoritmos (`visited[]`, `distance[]`, pilas del
  DFS): con grids de hasta 10^6 celdas, el overhead de un objeto por celda es
  significativo; un indice plano (`row * cols + col`) evita ese costo.
- **Union-Find con compresion de caminos y union por tamano** (Mission 4),
  exigido explicitamente por el enunciado.
- **Los fondos y titulos de cada pantalla SI son generados con IA** (imagenes,
  no codigo): ver `AI_USAGE.md` para los prompts usados.


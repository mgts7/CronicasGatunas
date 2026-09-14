package gui.mission3;

import gui.common.PixelArtUtils;
import gui.common.ScatterLayout;
import javafx.geometry.VPos;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.TextAlignment;
import model.graph.Edge;
import model.graph.Graph;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

/**
 * ============================================================
 * GraphCanvas - Mision 3
 * ============================================================
 *
 * Dibuja el grafo dirigido como un mapa del tesoro: cada nodo es un
 * pequeno "punto de interes" sobre pergamino (una X marcada, tipo
 * mapa de aventura), y cada pasaje es una linea punteada tipo
 * "ruta trazada a mano", con una flecha indicando la direccion
 * (las conexiones son dirigidas, a diferencia de Mission 2).
 *
 * Dos modos de resaltado, mutuamente excluyentes (ver Mission3Result):
 *   - ROUTE: la ruta que logra el churun maximo se resalta con una
 *     linea dorada continua (como el "camino trazado" del tesoro).
 *   - CYCLE: el ciclo de ganancia positiva se resalta en un color de
 *     advertencia (naranja/rojo), para comunicar "peligro, churun
 *     infinito", distinto del dorado de una ruta normal.
 *
 * Respeta el limite de dibujo de la seccion 2.3: mas de 60 nodos no
 * se dibujan.
 */
public final class GraphCanvas extends Canvas {

    private static final int MAX_DRAWABLE_NODES = 60;
    private static final double CANVAS_SIZE = 480;
    private static final double SCATTER_MARGIN = 50; // deja espacio para que los nodos y sus etiquetas no queden pegados al borde

    // Paleta "pergamino"
    private static final Color EDGE_DIM = Color.rgb(120, 95, 60, 0.55);
    private static final Color ROUTE_COLOR = Color.rgb(200, 160, 40);
    private static final Color ROUTE_GLOW = Color.rgb(230, 195, 90, 0.35);
    private static final Color CYCLE_COLOR = Color.rgb(200, 60, 40);
    private static final Color CYCLE_GLOW = Color.rgb(230, 110, 70, 0.4);

    private static final Color NODE_BODY = Color.rgb(235, 215, 170);
    private static final Color NODE_BORDER = Color.rgb(120, 90, 55);
    private static final Color SOURCE_COLOR = Color.rgb(90, 150, 70);
    private static final Color DESTINATION_COLOR = Color.rgb(180, 60, 40);
    private static final Color LABEL_COLOR = Color.rgb(70, 50, 30);

    // Recuadro que "interrumpe" la linea punteada para mostrar el peso de la arista
    private static final Color WEIGHT_LABEL_BACKGROUND = Color.rgb(235, 220, 180);
    private static final Color WEIGHT_LABEL_BORDER = Color.rgb(150, 115, 70);

    // Cuanto se arquea una arista cuando existe la arista opuesta entre
    // el mismo par de nodos (u->v Y v->u), para que no queden encimadas.
    private static final double CURVE_OFFSET = 18;

    // Genera una semilla nueva en cada render(), para que el mapa se
    // vea distinto entre una generacion y otra (ver ScatterLayout).
    private final Random layoutSeedGenerator = new Random();

    // Cuantas disposiciones candidatas se prueban para elegir la que
    // menos cruces de aristas tiene (ver computeBestLayout). Minimizar
    // cruces en general es NP-dificil; probar varias semillas al azar
    // y quedarse con la mejor es una heuristica simple y efectiva para
    // los tamanos de este proyecto (hasta 60 nodos visibles).
    private static final int LAYOUT_CANDIDATE_COUNT = 25;

    // Si el grafo tiene muchas mas aristas que esto, se salta la
    // optimizacion (probar 25 candidatos con miles de aristas cada
    // uno seria demasiado costoso para un simple redibujado de UI).
    private static final int MAX_EDGES_FOR_CROSSING_OPTIMIZATION = 300;

    // Cachea el layout ya calculado para un grafo, usando una clave
    // basada en su CONTENIDO (no en la instancia de Graph, que
    // cambia cada vez que se vuelve a parsear el mismo texto). Sin
    // esto, presionar "Resolver" repetidas veces sobre el MISMO
    // input generaba un mapa distinto cada vez, porque cada click
    // volvia a llamar a Mission3Parser.parse(...) y creaba un objeto
    // Graph nuevo (aunque con el mismo contenido). Con esta cache,
    // el mapa solo cambia cuando el grafo realmente cambia (por
    // ejemplo, al presionar "Generar").
    private final Map<String, double[][]> layoutCache = new HashMap<>();

    public GraphCanvas() {
        super(CANVAS_SIZE, CANVAS_SIZE);
    }

    /**
     * Dibuja el grafo completo.
     *
     * @param highlightNodes secuencia de nodos a resaltar (la ruta o
     *                       el ciclo, segun corresponda), o null/vacio
     *                       para no resaltar nada.
     * @param highlightIsCycle true si highlightNodes representa un
     *                          ciclo (se dibuja en color de advertencia
     *                          y se cierra la figura); false si es una
     *                          ruta normal (se dibuja en dorado, abierta).
     */
    public boolean render(Graph graph, int n, int source, int destination,
                          int[] highlightNodes, boolean highlightIsCycle) {
        if (n > MAX_DRAWABLE_NODES) {
            return false;
        }

        GraphicsContext gc = getGraphicsContext2D();
        PixelArtUtils.disableSmoothing(gc);
        gc.clearRect(0, 0, CANVAS_SIZE, CANVAS_SIZE);

        if (n == 0) {
            return true;
        }

        double[][] positions = computeBestLayout(graph, n);
        double nodeSize = computeNodeSize(n);

        Set<Long> highlightEdges = buildHighlightEdgeSet(highlightNodes, highlightIsCycle);
        boolean[] onHighlight = buildHighlightNodeMask(n, highlightNodes);

        drawEdges(gc, graph, positions, highlightEdges, highlightIsCycle);
        drawNodes(gc, positions, nodeSize, onHighlight, source, destination);

        return true;
    }

    /**
     * Prueba LAYOUT_CANDIDATE_COUNT disposiciones distintas (cada una
     * con una semilla al azar) y devuelve la que tiene menos cruces
     * de aristas. Minimizar cruces de forma exacta es un problema
     * NP-dificil en general; esta es una heuristica "mejor de N
     * intentos al azar", simple y suficientemente efectiva para los
     * tamanos de este proyecto.
     */
    private double[][] computeBestLayout(Graph graph, int n) {
        String key = buildGraphKey(graph, n);
        double[][] cached = layoutCache.get(key);
        if (cached != null) {
            return cached;
        }

        List<int[]> edgePairs = collectEdgePairs(graph, n);
        double[][] result;

        if (edgePairs.size() > MAX_EDGES_FOR_CROSSING_OPTIMIZATION) {
            // Demasiadas aristas para que valga la pena optimizar: un
            // solo intento al azar, y ya.
            long seed = layoutSeedGenerator.nextLong();
            result = ScatterLayout.compute(n, CANVAS_SIZE, CANVAS_SIZE, SCATTER_MARGIN, SCATTER_MARGIN, seed);
        } else {
            double[][] bestPositions = null;
            int bestCrossings = Integer.MAX_VALUE;

            for (int i = 0; i < LAYOUT_CANDIDATE_COUNT; i++) {
                long seed = layoutSeedGenerator.nextLong();
                double[][] candidate = ScatterLayout.compute(n, CANVAS_SIZE, CANVAS_SIZE, SCATTER_MARGIN, SCATTER_MARGIN, seed);
                int crossings = countCrossings(candidate, edgePairs);

                if (crossings < bestCrossings) {
                    bestCrossings = crossings;
                    bestPositions = candidate;
                }
                if (bestCrossings == 0) {
                    break; // no se puede mejorar mas: cero cruces
                }
            }
            result = bestPositions;
        }

        layoutCache.put(key, result);
        return result;
    }

    /**
     * Construye una clave que identifica el CONTENIDO del grafo (no
     * la instancia de objeto): cantidad de nodos, mas cada arista con
     * su peso. Dos grafos con exactamente los mismos datos producen
     * la misma clave, sin importar si son instancias distintas de
     * Graph (como pasa cada vez que se vuelve a parsear el mismo texto).
     */
    private String buildGraphKey(Graph graph, int n) {
        StringBuilder key = new StringBuilder();
        key.append(n).append(';');
        for (int u = 0; u < n; u++) {
            for (Edge edge : graph.getNeighbors(u)) {
                key.append(u).append('-').append(edge.getTo()).append('-').append(edge.getWeight()).append(',');
            }
        }
        return key.toString();
    }

    /** Recolecta todas las aristas del grafo como pares de indices (u, v), ignorando self-loops. */
    private List<int[]> collectEdgePairs(Graph graph, int n) {
        List<int[]> edges = new ArrayList<>();
        for (int u = 0; u < n; u++) {
            for (Edge edge : graph.getNeighbors(u)) {
                int v = edge.getTo();
                if (u != v) {
                    edges.add(new int[] { u, v });
                }
            }
        }
        return edges;
    }

    /**
     * Cuenta cuantos pares de aristas se cruzan geometricamente,
     * tratando cada una como un segmento recto entre los centros de
     * sus nodos (una aproximacion: algunas aristas se dibujan curvas
     * si hay reciprocidad, pero optimizar sobre la version recta ya
     * reduce los cruces de la version final renderizada).
     */
    private int countCrossings(double[][] positions, List<int[]> edges) {
        int count = 0;
        for (int i = 0; i < edges.size(); i++) {
            int[] e1 = edges.get(i);
            for (int j = i + 1; j < edges.size(); j++) {
                int[] e2 = edges.get(j);
                // Aristas que comparten un extremo no cuentan como
                // "cruce": es normal que se junten en un nodo comun.
                if (e1[0] == e2[0] || e1[0] == e2[1] || e1[1] == e2[0] || e1[1] == e2[1]) {
                    continue;
                }
                if (segmentsIntersect(positions[e1[0]], positions[e1[1]], positions[e2[0]], positions[e2[1]])) {
                    count++;
                }
            }
        }
        return count;
    }

    /** Test clasico de interseccion de segmentos basado en orientacion (CCW). */
    private boolean segmentsIntersect(double[] p1, double[] p2, double[] p3, double[] p4) {
        double d1 = crossProduct(p3, p4, p1);
        double d2 = crossProduct(p3, p4, p2);
        double d3 = crossProduct(p1, p2, p3);
        double d4 = crossProduct(p1, p2, p4);

        return ((d1 > 0 && d2 < 0) || (d1 < 0 && d2 > 0))
                && ((d3 > 0 && d4 < 0) || (d3 < 0 && d4 > 0));
    }

    private double crossProduct(double[] a, double[] b, double[] c) {
        return (b[0] - a[0]) * (c[1] - a[1]) - (b[1] - a[1]) * (c[0] - a[0]);
    }

    /**
     * Con ScatterLayout no hay una formula geometrica regular como la
     * circunferencia (CircularLayout) o el tamano de celda (GridLayout):
     * se estima el tamano en base al area promedio disponible por nodo.
     */
    private double computeNodeSize(int n) {
        double usableWidth = CANVAS_SIZE - 2 * SCATTER_MARGIN;
        double usableHeight = CANVAS_SIZE - 2 * SCATTER_MARGIN;
        double areaPerNode = (usableWidth * usableHeight) / n;
        double estimatedSpacing = Math.sqrt(areaPerNode);
        return Math.max(10, Math.min(24, estimatedSpacing * 0.4));
    }

    private void drawEdges(GraphicsContext gc, Graph graph, double[][] positions,
                           Set<Long> highlightEdges, boolean highlightIsCycle) {
        // Pasada 1: todas las aristas normales (linea + flecha + peso).
        for (int u = 0; u < positions.length; u++) {
            for (Edge edge : graph.getNeighbors(u)) {
                int v = edge.getTo();
                if (highlightEdges.contains(edgeKey(u, v))) {
                    continue; // se dibuja despues, resaltada
                }
                double[] control = curveControlPointOrNull(graph, positions, u, v);

                gc.setLineDashes(4, 4);
                gc.setLineWidth(1.3);
                gc.setStroke(EDGE_DIM);
                drawCurvedArrow(gc, positions[u], positions[v], control);

                gc.setLineDashes(null);
                drawEdgeWeightLabel(gc, positions[u], positions[v], control, edge.getWeight());
            }
        }

        // Pasada 2: aristas resaltadas (ruta o ciclo), encima de todo.
        Color glowColor = highlightIsCycle ? CYCLE_GLOW : ROUTE_GLOW;
        Color coreColor = highlightIsCycle ? CYCLE_COLOR : ROUTE_COLOR;

        for (int u = 0; u < positions.length; u++) {
            for (Edge edge : graph.getNeighbors(u)) {
                int v = edge.getTo();
                if (!highlightEdges.contains(edgeKey(u, v))) {
                    continue;
                }
                double[] control = curveControlPointOrNull(graph, positions, u, v);

                gc.setLineDashes(null);
                gc.setLineWidth(6);
                gc.setStroke(glowColor);
                drawCurvedArrow(gc, positions[u], positions[v], control);
                gc.setLineWidth(2.2);
                gc.setStroke(coreColor);
                drawCurvedArrow(gc, positions[u], positions[v], control);

                drawEdgeWeightLabel(gc, positions[u], positions[v], control, edge.getWeight());
            }
        }
    }

    /**
     * Si existe la arista opuesta (v->u ademas de u->v), devuelve un
     * punto de control para dibujar u->v como una curva (arqueada
     * hacia un lado segun si u < v), en vez de una linea recta que
     * quedaria exactamente encimada con la de v->u. Si no existe la
     * arista opuesta, devuelve null (linea recta normal).
     */
    private double[] curveControlPointOrNull(Graph graph, double[][] positions, int u, int v) {
        if (!hasReverseEdge(graph, u, v)) {
            return null;
        }

        double[] from = positions[u];
        double[] to = positions[v];
        double dx = to[0] - from[0];
        double dy = to[1] - from[1];
        double len = Math.hypot(dx, dy);
        if (len < 1e-6) {
            return null;
        }

        // Vector perpendicular unitario a la linea u->v. Notese que
        // este vector YA se invierte naturalmente cuando se dibuja la
        // arista opuesta (v->u), porque su direccion (dx, dy) es la
        // opuesta. Por eso NO hace falta multiplicar por un signo
        // extra basado en u<v: hacerlo cancelaria ese efecto natural
        // y ambas aristas terminarian usando el mismo punto de
        // control (el mismo bug que se estaba viendo).
        double nx = -dy / len;
        double ny = dx / len;

        double midX = (from[0] + to[0]) / 2;
        double midY = (from[1] + to[1]) / 2;

        return new double[] { midX + nx * CURVE_OFFSET, midY + ny * CURVE_OFFSET };
    }

    private boolean hasReverseEdge(Graph graph, int u, int v) {
        for (Edge edge : graph.getNeighbors(v)) {
            if (edge.getTo() == u) {
                return true;
            }
        }
        return false;
    }

    /**
     * Dibuja el peso de una arista sobre un recuadro solido que
     * "interrumpe" la linea punteada, en el punto medio real de la
     * linea (que si es curva, es el punto medio de la curva de
     * Bezier cuadratica en t=0.5, no el punto medio geometrico entre
     * from y to).
     */
    private void drawEdgeWeightLabel(GraphicsContext gc, double[] from, double[] to, double[] control, long weight) {
        double midX;
        double midY;
        if (control != null) {
            // Punto medio (t=0.5) de la curva cuadratica de Bezier:
            // 0.25*from + 0.5*control + 0.25*to
            midX = 0.25 * from[0] + 0.5 * control[0] + 0.25 * to[0];
            midY = 0.25 * from[1] + 0.5 * control[1] + 0.25 * to[1];
        } else {
            midX = (from[0] + to[0]) / 2;
            midY = (from[1] + to[1]) / 2;
        }

        String text = String.valueOf(weight);
        double approxCharWidth = 6.0;
        double boxWidth = Math.max(14, text.length() * approxCharWidth + 6);
        double boxHeight = 12;

        gc.setFill(WEIGHT_LABEL_BACKGROUND);
        gc.fillRoundRect(midX - boxWidth / 2, midY - boxHeight / 2, boxWidth, boxHeight, 4, 4);
        gc.setStroke(WEIGHT_LABEL_BORDER);
        gc.setLineWidth(1);
        gc.strokeRoundRect(midX - boxWidth / 2, midY - boxHeight / 2, boxWidth, boxHeight, 4, 4);

        gc.setFont(Font.font("Monospace", FontWeight.BOLD, 9));
        gc.setTextAlign(TextAlignment.CENTER);
        gc.setTextBaseline(VPos.CENTER);
        gc.setFill(LABEL_COLOR);
        gc.fillText(text, midX, midY + 1);

        // Se restauran los defaults de alineacion de texto, ya que
        // drawNodes() tambien dibuja texto (los numeros de nodo) y
        // no usa alineacion centrada por coordenada.
        gc.setTextAlign(TextAlignment.LEFT);
        gc.setTextBaseline(VPos.BASELINE);
    }

    /** Dibuja la linea (recta si control es null, curva si no) con su punta de flecha en 'to'. */
    private void drawCurvedArrow(GraphicsContext gc, double[] from, double[] to, double[] control) {
        gc.beginPath();
        gc.moveTo(from[0], from[1]);
        if (control != null) {
            gc.quadraticCurveTo(control[0], control[1], to[0], to[1]);
        } else {
            gc.lineTo(to[0], to[1]);
        }
        gc.stroke();

        // Se desactiva el patron punteado antes de dibujar la flecha:
        // sus lineas son muy cortas (unos 8px), y con un patron de
        // guiones 4-4 caian mayormente en los "huecos", haciendo que
        // la flecha casi no se viera.
        gc.setLineDashes(null);

        // La flecha se orienta segun la tangente de llegada: en una
        // curva, esa tangente apunta desde el punto de control hacia
        // 'to'; en una linea recta, desde 'from' hacia 'to'.
        double[] tangentOrigin = (control != null) ? control : from;
        drawArrowHead(gc, tangentOrigin, to);
    }

    private void drawArrowHead(GraphicsContext gc, double[] tangentOrigin, double[] tip) {
        double angle = Math.atan2(tip[1] - tangentOrigin[1], tip[0] - tangentOrigin[0]);
        double arrowLength = 8;
        double arrowAngle = Math.toRadians(25);

        double x1 = tip[0] - arrowLength * Math.cos(angle - arrowAngle);
        double y1 = tip[1] - arrowLength * Math.sin(angle - arrowAngle);
        double x2 = tip[0] - arrowLength * Math.cos(angle + arrowAngle);
        double y2 = tip[1] - arrowLength * Math.sin(angle + arrowAngle);

        gc.strokeLine(tip[0], tip[1], x1, y1);
        gc.strokeLine(tip[0], tip[1], x2, y2);
    }

    private void drawNodes(GraphicsContext gc, double[][] positions, double nodeSize,
                           boolean[] onHighlight, int source, int destination) {
        gc.setFont(Font.font("Monospace", FontWeight.BOLD, Math.max(8, nodeSize * 0.5)));

        for (int i = 0; i < positions.length; i++) {
            double x = positions[i][0];
            double y = positions[i][1];

            boolean isSource = (i == source);
            boolean isDestination = (i == destination);
            double size = (isSource || isDestination) ? nodeSize * 1.3 : nodeSize;
            double h = size / 2;

            gc.setFill(NODE_BODY);
            gc.fillOval(x - h, y - h, size, size);

            Color borderColor;
            double lineWidth;
            if (isSource) {
                borderColor = SOURCE_COLOR;
                lineWidth = 2.5;
            } else if (isDestination) {
                borderColor = DESTINATION_COLOR;
                lineWidth = 2.5;
            } else if (onHighlight[i]) {
                borderColor = ROUTE_COLOR;
                lineWidth = 2.2;
            } else {
                borderColor = NODE_BORDER;
                lineWidth = 1.3;
            }
            gc.setStroke(borderColor);
            gc.setLineWidth(lineWidth);
            gc.strokeOval(x - h, y - h, size, size);

            // "X marca el lugar" para el destino
            if (isDestination) {
                gc.setStroke(DESTINATION_COLOR);
                gc.setLineWidth(1.8);
                double m = h * 0.4;
                gc.strokeLine(x - m, y - m, x + m, y + m);
                gc.strokeLine(x - m, y + m, x + m, y - m);
            }

            gc.setFill(LABEL_COLOR);
            gc.fillText(String.valueOf(i), x - h * 0.35, y + h * 0.35);
        }
    }

    private Set<Long> buildHighlightEdgeSet(int[] highlightNodes, boolean isCycle) {
        Set<Long> edges = new HashSet<>();
        if (highlightNodes == null || highlightNodes.length < 2) {
            return edges;
        }
        for (int i = 1; i < highlightNodes.length; i++) {
            edges.add(edgeKey(highlightNodes[i - 1], highlightNodes[i]));
        }
        if (isCycle) {
            // el ciclo se cierra: del ultimo nodo de vuelta al primero
            edges.add(edgeKey(highlightNodes[highlightNodes.length - 1], highlightNodes[0]));
        }
        return edges;
    }

    private boolean[] buildHighlightNodeMask(int n, int[] highlightNodes) {
        boolean[] mask = new boolean[n];
        if (highlightNodes == null) {
            return mask;
        }
        for (int node : highlightNodes) {
            mask[node] = true;
        }
        return mask;
    }

    /** A diferencia de Mission 2 (no dirigido), aqui el orden SI importa: u->v es distinto de v->u. */
    private long edgeKey(int u, int v) {
        return ((long) u << 32) | (v & 0xFFFFFFFFL);
    }

    public static int getMaxDrawableNodes() {
        return MAX_DRAWABLE_NODES;
    }
}
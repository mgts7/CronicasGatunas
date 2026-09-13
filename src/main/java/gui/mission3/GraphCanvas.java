package gui.mission3;

import gui.common.CircularLayout;
import gui.common.PixelArtUtils;
import javafx.geometry.VPos;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.TextAlignment;
import model.graph.Edge;
import model.graph.Graph;

import java.util.HashSet;
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
    private static final double RADIUS = 190;
    private static final double CENTER = CANVAS_SIZE / 2;

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

        double[][] positions = CircularLayout.compute(n, CENTER, CENTER, RADIUS);
        double nodeSize = computeNodeSize(n);

        Set<Long> highlightEdges = buildHighlightEdgeSet(highlightNodes, highlightIsCycle);
        boolean[] onHighlight = buildHighlightNodeMask(n, highlightNodes);

        drawEdges(gc, graph, positions, highlightEdges, highlightIsCycle, nodeSize, source, destination);
        drawNodes(gc, positions, nodeSize, onHighlight, source, destination);

        return true;
    }

    private double computeNodeSize(int n) {
        double circumferenceShare = (2 * Math.PI * RADIUS) / n;
        return Math.max(10, Math.min(24, circumferenceShare * 0.45));
    }

    /**
     * Radio real con el que se dibuja un nodo (ver drawNodes(): source
     * y destination se dibujan 1.3x mas grandes que el resto). Se
     * necesita aqui para saber cuanto acortar cada arista que llega a
     * ese nodo, de modo que la punta de flecha quede justo en el
     * borde del circulo y no debajo de su relleno.
     */
    private double nodeRadius(int node, double nodeSize, int source, int destination) {
        boolean isEndpoint = (node == source || node == destination);
        double size = isEndpoint ? nodeSize * 1.3 : nodeSize;
        return size / 2;
    }

    private void drawEdges(GraphicsContext gc, Graph graph, double[][] positions,
                           Set<Long> highlightEdges, boolean highlightIsCycle,
                           double nodeSize, int source, int destination) {
        // Pasada 1: todas las aristas normales (linea + flecha + peso).
        for (int u = 0; u < positions.length; u++) {
            for (Edge edge : graph.getNeighbors(u)) {
                int v = edge.getTo();
                if (highlightEdges.contains(edgeKey(u, v))) {
                    continue; // se dibuja despues, resaltada
                }
                double[] control = curveControlPointOrNull(graph, positions, u, v);
                double targetRadius = nodeRadius(v, nodeSize, source, destination);

                gc.setLineDashes(4, 4);
                gc.setLineWidth(1.3);
                gc.setStroke(EDGE_DIM);
                drawCurvedArrow(gc, positions[u], positions[v], control, targetRadius);

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
                double targetRadius = nodeRadius(v, nodeSize, source, destination);

                gc.setLineDashes(null);
                gc.setLineWidth(6);
                gc.setStroke(glowColor);
                drawCurvedArrow(gc, positions[u], positions[v], control, targetRadius);
                gc.setLineWidth(2.2);
                gc.setStroke(coreColor);
                drawCurvedArrow(gc, positions[u], positions[v], control, targetRadius);

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
     *
     * Se sigue calculando sobre el segmento/curva COMPLETA (from -> to
     * originales, sin acortar), y no sobre la version recortada que
     * usa drawCurvedArrow(): recortar la linea es solo para que la
     * flecha no quede tapada por el nodo, pero el punto medio real de
     * la arista (para el rotulo del peso) no deberia moverse por eso.
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

    /**
     * Dibuja la linea (recta si control es null, curva si no) con su
     * punta de flecha, ACORTADA para que termine justo en el borde
     * del circulo del nodo destino (a 'targetRadius' px de 'to') y no
     * en su centro exacto. Sin este recorte, la punta de flecha (de
     * solo 8px) queda completamente tapada por el relleno del nodo,
     * que se dibuja despues (encima) en drawNodes().
     */
    private void drawCurvedArrow(GraphicsContext gc, double[] from, double[] to, double[] control, double targetRadius) {
        // La tangente de llegada define desde donde se retrocede: en
        // una curva, la tangente en 'to' apunta desde el punto de
        // control hacia 'to'; en una linea recta, desde 'from' hacia 'to'.
        double[] tangentOrigin = (control != null) ? control : from;
        double[] shortenedTo = pullBackTowards(tangentOrigin, to, targetRadius);

        gc.beginPath();
        gc.moveTo(from[0], from[1]);
        if (control != null) {
            gc.quadraticCurveTo(control[0], control[1], shortenedTo[0], shortenedTo[1]);
        } else {
            gc.lineTo(shortenedTo[0], shortenedTo[1]);
        }
        gc.stroke();

        // Se desactiva el patron punteado antes de dibujar la flecha:
        // sus lineas son muy cortas (unos 8px), y con un patron de
        // guiones 4-4 caian mayormente en los "huecos", haciendo que
        // la flecha casi no se viera.
        gc.setLineDashes(null);

        drawArrowHead(gc, tangentOrigin, shortenedTo);
    }

    /**
     * Devuelve el punto sobre el segmento (origin -> point), a una
     * distancia 'distance' antes de 'point'. Si 'point' esta a menos
     * de 'distance' de 'origin' (grafo muy pequeno / nodos muy cerca),
     * devuelve 'point' sin modificar para no invertir la flecha.
     */
    private double[] pullBackTowards(double[] origin, double[] point, double distance) {
        double dx = point[0] - origin[0];
        double dy = point[1] - origin[1];
        double len = Math.hypot(dx, dy);
        if (len <= distance || len < 1e-6) {
            return point;
        }
        double ratio = (len - distance) / len;
        return new double[] { origin[0] + dx * ratio, origin[1] + dy * ratio };
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
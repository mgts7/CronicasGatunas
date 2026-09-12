package gui.mission2;

import gui.common.CircularLayout;
import gui.common.PixelArtUtils;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import model.graph.Edge;
import model.graph.Graph;

import java.util.HashSet;
import java.util.Set;

/**
 * ============================================================
 * GraphCanvas - Mision 2
 * ============================================================
 *
 * Dibuja la red como un mapa de servidores conectados por cables de
 * neon: cada nodo es una "torre de servidor" pixel-art, y cada
 * conexion es un cable con un halo brillante. El camino de Dijkstra
 * se resalta con un color mas intenso (cyan) y un halo mas ancho,
 * simulando el "pulso" de datos viajando por la red.
 *
 * Las posiciones de los nodos se calculan con CircularLayout: sin
 * ninguna libreria de grafos (ver seccion 2.1 del enunciado).
 *
 * Respeta el limite de dibujo de la seccion 2.3: redes de mas de 60
 * nodos no se dibujan (se devuelve false y el llamador debe mostrar
 * el mensaje "drawing omitted" en su lugar).
 */
public final class GraphCanvas extends Canvas {

    private static final int MAX_DRAWABLE_NODES = 60;
    private static final double CANVAS_SIZE = 520;
    private static final double RADIUS = 210;
    private static final double CENTER = CANVAS_SIZE / 2;

    // Paleta "servidor neon"
    private static final Color EDGE_DIM = Color.rgb(80, 90, 120, 0.55);
    private static final Color EDGE_PATH_GLOW = Color.rgb(0, 229, 255, 0.35);
    private static final Color EDGE_PATH_CORE = Color.rgb(120, 245, 255);

    private static final Color NODE_BODY = Color.rgb(40, 44, 58);
    private static final Color NODE_BORDER = Color.rgb(90, 100, 130);
    private static final Color NODE_LIGHT = Color.rgb(90, 160, 220);

    private static final Color PATH_NODE_BORDER = Color.rgb(0, 229, 255);
    private static final Color SOURCE_COLOR = Color.rgb(90, 230, 140);
    private static final Color DESTINATION_COLOR = Color.rgb(255, 195, 70);

    private static final Color LABEL_COLOR = Color.rgb(220, 226, 235);

    public GraphCanvas() {
        super(CANVAS_SIZE, CANVAS_SIZE);
    }

    /**
     * Dibuja la red completa.
     *
     * @param path la secuencia de nodos del camino a resaltar (desde
     *             source hasta destination), o null/vacio si no hay
     *             que resaltar ningun camino (por ejemplo, en la
     *             previsualizacion antes de resolver).
     * @return true si se dibujo (n dentro del limite de la seccion
     *         2.3), false si se omitio por ser demasiado grande.
     */
    public boolean render(Graph graph, int n, int source, int destination, int[] path) {
        if (n > MAX_DRAWABLE_NODES) {
            return false;
        }

        GraphicsContext gc = getGraphicsContext2D();
        PixelArtUtils.disableSmoothing(gc);
        gc.clearRect(0, 0, CANVAS_SIZE, CANVAS_SIZE);

        if (n == 0) {
            return true; // nada que dibujar, pero no es un error
        }

        double[][] positions = CircularLayout.compute(n, CENTER, CENTER, RADIUS);
        double nodeSize = computeNodeSize(n);

        Set<Long> pathEdges = buildPathEdgeSet(path);
        boolean[] onPath = buildPathNodeMask(n, path);

        drawEdges(gc, graph, positions, pathEdges);
        drawNodes(gc, positions, nodeSize, onPath, source, destination);

        return true;
    }

    /** El tamano visual de cada nodo se reduce a medida que hay mas nodos, para que no se amontonen. */
    private double computeNodeSize(int n) {
        double circumferenceShare = (2 * Math.PI * RADIUS) / n;
        return Math.max(10, Math.min(26, circumferenceShare * 0.45));
    }

    private void drawEdges(GraphicsContext gc, Graph graph, double[][] positions, Set<Long> pathEdges) {
        // Primero todas las conexiones normales (detras), luego las
        // del camino encima (con halo), para que el camino resaltado
        // nunca quede tapado por una conexion sin resaltar.
        gc.setLineWidth(1.5);
        gc.setStroke(EDGE_DIM);
        for (int u = 0; u < positions.length; u++) {
            for (Edge edge : graph.getNeighbors(u)) {
                int v = edge.getTo();
                if (v <= u) {
                    continue; // evita dibujar cada arista bidireccional dos veces
                }
                if (pathEdges.contains(edgeKey(u, v))) {
                    continue; // se dibuja despues, resaltada
                }
                drawLine(gc, positions[u], positions[v]);
            }
        }

        for (int u = 0; u < positions.length; u++) {
            for (Edge edge : graph.getNeighbors(u)) {
                int v = edge.getTo();
                if (v <= u) {
                    continue;
                }
                if (!pathEdges.contains(edgeKey(u, v))) {
                    continue;
                }
                // Halo ancho translucido primero (efecto de brillo neon)...
                gc.setLineWidth(6);
                gc.setStroke(EDGE_PATH_GLOW);
                drawLine(gc, positions[u], positions[v]);
                // ...y encima, el nucleo brillante y delgado.
                gc.setLineWidth(2.2);
                gc.setStroke(EDGE_PATH_CORE);
                drawLine(gc, positions[u], positions[v]);
            }
        }
    }

    private void drawLine(GraphicsContext gc, double[] from, double[] to) {
        gc.strokeLine(from[0], from[1], to[0], to[1]);
    }

    private void drawNodes(GraphicsContext gc, double[][] positions, double nodeSize,
                           boolean[] onPath, int source, int destination) {
        gc.setFont(Font.font(Math.max(8, nodeSize * 0.55)));

        for (int i = 0; i < positions.length; i++) {
            double x = positions[i][0];
            double y = positions[i][1];
            double half = nodeSize / 2;

            boolean isSource = (i == source);
            boolean isDestination = (i == destination);
            double size = isDestination ? nodeSize * 1.35 : nodeSize;
            double h = size / 2;

            // Cuerpo del "servidor"
            gc.setFill(NODE_BODY);
            gc.fillRoundRect(x - h, y - h, size, size, 4, 4);

            // Borde: color especial si es source/destination/parte del camino
            Color borderColor;
            if (isSource) {
                borderColor = SOURCE_COLOR;
            } else if (isDestination) {
                borderColor = DESTINATION_COLOR;
            } else if (onPath[i]) {
                borderColor = PATH_NODE_BORDER;
            } else {
                borderColor = NODE_BORDER;
            }
            gc.setStroke(borderColor);
            gc.setLineWidth(isSource || isDestination || onPath[i] ? 2.5 : 1.2);
            gc.strokeRoundRect(x - h, y - h, size, size, 4, 4);

            // Lucecita de "actividad" del servidor
            gc.setFill(isDestination ? DESTINATION_COLOR : (isSource ? SOURCE_COLOR : NODE_LIGHT));
            double dotSize = size * 0.16;
            gc.fillOval(x - h + size * 0.14, y - h + size * 0.14, dotSize, dotSize);

            // Numero de nodo
            gc.setFill(LABEL_COLOR);
            gc.fillText(String.valueOf(i), x - half * 0.35, y + half * 0.35);
        }
    }

    private Set<Long> buildPathEdgeSet(int[] path) {
        Set<Long> edges = new HashSet<>();
        if (path == null) {
            return edges;
        }
        for (int i = 1; i < path.length; i++) {
            edges.add(edgeKey(path[i - 1], path[i]));
        }
        return edges;
    }

    private boolean[] buildPathNodeMask(int n, int[] path) {
        boolean[] mask = new boolean[n];
        if (path == null) {
            return mask;
        }
        for (int node : path) {
            mask[node] = true;
        }
        return mask;
    }

    /** Codifica un par (u, v) sin importar el orden, para poder buscarlo en el Set independientemente de la direccion. */
    private long edgeKey(int u, int v) {
        int lo = Math.min(u, v);
        int hi = Math.max(u, v);
        return ((long) lo << 32) | (hi & 0xFFFFFFFFL);
    }

    public static int getMaxDrawableNodes() {
        return MAX_DRAWABLE_NODES;
    }
}
package gui.mission4;

import algorithm.mission4.KruskalSolver.Cable;
import gui.common.GridLayout;
import gui.common.PixelArtUtils;
import javafx.geometry.VPos;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.TextAlignment;

import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

/**
 * ============================================================
 * NetworkCanvas - Mision 4
 * ============================================================
 *
 * Dibuja la red de intersecciones como techos de ciudad conectados
 * por cables/tendederos: cada nodo es una pequena azotea (con una
 * ventanita iluminada, variable por nodo igual que el pasto de
 * Mission 1), y cada cable disponible es una linea que "cuelga"
 * levemente (una curva sutil hacia abajo, como un tendedero real).
 * Los cables que forman el MST (Kruskal) se resaltan en color calido
 * tipo "luces de navidad"; los descartados quedan tenues.
 *
 * Los nodos se numeran mostrando indice+1 (1-indexado), para que
 * coincidan con la numeracion que el usuario escribio en el input
 * (el enunciado numera las intersecciones de 1 a N), aunque
 * internamente el proyecto trabaja 0-indexado (ver Mission4Parser).
 *
 * Respeta el limite de dibujo de la seccion 2.3: Mission 4 permite
 * hasta 100 intersecciones y 300 cables.
 */
public final class NetworkCanvas extends Canvas {

    private static final int MAX_DRAWABLE_NODES = 100;
    private static final int MAX_DRAWABLE_CABLES = 300;
    private static final double CANVAS_SIZE = 520;
    private static final double GRID_MARGIN = 45;
    private static final double JITTER_FRACTION = 0.22; // que tan "desalineados" se ven los techos, como ciudad real
    private static final double SAG_AMOUNT = 10; // cuanto "cuelga" cada cable, efecto tendedero

    // Paleta "techos de ciudad"
    private static final Color CABLE_DIM = Color.rgb(180, 185, 200, 0.45);
    private static final Color CABLE_SELECTED_GLOW = Color.rgb(255, 210, 110, 0.4);
    private static final Color CABLE_SELECTED_CORE = Color.rgb(255, 200, 80);

    private static final Color ROOF_BODY = Color.rgb(95, 60, 48);
    private static final Color ROOF_RIDGE = Color.rgb(70, 42, 34);
    private static final Color ROOF_BORDER = Color.rgb(50, 30, 24);
    private static final Color WINDOW_LIT = Color.rgb(255, 205, 110);
    private static final Color WINDOW_OFF = Color.rgb(60, 55, 60);
    private static final Color LABEL_COLOR = Color.rgb(235, 225, 210);

    public NetworkCanvas() {
        super(CANVAS_SIZE, CANVAS_SIZE);
    }

    /**
     * Dibuja la red completa.
     *
     * @param mstCables los cables que forman el arbol de expansion
     *                  minima (para resaltarlos), o null/vacio si no
     *                  hay que resaltar nada todavia (previsualizacion).
     * @return true si se dibujo (n y cantidad de cables dentro de los
     *         limites de la seccion 2.3), false si se omitio.
     */
    public boolean render(int n, List<Cable> allCables, List<Cable> mstCables) {
        if (n > MAX_DRAWABLE_NODES || allCables.size() > MAX_DRAWABLE_CABLES) {
            return false;
        }

        GraphicsContext gc = getGraphicsContext2D();
        PixelArtUtils.disableSmoothing(gc);
        gc.clearRect(0, 0, CANVAS_SIZE, CANVAS_SIZE);

        if (n == 0) {
            return true;
        }

        double[][] positions = GridLayout.compute(n, CANVAS_SIZE, CANVAS_SIZE, GRID_MARGIN, GRID_MARGIN, JITTER_FRACTION);
        double nodeSize = computeNodeSize(n);

        Set<Cable> selected = new HashSet<>();
        if (mstCables != null) {
            selected.addAll(mstCables); // identidad de objeto: son las MISMAS instancias que allCables
        }

        drawCables(gc, allCables, positions, selected);
        drawRoofs(gc, positions, nodeSize);

        return true;
    }

    /** El tamano visual de cada nodo se ajusta al tamano de su celda en el grid, para que no se amontonen. */
    private double computeNodeSize(int n) {
        int cols = (int) Math.ceil(Math.sqrt(n));
        int rows = (int) Math.ceil((double) n / cols);

        double usableWidth = CANVAS_SIZE - 2 * GRID_MARGIN;
        double usableHeight = CANVAS_SIZE - 2 * GRID_MARGIN;
        double cellWidth = usableWidth / cols;
        double cellHeight = usableHeight / rows;

        double smallerDimension = Math.min(cellWidth, cellHeight);
        return Math.max(10, Math.min(26, smallerDimension * 0.5));
    }

    private void drawCables(GraphicsContext gc, List<Cable> allCables, double[][] positions, Set<Cable> selected) {
        // Pasada 1: cables NO seleccionados (tenues, detras).
        for (Cable cable : allCables) {
            if (cable.getU() == cable.getV()) {
                continue; // self-loop: no aporta nada visualmente, se omite
            }
            if (selected.contains(cable)) {
                continue; // se dibuja despues, resaltado
            }
            gc.setLineWidth(1.2);
            gc.setStroke(CABLE_DIM);
            drawSaggingCable(gc, positions[cable.getU()], positions[cable.getV()]);
            drawCostLabel(gc, positions[cable.getU()], positions[cable.getV()], cable.getCost(), false);
        }

        // Pasada 2: cables del MST (resaltados, encima de todo).
        for (Cable cable : allCables) {
            if (cable.getU() == cable.getV()) {
                continue;
            }
            if (!selected.contains(cable)) {
                continue;
            }
            gc.setLineWidth(5);
            gc.setStroke(CABLE_SELECTED_GLOW);
            drawSaggingCable(gc, positions[cable.getU()], positions[cable.getV()]);
            gc.setLineWidth(2);
            gc.setStroke(CABLE_SELECTED_CORE);
            drawSaggingCable(gc, positions[cable.getU()], positions[cable.getV()]);
            drawCostLabel(gc, positions[cable.getU()], positions[cable.getV()], cable.getCost(), true);
        }
    }

    /** Dibuja el cable con una leve curva hacia abajo, como un tendedero real, en vez de una linea perfectamente recta. */
    private void drawSaggingCable(GraphicsContext gc, double[] from, double[] to) {
        double midX = (from[0] + to[0]) / 2;
        double midY = (from[1] + to[1]) / 2 + SAG_AMOUNT;

        gc.beginPath();
        gc.moveTo(from[0], from[1]);
        gc.quadraticCurveTo(midX, midY, to[0], to[1]);
        gc.stroke();
    }

    private void drawCostLabel(GraphicsContext gc, double[] from, double[] to, long cost, boolean selected) {
        double midX = (from[0] + to[0]) / 2;
        double midY = (from[1] + to[1]) / 2 + SAG_AMOUNT * 0.6; // sigue aproximadamente la curva del cable

        String text = String.valueOf(cost);
        double approxCharWidth = 6.0;
        double boxWidth = Math.max(14, text.length() * approxCharWidth + 6);
        double boxHeight = 12;

        gc.setFill(selected ? Color.rgb(90, 65, 30, 0.9) : Color.rgb(40, 40, 55, 0.75));
        gc.fillRoundRect(midX - boxWidth / 2, midY - boxHeight / 2, boxWidth, boxHeight, 4, 4);

        gc.setFont(Font.font("Monospace", FontWeight.BOLD, 9));
        gc.setTextAlign(TextAlignment.CENTER);
        gc.setTextBaseline(VPos.CENTER);
        gc.setFill(selected ? CABLE_SELECTED_CORE : LABEL_COLOR);
        gc.fillText(text, midX, midY + 1);

        gc.setTextAlign(TextAlignment.LEFT);
        gc.setTextBaseline(VPos.BASELINE);
    }

    private void drawRoofs(GraphicsContext gc, double[][] positions, double nodeSize) {
        gc.setFont(Font.font("Monospace", FontWeight.BOLD, Math.max(8, nodeSize * 0.5)));

        for (int i = 0; i < positions.length; i++) {
            double x = positions[i][0];
            double y = positions[i][1];
            double h = nodeSize / 2;

            // Cuerpo de la azotea
            gc.setFill(ROOF_BODY);
            gc.fillRoundRect(x - h, y - h, nodeSize, nodeSize, 3, 3);
            gc.setStroke(ROOF_BORDER);
            gc.setLineWidth(1.3);
            gc.strokeRoundRect(x - h, y - h, nodeSize, nodeSize, 3, 3);

            // Linea de "cumbrera" del techo
            gc.setStroke(ROOF_RIDGE);
            gc.setLineWidth(1);
            gc.strokeLine(x - h, y, x + h, y);

            // Ventanita: iluminada o apagada, variable por nodo pero
            // determinista (misma decoracion en cada redibujado).
            Random rng = PixelArtUtils.deterministicRandom(i, i * 7);
            boolean lit = rng.nextDouble() < 0.6;
            double windowSize = nodeSize * 0.22;
            gc.setFill(lit ? WINDOW_LIT : WINDOW_OFF);
            gc.fillRect(x - windowSize / 2, y - h * 0.55, windowSize, windowSize);

            // Numero de interseccion (1-indexado, como en el input original)
            gc.setFill(LABEL_COLOR);
            gc.fillText(String.valueOf(i + 1), x - h * 0.4, y + h * 0.75);
        }
    }

    public static int getMaxDrawableNodes() {
        return MAX_DRAWABLE_NODES;
    }

    public static int getMaxDrawableCables() {
        return MAX_DRAWABLE_CABLES;
    }
}
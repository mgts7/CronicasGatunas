package gui.common;

import java.util.Random;

/**
 * ============================================================
 * GridLayout
 * ============================================================
 *
 * Distribuye N nodos en una cuadricula (filas x columnas lo mas
 * cuadrada posible), con un leve "jitter" (variacion de posicion)
 * determinista por nodo, para que no se vea perfectamente uniforme
 * -- como una ciudad real, donde los edificios no estan alineados
 * con precision milimetrica.
 *
 * Se usa en Mission 4 especificamente para que su distribucion de
 * nodos se vea distinta de CircularLayout (usado por Missions 2 y
 * 3): ademas de dar variedad visual entre misiones, una cuadricula
 * encaja mejor con la tematica de "techos de ciudad" (manzanas
 * urbanas) que un circulo.
 */
public final class GridLayout {

    private GridLayout() {
        // Utility class: no se instancia.
    }

    /**
     * @param n             cantidad de nodos
     * @param areaWidth     ancho total del area disponible (ej. el canvas)
     * @param areaHeight    alto total del area disponible
     * @param marginX       margen horizontal a dejar en los bordes
     * @param marginY       margen vertical a dejar en los bordes
     * @param jitterFraction cuanto puede desviarse cada nodo de su
     *                      posicion "perfecta" en la grilla, como
     *                      fraccion del tamano de su celda (0 = grid
     *                      perfecto sin variacion, 0.3 = hasta un 30%
     *                      de desviacion en cada eje)
     * @return un arreglo de n posiciones, positions[i] = {x, y}
     */
    public static double[][] compute(int n, double areaWidth, double areaHeight,
                                     double marginX, double marginY, double jitterFraction) {
        double[][] positions = new double[n][2];
        if (n == 0) {
            return positions;
        }

        int cols = (int) Math.ceil(Math.sqrt(n));
        int rows = (int) Math.ceil((double) n / cols);

        double usableWidth = areaWidth - 2 * marginX;
        double usableHeight = areaHeight - 2 * marginY;
        double cellWidth = usableWidth / cols;
        double cellHeight = usableHeight / rows;

        double maxJitterX = cellWidth * jitterFraction;
        double maxJitterY = cellHeight * jitterFraction;

        for (int i = 0; i < n; i++) {
            int row = i / cols;
            int col = i % cols;

            double baseX = marginX + cellWidth * (col + 0.5);
            double baseY = marginY + cellHeight * (row + 0.5);

            // Semilla determinista por nodo: la misma red siempre se
            // ve igual entre un redibujado y otro (ver PixelArtUtils).
            Random rng = PixelArtUtils.deterministicRandom(row * 131 + col * 37, i);
            double offsetX = (rng.nextDouble() - 0.5) * 2 * maxJitterX;
            double offsetY = (rng.nextDouble() - 0.5) * 2 * maxJitterY;

            positions[i][0] = baseX + offsetX;
            positions[i][1] = baseY + offsetY;
        }

        return positions;
    }
}
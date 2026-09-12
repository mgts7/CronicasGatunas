package gui.mission1;

import java.util.Random;

/**
 * ============================================================
 * RandomMission1Generator
 * ============================================================
 *
 * Genera un input de Mision 1 aleatorio, en el formato de texto
 * EXACTO que espera Mission1Parser (seccion 3 del enunciado), para
 * que el boton "Generar" de la GUI pueda demostrar la app sin
 * necesidad de escribir input a mano.
 *
 * Esto NO es parte de los requisitos de parsing/algoritmos del
 * enunciado: es una utilidad de conveniencia exclusiva de la GUI,
 * por eso vive en gui.mission1 y no en io/.
 *
 * El resultado se reinyecta a traves del mismo Mission1Parser que
 * usa cualquier otro input, asi que no hay dos caminos de codigo
 * distintos para "input real" vs "input generado".
 */
public final class RandomMission1Generator {

    private static final int MIN_DIMENSION = 8;
    private static final int MAX_DIMENSION = 35; // bien por debajo del limite de dibujo (50x50, seccion 2.3)
    private static final double BOMB_DENSITY = 0.18; // ~18% de las celdas (sin contar start/end)

    private final Random random = new Random();

    /** Genera un unico caso de prueba, en texto, listo para pasarle a Mission1Parser.parse(...). */
    public String generate() {
        int rows = randomDimension();
        int cols = randomDimension();

        int startRow;
        int startCol;
        int endRow;
        int endCol;
        do {
            startRow = random.nextInt(rows);
            startCol = random.nextInt(cols);
            endRow = random.nextInt(rows);
            endCol = random.nextInt(cols);
        } while (startRow == endRow && startCol == endCol);

        boolean[][] bomb = randomBombs(rows, cols, startRow, startCol, endRow, endCol);

        return formatAsInput(rows, cols, bomb, startRow, startCol, endRow, endCol);
    }

    private int randomDimension() {
        return MIN_DIMENSION + random.nextInt(MAX_DIMENSION - MIN_DIMENSION + 1);
    }

    private boolean[][] randomBombs(int rows, int cols, int startRow, int startCol, int endRow, int endCol) {
        boolean[][] bomb = new boolean[rows][cols];
        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                boolean isStart = (r == startRow && c == startCol);
                boolean isEnd = (r == endRow && c == endCol);
                if (isStart || isEnd) {
                    continue; // nunca poner una bomba justo en start/end
                }
                if (random.nextDouble() < BOMB_DENSITY) {
                    bomb[r][c] = true;
                }
            }
        }
        return bomb;
    }

    /** Arma el texto exactamente en el formato de la seccion 3: R C, filas con bombas, start, end, sentinela 0 0. */
    private String formatAsInput(int rows, int cols, boolean[][] bomb,
                                 int startRow, int startCol, int endRow, int endCol) {
        StringBuilder bombLines = new StringBuilder();
        int bombRowCount = 0;

        for (int r = 0; r < rows; r++) {
            StringBuilder columnsInRow = new StringBuilder();
            int countInRow = 0;
            for (int c = 0; c < cols; c++) {
                if (bomb[r][c]) {
                    countInRow++;
                    columnsInRow.append(' ').append(c);
                }
            }
            if (countInRow > 0) {
                bombRowCount++;
                bombLines.append(r).append(' ').append(countInRow).append(columnsInRow).append('\n');
            }
        }

        StringBuilder sb = new StringBuilder();
        sb.append(rows).append(' ').append(cols).append('\n');
        sb.append(bombRowCount).append('\n');
        sb.append(bombLines);
        sb.append(startRow).append(' ').append(startCol).append('\n');
        sb.append(endRow).append(' ').append(endCol).append('\n');
        sb.append("0 0\n");

        return sb.toString();
    }
}
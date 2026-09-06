package io;

import errorhandling.InputValidationException;

import java.util.ArrayList;
import java.util.List;
import model.grid.Grid;

/**
 * ============================================================
 * Mission1Parser
 * ============================================================
 * Convierte el texto pegado en la GUI (formato de la seccion 3 del
 * enunciado) en una lista de casos de prueba ya listos para
 * resolver con BFSSolver/DFSSolver.
 */
public final class Mission1Parser {

    private static final int MIN_DIMENSION = 1;
    private static final int MAX_DIMENSION = 1000;

    private Mission1Parser() {
        // Utility class: no se instancia.
    }

    /**
     * Parsea todos los casos de prueba del texto de entrada.
     * Se detiene (sin procesar) en cuanto encuentra R = 0 y C = 0,
     * tal como indica el enunciado.
     */
    public static List<TestCase> parse(String rawInput) {
        InputTokenizer tokenizer = new InputTokenizer(rawInput);
        List<TestCase> testCases = new ArrayList<>();

        while (tokenizer.hasNextToken()) {
            int rows = tokenizer.nextInt();
            int cols = tokenizer.nextInt();

            if (rows == 0 && cols == 0) {
                break; // Sentinela de fin de entrada: no se procesa.
            }

            validateDimensions(rows, cols);

            Grid grid = new Grid(rows, cols);
            readBombs(tokenizer, grid, rows, cols);

            int startRow = tokenizer.nextInt();
            int startCol = tokenizer.nextInt();
            int endRow = tokenizer.nextInt();
            int endCol = tokenizer.nextInt();

            validatePointInsideGrid(startRow, startCol, rows, cols, "de inicio");
            validatePointInsideGrid(endRow, endCol, rows, cols, "de destino");

            testCases.add(new TestCase(grid, startRow, startCol, endRow, endCol));
        }

        return testCases;
    }

    private static void readBombs(InputTokenizer tokenizer, Grid grid, int rows, int cols) {
        int bombRowCount = tokenizer.nextInt();
        validateRange(bombRowCount, 0, rows, "numero de filas con bombas");

        for (int i = 0; i < bombRowCount; i++) {
            int rowIndex = tokenizer.nextInt();
            validateRange(rowIndex, 0, rows - 1, "indice de fila con bombas");

            int bombCount = tokenizer.nextInt();
            validateRange(bombCount, 0, cols, "numero de bombas en la fila " + rowIndex);

            for (int b = 0; b < bombCount; b++) {
                int col = tokenizer.nextInt();
                validateRange(col, 0, cols - 1, "columna de bomba en la fila " + rowIndex);
                grid.setBomb(rowIndex, col);
            }
        }
    }

    private static void validateDimensions(int rows, int cols) {
        if (rows < MIN_DIMENSION || rows > MAX_DIMENSION
                || cols < MIN_DIMENSION || cols > MAX_DIMENSION) {
            throw new InputValidationException(
                    "Dimensiones de grid invalidas: R=" + rows + ", C=" + cols
                            + " (deben estar entre " + MIN_DIMENSION + " y " + MAX_DIMENSION
                            + ", o ambos 0 para terminar la entrada).");
        }
    }

    private static void validateRange(int value, int min, int max, String description) {
        if (value < min || value > max) {
            throw new InputValidationException(
                    "Valor fuera de rango para " + description + ": " + value
                            + " (se esperaba entre " + min + " y " + max + ").");
        }
    }

    private static void validatePointInsideGrid(int row, int col, int rows, int cols, String label) {
        if (row < 0 || row >= rows || col < 0 || col >= cols) {
            throw new InputValidationException(
                    "El punto " + label + " (" + row + ", " + col + ") queda fuera del grid "
                            + rows + "x" + cols + ".");
        }
    }

    /**
     * Un caso de prueba ya parseado: el grid con sus bombas cargadas,
     * mas las coordenadas de inicio y destino.
     */
    public static final class TestCase {
        private final Grid grid;
        private final int startRow;
        private final int startCol;
        private final int endRow;
        private final int endCol;

        public TestCase(Grid grid, int startRow, int startCol, int endRow, int endCol) {
            this.grid = grid;
            this.startRow = startRow;
            this.startCol = startCol;
            this.endRow = endRow;
            this.endCol = endCol;
        }

        public Grid getGrid() {
            return grid;
        }

        public int getStartRow() {
            return startRow;
        }

        public int getStartCol() {
            return startCol;
        }

        public int getEndRow() {
            return endRow;
        }

        public int getEndCol() {
            return endCol;
        }
    }
}
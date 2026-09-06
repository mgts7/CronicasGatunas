package model.grid;

/**
 * Representa el mapa de la Mision 1: una cuadricula de R filas por C
 * columnas, donde algunas celdas contienen bombas.
 *
 * Se guarda como un arreglo plano de boolean (bombs) en vez de un
 * boolean[][] para reducir overhead de memoria cuando R*C se acerca
 * a 10^6 celdas (limite del enunciado).
 */
public final class Grid {

    private final int rows;
    private final int cols;
    private final boolean[] bombs;

    public Grid(int rows, int cols) {
        this.rows = rows;
        this.cols = cols;
        this.bombs = new boolean[rows * cols];
    }

    public int getRows() {
        return rows;
    }

    public int getCols() {
        return cols;
    }

    /** Marca la celda (row, col) como que contiene una bomba. */
    public void setBomb(int row, int col) {
        bombs[toIndex(row, col)] = true;
    }

    /** Indica si la celda (row, col) contiene una bomba. */
    public boolean isBomb(int row, int col) {
        return bombs[toIndex(row, col)];
    }

    /** Indica si (row, col) esta dentro de los limites del grid. */
    public boolean isInside(int row, int col) {
        return row >= 0 && row < rows && col >= 0 && col < cols;
    }

    /**
     * Convierte una coordenada (row, col) en un indice plano unico,
     * usado por los solvers para arreglos de tipo visited/distance
     * de tamano rows*cols en vez de arreglos 2D.
     */
    public int toIndex(int row, int col) {
        return row * cols + col;
    }
}
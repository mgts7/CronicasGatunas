package algorithm.mission1;

import model.grid.Grid;

import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.Deque;

/**
 * ============================================================
 * BFS (Breadth-First Search) - Mision 1: Rescatando a Nina
 * ============================================================
 *
 * Por que BFS es la eleccion correcta aqui:
 *   El grid no tiene pesos (cada movimiento cuesta 1), asi que BFS
 *   explora "por niveles" y garantiza que la primera vez que se
 *   alcanza el destino, se hizo con el numero minimo de movimientos.
 *   DFS (ver DFSSolver) NO tiene esta garantia: encuentra *un*
 *   camino valido, no necesariamente el mas corto.
 *
 * Complejidad:
 *   - Tiempo:  O(R * C) - cada celda se encola y desencola una sola vez.
 *   - Espacio: O(R * C) - arreglo de distancias + cola.
 *
 * No se construye una lista de adyacencia explicita (a diferencia
 * del ejemplo generico de grafos): los vecinos se calculan al vuelo
 * con aritmetica de fila/columna, lo cual es indispensable para
 * grids de hasta 1000x1000 (10^6 celdas) sin desperdiciar memoria.
 */
public final class BFSSolver {

    /** Valor centinela que indica que el destino es inalcanzable. */
    public static final int UNREACHABLE = -1;

    // Orden fijo de exploracion: arriba, abajo, izquierda, derecha.
    // Para BFS este orden no afecta el resultado (la distancia minima
    // es la misma sin importar el orden), pero se mantiene por
    // consistencia con el DFS, donde el orden si es determinante.
    private static final int[] D_ROW = {-1, 1, 0, 0};
    private static final int[] D_COL = {0, 0, -1, 1};

    private BFSSolver() {
        // Utility class: no se instancia.
    }

    /**
     * Calcula el numero minimo de movimientos desde (startRow, startCol)
     * hasta (endRow, endCol).
     *
     * @return la distancia minima, 0 si start == end, o UNREACHABLE
     *         si no existe camino (o si start/end contienen una bomba).
     */
    public static int solve(Grid grid, int startRow, int startCol, int endRow, int endCol) {
        if (grid.isBomb(startRow, startCol) || grid.isBomb(endRow, endCol)) {
            return UNREACHABLE;
        }
        if (startRow == endRow && startCol == endCol) {
            return 0;
        }

        int rows = grid.getRows();
        int cols = grid.getCols();

        int[] distance = new int[rows * cols];
        Arrays.fill(distance, UNREACHABLE);

        int startIdx = grid.toIndex(startRow, startCol);
        int endIdx = grid.toIndex(endRow, endCol);

        distance[startIdx] = 0;

        Deque<Integer> queue = new ArrayDeque<>();
        queue.add(startIdx);

        while (!queue.isEmpty()) {
            int currentIdx = queue.poll();

            // Corte temprano: en cuanto se desencola el destino,
            // su distancia ya es minima (propiedad de BFS).
            if (currentIdx == endIdx) {
                return distance[currentIdx];
            }

            int currentRow = currentIdx / cols;
            int currentCol = currentIdx % cols;

            for (int dir = 0; dir < 4; dir++) {
                int neighborRow = currentRow + D_ROW[dir];
                int neighborCol = currentCol + D_COL[dir];

                if (!grid.isInside(neighborRow, neighborCol)) {
                    continue;
                }
                if (grid.isBomb(neighborRow, neighborCol)) {
                    continue;
                }

                int neighborIdx = grid.toIndex(neighborRow, neighborCol);
                if (distance[neighborIdx] != UNREACHABLE) {
                    continue; // ya visitado
                }

                distance[neighborIdx] = distance[currentIdx] + 1;
                queue.add(neighborIdx);
            }
        }

        // La cola se vacio sin encontrar el destino.
        return UNREACHABLE;
    }
}
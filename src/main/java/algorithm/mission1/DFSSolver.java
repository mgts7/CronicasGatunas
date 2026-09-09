package algorithm.mission1;

import model.grid.Grid;

/**
 * ============================================================
 * DFS (Depth-First Search) - Mision 1: Rescatando a Nina
 * ============================================================
 *
 * Por que este DFS es ITERATIVO (pila explicita) y no recursivo:
 *   El enunciado indica que los grids pueden llegar a 10^6 celdas.
 *   Una version recursiva como la del ejemplo generico de grafos
 *   (dfsRec, que usa la pila de llamadas del sistema) desbordaria
 *   el stack de Java en un grid asi de grande. Por eso se simula
 *   la recursion a mano con un arreglo que actua como pila:
 *   cada "frame" guarda el nodo actual y cual de sus 4 vecinos
 *   (en el orden arriba/abajo/izquierda/derecha) toca probar despues.
 *
 * Por que el resultado NO es necesariamente el camino mas corto:
 *   DFS se compromete con la primera rama que encuentra y solo
 *   retrocede (backtracking) cuando esa rama se agota. El camino
 *   que reporta es valido, pero normalmente mas largo que el de BFS
 *   (ver el ejemplo del enunciado: BFS 18 vs DFS 32 movimientos).
 *
 * Determinismo:
 *   El orden fijo arriba, abajo, izquierda, derecha hace que el
 *   resultado sea reproducible: cualquier otro orden de exploracion
 *   produce un camino (tambien valido) distinto, que no coincidiria
 *   con la respuesta esperada.
 *
 * Reconstruccion del camino (sin trabajo extra):
 *   La pila explicita YA ES el camino actual desde start hasta el
 *   nodo en el que estamos. Cuando currentIdx == endIdx, el
 *   contenido de stackNode[0..top] es exactamente la secuencia de
 *   celdas visitadas desde start hasta end: no hace falta ningun
 *   arreglo de padres ni reconstruccion hacia atras (a diferencia
 *   de BFS, que si lo necesita porque explora en orden de niveles,
 *   no en el orden final del camino).
 *
 * Complejidad:
 *   - Tiempo:  O(R * C) - cada celda se visita (se marca visited) una sola vez;
 *              cada frame de la pila hace a lo sumo 4 intentos de vecino.
 *   - Espacio: O(R * C) - arreglo visited + los dos arreglos que
 *              hacen de pila (nodo actual y siguiente direccion a probar).
 */
public final class DFSSolver {

    /** Valor centinela que indica que el destino es inalcanzable. */
    public static final int UNREACHABLE = -1;

    // Orden fijo y obligatorio de exploracion: arriba, abajo, izquierda, derecha.
    private static final int[] D_ROW = {-1, 1, 0, 0};
    private static final int[] D_COL = {0, 0, -1, 1};

    private DFSSolver() {
        // Utility class: no se instancia.
    }

    /**
     * Encuentra un camino desde (startRow, startCol) hasta (endRow, endCol)
     * explorando con DFS en el orden fijo arriba/abajo/izquierda/derecha.
     *
     * @return un Result con el numero de movimientos y el camino
     *         completo (celda por celda), o un Result no alcanzable
     *         si no existe camino (o si start/end contienen una bomba).
     */
    public static Result solve(Grid grid, int startRow, int startCol, int endRow, int endCol) {
        if (grid.isBomb(startRow, startCol) || grid.isBomb(endRow, endCol)) {
            return Result.unreachable();
        }
        if (startRow == endRow && startCol == endCol) {
            return Result.of(0, new int[][] { {startRow, startCol} });
        }

        int rows = grid.getRows();
        int cols = grid.getCols();
        int totalCells = rows * cols;

        boolean[] visited = new boolean[totalCells];

        // Pila explicita simulando los "frames" de una recursion:
        // stackNode[i]      -> indice de celda en el frame i
        // stackNextDir[i]   -> proxima direccion (0..3) a intentar en ese frame
        int[] stackNode = new int[totalCells];
        int[] stackNextDir = new int[totalCells];
        int top = 0;

        int startIdx = grid.toIndex(startRow, startCol);
        int endIdx = grid.toIndex(endRow, endCol);

        visited[startIdx] = true;
        stackNode[0] = startIdx;
        stackNextDir[0] = 0;

        while (top >= 0) {
            int currentIdx = stackNode[top];

            // El numero de movimientos hasta aqui es igual a la
            // profundidad actual de la pila (frame 0 = start = 0 movimientos).
            if (currentIdx == endIdx) {
                return Result.of(top, extractPath(stackNode, top, cols));
            }

            if (stackNextDir[top] < 4) {
                int dir = stackNextDir[top];
                stackNextDir[top] = dir + 1;

                int currentRow = currentIdx / cols;
                int currentCol = currentIdx % cols;
                int neighborRow = currentRow + D_ROW[dir];
                int neighborCol = currentCol + D_COL[dir];

                if (grid.isInside(neighborRow, neighborCol)
                        && !grid.isBomb(neighborRow, neighborCol)) {

                    int neighborIdx = grid.toIndex(neighborRow, neighborCol);

                    if (!visited[neighborIdx]) {
                        visited[neighborIdx] = true;
                        top++;
                        stackNode[top] = neighborIdx;
                        stackNextDir[top] = 0;
                    }
                }
                // Si el vecino no es valido o ya fue visitado, el bucle
                // vuelve a este mismo frame y probara la siguiente direccion.
            } else {
                // Se agotaron las 4 direcciones: backtracking.
                top--;
            }
        }

        // La pila se vacio sin encontrar el destino.
        return Result.unreachable();
    }

    /** Convierte los indices planos que quedaron en la pila (0..top) a pares (row, col), en orden start -> end. */
    private static int[][] extractPath(int[] stackNode, int top, int cols) {
        int[][] path = new int[top + 1][2];
        for (int i = 0; i <= top; i++) {
            path[i][0] = stackNode[i] / cols;
            path[i][1] = stackNode[i] % cols;
        }
        return path;
    }

    /**
     * Resultado de DFS: si el destino es alcanzable, el numero de
     * movimientos del camino encontrado y el camino completo (para
     * que la GUI lo resalte); si no, ninguno de los dos tiene sentido.
     */
    public static final class Result {
        private final int moves;
        private final int[][] path;

        private Result(int moves, int[][] path) {
            this.moves = moves;
            this.path = path;
        }

        static Result of(int moves, int[][] path) {
            return new Result(moves, path);
        }

        static Result unreachable() {
            return new Result(UNREACHABLE, new int[0][]);
        }

        public boolean isReachable() {
            return moves != UNREACHABLE;
        }

        public int getMoves() {
            return moves;
        }

        /** El camino completo, celda por celda, desde start hasta end (ambos incluidos). Vacio si es inalcanzable. */
        public int[][] getPath() {
            return path;
        }
    }
}
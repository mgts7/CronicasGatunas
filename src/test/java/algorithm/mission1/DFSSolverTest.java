package algorithm.mission1;

import model.grid.Grid;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DFSSolverTest {

    @Test
    @DisplayName("Ejemplo del enunciado (mismo grid que BFS): con orden fijo arriba/abajo/izquierda/derecha, DFS debe dar 32")
    void sampleFromStatement_returnsPathWithFixedOrder() {
        Grid grid = Mission1Test.sampleGridFromStatement();

        // Agregado .getMoves()
        int result = DFSSolver.solve(grid, 0, 0, 9, 9).getMoves();

        assertEquals(32, result);
    }

    @Test
    @DisplayName("DFS encuentra un camino valido, pero no necesariamente el mas corto (debe ser >= BFS)")
    void dfsResult_isNeverShorterThanBfs() {
        Grid grid = Mission1Test.sampleGridFromStatement();

        int bfs = BFSSolver.solve(grid, 0, 0, 9, 9).getMoves();
        // Agregado .getMoves()
        int dfs = DFSSolver.solve(grid, 0, 0, 9, 9).getMoves();

        assertTrue(dfs >= bfs, "DFS no puede encontrar un camino mas corto que el optimo de BFS");
    }

    @Test
    @DisplayName("Si start y end coinciden, la respuesta es 0")
    void sameStartAndEnd_returnsZero() {
        Grid grid = new Grid(5, 5);

        // Agregado .getMoves()
        int result = DFSSolver.solve(grid, 2, 2, 2, 2).getMoves();

        assertEquals(0, result);
    }

    @Test
    @DisplayName("Si la celda de inicio contiene una bomba, el destino es inalcanzable")
    void startIsBomb_returnsUnreachable() {
        Grid grid = new Grid(3, 3);
        grid.setBomb(0, 0);

        // Agregado .getMoves()
        int result = DFSSolver.solve(grid, 0, 0, 2, 2).getMoves();

        assertEquals(DFSSolver.UNREACHABLE, result);
    }

    @Test
    @DisplayName("Si la celda destino contiene una bomba, es inalcanzable")
    void endIsBomb_returnsUnreachable() {
        Grid grid = new Grid(3, 3);
        grid.setBomb(2, 2);

        // Agregado .getMoves()
        int result = DFSSolver.solve(grid, 0, 0, 2, 2).getMoves();

        assertEquals(DFSSolver.UNREACHABLE, result);
    }

    @Test
    @DisplayName("Si el destino queda completamente rodeado de bombas, es inalcanzable")
    void destinationSurroundedByBombs_returnsUnreachable() {
        Grid grid = new Grid(3, 3);
        grid.setBomb(1, 2);
        grid.setBomb(2, 1);

        // Agregado .getMoves()
        int result = DFSSolver.solve(grid, 0, 0, 2, 2).getMoves();

        assertEquals(DFSSolver.UNREACHABLE, result);
    }

    @Test
    @DisplayName("BFS y DFS deben coincidir en si el destino es alcanzable, aunque no en la distancia")
    void bfsAndDfsAgreeOnReachability() {
        Grid grid = Mission1Test.sampleGridFromStatement();

        // Usado .isReachable() de la clase Result en lugar de comparar con la constante UNREACHABLE directamente
        boolean bfsReachable = BFSSolver.solve(grid, 0, 0, 9, 9).isReachable();
        boolean dfsReachable = DFSSolver.solve(grid, 0, 0, 9, 9).isReachable();

        assertEquals(bfsReachable, dfsReachable);
    }

    @Test
    @DisplayName("Grid pequeno sin bombas: DFS debe encontrar el unico camino posible en linea recta")
    void singleRowGrid_findsOnlyPossiblePath() {
        // Grid de 1 fila x 5 columnas: el unico movimiento posible es hacia la derecha.
        Grid grid = new Grid(1, 5);

        // Agregado .getMoves()
        int result = DFSSolver.solve(grid, 0, 0, 0, 4).getMoves();

        assertEquals(4, result);
    }
}
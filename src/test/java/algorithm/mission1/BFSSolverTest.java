package algorithm.mission1;

import model.grid.Grid;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class BFSSolverTest {

    @Test
    @DisplayName("Ejemplo del enunciado (grid 10x10, 9 filas de bombas): BFS debe dar 18")
    void sampleFromStatement_returnsMinimumMoves() {
        Grid grid = Mission1Test.sampleGridFromStatement();

        int result = BFSSolver.solve(grid, 0, 0, 9, 9);

        assertEquals(18, result);
    }

    @Test
    @DisplayName("Si start y end coinciden, la respuesta es 0")
    void sameStartAndEnd_returnsZero() {
        Grid grid = new Grid(5, 5);

        int result = BFSSolver.solve(grid, 2, 2, 2, 2);

        assertEquals(0, result);
    }

    @Test
    @DisplayName("Si la celda de inicio contiene una bomba, el destino es inalcanzable")
    void startIsBomb_returnsUnreachable() {
        Grid grid = new Grid(3, 3);
        grid.setBomb(0, 0);

        int result = BFSSolver.solve(grid, 0, 0, 2, 2);

        assertEquals(BFSSolver.UNREACHABLE, result);
    }

    @Test
    @DisplayName("Si la celda destino contiene una bomba, es inalcanzable")
    void endIsBomb_returnsUnreachable() {
        Grid grid = new Grid(3, 3);
        grid.setBomb(2, 2);

        int result = BFSSolver.solve(grid, 0, 0, 2, 2);

        assertEquals(BFSSolver.UNREACHABLE, result);
    }

    @Test
    @DisplayName("Si el destino queda completamente rodeado de bombas, es inalcanzable")
    void destinationSurroundedByBombs_returnsUnreachable() {
        Grid grid = new Grid(3, 3);
        // (2,2) solo tiene dos vecinos validos dentro del grid: (1,2) y (2,1).
        // Al bloquear ambos, (2,2) queda aislado del resto del grid.
        grid.setBomb(1, 2);
        grid.setBomb(2, 1);

        int result = BFSSolver.solve(grid, 0, 0, 2, 2);

        assertEquals(BFSSolver.UNREACHABLE, result);
    }

    @Test
    @DisplayName("En un grid sin bombas, la distancia minima es la distancia Manhattan")
    void openGrid_returnsManhattanDistance() {
        Grid grid = new Grid(10, 10);

        int result = BFSSolver.solve(grid, 0, 0, 5, 5);

        assertEquals(10, result); // |5-0| + |5-0| = 10
    }
}
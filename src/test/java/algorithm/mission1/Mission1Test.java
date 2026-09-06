package algorithm.mission1;

import model.grid.Grid;

/**
 * Construye los grids de prueba usados por BFSSolverTest y DFSSolverTest,
 * para no duplicar la misma configuracion de bombas en ambas clases.
 */
final class Mission1Test {

    private Mission1Test() {
        // Utility class: no se instancia.
    }

    /**
     * Grid 10x10 exacto del ejemplo de la Mision 1 en el enunciado
     * (seccion 3, "Sample input"). Start = (0,0), End = (9,9).
     * Respuesta esperada: BFS 18, DFS 32.
     */
    static Grid sampleGridFromStatement() {
        Grid grid = new Grid(10, 10);

        grid.setBomb(0, 2);
        grid.setBomb(1, 2);
        grid.setBomb(2, 2);
        grid.setBomb(2, 9);
        grid.setBomb(3, 1);
        grid.setBomb(3, 7);
        grid.setBomb(5, 3);
        grid.setBomb(5, 6);
        grid.setBomb(5, 9);
        grid.setBomb(6, 0);
        grid.setBomb(6, 1);
        grid.setBomb(6, 2);
        grid.setBomb(6, 7);
        grid.setBomb(7, 0);
        grid.setBomb(7, 3);
        grid.setBomb(7, 8);
        grid.setBomb(8, 7);
        grid.setBomb(8, 9);
        grid.setBomb(9, 2);
        grid.setBomb(9, 3);
        grid.setBomb(9, 4);

        return grid;
    }
}
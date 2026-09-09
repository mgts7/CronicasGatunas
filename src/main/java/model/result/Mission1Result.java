package model.result;

import algorithm.mission1.BFSSolver;
import algorithm.mission1.DFSSolver;

/**
 * Resultado de un caso de prueba de Mission 1, listo para mostrarse
 * en la GUI o compararse en un test contra la salida esperada.
 *
 * No calcula nada: solo empaqueta lo que BFSSolver/DFSSolver ya
 * calcularon (incluyendo el camino completo, necesario para que la
 * GUI lo resalte sobre el grid), y sabe formatear la linea
 * "Case #k: ..." exacta.
 */
public final class Mission1Result {

    private final int caseNumber;
    private final boolean reachable;
    private final int bfsMoves;
    private final int dfsMoves;
    private final int[][] bfsPath;
    private final int[][] dfsPath;

    private Mission1Result(int caseNumber, boolean reachable, int bfsMoves, int dfsMoves,
                           int[][] bfsPath, int[][] dfsPath) {
        this.caseNumber = caseNumber;
        this.reachable = reachable;
        this.bfsMoves = bfsMoves;
        this.dfsMoves = dfsMoves;
        this.bfsPath = bfsPath;
        this.dfsPath = dfsPath;
    }

    /**
     * Construye el resultado a partir de lo que devolvieron
     * BFSSolver.solve(...) y DFSSolver.solve(...) para el mismo
     * caso. Ambos comparten el mismo grid subyacente, asi que si
     * BFS dice inalcanzable, DFS tambien lo dira (ver
     * bfsAndDfsAgreeOnReachability en DFSSolverTest).
     */
    public static Mission1Result of(int caseNumber, BFSSolver.Result bfsResult, DFSSolver.Result dfsResult) {
        return new Mission1Result(caseNumber, bfsResult.isReachable(),
                bfsResult.getMoves(), dfsResult.getMoves(),
                bfsResult.getPath(), dfsResult.getPath());
    }

    public int getCaseNumber() {
        return caseNumber;
    }

    public boolean isReachable() {
        return reachable;
    }

    public int getBfsMoves() {
        return bfsMoves;
    }

    public int getDfsMoves() {
        return dfsMoves;
    }

    /** El camino de BFS, celda por celda, para resaltarlo en el dibujo del grid. */
    public int[][] getBfsPath() {
        return bfsPath;
    }

    /** El camino de DFS, celda por celda, para resaltarlo en el dibujo del grid. */
    public int[][] getDfsPath() {
        return dfsPath;
    }

    /**
     * Formatea la linea de salida EXACTA que exige el enunciado
     * (seccion 2.2: "The special messages must be reproduced
     * exactly, character by character, in plain ASCII").
     */
    public String toOutputLine() {
        if (!reachable) {
            return "Case #" + caseNumber + ": Nina is unreachable";
        }
        return "Case #" + caseNumber + ": BFS " + bfsMoves + " DFS " + dfsMoves;
    }
}
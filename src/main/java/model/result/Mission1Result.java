package model.result;

import algorithm.mission1.BFSSolver;

/**
 * Resultado de un caso de prueba de Mission 1, listo para mostrarse
 * en la GUI o compararse en un test contra la salida esperada.
 *
 * No calcula nada: solo empaqueta lo que BFSSolver/DFSSolver ya
 * calcularon, y sabe formatear la linea "Case #k: ..." exacta.
 */
public final class Mission1Result {

    private final int caseNumber;
    private final boolean reachable;
    private final int bfsMoves;
    private final int dfsMoves;

    private Mission1Result(int caseNumber, boolean reachable, int bfsMoves, int dfsMoves) {
        this.caseNumber = caseNumber;
        this.reachable = reachable;
        this.bfsMoves = bfsMoves;
        this.dfsMoves = dfsMoves;
    }

    /**
     * Construye el resultado a partir de lo que devolvieron
     * BFSSolver.solve(...) y DFSSolver.solve(...) para el mismo
     * caso. Ambos comparten el mismo grafo subyacente, asi que si
     * BFS dice inalcanzable, DFS tambien lo dira (ver
     * bfsAndDfsAgreeOnReachability en DFSSolverTest).
     */
    public static Mission1Result of(int caseNumber, int bfsMoves, int dfsMoves) {
        boolean reachable = bfsMoves != BFSSolver.UNREACHABLE;
        return new Mission1Result(caseNumber, reachable, bfsMoves, dfsMoves);
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
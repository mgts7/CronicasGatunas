package model.result;

import algorithm.mission2.DijkstraSolver;

/**
 * Resultado de un caso de prueba de Mission 2, listo para mostrarse
 * en la GUI o compararse en un test contra la salida esperada.
 */
public final class Mission2Result {

    private final int caseNumber;
    private final boolean reachable;
    private final long cost;
    private final int[] path;

    private Mission2Result(int caseNumber, boolean reachable, long cost, int[] path) {
        this.caseNumber = caseNumber;
        this.reachable = reachable;
        this.cost = cost;
        this.path = path;
    }

    /** Construye el resultado a partir de lo que devolvio DijkstraSolver.solve(...). */
    public static Mission2Result of(int caseNumber, DijkstraSolver.Result result) {
        return new Mission2Result(caseNumber, result.isReachable(), result.getCost(), result.getPath());
    }

    public int getCaseNumber() {
        return caseNumber;
    }

    public boolean isReachable() {
        return reachable;
    }

    public long getCost() {
        return cost;
    }

    /** La secuencia de nodos del camino, para resaltarla en el dibujo de la red. */
    public int[] getPath() {
        return path;
    }

    /**
     * Formatea la linea de salida EXACTA que exige el enunciado
     * (seccion 4: mensaje especial "Nina is very sad" si D es
     * inalcanzable desde S).
     */
    public String toOutputLine() {
        if (!reachable) {
            return "Case #" + caseNumber + ": Nina is very sad";
        }
        return "Case #" + caseNumber + ": " + cost;
    }
}
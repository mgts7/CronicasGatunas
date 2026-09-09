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

    private Mission2Result(int caseNumber, boolean reachable, long cost) {
        this.caseNumber = caseNumber;
        this.reachable = reachable;
        this.cost = cost;
    }

    /** Construye el resultado a partir de lo que devolvio DijkstraSolver.solve(...). */
    public static Mission2Result of(int caseNumber, long cost) {
        boolean reachable = cost != DijkstraSolver.UNREACHABLE;
        return new Mission2Result(caseNumber, reachable, cost);
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
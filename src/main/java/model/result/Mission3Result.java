package model.result;

import algorithm.mission3.BellmanFordSolver;
import algorithm.mission3.FloydWarshallSolver;

/**
 * Resultado de un caso de prueba de Mission 3.
 *
 * A diferencia de Mission1Result/Mission2Result (que solo empaquetan
 * el resultado de un unico solver), esta clase es tambien el punto
 * donde se combinan FloydWarshallSolver y BellmanFordSolver: aplica
 * la precedencia de 3 niveles que exige la seccion 5 del enunciado
 * y verifica que ambos algoritmos coincidan (cross-check
 * obligatorio: "the GUI must report a mismatch if the two ever
 * disagree").
 *
 * Se hace aqui, y no en la GUI, para que esta logica de combinacion
 * sea testeable con JUnit sin abrir ninguna ventana (requisito 7.2).
 */
public final class Mission3Result {

    /** Que tipo de respuesta corresponde, segun la precedencia del enunciado. */
    public enum Classification {
        LIMON_BLOCKED,
        INFINITE_CHURUN,
        FINITE
    }

    private final int caseNumber;
    private final long[][] matrix;
    private final int source;
    private final int destination;
    private final Classification classification;
    private final long value;
    private final boolean crossCheckMismatch;

    private Mission3Result(int caseNumber, long[][] matrix, int source, int destination,
                           Classification classification, long value, boolean crossCheckMismatch) {
        this.caseNumber = caseNumber;
        this.matrix = matrix;
        this.source = source;
        this.destination = destination;
        this.classification = classification;
        this.value = value;
        this.crossCheckMismatch = crossCheckMismatch;
    }

    /**
     * Combina los resultados de FloydWarshallSolver.solve(...) y
     * BellmanFordSolver.solve(...) para un mismo caso (S, D),
     * aplicando la precedencia exacta del enunciado, en este orden:
     *   1. "Limon blocked the way"  si D no es alcanzable desde S.
     *   2. "Infinite churun!"       si el par (S, D) es unbounded.
     *   3. El valor maximo de churun, en cualquier otro caso.
     *
     * La matriz de Floyd-Warshall se usa como fuente de verdad para
     * la clasificacion (es la que exige mostrarse completa en la
     * GUI); si Bellman-Ford llega a una conclusion distinta para D,
     * queda registrado en isCrossCheckMismatch() sin alterar el
     * formato de toOutputLine().
     */
    public static Mission3Result of(int caseNumber,
                                    FloydWarshallSolver.Result floydWarshall,
                                    BellmanFordSolver.Result bellmanFord,
                                    int source, int destination) {

        boolean fwReachable = floydWarshall.hasRoute(source, destination);
        boolean bfReachable = bellmanFord.hasRoute(destination);
        boolean reachabilityMismatch = fwReachable != bfReachable;

        if (!fwReachable) {
            return new Mission3Result(caseNumber, floydWarshall.getMatrix(), source, destination,
                    Classification.LIMON_BLOCKED, 0L, reachabilityMismatch);
        }

        boolean fwUnbounded = floydWarshall.isUnbounded(source, destination);
        boolean bfUnbounded = bellmanFord.isUnbounded(destination);
        boolean unboundedMismatch = fwUnbounded != bfUnbounded;

        if (fwUnbounded) {
            return new Mission3Result(caseNumber, floydWarshall.getMatrix(), source, destination,
                    Classification.INFINITE_CHURUN, 0L, reachabilityMismatch || unboundedMismatch);
        }

        long fwValue = floydWarshall.getMaxChurun(source, destination);
        long bfValue = bellmanFord.getMaxChurun(destination);
        boolean valueMismatch = fwValue != bfValue;

        return new Mission3Result(caseNumber, floydWarshall.getMatrix(), source, destination,
                Classification.FINITE, fwValue,
                reachabilityMismatch || unboundedMismatch || valueMismatch);
    }

    public int getCaseNumber() {
        return caseNumber;
    }

    /** La matriz N x N completa de Floyd-Warshall, para el panel scrollable que exige la seccion 2.3. */
    public long[][] getMatrix() {
        return matrix;
    }

    public int getSource() {
        return source;
    }

    public int getDestination() {
        return destination;
    }

    public Classification getClassification() {
        return classification;
    }

    /** Solo tiene sentido cuando getClassification() == FINITE. */
    public long getValue() {
        return value;
    }

    /**
     * true si Floyd-Warshall y Bellman-Ford llegaron a conclusiones
     * distintas para este caso. En un proyecto correcto nunca deberia
     * ocurrir; la GUI debe mostrar una advertencia visible si pasa
     * (util tambien para el "mismatch warning" que pide probar el
     * checklist final de la seccion 10).
     */
    public boolean isCrossCheckMismatch() {
        return crossCheckMismatch;
    }

    /** Formatea la linea de salida EXACTA que exige el enunciado. */
    public String toOutputLine() {
        switch (classification) {
            case LIMON_BLOCKED:
                return "Case #" + caseNumber + ": Limon blocked the way";
            case INFINITE_CHURUN:
                return "Case #" + caseNumber + ": Infinite churun!";
            default:
                return "Case #" + caseNumber + ": " + value;
        }
    }
}
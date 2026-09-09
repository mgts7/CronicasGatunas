package io;

import errorhandling.InputValidationException;
import model.graph.Graph;

import java.util.ArrayList;
import java.util.List;

/**
 * ============================================================
 * Mission3Parser
 * ============================================================
 *
 * Convierte el texto pegado en la GUI (formato de la seccion 5 del
 * enunciado) en una lista de casos de prueba ya listos para
 * resolver con FloydWarshallSolver y BellmanFordSolver.
 *
 * Diferencias clave frente a Mission2Parser:
 *   - Las conexiones son DIRIGIDAS (addDirectedEdge, no
 *     addUndirectedEdge): un pasaje A->B no implica B->A.
 *   - Los pesos pueden ser negativos (-1000 a 1000), asi que la
 *     validacion de rango usa limites con signo.
 *   - N es mucho mas chico (hasta 100) pero M puede llegar a 5000.
 */
public final class Mission3Parser {

    private static final int MIN_N = 1;
    private static final int MAX_N = 100;
    private static final int MAX_M = 5000;
    private static final long MIN_WEIGHT = -1000;
    private static final long MAX_WEIGHT = 1000;

    private Mission3Parser() {
        // Utility class: no se instancia.
    }

    /**
     * Parsea todos los casos de prueba del texto de entrada.
     * La primera linea indica T, el numero total de casos.
     */
    public static List<TestCase> parse(String rawInput) {
        InputTokenizer tokenizer = new InputTokenizer(rawInput);

        int testCaseCount = tokenizer.nextInt();
        validateNonNegative(testCaseCount, "numero de casos de prueba (T)");

        List<TestCase> testCases = new ArrayList<>();

        for (int t = 0; t < testCaseCount; t++) {
            int n = tokenizer.nextInt();
            int m = tokenizer.nextInt();
            int source = tokenizer.nextInt();
            int destination = tokenizer.nextInt();

            validateRange(n, MIN_N, MAX_N, "numero de nodos (N)");
            validateRange(m, 0, MAX_M, "numero de pasajes (M)");
            validateRange(source, 0, n - 1, "nodo de inicio (S)");
            validateRange(destination, 0, n - 1, "nodo de destino (D)");

            Graph graph = new Graph(n);

            for (int i = 0; i < m; i++) {
                int a = tokenizer.nextInt();
                int b = tokenizer.nextInt();
                long weight = tokenizer.nextLong();

                validateRange(a, 0, n - 1, "nodo A de un pasaje");
                validateRange(b, 0, n - 1, "nodo B de un pasaje");
                validateRangeLong(weight, MIN_WEIGHT, MAX_WEIGHT, "churun de un pasaje (W)");

                // "Repeated passages between the same ordered pair may
                // appear; treat each one as a separate edge" (seccion 5):
                // se agregan todos, sin deduplicar. FloydWarshallSolver ya
                // se encarga de quedarse con el de mayor churun al inicializar
                // su matriz, y BellmanFordSolver no se ve afectado por
                // aristas paralelas (solo relaja cada una en su turno).
                graph.addDirectedEdge(a, b, weight);
            }

            testCases.add(new TestCase(graph, n, source, destination));
        }

        return testCases;
    }

    private static void validateNonNegative(int value, String description) {
        if (value < 0) {
            throw new InputValidationException(
                    "Valor invalido para " + description + ": " + value + " (no puede ser negativo).");
        }
    }

    private static void validateRange(int value, int min, int max, String description) {
        if (value < min || value > max) {
            throw new InputValidationException(
                    "Valor fuera de rango para " + description + ": " + value
                            + " (se esperaba entre " + min + " y " + max + ").");
        }
    }

    private static void validateRangeLong(long value, long min, long max, String description) {
        if (value < min || value > max) {
            throw new InputValidationException(
                    "Valor fuera de rango para " + description + ": " + value
                            + " (se esperaba entre " + min + " y " + max + ").");
        }
    }

    /**
     * Un caso de prueba ya parseado: el grafo dirigido con sus
     * pasajes cargados, el numero de nodos, y el nodo de origen/destino.
     *
     * Se guarda 'n' explicitamente (ademas de graph.getNumNodes(),
     * que es equivalente) para que quede claro en el sitio de uso
     * que FloydWarshallSolver.solve(graph, n) necesita ese valor
     * por separado, sin tener que ir a buscarlo dentro de graph.
     */
    public static final class TestCase {
        private final Graph graph;
        private final int n;
        private final int source;
        private final int destination;

        public TestCase(Graph graph, int n, int source, int destination) {
            this.graph = graph;
            this.n = n;
            this.source = source;
            this.destination = destination;
        }

        public Graph getGraph() {
            return graph;
        }

        public int getN() {
            return n;
        }

        public int getSource() {
            return source;
        }

        public int getDestination() {
            return destination;
        }
    }
}
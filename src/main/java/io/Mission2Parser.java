package io;

import errorhandling.InputValidationException;
import model.graph.Graph;

import java.util.ArrayList;
import java.util.List;

/**
 * ============================================================
 * Mission2Parser
 * ============================================================
 *
 * Convierte el texto pegado en la GUI (formato de la seccion 4 del
 * enunciado) en una lista de casos de prueba ya listos para
 * resolver con DijkstraSolver.
 *
 * Al igual que Mission1Parser, este parser NO calcula el resultado:
 * solo construye los datos de entrada (Graph + nodo origen/destino).
 */
public final class Mission2Parser {

    private static final int MIN_N = 1;
    private static final int MAX_N = 10_000;
    private static final int MAX_C = 100_000;
    private static final long MIN_WEIGHT = 0;
    private static final long MAX_WEIGHT = 1_000_000;

    private Mission2Parser() {
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
            int c = tokenizer.nextInt();
            int source = tokenizer.nextInt();
            int destination = tokenizer.nextInt();

            validateRange(n, MIN_N, MAX_N, "numero de nodos (N)");
            validateRange(c, 0, MAX_C, "numero de conexiones (C)");
            validateRange(source, 0, n - 1, "nodo de inicio (S)");
            validateRange(destination, 0, n - 1, "nodo de destino (D)");

            Graph graph = new Graph(n);

            for (int i = 0; i < c; i++) {
                int a = tokenizer.nextInt();
                int b = tokenizer.nextInt();
                long weight = tokenizer.nextLong();

                validateRange(a, 0, n - 1, "nodo A de una conexion");
                validateRange(b, 0, n - 1, "nodo B de una conexion");
                validateRangeLong(weight, MIN_WEIGHT, MAX_WEIGHT, "peso de una conexion (W)");

                // Conexiones repetidas y self-loops son validos segun el
                // enunciado: se agregan todas, sin deduplicar. Graph y
                // DijkstraSolver los manejan sin fallar (ver sus javadocs).
                graph.addUndirectedEdge(a, b, weight);
            }

            testCases.add(new TestCase(graph, source, destination));
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
     * Un caso de prueba ya parseado: el grafo con sus conexiones
     * cargadas, mas el nodo de origen y de destino.
     */
    public static final class TestCase {
        private final Graph graph;
        private final int source;
        private final int destination;

        public TestCase(Graph graph, int source, int destination) {
            this.graph = graph;
            this.source = source;
            this.destination = destination;
        }

        public Graph getGraph() {
            return graph;
        }

        public int getSource() {
            return source;
        }

        public int getDestination() {
            return destination;
        }
    }
}
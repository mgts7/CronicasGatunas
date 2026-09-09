package io;

import algorithm.mission4.KruskalSolver.Cable;
import errorhandling.InputValidationException;

import java.util.ArrayList;
import java.util.List;

/**
 * ============================================================
 * Mission4Parser
 * ============================================================
 *
 * Convierte el texto pegado en la GUI (formato de la seccion 6 del
 * enunciado) en una lista de casos de prueba ya listos para
 * resolver con KruskalSolver.
 *
 * Conversion de indices (1-indexado -> 0-indexado):
 *   El enunciado numera las intersecciones de 1 a N, a diferencia
 *   de las demas misiones (Grid y Graph son 0-indexados). Para que
 *   KruskalSolver / UnionFind se mantengan consistentes con el resto
 *   del proyecto, esta es la UNICA capa donde ocurre la conversion:
 *   cada intersección leida se le resta 1 antes de crear el Cable.
 *   Si algun dia el formato de entrada cambiara a 0-indexado, solo
 *   habria que tocar este parser.
 */
public final class Mission4Parser {

    private static final int MIN_N = 1;
    private static final int MAX_N = 10_000;
    private static final int MAX_C = 100_000;
    private static final long MIN_COST = 0;
    private static final long MAX_COST = 1_000_000;

    private Mission4Parser() {
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
            validateRange(n, MIN_N, MAX_N, "numero de intersecciones (N)");

            int c = tokenizer.nextInt();
            validateRange(c, 0, MAX_C, "numero de cables disponibles (C)");

            List<Cable> cables = new ArrayList<>(c);

            for (int i = 0; i < c; i++) {
                int start = tokenizer.nextInt();
                int end = tokenizer.nextInt();
                long cost = tokenizer.nextLong();

                validateRange(start, 1, n, "interseccion inicial de un cable");
                validateRange(end, 1, n, "interseccion final de un cable");
                validateRangeLong(cost, MIN_COST, MAX_COST, "costo de un cable");

                // Conversion a 0-indexado, unica vez, aqui.
                // Cables duplicados y self-loops son validos segun el
                // enunciado: se agregan todos tal cual, sin deduplicar.
                // KruskalSolver/UnionFind los manejan sin fallar (ver sus javadocs).
                cables.add(new Cable(start - 1, end - 1, cost));
            }

            testCases.add(new TestCase(n, cables));
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
     * Un caso de prueba ya parseado: el numero de intersecciones y
     * la lista de cables disponibles (ya en 0-indexado), listos
     * para pasarle directamente a KruskalSolver.solve(n, cables).
     */
    public static final class TestCase {
        private final int n;
        private final List<Cable> cables;

        public TestCase(int n, List<Cable> cables) {
            this.n = n;
            this.cables = cables;
        }

        public int getN() {
            return n;
        }

        public List<Cable> getCables() {
            return cables;
        }
    }
}
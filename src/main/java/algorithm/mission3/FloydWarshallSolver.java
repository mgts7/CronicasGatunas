package algorithm.mission3;

import model.graph.Edge;
import model.graph.Graph;

import java.util.Arrays;

/**
 * ============================================================
 * FLOYD-WARSHALL (variante de MAXIMIZACION) - Mision 3
 * ============================================================
 *
 * Por que Floyd-Warshall es la eleccion correcta aqui:
 *   El enunciado exige mostrar en la GUI la matriz N x N completa
 *   de churun maximo entre TODOS los pares de nodos (no solo S->D).
 *   Floyd-Warshall calcula exactamente eso en una sola pasada.
 *
 * Sobre "walk" vs "simple path":
 *   La recurrencia d[i][j] = max(d[i][j], d[i][k] + d[k][j]) no
 *   prohibe revisitar nodos: por construccion calcula el maximo
 *   sobre CAMINATAS (walks), no caminos simples. Buscar la caminata
 *   simple mas larga seria NP-duro; maximizar sobre caminatas es
 *   exactamente lo que esta recurrencia resuelve en O(N^3).
 *
 * Deteccion de "Infinite churun!" (ciclos de ganancia positiva):
 *   Al terminar el triple loop, d[k][k] > 0 significa que existe
 *   una caminata que sale de k y regresa a k con ganancia neta
 *   positiva (un ciclo rentable). Si ademas k es alcanzable desde i
 *   y desde k se puede llegar a j, entonces (i,j) es "unbounded":
 *   se puede dar vueltas al ciclo tantas veces como se quiera antes
 *   de continuar hacia j, acumulando churun sin limite.
 *
 * Complejidad:
 *   - Tiempo:  O(N^3) - triple loop sobre k, i, j.
 *   - Espacio: O(N^2) - la matriz de distancias y la de unbounded.
 *   Con N hasta 100 (limite del enunciado), N^3 = 10^6, muy rapido.
 */
public final class FloydWarshallSolver {

    /** Valor centinela: no existe ninguna caminata de i a j. */
    public static final long NO_ROUTE = Long.MIN_VALUE;

    private FloydWarshallSolver() {
        // Utility class: no se instancia.
    }

    public static Result solve(Graph graph, int n) {
        long[][] dist = new long[n][n];
        for (long[] row : dist) {
            Arrays.fill(row, NO_ROUTE);
        }
        for (int i = 0; i < n; i++) {
            dist[i][i] = 0L; // caminata vacia: quedarse en el mismo nodo cuesta 0
        }

        // Inicializacion con las aristas directas. "Keep the maximum
        // when an ordered pair appears more than once" (seccion 5):
        // si hay pasajes duplicados A->B, nos quedamos con el de mayor churun.
        for (int u = 0; u < n; u++) {
            for (Edge edge : graph.getNeighbors(u)) {
                int v = edge.getTo();
                long w = edge.getWeight();
                if (w > dist[u][v]) {
                    dist[u][v] = w;
                }
            }
        }

        // Triple loop clasico de Floyd-Warshall, adaptado a maximizacion.
        // Nunca se hace aritmetica sobre el centinela NO_ROUTE: se
        // verifica explicitamente que ambos tramos existan antes de sumar.
        for (int k = 0; k < n; k++) {
            for (int i = 0; i < n; i++) {
                if (dist[i][k] == NO_ROUTE) {
                    continue;
                }
                for (int j = 0; j < n; j++) {
                    if (dist[k][j] == NO_ROUTE) {
                        continue;
                    }
                    long candidate = dist[i][k] + dist[k][j];
                    if (candidate > dist[i][j]) {
                        dist[i][j] = candidate;
                    }
                }
            }
        }

        // Pasada extra para marcar los pares "unbounded" (churun infinito).
        boolean[][] unbounded = new boolean[n][n];
        for (int k = 0; k < n; k++) {
            if (dist[k][k] <= 0) {
                continue; // no hay ciclo de ganancia positiva pasando por k
            }
            for (int i = 0; i < n; i++) {
                if (dist[i][k] == NO_ROUTE) {
                    continue;
                }
                for (int j = 0; j < n; j++) {
                    if (dist[k][j] == NO_ROUTE) {
                        continue;
                    }
                    unbounded[i][j] = true;
                }
            }
        }

        return new Result(dist, unbounded);
    }

    /**
     * Resultado completo: la matriz N x N de churun maximo, y la
     * matriz paralela de que pares son "unbounded" (churun infinito).
     */
    public static final class Result {
        private final long[][] maxChurun;
        private final boolean[][] unbounded;

        public Result(long[][] maxChurun, boolean[][] unbounded) {
            this.maxChurun = maxChurun;
            this.unbounded = unbounded;
        }

        public long getMaxChurun(int i, int j) {
            return maxChurun[i][j];
        }

        public boolean isUnbounded(int i, int j) {
            return unbounded[i][j];
        }

        public boolean hasRoute(int i, int j) {
            return maxChurun[i][j] != NO_ROUTE;
        }

        /** La matriz completa, para que la GUI la muestre en el panel scrollable. */
        public long[][] getMatrix() {
            return maxChurun;
        }
    }
}
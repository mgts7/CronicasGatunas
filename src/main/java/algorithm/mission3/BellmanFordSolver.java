package algorithm.mission3;

import model.graph.Edge;
import model.graph.Graph;

import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.Deque;

/**
 * ============================================================
 * BELLMAN-FORD (variante de MAXIMIZACION) - Mision 3
 * ============================================================
 *
 * Por que Bellman-Ford es necesario aqui (ademas de Floyd-Warshall):
 *   Bellman-Ford es quien detecta los ciclos de ganancia positiva
 *   de forma directa: relaja todas las aristas N-1 veces y, si en
 *   una ronda extra algun nodo TODAVIA mejora, ese nodo pertenece a
 *   (o es alimentado por) un ciclo rentable. El enunciado exige
 *   implementar y ejercitar ambos algoritmos, y que sus resultados
 *   para el nodo D coincidan (cross-check).
 *
 * Propagacion del marcado "unbounded":
 *   Un nodo marcado directamente en la ronda extra es fuente de
 *   churun infinito. Pero cualquier nodo alcanzable DESDE ese nodo
 *   tambien es unbounded (se puede dar vueltas al ciclo rentable
 *   tantas veces como se quiera antes de continuar el viaje). Por
 *   eso se propaga el marcado con una busqueda (aqui BFS) siguiendo
 *   las aristas hacia adelante desde cada nodo marcado.
 *
 * Por que D es unbounded solo si quedo marcado por la propagacion:
 *   Que EXISTA un ciclo positivo en el grafo no basta: tiene que
 *   ser alcanzable desde S (por eso solo se marcan nodos con
 *   dist[u] != NO_ROUTE, es decir, ya alcanzados desde S) y el
 *   ciclo debe poder llegar hasta D (por eso se propaga hacia
 *   adelante y se pregunta si D quedo marcado).
 *
 * Complejidad:
 *   - Tiempo:  O(N * M) - hasta N-1 rondas de relajacion, cada una
 *              recorre las M aristas; mas O(N + M) para la
 *              propagacion final con BFS.
 *   - Espacio: O(N + M) - arreglo de distancias, arreglo de
 *              marcados y la cola de la propagacion.
 */
public final class BellmanFordSolver {

    /** Valor centinela: el nodo no ha sido alcanzado desde el origen. */
    public static final long NO_ROUTE = Long.MIN_VALUE;

    private BellmanFordSolver() {
        // Utility class: no se instancia.
    }

    public static Result solve(Graph graph, int n, int source) {
        long[] dist = new long[n];
        Arrays.fill(dist, NO_ROUTE);
        dist[source] = 0L;

        // Relajar todas las aristas N-1 veces, maximizando.
        // Nunca se hace aritmetica sobre el centinela: solo se
        // relaja a partir de un nodo u con dist[u] != NO_ROUTE.
        for (int iteration = 0; iteration < n - 1; iteration++) {
            boolean changedInThisRound = false;

            for (int u = 0; u < n; u++) {
                if (dist[u] == NO_ROUTE) {
                    continue;
                }
                for (Edge edge : graph.getNeighbors(u)) {
                    long candidate = dist[u] + edge.getWeight();
                    if (candidate > dist[edge.getTo()]) {
                        dist[edge.getTo()] = candidate;
                        changedInThisRound = true;
                    }
                }
            }

            if (!changedInThisRound) {
                break; // ya convergio, no hace falta seguir iterando
            }
        }

        // Ronda extra: cualquier nodo que TODAVIA mejora pertenece a,
        // o es alimentado por, un ciclo de ganancia positiva.
        boolean[] directlyMarked = new boolean[n];
        for (int u = 0; u < n; u++) {
            if (dist[u] == NO_ROUTE) {
                continue;
            }
            for (Edge edge : graph.getNeighbors(u)) {
                long candidate = dist[u] + edge.getWeight();
                if (candidate > dist[edge.getTo()]) {
                    directlyMarked[edge.getTo()] = true;
                }
            }
        }

        // Propagar "unbounded" a todo nodo alcanzable (hacia adelante)
        // desde cualquiera de los nodos marcados directamente.
        boolean[] unbounded = new boolean[n];
        Deque<Integer> queue = new ArrayDeque<>();
        for (int u = 0; u < n; u++) {
            if (directlyMarked[u]) {
                unbounded[u] = true;
                queue.add(u);
            }
        }
        while (!queue.isEmpty()) {
            int u = queue.poll();
            for (Edge edge : graph.getNeighbors(u)) {
                int v = edge.getTo();
                if (!unbounded[v]) {
                    unbounded[v] = true;
                    queue.add(v);
                }
            }
        }

        return new Result(dist, unbounded);
    }

    /**
     * Resultado desde un unico origen: el churun maximo hacia cada
     * nodo, y cuales nodos son "unbounded" (churun infinito).
     */
    public static final class Result {
        private final long[] maxChurun;
        private final boolean[] unbounded;

        public Result(long[] maxChurun, boolean[] unbounded) {
            this.maxChurun = maxChurun;
            this.unbounded = unbounded;
        }

        public long getMaxChurun(int node) {
            return maxChurun[node];
        }

        public boolean isUnbounded(int node) {
            return unbounded[node];
        }

        public boolean hasRoute(int node) {
            return maxChurun[node] != NO_ROUTE;
        }
    }
}
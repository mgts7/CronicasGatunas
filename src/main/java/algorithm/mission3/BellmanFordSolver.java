package algorithm.mission3;

import model.graph.Edge;
import model.graph.Graph;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Deque;
import java.util.List;

/**
 * ============================================================
 * BELLMAN-FORD (variante de MAXIMIZACION) - Mision 3
 * ============================================================
 *
 * Por que Bellman-Ford es necesario aqui (ademas de Floyd-Warshall):
 *   Bellman-Ford es quien detecta los ciclos de ganancia positiva
 *   de forma directa: relaja todas las aristas N-1 veces y, si en
 *   una ronda extra algun nodo TODAVIA mejora, ese nodo pertenece a
 *   (o es alimentado por) un ciclo rentable.
 *
 * Reconstruccion visual (seccion 5, punto 3: "The drawing must show
 * the graph with the route achieving the maximum highlighted. When
 * the answer is Infinite churun!, highlight the cycle responsible
 * instead of a route."):
 *   - Caso finito: se seguye la cadena de padres (parent[]) desde
 *     el destino hasta el origen. Esto es seguro (no da vueltas
 *     infinitas) porque, si no hay un ciclo de ganancia positiva
 *     que sea alcanzable desde S Y pueda llegar a D, la caminata
 *     optima siempre es equivalente a un camino SIMPLE (revisitar
 *     un nodo sin una ganancia neta positiva en el ciclo nunca
 *     mejora el resultado, asi que existe un optimo sin repeticiones).
 *   - Caso "Infinite churun!": se usa la tecnica clasica de Bellman-
 *     Ford para extraer un ciclo: se parte de un nodo que todavia
 *     mejora en la ronda extra, se retrocede N veces por parent[]
 *     (esto garantiza, por el principio del palomar, terminar
 *     DENTRO del ciclo), y desde ahi se seyue la cadena hacia atras
 *     hasta repetir un nodo, delimitando el ciclo completo.
 *
 * Complejidad:
 *   - Tiempo:  O(N * M) para las rondas de relajacion, mas O(N)
 *              adicional para la extraccion del ciclo si aplica.
 *   - Espacio: O(N + M).
 */
public final class BellmanFordSolver {

    /** Valor centinela: el nodo no ha sido alcanzado desde el origen. */
    public static final long NO_ROUTE = Long.MIN_VALUE;

    private BellmanFordSolver() {
        // Utility class: no se instancia.
    }

    public static Result solve(Graph graph, int n, int source) {
        long[] dist = new long[n];
        int[] parent = new int[n];
        Arrays.fill(dist, NO_ROUTE);
        Arrays.fill(parent, -1);
        dist[source] = 0L;

        // Relajar todas las aristas N-1 veces, maximizando.
        for (int iteration = 0; iteration < n - 1; iteration++) {
            boolean changedInThisRound = relaxAllEdges(graph, n, dist, parent);
            if (!changedInThisRound) {
                break; // ya convergio
            }
        }

        // Ronda extra: cualquier nodo que TODAVIA mejora pertenece a,
        // o es alimentado por, un ciclo de ganancia positiva.
        boolean[] directlyMarked = new boolean[n];
        int firstMarkedNode = -1;
        for (int u = 0; u < n; u++) {
            if (dist[u] == NO_ROUTE) {
                continue;
            }
            for (Edge edge : graph.getNeighbors(u)) {
                long candidate = dist[u] + edge.getWeight();
                if (candidate > dist[edge.getTo()]) {
                    directlyMarked[edge.getTo()] = true;
                    if (firstMarkedNode == -1) {
                        firstMarkedNode = edge.getTo();
                    }
                }
            }
        }

        // Propagar "unbounded" a todo nodo alcanzable (hacia adelante)
        // desde cualquiera de los nodos marcados directamente.
        boolean[] unbounded = propagateUnbounded(graph, n, directlyMarked);

        int[] cycle = new int[0];
        if (firstMarkedNode != -1) {
            // Se relajan N rondas MAS, arrancando desde el nodo marcado,
            // para que su cadena de padres quede empujada completamente
            // DENTRO del ciclo (tecnica clasica de extraccion de ciclo).
            for (int i = 0; i < n; i++) {
                relaxAllEdges(graph, n, dist, parent);
            }
            cycle = extractCycle(parent, firstMarkedNode, n);
        }

        return new Result(dist, unbounded, parent, cycle);
    }

    /** Una pasada de relajacion sobre todas las aristas del grafo. Devuelve true si algo mejoro. */
    private static boolean relaxAllEdges(Graph graph, int n, long[] dist, int[] parent) {
        boolean changed = false;
        for (int u = 0; u < n; u++) {
            if (dist[u] == NO_ROUTE) {
                continue;
            }
            for (Edge edge : graph.getNeighbors(u)) {
                long candidate = dist[u] + edge.getWeight();
                if (candidate > dist[edge.getTo()]) {
                    dist[edge.getTo()] = candidate;
                    parent[edge.getTo()] = u;
                    changed = true;
                }
            }
        }
        return changed;
    }

    private static boolean[] propagateUnbounded(Graph graph, int n, boolean[] directlyMarked) {
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
        return unbounded;
    }

    /**
     * Extrae el ciclo de ganancia positiva a partir de un nodo que
     * todavia mejoraba en la ronda extra. Retrocede n veces por
     * parent[] (garantiza caer dentro del ciclo) y luego recorre
     * hacia atras hasta repetir un nodo.
     *
     * IMPORTANTE sobre el orden del arreglo devuelto:
     *   Al recorrer parent[] hacia atras, cada nodo agregado es el
     *   padre del anterior; es decir, el arco REAL del grafo va de
     *   parent[x] -> x, o sea de la posicion (i+1) a la posicion (i)
     *   de la lista tal como se construye. Ese es el orden INVERSO
     *   al que espera GraphCanvas (que asume cycle[i] -> cycle[i+1]),
     *   asi que aqui se invierte el arreglo antes de devolverlo -
     *   exactamente como ya hacia reconstructPath() para la ruta
     *   finita. Sin esta inversion, el ciclo resaltado en rojo nunca
     *   coincide con ninguna arista real del grafo (salvo por
     *   casualidad en ciclos de 2 nodos, donde ambos sentidos
     *   existen), y por eso no se pintaba nada.
     */
    private static int[] extractCycle(int[] parent, int markedNode, int n) {
        int current = markedNode;
        for (int i = 0; i < n; i++) {
            current = parent[current];
        }

        List<Integer> cycleNodes = new ArrayList<>();
        int cycleStart = current;
        do {
            cycleNodes.add(current);
            current = parent[current];
        } while (current != cycleStart);

        int[] result = new int[cycleNodes.size()];
        for (int i = 0; i < result.length; i++) {
            result[i] = cycleNodes.get(result.length - 1 - i);
        }
        return result;
    }

    /**
     * Reconstruye la ruta desde source hasta destination siguiendo
     * parent[] hacia atras. Incluye un guard defensivo (conjunto de
     * visitados) para nunca dar vueltas infinitas, aunque
     * matematicamente no deberia hacer falta cuando destination no
     * es unbounded (ver el javadoc de la clase).
     */
    public static int[] reconstructPath(int[] parent, int source, int destination) {
        List<Integer> reversed = new ArrayList<>();
        boolean[] visited = new boolean[parent.length];

        int current = destination;
        while (true) {
            if (visited[current]) {
                break; // guard defensivo: nunca deberia pasar en el caso finito
            }
            visited[current] = true;
            reversed.add(current);
            if (current == source) {
                break;
            }
            current = parent[current];
        }

        int[] path = new int[reversed.size()];
        for (int i = 0; i < path.length; i++) {
            path[i] = reversed.get(path.length - 1 - i);
        }
        return path;
    }

    /**
     * Resultado desde un unico origen: el churun maximo hacia cada
     * nodo, cuales nodos son "unbounded", la cadena de padres (para
     * reconstruir rutas) y el ciclo de ganancia positiva detectado
     * (vacio si no existe ninguno alcanzable desde el origen).
     */
    public static final class Result {
        private final long[] maxChurun;
        private final boolean[] unbounded;
        private final int[] parent;
        private final int[] cycle;

        Result(long[] maxChurun, boolean[] unbounded, int[] parent, int[] cycle) {
            this.maxChurun = maxChurun;
            this.unbounded = unbounded;
            this.parent = parent;
            this.cycle = cycle;
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

        public int[] getParent() {
            return parent;
        }

        /** El ciclo de ganancia positiva detectado (nodos, en algun orden dentro del ciclo). Vacio si no existe ninguno. */
        public int[] getCycle() {
            return cycle;
        }
    }
}
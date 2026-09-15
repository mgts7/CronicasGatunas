package algorithm.mission3;

import model.graph.Edge;
import model.graph.Graph;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Deque;
import java.util.List;

/**
 * Complejidad:
 *   - Tiempo:  O(N * M) para las rondas de relajacion, mas O(N)
 *              adicional para la extraccion del ciclo si aplica.
 *   - Espacio: O(N + M).
 */

public final class BellmanFordSolver {

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
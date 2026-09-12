package algorithm.mission2;

import model.graph.Edge;
import model.graph.Graph;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.PriorityQueue;

/**
 * ============================================================
 * DIJKSTRA - Mision 2: Recuperando las cuentas de Claude
 * ============================================================
 *
 * Por que Dijkstra es la eleccion correcta aqui:
 *   Los pesos son NO negativos (0 <= W <= 1,000,000), que es
 *   exactamente la condicion que garantiza que Dijkstra sea
 *   correcto: una vez que un nodo se extrae de la cola de
 *   prioridad con su distancia minima, esa distancia ya no puede
 *   mejorar. Con pesos negativos este supuesto se rompe (ver
 *   Mission 3, que si permite pesos negativos y por eso usa
 *   Bellman-Ford en su lugar).
 *
 * Complejidad:
 *   - Tiempo:  O((N + C) log N) usando PriorityQueue.
 *   - Espacio: O(N + C) para distancias, padres y la cola.
 *
 * Reconstruccion del camino:
 *   Ademas del costo, se guarda un arreglo de padres (parent[]) que
 *   se actualiza cada vez que se relaja una arista con una distancia
 *   mejor. Al llegar al destino, se sigue esa cadena de padres hacia
 *   atras para reconstruir la ruta completa nodo por nodo, necesaria
 *   para que la GUI resalte el camino sobre la red.
 */
public final class DijkstraSolver {

    /** Valor centinela que indica que el destino es inalcanzable. */
    public static final long UNREACHABLE = -1L;

    private DijkstraSolver() {
        // Utility class: no se instancia.
    }

    /**
     * Calcula la ruta de costo minimo desde source hasta destination.
     *
     * @return un Result con el costo minimo y la secuencia completa
     *         de nodos del camino (desde source hasta destination,
     *         ambos incluidos), o un Result no alcanzable si
     *         destination no es alcanzable desde source.
     */
    public static Result solve(Graph graph, int source, int destination) {
        if (source == destination) {
            return Result.of(0L, new int[] { source });
        }

        int n = graph.getNumNodes();
        long[] dist = new long[n];
        int[] parent = new int[n];
        Arrays.fill(dist, Long.MAX_VALUE);
        Arrays.fill(parent, -1);
        dist[source] = 0L;

        PriorityQueue<NodeDistance> queue = new PriorityQueue<>();
        queue.add(new NodeDistance(source, 0L));

        while (!queue.isEmpty()) {
            NodeDistance current = queue.poll();

            // Lazy deletion: entradas obsoletas en la cola se ignoran.
            if (current.distance > dist[current.node]) {
                continue;
            }

            // Corte temprano: en cuanto se extrae el destino, su
            // distancia ya es minima (propiedad de Dijkstra con
            // pesos no negativos), y su camino ya quedo fijado en parent[].
            if (current.node == destination) {
                return Result.of(current.distance, reconstructPath(parent, source, destination));
            }

            for (Edge edge : graph.getNeighbors(current.node)) {
                long candidate = current.distance + edge.getWeight();
                if (candidate < dist[edge.getTo()]) {
                    dist[edge.getTo()] = candidate;
                    parent[edge.getTo()] = current.node;
                    queue.add(new NodeDistance(edge.getTo(), candidate));
                }
            }
        }

        // La cola se vacio sin extraer el destino: es inalcanzable.
        return Result.unreachable();
    }

    /** Sigue la cadena de padres desde destination hacia atras hasta source, y la invierte. */
    private static int[] reconstructPath(int[] parent, int source, int destination) {
        List<Integer> reversed = new ArrayList<>();
        int current = destination;
        while (true) {
            reversed.add(current);
            if (current == source) {
                break;
            }
            current = parent[current];
        }

        int[] path = new int[reversed.size()];
        for (int i = 0; i < path.length; i++) {
            path[i] = reversed.get(reversed.size() - 1 - i);
        }
        return path;
    }

    /**
     * Par (nodo, distancia acumulada) ordenado por distancia,
     * usado como elemento de la PriorityQueue.
     */
    private static final class NodeDistance implements Comparable<NodeDistance> {
        private final int node;
        private final long distance;

        NodeDistance(int node, long distance) {
            this.node = node;
            this.distance = distance;
        }

        @Override
        public int compareTo(NodeDistance other) {
            return Long.compare(this.distance, other.distance);
        }
    }

    /**
     * Resultado de Dijkstra: si el destino es alcanzable, el costo
     * minimo y el camino completo (para que la GUI lo resalte sobre
     * la red); si no, ninguno de los dos tiene sentido.
     */
    public static final class Result {
        private final long cost;
        private final int[] path;

        private Result(long cost, int[] path) {
            this.cost = cost;
            this.path = path;
        }

        static Result of(long cost, int[] path) {
            return new Result(cost, path);
        }

        static Result unreachable() {
            return new Result(UNREACHABLE, new int[0]);
        }

        public boolean isReachable() {
            return cost != UNREACHABLE;
        }

        public long getCost() {
            return cost;
        }

        /** La secuencia de nodos del camino, desde source hasta destination (ambos incluidos). Vacio si es inalcanzable. */
        public int[] getPath() {
            return path;
        }
    }
}
package algorithm.mission2;

import model.graph.Edge;
import model.graph.Graph;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.PriorityQueue;

/**

 * Complejidad:
 *   - Tiempo:  O((N + C) log N) usando PriorityQueue.
 *   - Espacio: O(N + C) para distancias, padres y la cola.
 *
 */
public final class DijkstraSolver {

    public static final long UNREACHABLE = -1L;

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

        public int[] getPath() {
            return path;
        }
    }
}
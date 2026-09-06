package algorithm.mission2;

import model.graph.Edge;
import model.graph.Graph;

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
 *   mejorar (ningun peso negativo podria "abaratar" un camino
 *   futuro). Con pesos negativos este supuesto se rompe y Dijkstra
 *   puede dar respuestas incorrectas (ver Mission 3, que si permite
 *   pesos negativos y por eso usa Bellman-Ford en su lugar).
 *
 * Complejidad:
 *   - Tiempo:  O((N + C) log N) usando PriorityQueue: cada arista
 *              provoca a lo sumo una insercion en la cola, y cada
 *              insercion/extraccion cuesta O(log N).
 *   - Espacio: O(N + C) para el arreglo de distancias y la cola.
 *   Un escaneo O(N^2) sin cola de prioridad NO es aceptable a los
 *   limites del enunciado (N hasta 10,000).
 *
 * Sobre el valor centinela:
 *   dist[] se inicializa en Long.MAX_VALUE para representar "aun no
 *   alcanzado". Nunca se hace aritmetica sobre ese valor: solo se
 *   compara (newDist < dist[vecino]), y newDist siempre se calcula
 *   a partir de la distancia de un nodo YA extraido de la cola
 *   (por lo tanto, siempre finita). Esto evita el desborde que
 *   ocurriria si se sumara un peso a Long.MAX_VALUE.
 */
public final class DijkstraSolver {

    /** Valor centinela que indica que el destino es inalcanzable. */
    public static final long UNREACHABLE = -1L;

    private DijkstraSolver() {
        // Utility class: no se instancia.
    }

    /**
     * Calcula el costo minimo de la ruta desde source hasta destination.
     *
     * @return el costo minimo, 0 si source == destination, o
     *         UNREACHABLE si destination no es alcanzable desde source.
     */
    public static long solve(Graph graph, int source, int destination) {
        if (source == destination) {
            return 0L;
        }

        int n = graph.getNumNodes();
        long[] dist = new long[n];
        java.util.Arrays.fill(dist, Long.MAX_VALUE);
        dist[source] = 0L;

        PriorityQueue<NodeDistance> queue = new PriorityQueue<>();
        queue.add(new NodeDistance(source, 0L));

        while (!queue.isEmpty()) {
            NodeDistance current = queue.poll();

            // Lazy deletion: puede haber entradas obsoletas en la cola
            // (se agrego una version mas barata despues). Se ignoran.
            if (current.distance > dist[current.node]) {
                continue;
            }

            // Corte temprano: en cuanto se extrae el destino, su
            // distancia ya es minima (propiedad de Dijkstra con
            // pesos no negativos).
            if (current.node == destination) {
                return current.distance;
            }

            for (Edge edge : graph.getNeighbors(current.node)) {
                long candidate = current.distance + edge.getWeight();
                if (candidate < dist[edge.getTo()]) {
                    dist[edge.getTo()] = candidate;
                    queue.add(new NodeDistance(edge.getTo(), candidate));
                }
            }
        }

        return dist[destination] == Long.MAX_VALUE ? UNREACHABLE : dist[destination];
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
}
package algorithm.mission2;

import model.graph.Edge;
import model.graph.Graph;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DijkstraSolverTest {

    @Test
    @DisplayName("Ejemplo 1 del enunciado: arista directa 0-1 peso 100")
    void sampleCase1_directEdge() {
        Graph graph = new Graph(2);
        graph.addUndirectedEdge(0, 1, 100);

        DijkstraSolver.Result result = DijkstraSolver.solve(graph, 0, 1);

        assertEquals(100L, result.getCost());
    }

    @Test
    @DisplayName("Ejemplo 2 del enunciado: la ruta de dos saltos es mas barata que la directa, y el camino es correcto")
    void sampleCase2_cheaperTwoHopRoute() {
        Graph graph = new Graph(3);
        graph.addUndirectedEdge(0, 1, 100);
        graph.addUndirectedEdge(0, 2, 200);
        graph.addUndirectedEdge(1, 2, 50);

        DijkstraSolver.Result result = DijkstraSolver.solve(graph, 2, 0);

        assertEquals(150L, result.getCost());
        assertEquals(3, result.getPath().length);
        assertEquals(2, result.getPath()[0]);
        assertEquals(1, result.getPath()[1]);
        assertEquals(0, result.getPath()[2]);
    }

    @Test
    @DisplayName("La suma de pesos a lo largo del camino reconstruido coincide con el costo reportado")
    void path_weightSumMatchesReportedCost() {
        Graph graph = new Graph(5);
        graph.addUndirectedEdge(0, 1, 10);
        graph.addUndirectedEdge(1, 2, 20);
        graph.addUndirectedEdge(2, 3, 5);
        graph.addUndirectedEdge(0, 3, 100);
        graph.addUndirectedEdge(3, 4, 7);

        DijkstraSolver.Result result = DijkstraSolver.solve(graph, 0, 4);

        long sumAlongPath = 0;
        int[] path = result.getPath();
        for (int i = 1; i < path.length; i++) {
            sumAlongPath += weightBetween(graph, path[i - 1], path[i]);
        }

        assertEquals(result.getCost(), sumAlongPath);
        assertEquals(0, path[0]);
        assertEquals(4, path[path.length - 1]);
    }

    @Test
    @DisplayName("Ejemplo 3 del enunciado: grafo sin aristas es inalcanzable, con camino vacio")
    void sampleCase3_noEdges_returnsUnreachable() {
        Graph graph = new Graph(2);

        DijkstraSolver.Result result = DijkstraSolver.solve(graph, 0, 1);

        assertEquals(DijkstraSolver.UNREACHABLE, result.getCost());
        assertTrue(!result.isReachable());
        assertEquals(0, result.getPath().length);
    }

    @Test
    @DisplayName("Si source y destination coinciden, la respuesta es 0 y el camino es un solo nodo")
    void sameSourceAndDestination_returnsZero() {
        Graph graph = new Graph(5);

        DijkstraSolver.Result result = DijkstraSolver.solve(graph, 3, 3);

        assertEquals(0L, result.getCost());
        assertEquals(1, result.getPath().length);
        assertEquals(3, result.getPath()[0]);
    }

    @Test
    @DisplayName("Un self-loop no debe afectar el resultado")
    void selfLoop_doesNotAffectResult() {
        Graph graph = new Graph(2);
        graph.addUndirectedEdge(0, 0, 999);
        graph.addUndirectedEdge(0, 1, 10);

        DijkstraSolver.Result result = DijkstraSolver.solve(graph, 0, 1);

        assertEquals(10L, result.getCost());
    }

    @Test
    @DisplayName("Conexiones repetidas entre el mismo par: Dijkstra debe quedarse con el costo mas barato")
    void duplicateEdges_usesCheapestOne() {
        Graph graph = new Graph(2);
        graph.addUndirectedEdge(0, 1, 50);
        graph.addUndirectedEdge(0, 1, 5);

        DijkstraSolver.Result result = DijkstraSolver.solve(graph, 0, 1);

        assertEquals(5L, result.getCost());
    }

    @Test
    @DisplayName("En una cadena de N nodos con peso 1 por arista, el costo es N-1 y el camino pasa por todos los nodos en orden")
    void chainGraph_returnsNumberOfHopsAndFullPath() {
        int n = 1000;
        Graph graph = new Graph(n);
        for (int i = 0; i < n - 1; i++) {
            graph.addUndirectedEdge(i, i + 1, 1);
        }

        DijkstraSolver.Result result = DijkstraSolver.solve(graph, 0, n - 1);

        assertEquals(n - 1, result.getCost());
        assertEquals(n, result.getPath().length);
    }

    @Test
    @DisplayName("Un nodo aislado (sin conexiones) dentro de un grafo mas grande es inalcanzable")
    void isolatedNodeInLargerGraph_returnsUnreachable() {
        Graph graph = new Graph(4);
        graph.addUndirectedEdge(0, 1, 10);
        graph.addUndirectedEdge(1, 2, 10);
        // el nodo 3 queda aislado, sin conexiones

        DijkstraSolver.Result result = DijkstraSolver.solve(graph, 0, 3);

        assertEquals(DijkstraSolver.UNREACHABLE, result.getCost());
    }

    private long weightBetween(Graph graph, int from, int to) {
        for (Edge edge : graph.getNeighbors(from)) {
            if (edge.getTo() == to) {
                return edge.getWeight();
            }
        }
        throw new IllegalStateException("No existe arista " + from + " -> " + to);
    }
}
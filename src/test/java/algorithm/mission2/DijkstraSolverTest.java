package algorithm.mission2;

import model.graph.Graph;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class DijkstraSolverTest {

    @Test
    @DisplayName("Ejemplo 1 del enunciado: arista directa 0-1 peso 100")
    void sampleCase1_directEdge() {
        Graph graph = new Graph(2);
        graph.addUndirectedEdge(0, 1, 100);

        long result = DijkstraSolver.solve(graph, 0, 1);

        assertEquals(100L, result);
    }

    @Test
    @DisplayName("Ejemplo 2 del enunciado: la ruta de dos saltos es mas barata que la directa")
    void sampleCase2_cheaperTwoHopRoute() {
        Graph graph = new Graph(3);
        graph.addUndirectedEdge(0, 1, 100);
        graph.addUndirectedEdge(0, 2, 200);
        graph.addUndirectedEdge(1, 2, 50);

        long result = DijkstraSolver.solve(graph, 2, 0);

        assertEquals(150L, result);
    }

    @Test
    @DisplayName("Ejemplo 3 del enunciado: grafo sin aristas es inalcanzable")
    void sampleCase3_noEdges_returnsUnreachable() {
        Graph graph = new Graph(2);

        long result = DijkstraSolver.solve(graph, 0, 1);

        assertEquals(DijkstraSolver.UNREACHABLE, result);
    }

    @Test
    @DisplayName("Si source y destination coinciden, la respuesta es 0 incluso sin aristas")
    void sameSourceAndDestination_returnsZero() {
        Graph graph = new Graph(5);

        long result = DijkstraSolver.solve(graph, 3, 3);

        assertEquals(0L, result);
    }

    @Test
    @DisplayName("Un self-loop no debe afectar el resultado")
    void selfLoop_doesNotAffectResult() {
        Graph graph = new Graph(2);
        graph.addUndirectedEdge(0, 0, 999);
        graph.addUndirectedEdge(0, 1, 10);

        long result = DijkstraSolver.solve(graph, 0, 1);

        assertEquals(10L, result);
    }

    @Test
    @DisplayName("Conexiones repetidas entre el mismo par: Dijkstra debe quedarse con el costo mas barato")
    void duplicateEdges_usesCheapestOne() {
        Graph graph = new Graph(2);
        graph.addUndirectedEdge(0, 1, 50);
        graph.addUndirectedEdge(0, 1, 5);

        long result = DijkstraSolver.solve(graph, 0, 1);

        assertEquals(5L, result);
    }

    @Test
    @DisplayName("Un peso de 0 es valido y no rompe el algoritmo")
    void zeroWeightEdge_isValid() {
        Graph graph = new Graph(2);
        graph.addUndirectedEdge(0, 1, 0);

        long result = DijkstraSolver.solve(graph, 0, 1);

        assertEquals(0L, result);
    }

    @Test
    @DisplayName("En una cadena de N nodos con peso 1 por arista, la distancia minima es N-1")
    void chainGraph_returnsNumberOfHops() {
        int n = 1000;
        Graph graph = new Graph(n);
        for (int i = 0; i < n - 1; i++) {
            graph.addUndirectedEdge(i, i + 1, 1);
        }

        long result = DijkstraSolver.solve(graph, 0, n - 1);

        assertEquals(n - 1, result);
    }

    @Test
    @DisplayName("Un nodo aislado (sin conexiones) dentro de un grafo mas grande es inalcanzable")
    void isolatedNodeInLargerGraph_returnsUnreachable() {
        Graph graph = new Graph(4);
        graph.addUndirectedEdge(0, 1, 10);
        graph.addUndirectedEdge(1, 2, 10);
        // el nodo 3 queda aislado, sin conexiones

        long result = DijkstraSolver.solve(graph, 0, 3);

        assertEquals(DijkstraSolver.UNREACHABLE, result);
    }
}
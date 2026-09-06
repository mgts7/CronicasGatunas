package model.graph;

import java.util.ArrayList;
import java.util.List;

/**
 * Grafo ponderado representado con lista de adyacencia.
 *
 * Se disena para ser reutilizado tanto por Mission 2 (Dijkstra,
 * conexiones bidireccionales) como por Mission 3 (Floyd-Warshall /
 * Bellman-Ford, pasajes de un solo sentido): por eso expone tanto
 * addDirectedEdge como addUndirectedEdge, en vez de asumir un tipo
 * de grafo fijo.
 *
 * Conexiones repetidas entre el mismo par de nodos y self-loops son
 * validos segun el enunciado (no deben provocar un crash): esta
 * clase simplemente los almacena todos, sin deduplicar. Los
 * algoritmos que consumen el grafo (Dijkstra, etc.) son correctos
 * incluso con aristas duplicadas o self-loops.
 */
public final class Graph {

    private final int numNodes;
    private final List<List<Edge>> adjacency;

    public Graph(int numNodes) {
        this.numNodes = numNodes;
        this.adjacency = new ArrayList<>(numNodes);
        for (int i = 0; i < numNodes; i++) {
            adjacency.add(new ArrayList<>());
        }
    }

    public int getNumNodes() {
        return numNodes;
    }

    /** Agrega una arista dirigida de 'from' hacia 'to' (Mission 3: pasajes de un solo sentido). */
    public void addDirectedEdge(int from, int to, long weight) {
        adjacency.get(from).add(new Edge(to, weight));
    }

    /** Agrega una arista bidireccional agregando la dirigida en ambos sentidos (Mission 2). */
    public void addUndirectedEdge(int a, int b, long weight) {
        addDirectedEdge(a, b, weight);
        addDirectedEdge(b, a, weight);
    }

    public List<Edge> getNeighbors(int node) {
        return adjacency.get(node);
    }
}
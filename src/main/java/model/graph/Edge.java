package model.graph;

/**
 * Representa una arista dirigida hacia el nodo 'to' con un peso.
 *
 * El peso se guarda como long (no int) porque en Missions 2 y 3 los
 * costos acumulados a lo largo de una ruta pueden superar el rango
 * de int: hasta 10,000 nodos con pesos de hasta 1,000,000 cada uno
 * dan un acumulado maximo de ~10^10, que desborda un int (~2.1*10^9).
 */
public final class Edge {

    private final int to;
    private final long weight;

    public Edge(int to, long weight) {
        this.to = to;
        this.weight = weight;
    }

    public int getTo() {
        return to;
    }

    public long getWeight() {
        return weight;
    }
}
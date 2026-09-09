package algorithm.mission4;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * ============================================================
 * KRUSKAL (Minimum Spanning Tree) - Mision 4
 * ============================================================
 *
 * Por que Kruskal es la eleccion correcta aqui:
 *   El problema pide reconectar todas las intersecciones con el
 *   costo total minimo posible, sin restriccion de que el proceso
 *   empiece desde algun nodo en particular. Kruskal resuelve esto
 *   directamente: ordena todos los cables disponibles de mas barato
 *   a mas caro y los va aceptando uno por uno, siempre que no
 *   formen un ciclo (verificado con UnionFind). El resultado es,
 *   demostrablemente, el arbol de expansion de costo minimo.
 *
 * Diferencia con Prim (no usado aqui):
 *   Prim crece un unico arbol desde un nodo semilla, expandiendolo
 *   arista por arista. Kruskal no necesita ese nodo semilla: mira
 *   el problema completo de una vez, ordenado por costo. Con C
 *   cables dispersos (no necesariamente densos) y hasta 100,000
 *   cables, ordenar y usar Union-Find es simple y eficiente.
 *
 * Complejidad:
 *   - Tiempo:  O(C log C) - el ordenamiento de los C cables domina
 *              el tiempo total (las operaciones de UnionFind son
 *              casi O(1) amortizado cada una, ver UnionFind.java).
 *   - Espacio: O(N + C) - la estructura Union-Find y la lista
 *              ordenada de cables.
 *
 * Cables duplicados y self-loops (seccion 6, "Duplicated cables and
 * cables from an intersection to itself may appear"):
 *   No requieren ningun manejo especial. Un self-loop (u == v)
 *   simplemente hace que union(u, v) devuelva false de inmediato
 *   (find(u) == find(v) porque son el mismo nodo), asi que se
 *   descarta automaticamente sin necesidad de un chequeo explicito.
 *   Cables duplicados entre el mismo par: el mas barato se procesa
 *   primero (por el ordenamiento) y se usa; los mas caros luego
 *   encuentran que ese par ya esta conectado y se descartan solos.
 */
public final class KruskalSolver {

    /** Valor centinela: no fue posible conectar todas las intersecciones. */
    public static final long CANNOT_CONNECT = -1L;

    private KruskalSolver() {
        // Utility class: no se instancia.
    }

    public static Result solve(int n, List<Cable> cables) {
        List<Cable> sorted = new ArrayList<>(cables);
        sorted.sort(Comparator.comparingLong(Cable::getCost));

        UnionFind unionFind = new UnionFind(n);
        long totalCost = 0L;
        int edgesUsed = 0;
        List<Cable> mstCables = new ArrayList<>();

        for (Cable cable : sorted) {
            if (unionFind.union(cable.getU(), cable.getV())) {
                totalCost += cable.getCost();
                mstCables.add(cable);
                edgesUsed++;

                // Un arbol de expansion sobre n nodos siempre tiene
                // exactamente n-1 aristas: en cuanto se alcanzan, el
                // resto de los cables (mas caros) son innecesarios.
                if (edgesUsed == n - 1) {
                    break;
                }
            }
        }

        boolean allConnected = (edgesUsed == n - 1);
        if (!allConnected) {
            return new Result(false, CANNOT_CONNECT, mstCables);
        }
        return new Result(true, totalCost, mstCables);
    }

    /** Un cable disponible: conecta u con v (0-indexados) con un costo dado. */
    public static final class Cable {
        private final int u;
        private final int v;
        private final long cost;

        public Cable(int u, int v, long cost) {
            this.u = u;
            this.v = v;
            this.cost = cost;
        }

        public int getU() {
            return u;
        }

        public int getV() {
            return v;
        }

        public long getCost() {
            return cost;
        }
    }

    /** Resultado de Kruskal: si fue posible conectar todo, el costo total, y los cables usados (para resaltar en la GUI). */
    public static final class Result {
        private final boolean connectable;
        private final long totalCost;
        private final List<Cable> mstCables;

        public Result(boolean connectable, long totalCost, List<Cable> mstCables) {
            this.connectable = connectable;
            this.totalCost = totalCost;
            this.mstCables = mstCables;
        }

        public boolean isConnectable() {
            return connectable;
        }

        public long getTotalCost() {
            return totalCost;
        }

        /** Los cables que forman el MST, en el orden en que fueron aceptados. */
        public List<Cable> getMstCables() {
            return mstCables;
        }
    }
}
package algorithm.mission4;

/**
 * ============================================================
 * UNION-FIND (Disjoint Set) - Mision 4
 * ============================================================
 *
 * Estructura auxiliar de Kruskal: permite saber en tiempo casi
 * constante si dos intersecciones ya estan conectadas (directa o
 * indirectamente), lo cual es exactamente lo que Kruskal necesita
 * para decidir si un cable formaria un ciclo (y por lo tanto debe
 * descartarse) o si conecta dos componentes distintas (y por lo
 * tanto debe usarse).
 *
 * Las dos optimizaciones que exige el enunciado:
 *
 * 1. Compresion de caminos (path compression):
 *    Cada vez que se llama find(x), todos los nodos visitados en el
 *    camino hacia la raiz quedan apuntando mas cerca de la raiz
 *    (aqui con "path halving": cada nodo salta a su abuelo). Esto
 *    aplana el arbol con el tiempo, acelerando busquedas futuras.
 *
 * 2. Union por tamano (union by size):
 *    Al unir dos conjuntos, el arbol mas chico siempre cuelga del
 *    mas grande. Sin esto, en el peor caso los arboles podrian
 *    degenerar en una lista enlazada de profundidad N.
 *
 * Complejidad:
 *   Con ambas optimizaciones combinadas, cada operacion (find o
 *   union) es O(alpha(N)) amortizado, donde alpha es la inversa de
 *   la funcion de Ackermann: en la practica, indistinguible de O(1)
 *   incluso para N tan grande como 10,000 (limite del enunciado).
 */
public final class UnionFind {

    private final int[] parent;
    private final int[] size;
    private int componentCount;

    public UnionFind(int n) {
        parent = new int[n];
        size = new int[n];
        for (int i = 0; i < n; i++) {
            parent[i] = i;
            size[i] = 1;
        }
        componentCount = n;
    }

    /** Encuentra la raiz del conjunto al que pertenece x, comprimiendo el camino. */
    public int find(int x) {
        while (parent[x] != x) {
            parent[x] = parent[parent[x]]; // path halving: salta al abuelo
            x = parent[x];
        }
        return x;
    }

    /**
     * Intenta unir los conjuntos de a y b.
     *
     * @return true si a y b estaban en conjuntos distintos (la union
     *         se realizo), o false si ya estaban en el mismo conjunto
     *         (en Kruskal, esto significa que la arista formaria un ciclo
     *         y debe descartarse).
     */
    public boolean union(int a, int b) {
        int rootA = find(a);
        int rootB = find(b);

        if (rootA == rootB) {
            return false;
        }

        // Union por tamano: el arbol mas chico cuelga del mas grande.
        if (size[rootA] < size[rootB]) {
            int temp = rootA;
            rootA = rootB;
            rootB = temp;
        }
        parent[rootB] = rootA;
        size[rootA] += size[rootB];
        componentCount--;
        return true;
    }

    /** Cuantos componentes conexos quedan actualmente. Al terminar Kruskal, 1 significa "todo conectado". */
    public int getComponentCount() {
        return componentCount;
    }
}
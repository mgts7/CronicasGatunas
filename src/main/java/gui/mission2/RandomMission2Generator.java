package gui.mission2;

import java.util.Random;

/**
 * ============================================================
 * RandomMission2Generator
 * ============================================================
 *
 * Genera un input de Mision 2 aleatorio, en el formato de texto
 * EXACTO que espera Mission2Parser (seccion 4 del enunciado), para
 * el boton "Generar" de la GUI.
 *
 * Estrategia: primero se genera un arbol de expansion aleatorio
 * (cada nodo nuevo se conecta a un nodo ya existente elegido al
 * azar), lo que GARANTIZA que el grafo quede conectado. Luego se
 * agregan algunas aristas extra al azar para crear rutas
 * alternativas (y que Dijkstra tenga algo interesante que elegir).
 * Sin esto, un grafo puramente aleatorio con pocas aristas suele
 * quedar desconectado casi siempre, y la demo mostraria "Nina is
 * very sad" con demasiada frecuencia.
 */
public final class RandomMission2Generator {

    private static final int MIN_NODES = 5;
    private static final int MAX_NODES = 20; // bien por debajo del limite de dibujo (60, seccion 2.3)
    private static final int MIN_WEIGHT = 1;
    private static final int MAX_WEIGHT = 50; // valores chicos para que los costos sean faciles de leer en la demo
    private static final double EXTRA_EDGE_PROBABILITY = 0.15;

    private final Random random = new Random();

    /** Genera un unico caso de prueba (T=1), en texto, listo para pasarle a Mission2Parser.parse(...). */
    public String generate() {
        int n = MIN_NODES + random.nextInt(MAX_NODES - MIN_NODES + 1);

        StringBuilder edges = new StringBuilder();
        int edgeCount = 0;

        // Arbol aleatorio: garantiza conectividad total.
        for (int node = 1; node < n; node++) {
            int parent = random.nextInt(node); // un nodo ya existente, entre 0 y node-1
            int weight = randomWeight();
            edges.append(parent).append(' ').append(node).append(' ').append(weight).append('\n');
            edgeCount++;
        }

        // Aristas extra, para que existan rutas alternativas.
        for (int u = 0; u < n; u++) {
            for (int v = u + 1; v < n; v++) {
                if (random.nextDouble() < EXTRA_EDGE_PROBABILITY) {
                    int weight = randomWeight();
                    edges.append(u).append(' ').append(v).append(' ').append(weight).append('\n');
                    edgeCount++;
                }
            }
        }

        int source;
        int destination;
        do {
            source = random.nextInt(n);
            destination = random.nextInt(n);
        } while (source == destination);

        StringBuilder sb = new StringBuilder();
        sb.append("1\n"); // T = 1 caso de prueba
        sb.append(n).append(' ').append(edgeCount).append(' ').append(source).append(' ').append(destination).append('\n');
        sb.append(edges);

        return sb.toString();
    }

    private int randomWeight() {
        return MIN_WEIGHT + random.nextInt(MAX_WEIGHT - MIN_WEIGHT + 1);
    }
}
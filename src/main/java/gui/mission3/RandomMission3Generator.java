package gui.mission3;

import java.util.Random;

/**
 * ============================================================
 * RandomMission3Generator
 * ============================================================
 *
 * Genera un input de Mision 3 aleatorio, en el formato de texto
 * EXACTO que espera Mission3Parser (seccion 5 del enunciado).
 *
 * A diferencia de RandomMission2Generator (que fuerza conectividad
 * total via arbol de expansion), aqui NO se fuerza nada: el grafo es
 * dirigido y puede legitimamente contener ciclos de ganancia
 * positiva ("Infinite churun!") o dejar el destino inalcanzable
 * ("Limon blocked the way") -- ambos son resultados validos y
 * educativos que vale la pena ver ocasionalmente en la demo, no
 * casos a evitar.
 */
public final class RandomMission3Generator {

    private static final int MIN_NODES = 5;
    private static final int MAX_NODES = 10; // bien por debajo del limite de dibujo (60, seccion 2.3)
    private static final int MIN_WEIGHT = -30;
    private static final int MAX_WEIGHT = 30;
    private static final double EDGE_DENSITY = 1.8; // aristas por nodo, aprox

    private final Random random = new Random();

    /** Genera un unico caso de prueba (T=1), en texto, listo para pasarle a Mission3Parser.parse(...). */
    public String generate() {
        int n = MIN_NODES + random.nextInt(MAX_NODES - MIN_NODES + 1);
        int m = Math.max(1, (int) (n * EDGE_DENSITY));

        StringBuilder edges = new StringBuilder();
        for (int i = 0; i < m; i++) {
            int a = random.nextInt(n);
            int b;
            do {
                b = random.nextInt(n);
            } while (b == a); // sin self-loops en la demo, por simplicidad visual

            int weight = randomWeight();
            edges.append(a).append(' ').append(b).append(' ').append(weight).append('\n');
        }

        int source;
        int destination;
        do {
            source = random.nextInt(n);
            destination = random.nextInt(n);
        } while (source == destination);

        StringBuilder sb = new StringBuilder();
        sb.append("1\n");
        sb.append(n).append(' ').append(m).append(' ').append(source).append(' ').append(destination).append('\n');
        sb.append(edges);

        return sb.toString();
    }

    private int randomWeight() {
        return MIN_WEIGHT + random.nextInt(MAX_WEIGHT - MIN_WEIGHT + 1);
    }
}
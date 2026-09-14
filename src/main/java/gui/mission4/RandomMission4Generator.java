package gui.mission4;

import java.util.Random;

/**
 * ============================================================
 * RandomMission4Generator
 * ============================================================
 *
 * Genera un input de Mision 4 aleatorio, en el formato de texto
 * EXACTO que espera Mission4Parser (seccion 6 del enunciado): nodos
 * en 1-indexado, tal como los escribe el enunciado.
 *
 * Misma estrategia que RandomMission2Generator: un arbol de
 * expansion aleatorio garantiza que la red quede completamente
 * conectable (si no, casi todas las demos terminarian en
 * "Limon cut too many cables"), mas algunos cables extra para que
 * Kruskal tenga alternativas reales entre las que elegir.
 */
public final class RandomMission4Generator {

    private static final int MIN_NODES = 5;
    private static final int MAX_NODES = 20; // bien por debajo del limite de dibujo (100, seccion 2.3)
    private static final int MIN_COST = 1;
    private static final int MAX_COST = 50;
    private static final double EXTRA_CABLE_PROBABILITY = 0.15;

    private final Random random = new Random();

    /** Genera un unico caso de prueba (T=1), en texto, listo para pasarle a Mission4Parser.parse(...). */
    public String generate() {
        int n = MIN_NODES + random.nextInt(MAX_NODES - MIN_NODES + 1);

        StringBuilder cables = new StringBuilder();
        int cableCount = 0;

        // Arbol aleatorio en 1-indexado: cada nodo nuevo (2..n) se
        // conecta a un nodo ya existente (1..node-1). Garantiza
        // conectividad total.
        for (int node = 2; node <= n; node++) {
            int parent = 1 + random.nextInt(node - 1);
            int cost = randomCost();
            cables.append(parent).append(' ').append(node).append(' ').append(cost).append('\n');
            cableCount++;
        }

        // Cables extra, para que Kruskal tenga alternativas entre las que elegir.
        for (int u = 1; u <= n; u++) {
            for (int v = u + 1; v <= n; v++) {
                if (random.nextDouble() < EXTRA_CABLE_PROBABILITY) {
                    int cost = randomCost();
                    cables.append(u).append(' ').append(v).append(' ').append(cost).append('\n');
                    cableCount++;
                }
            }
        }

        StringBuilder sb = new StringBuilder();
        sb.append("1\n"); // T = 1 caso de prueba
        sb.append(n).append('\n');
        sb.append(cableCount).append('\n');
        sb.append(cables);

        return sb.toString();
    }

    private int randomCost() {
        return MIN_COST + random.nextInt(MAX_COST - MIN_COST + 1);
    }
}
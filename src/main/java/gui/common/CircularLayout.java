package gui.common;

/**
 * ============================================================
 * CircularLayout
 * ============================================================
 *
 * Calcula posiciones (x, y) para N nodos distribuidos uniformemente
 * en un circulo. Es simple, determinista (el mismo N siempre da las
 * mismas posiciones) y suficiente para los limites de dibujo de
 * este proyecto (Missions 2 y 3: hasta 60 nodos; Mission 4: hasta
 * 100 intersecciones).
 *
 * No se uso una libreria de grafos para esto (ver seccion 2.1 del
 * enunciado: estan permitidas solo para layout/render, nunca para
 * el nucleo algoritmico, que ya esta resuelto desde cero). Se opto
 * por no usar ninguna de todas formas: con estos tamanos, un layout
 * circular a mano es mas simple de razonar, de mantener y de
 * defender en la sustentacion que integrar una dependencia externa
 * solo para esto.
 */
public final class CircularLayout {

    private CircularLayout() {
        // Utility class: no se instancia.
    }

    /**
     * @return un arreglo de n posiciones, donde positions[i] = {x, y}
     *         es la posicion del nodo i. Si n == 0, devuelve un
     *         arreglo vacio.
     */
    public static double[][] compute(int n, double centerX, double centerY, double radius) {
        double[][] positions = new double[n][2];

        for (int i = 0; i < n; i++) {
            // -PI/2 para que el nodo 0 arranque arriba (12 en punto),
            // en vez de a la derecha (3 en punto) que seria el default
            // matematico de angulo 0.
            double angle = (2 * Math.PI * i / n) - (Math.PI / 2);
            positions[i][0] = centerX + radius * Math.cos(angle);
            positions[i][1] = centerY + radius * Math.sin(angle);
        }

        return positions;
    }
}
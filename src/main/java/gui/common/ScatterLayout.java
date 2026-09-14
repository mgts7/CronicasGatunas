package gui.common;

import java.util.Random;

/**
 * ============================================================
 * ScatterLayout
 * ============================================================
 *
 * Distribuye N nodos de forma DISPERSA sobre un area rectangular,
 * como puntos de interes esparcidos sobre un mapa (encaja con la
 * tematica de "mapa del tesoro" de Mission 3), a diferencia de
 * CircularLayout (Mission 2, prolijamente en un circulo) y
 * GridLayout (Mission 4, en una cuadricula tipo manzana urbana).
 *
 * Se usa una secuencia de Halton en vez de numeros puramente
 * aleatorios: un generador aleatorio comun (incluso con semilla fija)
 * tiende a dejar huecos vacios y amontonamientos por puro azar. La
 * secuencia de Halton es de "baja discrepancia": genera puntos que
 * SE VEN dispersos e irregulares (no forman ningun patron obvio como
 * un circulo o una grilla), pero estan matematicamente garantizados
 * a cubrir el area de forma pareja, sin huecos ni amontonamientos.
 *
 * Version con semilla (rotacion aleatoria):
 *   Una secuencia de Halton "pura" siempre da los mismos N puntos
 *   para el mismo N (util para reproducibilidad, pero aburrido si se
 *   genera un caso nuevo y el mapa se ve identico al anterior). La
 *   tecnica estandar para "aleatorizar" una secuencia de baja
 *   discrepancia sin perder sus propiedades de buena cobertura se
 *   llama "Cranley-Patterson rotation": se le suma un desplazamiento
 *   aleatorio (distinto por generacion, pero el MISMO para todos los
 *   nodos de esa generacion) a cada coordenada, y se toma modulo 1.
 *   Esto rota/traslada el patron completo como si fuera un mapa
 *   fisico desplazado, preservando que los puntos sigan bien
 *   distribuidos entre si.
 */
public final class ScatterLayout {

    private ScatterLayout() {
        // Utility class: no se instancia.
    }

    /**
     * @param n          cantidad de nodos
     * @param areaWidth  ancho total del area disponible (ej. el canvas)
     * @param areaHeight alto total del area disponible
     * @param marginX    margen horizontal a dejar en los bordes
     * @param marginY    margen vertical a dejar en los bordes
     * @return un arreglo de n posiciones, positions[i] = {x, y}
     */
    /**
     * Version sin semilla: siempre da exactamente los mismos N
     * puntos para el mismo N (equivalente a compute(n, ..., 0L)).
     */
    public static double[][] compute(int n, double areaWidth, double areaHeight,
                                     double marginX, double marginY) {
        return compute(n, areaWidth, areaHeight, marginX, marginY, 0L);
    }

    /**
     * Version con semilla: aplica una rotacion aleatoria (Cranley-
     * Patterson) a la secuencia de Halton, para que generaciones
     * distintas (semillas distintas) den distribuciones distintas,
     * sin perder la buena cobertura del area.
     *
     * @param seed semilla de la rotacion. La misma semilla siempre da
     *             exactamente los mismos puntos (determinista por
     *             semilla); semillas distintas dan disposiciones
     *             visualmente distintas.
     */
    public static double[][] compute(int n, double areaWidth, double areaHeight,
                                     double marginX, double marginY, long seed) {
        double[][] positions = new double[n][2];
        if (n == 0) {
            return positions;
        }

        double usableWidth = areaWidth - 2 * marginX;
        double usableHeight = areaHeight - 2 * marginY;

        // Un unico desplazamiento aleatorio por generacion (no por
        // nodo): esto es lo que hace que TODOS los puntos se muevan
        // juntos como un patron coherente, en vez de aleatorizar cada
        // nodo por separado (lo cual destruiria la buena distribucion
        // que da Halton).
        Random rng = new Random(seed);
        double offsetX = rng.nextDouble();
        double offsetY = rng.nextDouble();

        for (int i = 0; i < n; i++) {
            double hx = fractionalPart(halton(i + 1, 2) + offsetX);
            double hy = fractionalPart(halton(i + 1, 3) + offsetY);

            positions[i][0] = marginX + hx * usableWidth;
            positions[i][1] = marginY + hy * usableHeight;
        }

        return positions;
    }

    private static double fractionalPart(double x) {
        return x - Math.floor(x);
    }

    /** El i-esimo termino de la secuencia de Halton en la base dada, siempre en el rango [0, 1). */
    private static double halton(int index, int base) {
        double result = 0.0;
        double fraction = 1.0 / base;
        int i = index;

        while (i > 0) {
            result += fraction * (i % base);
            i /= base;
            fraction /= base;
        }

        return result;
    }
}
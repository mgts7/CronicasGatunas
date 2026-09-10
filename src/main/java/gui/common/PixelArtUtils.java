package gui.common;

import javafx.scene.canvas.GraphicsContext;

import java.util.Random;

/**
 * ============================================================
 * PixelArtUtils
 * ============================================================
 *
 * Utilidades compartidas por los distintos *Canvas de la GUI
 * (GridCanvas de Mission 1, y los que sigan para Missions 2-4) para
 * lograr un look "8-bit" consistente entre todas las misiones, sin
 * depender de ninguna libreria externa de graficos.
 */
public final class PixelArtUtils {

    private PixelArtUtils() {
        // Utility class: no se instancia.
    }

    /**
     * JavaFX suaviza (antialiasing) las formas por defecto, lo cual
     * arruina el look pixelado/retro. Llamar esto una vez al inicio
     * de cada render(), antes de dibujar cualquier tile.
     */
    public static void disableSmoothing(GraphicsContext gc) {
        gc.setImageSmoothing(false);
    }

    /**
     * Genera un Random DETERMINISTA a partir de dos enteros
     * (tipicamente fila y columna de una celda, o el indice de un
     * nodo). Es clave que sea determinista: si se usara
     * Math.random() directamente, la decoracion (posicion de
     * flores, forma del monticulo, etc.) cambiaria cada vez que
     * JavaFX vuelve a pintar la ventana (al hacer scroll, resize,
     * etc.), dando un efecto de parpadeo caotico. Con esta semilla,
     * la celda (r, c) siempre se ve exactamente igual.
     */
    public static Random deterministicRandom(int a, int b) {
        long seed = (((long) a) * 73856093L) ^ (((long) b) * 19349663L);
        return new Random(seed);
    }
}
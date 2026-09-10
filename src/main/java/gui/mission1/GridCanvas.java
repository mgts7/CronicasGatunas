package gui.mission1;

import gui.common.PixelArtUtils;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import model.grid.Grid;

import java.util.Random;

/**
 * ============================================================
 * GridCanvas - Mision 1
 * ============================================================
 *
 * Dibuja el grid del minefield con un estilo "8-bit" procedural:
 * cada celda se subdivide internamente en una cuadricula de
 * ART_RESOLUTION x ART_RESOLUTION "pixeles de arte", pintados con
 * fillRect(), sin usar ninguna imagen ni libreria externa.
 *
 * Tres variantes de celda:
 *   - Pasto normal: base verde con motas de textura + a veces una
 *     flor, en posicion/color variable por celda (pero determinista,
 *     ver PixelArtUtils.deterministicRandom).
 *   - Mina: pasto de fondo con un monticulo de tierra sospechoso
 *     (blob circular irregular) en el centro.
 *   - Camino recorrido: pasto aplastado (tono mas apagado/amarillento)
 *     con manchas oscuras simulando pisadas.
 *
 * Respeta el limite de dibujo de la seccion 2.3: grids de mas de
 * 50x50 no se dibujan (se devuelve false y el llamador debe mostrar
 * el mensaje "drawing omitted" en su lugar, junto con la respuesta
 * numerica igual).
 */
public final class GridCanvas extends Canvas {

    private static final int MAX_DRAWABLE_DIMENSION = 50;
    private static final int TILE_SIZE = 16;       // pixeles reales en pantalla por celda
    private static final int ART_RESOLUTION = 8;    // subdivision interna (8x8 "pixeles de arte")
    private static final double ART_PIXEL_SIZE = (double) TILE_SIZE / ART_RESOLUTION;

    // Paleta de colores del pasto
    private static final Color GRASS_BASE = Color.rgb(86, 150, 60);
    private static final Color GRASS_FLECK = Color.rgb(74, 133, 51);
    private static final Color GRASS_DARK = Color.rgb(58, 107, 40);

    // Colores de flores (variables por celda)
    private static final Color[] FLOWER_COLORS = {
            Color.rgb(255, 105, 180), // rosado
            Color.rgb(255, 223, 0),   // amarillo
            Color.rgb(240, 240, 240)  // blanco
    };

    // Paleta del monticulo de tierra (minas)
    private static final Color DIRT_BASE = Color.rgb(120, 90, 60);
    private static final Color DIRT_DARK = Color.rgb(90, 65, 40);
    private static final Color DIRT_SHADOW = Color.rgb(70, 50, 30);

    // Paleta del pasto aplastado (camino)
    private static final Color TRAMPLED_BASE = Color.rgb(168, 150, 80);
    private static final Color TRAMPLED_DARK = Color.rgb(128, 112, 58);
    private static final Color FOOTPRINT = Color.rgb(96, 82, 46);

    // Marcadores de inicio/fin
    private static final Color START_MARKER = Color.rgb(60, 140, 220);
    private static final Color END_MARKER = Color.rgb(230, 90, 60);

    public GridCanvas() {
        super();
    }

    /**
     * Dibuja el grid completo. Si algun path se pasa como null o
     * vacio, no se resalta ningun camino (solo se muestran pasto y
     * minas).
     *
     * @return true si se dibujo (dimensiones dentro del limite de
     *         la seccion 2.3), false si se omitio por ser demasiado
     *         grande (el llamador debe mostrar el mensaje correspondiente).
     */
    public boolean render(Grid grid, int startRow, int startCol, int endRow, int endCol, int[][] path) {
        int rows = grid.getRows();
        int cols = grid.getCols();

        if (rows > MAX_DRAWABLE_DIMENSION || cols > MAX_DRAWABLE_DIMENSION) {
            return false;
        }

        setWidth(cols * TILE_SIZE);
        setHeight(rows * TILE_SIZE);

        GraphicsContext gc = getGraphicsContext2D();
        PixelArtUtils.disableSmoothing(gc);
        gc.clearRect(0, 0, getWidth(), getHeight());

        boolean[][] onPath = buildPathMask(rows, cols, path);

        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                double tileX = c * TILE_SIZE;
                double tileY = r * TILE_SIZE;

                if (onPath[r][c]) {
                    drawTrampledTile(gc, tileX, tileY, r, c);
                } else if (grid.isBomb(r, c)) {
                    drawMineTile(gc, tileX, tileY, r, c);
                } else {
                    drawGrassTile(gc, tileX, tileY, r, c);
                }
            }
        }

        drawMarker(gc, startCol * TILE_SIZE, startRow * TILE_SIZE, START_MARKER);
        drawMarker(gc, endCol * TILE_SIZE, endRow * TILE_SIZE, END_MARKER);

        return true;
    }

    private boolean[][] buildPathMask(int rows, int cols, int[][] path) {
        boolean[][] mask = new boolean[rows][cols];
        if (path == null) {
            return mask;
        }
        for (int[] cell : path) {
            mask[cell[0]][cell[1]] = true;
        }
        return mask;
    }

    /**
     * Pasto normal: fondo verde con motas de textura distribuidas al
     * azar (determinista), y con cierta probabilidad, una flor de
     * color y posicion variables.
     */
    private void drawGrassTile(GraphicsContext gc, double tileX, double tileY, int row, int col) {
        Random rng = PixelArtUtils.deterministicRandom(row, col);

        for (int i = 0; i < ART_RESOLUTION; i++) {
            for (int j = 0; j < ART_RESOLUTION; j++) {
                double roll = rng.nextDouble();
                Color color;
                if (roll < 0.65) {
                    color = GRASS_BASE;
                } else if (roll < 0.9) {
                    color = GRASS_FLECK;
                } else {
                    color = GRASS_DARK;
                }
                fillArtPixel(gc, tileX, tileY, i, j, color);
            }
        }

        // ~40% de las celdas de pasto tienen una flor.
        if (rng.nextDouble() < 0.4) {
            int flowerI = 1 + rng.nextInt(ART_RESOLUTION - 2);
            int flowerJ = 1 + rng.nextInt(ART_RESOLUTION - 2);
            Color flowerColor = FLOWER_COLORS[rng.nextInt(FLOWER_COLORS.length)];

            // Centro de la flor + 4 petalos ortogonales, todo dentro del tile.
            fillArtPixel(gc, tileX, tileY, flowerI, flowerJ, flowerColor);
            fillArtPixel(gc, tileX, tileY, flowerI - 1, flowerJ, flowerColor);
            fillArtPixel(gc, tileX, tileY, flowerI + 1, flowerJ, flowerColor);
            fillArtPixel(gc, tileX, tileY, flowerI, flowerJ - 1, flowerColor);
            fillArtPixel(gc, tileX, tileY, flowerI, flowerJ + 1, flowerColor);
        }
    }

    /**
     * Mina: pasto de fondo (mismo patron que drawGrassTile, misma
     * semilla para que el resto de la celda luzca consistente) con
     * un monticulo de tierra sospechoso en el centro: un blob
     * circular irregular, mas oscuro en el borde inferior para dar
     * sensacion de volumen.
     */
    private void drawMineTile(GraphicsContext gc, double tileX, double tileY, int row, int col) {
        Random rng = PixelArtUtils.deterministicRandom(row, col);

        // Fondo de pasto (version simplificada, sin flores: séria
        // raro decorar con flores la celda que esconde la mina).
        for (int i = 0; i < ART_RESOLUTION; i++) {
            for (int j = 0; j < ART_RESOLUTION; j++) {
                Color color = (rng.nextDouble() < 0.75) ? GRASS_BASE : GRASS_FLECK;
                fillArtPixel(gc, tileX, tileY, i, j, color);
            }
        }

        // Centro y radio del monticulo, con variacion leve por celda.
        double centerI = ART_RESOLUTION / 2.0 + (rng.nextDouble() - 0.5);
        double centerJ = ART_RESOLUTION / 2.0 + (rng.nextDouble() - 0.5);
        double radius = 2.4 + rng.nextDouble() * 0.8;

        for (int i = 0; i < ART_RESOLUTION; i++) {
            for (int j = 0; j < ART_RESOLUTION; j++) {
                double dist = Math.hypot(i - centerI, j - centerJ);
                // Ruido leve en el borde para que el blob no sea un circulo perfecto.
                double noise = (rng.nextDouble() - 0.5) * 0.6;
                if (dist + noise < radius) {
                    // Parte inferior del monticulo mas oscura: sensacion de sombra/volumen.
                    Color dirtColor = (j > centerJ) ? DIRT_SHADOW : (dist < radius * 0.55 ? DIRT_DARK : DIRT_BASE);
                    fillArtPixel(gc, tileX, tileY, i, j, dirtColor);
                }
            }
        }
    }

    /**
     * Camino recorrido: pasto aplastado. En vez del verde vivo, un
     * tono apagado amarillento (como pasto pisoteado), con algunas
     * manchas oscuras simulando pisadas puntuales.
     */
    private void drawTrampledTile(GraphicsContext gc, double tileX, double tileY, int row, int col) {
        Random rng = PixelArtUtils.deterministicRandom(row * 31, col * 17); // semilla distinta a la del pasto normal

        for (int i = 0; i < ART_RESOLUTION; i++) {
            for (int j = 0; j < ART_RESOLUTION; j++) {
                double roll = rng.nextDouble();
                Color color;
                if (roll < 0.15) {
                    color = FOOTPRINT;
                } else if (roll < 0.55) {
                    color = TRAMPLED_DARK;
                } else {
                    color = TRAMPLED_BASE;
                }
                fillArtPixel(gc, tileX, tileY, i, j, color);
            }
        }
    }

    /** Dibuja un "pixel de arte" (subcelda) en la posicion (artRow, artCol) dentro del tile en (tileX, tileY). */
    private void fillArtPixel(GraphicsContext gc, double tileX, double tileY, int artRow, int artCol, Color color) {
        if (artRow < 0 || artRow >= ART_RESOLUTION || artCol < 0 || artCol >= ART_RESOLUTION) {
            return; // fuera del tile (puede pasar con petalos de flores cerca del borde)
        }
        gc.setFill(color);
        gc.fillRect(tileX + artCol * ART_PIXEL_SIZE, tileY + artRow * ART_PIXEL_SIZE, ART_PIXEL_SIZE, ART_PIXEL_SIZE);
    }

    /** Dibuja un marcador circular simple sobre el centro de una celda (usado para start/end). */
    private void drawMarker(GraphicsContext gc, double tileX, double tileY, Color color) {
        gc.setFill(color);
        double margin = TILE_SIZE * 0.22;
        gc.fillOval(tileX + margin, tileY + margin, TILE_SIZE - 2 * margin, TILE_SIZE - 2 * margin);
    }

    /** El tamano de tile usado, por si el contenedor (Mission1View) necesita calcular dimensiones. */
    public static int getTileSize() {
        return TILE_SIZE;
    }

    public static int getMaxDrawableDimension() {
        return MAX_DRAWABLE_DIMENSION;
    }
}
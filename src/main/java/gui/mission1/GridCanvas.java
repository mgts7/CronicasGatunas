package gui.mission1;

import gui.common.PixelArtUtils;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import model.grid.Grid;

import java.util.Arrays;
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
 *   - Camino recorrido: piedras de paso. Pasto de fondo (igual que
 *     una celda de pasto normal, sin flor) con una losa de piedra
 *     individual, de esquinas redondeadas, centrada en la celda y
 *     con un margen de pasto visible alrededor - entre dos losas
 *     vecinas del camino queda una franja de pasto entre ambas.
 *
 * Los puntos de inicio y fin no son circulos lisos: son un gatito
 * pixel art naranja (inicio) y uno blanco (fin), en una cuadricula
 * de 16x16 "pixeles de gato" (el doble de resolucion lineal que el
 * resto del arte de la celda), generados por codigo a partir de un
 * perfil de silueta simetrico en vez de una plantilla escrita a mano.
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

    // Paleta de las piedras de paso (camino)
    private static final Color STONE_LIGHT = Color.rgb(176, 176, 182);
    private static final Color STONE_BASE = Color.rgb(148, 148, 154);
    private static final Color STONE_DARK = Color.rgb(112, 112, 118);

    // --- Gatos (marcadores de inicio/fin), en su propia resolucion mas fina ---
    private static final int CAT_RESOLUTION = 16;   // 16x16: el doble de resolucion lineal que un tile normal
    private static final double CAT_PIXEL_SIZE = (double) TILE_SIZE / CAT_RESOLUTION;
    private static final char[][] CAT_SPRITE = buildCatSprite();
    private static final Color NOSE_COLOR = Color.rgb(255, 150, 180);

    // Gato naranja: marca la casilla de inicio.
    private static final Color CAT_ORANGE_OUTLINE = Color.rgb(90, 55, 20);
    private static final Color CAT_ORANGE_FUR = Color.rgb(255, 140, 40);
    private static final Color CAT_ORANGE_FUR_SHADE = Color.rgb(225, 110, 25);
    private static final Color CAT_ORANGE_EYE = Color.rgb(70, 190, 90);

    // Gato blanco: marca la casilla de fin.
    private static final Color CAT_WHITE_OUTLINE = Color.rgb(140, 140, 145);
    private static final Color CAT_WHITE_FUR = Color.rgb(248, 248, 250);
    private static final Color CAT_WHITE_FUR_SHADE = Color.rgb(212, 214, 220);
    private static final Color CAT_WHITE_EYE = Color.rgb(90, 160, 220);

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
                    drawStoneTile(gc, tileX, tileY, r, c);
                } else if (grid.isBomb(r, c)) {
                    drawMineTile(gc, tileX, tileY, r, c);
                } else {
                    drawGrassTile(gc, tileX, tileY, r, c);
                }
            }
        }

        drawCatMarker(gc, startCol * TILE_SIZE, startRow * TILE_SIZE,
                CAT_ORANGE_OUTLINE, CAT_ORANGE_FUR, CAT_ORANGE_FUR_SHADE, CAT_ORANGE_EYE);
        drawCatMarker(gc, endCol * TILE_SIZE, endRow * TILE_SIZE,
                CAT_WHITE_OUTLINE, CAT_WHITE_FUR, CAT_WHITE_FUR_SHADE, CAT_WHITE_EYE);

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
        paintGrassBackground(gc, tileX, tileY, rng);

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

    /** Pinta solo la base de pasto (sin flor) de una celda, usando el rng ya posicionado que le pasen. */
    private void paintGrassBackground(GraphicsContext gc, double tileX, double tileY, Random rng) {
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
     * Camino recorrido: piedra de paso individual. Primero se pinta
     * pasto de fondo igual que una celda de pasto normal (misma
     * semilla que usaria drawGrassTile para esa posicion, sin flor),
     * y encima una losa cuadrada de esquinas redondeadas, centrada en
     * la celda y con un margen de un pixel de arte dejando ver el
     * pasto alrededor - asi entre dos losas vecinas del camino queda
     * una franja de pasto visible entre ambas, como piedras de paso
     * sobre el pasto en vez de un sendero solido.
     */
    private void drawStoneTile(GraphicsContext gc, double tileX, double tileY, int row, int col) {
        Random grassRng = PixelArtUtils.deterministicRandom(row, col);
        paintGrassBackground(gc, tileX, tileY, grassRng);

        Random stoneRng = PixelArtUtils.deterministicRandom(row * 31, col * 17);
        int lo = 1;
        int hi = ART_RESOLUTION - 1 - lo; // 6, con ART_RESOLUTION = 8

        for (int i = lo; i <= hi; i++) {
            for (int j = lo; j <= hi; j++) {
                boolean corner = (i == lo || i == hi) && (j == lo || j == hi);
                if (corner) {
                    continue; // esquina redondeada: se deja ver el pasto de fondo
                }
                Color color;
                if (i == lo || j == lo) {
                    color = STONE_LIGHT; // borde superior/izquierdo, "iluminado"
                } else if (i == hi || j == hi) {
                    color = STONE_DARK; // borde inferior/derecho, en sombra: da sensacion de volumen
                } else {
                    double roll = stoneRng.nextDouble();
                    if (roll < 0.25) {
                        color = STONE_LIGHT;
                    } else if (roll < 0.8) {
                        color = STONE_BASE;
                    } else {
                        color = STONE_DARK;
                    }
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

    /**
     * Dibuja el gatito pixel art (inicio o fin) sobre el centro de una
     * celda, en la cuadricula CAT_SPRITE (16x16) y su propia escala
     * CAT_PIXEL_SIZE. Los pixeles '.' de la plantilla se saltan,
     * dejando ver el fondo de la celda (pasto o piedra) debajo del gato.
     */
    private void drawCatMarker(GraphicsContext gc, double tileX, double tileY,
                               Color outline, Color fur, Color furShade, Color eye) {
        for (int i = 0; i < CAT_RESOLUTION; i++) {
            for (int j = 0; j < CAT_RESOLUTION; j++) {
                char symbol = CAT_SPRITE[i][j];
                Color color;
                switch (symbol) {
                    case 'O':
                        color = outline;
                        break;
                    case 'F':
                        // Textura sutil de pelaje: alterna tono para dar sensacion de volumen.
                        color = ((i + j) % 3 == 0) ? furShade : fur;
                        break;
                    case 'E':
                        color = Color.rgb(250, 250, 250); // blanco del ojo
                        break;
                    case 'P':
                        color = eye; // pupila (color de ojo variable por gato)
                        break;
                    case 'N':
                        color = NOSE_COLOR;
                        break;
                    default:
                        continue; // '.' -> deja ver el fondo de la celda
                }
                double px = tileX + j * CAT_PIXEL_SIZE;
                double py = tileY + i * CAT_PIXEL_SIZE;
                gc.setFill(color);
                gc.fillRect(px, py, CAT_PIXEL_SIZE, CAT_PIXEL_SIZE);
            }
        }
    }

    /**
     * Genera la silueta del gato en una cuadricula de 16x16, simetrica
     * izquierda-derecha por construccion (en vez de escribirla pixel a
     * pixel a mano): orejas triangulares en las filas superiores,
     * cabeza ovalada por debajo con un perfil de margenes por fila, y
     * ojos/nariz superpuestos en coordenadas fijas.
     */
    private static char[][] buildCatSprite() {
        int n = CAT_RESOLUTION;
        char[][] grid = new char[n][n];
        for (char[] rowArr : grid) {
            Arrays.fill(rowArr, '.');
        }

        // Orejas (filas 0-3): rango de columnas de la oreja izquierda por fila;
        // la oreja derecha es el espejo respecto al centro de la cuadricula.
        int[][] earColumnRanges = {
                {2, 3},
                {1, 4},
                {0, 5},
                {0, 6}
        };
        for (int r = 0; r < earColumnRanges.length; r++) {
            int start = earColumnRanges[r][0];
            int end = earColumnRanges[r][1];
            for (int c = start; c <= end; c++) {
                grid[r][c] = 'F';
                grid[r][n - 1 - c] = 'F';
            }
            grid[r][start] = 'O';
            grid[r][n - 1 - start] = 'O';
            if (r == 0) {
                // Punta de la oreja: fila muy angosta, se pinta entera como contorno.
                grid[r][end] = 'O';
                grid[r][n - 1 - end] = 'O';
            }
        }

        // Cabeza (filas 4-15): margen de fondo por fila (igual a ambos lados,
        // por eso sale simetrica), mas angosta arriba y abajo, mas ancha al medio.
        int[] headMarginByRow = {2, 1, 0, 0, 0, 0, 0, 1, 2, 3, 5, 7};
        for (int i = 0; i < headMarginByRow.length; i++) {
            int r = 4 + i;
            int margin = headMarginByRow[i];
            for (int c = margin; c <= n - 1 - margin; c++) {
                grid[r][c] = 'F';
            }
            grid[r][margin] = 'O';
            grid[r][n - 1 - margin] = 'O';
        }

        // Ojos: bloque blanco de 2x2 por ojo (filas 7-8) mas una pupila.
        int[] leftEyeCols = {4, 5};
        int[] rightEyeCols = {10, 11};
        for (int c : leftEyeCols) {
            grid[7][c] = 'E';
            grid[8][c] = 'E';
        }
        for (int c : rightEyeCols) {
            grid[7][c] = 'E';
            grid[8][c] = 'E';
        }
        grid[8][5] = 'P';
        grid[8][10] = 'P';

        // Nariz: 2x2 centrada.
        grid[11][7] = 'N';
        grid[11][8] = 'N';
        grid[12][7] = 'N';
        grid[12][8] = 'N';

        return grid;
    }

    /** El tamano de tile usado, por si el contenedor (Mission1View) necesita calcular dimensiones. */
    public static int getTileSize() {
        return TILE_SIZE;
    }

    public static int getMaxDrawableDimension() {
        return MAX_DRAWABLE_DIMENSION;
    }
}
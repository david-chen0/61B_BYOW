package byow.lab12;
import org.junit.Test;
import static org.junit.Assert.*;

import byow.TileEngine.TERenderer;
import byow.TileEngine.TETile;
import byow.TileEngine.Tileset;

import java.util.Random;

/**
 * Draws a world consisting of hexagonal regions.
 */
public class HexWorld {
    private static final int WIDTH = 40;
    private static final int HEIGHT = 54;
    private static final long SEED = (long) (10000000 * Math.random());
    private static final Random RANDOM = new Random(SEED);

    private static int rowWidth(int size, int rowNum) {
        return size + (2 * leftMost(size, rowNum));
    }

    private static int leftMost(int size, int rowNum) {
        if (rowNum >= size) {
            rowNum = (2 * size) - rowNum - 1;
        }
        return rowNum;
    }

    private static void addRow(TETile[][] world, int x, int y, int width, TETile tile) {
        for (int i = 0; i < width; i++) {
            world[x + i][y] = tile;
        }
    }

    private static void addHexagon(TETile[][] world, int x, int y, int size, TETile tile) {
        for (int i = 0; i < 2 * size; i++) {
            addRow(world, x - leftMost(size, i), y + i, rowWidth(size, i), tile);
        }
    }

    private static TETile randomTile() {
        int tileNum = RANDOM.nextInt(6);
        return switch (tileNum) {
            case 0 -> Tileset.SAND;
            case 1 -> Tileset.FLOWER;
            case 2 -> Tileset.TREE;
            case 3 -> Tileset.MOUNTAIN;
            case 4 -> Tileset.GRASS;
            case 5 -> Tileset.WALL;
            default -> Tileset.NOTHING;
        };
    }

    private static void draw(TETile[][] world, int x, int y, int size, int numHex) {
        TETile tile = randomTile();
        for (int i = 0; i < numHex; i++) {
            y += 2 * size;
            addHexagon(world, x, y, size, tile);
            tile = randomTile();
        }
    }

    private static int[] topRight(int x, int y, int size) {
        return new int[]{(2 * size) + x - 1, y + size};
    }

    private static int[] botRight(int x, int y, int size) {
        return new int[]{(2 * size) + x - 1, y - size};
    }

    @Test
    public void testWidth() {
        assertEquals(2, rowWidth(2, 0));
        assertEquals(5, rowWidth(3, 1));
        assertEquals(8, rowWidth(4, 5));
        assertEquals(11, rowWidth(5, 6));
    }

    public static void main(String[] args) {
        TERenderer ter = new TERenderer();
        ter.initialize(WIDTH, HEIGHT);
        TETile[][] world = new TETile[WIDTH][HEIGHT];
        for (int i = 0; i < WIDTH; i++) {
            for (int j = 0; j < HEIGHT; j++) {
                world[i][j] = Tileset.NOTHING;
            }
        }
        int size = 4; /* Anything up to size 4 fits on a 40x48 graph */
        int[] currentPos = new int[]{size, 2 * size};
        for (int i = 3; i < 5; i++) {
            draw(world, currentPos[0], currentPos[1], size, i);
            currentPos = botRight(currentPos[0], currentPos[1], size);
        }
        for (int i = 5; i > 2; i--) {
            draw(world, currentPos[0], currentPos[1], size, i);
            currentPos = topRight(currentPos[0], currentPos[1], size);
        }
        ter.renderFrame(world);
    }

}

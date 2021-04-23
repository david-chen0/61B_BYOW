package byow.Core;

import byow.TileEngine.TERenderer;
import byow.TileEngine.TETile;
import byow.TileEngine.Tileset;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;

public class Engine {
    private TETile[][] world = new TETile[WIDTH][HEIGHT];
    private TERenderer ter = new TERenderer();
    public static final int WIDTH = 80;
    public static final int HEIGHT = 40;

    private static final TETile BACKGROUND = Tileset.LAVA;
    private  static final TETile portTile = Tileset.FLOWER;
    private int[] firstPort;
    private int[] secondPort;
    private ArrayList<Space> SPACES;
    private int[] playerLocation;
    private Space ROOT;
    private Random RANDOM;
    private long seed = 0;
    private boolean[][] checker = new boolean[WIDTH][HEIGHT];

    /**
     * Method used for exploring a fresh world. This method should handle all inputs,
     * including inputs from the main menu.
     */


    /**
     * Method used for autograding and testing your code. The input string will be a series
     * of characters (for example, "n123sswwdasdassadwas", "n123sss:q", "lwww". The engine should
     * behave exactly as if the user typed these characters into the engine using
     * interactWithKeyboard.
     *
     * Recall that strings ending in ":q" should cause the game to quite save. For example,
     * if we do interactWithInputString("n123sss:q"), we expect the game to run the first
     * 7 commands (n123sss) and then quit and save. If we then do
     * interactWithInputString("l"), we should be back in the exact same state.
     *
     * In other words, both of these calls:
     *   - interactWithInputString("n123sss:q")
     *   - interactWithInputString("lww")
     *
     * should yield the exact same world state as:
     *   - interactWithInputString("n123sssww")
     *
     * @param input the input string to feed to your program
     * @return the 2D TETile[][] representing the state of the world
     */
    public TETile[][] interactWithInputString(String input) {
        genBackground(world);
        if (Character.toLowerCase(input.charAt(0)) == 'n') {
            int index = 1;
            while (Character.toLowerCase(input.charAt(index)) != 's') {
                index++;
            }
            seed = Long.parseLong(input.substring(1, index));
            RANDOM = new Random(seed);
            genWorld(world);
            loadMov(input.substring(index + 1));
            ter.initialize(WIDTH, HEIGHT, 0, 0);
            ter.renderFrame(world);
        } else if (Character.toLowerCase(input.charAt(0)) == 'l') {
            Persistence saveUtil = new Persistence();
            Engine en = new Engine();
            saveUtil.load(en);
            en.loadMov(input.substring(1));
            en.ter.renderFrame(en.world);
            world = en.world;
        }
        return world;
    }

    private void loadMov(String typedKeys) {
        Persistence saveUtil = new Persistence();
        for (char key : typedKeys.toCharArray()) {
            switch (Character.toLowerCase(key)) {
                case ':':
                    saveUtil.save(seed, typedKeys.substring(2),
                            playerLocation[0], playerLocation[1]);
                    break;
                case 'a':
                    movePlayer(playerLocation[0] - 1,
                        playerLocation[1]);
                    break;
                case 's':
                    movePlayer(playerLocation[0],
                            playerLocation[1] - 1);
                    break;
                case 'd':
                    movePlayer(playerLocation[0] + 1,
                            playerLocation[1]);
                    break;
                case 'w':
                    movePlayer(playerLocation[0],
                            playerLocation[1] + 1);
                    break;
                default:
                    break;
            }
        }
    }

    /**
     * @source Binary Space Partitioning idea:
     * www.roguebasin.com/index.php?title=Basic_BSP_Dungeon_generation
     * gamedevelopment.tutsplus.com/
     * tutorials/how-to-use-bsp-trees-to-generate-game-maps--gamedev-12268
     */
    private class Space {
        private final int leftX, width, height, botY;
        private Space leftChild, rightChild;
        private static final int MIN_SIZE = 6;
        private int xMid;
        private int yMid;

        private Space(int lX, int bY, int w, int h) {
            leftX = lX;
            botY = bY;
            width = w;
            height = h;
        }

        private boolean split() {
            //Check if split already
            if (leftChild != null || rightChild != null) {
                return false;
            }

            //Split direction: true for horizontal, false for vertical
            boolean horizontal = RandomUtils.bernoulli(RANDOM);

            //Set divider
            int divider;
            if (horizontal) {
                int max = height - MIN_SIZE;
                if (max <= MIN_SIZE) {
                    return false;
                }
                divider = RandomUtils.uniform(RANDOM, MIN_SIZE, max);
                leftChild = new Space(leftX, botY, width, divider);
                rightChild = new Space(leftX, botY + divider, width, height - divider);
            } else {
                int max = width - MIN_SIZE;
                if (max <= MIN_SIZE) {
                    return false;
                }
                divider = RandomUtils.uniform(RANDOM, MIN_SIZE, max);
                leftChild = new Space(leftX, botY, divider, height);
                rightChild = new Space(leftX + divider, botY, width - divider, height);
            }
            return true;
        }
    }

    private void genSpace() {
        SPACES = new ArrayList<Space>();
        ROOT = new Space(0, 2, WIDTH, HEIGHT - 2);
        SPACES.add(ROOT);
        int roomNum = RandomUtils.uniform(RANDOM, 20, 40);
        while (SPACES.size() < roomNum) {
            Space temp = SPACES.get(RandomUtils.uniform(RANDOM, SPACES.size()));
            if (temp.split()) {
                SPACES.add(temp.rightChild);
                SPACES.add(temp.leftChild);
            }
        }
    }

    private static void genBackground(TETile[][] world) {
        for (int i = 0; i < world.length; i++) {
            for (int j = 0; j < world[i].length; j++) {
                world[i][j] = BACKGROUND;
            }
        }
    }

    private void fillChecker() {
        for (int i = 0; i < checker.length; i++) {
            for (int j = 0; j < checker[i].length; j++) {
                checker[i][j] = false;
            }
        }
    }

    private void genWorld(TETile[][] worl) {
        genBackground(worl);
        genSpace();
        fillChecker();
        roomHelper(ROOT, worl);
        roomHallways(ROOT, worl);
        genType(worl, Tileset.WALL, Tileset.LOCKED_DOOR);
        if (playerLocation == null) {
            playerLocation = genType(worl, Tileset.FLOOR, Tileset.AVATAR);
        }
        firstPort = genType(worl, Tileset.WALL, Tileset.FLOWER);
        secondPort = genType(worl, Tileset.WALL, Tileset.FLOWER);
    }

    private void roomHallways(Space room, TETile[][] worl) {
        if (room.leftChild == null) {
            return;
        }
        int[] leftConnector = bottomLevel(room.leftChild);
        int[] rightConnector = bottomLevel(room.rightChild);
        genHallway(true, leftConnector, rightConnector, worl);
        if (leftConnector[1] > rightConnector[1]) {
            int[] temp = leftConnector;
            leftConnector = rightConnector;
            rightConnector = temp;
        }
        genHallway(false, leftConnector, rightConnector, worl);
        roomHallways(room.leftChild, worl);
        roomHallways(room.rightChild, worl);
    }

    /**
     *Generate Hallway of specific orientation at given position in the given world
     *
     * @param orientation true for horizontal, false for vertical
     * @param pos Array of size 2, with pos[0] representing x, pos[1] representing y
     * @param goal
     * @param worl specified world (TETile[][]) to generate room on
     */
    private void genHallway(boolean orientation, int[] pos, int[] goal, TETile[][] worl) {
        if (orientation) {
            int y = pos[1];
            int lower = Math.max(0, y - 1);
            int upper = Math.min(HEIGHT - 1, y + 1);
            int end = goal[0];
            if (end < WIDTH && worl[end][y].equals(BACKGROUND)) {
                end++;
            }
            for (int i = pos[0]; i < end + 1; i++) {
                if (!checker[i][y] || worl[i][lower].equals(BACKGROUND)) {
                    worl[i][lower] = Tileset.WALL;
                    checker[i][lower] = true;
                }
                if (!checker[i][y] || worl[i][upper].equals(BACKGROUND)) {
                    worl[i][upper] = Tileset.WALL;
                    checker[i][upper] = true;
                }
                worl[i][y] = Tileset.FLOOR;
                checker[i][y] = true;
            }
            pos[0] = end;
        } else {
            int x = pos[0];
            int left = Math.max(0, x - 1);
            int right = Math.min(WIDTH - 1, x + 1);
            int end = goal[1];
            if (end < HEIGHT && worl[x][end].equals(BACKGROUND)) {
                end++;
            }
            for (int i = pos[1]; i < end + 1; i++) {
                if (!checker[x][i] || worl[left][i].equals(BACKGROUND)) {
                    worl[left][i] = Tileset.WALL;
                    checker[left][i] = true;
                }
                if (!checker[x][i] || worl[right][i].equals(BACKGROUND)) {
                    worl[right][i] = Tileset.WALL;
                    checker[right][i] = true;
                }
                worl[x][i] = Tileset.FLOOR;
                checker[x][i] = true;
            }
            pos[1] = end;
        }
    }

    private void roomHelper(Space space, TETile[][] worl) {
        if (space.leftChild != null) {
            roomHelper(space.leftChild, worl);
            roomHelper(space.rightChild, worl);
        } else {
            //Random Dimensions
            int w = Math.max(4, RandomUtils.uniform(RANDOM, space.width));
            int h = Math.max(4, RandomUtils.uniform(RANDOM, space.height));
            //Random position
            int x =  space.leftX + RandomUtils.uniform(RANDOM, space.width - w);
            int y =  space.botY + RandomUtils.uniform(RANDOM, space.height - h);
            int[] pos = new int[]{x, y};
            space.xMid = x + (w / 2);
            space.yMid = y + (h / 2);
            genRoom(w, h, pos, worl);
        }
    }

    private int[] bottomLevel(Space room) {
        Space temp = room;
        while (temp.leftChild != null) {
            if (RandomUtils.bernoulli(RANDOM)) {
                temp = temp.leftChild;
            } else {
                temp = temp.rightChild;
            }
        }
        return new int[]{temp.xMid, temp.yMid};
    }

    /**
     Create a random room on the given world without filling
     existing tiles (all tiles not BACKGROUND).

     @param width desired width of room
     @param height desired height of room
     @param worl specified world (TeTile[][]) to generate room on
     */
    private void genRoom(int width, int height, int[] pos, TETile[][] worl) {
        for (int i = pos[0]; i < pos[0] + width; i++) {
            TETile temp;
            if (i == pos[0] || i == pos[0] + width - 1) {
                temp = Tileset.WALL;
            } else {
                temp = Tileset.FLOOR;
            }
            for (int j = pos[1]; j < pos[1] + height; j++) {
                if (j == pos[1] || j == pos[1] + height - 1) {
                    worl[i][j] = Tileset.WALL;
                } else {
                    worl[i][j] = temp;
                }
                checker[i][j] = true;
            }
        }
    }

    private int[] genType(TETile[][] worl, TETile target, TETile tile) {
        ArrayList<Integer> temp = new ArrayList<>();
        for (int i = 0; i < worl.length; i++) {
            for (int j = 0; j < worl[i].length; j++) {
                if (worl[i][j].equals(target)) {
                    temp.add(i);
                    temp.add(j);
                }
            }
        }
        int rand = RANDOM.nextInt(temp.size() / 2);
        int[] result = new int[]{temp.get(2 * rand), temp.get(1 + (2 * rand))};
        worl[result[0]][result[1]] = tile;
        return result;
    }

    public void movePlayer(int x, int y) {
        if (x > WIDTH || x < 0 || y > HEIGHT || y < 0 || world[x][y].equals(Tileset.WALL)) {
            return;
        } else if (world[x][y].equals(Tileset.LAVA)) {
            Interactivity.endGame();
            return;
        } else if (world[x][y].equals(Tileset.LOCKED_DOOR)
                || world[x][y].equals(Tileset.UNLOCKED_DOOR)) {
            world[x][y] = Tileset.UNLOCKED_DOOR;
            Interactivity.win();
            return;
        } else if (world[x][y].equals(portTile)) {
            int[] temp = new int[]{x, y};
            if (Arrays.equals(temp, firstPort)) {
                temp = secondPort;
            } else {
                temp = firstPort;
            }
            world[temp[0]][temp[1]] = Tileset.FLOOR;
            world[x][y] = Tileset.FLOOR;
            movePlayer(temp[0], temp[1]);
            return;
        }
        world[playerLocation[0]][playerLocation[1]] = Tileset.FLOOR;
        world[x][y] = Tileset.AVATAR;
        playerLocation = new int[]{x, y};
    }

    public long getSeed() {
        return seed;
    }

    public TETile[][] getWorld() {
        return world;
    }

    public TERenderer getTer() {
        return ter;
    }

    public int[] getPlayerLocation() {
        return playerLocation;
    }
}

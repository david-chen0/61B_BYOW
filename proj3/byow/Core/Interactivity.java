package byow.Core;

import byow.InputDemo.InputSource;
import byow.InputDemo.KeyboardInputSource;
import byow.TileEngine.TERenderer;
import byow.TileEngine.TETile;
import byow.TileEngine.Tileset;
import edu.princeton.cs.introcs.StdDraw;

import java.awt.Font;
import java.awt.Color;

public class Interactivity {

    private static final Font FONT = new Font("Sans Serif", Font.PLAIN, 24);
    private static final int OFFSET = 2;
    private static String name;
    private long seed;
    private String replay = "";

    public void interactWithKeyboard(Engine engine) {
        initStdDraw();
        char typed = ' ';
        while (!(typed == 'n' || typed == 'l'
                || typed == 'o' || typed == 'q')) {
            if (StdDraw.hasNextKeyTyped()) {
                typed = StdDraw.nextKeyTyped();
                Character.toLowerCase(typed);
            }
            drawOptions();
        }

        Persistence saveUtil = new Persistence();

        if (typed == 'n') {
            drawFrame("Please enter seed");
            seed = parseSeed();
            engine.interactWithInputString("n" + seed + "s");
            saveUtil.clean();
            game(engine, saveUtil);
        } else if (typed == 'l') {
            saveUtil.load(engine);
            seed = engine.getSeed();
            game(engine, saveUtil);
        } else if (typed == 'o') {
            giveName(engine);
        } else {
            System.exit(0);
        }
    }

    private void game(Engine engine, Persistence saveUtil) {
        String avatar;
        if (name == null) {
            avatar = "Player";
        } else {
            avatar = name;
        }
        initStdDraw();
        InputSource input = new KeyboardInputSource();
        while (true) {
            StdDraw.setPenColor(Color.WHITE);
            StdDraw.textLeft(0, Engine.HEIGHT + 1, "Name: " + avatar);
            StdDraw.text(Engine.WIDTH / 2, Engine.HEIGHT + 1,
                    "Press :Q to save and exit");
            StdDraw.textRight(Engine.WIDTH, Engine.HEIGHT + 1,
                    "Current Tile: " + currentTile(engine));
            StdDraw.show();
            TETile[][] world = engine.getWorld();
            TERenderer ter = engine.getTer();
            if (StdDraw.hasNextKeyTyped()) {
                char key = input.getNextKey();
                int[] location = engine.getPlayerLocation();
                switch (Character.toLowerCase(key)) {
                    case ':':
                        StdDraw.text(1, 1, ":");
                        StdDraw.show();
                        char next = Character.toLowerCase(input.getNextKey());
                        StdDraw.text(2, 1, Character.toString(next));
                        StdDraw.show();
                        StdDraw.pause(20);
                        if (next == 'q') {
                            saveUtil.save(seed, replay,
                                    location[0], location[1]);
                            System.exit(0);
                        } else if (next == 'r') {
                            saveUtil.startReplay(new Engine());
                        } else {
                            StdDraw.clear();
                            ter.renderFrame(world);
                            game(engine, saveUtil);
                        }
                        break;
                    case 'a':
                        engine.movePlayer(location[0] - 1, location[1]);
                        replay += "a";
                        break;
                    case 's':
                        engine.movePlayer(location[0], location[1] - 1);
                        replay += "s";
                        break;
                    case 'd':
                        engine.movePlayer(location[0] + 1, location[1]);
                        replay += "d";
                        break;
                    case 'w':
                        engine.movePlayer(location[0], location[1] + 1);
                        replay += "w";
                        break;
                    default:
                        break;
                }
            } else if (StdDraw.isMousePressed()) {
                int[] pos = new int[]{(int) StdDraw.mouseX(), (int) StdDraw.mouseY()};
                TETile current = world[pos[0]][pos[1]];
                if (current.description().equals(Tileset.WALL.description())) {
                    world[pos[0]][pos[1]] = Tileset.FLOOR;
                }
            } else {
                ter.renderFrame(world);
            }
        }
    }

    public static void endGame() {
        StdDraw.clear();
        StdDraw.setPenColor(Color.RED);
        StdDraw.setFont(new Font("Arial", Font.PLAIN, 50));
        StdDraw.text(40, 20, "You died");
        StdDraw.setFont(new Font("Arial", Font.PLAIN, 20));
        StdDraw.text(40, 15, "(R)espawn");
        StdDraw.show();
        while (true) {
            if (StdDraw.hasNextKeyTyped()) {
                if (Character.toLowerCase(StdDraw.nextKeyTyped()) == 'r') {
                    initStdDraw();
                    return;
                }
            }
        }
    }

    public static void win() {
        StdDraw.clear();
        StdDraw.setPenColor(Color.RED);
        StdDraw.setFont(new Font("Arial", Font.PLAIN, 50));
        StdDraw.text(40, 20, "You win");
        StdDraw.setFont(new Font("Arial", Font.PLAIN, 20));
        StdDraw.text(40, 15, "(R)estart");
        StdDraw.show();
        while (true) {
            if (StdDraw.hasNextKeyTyped()) {
                if (Character.toLowerCase(StdDraw.nextKeyTyped()) == 'r') {
                    initStdDraw();
                    return;
                }
            }
        }
    }

    private static String currentTile(Engine engine) {
        try {
            int[] pos = new int[]{(int) StdDraw.mouseX(), (int) StdDraw.mouseY()};
            TETile current = engine.getWorld()[pos[0]][pos[1]];
            return current.description();
        } catch (ArrayIndexOutOfBoundsException e) {
            return "nothing";
        }
    }

    private static void initStdDraw() {
        StdDraw.setCanvasSize(16 * Engine.WIDTH, 16 * (Engine.HEIGHT + OFFSET));
        StdDraw.setXscale(0, Engine.WIDTH);
        StdDraw.setYscale(0, Engine.HEIGHT + OFFSET);
        StdDraw.setPenColor(Color.WHITE);
        StdDraw.setFont();
        StdDraw.clear(Color.BLACK);
    }

    private void giveName(Engine engine) {
        drawFrame("Enter a name for your avatar");
        String current = "";
        InputSource input = new KeyboardInputSource();
        char key = 'w';
        while (true) {
            if (StdDraw.hasNextKeyTyped()) {
                key = input.getNextKey();
                if (key == '*') {
                    break;
                }
                current += key;
                drawFrame("Enter a name for your avatar");
                StdDraw.text(Engine.WIDTH / 2, Engine.HEIGHT / 2 - 4,
                        "Press * to exit");
                StdDraw.text(Engine.WIDTH / 2, Engine.HEIGHT / 2 - 2, current);
                StdDraw.show();
            }
        }
        name = current;
        interactWithKeyboard(engine);
    }

    private static void drawOptions() {
        StdDraw.setFont(FONT);
        StdDraw.text(Engine.WIDTH / 2, Engine.HEIGHT / 2, "CS61B Project 3");
        StdDraw.text(Engine.WIDTH / 2, Engine.HEIGHT / 2 - 4, "New Game (N)");
        StdDraw.text(Engine.WIDTH / 2, Engine.HEIGHT / 2 - 6, "Load Game (L)");
        StdDraw.text(Engine.WIDTH / 2, Engine.HEIGHT / 2 - 8, "Options (O)");
        StdDraw.text(Engine.WIDTH / 2, Engine.HEIGHT / 2 - 10, "Quit Game (Q)");
        StdDraw.show();
    }

    private static void drawFrame(String s) {
        drawFrame(s, Engine.WIDTH / 2, Engine.HEIGHT / 2);
    }

    private static void drawFrame(String s, int x, int y) {
        StdDraw.clear(Color.BLACK);
        StdDraw.text(x, y, s);
        StdDraw.show();
    }

    private static long parseSeed() {
        long seed = 0;
        char next = 'n';
        int x = Engine.WIDTH / 2;
        int y = (Engine.HEIGHT / 2) - 4;
        StdDraw.setFont(FONT);
        StdDraw.text(x, y, "Seed: ");
        StdDraw.show();
        while (next != 's' && next != 'S') {
            if (StdDraw.hasNextKeyTyped()) {
                next = StdDraw.nextKeyTyped();
                if ((int) next < 58) {
                    seed = (seed * 10) + (next - 48);
                    drawFrame("Please enter seed");
                    StdDraw.text(x, y, "Seed: " + seed);
                    StdDraw.text(x, y - 4, "Press s to continue");
                    StdDraw.show();
                }
            }
        }
        return seed;
    }
}

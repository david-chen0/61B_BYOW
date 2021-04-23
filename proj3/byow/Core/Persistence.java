package byow.Core;

import byow.TileEngine.TERenderer;
import byow.TileEngine.TETile;
import edu.princeton.cs.introcs.StdDraw;

import java.io.File;
import java.io.IOException;
import java.io.FileWriter;
import java.util.Scanner;

public class Persistence {

    /**
     * Save File saves seed, typed keys, Character Location
     *
     * @source File read, write references
     * https://www.w3schools.com/java/java_files_create.asp
     * https://www.w3schools.com/java/java_files_read.asp
     */
    private File saveFile;
    private long seed;
    private int x, y; //player location
    private String replay = "";

//    public Persistence() {
//        File saveDir = new File("byow/Core/saves");
//        if (!saveDir.exists()) {
//            saveDir.mkdir();
//        }
//    }

    public void save(long s, String r, int pX, int pY) {
        seed = s;
        x = pX;
        y = pY;
        try {
            saveFile = new File("0.txt");
            if (saveFile.exists()) {
                Scanner reader = new Scanner(saveFile);
                String[] data = reader.nextLine().split(",");
                replay = data[3];
                saveFile.delete();
            }
            saveFile.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
        }
        replay += r;
        String save = seed + "," + x + "," + y + "," + replay;
        try {
            FileWriter writer = new FileWriter("0.txt");
            writer.write(save);
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void load(Engine en) {
        saveFile = new File("0.txt");
        if (!saveFile.exists()) {
            System.exit(0);
        }
        try {
            Scanner reader = new Scanner(saveFile);
            String[] data = reader.nextLine().split(",");
            en.interactWithInputString("n" + data[0] + "s");
            en.movePlayer(Integer.parseInt(data[1]), Integer.parseInt(data[2]));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void startReplay(Engine engine) {
        saveFile = new File("0.txt");
        if (!saveFile.exists()) {
            StdDraw.textLeft(1, 1, "No save files, can't replay");
        }
        try {
            Scanner reader = new Scanner(saveFile);
            String[] data = reader.nextLine().split(",");
            engine.interactWithInputString("n" + data[0] + "s");
            replay = data[3];
            for (char key : replay.toCharArray()) {
                int[] location = engine.getPlayerLocation();
                TERenderer ter = engine.getTer();
                TETile[][] world = engine.getWorld();
                switch (Character.toLowerCase(key)) {
                    case 'a':
                        engine.movePlayer(location[0] - 1, location[1]);
                        break;
                    case 's':
                        engine.movePlayer(location[0], location[1] - 1);
                        break;
                    case 'd':
                        engine.movePlayer(location[0] + 1, location[1]);
                        break;
                    case 'w':
                        engine.movePlayer(location[0], location[1] + 1);
                        break;
                    default:
                        break;
                }
                ter.renderFrame(world);
                StdDraw.pause(500);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void clean() {
        saveFile = new File("0.txt");
        saveFile.delete();
    }
}

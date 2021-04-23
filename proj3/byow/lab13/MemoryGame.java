package byow.lab13;

import edu.princeton.cs.introcs.StdDraw;
import edu.princeton.cs.introcs.Stopwatch;

import java.awt.Color;
import java.awt.Font;
import java.util.Random;

public class MemoryGame {
    private int width;
    private int height;
    private int round;
    private static Random rand;
    private boolean gameOver;
    private boolean playerTurn;
    private static final char[] CHARACTERS = "abcdefghijklmnopqrstuvwxyz".toCharArray();
    private static final String[] ENCOURAGEMENT = {"You can do this!", "I believe in you!",
                                                   "You got this!", "You're a star!", "Go Bears!",
                                                   "Too easy for you!", "Wow, so impressive!"};

    public static void main(String[] args) {
        if (args.length < 1) {
            System.out.println("Please enter a seed");
            //return;
        }
        //long seed = Integer.parseInt(args[0]);
        long seed = 21589213;
        MemoryGame game = new MemoryGame(40, 40, seed);
        game.startGame();
    }

    public MemoryGame(int width, int height, long seed) {
        /* Sets up StdDraw so that it has a width by height grid of 16 by 16 squares as its canvas
         * Also sets up the scale so the top left is (0,0) and the bottom right is (width, height)
         */
        this.width = width;
        this.height = height;
        StdDraw.setCanvasSize(this.width * 16, this.height * 16);
        Font font = new Font("Monaco", Font.BOLD, 30);
        StdDraw.setFont(font);
        StdDraw.setXscale(0, this.width);
        StdDraw.setYscale(0, this.height);
        StdDraw.clear(Color.BLACK);
        StdDraw.enableDoubleBuffering();

        rand = new Random(seed);
    }

    public String generateRandomString(int n) {
        String result = "";
        for (int i = 0; i < n; i++) {
            result += CHARACTERS[rand.nextInt(25)];
        }
        return result;
    }

    public void drawFrame(String s) {
        StdDraw.clear(Color.BLACK);
        drawHeader();
        StdDraw.setFont(new Font("Monaco", Font.BOLD, 30));
        StdDraw.setPenColor(255, 255, 255);
        StdDraw.text(width / 2, height / 2, s);
        StdDraw.show();
    }

    private void drawHeader() {
        StdDraw.line(0, height - 3, width, height - 3);
        StdDraw.textLeft(0, height - 2, "Round: " + round);
        StdDraw.text(width / 2, height - 2, ENCOURAGEMENT[3]);
    }

    public void flashSequence(String letters) {
        drawFrame("");
        for (int i = 0; i < letters.length(); i++) {
            char temp = letters.charAt(i);
            drawFrame(Character.toString(temp));
            Stopwatch timer = new Stopwatch();
            while (timer.elapsedTime() < 1) { }
            drawFrame("");
            double time = timer.elapsedTime();
            while (timer.elapsedTime() - time < 0.5) { }
        }
    }

    public String solicitNCharsInput(int n) {
        String result = "";
        while (n > 0) {
            if (StdDraw.hasNextKeyTyped()) {
                result += Character.toString(StdDraw.nextKeyTyped());
                n--;
            }
        }
        return result;
    }

    public void startGame() {
        String input = "";
        String player = "";
        drawFrame("");
        for (round = 1; input.equals(player); round++) {
            Stopwatch timer = new Stopwatch();
            drawFrame("Round: " + round);
            while (timer.elapsedTime() < 2) { }
            input = generateRandomString(round);
            flashSequence(input);
            player = solicitNCharsInput(round);
        }
        round--;
        drawFrame("Game Over");
    }

}

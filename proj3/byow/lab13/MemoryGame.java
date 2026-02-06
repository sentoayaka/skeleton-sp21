package byow.lab13;

import byow.Core.RandomUtils;
import edu.princeton.cs.introcs.StdDraw;

import java.awt.Color;
import java.awt.Font;
import java.util.Objects;
import java.util.Random;

public class MemoryGame {
    /** The width of the window of this game. */
    private int width;
    /** The height of the window of this game. */
    private int height;
    /** The current round the user is on. */
    private int round;
    /** The Random object used to randomly generate Strings. */
    private Random rand;
    /** Whether or not the game is over. */
    private boolean gameOver;
    /** Whether or not it is the player's turn. Used in the last section of the
     * spec, 'Helpful UI'. */
    private boolean playerTurn;
    /** The characters we generate random Strings from. */
    private static final char[] CHARACTERS = "abcdefghijklmnopqrstuvwxyz".toCharArray();
    /** Encouraging phrases. Used in the last section of the spec, 'Helpful UI'. */
    private static final String[] ENCOURAGEMENT = {"You can do this!", "I believe in you!",
                                                   "You got this!", "You're a star!", "Go Bears!",
                                                   "Too easy for you!", "Wow, so impressive!"};

    public static void main(String[] args) {
        if (args.length < 1) {
            System.out.println("Please enter a seed");
            return;
        }

        long seed = Long.parseLong(args[0]);
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

        this.rand = new Random(seed);
    }

    public String generateRandomString(int n) {
        String str = "";
        for (int i = 0; i < n; i++) {
            int index = rand.nextInt(CHARACTERS.length);
            str += CHARACTERS[index];
        }

        return str;
    }

    public void drawHUD() {
        StdDraw.setPenColor(Color.WHITE);
        Font smallFont = new Font("Monospaced", Font.BOLD, 14);
        StdDraw.setFont(smallFont);

        StdDraw.line(0, height - 2, width, height - 2);

        // 3. 显示左侧信息：当前回合
        StdDraw.textLeft(1, height - 1, "Round: " + this.round);

        // 4. 显示右侧信息：鼓励话语或当前动作
        if (playerTurn) {
            StdDraw.textRight(width - 1, height - 1, "Type!");
        } else {
            StdDraw.textRight(width - 1, height - 1, "Watch!");
        }
    }

    public void drawFrame(String s) {
        StdDraw.clear(Color.BLACK);
        StdDraw.setPenColor(Color.WHITE);
        Font font = new Font("Monospaced", Font.BOLD, 30);
        StdDraw.setFont(font);

        StdDraw.text(this.width / 2, this.height / 2, s);

        if (!gameOver) {
            drawHUD();
        }

        StdDraw.show();
    }

    public void flashSequence(String letters) {
        for (int i = 0; i < letters.length(); i++) {
            drawFrame(letters.substring(i, i + 1));
            StdDraw.pause(1000);

            drawFrame("");
            StdDraw.pause(500);
        }
    }

    public String solicitNCharsInput(int n) {
        String input = "";
        drawFrame(input);

        while (input.length() < n) {
            if (StdDraw.hasNextKeyTyped()) {
                char c = StdDraw.nextKeyTyped();
                input += c;
                drawFrame(input);
            }
        }

        StdDraw.pause(1000);
        return input;
    }

    public void startGame() {
        this.round = 1;
        this.gameOver = false;

        while (!this.gameOver) {
            playerTurn = false;
            drawFrame("Round: " + this.round);
            StdDraw.pause(1500);

            String target = generateRandomString(this.round);
            flashSequence(target);

            while (StdDraw.hasNextKeyTyped()) {
                StdDraw.nextKeyTyped();
            }
            playerTurn = true;
            String userInput = solicitNCharsInput(this.round);

            if (Objects.equals(userInput, target)) {
                this.round++;
                drawFrame("Correct! Next Round...");
                StdDraw.pause(1000);
            } else {
                this.gameOver = true;
                drawFrame("Game Over! Final Score: " + (this.round - 1));
            }
        }
    }

}

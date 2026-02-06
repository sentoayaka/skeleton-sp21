package byow.Core;

import byow.TileEngine.TERenderer;
import byow.TileEngine.TETile;
import byow.TileEngine.Tileset;
import edu.princeton.cs.introcs.In;
import edu.princeton.cs.introcs.StdDraw;
import jdk.jshell.execution.Util;

import java.awt.*;
import java.io.*;
import java.util.Objects;
import java.util.Random;

import static java.lang.String.*;

public class Engine {
    TERenderer ter = new TERenderer();
    public static final int WIDTH = 80;
    public static final int HEIGHT = 30;
    private boolean showGUI;
    private Player player;
    private TETile[][] world;
    private long seed;
    private Random rand;

    private long getSeed(InputSource inputSource) {
        String seedString = "";
        while (inputSource.hasNextKey()) {
            char c = inputSource.getNextKey();
            if (Character.isDigit(c)) {
                seedString += c;
            } else if (c == 'S') {
                break;
            }
        }

        seed = Long.parseLong(seedString);
        rand = new Random(seed);
        return seed;
    }

    private TETile[][] generateWorld(long seed) {
        MapGenerator mg = new MapGenerator(WIDTH, HEIGHT, seed);
        return mg.build();
    }

    private void initializePlayerPosition() {
        while (true) {
            int x = RandomUtils.uniform(rand, 0, WIDTH - 1);
            int y = RandomUtils.uniform(rand, 0, HEIGHT - 1);
            if (Player.checkLegality(x, y, world)) {
                player = new Player(x, y, world[x][y]);
                world[x][y] = Tileset.AVATAR;
                return ;
            }
        }
    }

    private void movePlayer(char input) {
        int dx = 0, dy = 0;
        switch (input) {
            case 'W': dy = 1; break;
            case 'S': dy = -1; break;
            case 'A': dx = -1; break;
            case 'D': dx = 1; break;
            default: return;
        }

        player.move(dx, dy, world);
    }

    private void loadGame() {
        File f = new File("save.txt");
        if (!f.exists()) {
            return ;
        }

        try {
            FileInputStream fs = new FileInputStream(f);
            ObjectInputStream is = new ObjectInputStream(fs);

            GameState state = (GameState) is.readObject();

            this.seed = state.seed;
            this.world = generateWorld(this.seed);

            this.player = state.player;
            this.world[player.getX()][player.getY()] = Tileset.AVATAR;

            is.close();
        } catch (Exception e) {
            System.out.println(e);
        }
    }

    private void saveGame() {
        File f = new File("save.txt");
        try {
            if (!f.exists()) {
                f.createNewFile();
            }
            FileOutputStream fs = new FileOutputStream(f);
            ObjectOutputStream os = new ObjectOutputStream(fs);

             GameState state =new GameState(this.seed, this.player);

            os.writeObject(state);
            os.close();
        } catch (IOException e) {
            System.out.println(e);
        }
    }

    private void render() {
        ter.renderFrame(world);

        StdDraw.setPenColor(Color.WHITE);
        Font font = new Font("Monospaced", Font.BOLD, 14);
        StdDraw.setFont(font);

        int mouseX = (int) StdDraw.mouseX();
        int mouseY = (int) StdDraw.mouseY();

        if (mouseX >= 0 && mouseX < WIDTH && mouseY >= 0 && mouseY < HEIGHT) {
            String description = world[mouseX][mouseY].description();
            StdDraw.textLeft(1, HEIGHT, "Tile: " + description);
        }

        StdDraw.show();
    }

    private void startNewGame(InputSource inputSource) {
        long seed = getSeed(inputSource);
        world = generateWorld(seed);
        initializePlayerPosition();
    }

    private void play(InputSource inputSource) {
        while (inputSource.hasNextKey()) {
            char c = inputSource.getNextKey();

            if (c == 'N') {
                startNewGame(inputSource);
            } else if(c == 'W' || c == 'S' || c == 'A' || c == 'D') {
                movePlayer(c);
            } else if (c == 'L') {
                loadGame();
            } else if (c == ':') {
                char next = inputSource.getNextKey();
                if (next == 'Q') {
                    saveGame();
                    return ;
                }
            }

            if (showGUI) {
                render();
            }
            StdDraw.pause(10);
        }
    }

    public void interactWithKeyboard() {
        showGUI = true;
        ter.initialize(WIDTH, HEIGHT + 1);

        KeyboardInputSource inputSource = new KeyboardInputSource();
        play(inputSource);
    }

    public TETile[][] interactWithInputString(String input) {
        showGUI = false;
        input = input.toUpperCase();

        StringInputSource inputSource = new StringInputSource(input);
        play(inputSource);

        return world;
    }

    public boolean isShowGUI() {
        return showGUI;
    }
}

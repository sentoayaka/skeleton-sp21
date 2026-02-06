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
    private Monster monster;
    private TETile[][] world;
    private int currentLevel;
    private long seed;
    private Random rand;

    private void getSeed(InputSource inputSource) {
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
    }

    private TETile[][] generateWorld(long s) {
        MapGenerator mg = new MapGenerator(WIDTH, HEIGHT, s);
        return mg.build();
    }

    private void initializePlayer() {
        while (true) {
            int x = RandomUtils.uniform(rand, 0, WIDTH - 1);
            int y = RandomUtils.uniform(rand, 0, HEIGHT - 1);
            if (Entity.canMoveTo(x, y, world)) {
                player = new Player(x, y, world[x][y]);
                world[x][y] = Tileset.AVATAR;
                return;
            }
        }
    }

    private void initializeMonster() {
        while (true) {
            int x = RandomUtils.uniform(rand, 0, WIDTH - 1);
            int y = RandomUtils.uniform(rand, 0, HEIGHT - 1);
            if (Entity.canMoveTo(x, y, world) && (x != player.x || y != player.y)) {
                monster = new Monster(x, y, world[x][y]);
                world[x][y] = Tileset.MONSTER;
                return;
            }
        }
    }

    private void initializeExit() {
        while (true) {
            int x = RandomUtils.uniform(rand, 0, WIDTH - 1);
            int y = RandomUtils.uniform(rand, 0, HEIGHT - 1);
            if (Entity.canMoveTo(x, y, world) && (x != player.x || y != player.y) && (x != monster.x || y != monster.y)) {
                world[x][y] = Tileset.UNLOCKED_DOOR;
                return;
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
            return;
        }

        try {
            FileInputStream fs = new FileInputStream(f);
            ObjectInputStream is = new ObjectInputStream(fs);

            GameState state = (GameState) is.readObject();

            this.currentLevel = state.currentLevel;
            this.seed = state.seed;
            this.world = generateWorld(this.seed);
            this.player = state.player;
            player.tile = world[player.x][player.y];
            this.world[player.x][player.y] = Tileset.AVATAR;

            this.monster = state.monster;
            monster.tile = world[monster.x][monster.y];
            this.world[monster.x][monster.y] = Tileset.MONSTER;

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

            GameState state = new GameState(this.currentLevel, this.seed, this.player, this.monster);

            os.writeObject(state);
            os.close();
        } catch (IOException e) {
            System.out.println(e);
        }
    }

    public void render() {

        TETile[][] offsetWorld = new TETile[WIDTH][HEIGHT];
        int visionRange = 5;

        for (int x = 0; x < WIDTH; x++) {
            for (int y = 0; y < HEIGHT; y++) {
                double distance = Math.sqrt(Math.pow(x - player.x, 2) + Math.pow(y - player.y, 2));

                if (distance < visionRange) {
                    offsetWorld[x][y] = world[x][y];
                } else {
                    offsetWorld[x][y] = Tileset.NOTHING;
                }
            }
        }

//        ter.renderFrame(world);
        ter.renderFrame(offsetWorld);

        StdDraw.setPenColor(Color.WHITE);
        Font font = new Font("Monospaced", Font.BOLD, 12);
        StdDraw.setFont(font);

        int mouseX = (int) StdDraw.mouseX();
        int mouseY = (int) StdDraw.mouseY();

        String invDesc = "Inventory: " + player.inventory.toString();
        String tileDesc = "";
        if (mouseX >= 0 && mouseX < WIDTH && mouseY >= 0 && mouseY < HEIGHT - 1) {
            tileDesc = world[mouseX][mouseY].description();
        }
        StdDraw.textLeft(1, HEIGHT - 1, "Current: " + currentLevel + " | " + "Tile: " + tileDesc + " | " + invDesc);
        StdDraw.show();
        StdDraw.pause(10);
    }


    private void startNewGame(InputSource inputSource) {
        world = generateWorld(seed);
        initializePlayer();
        initializeMonster();
        initializeExit();
    }

    private void play(InputSource inputSource) {
        currentLevel = 1;

        while (inputSource.hasNextKey()) {
            char c = inputSource.getNextKey();

            if (c == 'N') {
                getSeed(inputSource);
                startNewGame(inputSource);
            } else if (c == 'W' || c == 'S' || c == 'A' || c == 'D') {
                movePlayer(c);
                monster.moveTowards(player.x, player.y, world);

//                if (player.tile.equals(Tileset.UNLOCKED_DOOR)) {
//                    currentLevel++;
//                    seed += 1;
//                    startNewGame(inputSource);
//                }
            } else if (c == 'L') {
                loadGame();
            } else if (c == ':') {
                char next = inputSource.getNextKey();
                if (next == 'Q') {
                    saveGame();
                    return;
                }
            }

            if (showGUI) {
                render();
            }
        }
    }

    public void interactWithKeyboard() {
        showGUI = true;
        ter.initialize(WIDTH, HEIGHT);

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

    public int getCurrentLevel() {
        return currentLevel;
    }
}

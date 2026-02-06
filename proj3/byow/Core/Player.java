package byow.Core;

import byow.TileEngine.TETile;
import byow.TileEngine.Tileset;

import java.io.Serializable;

public class Player implements Serializable {
    public int x;
    public int y;
    public transient TETile tile;

    public Player(int x, int y, TETile tileUnder) {
        this.x = x;
        this.y = y;
        this.tile = tileUnder;
    }

    public static boolean checkLegality(int x, int y, TETile[][] world) {
        return x >= 0 && x < Engine.HEIGHT && y >= 0 && y < Engine.WIDTH &&
                !world[x][y].equals(Tileset.WALL) &&
                !world[x][y].equals(Tileset.NOTHING);
    }

    public void move(int dx, int dy, TETile[][] world) {
        int nextX = x + dx;
        int nextY = y + dy;

        if (world[nextX][nextY].equals(Tileset.FLOOR)) {
            world[x][y] = tile;
            x = nextX;
            y = nextY;
            tile = world[x][y];
            world[x][y] = Tileset.AVATAR;
        }
    }
}

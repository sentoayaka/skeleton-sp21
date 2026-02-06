package byow.Core;

import byow.TileEngine.TETile;
import byow.TileEngine.Tileset;

import java.io.Serializable;
import java.io.Serializable;

public class Entity implements Serializable {
    protected int x;
    protected int y;
    // 使用 transient 关键字修复你之前的 NotSerializableException
    protected transient TETile tile;

    public Entity(int x, int y, TETile tileUnder) {
        this.x = x;
        this.y = y;
        this.tile = tileUnder;
    }

    public static boolean canMoveTo(int x, int y, TETile[][] world) {
        return x >= 0 && x < Engine.WIDTH && y >= 0 && y < Engine.HEIGHT
                && !world[x][y].equals(Tileset.WALL)
                && !world[x][y].equals(Tileset.NOTHING);
    }

    public void updatePosition(int nextX, int nextY, TETile[][] world, TETile avatar) {
        world[x][y] = tile;
        x = nextX;
        y = nextY;
        tile = world[x][y];
        world[x][y] = avatar;
    }
}

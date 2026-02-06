package byow.Core;

import byow.TileEngine.TETile;
import byow.TileEngine.Tileset;

public class Monster extends Entity {

    public Monster(int x, int y, TETile tileUnder) {
        super(x, y, tileUnder);
    }

    public void moveTowards(int playerX, int playerY, TETile[][] world) {
        int dx = Integer.compare(playerX, x);
        int dy = Integer.compare(playerY, y);

        // 尝试水平或垂直移动
        if (dx != 0 && canMoveTo(x + dx, y, world)) {
            updatePosition(x + dx, y, world, Tileset.MONSTER);
        } else if (canMoveTo (x, y + dy, world)) {
            updatePosition(x, y + dy, world, Tileset.MONSTER);
        }
    }
}

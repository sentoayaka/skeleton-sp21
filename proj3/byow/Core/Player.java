package byow.Core;

import byow.TileEngine.TETile;
import byow.TileEngine.Tileset;

import java.util.ArrayList;
import java.util.List;

public class Player extends Entity {
    public List<String> inventory = new ArrayList<>();

    public Player(int x, int y, TETile tileUnder) {
        super(x, y, tileUnder);
    }

    public void addItem(String itemName) {
        inventory.add(itemName);
    }

    public void move(int dx, int dy, TETile[][] world) {
        int nextX = x + dx;
        int nextY = y + dy;

        if (canMoveTo(nextX, nextY, world)) {
            if (world[nextX][nextY].equals(Tileset.FLOWER)) {
                addItem("Flower");
                world[nextX][nextY] = Tileset.FLOOR;
            }
            updatePosition(nextX, nextY, world, Tileset.AVATAR);
        }
    }
}

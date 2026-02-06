package byow.lab12;
import org.junit.Test;

import static org.junit.Assert.*;

import byow.TileEngine.TERenderer;
import byow.TileEngine.TETile;
import byow.TileEngine.Tileset;
import java.util.Random;

/**
 * Draws a world consisting of hexagonal regions.
 */
public class HexWorld {

    /* 计算第 i 行应该有多宽 */
    private static int rowWidth(int s, int i) {
        if (i < s) {
            return s + 2 * i; // 下半部分，越往上越宽
        } else {
            int j = i - s;
            return (s + 2 * (s - 1)) - 2 * j; // 上半部分，越往上越窄
        }
    }

    /* 计算第 i 行相对于最左侧(x)的偏移量 */
    private static int rowOffset(int s, int i) {
        if (i < s) {
            return -i; // 每一行向左移动 1 位
        } else {
            return -(2 * s - 1 - i);
        }
    }

    private static final long SEED = 2873123;
    private static final Random RANDOM = new Random(SEED);
    private static TETile randomTile() {
        int tileNum = RANDOM.nextInt(3);
        switch (tileNum) {
            case 0: return Tileset.WALL;
            case 1: return Tileset.FLOWER;
            case 2: return Tileset.GRASS;
            default: return Tileset.MOUNTAIN;
        }
    }

    public static void addHexagon(TETile[][] world, int x, int y, int s, TETile tile) {
        for (int i = 0; i < 2 * s; i ++) {
            int width = rowWidth(s, i);
            int xOffset = rowOffset(s, i);

            // 计算这一行的起始绘制点
            int startX = x + xOffset;
            int startY = y + i;

            // 绘制这一行的每一个 tile
            for (int xColumn = 0; xColumn < width; xColumn++) {
                world[startX + xColumn][startY] = tile;
            }
        }
    }

    public static void drawTesselationHexagons(TETile[][] world, int x, int y) {
        int s = 3;
        int xSpace = 2 * s -1;
        int[] colCounts = {3, 4, 5, 4, 3};
        for (int c = 0; c < 5; c++) {
            int numInCol = colCounts[c];
            int startY = y - (numInCol - 3) * s;
            int startX = x + c * xSpace;
            for (int i = 0; i < numInCol; i++) {
                int thisX = startX;
                int thisY = startY + i * (2 * s);
                addHexagon(world, thisX, thisY, s, randomTile());
            }
        }
    }
}

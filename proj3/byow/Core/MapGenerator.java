package byow.Core;

import byow.TileEngine.TERenderer;
import byow.TileEngine.TETile;
import byow.TileEngine.Tileset;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class MapGenerator {
    private int width;
    private int height;
    private Random random;
    private TETile[][] world;

    private static class Room {
        int xL, yL, w, h;

        Room(int x, int y, int width, int height) {
            this.xL = x;
            this.yL = y;
            this.w = width;
            this.h = height;
        }

        int centerX() { return xL + w / 2; }
        int centerY() { return yL + h / 2; }

        boolean overlaps(Room other) {
            if (this.xL + this.w <= other.xL
                    || this.xL >= other.xL + other.w
                    || this.yL + this.h <= other.yL
                    || this.yL >= other.yL + other.h) {
                return false;
            }
            return true;
        }
    }

    // 构造函数：初始化“工具”和“画布”
    public MapGenerator(int width, int height, long seed) {
        this.width = width;
        this.height = height;
        this.random = new Random(seed);
        this.world = new TETile[width][height];

        // 关键：先把世界填满 NOTHING，否则会报空指针
        initializeEmptyWorld();
    }

    private void initializeEmptyWorld() {
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                world[x][y] = Tileset.NOTHING;
            }
        }
    }

    private boolean isSafe(Room r, List<Room> rooms) {
        if (r.xL < 0 || r.xL + r.w >= width || r.yL < 0 || r.yL + r.h >= height) {
            return false;
        }

        for (Room other : rooms) {
            if (r.overlaps(other)) {
                return false;
            }
        }
        return true;
    }

    private void fillWithFloor(Room r) {
        for (int i = r.xL; i <= r.xL + r.w; i++) {
            for (int j = r.yL; j <= r.yL + r.h; j++) {
                world[i][j] = Tileset.FLOOR;
            }
        }
    }

    private void drawHorizontalLine(int x1, int x2, int y) {
        int start = Math.min(x1, x2);
        int end = Math.max(x1, x2);
        for (int x = start; x <= end; x++) {
            world[x][y] = Tileset.FLOOR;
        }
    }

    private void drawVerticalLine(int y1, int y2, int x) {
        int start = Math.min(y1, y2);
        int end = Math.max(y1, y2);
        for (int y = start; y <= end; y++) {
            world[x][y] = Tileset.FLOOR;
        }
    }

    private void addHallways(List<Room> rooms) {
        for (int i = 0; i < rooms.size() - 1; i++) {
            Room roomA = rooms.get(i);
            Room roomB = rooms.get(i + 1);

            int x1 = roomA.centerX();
            int y1 = roomA.centerY();
            int x2 = roomB.centerX();
            int y2 = roomB.centerY();

            if (random.nextBoolean()) {
                drawHorizontalLine(x1, x2, y1);
                drawVerticalLine(y1, y2, x2);
            } else {
                drawVerticalLine(y1, y2, x1);
                drawHorizontalLine(x1, x2, y2);
            }
        }
    }

    private List<Room> addRooms() {
        List<Room> rooms = new ArrayList<>();
        int numAttemps = 50;

        for (int i = 0; i < numAttemps; i++) {
            int w = RandomUtils.uniform(random, 3, 10);
            int h = RandomUtils.uniform(random, 3, 10);

            int x = RandomUtils.uniform(random, 1, width - w - 1);
            int y = RandomUtils.uniform(random, 1, height -h - 2);

            Room newRoom = new Room(x, y, w, h);

            if (isSafe(newRoom, rooms)) {
                fillWithFloor(newRoom);
                rooms.add(newRoom);
            }
        }
        return rooms;
    }

    private boolean isNeighborToFloor(int x, int y) {
        for (int dx = -1; dx <= 1; dx++) {
            for (int dy = -1; dy <= 1; dy++) {
                if (dx == 0 && dy == 0) continue; // 跳过自己

                int nx = x + dx;
                int ny = y + dy;

                if (nx >= 0 && nx < width && ny >= 0 && ny < height) {
                    if (world[nx][ny].equals(Tileset.FLOOR)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    private void addWalls() {
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                // 只有当当前位置是空的，且旁边有地板时，才放墙
                if (world[x][y].equals(Tileset.NOTHING) && isNeighborToFloor(x, y)) {
                    world[x][y] = Tileset.WALL;
                }
            }
        }
    }

    public TETile[][] build() {
        List<Room> rooms = addRooms();
        addHallways(rooms);
        addWalls();
        return world;
    }
}
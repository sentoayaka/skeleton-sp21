package byow.Core;
import byow.TileEngine.TETile;

import java.io.Serializable;

public class GameState implements Serializable {
    public int currentLevel;
    public long seed;
    public Player player;
    public Monster monster;
    public Entity exit;

    public GameState(int currentLevel, long seed, Player player, Monster monster, Entity exit) {
        this.currentLevel = currentLevel;
        this.seed = seed;
        this.player = player;
        this.monster = monster;
        this.exit = exit;
    }
}

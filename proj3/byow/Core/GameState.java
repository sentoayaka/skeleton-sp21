package byow.Core;
import byow.TileEngine.TETile;

import java.io.Serializable;

public class GameState implements Serializable {
    public long seed;
    public Player player;
    public Monster monster;

    public GameState(long s, Player player, Monster monster) {
        this.seed = s;
        this.player = player;
        this.monster = monster;
    }
}

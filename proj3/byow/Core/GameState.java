package byow.Core;
import byow.TileEngine.TETile;

import java.io.Serializable;

public class GameState implements Serializable{
    public long seed;
    public Player player;

    public GameState(long s, Player player) {
        this.seed = s;
        this.player = player;
    }
}

package byow.Core;
import edu.princeton.cs.introcs.StdDraw;

public class KeyboardInputSource implements InputSource{

    @Override
    public char getNextKey() {
        while (true) {
            if (StdDraw.hasNextKeyTyped()) {
                char c = StdDraw.nextKeyTyped();
                return Character.toUpperCase(c);
            }
        }
    }

    @Override
    public boolean hasNextKey() {
        return true;
    }
}

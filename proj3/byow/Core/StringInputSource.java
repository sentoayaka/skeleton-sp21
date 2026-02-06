package byow.Core;

public class StringInputSource implements InputSource{
    private String input;
    private int index;

    public StringInputSource(String s) {
        input = s;
        index = 0;
    }
    @Override
    public char getNextKey() {
        char c = input.charAt(index);
        index += 1;
        return Character.toUpperCase(c);
    }

    @Override
    public boolean hasNextKey() {
        return index < input.length();
    }
}

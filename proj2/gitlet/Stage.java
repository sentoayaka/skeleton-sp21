package gitlet;

import java.io.Serializable;
import java.util.HashMap;
import java.util.HashSet;

public class Stage implements Serializable {

    public HashMap<String, String> added = new HashMap<>();

    public HashSet<String> removed = new HashSet<>();
}

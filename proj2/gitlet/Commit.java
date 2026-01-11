package gitlet;

import java.io.File;
import static gitlet.Utils.*;

import java.io.Serializable;
import java.util.Map;

public class Commit implements Serializable {

    public String logMessage, commitTime, parent, mergeParent;

    public Map<String, String> fileBlobTable;

    private String name;

    public Commit(String message, String time, String parent, String mergeParent, Map<String, String> blobs) {
        this.logMessage = message;
        this.commitTime = time;
        this.parent = parent;
        this.mergeParent = mergeParent;
        this.fileBlobTable = blobs;
    }

    public static Commit fromFile(String name) {
        File commitFile = join(Repository.OBJECTS_FOLDER, name);
        return readObject(commitFile, Commit.class);
    }

    public String saveCommit() {
        byte[] content = serialize(this);
        String commitID = sha1(content);

        File commitFile = join(Repository.OBJECTS_FOLDER, commitID);
        writeObject(commitFile, this);

        return commitID;
    }
}

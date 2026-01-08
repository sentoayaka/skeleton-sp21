package gitlet;

import java.io.File;
import static gitlet.Utils.*;

// TODO: any imports you need here

import java.io.Serializable;
import java.util.Map;

/** Represents a gitlet commit object.
 *  TODO: It's a good idea to give a description here of what else this Class
 *  does at a high level.
 *
 *  @author TODO
 */
public class Commit implements Serializable {
    /**
     * TODO: add instance variables here.
     *
     * List all instance variables of the Commit class here with a useful
     * comment above them describing what that variable represents and how that
     * variable is used. We've provided one example for `message`.
     */

    /** The message of this Commit. */

    public String logMessage, commitTime, parentRef, parentMergeRef, SHA1ID;

    public Map<String, String> fileBlobTable;

    private String name;

    /* TODO: fill in the rest of this class. */

    public Commit(String message, String time, String parent, Map<String, String> blobs) {
        this.logMessage = message;
        this.commitTime = time;
        this.parentRef = parent;
        this.fileBlobTable = blobs;
    }

    public static Commit fromFile(String name){
        File commitFile = join(Repository.OBJECTS_FOLDER, name);
        return readObject(commitFile, Commit.class);
    }

    public String saveCommit(){
        byte[] content = serialize(this);
        String commitID = sha1(content);

        File commitFile = join(Repository.OBJECTS_FOLDER, commitID);
        writeObject(commitFile, this);

        return commitID;
    }
}

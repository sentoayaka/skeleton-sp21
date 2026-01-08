package gitlet;

import java.io.File;
import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.*;

import static gitlet.Utils.*;

// TODO: any imports you need here

/** Represents a gitlet repository.
 *  TODO: It's a good idea to give a description here of what else this Class
 *  does at a high level.
 *
 *  @author TODO
 */
public class Repository {
    /**
     *
     * List all instance variables of the Repository class here with a useful
     * comment above them describing what that variable represents and how that
     * variable is used. We've provided two examples for you.
     */

    /** The current working directory. */
    public static final File CWD = new File(System.getProperty("user.dir"));
    /** The .gitlet directory. */
    public static final File GITLET_DIR = join(CWD, ".gitlet");

    public static final File OBJECTS_FOLDER = join(GITLET_DIR, "objects");

    public static final File HEADS_FOLDER = join(GITLET_DIR, "heads");

    public static final File HEAD_FILE = join(GITLET_DIR, "HEAD");

    public static final File STAGE_FILE = join(GITLET_DIR, "STAGE");
    /* TODO: fill in the rest of this class. */

    private static String formatDate(Date date) {
        // 格式必须为：星期几 月份 日期 时间 年份 时区
        // 例如：Thu Jan 1 00:00:00 1970 -0800
        SimpleDateFormat formatter = new SimpleDateFormat("EEE MMM d HH:mm:ss yyyy Z", Locale.US);
        return formatter.format(date);
    }

    public static void Init() {
        if(GITLET_DIR.exists()){
            System.out.println("A Gitlet version-control system already exists in the current directory.");
            System.exit(0);
        }

        GITLET_DIR.mkdir();
        OBJECTS_FOLDER.mkdir();
        HEADS_FOLDER.mkdir();
        Utils.writeContents(HEAD_FILE, "");

        String initTimestamp = formatDate(new Date(0));
        Commit initCommit = new Commit("initial commit", initTimestamp, null, new HashMap<>());

        String commitID = initCommit.saveCommit();

        File masterFile = join(HEADS_FOLDER, "master");
        writeContents(masterFile, commitID);

        writeContents(HEAD_FILE, "master");

        writeObject(STAGE_FILE, new Stage());
    }

    private static File getbranchFile(){
        String headID = readContentsAsString(HEAD_FILE);
        File branchFile = join(HEADS_FOLDER, headID);
        return branchFile;
    }

    private static String getHeadCommitID(){
        File branchFile = getbranchFile();
        String headCommitID = readContentsAsString(branchFile);
        return headCommitID;
    }

    private static Commit getHeadCommit(){
        String headCommitID = getHeadCommitID();
        Commit headCommit = Commit.fromFile(headCommitID);
        return headCommit;
    }

    public static void Add(String name) {
        File fileToAdd = join(CWD, name);
        if(!fileToAdd.exists()){
            System.out.println("File does not exist.");
            System.exit(0);
        }

        byte[] content = readContents(fileToAdd);
        String blobID = sha1(content);

        Commit headCommit = getHeadCommit();

        Stage stage = readObject(STAGE_FILE, Stage.class);

        if(Objects.equals(headCommit.fileBlobTable.get(name), blobID)) {
            if(stage.removed.contains(name)) {
                stage.removed.remove(name);
            }

            stage.added.remove(name);
        } else {
            stageMap.put(name, blobID);

            File newBlob = join(OBJECTS_FOLDER, blobID);
            if(!newBlob.exists()) {
                writeContents(newBlob, content);
            }
        }

        writeObject(STAGE_FILE, (Serializable) stageMap);
    }

    public static void Commit(String message) {
        Map<String, String> stageMap = readObject(STAGE_FILE, HashMap.class);

        if(stageMap.isEmpty()){
            System.out.println("No changes added to the commit.");
            System.exit(0);
        }

        Commit parentCommit = getHeadCommit();
        Map<String, String> newFileBlobTable = new HashMap<>(parentCommit.fileBlobTable);

        for(String name : stageMap.keySet()){
            String blobID = stageMap.get(name);
            newFileBlobTable.put(name, blobID);
        }
        writeObject(STAGE_FILE, new HashMap<String, String>());

        String timeStamp = formatDate(new Date());
        Commit headCommit = new Commit(message, timeStamp, getHeadCommitID(), newFileBlobTable);

        String headCommitID = headCommit.saveCommit();
        File brachFile = getbranchFile();

        writeContents(brachFile, headCommitID);
    }

    public static void Log() {
        String curCommitID = getHeadCommitID();

        while(true) {
            Commit curCommit = Commit.fromFile(curCommitID);
            System.out.println("===");
            System.out.println("commit " + curCommitID);
            System.out.println("Date: " + curCommit.commitTime);
            System.out.println(curCommit.logMessage);
            System.out.println();

            if(curCommit.parentRef == null) break;
            else curCommitID = curCommit.parentRef;
        }
    }
}

package gitlet;

import java.io.File;
import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.*;

import static gitlet.Utils.*;
import static gitlet.Utils.readContentsAsString;

public class Repository {

    public static final File CWD = new File(System.getProperty("user.dir"));

    public static final File GITLET_DIR = join(CWD, ".gitlet");

    public static final File OBJECTS_FOLDER = join(GITLET_DIR, "objects");

    public static final File HEADS_FOLDER = join(GITLET_DIR, "heads");

    public static final File HEAD_FILE = join(GITLET_DIR, "HEAD");

    public static final File STAGE_FILE = join(GITLET_DIR, "STAGE");

    public static final File REMOTE_FILE = join(GITLET_DIR, "REMOTES");

    private static String formatDate(Date date) {
        // 格式必须为：星期几 月份 日期 时间 年份 时区
        // 例如：Thu Jan 1 00:00:00 1970 -0800
        SimpleDateFormat formatter = new SimpleDateFormat("EEE MMM d HH:mm:ss yyyy Z", Locale.US);
        return formatter.format(date);
    }

    private static File getHeadBranchFile() {
        String headID = readContentsAsString(HEAD_FILE);
        File branchFile = join(HEADS_FOLDER, headID);
        return branchFile;
    }

    private static String getHeadCommitID() {
        File branchFile = getHeadBranchFile();
        String headCommitID = readContentsAsString(branchFile);
        return headCommitID;
    }

    private static Commit getHeadCommit() {
        String headCommitID = getHeadCommitID();
        Commit headCommit = Commit.fromFile(headCommitID);
        return headCommit;
    }

    private static String getCommitID(String branchID) {
        File branchFile = join(HEADS_FOLDER, branchID);
        String commitID = readContentsAsString(branchFile);
        return commitID;
    }

    public static void init() {
        if (GITLET_DIR.exists()) {
            System.out.println("A Gitlet version-control system already exists in the current directory.");
            System.exit(0);
        }

        GITLET_DIR.mkdir();
        OBJECTS_FOLDER.mkdir();
        HEADS_FOLDER.mkdir();

        String initTimestamp = formatDate(new Date(0));
        Commit initCommit = new Commit("initial commit", initTimestamp, null, null, new HashMap<>());

        String commitID = initCommit.saveCommit();

        File masterFile = join(HEADS_FOLDER, "master");
        writeContents(masterFile, commitID);

        writeContents(HEAD_FILE, "master");

        writeObject(STAGE_FILE, new Stage());
    }

    public static void add(String name) {
        File fileToAdd = join(CWD, name);
        if (!fileToAdd.exists()) {
            System.out.println("File does not exist.");
            System.exit(0);
        }

        byte[] content = readContents(fileToAdd);
        String blobID = sha1(content);

        Commit headCommit = getHeadCommit();

        Stage stage = readObject(STAGE_FILE, Stage.class);
        if (stage.removed.contains(name)) {
            stage.removed.remove(name);
        }

        if (Objects.equals(headCommit.fileBlobTable.get(name), blobID)) {
            if (stage.added.containsKey(name)) {
                stage.added.remove(name);
            }
        } else {
            stage.added.put(name, blobID);

            File newBlob = join(OBJECTS_FOLDER, blobID);
            if (!newBlob.exists()) {
                writeContents(newBlob, content);
            }
        }

        writeObject(STAGE_FILE, (Serializable) stage);
    }

    public static void commit(String message) {
        if (message == null || message.isEmpty()) {
            System.out.println("Please enter a commit message.");
            System.exit(0);
        }

        Stage stage = readObject(STAGE_FILE, Stage.class);

        if (stage.added.isEmpty() && stage.removed.isEmpty()) {
            System.out.println("No changes added to the commit.");
            System.exit(0);
        }

        Commit parentCommit = getHeadCommit();
        Map<String, String> newFileBlobTable = new HashMap<>(parentCommit.fileBlobTable);

        for (String name : stage.added.keySet()) {
            String blobID = stage.added.get(name);
            newFileBlobTable.put(name, blobID);
        }
        for (String name : stage.removed) {
            newFileBlobTable.remove(name);
        }
        writeObject(STAGE_FILE, new Stage());

        String timeStamp = formatDate(new Date());
        Commit headCommit = new Commit(message, timeStamp, getHeadCommitID(), null, newFileBlobTable);

        String headCommitID = headCommit.saveCommit();
        File headBrachFile = getHeadBranchFile();

        writeContents(headBrachFile, headCommitID);
    }

    public static void log() {
        String curCommitID = getHeadCommitID();

        while (true) {
            Commit curCommit = Commit.fromFile(curCommitID);
            System.out.println("===");
            System.out.println("commit " + curCommitID);
            if (curCommit.parent != null && curCommit.mergeParent != null) {
                System.out.println("Merge: " + curCommit.parent.substring(0, 7) + " " + curCommit.mergeParent.substring(0, 7));
            }
            System.out.println("Date: " + curCommit.commitTime);
            System.out.println(curCommit.logMessage);
            System.out.println();

            if (curCommit.parent == null) break;
            else {
                curCommitID = curCommit.parent;
            }
        }
    }

    private static void writeBlobToCWD(Commit commit, String name) {
        if (!commit.fileBlobTable.containsKey(name)) {
            System.out.println("File does not exist in that commit.");
            System.exit(0);
        }

        String blobID = commit.fileBlobTable.get(name);
        File blob = join(OBJECTS_FOLDER, blobID);

        byte[] content = readContents(blob);
        File file = join(CWD, name);
        writeContents(file, content);
    }

    public static void checkoutFile(String name) {
        Commit headCommit = getHeadCommit();
        writeBlobToCWD(headCommit, name);
    }

    private static String getFullID(String shortID) {
        if (shortID.length() == 40) {
            if (join(OBJECTS_FOLDER, shortID).exists()) {
                return shortID;
            } else {
                return null;
            }
        }
        List<String> objectIDs = plainFilenamesIn(OBJECTS_FOLDER);
        for (String fullID : objectIDs) {
            if (fullID.startsWith(shortID)) {
                return fullID;
            }
        }
        return null;
    }

    public static void checkoutCommitFile(String commitID, String name) {
        commitID = getFullID(commitID);
        if (commitID == null) {
            System.out.println("No commit with that id exists.");
            System.exit(0);
        }
        File commitFile = join(OBJECTS_FOLDER, commitID);
        Commit commit = readObject(commitFile, Commit.class);
        if (!commit.fileBlobTable.containsKey(name)) {
            System.out.println("File does not exist in that commit.");
            System.exit(0);
        }
        writeBlobToCWD(commit, name);
    }

    private static void rebuildCWD(Commit targetCommit) {
        List<String> cwdFiles = plainFilenamesIn(CWD);
        Commit currentCommit = getHeadCommit();

        for (String name : cwdFiles) {
            if (!currentCommit.fileBlobTable.containsKey(name)) {
                if (targetCommit.fileBlobTable.containsKey(name)) {
                    System.out.println("There is an untracked file in the way; delete it, or add and commit it first.");
                    System.exit(0);
                }
            }
        }

        for (String name : currentCommit.fileBlobTable.keySet()) {
            if (!targetCommit.fileBlobTable.containsKey(name)) {
                restrictedDelete(join(CWD, name));
            }
        }

        for (String name : targetCommit.fileBlobTable.keySet()) {
            writeBlobToCWD(targetCommit, name);
        }

        writeObject(STAGE_FILE, new Stage());
    }

    public static void checkoutBranch(String branchID) {
        File targetBranchFile = join(HEADS_FOLDER, branchID);
        if (!targetBranchFile.exists()) {
            System.out.println("No such branch exists.");
            System.exit(0);
        }
        if  (branchID.equals(readContentsAsString(HEAD_FILE))) {
            System.out.println("No need to checkout the current branch.");
            System.exit(0);
        }

        String targetCommitID = readContentsAsString(targetBranchFile);
        Commit targetCommit = Commit.fromFile(targetCommitID);

        rebuildCWD(targetCommit);

        writeContents(HEAD_FILE, branchID);
    }

    public static void remove(String name) {
        Stage stage = readObject(STAGE_FILE, Stage.class);
        Commit currentCommit = getHeadCommit();

        boolean isStaged = stage.added.containsKey(name);
        boolean isTracked = currentCommit.fileBlobTable.containsKey(name);

        if (!isStaged && !isTracked) {
            System.out.println("No reason to remove the file.");
            System.exit(0);
        }
        if (isStaged) {
            stage.added.remove(name);
        }
        if (isTracked) {
            stage.removed.add(name);
            restrictedDelete(join(CWD, name));
        }

        writeObject(STAGE_FILE, stage);
    }

    private static void printModifications(Commit headCommit, Stage stage, List<String> cwdFiles) {
        List<String> modifications = new ArrayList<>();

        for (String name : headCommit.fileBlobTable.keySet()) {
            File file = join(CWD, name);
            if (file.exists()) {
                String blobID = sha1(readContents(file));
                if (!Objects.equals(headCommit.fileBlobTable.get(name), blobID) && !stage.added.containsKey(name)) {
                    modifications.add(name + " (modified)");
                }
            } else if (!stage.removed.contains(name)){
                modifications.add(name + " (deleted)");
            }
        }

        for (String name : stage.added.keySet()) {
            File file = join(CWD, name);
            if (!file.exists()) {
                modifications.add(name + " (deleted)");
            } else {
                String currentBlob = sha1(readContents(file));
                if (!Objects.equals(currentBlob, stage.added.get(name))) {
                    modifications.add(name + " (modified)");
                }
            }
        }

        Collections.sort(modifications);
        for (String line : modifications) {
            System.out.println(line);
        }
    }

     private static void printUntracked(Commit headCommit, Stage stage, List<String> cwdFiles) {
         List<String> untracked = new ArrayList<>();
         for (String name : cwdFiles) {
             if (!headCommit.fileBlobTable.containsKey(name) && !stage.added.containsKey(name)) {
                 untracked.add(name);
             }
         }
         Collections.sort(untracked);
         for (String name : untracked) {
             System.out.println(name);
         }
     }

    public static void status() {
        System.out.println("=== Branches ===");
        String currentBranch = readContentsAsString(HEAD_FILE);
        List<String> branches = plainFilenamesIn(HEADS_FOLDER);
        Collections.sort(branches);
        for (String branchID : branches) {
            if (branchID.equals(currentBranch)) {
                System.out.println("*" + branchID);
            } else {
                System.out.println(branchID);
            }
        }
        System.out.println();

        Stage stage = readObject(STAGE_FILE, Stage.class);

        System.out.println("=== Staged Files ===");
        List<String> addedFiles = new ArrayList<>(stage.added.keySet());
        Collections.sort(addedFiles);
        for (String name : addedFiles) {
            System.out.println(name);
        }
        System.out.println();

        System.out.println("=== Removed Files ===");
        List<String> removedFiles = new ArrayList<>(stage.removed);
        Collections.sort(removedFiles);
        for (String name : removedFiles) {
            System.out.println(name);
        }
        System.out.println();
        Commit headCommit = getHeadCommit();
        List<String> cwdFiles = plainFilenamesIn(CWD);
        System.out.println("=== Modifications Not Staged For Commit ===");
        printModifications(headCommit, stage, cwdFiles);
        System.out.println();
        System.out.println("=== Untracked Files ===");
        printUntracked(headCommit, stage, cwdFiles);
        System.out.println();
    }

    public static void addBranch(String name) {
        File newBranch = join(HEADS_FOLDER, name);

        if (newBranch.exists()) {
            System.out.println("A branch with that name already exists.");
            System.exit(0);
        }

        writeContents(newBranch, getHeadCommitID());
    }

    public static void removeBranch(String name) {
        File curBranch = join(HEADS_FOLDER, name);
        if (!curBranch.exists()) {
            System.out.println("A branch with that name does not exist.");
            System.exit(0);
        }
        if (curBranch.equals(getHeadBranchFile())) {
            System.out.println("Cannot remove the current branch.");
            System.exit(0);
        }

        curBranch.delete();
    }

    public static void globalLog() {
        List<String> objects = plainFilenamesIn(OBJECTS_FOLDER);
        for (String name : objects) {
            try {
                File commitFile = join(OBJECTS_FOLDER, name);
                Commit commit = readObject(commitFile, Commit.class);

                System.out.println("===");
                System.out.println("commit " + name);
                System.out.println("Date: " + commit.commitTime);
                System.out.println(commit.logMessage);
                System.out.println();
            } catch (Exception e) {
                continue;
            }
        }
    }

    public static void find(String targetMessage) {
        List<String> objects = plainFilenamesIn(OBJECTS_FOLDER);
        boolean found = false;
        for (String name : objects) {
            try {
                File commitFile = join(OBJECTS_FOLDER, name);
                Commit commit = readObject(commitFile, Commit.class);
                if (commit.logMessage.equals(targetMessage)) {
                    found = true;

                    System.out.println(name);
                }
            } catch (IllegalArgumentException e) {
                continue;
            }
        }
        if (!found) {
            System.out.println("Found no commit with that message.");
            System.exit(0);
        }
    }

    public static void reset(String targetCommitID) {
        targetCommitID = getFullID(targetCommitID);
        if (targetCommitID == null) {
            System.out.println("No commit with that id exists.");
            System.exit(0);
        }

        File targetCommitFile = join(OBJECTS_FOLDER, targetCommitID);
        Commit targetCommit = readObject(targetCommitFile, Commit.class);

        rebuildCWD(targetCommit);

        String currentBranchID = readContentsAsString(HEAD_FILE);
        File currentBranch = join(HEADS_FOLDER, currentBranchID);
        writeContents(currentBranch, targetCommitID);
    }

    private static String processConflict(String name, String currentBlobID, String givenBlobID) {
        String curContent = "";
        if  (currentBlobID != null) {
            byte[] content = readContents(join(OBJECTS_FOLDER, currentBlobID));
            curContent = new String(content, java.nio.charset.StandardCharsets.UTF_8);
        }

        String givenContent = "";
        if  (givenBlobID != null) {
            byte[] content = readContents(join(OBJECTS_FOLDER, givenBlobID));
            givenContent = new String(content, java.nio.charset.StandardCharsets.UTF_8);
        }

        String conflictText = "<<<<<<< HEAD\n"
                            + curContent
                            + "=======\n"
                            + givenContent
                            + ">>>>>>>\n";
        writeContents(join(CWD, name), conflictText);

        byte[] content = readContents(join(CWD, name));
        String blobID = sha1(content);
        writeContents(join(OBJECTS_FOLDER, blobID), content);
        return blobID;
    }

    private static String findSplitCommitID(String currentCommitID, String givenCommitID) {
        HashSet<String> ancestors = new HashSet<>();
        Queue<String> q = new ArrayDeque<>();
        q.add(currentCommitID);
        while (!q.isEmpty()) {
            String id = q.poll();
            if (!ancestors.contains(id)) {
                ancestors.add(id);
                Commit c = Commit.fromFile(id);
                if (c.parent != null) {
                    q.add(c.parent);
                }
                if (c.mergeParent != null) {
                    q.add(c.mergeParent);
                }
            }
        }
        HashSet<String> visited = new HashSet<>();
        q.add(givenCommitID);
        while (!q.isEmpty()) {
            String id = q.poll();
            if (ancestors.contains(id)) return id;
            if (!visited.contains(id)) {
                visited.add(id);
                Commit c = Commit.fromFile(id);
                if (c.parent != null) {
                    q.add(c.parent);
                }
                if (c.mergeParent != null) {
                    q.add(c.mergeParent);
                }
            }
        }
        return null;
    }

    private static void checkUntrackedFilesForMerge(Commit splitCommit, Commit givenCommit, Commit currentCommit) {
        List<String> cwdFiles = plainFilenamesIn(CWD);
        for (String name : cwdFiles) {
            if (!currentCommit.fileBlobTable.containsKey(name)) {
                String splitBlobID = splitCommit.fileBlobTable.get(name);
                String givenBlobID = givenCommit.fileBlobTable.get(name);

                if (!Objects.equals(splitBlobID, givenBlobID)) {
                    System.out.println("There is an untracked file in the way; delete it, or add and commit it first.");
                    System.exit(0);
                }
            }
        }
    }

    public static void merge(String givenBranchID) {
        if (!join(HEADS_FOLDER, givenBranchID).exists()) {
            System.out.println("A branch with that name does not exist.");
            System.exit(0);
        }

        String currentCommitID = getHeadCommitID();
        String givenCommitID = getCommitID(givenBranchID);
        String splitCommitID = findSplitCommitID(currentCommitID, givenCommitID);

        Stage stage = readObject(STAGE_FILE, Stage.class);
        if (!stage.added.isEmpty() || !stage.removed.isEmpty()) {
            System.out.println("You have uncommitted changes.");
            System.exit(0);
        }

        String currentBranchID = readContentsAsString(HEAD_FILE);
        if (Objects.equals(currentBranchID, givenBranchID)) {
            System.out.println("Cannot merge a branch with itself.");
            System.exit(0);
        }

        checkUntrackedFilesForMerge(Commit.fromFile(splitCommitID), Commit.fromFile(givenCommitID), Commit.fromFile(currentCommitID));

        if (Objects.equals(splitCommitID, givenCommitID)) {
            System.out.println("Given branch is an ancestor of the current branch.");
            System.exit(0);
        }
        if (Objects.equals(splitCommitID, currentCommitID)) {
            rebuildCWD(Commit.fromFile(givenCommitID));
            writeContents(getHeadBranchFile(), givenCommitID);

            System.out.println("Current branch fast-forwarded.");
            System.exit(0);
        }

        HashSet<String> files = new HashSet<>();
        Commit splitCommit = Commit.fromFile(splitCommitID);
        Commit currentCommit = Commit.fromFile(currentCommitID);
        Commit givenCommit = Commit.fromFile(givenCommitID);
        for (String name : splitCommit.fileBlobTable.keySet()) {
            files.add(name);
        }
        for (String name : currentCommit.fileBlobTable.keySet()) {
            files.add(name);
        }
        for (String name : givenCommit.fileBlobTable.keySet()) {
            files.add(name);
        }

        Map<String, String> newFileTable = new HashMap<>(currentCommit.fileBlobTable);

        boolean hasConflict = false;
        for (String name : files) {
            String splitBlobID = splitCommit.fileBlobTable.get(name);
            String currentBlobID = currentCommit.fileBlobTable.get(name);
            String givenBlobID = givenCommit.fileBlobTable.get(name);

            if (givenBlobID != null && Objects.equals(splitBlobID, currentBlobID) && !Objects.equals(splitBlobID, givenBlobID)) {
                checkoutCommitFile(givenCommitID, name);

                stage.added.put(name, givenBlobID);
                stage.removed.remove(name);

                newFileTable.put(name, givenBlobID);
            } else if (!Objects.equals(splitBlobID, currentBlobID) && Objects.equals(splitBlobID, givenBlobID)) {
                continue;
            } else if (Objects.equals(currentBlobID, givenBlobID)) {
                continue;
            } else if (splitBlobID == null && currentBlobID != null && givenBlobID == null) {
                continue;
            } else if (splitBlobID == null && currentBlobID == null && givenBlobID != null) {
                checkoutCommitFile(givenCommitID, name);

                stage.added.put(name, givenBlobID);
                stage.removed.remove(name);
                newFileTable.put(name, givenBlobID);
            } else if (Objects.equals(splitBlobID, currentBlobID) && givenBlobID == null) {
                restrictedDelete(join(CWD, name));
                stage.added.remove(name);
                stage.removed.add(name);
                newFileTable.remove(name);
            } else if (Objects.equals(splitBlobID, givenBlobID) && currentBlobID == null) {
                continue;
            } else {
                String blobID = processConflict(name, currentBlobID, givenBlobID);
                stage.added.put(name, blobID);

                newFileTable.put(name, blobID);

                hasConflict = true;
            }
        }

        writeObject(STAGE_FILE, stage);
        if (hasConflict) {
            System.out.println("Encountered a merge conflict.");
//            System.exit(0);
        }

        String message = "Merged " + givenBranchID + " into " + currentBranchID + ".";

        String timeStamp = formatDate(new Date());
        Commit mergeCommit = new Commit(message, timeStamp, currentCommitID, givenCommitID, newFileTable);
        String mergeCommitID = mergeCommit.saveCommit();

        writeContents(getHeadBranchFile(), mergeCommitID);
        writeObject(STAGE_FILE, new Stage());
    }

    public static void addRemote(String remoteName, String remotePath) {
        HashMap<String, String> remotes = new HashMap<>();
        if (REMOTE_FILE.exists()) {
            remotes = readObject(REMOTE_FILE, HashMap.class);
        }

        if (remotes.containsKey(remoteName)) {
            System.out.println("A remote with that name already exists.");
            System.exit(0);
        }

        String normalizedPath = remotePath.replace("/", File.separator);
        remotes.put(remoteName, normalizedPath);
        writeObject(REMOTE_FILE, remotes);
    }

    public static void removeRemote(String remoteName) {
        HashMap<String, String> remotes = new HashMap<>();
        if (REMOTE_FILE.exists()) {
            remotes = readObject(REMOTE_FILE, HashMap.class);
        }

        if (!remotes.containsKey(remoteName)) {
            System.out.println("A remote with that name does not exist.");
            System.exit(0);
        }

        remotes.remove(remoteName);
        writeObject(REMOTE_FILE, remotes);
    }

    private static void copyCommitsAndBlobs(File remoteGitletDir, String remoteHeadCommitID) {
        File remoteObjectsDir = join(remoteGitletDir, "objects");

        Queue<String> q = new ArrayDeque<>();
        q.add(remoteHeadCommitID);
        Set<String> visited = new HashSet<>();

        while (!q.isEmpty()) {
            String id = q.poll();
            File localObjectFile = join(OBJECTS_FOLDER, id);
            File remoteObjectFile = join(remoteObjectsDir, id);

            if (!localObjectFile.exists()) {
                if (!remoteObjectFile.exists()) {
                    continue;
                }
                byte[] content = readContents(remoteObjectFile);
                writeContents(localObjectFile, content);
            }

            if (visited.contains(id)) {
                continue;
            }
            visited.add(id);

            try {
                Commit c = readObject(remoteObjectFile, Commit.class);

                if (c.parent != null) {
                    q.add(c.parent);
                }
                if (c.mergeParent != null) {
                    q.add(c.mergeParent);
                }

                for(String blobID : c.fileBlobTable.values()) {
                    if (!join(OBJECTS_FOLDER, blobID).exists()) {
                        File remoteBlobFile = join(remoteObjectsDir, blobID);
                        if (remoteBlobFile.exists()) {
                            writeContents(join(OBJECTS_FOLDER, blobID), readContents(remoteBlobFile));
                        }
                    }
                }
            } catch (Exception e) {
                continue;
            }
        }
    }

    public static void fetch(String remoteName, String remoteBranchName) {
        HashMap<String, String> remotes = new HashMap<>();
        if (REMOTE_FILE.exists()) {
            remotes = readObject(REMOTE_FILE, HashMap.class);
        }
        if (!remotes.containsKey(remoteName)) {
            System.out.println("Remote directory not found.");
            System.exit(0);
        }

        File remoteGitletDir = new File(remotes.get(remoteName));
        if (!remoteGitletDir.exists()) {
            System.out.println("Remote directory not found.");
            System.exit(0);
        }

        File remoteHeadsDir = join(remoteGitletDir, "heads");
        File remoteBranchFile = join(remoteHeadsDir, remoteBranchName);

        if(!remoteBranchFile.exists()) {
            System.out.println("That remote does not have that branch.");
            System.exit(0);
        }

        String remoteHeadID = readContentsAsString(remoteBranchFile);

        copyCommitsAndBlobs(remoteGitletDir, remoteHeadID);

        File localRemoteBranchDir = join(HEADS_FOLDER, remoteName);
        localRemoteBranchDir.mkdirs();
        File localRemoteBranchFile = join(localRemoteBranchDir, remoteBranchName); // heads/origin/master

        writeContents(localRemoteBranchFile, remoteHeadID);
    }

    private static boolean isAncestor(String remoteCommitId, String currentCommitID) {
        Queue<String> q = new ArrayDeque<>();
        q.add(currentCommitID);
        Set<String> visited = new HashSet<>();

        while (!q.isEmpty()) {
            String id = q.poll();
            if (id.equals(remoteCommitId)) {
                return true;
            }
            if (visited.contains(id)) {
                continue;
            }
            visited.add(id);
            Commit c = Commit.fromFile(id);

            if (c.parent != null) {
                q.add(c.parent);
            }
            if (c.mergeParent != null) {
                q.add(c.mergeParent);
            }
        }
        return false;
    }

    private static void pushObjectsToRemote(File remoteGitletDir, String headCommitID) {
        File remoteObjectsDir = join(remoteGitletDir, "objects");

        Queue<String> q = new ArrayDeque<>();
        q.add(headCommitID);
        Set<String> visited = new HashSet<>();

        while (!q.isEmpty()) {
            String id = q.poll();

            File localObjectFile = join(OBJECTS_FOLDER, id);
            File remoteObjectFile = join(remoteObjectsDir, id);

            if (remoteObjectFile.exists()) {
                continue;
            }

            byte[] content = readContents(localObjectFile);
            writeContents(remoteObjectFile, content);

            if (visited.contains(id)) {
                continue;
            }
            visited.add(id);

            try {
                Commit c = readObject(remoteObjectFile, Commit.class);

                if (c.parent != null) {
                    q.add(c.parent);
                }
                if (c.mergeParent != null) {
                    q.add(c.mergeParent);
                }

                for(String blobID : c.fileBlobTable.values()) {
                    if (!join(remoteObjectsDir, blobID).exists()) {
                        File blobFile = join(OBJECTS_FOLDER, blobID);
                        if (blobFile.exists()) {
                            writeContents(join(remoteObjectsDir, blobID), readContents(blobFile));
                        }
                    }
                }
            } catch (Exception e) {
                continue;
            }
        }
    }

    public static void push(String remoteName, String remoteBranchName) {
        HashMap<String, String> remotes = new HashMap<>();
        if (REMOTE_FILE.exists()) {
            remotes = readObject(REMOTE_FILE, HashMap.class);
        }
        if (!remotes.containsKey(remoteName)) {
            System.out.println("Remote directory not found.");
            System.exit(0);
        }

        File remoteGitletDir = new File(remotes.get(remoteName));
        if (!remoteGitletDir.exists()) {
            System.out.println("Remote directory not found.");
            System.exit(0);
        }

        File remoteHeadsDir = join(remoteGitletDir, "heads");
        File remoteBranchFile = join(remoteHeadsDir, remoteBranchName);

        if (!remoteBranchFile.exists()) {
            String remoteHeadID = readContentsAsString(remoteBranchFile);
            String currentHeadID = getHeadCommitID();

            if (!isAncestor(remoteHeadID, currentHeadID)) {
                System.out.println("Please pull down remote changes before pushing.");
                System.exit(0);
            }
        }

        String headCommitID = getHeadCommitID();
        pushObjectsToRemote(remoteGitletDir, headCommitID);

        writeContents(remoteBranchFile, headCommitID);
    }

    public static void pull(String remoteName, String remoteBrachName) {
        fetch(remoteName, remoteBrachName);

        String mergeBranchName = remoteName + File.separator +remoteBrachName;
        merge(mergeBranchName);
    }
}

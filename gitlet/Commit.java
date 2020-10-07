package gitlet;


import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.Map;
import java.util.HashMap;


/** This class represents a singular commit object with info such as
 * a log message, a blob reference, commit date (and author), a
 * reference to a tree, and a reference to a parent commit.
 * @author Eduardo Huerta Mercado
 */

public class Commit implements Serializable {
    /** Constructor method for the commit class.
     * @param m the commit message.
     * @param t the commit time.
     * @param tree the TreeP.
     * @param stage the stage I was created from.
     * @param parent my parents.
     */
    public Commit(String m, Date t, TreeP tree,
                  Stage stage, String parent) {

        myMessage = m;
        myDate = t;
        myDateStr = t.toString().substring(0, GETRIDOFTIME1)
                + t.toString().substring(GETRIDOFTIME2) + " -0800";
        myTree = tree;
        myFilePointers = new HashMap<>();
        oldFileToRepoLoc = new HashMap<>();
        myUntrackedFiles = new HashSet<>();
        allMyParents = new ArrayList<>();
        myStage = stage;
        myID = "commit" + stage.getMyID();
        myParent = null;
        setMyParent(parent);
        Utils.writeObject(new File(objectRepo + myID), this);
    }

    /** Saves changes made to this commit.
     */
    public void saveCommit() {
        Utils.writeObject(new File(objectRepo + myID), this);
    }

    /** Sets my parent commit to this commit.
     * @param parentID the previous commit leading ot this one.
     */
    public void setMyParent(String parentID) {
        if (parentID == null) {
            myParent = null;
            return;
        }

        Commit commit = Utils.readObject(new File(
                objectRepo + parentID), Commit.class);

        myParent = commit.getMyID();
        for (String keys: commit.oldFileToRepoLoc.keySet()) {
            if (!myTree.getCurBranch().getMyStage()
                    .getRemovedFiles().contains(keys)) {
                oldFileToRepoLoc.put(keys, commit.oldFileToRepoLoc.get(keys));
            }
        }
        for (String keys: commit.getMyFilePointers().keySet()) {
            commit.processString(keys);
            if (!commit.getMyUntrackedFiles().contains(keys)) {
                myFilePointers.put(keys, commit.getMyFilePointers().get(keys));
            }
        }
        myUntrackedFiles.addAll(commit.getMyUntrackedFiles());
        allMyParents.add(parentID);
    }

    /** Creates a commit folder in .gitlet\objectRepository. If this is
     * called by init()then it also creates the objectRepository.
     * Otherwise, it will go through the files within it's stage and
     * write into the commit folder. If the object is the same as it
     * used to be (checked by sha1(readContentsAsString(f))) then it
     * will do nothing.
     */
    public void addCommit() {
        File myCommitFolder = new File(objectRepo
                + "folder" + myID + separator);
        myCommitFolder.mkdirs();
        File commitFile = new File(objectRepo + myID);
        if (myParent == null) {
            Utils.writeObject(commitFile, this);
        } else {
            Commit myParentCommit = Utils.readObject(new File(objectRepo
                    + myParent), Commit.class);

            String stageDirectory = ".gitlet" + separator
                    + "stages" + separator + myStage.getMyID() + separator;

            for (String fileName: Utils.plainFilenamesIn(stageDirectory)) {
                if (myStage.getStagedFileNames().contains(fileName)
                        && !myUntrackedFiles.contains(
                        processStringRev(fileName))) {

                    String currContents = Utils.readContentsAsString(
                            new File(stageDirectory + fileName));

                    if (myParentCommit.getOldFileToRepoLoc()
                            .containsKey(fileName)) {
                        File oldFile = new File(myParentCommit
                                .getOldFileToRepoLoc().get(fileName));
                        String prevContents =
                                Utils.readContentsAsString(oldFile);

                        if (!currContents.equals(prevContents)) {
                            oldFileToRepoLoc.replace(fileName,
                                    myCommitFolder.getPath()
                                            + separator + fileName);
                            File theFile =
                                    new File(processStringRev(fileName));
                            String contentString =
                                    Utils.readContentsAsString(theFile);
                            myFilePointers.replace(fileName,
                                    Utils.sha1(stageDirectory + fileName));
                            Utils.writeContents(new File(
                                    myCommitFolder.getPath() + separator
                                            + fileName), contentString);
                        }
                    } else {
                        oldFileToRepoLoc.put(fileName, myCommitFolder.getPath()
                                + separator + fileName);
                        File writeFile = new File(stageDirectory + fileName);
                        String contentString =
                                Utils.readContentsAsString(writeFile);
                        myFilePointers.put(fileName,
                                Utils.sha1(stageDirectory + fileName));
                        Utils.writeContents(new File(myCommitFolder.getPath()
                                + separator + fileName), contentString);
                    }
                }
            }
            Utils.writeObject(commitFile, this);
        }
    }

    /** Changes the "\" character into a "@" for the provided
     * string. Intended for acquiring files which are within
     * another directory. Note that this is ONLY DONE for the
     * file creation process. The original name is still used
     * for all the map structures.
     * @param name the name we want to process.
     * @return the new file name.
     */
    public String processString(String name) {
        if (name.contains("\\")) {
            name = name.replace("\\", "@");
        }
        return name;
    }

    /** Basically processString() but in reverse :P.
     * @param name the name we want to process back.
     * @return the original file name.
     */
    public String processStringRev(String name) {
        if (name.contains("@")) {
            name = name.replace("@", "\\");
        }
        return name;
    }

    /** The toString structure of each commit. Note that this is formatted
     * to the exact format specified by what log requires. Note that the
     * supposed "commitID" has ALREADY BEEN processed.
     * @return String of what log should print.
     */
    @Override
    public String toString() {
        StringBuilder myStringRepr = new StringBuilder();
        myStringRepr.append("===\n");
        myStringRepr.append("commit " + myID.substring(6) + "\n");
        if (mergeCommit) {
            myStringRepr.append("Merge:");
            for (String parents: allMyParents) {
                myStringRepr.append(" " + parents.substring(7, 14));
            }
            myStringRepr.append("\n");
        }
        myStringRepr.append("Date: " + myDateStr + "\n");
        myStringRepr.append(myMessage + "\n");
        return myStringRepr.toString();
    }


    /** The message that came with this commit.
     */
    private String myMessage;
    /** Get method for my prayer to the CS gods.
     * @return heathens and blasphemers instead.
     */
    public String getMyMessage() {
        return myMessage;
    }

    /** The time this commit was made.
     */
    private Date myDate;
    /** Get method for my nonexistant Date... EMILY WHY!!!!
     * @return I cri.
     */
    public Date getMyDate() {
        return myDate;
    }

    /** The parent's (the commit leading to this one) commit ID.
     */
    private String myParent;

    /** "I am lost. Please help me" - some kid in a mall probably
     * @return the non-gender-associated-guardian-of-the-small-creature.
     */
    public String getMyParent() {
        return myParent;
    }
    /** Get method for my Parent commit but resurrecting him/her/it
     * from the graveyard we know as "objectRepository".
     * @return Necromancy.
     */
    public Commit getMyParentCommit() {
        File parentFile = new File(objectRepo + myParent);
        try {
            Commit retCommit = Utils.readObject(parentFile, Commit.class);
            return retCommit;
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    /** Sets the current commit to a merge commit.
     */
    public void setMeToMergeCommit() {
        mergeCommit = true;
        Utils.writeObject(new File(objectRepo + myID), this);
    }

    /** variable checking whether I am a merge commit.
     */
    private boolean mergeCommit = false;

    /** HashSet of all my parent's commit IDs.
     */
    private ArrayList<String> allMyParents;
    /** Get method for the ArrayList containing all my parent's
     * commit IDs.
     * @return the Arraylist containing all my parent's commit IDs.
     */
    public ArrayList<String> getAllMyParents() {
        return allMyParents;
    }

    /** The time this commit was made, in string, and with the UTC offset.
     */
    private String myDateStr;
    /** Get method for my Date.
     * @return the date of my inception.
     */
    public String getMyDateStr() {
        return myDateStr;
    }

    /** The tree that this particular commit belongs to.
     */
    private TreeP myTree;
    /** Get method for my lumberjack.
     * @return and his pet russel terrier.
     */
    public TreeP getMyTree() {
        return myTree;
    }

    /** The stage that I had when I made this commit.
     */
    private Stage myStage;
    /** Get method for my stage.
     * @return stages
     */
    public Stage getMyStage() {
        return myStage;
    }

    /** Map from the file NAME to the file's OWN SHA1 ID.
     * Inherits from parent and updates iff file is different
     * from parent commit's version.
     */
    private Map<String, String> myFilePointers;
    /** Get method for my File pointers. I never really had
     * a use for this guy other than checking whether something
     * was in something else. Could probs have done that with
     * oldFileToRepoLoc, but don't fix what's not broken amarite?
     * @return that thing.
     */
    public Map<String, String> getMyFilePointers() {
        return myFilePointers;
    }

    /** A (hopefully) unique commit ID. Created from the Date and
     * a random integer generated by the Random class belonging to
     * the TreeP. Should be in the format "commit[sha1]"
     */
    private String myID;
    /** Get method for my commit ID. Should be in the format:
     * "commit[sha1 of stage at the moment of my creation]"
     * @return my ID.
     */
    public String getMyID() {
        return myID;
    }

    /** The separator symbol.
     */
    private String separator = File.separator;

    /** The directory of the objectRepository.
     */
    private String objectRepo = ".gitlet" + separator
            + "objectRepository" + separator;

    /** Map of the old fileName (which is also it's directory in it's
     * working directory) to the copy of the file located in the
     * object repository.
     * NOTE that both the keys and the values are the processed versions
     * of the filenames.
     */
    private Map<String, String> oldFileToRepoLoc;
    /** Get method for my penis.
     * @return I'm just kidding, it's for this mapping of
     * filenames to repository locations.
     */
    public Map<String, String> getOldFileToRepoLoc() {
        return oldFileToRepoLoc;
    }

    /** Hashset of untracked files for this particular commit.
     * NOTE THAT the names of the files are UNPROCESSED as they
     * are in the working directory.
     */
    private HashSet<String> myUntrackedFiles;
    /** Get method for my untracked files hashset. Note that
     * the filenames contained are PROCESSED versions.
     * @return exactly what I stated above
     */
    public HashSet<String> getMyUntrackedFiles() {
        return myUntrackedFiles;
    }

    /** a magic number for wizards knowledge only.
     */
    private static final int GETRIDOFTIME1 = 19;

    /** a magic number for witches knowledge only.
     */
    private static final int GETRIDOFTIME2 = 23;
}

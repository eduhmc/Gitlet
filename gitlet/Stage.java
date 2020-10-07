package gitlet;

import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;

/** The working stage in which all the files are added to and removed form.
 * Once the stage gets committed, everything on the stage becomes a part of
 * the next commit as it transcends into codehood.
 * @author Eduardo Huerta Mercado
 */
public class Stage implements Serializable {

    /** Constructor. Stage associated to LATEST commit.
     * @param tree the TreePr
     * @param date the date I was founded
     */
    public Stage(TreeP tree, Date date) {
        stagedFileNames = new HashSet<>();
        removedFiles = new ArrayList<String>();
        myID = Utils.sha1(date.toString() + tree.getRandomGen().nextDouble());
        myFolder = new File(".gitlet"
                + separator + "stages" + separator + myID);
        myFolder.mkdirs();
        myDate = date;
        saveStage();
    }

    /** Saves the stage object to the "stages" directory.
     */
    public void saveStage() {
        File myFile = new File(".gitlet"
                + separator + "stages" + separator + "stage" + myID);
        Utils.writeObject(myFile, this);
    }

    /** Set method to update my commit. Should be ran after
     * every single stage creation.
     * @param commit the commit I want to make mine
     */
    void setMyCommit(Commit commit) {
        curCommit = commit;
        saveStage();
    }

    /** Method run by the "add" command. It will add the input
     * fileName onto the stage. If the file has been marked for
     * removal, it removes the mark. If the file is contained
     * within the previous commit and is the same version, then
     * it is not staged and the method terminates. If it had
     * already been staged previously, the current version is
     * deleted and the updated version is staged instead.
     * @param fileName the name of the file to be staged from
     *                 within the working directory.
     */
    public void add(String fileName) {
        File thisFile = new File(fileName);

        if (!thisFile.exists()) {
            System.out.println("File does not exist.");
            return;
        }

        if (removedFiles.contains(fileName)) {
            removedFiles.remove(fileName);
        }

        if (curCommit.getMyUntrackedFiles().contains(fileName)) {
            curCommit.getMyUntrackedFiles().remove(fileName);
        }

        if (curCommit.getOldFileToRepoLoc().containsKey(fileName)) {
            File previousFile = new File(curCommit
                    .getOldFileToRepoLoc().get(fileName));

            if (Utils.readContentsAsString(previousFile)
                    .equals(Utils.readContentsAsString(thisFile))) {
                File writtenFile = new File(
                        myFolder.getPath() + separator + fileName);
                stagedFileNames.remove(fileName);
                writtenFile.delete();
                return;
            }
        }

        fileName = processString(fileName);
        File writtenFile = new File(myFolder.getPath() + separator + fileName);

        if (stagedFileNames.contains(fileName)) {
            writtenFile.delete();
            stagedFileNames.remove(fileName);
        }
        stagedFileNames.add(fileName);
        String contentString = Utils.readContentsAsString(
                new File(curCommit.processStringRev(fileName)));
        Utils.writeContents(writtenFile, contentString);

        Utils.writeObject(new File(objectRepo
                + curCommit.getMyID()), curCommit);
        saveStage();
    }

    /** Changes the "\" character into a "@" for the provided
     * string. Intended for acquiring files which are within
     * another directory. Note that this is ONLY DONE for the
     * file creation process. The original name is still used
     * for all the map structures.
     * @param name the name we wanna process.
     * @return the new file name.
     */
    public String processString(String name) {
        if (name.contains("\\")) {
            name = name.replace("\\", "@");
        }
        return name;
    }

    /** Basically processString() but in reverse :P.
     * @param name the name we wanna process backwards.
     * @return the original file name.
     */
    public String processStringRev(String name) {
        if (name.contains("@")) {
            name = name.replace("@", "\\");
        }
        return name;
    }

    /** Helper function used to remove files from
     * removedFiles. This is because saving and whatnot
     * doesn't seem to like me.
     * @param fileName the file we wanna remove.
     * @return boolean value representing whether the
     * removal was a success
     */
    public boolean removedFilesRemove(String fileName) {
        boolean returnValue = removedFiles.remove(fileName);
        saveStage();
        return returnValue;
    }

    @Override
    public String toString() {
        StringBuilder myStringrepr = new StringBuilder();
        myStringrepr.append("----- Staged Files ----- \n");
        myStringrepr.append("size: " + stagedFileNames.size() + "\n");
        for (String files: stagedFileNames) {
            myStringrepr.append(files + "\n");
        }
        myStringrepr.append(stagedFileNames.hashCode() + "\n");
        myStringrepr.append("----- Current Commit ----- \n");

        return myStringrepr.toString();
    }

    /** A pointer to the previous commit. Note that
     * as of now this thing is useless other than getting
     * some neat helper functions.
     */
    private Commit curCommit;
    /** Get method for useless cunts.
     * @return blond burgers.
     */
    public Commit getCurCommit() {
        return curCommit;
    }

    /** All the files staged for commit's names.
     * NOTE THAT the names stored ARE PROCESESSED.
     */
    private HashSet<String> stagedFileNames;
    /** Get method for pop stars.
     * @return foxy ladies
     */
    public HashSet<String> getStagedFileNames() {
        return stagedFileNames;
    }

    /** The ID for my stage.
     */
    private String myID;
    /** Get method for my ID.
     * @return passport numbers for all the presidents.
     */
    public String getMyID() {
        return myID;
    }

    /** The separator character for my OS. It should be a "/".
     */
    private static String separator = File.separator;

    /** the folder that contains all my stuff.
     */
    private File myFolder;
    /** Get method for my home.
     * @return houses.
     */
    public File getMyFolder() {
        return myFolder;
    }

    /** ArrayList tracking fileNames (also path to the file in
     * the working directory) to be removed.
     * NOTE THAT the names here are UNPROCESSED
     */
    private ArrayList<String> removedFiles;

    /** Get method for the unloved files.
     * @return orphans.
     */
    public ArrayList<String> getRemovedFiles() {
        return removedFiles;
    }

    /** My date object.
     */
    private Date myDate;

    /** Get method for my date object.
     * @return my date
     */
    public Date getMyDate() {
        return myDate;
    }

    /** String representing the objectRepository's path.
     */
    private String objectRepo = ".gitlet"
            + separator + "objectRepository" + separator;
}


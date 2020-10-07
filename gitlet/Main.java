package gitlet;

import java.io.File;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.io.FileOutputStream;
import java.nio.file.FileSystems;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.HashSet;
import java.util.Arrays;
import java.util.Collections;


/** Driver class for Gitlet, the tiny kinda-stupid-but-more
 * -annoying-in-a-cute-kind-of-way version-control system.
 *  @author Eduardo Huerta Mercado
 */
public class Main implements Serializable {

    /** Helper function to get name of all files in the working
     * directory that does not include the original git functions.
     * @return List of all the fileNames.
     */
    private static ArrayList<String> workingDirFiles() {
        ArrayList<String> tempList = new ArrayList<>();
        tempList.addAll(Utils.plainFilenamesIn(
                FileSystems.getDefault().getPath(".").toString()));
        tempList.remove(".gitignore");
        tempList.remove("Makefile");
        tempList.remove("proj3.iml");
        return tempList;
    }

    /** Saves the current directory state in order to pull it
     * back out upon the next call.
     */
    static void saveDirectory() {
        if (directory == null) {
            return;
        }

        try {
            File f = new File(".gitlet" + separator + "repo" + separator);
            ObjectOutputStream dirStream =
                    new ObjectOutputStream(new FileOutputStream(f));
            dirStream.writeObject(directory);
            dirStream.close();
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }

    }

    /** Loads a previously saved directory state.
     * @return The TreeP structure representing the former directory.
     */
    static TreeP loadDirectory() {
        File f = new File(".gitlet" + separator + "repo" + separator);
        try {
            TreeP retTree = Utils.readObject(f, TreeP.class);
            return retTree;
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    /** For testing with the staff gitlet, deletes the exisitng ".gitlet"
     * repository.
     * @param directoryToBeDeleted the directory to be deleted.
     * @return boolean value denoting whether something was deleted.
     */
    private static boolean deleteRepository(File directoryToBeDeleted) {
        if (!directoryToBeDeleted.exists()) {
            System.out.println("there ain't nothin' to delete");
            return false;
        }
        File[] allContents = directoryToBeDeleted.listFiles();
        if (allContents != null) {
            for (File file : allContents) {
                deleteRepository(file);
            }
        }
        return directoryToBeDeleted.delete();
    }


    /** Method run by the "init" command. It aims to initialize a .gitlet
     * repository if ones does not already exist. And it creates a
     * Stage -> Commit -> Branch -> TreeP.
     */
    static void init() {
        File initialDirectory = new File(".gitlet" + separator);
        File stageFolder = new File(".gitlet" + separator + "stages");
        File objectRepository = new File(
                ".gitlet" + separator + "objectRepository");

        if (initialDirectory.exists()) {
            System.out.println(" A Gitlet version-control system "
                    + "already exists in the current directory.");
            return;
        }
        initialDirectory.mkdirs();
        stageFolder.mkdirs();
        objectRepository.mkdirs();

        directory = new TreeP();
        Date initialCommitDate = new Date();
        initialCommitDate.setTime(0);
        Stage initStage = new Stage(directory, initialCommitDate);
        Commit initCommit = new Commit("initial commit",
                initialCommitDate, directory, initStage, null);

        initCommit.addCommit();
        initStage.setMyCommit(initCommit);

        Stage nextCommitsStage = new Stage(directory, new Date());
        nextCommitsStage.setMyCommit(initCommit);

        Branch newBranch = new Branch("master", initCommit, directory);

        directory.getMyChildren().put(newBranch.getMyName(), newBranch);
        directory.setCurBranch(newBranch);
        newBranch.updateHead(initCommit);
        newBranch.updateStage(nextCommitsStage);
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
    static void add(String fileName) {
        Commit headCommit = directory.getCurBranch().getHeadCommit();
        directory.getCurBranch().getMyStage().add(fileName);
        if (headCommit.getMyUntrackedFiles().contains(fileName)) {
            headCommit.getMyUntrackedFiles().remove(fileName);
        }
        directory.getCurBranch().getMyStage().removedFilesRemove(fileName);
        headCommit.saveCommit();
    }

    /** Method run by the "commit" command.
     * @param message the message that comes with this commit.
     */
    static void commit(String message) {
        Date tempCommitDate = new Date();
        Branch curBranch = directory.getCurBranch();
        Stage newStage = new Stage(directory, tempCommitDate);

        Stage currStage = curBranch.getMyStage();
        Commit currCommit = curBranch.getHeadCommit();

        if (message.equals("")) {
            System.out.println("Please enter a commit message.");
            return;
        } else if (curBranch.getMyStage().getStagedFileNames().size() == 0) {
            try {
                Commit parentCommit = currCommit.getMyParentCommit();
                boolean newRemovedFile = false;
                if (parentCommit.getMyUntrackedFiles().size()
                        != currCommit.getMyUntrackedFiles().size()) {
                    newRemovedFile = true;
                } else {
                    for (String fileName: parentCommit.getMyUntrackedFiles()) {
                        if (!currCommit.getMyUntrackedFiles()
                                .contains(fileName)) {
                            newRemovedFile = true;
                        }
                    }
                }
                if (!newRemovedFile) {
                    System.out.println("No changes added to the commit.");
                    return;
                }
            } catch (NullPointerException e) {
                System.out.println("No changes added to the commit.");
                return;
            }
        }
        Commit newCommit = new Commit(message, tempCommitDate,
                directory, currStage, currCommit.getMyID());

        newCommit.addCommit();

        curBranch.updateHead(newCommit);
        directory.getMyCommits().add(newCommit.getMyID());
        curBranch.updateStage(newStage);
        newStage.setMyCommit(newCommit);
    }

    /** Checkout method, where we retrieve only the file
     * with the name specified from the current headCommit
     * folder.
     * @param headCommit the commit we want to perform the
     *                   checkout upon
     * @param fileName the name of the file we want.
     */
    private static void checkoutName(String fileName, Commit headCommit) {
        fileName = headCommit.processString(fileName);

        if (headCommit.getOldFileToRepoLoc() == null
                || !headCommit.getOldFileToRepoLoc().containsKey(fileName)
                || (headCommit.getMyParentCommit()
                .getMyUntrackedFiles().contains(fileName))
                && !headCommit.getOldFileToRepoLoc().containsKey(fileName)) {
            System.out.println("File does not exist in that commit.");
            return;
        }
        File oldVersion = new File(headCommit
                .getOldFileToRepoLoc().get(fileName));

        fileName = headCommit.processStringRev(fileName);
        Utils.writeContents(new File(fileName),
                Utils.readContentsAsString(oldVersion));
    }


    /** Checkout method, where we retrieve only the file
     * with the name specified from the specified commit.
     * folder.
     * @param commitID the ID of the commit we want to
     *                 checkout our file from.
     * @param fileName the name of the file we want.
     */
    private static void checkoutID(String commitID, String fileName) {
        if (commitID.length() == 6) {
            for (String ids: directory.getMyCommits()) {
                if (ids.substring(6, 12).equals(commitID)) {
                    commitID = ids.substring(6);
                }
            }
        }
        if (!commitID.contains("commit")) {
            commitID = "commit" + commitID;
        }
        File desiredCommitFile = new File(objectRepo + commitID);

        if (!desiredCommitFile.exists()) {
            System.out.println("No commit with that id exists.");
            return;
        }

        Commit desiredCommit =
                Utils.readObject(new File(objectRepo + commitID), Commit.class);

        fileName = desiredCommit.processString(fileName);
        if (!desiredCommit.getMyFilePointers().containsKey(fileName)
                || desiredCommit.getMyParentCommit()
                .getMyUntrackedFiles().contains(fileName)) {
            System.out.println("File does not exist in that commit.");
            return;
        }
        File oldVersion = new File(desiredCommit
                .getOldFileToRepoLoc().get(fileName));

        fileName = desiredCommit.processStringRev(fileName);
        Utils.writeContents(new File(fileName),
                Utils.readContentsAsString(oldVersion));
    }

    /** Checkout method, where we retrieve everything
     * from the headcommit of the specified branch
     * and sets the branch to the current branch.
     * @param branchName the name of the file we want.
     */
    private static void checkoutBranchName(String branchName) {
        if (!directory.getMyChildren().keySet().contains(branchName)) {
            System.out.println("No such branch exists.");
            return;
        } else if (directory.getCurBranch().getMyName().equals(branchName)) {
            System.out.println("No need to checkout the current branch.");
            return;
        }
        Branch checkoutBranch = directory.getMyChildren().get(branchName);
        Branch currBranch = directory.getCurBranch();

        for (String fileName: checkoutBranch
                .getHeadCommit().getOldFileToRepoLoc().keySet()) {
            File checkoutFile = new File(checkoutBranch.getHeadCommit()
                    .getOldFileToRepoLoc().get(fileName));
            File oldFile;
            try {
                oldFile = new File(currBranch.getHeadCommit()
                        .getOldFileToRepoLoc().get(fileName));
            } catch (NullPointerException ignored) {
                oldFile = new File(checkoutBranch.getHeadCommit()
                        .getOldFileToRepoLoc().get(fileName));
            }
            File tempFile = new File(currBranch.getHeadCommit()
                    .processStringRev(fileName));
            if (tempFile.exists() && (!Utils.readContentsAsString(checkoutFile)
                    .equals(Utils.readContentsAsString(tempFile))
                    && !Utils.readContentsAsString(tempFile)
                    .equals(Utils.readContentsAsString(oldFile)))) {
                System.out.println("There is an untracked file"
                        + " in the way; delete it or add it first.");
                return;
            }
        }

        ArrayList<File> filesToDelete = new ArrayList<>();
        for (String fileName: currBranch.getHeadCommit()
                .getOldFileToRepoLoc().keySet()) {
            if (!checkoutBranch.getHeadCommit()
                    .getOldFileToRepoLoc().containsKey(fileName)) {
                File targetFile = new File(fileName);
                filesToDelete.add(targetFile);
            }
        }

        for (File files: filesToDelete) {
            files.delete();
        }

        for (String fileName: checkoutBranch
                .getHeadCommit().getOldFileToRepoLoc().keySet()) {
            fileName = checkoutBranch.getHeadCommit()
                    .processStringRev(fileName);
            checkoutName(fileName, checkoutBranch.getHeadCommit());
        }

        directory.setCurBranch(checkoutBranch);
    }

    /** Prints out a log of all the commits starting with the
     * current branch head, trailing backwards to the init commit.
     */
    private static void log() {
        Commit currCommit = directory.getCurBranch().getHeadCommit();
        while (currCommit != null) {
            System.out.println(currCommit.toString());
            currCommit = currCommit.getMyParentCommit();
        }
    }

    /** Unstage the file and mark it to be not included in next commit.
     * Also remove the file from the working directory if it is still
     * there UNLESS it is untracked in the current commit.
     * @param fileName the name of the file we want to remove.
     */
    private static void rm(String fileName) {
        Branch currBranch = directory.getCurBranch();
        Commit currCommit = currBranch.getHeadCommit();
        String processedFileName = currCommit.processString(fileName);
        Stage currStage = currBranch.getMyStage();
        String myStageRepo = stageRepo + currStage.getMyID() + separator;

        if (!currCommit.getOldFileToRepoLoc().containsKey(processedFileName)
                && !Utils.plainFilenamesIn(myStageRepo)
                .contains(processedFileName)) {
            System.out.println("No reason to remove the file.");
            return;
        }

        if (Utils.plainFilenamesIn(myStageRepo).contains(processedFileName)) {
            File theFile = new File(myStageRepo + processedFileName);
            theFile.delete();
        }
        if (currStage.getStagedFileNames().contains(processedFileName)) {
            currStage.getStagedFileNames().remove(processedFileName);
        }
        if (!currStage.getRemovedFiles().contains(fileName)
                && currCommit.getOldFileToRepoLoc()
                .containsKey(processedFileName)) {
            currStage.getRemovedFiles().add(fileName);
        }
        currStage.saveStage();

        if (currCommit.getOldFileToRepoLoc().keySet().contains(fileName)
                && !currCommit.getMyUntrackedFiles().contains(fileName)) {
            currCommit.getMyUntrackedFiles().add(fileName);
        }

        if (currCommit.getOldFileToRepoLoc().containsKey(processedFileName)) {
            File thisFile = new File(fileName);
            if (thisFile.exists()) {
                try {
                    Utils.restrictedDelete(thisFile);
                } catch (IllegalArgumentException e) {
                    thisFile.delete();
                }
            }
        }
        Utils.writeObject(new File(objectRepo
                + currCommit.getMyID()), currCommit);
    }

    /** Basically log, except it displays the information for
     * every commit ever made, ordering does not matter.
     */
    private static void globalLog() {
        for (String commitNames: Utils.plainFilenamesIn(objectRepo)) {
            File thisFile = new File(objectRepo + commitNames);
            System.out.println(Utils.readObject(thisFile, Commit.class));
        }
    }

    /** Finds all files with the given commit message and prints
     * their commit IDs out one per line, one at a time.
     * @param message the commit message.
     */
    private static void find(String message) {
        boolean foundAtLeastOne = false;
        for (String commitNames: Utils.plainFilenamesIn(objectRepo)) {
            File thisFile = new File(objectRepo + commitNames);
            Commit thisCommit = Utils.readObject(thisFile, Commit.class);
            if (thisCommit.getMyMessage().equals(message)) {
                System.out.println(thisCommit.getMyID().substring(6));
                foundAtLeastOne = true;
            }
        }
        if (!foundAtLeastOne) {
            System.out.println("Found no commit with that message.");
            return;
        }
    }


    /** Displays what branches exist (and marks the current one), what
     * files have been staged, and what files have been marked for untracking.
     * Note that the files should be in SORTED ORDER.
     */
    private static void status() {
        ArrayList<String> filesToPrint = new ArrayList<>();
        String fileName = "";

        System.out.println("=== Branches ===");
        for (Branch branches: directory.getMyChildren().values()) {
            if (branches == directory.getCurBranch()) {
                fileName = "*" + fileName;
            }
            fileName = fileName + branches.getMyName();
            filesToPrint.add(fileName);
            fileName = "";
        }
        Collections.sort(filesToPrint);
        for (String files: filesToPrint) {
            System.out.println(files);
        }
        System.out.println();

        filesToPrint.clear();

        System.out.println("=== Staged Files ===");
        Stage currStage = directory.getCurBranch().getMyStage();
        Commit currCommit = directory.getCurBranch().getHeadCommit();
        for (String stagedFiles: currStage.getStagedFileNames()) {
            filesToPrint.add(currCommit.processStringRev(stagedFiles));
        }
        Collections.sort(filesToPrint);
        for (String files: filesToPrint) {
            System.out.println(files);
        }
        System.out.println();

        filesToPrint.clear();

        System.out.println("=== Removed Files ===");
        filesToPrint.addAll(directory.getCurBranch()
                .getMyStage().getRemovedFiles());
        Collections.sort(filesToPrint);
        for (String files: filesToPrint) {
            System.out.println(files);
        }
        System.out.println();
        filesToPrint.clear();

        System.out.println("=== Modifications Not Staged For Commit ===");
        statusPt2(currStage, currCommit, fileName, filesToPrint);
    }

    /** Part 2 of the "status" command.
     * @param currStage The current stage
     * @param currCommit The current commit
     * @param fileName The file name
     * @param filesToPrint The files to print
     */
    private static void statusPt2(Stage currStage,
                                  Commit currCommit, String fileName,
                                  ArrayList<String> filesToPrint) {
        System.out.println();
        System.out.println("=== Untracked Files ===");
        System.out.println();
    }

    /** Creates a new branch with it's current commit pointer
     * pointed at the current head commit of the current branch.
     * @param name the name of the new branch.
     */
    private static void branch(String name) {

        if (directory.getMyChildren().containsKey(name)) {
            System.out.println("A branch with that name already exists.");
            return;
        }
        Branch newBranch = new Branch(name,
                directory.getCurBranch().getHeadCommit(), directory);

        Stage newBranchStage = new Stage(directory, new Date());
        newBranchStage.setMyCommit(directory.getCurBranch().getHeadCommit());

        directory.getMyChildren().put(newBranch.getMyName(), newBranch);
        newBranch.updateStage(newBranchStage);
    }

    /** Removes the current branch from directory.myChildren. That's
     * about it really lol.
     * @param branchName literally what it says it is.
     */
    private static void removeBranch(String branchName) {
        if (!directory.getMyChildren().containsKey(branchName)) {
            System.out.println("A branch with that name does not exist.");
            return;
        }
        if (directory.getCurBranch().getMyName().equals(branchName)) {
            System.out.println("Cannot remove the current branch.");
            return;
        }
        directory.getMyChildren().remove(branchName);
    }

    /** Resets the working directory to the given commitID.
     * @param commitID the commitID of the commit we're trying to reset
     *                 to.
     */
    private static void reset(String commitID) {
        if (commitID.length() == 6) {
            for (String ids: directory.getMyCommits()) {
                if (ids.substring(6, 12).equals(commitID)) {
                    commitID = ids.substring(6);
                }
            }
        }
        if (commitID.contains("commit")) {
            commitID = commitID.substring(6);
        }
        if (!directory.getMyCommits().contains("commit" + commitID)) {
            System.out.println("No commit with that id exists.");
            return;
        }
        Branch currBranch = directory.getCurBranch();
        Commit currCommit = currBranch.getHeadCommit();
        Stage currStage = currBranch.getMyStage();
        Commit revertToCommit = Utils.readObject(
                new File(objectRepo + "commit" + commitID), Commit.class);
        ArrayList<String> checkoutFiles = new ArrayList<>();
        ArrayList<File> toRemoveFiles = new ArrayList<>();
        for (String fileName: revertToCommit.getOldFileToRepoLoc().keySet()) {
            File theFile = new File(currCommit.processStringRev(fileName));
            File oldFile = new File(revertToCommit
                    .getOldFileToRepoLoc().get(fileName));
            if (theFile.exists()
                    && !Utils.readContentsAsString(theFile).equals(
                    Utils.readContentsAsString(oldFile))
                    && !currCommit.getOldFileToRepoLoc()
                    .containsKey(fileName)) {
                System.out.println("There is an untracked file "
                        + "in the way; delete it or add it first.");
                return;
            }
            checkoutFiles.add(fileName);
            Stage tempStage = new Stage(directory, currStage.getMyDate());
            directory.getCurBranch().updateStage(tempStage);
            tempStage.setMyCommit(revertToCommit);
        }

        for (String fileName: currCommit.getOldFileToRepoLoc().keySet()) {
            if (!revertToCommit.getOldFileToRepoLoc().containsKey(fileName)) {
                toRemoveFiles.add(new File(
                        currCommit.processStringRev(fileName)));
            }
        }

        for (File files: toRemoveFiles) {
            Utils.restrictedDelete(files);
        }
        for (String fileName: checkoutFiles) {
            Utils.writeContents(new File(currCommit.processStringRev(fileName)),
                    Utils.readContentsAsString(new File(
                            revertToCommit
                                    .getOldFileToRepoLoc().get(fileName))));
        }
        currBranch.updateHead(revertToCommit);
    }


    /** Finds the appropriate split point by compiling all the commits
     * within the given branch. And then traversing the currentBranch
     * one commit at a time until we reach a commit that is contained
     * within the given Branch.
     * @param givenCommit the commit that is indicated.
     * @param curCommit the current commit.
     * @return commit which is the closest split point to the
     * current branch
     */
    private static Commit findSplitPoint(Commit curCommit, Commit givenCommit) {
        HashSet<String> allGBCommits = new HashSet<>();
        Commit trackerCommit = givenCommit;

        while (trackerCommit != null) {
            allGBCommits.add(trackerCommit.getMyID());
            trackerCommit = trackerCommit.getMyParentCommit();
        }

        while (!allGBCommits.contains(curCommit.getMyID())) {
            curCommit.getMyID();
            for (String theCommit: curCommit.getAllMyParents()) {
                if (allGBCommits.contains(theCommit)) {
                    return Utils.readObject(
                            new File(objectRepo + theCommit), Commit.class);
                }
            }
            curCommit = curCommit.getMyParentCommit();
        }
        return curCommit;
    }

    /** Merges files from the given branch into the current branch.
     * @param branchName the name of the branch we want
     */
    private static void merge(String branchName) {
        if (!directory.getMyChildren().containsKey(branchName)) {
            System.out.println("A branch with that name does not exist.");
            return;
        }
        Branch currBranch = directory.getCurBranch();
        Branch gottenBranch = directory.getMyChildren().get(branchName);
        Commit currCommit = currBranch.getHeadCommit();
        Commit getCommit = gottenBranch.getHeadCommit();
        if (currBranch.getMyName().equals(branchName)) {
            System.out.println("Cannot merge a branch with itself.");
            return;
        }
        Commit splitPoint = findSplitPoint(currCommit, getCommit);
        if (splitPoint.getMyID().equals(getCommit.getMyID())) {
            System.out.println("Given branch is an "
                    + "ancestor of the current branch.");
            return;
        } else if (splitPoint.getMyID()
                .equals(currBranch.getHeadCommitID())) {
            System.out.println("Current branch fast-forwarded.");
            checkoutBranchName(gottenBranch.getMyName());
            return;
        }
        if (!Utils.plainFilenamesIn(currBranch
                .getMyStage().getMyFolder()).isEmpty()) {
            System.out.println("You have uncommitted changes.");
            return;
        }
        ArrayList<String> filesToDelete = new ArrayList<>();
        for (String fileName: splitPoint.getOldFileToRepoLoc().keySet()) {
            File spFile = new File(splitPoint
                    .getOldFileToRepoLoc().get(fileName));
            try {
                File ccFile = new File(currCommit
                        .getOldFileToRepoLoc().get(fileName));
                File gcFile = new File(getCommit
                        .getOldFileToRepoLoc().get(fileName));
                if (Utils.readContentsAsString(spFile)
                        .equals(Utils.readContentsAsString(ccFile))
                        && !gcFile.exists()) {
                    if (!currCommit.getOldFileToRepoLoc()
                            .containsKey(fileName)) {
                        System.out.println("There is an untracked file"
                                + " in the way; delete it or add it first.");
                        return;
                    }
                    filesToDelete.add(currCommit.processStringRev(fileName));
                }
            } catch (NullPointerException e) {
                filesToDelete = filesToDelete;
            }
        }
        mergePt1(currBranch, gottenBranch, getCommit,
                currCommit, splitPoint, filesToDelete);
    }

    /** Part of the "merge" command. This particular line tests for a very
     * specific edge case where a file that exists in a different branch
     * is written with a different content in the current branch, and then
     * the branches are merged.
     * @param filesToDelete the arraylist of files we should delete.
     * @param currBranch the current branch we're on.
     * @param getCommit the commit we're getting.
     * @param currCommit the current commit we're at.
     * @param gottenBranch the branch we're trying to merge into.
     * @param splitPoint the point where we split.
     */
    private static void mergePt1(Branch currBranch, Branch gottenBranch,
                                 Commit getCommit, Commit currCommit,
                                 Commit splitPoint,
                                 ArrayList<String> filesToDelete) {
        ArrayList<String> workingDir = workingDirFiles();
        for (String fileName: getCommit.getOldFileToRepoLoc().keySet()) {
            String revProcessedName = currCommit.processStringRev(fileName);
            if (workingDir.contains(revProcessedName)
                    && !currCommit.getOldFileToRepoLoc()
                    .containsKey(fileName)) {
                File oldFile = new File(getCommit
                        .getOldFileToRepoLoc().get(fileName));
                File thisFile = new File(revProcessedName);
                if (!Utils.readContentsAsString(oldFile).equals(
                        Utils.readContentsAsString(thisFile))) {
                    System.out.println("There is an untracked file in "
                            + "the way; delete it or add it first.");
                    return;
                }
            }
        }
        for (String files: filesToDelete) {
            Utils.restrictedDelete(files);
        }

        mergePt21(currBranch, gottenBranch, getCommit, currCommit, splitPoint);
    }

    /** Performs the merge condition checks for files that
     * are not tracked by the given commit.
     * @param currBranch some
     * @param gottenBranch body
     * @param getCommit once
     * @param currCommit told
     * @param splitPoint me
     */
    private static void mergePt21(Branch currBranch, Branch gottenBranch,
                                  Commit getCommit, Commit currCommit,
                                  Commit splitPoint) {
        boolean occMConflict = false;
        for (String fileName: splitPoint.getOldFileToRepoLoc().keySet()) {
            if (currCommit.getOldFileToRepoLoc().containsKey(fileName)
                    && !getCommit.getOldFileToRepoLoc().containsKey(fileName)) {
                File oldFile = new File(splitPoint
                        .getOldFileToRepoLoc().get(fileName));
                File currFile = new File(currCommit
                        .getOldFileToRepoLoc().get(fileName));
                if (Utils.readContentsAsString(oldFile).equals(
                        Utils.readContentsAsString(currFile))) {
                    Utils.restrictedDelete(currCommit
                            .processStringRev(fileName));
                } else {
                    String concatFiles = "<<<<<<< HEAD\n";
                    concatFiles = concatFiles
                            + Utils.readContentsAsString(currFile);
                    concatFiles = concatFiles + "=======\n";
                    concatFiles = concatFiles + ">>>>>>>";
                    Utils.writeContents(new File(fileName), concatFiles);
                    occMConflict = true;
                }
            }
        }
        mergePt2(currBranch, gottenBranch, getCommit,
                currCommit, splitPoint, occMConflict);
    }

    /** Basically part 2 of the "merge" command to make the style
     * checker happy :P.
     * @param currBranch it
     * @param gottenBranch be
     * @param getCommit like
     * @param currCommit that
     * @param splitPoint sometimes
     * @param occMConflict PATRICIA
     */
    private static void mergePt2(Branch currBranch, Branch gottenBranch,
                                 Commit getCommit, Commit currCommit,
                                 Commit splitPoint, boolean occMConflict) {
        for (String fileName: getCommit.getOldFileToRepoLoc().keySet()) {
            File gcFile = new File(getCommit
                    .getOldFileToRepoLoc().get(fileName));
            File ccFile, spFile;
            try {
                ccFile = new File(currCommit
                        .getOldFileToRepoLoc().get(fileName));
            } catch (NullPointerException ignored) {
                ccFile = null;
            }
            try {
                spFile = new File(splitPoint
                        .getOldFileToRepoLoc().get(fileName));
            } catch (NullPointerException ignored) {
                spFile = null;
            }
            fileName = currCommit.processStringRev(fileName);
            if (spFile == null && ccFile == null) {
                checkoutID(getCommit.getMyID(), fileName);
                add(fileName);
            } else if ((spFile != null && ccFile != null)
                    && (!Utils.readContentsAsString(gcFile)
                    .equals(Utils.readContentsAsString(spFile))
                    && Utils.readContentsAsString(ccFile)
                    .equals(Utils.readContentsAsString(spFile)))) {
                checkoutID(getCommit.getMyID(), fileName);
                add(fileName);
            } else {
                String gcContent = Utils.readContentsAsString(gcFile);
                String ccContent, spContent;
                try {
                    ccContent = Utils.readContentsAsString(ccFile);
                } catch (NullPointerException ignored) {
                    ccContent = null;
                }
                try {
                    spContent = Utils.readContentsAsString(spFile);
                } catch (NullPointerException ignored) {
                    spContent = null;
                }
                if (!gcContent.equals(ccContent)
                        && !gcContent.equals(spContent)) {
                    String concatFiles = "<<<<<<< HEAD\n";
                    concatFiles = concatFiles
                            + Utils.readContentsAsString(ccFile);
                    concatFiles = concatFiles + "=======\n";
                    concatFiles = concatFiles
                            + Utils.readContentsAsString(gcFile);
                    concatFiles = concatFiles + ">>>>>>>";
                    Utils.writeContents(new File(fileName), concatFiles);
                    occMConflict = true;
                }
            }
        }
        mergePt3(gottenBranch, currBranch, occMConflict);
    }

    /** Basically, part 3 of the "merge" command to make the
     * style checker happy...
     * @param gottenBranch I
     * @param currBranch Like
     * @param mergeConflict Pies
     */
    private static void mergePt3(Branch gottenBranch,
                                 Branch currBranch, boolean mergeConflict) {
        commit("Merged " + gottenBranch.getMyName()
                + " into " + currBranch.getMyName() + ".");
        directory.getCurBranch().getHeadCommit().setMeToMergeCommit();
        Commit thisCommit = directory.getCurBranch().getHeadCommit();
        thisCommit.getAllMyParents().add(gottenBranch.getHeadCommitID());
        Utils.writeObject(new File(objectRepo
                + thisCommit.getMyID()), thisCommit);
        if (mergeConflict) {
            System.out.println("Encountered a merge conflict.");
        }
    }


    /** Usage: java gitlet.Main ARGS, where ARGS contains
     *  <COMMAND> <OPERAND> .... */
    public static void main(String... args) {
        if (args.length == 0) {
            System.out.println("Please enter a command.");
            return;
        }
        String command = args[0];
        directory = loadDirectory();
        List<String> operandList;
        try {
            operandList = Arrays.asList(
                    Arrays.copyOfRange(args, 1, args.length));
        } catch (IndexOutOfBoundsException ignored) {
            operandList = new ArrayList<>();
        }
        try {
            if (command.equals("init")) {
                init();
            } else if (command.equals("add")) {
                add(operandList.get(0));
            } else if (command.equals("commit")) {
                commit(operandList.get(0));
            } else if (command.equals("checkout")) {
                if (!operandList.contains("--") && operandList.size() == 1) {
                    checkoutBranchName(operandList.get(0));
                } else if (operandList.get(0).equals("--")) {
                    checkoutName(operandList.get(1),
                            directory.getCurBranch().getHeadCommit());
                } else if (operandList.get(1).equals("--")) {
                    checkoutID(operandList.get(0), operandList.get(2));
                } else {
                    throw new IndexOutOfBoundsException();
                }
            } else if (command.equals("log")) {
                log();
            } else if (command.equals("rm")) {
                rm(operandList.get(0));
            } else if (command.equals("global-log")) {
                globalLog();
            } else if (command.equals("find")) {
                find(operandList.get(0));
            } else if (command.equals("status")) {
                status();
            } else if (command.equals("branch")) {
                branch(operandList.get(0));
            } else if (command.equals("rm-branch")) {
                removeBranch(operandList.get(0));
            } else if (command.equals("reset")) {
                reset(operandList.get(0));
            } else if (command.equals("merge")) {
                merge(operandList.get(0));
            } else {
                System.out.println("No command with that name exists.");
            }
        } catch (NullPointerException e) {
            System.out.println("Not in an initialized Gitlet directory.");
        } catch (IndexOutOfBoundsException e) {
            System.out.println("Incorrect operands.");
        }
        saveDirectory();
    }

    /** Just a method allowing me to play around with the main method :D.
     */
    public static void trialMethod() {
        System.out.println("The council calls for your trial");
    }

    /** Just another method that allows me to play around, except this
     * one allows for arguments.
     * @param name the operand, in this case it's a name because the
     *             message is retarded lmao.
     */
    public static void trialMethod(String name) {
        System.out.println("The council calls for your execution " + name);
    }

    /** The separator character for my OS. It should be a "/".
     */
    private static String separator = File.separator;

    /** The directory to the object repository.
     */
    private static String objectRepo = ".gitlet"
            + separator + "objectRepository" + separator;

    /** The directory to the stage repository.
     */
    private static String stageRepo =
            ".gitlet" + separator + "stages" + separator;

    /** The overarching TreeP which represents me entire repository.
     */
    private static TreeP directory = null;
    /** Get method for the TreeP. Mostly for use in my UnitTests.
     * @return the directory.
     */
    public static TreeP getDirectory() {
        File directoryFile = new File(".gitlet" + separator + "repo");
        return Utils.readObject(directoryFile, TreeP.class);
    }
}
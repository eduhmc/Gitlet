package gitlet.Managers;

import gitlet.GitletException;
import gitlet.Managers.Models.Commit;
import gitlet.Managers.Models.FileData;
import gitlet.Managers.Models.FileSnapshot;
import gitlet.Managers.Models.FileSnapshotContainer;
//import gitlet.Utils;

import java.io.*;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

public class GitManager {
    private String fileSystemPath;
    private FileManager fileManager;

    public GitManager(String path){
        this.fileSystemPath = path;//FileSystems.getDefault().getPath(path);
        this.fileManager = new FileManager(fileSystemPath);
    }

    public void initialize() throws IOException {
        if(this.directoryForGitletAlreadyExist()) throw new GitletException("A Gitlet version-control system already exists in the current directory.");
        new File(this.fileSystemPath+"\\.gitlet").mkdir();
        new File(this.fileSystemPath+"\\.gitlet"+"\\staging").mkdir();
        new File(this.fileSystemPath+"\\.gitlet"+"\\files").mkdir();
        Commit commit = new Commit();
        commit.CommitMessage = "";
        commit.Files = new ArrayList<>();
        commit.PreviousCommit = null;
        commit.HashSignature = "MYTHOLOGICAL-ADAM";
        commit.Timestamp = new Timestamp(0);
        writeCommitToFile(commit);
    }

    public void commit(String commitMessage) {
        Commit currentCommit = this.GetCurrentCommit();
        Commit newCommit = currentCommit.Copy();
        newCommit.PreviousCommit = currentCommit;
        newCommit.Timestamp = new Timestamp(0);
        newCommit.CommitMessage = commitMessage;
        //newCommit.HashSignature = Utils.sha1(newCommit);
        addAllChanges(newCommit);
        this.writeCommitToFile(newCommit);
    }

    private void addAllChanges(Commit newCommit) {

    }


    private Boolean directoryForGitletAlreadyExist() {
        return Files.exists(Paths.get(this.fileSystemPath+"\\.gitlet"));
    }

    private Boolean fileExistOnGitletStagingArea(String filename) {
        return Files.exists(Paths.get(this.fileSystemPath+"\\.gitlet"+"\\staging"+"\\"+filename));
    }

    private void writeCommitToFile(Commit commit) {
        try {
            FileOutputStream fileOut  = new FileOutputStream(new File(fileSystemPath+"\\.gitlet"+"\\data.bin"));
            ObjectOutputStream objectOut = new ObjectOutputStream(fileOut);
            objectOut.writeObject(commit);
            objectOut.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private Commit GetCurrentCommit() {
        try {
            InputStream inputStream = new FileInputStream(fileSystemPath+"\\.gitlet"+"\\data.bin");
            ObjectInputStream ostream = new ObjectInputStream(inputStream);
            Commit commit = (Commit)ostream.readObject();
            inputStream.close();
            return commit;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private void writeFile(FileSnapshotContainer fileSnapshotContainer) {
        try {
            FileOutputStream fileOut  = new FileOutputStream(new File(fileSystemPath+"\\.gitlet"+"\\staging\\"+fileSnapshotContainer.FileName));
            ObjectOutputStream objectOut = new ObjectOutputStream(fileOut);
            objectOut.writeObject(fileSnapshotContainer.Snapshot);
            objectOut.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void addFile(String fileName) throws IOException {
        FileSnapshotContainer container = this.fileManager.GetSnapshotContainer(fileName);


        if(this.fileExistOnGitletStagingArea(container.FileName)){

        }
        else {
            this.writeFile(container);
        }

    }

}


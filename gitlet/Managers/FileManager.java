package gitlet.Managers;
import gitlet.Managers.Models.Commit;
import gitlet.Managers.Models.FileSnapshotContainer;
import gitlet.Utils;
import gitlet.Managers.Models.FileSnapshot;
//import javax.rmi.CORBA.Util;
import java.awt.*;
import java.io.*;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Stream;

public class FileManager {
    private Path fileSystemPath;
    private Path stagingPath;

    public FileManager(String path){

        this.fileSystemPath = FileSystems.getDefault().getPath(path);
        this.stagingPath = FileSystems.getDefault().getPath(path+"\\.gitlet\\staging");
    }

    private Stream<FileSnapshot> getDirectories() throws IOException {
        return Files.walk(this.stagingPath)
                .filter(Files::isRegularFile)
                .map(filePaths->{
                    FileSnapshot fileSnapshot;
                    try {
                        FileInputStream inputStream = new FileInputStream (filePaths.toFile());
                        ObjectInputStream ostream = new ObjectInputStream(inputStream);
                        fileSnapshot = (FileSnapshot)ostream.readObject();
                        inputStream.close();
                    } catch (Exception e) {
                        e.printStackTrace();
                        fileSnapshot = null;
                    }
                    return fileSnapshot;
                });
    }

    public Stream<FileSnapshot> getFilesSnapshot() throws IOException {
        return Files.walk(this.fileSystemPath)
                .filter(Files::isRegularFile)
                .map(filePath->{
                    File file = filePath.toFile();
                    byte[] content = Utils.readContents(file);
                    FileSnapshot fileData = new FileSnapshot();
                    fileData.Path = this.fileSystemPath.relativize(filePath).toString();
                    fileData.Signature = Utils.sha1(content);
                    fileData.Content = content;
                    fileData.Name = filePath.getFileName().toString();
                    return fileData;
                });
    }

    public FileSnapshotContainer GetSnapshotContainer(String fileName) throws IOException {
        FileSnapshot fileSnapshot = new FileSnapshot();
        File file = new File(this.fileSystemPath.toString()+"//fileName");
        byte[] content = Utils.readContents(file);
        fileSnapshot.Path = this.fileSystemPath.relativize(file.toPath()).toString();
        fileSnapshot.Signature = Utils.sha1(content);
        fileSnapshot.Content = content;
        fileSnapshot.Name = file.getName();
        FileSnapshotContainer fileSnapshotContainer = new FileSnapshotContainer();
        fileSnapshotContainer.Snapshot = fileSnapshot;
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ObjectOutput out = new ObjectOutputStream(bos);
        out.writeObject(fileSnapshot.Path);
        out.flush();
        fileSnapshotContainer.FileName = Utils.sha1(bos.toByteArray());
        return fileSnapshotContainer;
    }


}

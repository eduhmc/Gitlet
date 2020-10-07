package gitlet.Managers.Models;

import java.io.Serializable;

public class FileData implements Serializable {
    public String Name;
    public String Path;
    public FileVersion Version;

    public FileData copy() {
        FileData fileData=new FileData();
        fileData.Name = this.Name;
        fileData.Path = this.Path;
        FileVersion  version = new FileVersion();
        fileData.Version = version;
        fileData.Version.HashKey = this.Version.HashKey;
        fileData.Version.PreviousVersion = this.Version.PreviousVersion;
        return  fileData;
    }
}

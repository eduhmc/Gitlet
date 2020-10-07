package gitlet.Managers.Models;

import java.io.Serializable;

public class FileVersion implements Serializable {
    public String HashKey;
    public FileVersion PreviousVersion;
}

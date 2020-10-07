package gitlet.Managers.Models;

import java.io.Serializable;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.stream.Collectors;

public class Commit implements Serializable {
    public java.sql.Timestamp Timestamp;
    public String CommitMessage;
    public String HashSignature;
    public ArrayList<FileData> Files;
    public Commit PreviousCommit;

    public Commit Copy(){
        Commit commit = new Commit();
        commit.Files = new ArrayList<FileData>(this.Files.stream().map(x->x.copy()).collect(Collectors.toList()));
        return  commit;
    }

}
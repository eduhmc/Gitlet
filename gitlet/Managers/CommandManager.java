package gitlet.Managers;

import gitlet.GitletException;

import java.io.IOException;
import java.nio.file.Paths;

public class CommandManager {



    public void HandlerCommandLine(String... args) throws IOException {
        GitManager gitManager = new GitManager(Paths.get("").toAbsolutePath().toString());
        switch(args[0]) {
            case "init":
                gitManager.initialize();
                break;
            case "add":
                gitManager.addFile(args[1]);
                break;
            case "commit":
                gitManager.commit(args[1]);
                break;
            default: throw new GitletException("Unknown command");
        }

    }
}
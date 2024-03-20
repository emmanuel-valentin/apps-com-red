package com.ipn.mx.commands;

import com.ipn.mx.constants.Constants;
import com.ipn.mx.state.DirectoryState;
import picocli.CommandLine;

import java.nio.file.Files;
import java.nio.file.Paths;

@CommandLine.Command(name = "cd", description = "Cambia el directorio actual")
public class CdCommand implements Runnable {
    private String newPath;

    @CommandLine.Parameters(description = "Directorio al que se desea cambiar", defaultValue = "")
    private String path;

    @CommandLine.Option(names = {"-r", "--remote"}, description = "Cambia el directorio en el servidor")
    private boolean remote = false;

    public String getNewPath() {
        return newPath;
    }

    @Override
    public void run() {
        try {
            String pathToChange = DirectoryState.getInstance().getPath();
            if (remote) {
                pathToChange = Constants.SERVER_PATH + "/" + path;
            } else {
                pathToChange += "/" + path;
            }

            if (Files.exists(Paths.get(pathToChange))) {
                DirectoryState.getInstance().setPath(pathToChange);
                this.newPath = pathToChange;
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}

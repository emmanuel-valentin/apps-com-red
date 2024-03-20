package com.ipn.mx.commands;

import com.ipn.mx.constants.Constants;
import com.ipn.mx.state.DirectoryState;
import picocli.CommandLine;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@CommandLine.Command(name = "mkdir", description = "Crea un directorio")
public class MkdirCommand implements Runnable {
    private String operationMessage = "";

    @CommandLine.Parameters(description = "Nombre del directorio a crear")
    private String path;

    @CommandLine.Option(names = {"-r", "--remote"}, description = "Crea el directorio en el servidor")
    private boolean remote = false;

    public String getOperationMessage() {
        return this.operationMessage;
    }

    @Override
    public void run() {
        try {
            Path folderCreated;
            if (remote) {
                folderCreated = Files.createDirectories(Paths.get(Constants.SERVER_PATH + "/" + path));
            }
            else {
                folderCreated = Files.createDirectories(Paths.get(DirectoryState.getInstance().getPath() + "/" + path));
            }
            this.operationMessage = folderCreated.toAbsolutePath() + " creado exitosamente.";
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}

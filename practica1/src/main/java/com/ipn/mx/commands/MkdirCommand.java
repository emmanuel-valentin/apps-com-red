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
    private boolean remote;

    @CommandLine.Parameters(description = "Nombre del directorio a crear")
    private String path;

    public MkdirCommand(boolean remote) {
        this.remote = remote;
    }

    public MkdirCommand() {
        this.remote = false;
    }

    public String getOperationMessage() {
        return this.operationMessage;
    }

    @Override
    public void run() {
        try {
            Path folderCreated;
            DirectoryState directoryState = DirectoryState.getInstance(remote);
            folderCreated = Files.createDirectories(Paths.get(directoryState.getPath() + "/" + path));
            this.operationMessage = folderCreated.toAbsolutePath() + " creado exitosamente.";
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}

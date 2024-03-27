package com.ipn.mx.commands;

import com.ipn.mx.constants.Constants;
import com.ipn.mx.state.DirectoryState;
import org.apache.commons.io.FileUtils;
import picocli.CommandLine;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Paths;

@CommandLine.Command(name = "rmdir", description = "Elimina un directorio")
public class RmdirCommand implements Runnable {
    private String operationMessage = "";
    private boolean remote;

    @CommandLine.Parameters(description = "Nombre del directorio a eliminar")
    private String path;

    public RmdirCommand(boolean remote) {
        this.remote = remote;
    }

    public RmdirCommand() {
        this.remote = false;
    }

    public String getOperationMessage() {
        return this.operationMessage;
    }

    @Override
    public void run() {
        try {
            File dirToBeDeleted;
            DirectoryState directoryState = DirectoryState.getInstance(remote);
            dirToBeDeleted = new File(directoryState.getPath() + "/" + path);
            FileUtils.deleteDirectory(dirToBeDeleted);
            operationMessage = dirToBeDeleted.toPath().toAbsolutePath() + " eliminado exitosamente.";
        } catch (NoSuchFileException e) {
            operationMessage = "El directorio o archivo no existe";
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}

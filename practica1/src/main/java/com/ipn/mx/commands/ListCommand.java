package com.ipn.mx.commands;

import com.ipn.mx.constants.Constants;
import com.ipn.mx.state.DirectoryState;
import picocli.CommandLine;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Paths;
import java.rmi.Remote;

@CommandLine.Command(name = "ls", description = "Lista los archivos y directorios del directorio actual")
public class ListCommand implements Runnable {
    private String listed = "";

    @CommandLine.Parameters(description = "Directorio a listar", defaultValue = "")
    private String directory;

    private boolean remote;

    public ListCommand(boolean remote) {
        this.remote = remote;
    }

    public ListCommand() {
        this.remote = false;
    }

    public String getListed() {
        return listed;
    }

    @Override
    public void run() {
        try {
            String path = DirectoryState.getInstance(remote).getPath();
            if (!directory.isEmpty()) {
                path += "/" + directory;
            }
            Files.list(Paths.get(path)).forEach(p -> listed += p.getFileName() + " ");
            if (listed.isEmpty()) {
                listed = "No hay archivos o directorios en el directorio actual.";
            }
        } catch (NoSuchFileException e) {
            listed = "El directorio o archivo no existe.";
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

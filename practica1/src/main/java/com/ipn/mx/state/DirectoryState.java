package com.ipn.mx.state;

import com.ipn.mx.constants.Constants;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class DirectoryState {
    private static DirectoryState instance;
    private String path;

    private DirectoryState(String path) {
        this.path = path;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public void initializeDirectory() {
        try {
            if (!Files.exists(Path.of(this.path))) {
                Files.createDirectory(Path.of(this.path));
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static synchronized DirectoryState getInstance() {
        if (instance == null) {
            instance = new DirectoryState(Constants.CLIENT_PATH);
        }
        return instance;
    }
}

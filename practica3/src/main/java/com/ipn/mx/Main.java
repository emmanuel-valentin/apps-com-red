package com.ipn.mx;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Main {
    public static final int MAX = 5;
    public static final int PORT = 8080;

    public static void main(String[] args) {
        try {
            ServerSocket serverSocket = new ServerSocket(PORT);
            ExecutorService pool = Executors.newFixedThreadPool(MAX);
            System.out.println("Iniciando servidor: http://localhost:" + PORT);

            for (;;) {
                Socket socket = serverSocket.accept();
                Runnable conn = new PoolThread(socket);
                pool.execute(conn);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
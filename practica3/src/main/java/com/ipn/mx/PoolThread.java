package com.ipn.mx;

import java.net.Socket;

public class PoolThread implements Runnable {
    private Socket socket;

    public PoolThread(Socket socket) {
        this.socket = socket;
    }

    @Override
    public void run() {
        new Request(socket).start();
    }
}

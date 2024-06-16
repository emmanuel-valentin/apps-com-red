package com.ipn.mx;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.*;
import java.util.Iterator;

public class Main {
    public static final int PORT = 8080;

    public static void main(String[] args) {
        try {
            Selector selector = Selector.open();
            ServerSocketChannel serverChannel = ServerSocketChannel.open();
            serverChannel.configureBlocking(false);
            serverChannel.socket().bind(new InetSocketAddress(PORT));
            serverChannel.register(selector, SelectionKey.OP_ACCEPT);

            System.out.println("Iniciando servidor: http://localhost:" + PORT);

            while (true) {
                selector.select();
                Iterator<SelectionKey> keys = selector.selectedKeys().iterator();

                while (keys.hasNext()) {
                    SelectionKey key = keys.next();
                    keys.remove();

                    if (!key.isValid()) {
                        continue;
                    }

                    if (key.isAcceptable()) {
                        accept(key, selector);
                    } else if (key.isReadable()) {
                        handleClient(key);
                    }
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static void accept(SelectionKey key, Selector selector) throws IOException {
        ServerSocketChannel serverChannel = (ServerSocketChannel) key.channel();
        SocketChannel clientChannel = serverChannel.accept();
        clientChannel.configureBlocking(false);
        clientChannel.register(selector, SelectionKey.OP_READ);
    }

    private static void handleClient(SelectionKey key) {
        SocketChannel clientChannel = (SocketChannel) key.channel();
        Request request = new Request(clientChannel);
        request.handle();
        try {
            clientChannel.register(key.selector(), SelectionKey.OP_READ);
        } catch (ClosedChannelException e) {
            e.printStackTrace();
        }
    }
}

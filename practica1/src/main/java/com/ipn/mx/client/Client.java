package com.ipn.mx.client;

import com.ipn.mx.constants.Constants;
import com.ipn.mx.state.DirectoryState;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

public class Client {
    public static void main(String[] args) {
        try {
            DirectoryState.getInstance().initializeDirectory();

            BufferedReader clientReader = new BufferedReader(new InputStreamReader(System.in, StandardCharsets.UTF_8));
            InetAddress host = null;
            String dir = "";

            try {
                System.out.print("Escribe la dirección IP del servidor: ");
                dir = clientReader.readLine();
                host = InetAddress.getByName(dir);
            } catch (Exception e) {
                e.printStackTrace();
            }

            Socket socket = new Socket(host, Constants.PORT);
            socket.setTcpNoDelay(true);
            socket.setOOBInline(true);
            System.out.println("Conexión establecida con el servidor " + dir + ":" + Constants.PORT);

            PrintWriter printWriter = new PrintWriter(new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.ISO_8859_1));
            BufferedReader serverReader = new BufferedReader(new InputStreamReader(socket.getInputStream(), StandardCharsets.ISO_8859_1));

            while (true) {
                System.out.print("> ");
                String command = clientReader.readLine();
                printWriter.println(command);
                printWriter.flush();

                if (command.compareToIgnoreCase("exit") == 0) {
                    System.out.println("Cerrando la conexión con el servidor...");
                    printWriter.close();
                    serverReader.close();
                    clientReader.close();
                    socket.close();
                    break;
                } else {
                    System.out.println(serverReader.readLine());
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

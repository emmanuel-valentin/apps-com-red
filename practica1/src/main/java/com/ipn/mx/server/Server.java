package com.ipn.mx.server;

import com.ipn.mx.commands.CdCommand;
import com.ipn.mx.commands.ListCommand;
import com.ipn.mx.commands.MkdirCommand;
import com.ipn.mx.commands.RmdirCommand;
import com.ipn.mx.constants.Constants;
import com.ipn.mx.helpers.Helpers;
import com.ipn.mx.state.DirectoryState;
import picocli.CommandLine;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.StandardSocketOptions;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

public class Server {
    public static void main(String[] args) {
        try {
            DirectoryState.getInstance(true).initializeDirectory();
            ServerSocket socket = new ServerSocket(Constants.PORT);
            socket.setOption(StandardSocketOptions.SO_REUSEADDR, true);
            System.out.println("Servidor iniciado en el puerto " + Constants.PORT + ", esperando cliente...");

            for (; ; ) {
                Socket client = socket.accept();
                System.out.println("Cliente conectado desde " + client.getInetAddress() + ":" + client.getPort());

                PrintWriter printWriter = new PrintWriter(new OutputStreamWriter(client.getOutputStream(), StandardCharsets.ISO_8859_1));
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(client.getInputStream(), StandardCharsets.ISO_8859_1));

                while (true) {
                    String[] commandWithArgs = bufferedReader.readLine().split(" ");
                    String[] commandArgs = Arrays.copyOfRange(commandWithArgs, 1, commandWithArgs.length);
                    String command = commandWithArgs[0];

                    System.out.println("Comando recibido: " + Arrays.toString(commandWithArgs));

                    if (command.compareToIgnoreCase("exit") == 0) {
                        bufferedReader.close();
                        printWriter.close();
                        client.close();
                        break;
                    } else if (command.compareToIgnoreCase("ls") == 0) {
                        ListCommand listCommand = new ListCommand(true);
                        new CommandLine(listCommand).execute(commandArgs);
                        printWriter.println(listCommand.getListed());
                        printWriter.flush();
                    } else if (command.compareToIgnoreCase("mkdir") == 0) {
                        MkdirCommand mkdirCommand = new MkdirCommand(true);
                        new CommandLine(mkdirCommand).execute(commandArgs);
                        printWriter.println(mkdirCommand.getOperationMessage());
                        printWriter.flush();
                    } else if (command.compareToIgnoreCase("rmdir") == 0) {
                        RmdirCommand rmdirCommand = new RmdirCommand(true);
                        new CommandLine(rmdirCommand).execute(commandArgs);
                        printWriter.println(rmdirCommand.getOperationMessage());
                        printWriter.flush();
                    } else if (command.compareToIgnoreCase("cd") == 0) {
                        CdCommand cdCommand = new CdCommand(true);
                        new CommandLine(cdCommand).execute(commandArgs);
                        printWriter.println(cdCommand.getNewPath());
                        printWriter.flush();
                    } else if (command.compareToIgnoreCase("put") == 0) {
                        String path = commandArgs.length >= 1 ? Constants.SERVER_PATH + "/" + commandArgs[0] : Constants.SERVER_PATH;
                        printWriter.println("putok");
                        printWriter.flush();
                        Helpers.receiveFilesFromClient(path);
                    } else if (command.compareToIgnoreCase("get") == 0) {
                        printWriter.println("getok");
                        printWriter.flush();
                        Helpers.sendFilesFromServer(commandArgs);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            //main(args);
        }
    }
}

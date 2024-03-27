package com.ipn.mx.client;

import com.ipn.mx.commands.CdCommand;
import com.ipn.mx.commands.ListCommand;
import com.ipn.mx.commands.MkdirCommand;
import com.ipn.mx.commands.RmdirCommand;
import com.ipn.mx.constants.Constants;
import com.ipn.mx.helpers.Helpers;
import com.ipn.mx.state.DirectoryState;
import picocli.CommandLine;

import javax.swing.*;
import java.io.*;
import java.net.InetAddress;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

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
                //e.printStackTrace();
                main(args);
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
                String[] commandWithArgs = command.split(" ");
                String[] subcommandWithArgs = Arrays.copyOfRange(commandWithArgs, 1, commandWithArgs.length);
                String[] subcommandArgs = Arrays.copyOfRange(subcommandWithArgs, 1, subcommandWithArgs.length);

                if (command.compareToIgnoreCase("exit") == 0) {
                    System.out.println("Cerrando la conexión con el servidor...");
                    printWriter.close();
                    serverReader.close();
                    clientReader.close();
                    socket.close();
                    break;
                } else if (command.startsWith("local")) {
                    String subcommand = commandWithArgs[1];

                    if (subcommand.compareToIgnoreCase("ls") == 0) {
                        ListCommand listCommand = new ListCommand();
                        new CommandLine(listCommand).execute(subcommandArgs);
                        System.out.println(listCommand.getListed());
                    } else if (subcommand.compareToIgnoreCase("mkdir") == 0) {
                        MkdirCommand mkdirCommand = new MkdirCommand();
                        new CommandLine(mkdirCommand).execute(subcommandArgs);
                        System.out.println(mkdirCommand.getOperationMessage());
                    } else if (subcommand.compareToIgnoreCase("rmdir") == 0) {
                        RmdirCommand rmdirCommand = new RmdirCommand();
                        new CommandLine(rmdirCommand).execute(subcommandArgs);
                        System.out.println(rmdirCommand.getOperationMessage());
                    } else if (subcommand.compareToIgnoreCase("cd") == 0) {
                        CdCommand cdCommand = new CdCommand();
                        new CommandLine(cdCommand).execute(subcommandArgs);
                        System.out.println(cdCommand.getNewPath());
                    }
                } else if (command.startsWith("remote")) {
                    printWriter.println(String.join(" ", subcommandWithArgs));
                    printWriter.flush();
                    String response = serverReader.readLine();
                    if (response.equals("putok")) {
                        Helpers.sendFilesFromClient(host);
                    } else if (response.equals("getok")) {
                        Helpers.receiveFilesFromServer(host);
                    } else {
                        System.out.println(response);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            main(args);
        }
    }
}

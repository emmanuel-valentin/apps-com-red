package com.ipn.mx.helpers;

import com.ipn.mx.constants.Constants;

import javax.swing.*;
import java.io.*;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

public class Helpers {
    public static void sendFilesFromClient(InetAddress host) throws IOException {
        Socket fileSocket = new Socket(host, Constants.FILE_PORT);
        fileSocket.setTcpNoDelay(true);
        fileSocket.setOOBInline(true);

        JFileChooser jFileChooser = new JFileChooser();
        jFileChooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
        jFileChooser.setCurrentDirectory(new File(Constants.CLIENT_PATH));
        jFileChooser.setMultiSelectionEnabled(true);

        int r = jFileChooser.showOpenDialog(null);

        if (r == JFileChooser.APPROVE_OPTION) {
            File[] selectedFilesAndDirs = jFileChooser.getSelectedFiles();
            File zipFile = new File(Constants.CLIENT_PATH + "/files.zip");
            FileOutputStream fos = new FileOutputStream(zipFile);
            ZipOutputStream zos = new ZipOutputStream(fos);

            for (File file : selectedFilesAndDirs) {
                zip(file, zos, "");
            }
            zos.close();

            String name = zipFile.getName();
            long size = zipFile.length();
            System.out.println("Tamaño total del envío: " + size + " bytes");

            DataOutputStream dos = new DataOutputStream(fileSocket.getOutputStream());
            DataInputStream dis = new DataInputStream(new FileInputStream(zipFile));

            dos.writeUTF(name);
            dos.writeLong(size);

            long sent = 0;
            int l = 0, percentage = 0;

            while (sent < size) {
                byte[] b = new byte[1500];
                l = dis.read(b);
                System.out.println("Enviados: " + l + " bytes");
                dos.write(b, 0, l);
                dos.flush();
                sent += l;
                percentage = (int) ((sent * 100) / size);
                System.out.println("Enviado: " + percentage + "%");
            }

            System.out.println("Los archivos han sido enviados correctamente");
            dis.close();
            dos.close();
            fileSocket.close();
            zipFile.delete();
        }
    }

    public static void receiveFilesFromClient(String path) throws IOException {
        ServerSocket fileSocket = new ServerSocket(Constants.FILE_PORT);
        Socket fileClient = fileSocket.accept();
        DataInputStream dis = new DataInputStream(fileClient.getInputStream());
        String fileName = dis.readUTF();
        long fileSize = dis.readLong();
        DataOutputStream dos = new DataOutputStream(new FileOutputStream(path + "/" + fileName));
        long received = 0;
        int l = 0, percentage = 0;

        while (received < fileSize) {
            byte[] b = new byte[1500];
            l = dis.read(b);
            dos.write(b, 0, l);
            dos.flush();
            received += l;
            percentage = (int) ((received * 100) / fileSize);
            System.out.println("Recibido: " + percentage + "%");
        }

        System.out.println("Archivos recibidos...");
        dos.close();
        dis.close();
        fileClient.close();
        fileSocket.close();
        unzip(new File(path + "/" + fileName), new File(path));
    }

    public static void zip(File fileOrDir, ZipOutputStream zos, String parentDir) throws IOException {
        if (fileOrDir.isDirectory()) {
            // Comprimir directorio y su contenido de manera recursiva
            if (fileOrDir.listFiles().length != 0) {
                File[] files = fileOrDir.listFiles();
                for (File file : files) {
                    zip(file, zos, parentDir + fileOrDir.getName() + "/");
                }
            }
        } else {
            // Comprimir archivo
            FileInputStream fis = new FileInputStream(fileOrDir);
            ZipEntry zipEntry = new ZipEntry(parentDir + fileOrDir.getName());
            zos.putNextEntry(zipEntry);

            byte[] buffer = new byte[1024];
            int bytesRead;
            while ((bytesRead = fis.read(buffer)) != -1) {
                zos.write(buffer, 0, bytesRead);
            }
            fis.close();
            zos.closeEntry();
        }
    }

    private static void unzip(File zipFile, File destinationDir) throws IOException {
        byte[] buffer = new byte[1024];

        ZipInputStream zis = new ZipInputStream(new FileInputStream(zipFile));
        ZipEntry zipEntry = zis.getNextEntry();
        while (zipEntry != null) {
            String fileName = zipEntry.getName();
            File newFile = new File(destinationDir, fileName);

            // Crear directorios necesarios para el archivo
            new File(newFile.getParent()).mkdirs();

            FileOutputStream fos = new FileOutputStream(newFile);
            int len;
            while ((len = zis.read(buffer)) > 0) {
                fos.write(buffer, 0, len);
            }
            fos.close();
            zipEntry = zis.getNextEntry();
        }
        zipFile.delete();
        zis.closeEntry();
        zis.close();
    }

    public static void receiveFilesFromServer(InetAddress host) throws IOException {
        Socket fileSocket = new Socket(host, Constants.FILE_PORT);
        fileSocket.setTcpNoDelay(true);
        fileSocket.setOOBInline(true);

        FileOutputStream fos = new FileOutputStream("files.zip");
        DataInputStream dis = new DataInputStream(fileSocket.getInputStream());

        String fileName = dis.readUTF();
        long fileSize = dis.readLong();

        long received = 0;
        int l = 0, percentage = 0;

        while (received < fileSize) {
            byte[] b = new byte[1500];
            l = dis.read(b);
            fos.write(b, 0, l);
            fos.flush();
            received += l;
            percentage = (int) ((received * 100) / fileSize);
            System.out.println("Recibido: " + percentage + "%");
        }
        fos.close();
        dis.close();
        fileSocket.close();

        System.out.println("Archivos recibidos...");
        unzip(new File(fileName), new File(Constants.CLIENT_PATH));
    }

    public static void sendFilesFromServer(String[] commandArgs) throws IOException {
        ServerSocket fileSocket = new ServerSocket(Constants.FILE_PORT);
        File zipFile = new File(Constants.SERVER_PATH + "/files.zip");
        FileOutputStream fos = new FileOutputStream(zipFile);
        ZipOutputStream zos = new ZipOutputStream(fos);
        List<File> files = new ArrayList<File>();
        for (String file : commandArgs) {
            files.add(new File(Constants.SERVER_PATH + "/" + file));
        }

        for (File file : files) {
            zip(file, zos, "");
        }
        zos.close();

        String name = zipFile.getName();
        long fileSize = zipFile.length();
        System.out.println("Tamaño total del envío: " + fileSize + " bytes");

        Socket fileClient = fileSocket.accept();
        DataOutputStream dos = new DataOutputStream(fileClient.getOutputStream());
        DataInputStream dis = new DataInputStream(new FileInputStream(zipFile));

        dos.writeUTF(name);
        dos.writeLong(fileSize);

        long received = 0;
        int l = 0, percentage = 0;

        while (received < fileSize) {
            byte[] b = new byte[1500];
            l = dis.read(b);
            dos.write(b, 0, l);
            dos.flush();
            received += l;
            percentage = (int) ((received * 100) / fileSize);
            System.out.println("Recibido: " + percentage + "%");
        }

        dos.writeUTF(name);
        dos.writeLong(fileSize);
        zipFile.delete();
    }
}

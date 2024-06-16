package com.ipn.mx;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Date;
import java.util.StringTokenizer;

public class Request {
    private final SocketChannel socketChannel;
    private final ByteBuffer buffer = ByteBuffer.allocate(50000);

    public Request(SocketChannel socketChannel) {
        this.socketChannel = socketChannel;
    }

    public void handle() {
        try {
            int bytesRead = socketChannel.read(buffer);
            if (bytesRead == -1) {
                socketChannel.close();
                return;
            }

            buffer.flip();
            byte[] bytes = new byte[buffer.remaining()];
            buffer.get(bytes);
            String request = new String(bytes, StandardCharsets.UTF_8);

            System.out.println("****************** Request: ");
            System.out.println(request);
            System.out.println("****************************");

            String firstLine = getFirstLine(request);
            String methodType = getMethodType(firstLine);
            String resource = getResource(firstLine).getAbsolutePath();

            if (!firstLine.contains("?")) {
                switch (methodType) {
                    case "GET" -> handleGet(resource);
                    default -> sendBadRequest();
                }
            }

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void handleGet(String resource) {
        sendResource(resource);
    }

    public String getFirstLine(String request) {
        StringTokenizer st = new StringTokenizer(request, "\n");
        return st.nextToken();
    }

    public String getMethodType(String line) {
        StringTokenizer st = new StringTokenizer(line, " ");
        return st.nextToken();
    }

    public File getResource(String line) {
        int resourceRequestIndex = line.indexOf("/");
        int resourceNameIndex = line.indexOf(" ", resourceRequestIndex);
        String fileName = line.substring(resourceRequestIndex + 1, resourceNameIndex);
        if (fileName.isEmpty() || fileName.equals("index.html")) {
            return new File("server/index.html");
        }
        return new File("server/" + fileName);
    }

    public void sendResource(String resource) {
        try {
            File fileResource = new File(resource);
            if (!fileResource.exists()) {
                sendNotFound();
                return;
            }
            byte[] fileBytes = Files.readAllBytes(fileResource.toPath());
            String responseHeader = getResponseHeaderFromFile(fileResource);
            ByteBuffer responseBuffer = ByteBuffer.allocate(responseHeader.length() + fileBytes.length);
            responseBuffer.put(responseHeader.getBytes(StandardCharsets.UTF_8));
            responseBuffer.put(fileBytes);
            responseBuffer.flip();
            while (responseBuffer.hasRemaining()) {
                socketChannel.write(responseBuffer);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public String getResponseHeaderFromFile(File resource) throws IOException {
        StringBuilder responseHeader = new StringBuilder();
        String contentType = Files.probeContentType(resource.toPath());
        responseHeader.append("HTTP/1.1 200 OK\n")
                .append("Date:").append(new Date()).append("\n")
                .append("Server: Java HTTP Server\n")
                .append("Content-Type: ").append(contentType).append("\n")
                .append("Content-Length: ").append(resource.length()).append("\n")
                .append("\n");
        return responseHeader.toString();
    }

    private void sendNotFound() {
        try {
            StringBuilder responseBody = new StringBuilder();
            responseBody.append("<html lang=\"es\">")
                    .append("<head>")
                    .append("<meta charset=\"UTF-8\">")
                    .append("<meta name=\"viewport\" content=\"width=device-width, user-scalable=no, initial-scale=1.0, maximum-scale=1.0, minimum-scale=1.0\">")
                    .append("<meta http-equiv=\"X-UA-Compatible\" content=\"ie=edge\">")
                    .append("<title>Java Server</title>")
                    .append("</head>")
                    .append("<body>")
                    .append("<h1>404 - Not Found</h1>")
                    .append("<p>El recurso solicitado no existe</p>")
                    .append("<a href=\"/\">Volver a inicio</a>")
                    .append("</body>")
                    .append("</html>\n");

            String responseHeader = "HTTP/1.1 404 Not Found\n" +
                    "Date:" + new Date() + "\n" +
                    "Server: Java HTTP Server\n" +
                    "Content-Type: text/html\n" +
                    "Content-Length: " + responseBody.length() + "\n" + "\n";
            ByteBuffer responseBuffer = ByteBuffer.allocate(responseHeader.length() + responseBody.length());
            responseBuffer.put(responseHeader.getBytes(StandardCharsets.UTF_8));
            responseBuffer.put(responseBody.toString().getBytes(StandardCharsets.UTF_8));
            responseBuffer.flip();
            while (responseBuffer.hasRemaining()) {
                socketChannel.write(responseBuffer);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void sendBadRequest() {
        try {
            StringBuilder responseBody = new StringBuilder();
            responseBody.append("<html lang=\"es\">")
                    .append("<head>")
                    .append("<meta charset=\"UTF-8\">")
                    .append("<meta name=\"viewport\" content=\"width=device-width, user-scalable=no, initial-scale=1.0, maximum-scale=1.0, minimum-scale=1.0\">")
                    .append("<meta http-equiv=\"X-UA-Compatible\" content=\"ie=edge\">")
                    .append("<title>Java Server</title>")
                    .append("</head>")
                    .append("<body>")
                    .append("<h1>400 - Bad Request</h1>")
                    .append("<p>La solicitud no es válida</p>")
                    .append("<a href=\"/\">Volver a inicio</a>")
                    .append("</body>")
                    .append("</html>\n");

            String responseHeader = "HTTP/1.1 400 Bad Request\n" +
                    "Date:" + new Date() + "\n" +
                    "Server: Java HTTP Server\n" +
                    "Content-Type: text/html\n" +
                    "Content-Length: " + responseBody.length() + "\n" + "\n";
            ByteBuffer responseBuffer = ByteBuffer.allocate(responseHeader.length() + responseBody.length());
            responseBuffer.put(responseHeader.getBytes(StandardCharsets.UTF_8));
            responseBuffer.put(responseBody.toString().getBytes(StandardCharsets.UTF_8));
            responseBuffer.flip();
            while (responseBuffer.hasRemaining()) {
                socketChannel.write(responseBuffer);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}

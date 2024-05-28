package com.ipn.mx;

import java.io.*;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Date;
import java.util.StringTokenizer;

public class Request extends Thread {
    private final Socket socket;

    public Request(Socket socket) {
        this.socket = socket;
    }

    @Override
    public void run() {
        try {
            DataInputStream dis = new DataInputStream(socket.getInputStream());
            byte[] buffer = new byte[50000];
            int t = dis.read(buffer);
            String request = new String(buffer, 0, t);

            System.out.println("****************** Request: ");
            System.out.println(request);
            System.out.println("****************************");

            String firstLine = getFirstLine(request);
            String methodType = getMethodType(firstLine);
            String body = getBodyIfExists(request);
            String resource = getResource(firstLine).getAbsolutePath();
            if (!firstLine.contains("?")) {
                switch (methodType) {
                    case "GET" -> handleGet(resource);
                    case "HEAD" -> handleHead(resource);
                    case "POST" -> handlePost(body);
                    case "PUT" -> handlePut(buffer, request, resource);
                    default -> sendBadRequest();
                }
            }


        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void handlePut(byte[] buffer, String request, String resource) {
        try {
            // Get the body based on request
            int headerEndIndex = request.indexOf("\r\n\r\n");
            byte[] body = Arrays.copyOfRange(buffer, headerEndIndex + 4, buffer.length);
            Path path = Paths.get(resource);
            Files.write(path, body);
            sendResource(resource);
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
        int resourceRequestIndex, resourceNameIndex;
        resourceRequestIndex = line.indexOf("/");
        resourceNameIndex = line.indexOf(" ", resourceRequestIndex);
        String fileName = line.substring(resourceRequestIndex + 1, resourceNameIndex);
        if (fileName.isEmpty() || fileName.equals("index.html")) {
            return new File("server/index.html");
        }
        return new File("server/" + fileName);
    }

    public String getBodyIfExists(String request) {
        int headerEndIndex = request.indexOf("\r\n\r\n");
        if (headerEndIndex > -1) {
            return request.substring(headerEndIndex + 4).trim(); // +4 to skip the \r\n\r\n
        } else {
            return null; // Body not found
        }
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
            OutputStream os = socket.getOutputStream();
            os.write(responseHeader.getBytes());
            os.write(fileBytes);
            os.flush();
            os.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public String getResponseHeaderFromFile(File resource) throws IOException {
        StringBuilder responseHeader = new StringBuilder();
        String contentType = Files.probeContentType(resource.toPath());
        responseHeader.append("HTTP/1.1 200 OK\n").append("Date:").append(new Date()).append("\n").append("Server: Java HTTP Server\n").append("Content-Type: ").append(contentType).append("\n").append("Content-Length: ").append(resource.length()).append("\n").append("\n");
        return responseHeader.toString();
    }

    private void handleHead(String resource) throws IOException {
        DataOutputStream dos = new DataOutputStream(socket.getOutputStream());
        File fileResource = new File(resource);
        if (!fileResource.exists()) {
            sendNotFound();
            return;
        }
        dos.write(getResponseHeaderFromFile(fileResource).getBytes());
    }

    private void handlePost(String body) {
        if (body == null || body.isEmpty()) {
            sendBadRequest();
            return;
        }
        sendResource("server/" + body);
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
                    "Content-Type: " + "text/html" + "\n" +
                    "Content-Length: " + responseBody.length() + "\n" + "\n";
            DataOutputStream dos = new DataOutputStream(socket.getOutputStream());
            dos.write(responseHeader.getBytes());
            dos.write(responseBody.toString().getBytes());
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
                    "Content-Type: " + "text/html" + "\n" +
                    "Content-Length: " + responseBody.length() + "\n" + "\n";
            DataOutputStream dos = new DataOutputStream(socket.getOutputStream());
            dos.write(responseHeader.getBytes());
            dos.write(responseBody.toString().getBytes());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}

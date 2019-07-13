package com.dz.httpserver;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.time.LocalDateTime;
import java.util.StringTokenizer;

public class JHttpServer implements Runnable{

    static final File WEB_ROOT = new File(".");
    static final String DEFAULT_FILE = "index.html";
    static final String FILE_NOT_FOUND = "404.html";
    static final String METHOD_NOT_SUPPORTED = "not_supported.html";
    private static final Logger logger = LoggerFactory.getLogger(JHttpServer.class);

    static final int PORT = 8080;

    static final boolean verbose = true;

    private Socket connect;

    public JHttpServer(Socket connect) {
        this.connect = connect;
    }

    public static void main(String[] args){

        try {
            ServerSocket serverSocket = new ServerSocket(PORT);
            System.out.println("Server is up running\nListening on port : " + PORT);
            while(true){
                JHttpServer jHttpServer = new JHttpServer(serverSocket.accept());
                logger.info("Connection open. Date : {}", LocalDateTime.now());

                Thread t = new Thread(jHttpServer);
                t.start();
            }
        }catch (IOException e) {
            logger.error("An error occurred while creating connection : {}", e.getMessage());
        }
    }

    @Override
    public void run() {

        String fileRequested = null;
        try(BufferedReader in = new BufferedReader(new InputStreamReader(connect.getInputStream()));
            PrintWriter out = new PrintWriter(connect.getOutputStream());
            BufferedOutputStream dataOut = new BufferedOutputStream(connect.getOutputStream())) {

            String input = in.readLine();
            StringTokenizer tokenizer = new StringTokenizer(input);
            String method = tokenizer.nextToken().toUpperCase();
            fileRequested = tokenizer.nextToken().toLowerCase();

            if (!method.equals("GET") && !method.equals("HEAD")) {
                logger.info("501 : Not implemented : {}", method);
                File file = new File(WEB_ROOT, METHOD_NOT_SUPPORTED);
                int fileLength = (int) file.length();
                String contentMimeType = "text/html";
                fileNotFound(out,dataOut,fileRequested);
            }
        }catch (IOException e){
            logger.error("IO EXception : {}", e.getMessage());
        }
    }

    private byte[] readFile(File file, int fileLength) throws IOException{
        byte[] fileData =  new byte[fileLength];
        try(FileInputStream in = new FileInputStream(file)){
            in.read(fileData);
        }
        return fileData;
    }

    private String getContentType(String requestedFile){
        if(requestedFile.endsWith(".htm") || requestedFile.endsWith(".html")){
            return "text/html";
        }else{
            return "text/plain";
        }
    }

    private void fileNotFound(PrintWriter out, OutputStream dataOut, String requestedFile) throws IOException{
        File file = new File(WEB_ROOT, FILE_NOT_FOUND);
        int fileLength = (int) file.length();
        String content = "text/html";
        byte[] fileData = readFile(file, fileLength);

        out.println("HTTP/1.1 404 File Not Found");
        out.println("Server: DZ Http Server V1.0");
        out.println("Date: " + LocalDateTime.now());
        out.println("Content-Type: " + content);
        out.println("Content-Length: " + fileLength);
        out.println();

        dataOut.write(fileData, 0, fileLength);
        dataOut.flush();

        logger.info("File Not Found: {}" + requestedFile);
    }
}

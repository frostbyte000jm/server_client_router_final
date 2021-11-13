package com.threads;

import java.io.*;
import java.net.Socket;

import com.Main.TCPRouter;
import com.classes.MachineContainer;
import com.staticFields.settingsForRouter;

public class RouterThread extends Thread {
    //declarations
    private DataInputStream dataInputStreamClient, dataInputStreamServer;
    private DataOutputStream dataOutputStreamClient, dataOutputStreamServer;
    private TCPRouter tcpRouter;

    public RouterThread(Socket clientSocket, TCPRouter tcpRouter) throws IOException, ClassNotFoundException {
        this.tcpRouter = tcpRouter;

        //Connect to Client
        System.out.println("Connection established.");

        //setup input and output stream
        dataOutputStreamClient = new DataOutputStream(clientSocket.getOutputStream());
        dataInputStreamClient = new DataInputStream(clientSocket.getInputStream());

        //handshake Client
        dataOutputStreamClient.writeUTF("Hello Client");
        String msgClient = dataInputStreamClient.readUTF();
        System.out.println("Client Said: "+msgClient);

        //connect to Server
        String serverIPAddress = tcpRouter.getServerIPAddress();
        int serverPortNum = tcpRouter.getPortNumServer();
        System.out.println("Attempting to connect to: "+serverIPAddress+" port: "+serverPortNum);
        Socket serverSocket = new Socket(serverIPAddress,serverPortNum);
        dataOutputStreamServer = new DataOutputStream(serverSocket.getOutputStream());
        dataInputStreamServer = new DataInputStream(serverSocket.getInputStream());

        //Wait for Server to ask who Client is
        String msgServer = dataInputStreamServer.readUTF();
        System.out.println("Server Said: "+ msgServer);
        dataOutputStreamClient.writeUTF("Server says: "+msgServer);
        msgClient = dataInputStreamClient.readUTF();
        System.out.println("Client Said: "+msgClient);
        dataOutputStreamServer.writeUTF(msgClient);
    }

    public void run(){
        // Loop that waits and interprets messages
        try {
            waitForMessages();
        } catch (IOException e) {
            System.out.println("Something went wrong with the Router.");
        }
    }

    private void waitForMessages() throws IOException {
        String msgClient, msgServer;
        boolean doRun = true;

        while (doRun){
            // wait for client to say ready
            msgClient = dataInputStreamClient.readUTF();
            System.out.println("Client says: "+msgClient);

            // Let server know you're ready
            dataOutputStreamServer.writeUTF("ready for action");
            System.out.println("Waiting for message from Server.");

            // message from server to know what kind of action to pass.
            msgServer = dataInputStreamServer.readUTF();
            System.out.println("Server: "+msgServer);
            if (msgServer.equals("message")) {
                sendMessage();
            } else if (msgServer.equals("good_bye")) {
                doRun = goodbye();
            } else if (msgServer.equals("request_reply")) {
                requestReply();
            } else if (msgServer.equals("retrieve_file")) {
                retrieveFile();
            } else if (msgServer.equals("sending_file")) {
                sendFile();
            } else if (msgServer.equals("login_info")) {
                loginInfo();
            }
        }
    }

    private void loginInfo() throws IOException {
        // tell client to send login info
        dataOutputStreamClient.writeUTF("login_info");

        //receive login info
        String machineInfo = dataInputStreamClient.readUTF();
        System.out.println("Client: "+machineInfo);

        //pass login info
        dataOutputStreamServer.writeUTF(machineInfo);
    }

    private void retrieveFile() throws IOException {
        // start timer
        long timeStart = System.currentTimeMillis();

        // Tell Client that the Server wants to send a message, and let the server know when ready.
        dataOutputStreamClient.writeUTF("retrieve_file");

        // receive the File Name bytes
        int fileNameBytesLength = dataInputStreamClient.readInt();
        byte[] fileNameBytes = new byte[fileNameBytesLength];
        dataInputStreamClient.readFully(fileNameBytes,0,fileNameBytesLength);

        // receive the File
        int fileBytesLength = dataInputStreamClient.readInt();
        byte[] fileBytes = new byte[fileBytesLength];
        dataInputStreamClient.readFully(fileBytes,0,fileBytesLength);

        // send file name to Server
        dataOutputStreamServer.writeInt(fileNameBytesLength);
        dataOutputStreamServer.write(fileNameBytes);

        //send file to Server
        dataOutputStreamServer.writeInt(fileBytesLength);
        dataOutputStreamServer.write(fileBytes);

        //stop timer
        long timeEnd = System.currentTimeMillis();
        long timeDisplay = timeEnd - timeStart;
        System.out.println("Time to read file from client and pass to server: "+timeDisplay);
    }

    private void sendFile() throws IOException {
        //declarations
        String msgClient;

        // start timer
        long timeStart = System.currentTimeMillis();

        // tell client to get ready to receive a file.
        dataOutputStreamClient.writeUTF("sending_file");
        msgClient = dataInputStreamClient.readUTF();
        System.out.println("Client Said: "+ msgClient);
        dataOutputStreamServer.writeUTF(msgClient);

        // receive the File Name bytes
        int fileNameBytesLength = dataInputStreamServer.readInt();
        byte[] fileNameBytes = new byte[fileNameBytesLength];
        dataInputStreamServer.readFully(fileNameBytes,0,fileNameBytesLength);

        // receive the File
        int fileBytesLength = dataInputStreamServer.readInt();
        byte[] fileBytes = new byte[fileBytesLength];
        dataInputStreamServer.readFully(fileBytes,0,fileBytesLength);

        // send file name to Server
        dataOutputStreamClient.writeInt(fileNameBytesLength);
        dataOutputStreamClient.write(fileNameBytes);

        //send file to Server
        dataOutputStreamClient.writeInt(fileBytesLength);
        dataOutputStreamClient.write(fileBytes);

        //stop timer
        long timeEnd = System.currentTimeMillis();
        long timeDisplay = timeEnd - timeStart;
        System.out.println("Time to read file from server and pass to client: "+timeDisplay);
    }

    private boolean goodbye() throws IOException {
        // End router loop and tell client to disconnect.
        dataOutputStreamClient.writeUTF("good_bye");
        return false;
    }

    private void sendMessage() throws IOException {
        //declarations
        String msgClient, msgServer;

        // Tell Client that the Server wants to send a message, and let the server know when ready.
        dataOutputStreamClient.writeUTF("message");
        msgClient = dataInputStreamClient.readUTF();
        System.out.println("Client Said: "+ msgClient);
        dataOutputStreamServer.writeUTF(msgClient);

        // Pass Message to Client
        msgServer = dataInputStreamServer.readUTF();
        dataOutputStreamClient.writeUTF(msgServer);
    }

    private void requestReply() throws IOException {
        //declarations
        String msgClient;

        // Server will request the router to wait for a reply.
        dataOutputStreamClient.writeUTF("request_reply");
        msgClient = dataInputStreamClient.readUTF();
        dataOutputStreamServer.writeUTF(msgClient);
    }
}

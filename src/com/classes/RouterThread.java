package com.classes;

import com.staticLoc.settingsForRouter;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

public class RouterThread extends Thread{
    //declarations
    private DataInputStream dataInputStreamClient, dataInputStreamServer;
    private DataOutputStream dataOutputStreamClient, dataOutputStreamServer;
    Boolean doRun = true;

    public RouterThread(Socket socketClient) throws IOException {
        // declarations
        String msgClient, msgServer;

        // Connect to Client
        System.out.println("Connection established.");
        dataOutputStreamClient = new DataOutputStream(socketClient.getOutputStream());
        dataInputStreamClient = new DataInputStream(socketClient.getInputStream());
        msgClient = dataInputStreamClient.readUTF();
        System.out.println("Client Said: "+msgClient);
        dataOutputStreamClient.writeUTF("Hello Client");

        // Connect to Server
        String serverIPAddress = settingsForRouter.getServerIpAddress();
        int serverPortNum = settingsForRouter.getServerPortNum();
        Socket socket = new Socket(serverIPAddress,serverPortNum);
        dataOutputStreamServer = new DataOutputStream(socket.getOutputStream());
        dataInputStreamServer = new DataInputStream(socket.getInputStream());
        dataOutputStreamServer.writeUTF("Hello Server");
        msgServer = dataInputStreamServer.readUTF();
        System.out.println("Server Said: "+ msgServer);

        // Let Client know Server is Ready.
        dataOutputStreamClient.writeUTF("Server says Hello");


        // Wait for Server to make the next move.
        waitForMessageFormServer();

        // close socket
        socket.close();

        return;
    }

    private void waitForMessageFormServer() throws IOException {
        // Loop that waits and interprets messages
        String msgClient, msgServer;

        while (doRun){
            // wait for client to say ready
            msgClient = dataInputStreamClient.readUTF();
            System.out.println("Client says: "+msgClient);

            // Let server know you're ready
            dataOutputStreamServer.writeUTF("ready for action");
            System.out.println("Waiting for message from Server.");

            // message from server to know what kind of action to pass.
            msgServer = dataInputStreamServer.readUTF();
            if (msgServer.equals("message")) {
                sendMessage();
            } else if (msgServer.equals("good_bye")) {
                goodbye();
            } else if (msgServer.equals("request_reply")) {
                requestReply();
            } else if (msgServer.equals("retrieve_file")) {
                retrieveFile();
            } else if (msgServer.equals("sending_file")) {
                sendFile();
            }
        }
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

    private void goodbye() throws IOException {
        // End router loop and tell client to disconnect.
        doRun = false;
        dataOutputStreamClient.writeUTF("good_bye");
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

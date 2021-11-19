package com.threads;

import java.io.*;
import java.net.Socket;

import com.Main.TCPRouter;

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
        String msgServer = dataInputStreamServer.readUTF();  //Hello Who is Calling?
        System.out.println("Server Said: "+ msgServer);
        dataOutputStreamClient.writeUTF("Server says: "+msgServer); //Hello Who is Calling?
        msgClient = dataInputStreamClient.readUTF();    //__Client__
        System.out.println("Client Said: "+msgClient);
        dataOutputStreamServer.writeUTF(msgClient);     //__Client__
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

        while (doRun) {
            // wait for client to say ready
            msgClient = dataInputStreamClient.readUTF();    //Waiting for action
            System.out.println("Client says: " + msgClient);

            // Let server know you're ready
            dataOutputStreamServer.writeUTF("Client says: "+msgClient);
            System.out.println("Waiting for message from Server.");

            // message from server to know what kind of action to pass.
            msgServer = dataInputStreamServer.readUTF();            //First message: login_info
            System.out.println("Server: " + msgServer);
            if (msgServer.equals("login_info")) {
                loginInfo(msgServer);
            }else if (msgServer.equals("message_server_to_client")) {
                sendMessage(msgServer);
            } else if (msgServer.equals("request_reply_client_to_server")) {
                requestReply(msgServer);
            } else if (msgServer.equals("file_client_to_server")) {
                retrieveFile(msgServer);
            } else if (msgServer.equals("file_server_to_client")) {
                sendFile(msgServer);
            } else if (msgServer.equals("client_chat_room")) {
                chatRoomStart(msgServer);
            } else if (msgServer.equals("good_bye")) {
                doRun = goodbye(msgServer);
            }
        }
    }

    private void loginInfo(String action) throws IOException {
        //declarations
        String msgClient, msgServer;

        // tell client to send login info
        dataOutputStreamClient.writeUTF(action);    //login_info

        //receive login info
        msgClient = dataInputStreamClient.readUTF();       //Machine Info
        System.out.println("Client: "+msgClient);

        //pass login info to server
        dataOutputStreamServer.writeUTF(msgClient);   //Client Machine Info

        // wait for confirmation
        msgServer = dataInputStreamServer.readUTF();
        System.out.println("loginInfo - Expected: login_info_received - Server: "+msgServer);
        dataOutputStreamClient.writeUTF(msgServer);
    }

    //  message_server_to_client
    private void sendMessage(String action) throws IOException {
        //declarations
        String msgClient, msgServer;

        // Tell Client that the Server wants to send a message, and let the server know when ready.
        dataOutputStreamClient.writeUTF(action);
        msgClient = dataInputStreamClient.readUTF();
        System.out.println("Client Said: "+ msgClient);
        dataOutputStreamServer.writeUTF(msgClient);

        // Pass Message to Client
        msgServer = dataInputStreamServer.readUTF();
        dataOutputStreamClient.writeUTF(msgServer);
    }

    //  request_reply_client_to_server
    private void requestReply(String action) throws IOException {
        //declarations
        String msgClient;

        // Server will request the router to wait for a reply.
        dataOutputStreamClient.writeUTF(action);
        msgClient = dataInputStreamClient.readUTF();
        dataOutputStreamServer.writeUTF(msgClient);
    }

    //  file_client_to_server
    private void retrieveFile(String action) throws IOException {
        // start timer
        long timeStart = System.currentTimeMillis();

        // Tell Client that the Server wants to send a message, and let the server know when ready.
        dataOutputStreamClient.writeUTF(action);

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

    //  file_server_to_client
    private void sendFile(String action) throws IOException {
        //declarations
        String msgClient;

        // start timer
        long timeStart = System.currentTimeMillis();

        // tell client to get ready to receive a file.
        dataOutputStreamClient.writeUTF(action);
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

    private void chatRoomStart(String action) throws IOException {
        //declarations
        String msgClient, msgServer;

        // Tell Client that the Server wants to send a message, and let the server know when ready.
        dataOutputStreamClient.writeUTF(action);
        msgClient = dataInputStreamClient.readUTF();
        System.out.println("chatRoomStart() - Expect: who with? "+ msgClient);
        dataOutputStreamServer.writeUTF(msgClient);
        msgServer = dataInputStreamServer.readUTF(); //chatrooms or chat_room_create
        dataOutputStreamClient.writeUTF(msgServer);

        //Let Server know when complete
        msgClient = dataInputStreamClient.readUTF();
        System.out.println("chatRoomStart() - Expect: Done "+ msgClient);
        dataOutputStreamServer.writeUTF(msgClient);
    }

    private boolean goodbye(String action) throws IOException {
        // End router loop and tell client to disconnect.
        dataOutputStreamClient.writeUTF(action);
        return false;
    }
}

package com.containers;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;

public class ClientContiner {
    //declarations
    private DataInputStream dataInputStreamClient, dataInputStreamServer;
    private DataOutputStream dataOutputStreamClient, dataOutputStreamServer;
    private String clientIP;

    public ClientContiner(Socket socketClient) throws IOException {
        // declarations
        String msgClient;

        // Connect to Client
        InetAddress test = socketClient.getInetAddress();
        System.out.println("test: "+test+" hostName: "+test.getHostName());
        clientIP = socketClient.getInetAddress().getHostAddress();
        System.out.println("Connection established.");
        dataOutputStreamClient = new DataOutputStream(socketClient.getOutputStream());
        dataInputStreamClient = new DataInputStream(socketClient.getInputStream());
        msgClient = dataInputStreamClient.readUTF();
        System.out.println("Client Said: "+msgClient);
        dataOutputStreamClient.writeUTF("Hello Client");
    }
}

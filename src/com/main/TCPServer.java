package com.main;

import com.classes.ServerThread;
import com.staticLoc.settingsForServer;
import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;

public class TCPServer {
    public static void main(String[] args) throws IOException {
        //declarations
        Socket routerSocket = null;
        ServerSocket serverSocket = null;
        int portNum = settingsForServer.getPortNum();
        String localHostName = "";
        Boolean doRun = true;

        // Who am I
        try{
            InetAddress addr = InetAddress.getLocalHost();
            String localIPAddress = addr.getHostAddress(); // Machine's IP Address
            localHostName = addr.getCanonicalHostName(); // Machine's Host Name
            System.out.println("Local Host Information\nHost Name: "+localHostName+"\nIP Address: "+localIPAddress);
        } catch (UnknownHostException uhe) {
            System.out.println("Something Majorly Wrong. I can't find your IP address or HostName.");
        }

        //set up ServerSocket and wait on port 5555
        try {
            serverSocket = new ServerSocket(portNum);
            System.out.println("Router: "+localHostName+" is listening on port: "+ portNum);
        } catch (IOException e) {
            System.err.println("Could not listen on port: "+ portNum +".");
            System.exit(1);
        }

        //loop to wait for connection and create Thread
        while(doRun){
            System.out.println("Waiting for Connection...");
            try {
                routerSocket = serverSocket.accept();
            } catch (IOException e) {
                doRun = false;
                System.err.println("Server is unable to accept a connection.");
                System.exit(1);
            }
            ServerThread serverThread = new ServerThread(routerSocket);
            serverThread.start();
        }

        //close connections
        routerSocket.close();
        serverSocket.close();
    }
}

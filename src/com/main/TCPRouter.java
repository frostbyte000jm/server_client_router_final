package com.main;

import com.classes.RouterThread;
import com.staticLoc.settingsForRouter;
import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;

public class TCPRouter {
    public static void main(String[] args) throws IOException {
        //declarations
        Socket clientSocket = null;
        ServerSocket serverSocket = null;
        int portNum = settingsForRouter.getPortNum();
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

        //set up ServerSocket and wait on port 5678
        try {
            serverSocket = new ServerSocket(portNum);
            System.out.println("Router: "+localHostName+" is listening on port: "+ portNum);
        } catch (IOException e) {
            System.err.println("Could not listen on port: "+ portNum +".");
            System.exit(1);
        }

        //loop to wait for connection and create Thread
        while(doRun){
            // Wait for Connection
            System.out.println("Waiting for Connection...");
            try {
                clientSocket = serverSocket.accept();
            } catch (IOException e) {
                doRun = false;
                System.err.println("Unable to accept a connection.");
                System.exit(1);
            }

            //Check if Thread count reached, connect to another Router
            int threadCount = java.lang.Thread.activeCount();
            System.out.println(threadCount);
            if (threadCount <= settingsForRouter.getThreadMax()) {
                // Once Received, send to Thread
                RouterThread routerThread = null;
                try {
                    routerThread = new RouterThread(clientSocket);
                } catch (IOException e) {
                    doRun = false;
                    System.err.println("Unable to establish Thread.");
                    System.exit(1);
                }
                routerThread.start();
            } else {
                System.out.println("too much!");
            }
        }

        //close connection
        clientSocket.close();
        serverSocket.close();
    }
}

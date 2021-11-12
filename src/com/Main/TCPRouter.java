package com.Main;

import com.classes.ProcessComputerInfo;
import com.staticFields.settingsForRouter;
import com.threads.RouterThread;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class TCPRouter {
    public static void main (String[] args) throws IOException, ClassNotFoundException {
        //Who am I?
        ProcessComputerInfo processComputerInfo = new ProcessComputerInfo();
        String message = processComputerInfo.getWhoAmI();
        System.out.println(message);

        new TCPRouter().waitForConnection(processComputerInfo);
    }

    public void waitForConnection(ProcessComputerInfo processComputerInfo) throws IOException, ClassNotFoundException {
        //declarations
        int portNum = settingsForRouter.getPortNum();
        ServerSocket serverSocket = null;
        Socket clientSocket = null;
        boolean doRun = true;

        while(doRun){
            //set up ServerSocket and wait on port 5678
            try {
                serverSocket = new ServerSocket(portNum);
                System.out.println("Router: "+ processComputerInfo.getLocalHostName()+" is listening on port: "+ portNum);
            } catch (IOException e) {
                System.err.println("Could not listen on port: "+ portNum +".");
                System.exit(1);
            }

            //loop to wait for connection and create Thread

            // Wait for Connection
            System.out.println("Waiting for Connection...");
            try {
                clientSocket = serverSocket.accept();
            } catch (IOException e) {
                doRun = false;
                System.err.println("Unable to accept a connection.");
                System.exit(1);
            }

            RouterThread routerThread = new RouterThread(clientSocket);
            routerThread.start();

            System.out.println("Thread Created");
        }
    }
}

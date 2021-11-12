package com.Main;

import com.classes.MachineContainer;
import com.classes.ProcessComputerInfo;
import com.threads.RouterThread;
import com.threads.ServerRouterThread;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class TCPServerRouter {
    //declarations
    MachineContainer machineContainer;

    public static void main (String[] args) throws IOException {
        // Set static Port Number
        int portNum = 5555;

        //Who am I?
        ProcessComputerInfo processComputerInfo = new ProcessComputerInfo();
        String message = processComputerInfo.getWhoAmI();
        System.out.println(message);

        //set a container with this info
        MachineContainer machineContainer = new MachineContainer();
        machineContainer.setLocalHostName(processComputerInfo.getLocalHostName());
        machineContainer.setLocalIPAddress(processComputerInfo.getLocalIPAddress());
        machineContainer.setExternalIPAddress(processComputerInfo.getExternalIP());
        machineContainer.setPortNum(portNum);

        // wait for call
        new TCPServerRouter().waitForConnection(machineContainer);
    }
    private void waitForConnection(MachineContainer machineContainer) throws IOException {
        //declarations
        this.machineContainer = machineContainer;
        int portNum = machineContainer.getPortNum();
        ServerSocket serverSocket = null;
        Socket socket = null;
        boolean doRun = true;

        while(doRun){
            //set up ServerSocket and wait on port ????
            try {
                serverSocket = new ServerSocket(portNum);
                System.out.println("Router: "+ machineContainer.getLocalHostName() +" is listening on port: "+ portNum);
            } catch (IOException e) {
                System.err.println("Could not listen on port: "+ portNum +".");
                System.exit(1);
            }

            // Wait for Connection
            System.out.println("Waiting for Connection...");
            try {
                socket = serverSocket.accept();
            } catch (IOException e) {
                doRun = false;
                System.err.println("Unable to accept a connection.");
                System.exit(1);
            }

            ServerRouterThread serverRouterThread = new ServerRouterThread(socket, this);
            RouterThread routerThread = new RouterThread(clientSocket,this);
            routerThread.start();

            System.out.println("Thread Created");
    }
}

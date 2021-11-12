package com.Main;

import com.classes.MachineContainer;
import com.classes.ProcessComputerInfo;
import com.staticFields.settingsForRouter;
import com.threads.RouterThread;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Scanner;

public class TCPRouter {
    //declaration
    private int portNum;
    private int portNumServer;
    private String serverIPAddress;
    private MachineContainer machineContainer;

    public static void main (String[] args) throws IOException, ClassNotFoundException {
        //Who am I?
        ProcessComputerInfo processComputerInfo = new ProcessComputerInfo();
        String message = processComputerInfo.getWhoAmI();
        System.out.println(message);

        //set a container with this info
        MachineContainer machineContainer = new MachineContainer();
        machineContainer.setLocalHostName(processComputerInfo.getLocalHostName());
        machineContainer.setLocalIPAddress(processComputerInfo.getLocalIPAddress());
        machineContainer.setExternalIPAddress(processComputerInfo.getExternalIP());
        machineContainer.setIsRouter(1);

        new TCPRouter().Login(machineContainer);
    }

    private void Login(MachineContainer machineContainer) throws IOException, ClassNotFoundException {
        //declarations
        Scanner scanner = new Scanner(System.in);

        //Setup Server
        System.out.println("Enter Router Name:");
        String username = scanner.nextLine();
        machineContainer.setUserName(username);
        System.out.println("Enter your Port Number:");
        int portNum;
        while (true) {
            try {
                portNum = Integer.parseInt(scanner.nextLine());
                break;
            } catch (NumberFormatException e) {
                System.out.println("Please enter your Port Number: ");
            }
        }
        machineContainer.setPortNum(portNum);
        System.out.println("Enter Server IP Address:");
        String serverIPAddress = scanner.nextLine();
        machineContainer.setServerIPAddress(serverIPAddress);
        System.out.println("Enter the Server Port Number:");
        int portNumServer;
        while (true) {
            try {
                portNumServer = Integer.parseInt(scanner.nextLine());
                break;
            } catch (NumberFormatException e) {
                System.out.println("Please enter your Port Number: ");
            }
        }
        machineContainer.setPortNumToCall(portNumServer);

        //set Globals
        this.portNum = portNum;
        this.serverIPAddress = serverIPAddress;
        this.portNumServer = portNumServer;
        this.machineContainer = machineContainer;

        waitForConnection();
    }

    public void waitForConnection() throws IOException, ClassNotFoundException {
        //declarations
        ServerSocket serverSocket = null;
        Socket clientSocket = null;
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
                clientSocket = serverSocket.accept();
            } catch (IOException e) {
                doRun = false;
                System.err.println("Unable to accept a connection.");
                System.exit(1);
            }

            RouterThread routerThread = new RouterThread(clientSocket,this);
            routerThread.start();

            System.out.println("Thread Created");
        }
    }

    public String getServerIPAddress() { return serverIPAddress; }
    public int getPortNumServer(){ return portNumServer; }
}

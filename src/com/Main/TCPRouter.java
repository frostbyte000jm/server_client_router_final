package com.Main;

import com.classes.MachineContainer;
import com.classes.ProcessComputerInfo;
import com.staticFields.SettingsForRouter;
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
        SettingsForRouter settingsForRouter = new SettingsForRouter();

        //  Router Name
        System.out.println("Enter Router Name (leave bank for "+settingsForRouter.getRouterName()+"):");
        String username = scanner.nextLine();
        if (username.length() == 0){
            username = settingsForRouter.getRouterName();
        } else {
            settingsForRouter.setRouterName(username);
            System.out.println("Updated Server Name in Settings.");
        }
        machineContainer.setUserName(username);

        //  Server Port Number
        System.out.println("Enter your Port Number (Leave Blank for "+settingsForRouter.getPortNum()+"):");
        int portNum;
        while (true) {
            try {
                String strPortNum = scanner.nextLine();
                if (strPortNum.length() == 0){
                    portNum = settingsForRouter.getPortNum();
                } else {
                    portNum = Integer.parseInt(strPortNum);
                    settingsForRouter.setPortNum(portNum);
                    System.out.println("Port Number has been updated in settings.\n" +
                            "Please Make sure this port number is valid");
                }
                break;
            } catch (NumberFormatException e) {
                System.out.println("Please enter your Port *Number*: ");
            }
        }
        machineContainer.setPortNum(portNum);

        //      Set IP Address of Server
        System.out.println("Router IP Address (Leave Blank for "+settingsForRouter.getServerIPAddress()+"):");
        String serverIPAddress = scanner.nextLine();
        if (serverIPAddress.length() == 0){
            serverIPAddress = settingsForRouter.getServerIPAddress();
        } else {
            settingsForRouter.setServerIPAddress(serverIPAddress);
            System.out.println("Router IP Address has been revised in Settings.");
        }
        machineContainer.setServerIPAddress(serverIPAddress);

        //      Set Port number of Server
        System.out.println("Port Number for Server (Leave Blank for "+settingsForRouter.getServerPortNum()+"):");
        int portNumServer;
        while (true){
            try {
                String strPortNum = scanner.nextLine();
                if (strPortNum.length() == 0) {
                    portNumServer = settingsForRouter.getServerPortNum();
                } else {
                    portNumServer = Integer.parseInt(strPortNum);
                    settingsForRouter.setServerPortNum(portNumServer);
                    System.out.println("Server Router Port Number has been updated in settings.\n" +
                            "Please Make sure this port number is valid");
                }
                break;
            } catch (NumberFormatException e){
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
                serverSocket.close();
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
